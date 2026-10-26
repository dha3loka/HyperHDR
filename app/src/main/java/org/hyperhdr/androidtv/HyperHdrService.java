package org.hyperhdr.androidtv;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import org.json.JSONObject;

public class HyperHdrService extends Service {

    public static final String ACTION_START_SERVER = "org.hyperhdr.androidtv.ACTION_START_SERVER";
    public static final String ACTION_START_CAPTURE = "org.hyperhdr.androidtv.ACTION_START_CAPTURE";
    public static final String ACTION_STOP_CAPTURE = "org.hyperhdr.androidtv.ACTION_STOP_CAPTURE";
    public static final String EXTRA_RESULT_CODE = "result_code";
    public static final String EXTRA_RESULT_DATA = "result_data";

    private static final String CHANNEL_ID = "hyperhdr_server_channel";
    private static final int NOTIF_ID = 2400;

    private static volatile HyperHdrService instance;

    private WledDriver wledDriver;
    private InfiniteColorEngine colorEngine;
    private ScreenCaptureManager screenCaptureManager;
    private HyperHdrServer server;

    public static HyperHdrService getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        createNotificationChannel();
        promoteToForeground(false);

        wledDriver = new WledDriver();
        colorEngine = new InfiniteColorEngine(wledDriver);
        screenCaptureManager = new ScreenCaptureManager(this, colorEngine);
        server = new HyperHdrServer(this, colorEngine, wledDriver, screenCaptureManager);
        server.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if (ACTION_START_CAPTURE.equals(action)) {
                int resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0);
                Intent data = intent.getParcelableExtra(EXTRA_RESULT_DATA);
                promoteToForeground(true);
                if (data != null) {
                    boolean ok = screenCaptureManager.startCapture(resultCode, data);
                    server.appendLog(ok ? "INFO" : "ERROR", "GRABBER",
                            ok ? "Android TV MediaProjection screen capture started (192x108 low-latency RGBA)"
                               : "Failed to start MediaProjection screen capture");
                    promoteToForeground(ok);
                }
            } else if (ACTION_STOP_CAPTURE.equals(action)) {
                screenCaptureManager.stopCapture();
                server.appendLog("INFO", "GRABBER", "Android TV MediaProjection screen capture stopped");
                promoteToForeground(false);
            }
        }
        return START_STICKY;
    }

    private void promoteToForeground(boolean withMediaProjectionType) {
        Notification notification = buildNotification();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int type = withMediaProjectionType
                    ? ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                    : ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE;
            try {
                startForeground(NOTIF_ID, notification, type);
            } catch (Exception e) {
                startForeground(NOTIF_ID, notification);
            }
        } else {
            startForeground(NOTIF_ID, notification);
        }
    }

    private Notification buildNotification() {
        String ip = WledDriver.getLocalIpv4Address();
        int port = server != null ? server.getHttpPort() : 8090;
        boolean cap = screenCaptureManager != null && screenCaptureManager.isCapturing();

        Intent openIntent = new Intent(this, MainActivity.class);
        int piFlags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0;
        PendingIntent pi = PendingIntent.getActivity(this, 0, openIntent, piFlags);

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);

        builder.setContentTitle("HyperHDR v24.0.0-beta.0 Server (" + ip + ":" + port + ")")
                .setContentText((cap ? "Screen Capture: ACTIVE | " : "Screen Capture: Idle | ")
                        + "WLED: " + (wledDriver != null ? wledDriver.getHost() : ""))
                .setSmallIcon(R.drawable.app_icon)
                .setContentIntent(pi)
                .setOngoing(true);

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm != null) {
                NotificationChannel ch = new NotificationChannel(
                        CHANNEL_ID,
                        "HyperHDR v24.0.0-beta.0 Server",
                        NotificationManager.IMPORTANCE_LOW
                );
                ch.setDescription("Runs the HyperHDR v24.0.0-beta.0 Web UI (:8090), Screen Grabber, and WLED driver");
                nm.createNotificationChannel(ch);
            }
        }
    }

    public WledDriver getWledDriver() {
        return wledDriver;
    }

    public InfiniteColorEngine getColorEngine() {
        return colorEngine;
    }

    public ScreenCaptureManager getScreenCaptureManager() {
        return screenCaptureManager;
    }

    public HyperHdrServer getServer() {
        return server;
    }

    public void updateWledHostQuick(String newHost) {
        if (newHost == null || newHost.trim().isEmpty()) return;
        try {
            JSONObject cfg = server.getActiveInstanceConfig();
            JSONObject dev = cfg.optJSONObject("device");
            if (dev == null) {
                dev = new JSONObject();
                cfg.put("device", dev);
            }
            dev.put("type", "wled");
            dev.put("host", newHost.trim());
            colorEngine.applyConfig(cfg);
            server.appendLog("INFO", "WLED", "Updated WLED target host to " + newHost.trim());
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onDestroy() {
        if (screenCaptureManager != null) screenCaptureManager.stopCapture();
        if (server != null) server.stop();
        instance = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
