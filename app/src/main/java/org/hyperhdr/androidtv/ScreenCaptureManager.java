package org.hyperhdr.androidtv;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Shader;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.HandlerThread;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScreenCaptureManager {

    public interface FramePreviewListener {
        void onPreviewJpegFrame(byte[] jpegBytes);
    }

    private final Context context;
    private final InfiniteColorEngine colorEngine;
    private FramePreviewListener previewListener;

    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    private HandlerThread captureThread;
    private Handler captureHandler;

    private final ScheduledExecutorService fallbackScheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> fallbackFuture;

    private volatile boolean capturing = false;
    private volatile String activeCaptureMode = "Idle";
    private volatile boolean imageStreamRequested = false;
    private volatile int captureWidth = 192;
    private volatile int captureHeight = 108;
    private volatile int targetFps = 30;
    private volatile int cropLeft = 0, cropRight = 0, cropTop = 0, cropBottom = 0;

    private volatile double currentCaptureFps = 0.0;
    private volatile long lastLatencyMs = 2;
    private volatile long lastRealFrameAtMs = 0;
    private volatile byte[] latestPreviewJpeg = null;
    private long framesInWindow = 0;
    private long windowStartMs = System.currentTimeMillis();
    private long lastFrameProcessedMs = 0;
    private long lastPreviewSentMs = 0;

    public ScreenCaptureManager(Context context, InfiniteColorEngine colorEngine) {
        this.context = context.getApplicationContext();
        this.colorEngine = colorEngine;
        startFallbackGrabberLoop();
    }

    public void setPreviewListener(FramePreviewListener listener) {
        this.previewListener = listener;
    }

    public void setImageStreamRequested(boolean requested) {
        this.imageStreamRequested = requested;
        if (requested && previewListener != null) {
            byte[] jpeg = latestPreviewJpeg;
            if (jpeg == null) {
                jpeg = generateFallbackPreviewJpeg(null);
            }
            if (jpeg != null) {
                previewListener.onPreviewJpegFrame(jpeg);
            }
        }
    }

    public synchronized void applyConfig(JSONObject rootConfig) {
        if (rootConfig == null) return;
        JSONObject sysGrab = rootConfig.optJSONObject("systemGrabber");
        if (sysGrab != null) {
            int cfgFps = sysGrab.optInt("fps", 30);
            this.targetFps = Math.max(5, Math.min(60, cfgFps));
            this.cropLeft = Math.max(0, sysGrab.optInt("cropLeft", 0));
            this.cropRight = Math.max(0, sysGrab.optInt("cropRight", 0));
            this.cropTop = Math.max(0, sysGrab.optInt("cropTop", 0));
            this.cropBottom = Math.max(0, sysGrab.optInt("cropBottom", 0));
        }
    }

    public boolean isCapturing() {
        if (capturing && mediaProjection != null) {
            return true;
        }
        return (System.currentTimeMillis() - lastRealFrameAtMs) < 3500L;
    }

    public String getActiveCaptureMode() {
        return activeCaptureMode;
    }

    public double getCurrentCaptureFps() {
        return isCapturing() ? currentCaptureFps : colorEngine.getSmoothingFps();
    }

    public long getLastLatencyMs() {
        return lastLatencyMs;
    }

    public byte[] getLatestPreviewJpeg() {
        return latestPreviewJpeg;
    }

    public synchronized boolean startCapture(int resultCode, Intent data) {
        stopMediaProjectionOnly();
        try {
            MediaProjectionManager mpm = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            if (mpm == null || data == null) return false;

            mediaProjection = mpm.getMediaProjection(resultCode, data);
            if (mediaProjection == null) return false;

            captureThread = new HandlerThread("HyperHdrScreenCapture");
            captureThread.start();
            captureHandler = new Handler(captureThread.getLooper());

            mediaProjection.registerCallback(new MediaProjection.Callback() {
                @Override
                public void onStop() {
                    stopMediaProjectionOnly();
                }
            }, captureHandler);

            imageReader = ImageReader.newInstance(captureWidth, captureHeight, PixelFormat.RGBA_8888, 3);
            imageReader.setOnImageAvailableListener(this::onImageAvailable, captureHandler);

            int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
                    | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
                    | DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION;
            try {
                virtualDisplay = mediaProjection.createVirtualDisplay(
                        "HyperHDR-ScreenGrabber",
                        captureWidth,
                        captureHeight,
                        160,
                        flags,
                        imageReader.getSurface(),
                        null,
                        captureHandler
                );
            } catch (Exception fallbackFlags) {
                virtualDisplay = mediaProjection.createVirtualDisplay(
                        "HyperHDR-ScreenGrabber",
                        captureWidth,
                        captureHeight,
                        160,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                        imageReader.getSurface(),
                        null,
                        captureHandler
                );
            }

            if (virtualDisplay == null) {
                stopMediaProjectionOnly();
                return false;
            }

            capturing = true;
            activeCaptureMode = "MediaProjection";
            windowStartMs = System.currentTimeMillis();
            framesInWindow = 0;
            return true;
        } catch (Exception e) {
            stopMediaProjectionOnly();
            return false;
        }
    }

    private synchronized void stopMediaProjectionOnly() {
        capturing = false;
        if (virtualDisplay != null) {
            try { virtualDisplay.release(); } catch (Exception ignored) {}
            virtualDisplay = null;
        }
        if (imageReader != null) {
            try { imageReader.close(); } catch (Exception ignored) {}
            imageReader = null;
        }
        if (mediaProjection != null) {
            try { mediaProjection.stop(); } catch (Exception ignored) {}
            mediaProjection = null;
        }
        if (captureThread != null) {
            try { captureThread.quitSafely(); } catch (Exception ignored) {}
            captureThread = null;
            captureHandler = null;
        }
    }

    public synchronized void stopCapture() {
        stopMediaProjectionOnly();
        activeCaptureMode = "Idle";
        lastRealFrameAtMs = 0;
        latestPreviewJpeg = null;
        colorEngine.updateScreenFrame(null, 0, 0);
        currentCaptureFps = 0.0;
    }

    private void startFallbackGrabberLoop() {
        if (fallbackFuture != null) {
            fallbackFuture.cancel(false);
        }
        fallbackFuture = fallbackScheduler.scheduleWithFixedDelay(this::pollFallbackTick, 400, 120, TimeUnit.MILLISECONDS);
    }

    private void pollFallbackTick() {
        try {
            if (!colorEngine.getComponentState("ALL") || !colorEngine.getComponentState("GRABBER") || !colorEngine.getComponentState("SYSTEMGRABBER")) {
                return;
            }
            long now = System.currentTimeMillis();
            // If MediaProjection is active and recently delivered frames, no fallback needed
            if (capturing && mediaProjection != null && (now - lastRealFrameAtMs) < 1500L) {
                return;
            }
            // Try shell screencap raw RGBA fallback if MediaProjection is not running or has stalled
            if (tryScreencapRawFrame()) {
                return;
            }
        } catch (Exception ignored) {
        }
    }

    private boolean tryScreencapRawFrame() {
        Process proc = null;
        try {
            long t0 = System.nanoTime();
            proc = new ProcessBuilder("screencap").start();
            InputStream is = proc.getInputStream();

            byte[] hdr = readExact(is, 12);
            if (hdr == null) {
                proc.destroy();
                return false;
            }
            ByteBuffer bb = ByteBuffer.wrap(hdr).order(ByteOrder.LITTLE_ENDIAN);
            int srcW = bb.getInt();
            int srcH = bb.getInt();
            int fmt = bb.getInt();
            if (srcW <= 0 || srcH <= 0 || srcW > 7680 || srcH > 4320) {
                // Android 11+ may have 16-byte header (width, height, format, dataspace)
                byte[] extra4 = readExact(is, 4);
                if (extra4 == null || srcW <= 0 || srcH <= 0 || srcW > 7680 || srcH > 4320) {
                    proc.destroy();
                    return false;
                }
            } else {
                // Peek if 4th int is dataspace when total size matches 16 + w*h*4
                // For standard RGBA_8888 (fmt == 1), bytes per pixel is 4
            }

            int dstW = captureWidth;
            int dstH = captureHeight;
            int[] argb = new int[dstW * dstH];
            int rowBytesLen = srcW * 4;
            byte[] rowBuf = new byte[rowBytesLen];

            int nextTargetY = 0;
            int dstY = 0;
            for (int sy = 0; sy < srcH; sy++) {
                int r = readUpTo(is, rowBuf, rowBytesLen);
                if (r < rowBytesLen) break;
                if (sy >= nextTargetY && dstY < dstH) {
                    int outRowOff = dstY * dstW;
                    for (int dx = 0; dx < dstW; dx++) {
                        int sx = (dx * srcW) / dstW;
                        int bOff = sx * 4;
                        int red = rowBuf[bOff] & 0xFF;
                        int green = rowBuf[bOff + 1] & 0xFF;
                        int blue = rowBuf[bOff + 2] & 0xFF;
                        argb[outRowOff + dx] = 0xFF000000 | (red << 16) | (green << 8) | blue;
                    }
                    dstY++;
                    nextTargetY = (dstY * srcH) / dstH;
                }
            }
            proc.destroy();
            if (dstY < dstH / 2) {
                return false;
            }
            activeCaptureMode = "Android Screencap";
            onDecodedFrame(argb, dstW, dstH, t0);
            return true;
        } catch (Exception e) {
            if (proc != null) {
                try { proc.destroy(); } catch (Exception ignored) {}
            }
            return false;
        }
    }

    private static byte[] readExact(InputStream is, int len) throws Exception {
        byte[] buf = new byte[len];
        int off = 0;
        while (off < len) {
            int r = is.read(buf, off, len - off);
            if (r < 0) return null;
            off += r;
        }
        return buf;
    }

    private static int readUpTo(InputStream is, byte[] buf, int len) throws Exception {
        int off = 0;
        while (off < len) {
            int r = is.read(buf, off, len - off);
            if (r < 0) break;
            off += r;
        }
        return off;
    }

    public void injectExternalFrame(int[] argb, int w, int h, String sourceName) {
        if (argb == null || w <= 0 || h <= 0) return;
        activeCaptureMode = sourceName != null ? sourceName : "External Stream";
        onDecodedFrame(argb, w, h, System.nanoTime());
    }

    private void onImageAvailable(ImageReader reader) {
        Image image = null;
        try {
            image = reader.acquireLatestImage();
            if (image == null) return;

            long now = System.currentTimeMillis();
            long minInterval = Math.max(8L, 1000L / Math.max(5, targetFps));
            if (now - lastFrameProcessedMs < minInterval) {
                return;
            }
            long t0 = System.nanoTime();

            Image.Plane[] planes = image.getPlanes();
            if (planes == null || planes.length == 0) return;

            ByteBuffer buffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int w = image.getWidth();
            int h = image.getHeight();

            int cL = Math.min(w / 3, cropLeft);
            int cR = Math.min(w / 3, cropRight);
            int cT = Math.min(h / 3, cropTop);
            int cB = Math.min(h / 3, cropBottom);
            int outW = Math.max(16, w - cL - cR);
            int outH = Math.max(16, h - cT - cB);

            int[] argb = new int[outW * outH];
            byte[] rowBytes = new byte[rowStride];
            for (int y = 0; y < outH; y++) {
                int srcY = y + cT;
                buffer.position(srcY * rowStride);
                int toRead = Math.min(rowStride, buffer.remaining());
                buffer.get(rowBytes, 0, toRead);
                int rowOff = y * outW;
                for (int x = 0; x < outW; x++) {
                    int bOff = (x + cL) * pixelStride;
                    if (bOff + 3 < toRead) {
                        int r = rowBytes[bOff] & 0xFF;
                        int g = rowBytes[bOff + 1] & 0xFF;
                        int b = rowBytes[bOff + 2] & 0xFF;
                        argb[rowOff + x] = 0xFF000000 | (r << 16) | (g << 8) | b;
                    }
                }
            }

            activeCaptureMode = "MediaProjection";
            onDecodedFrame(argb, outW, outH, t0);
        } catch (Exception ignored) {
        } finally {
            if (image != null) {
                try { image.close(); } catch (Exception ignored) {}
            }
        }
    }

    private void onDecodedFrame(int[] argb, int w, int h, long t0Ns) {
        long now = System.currentTimeMillis();
        lastFrameProcessedMs = now;
        lastRealFrameAtMs = now;

        colorEngine.updateScreenFrame(argb, w, h);

        if (now - lastPreviewSentMs >= 100 || latestPreviewJpeg == null) {
            lastPreviewSentMs = now;
            try {
                Bitmap bmp = Bitmap.createBitmap(argb, w, h, Bitmap.Config.ARGB_8888);
                ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
                bmp.compress(Bitmap.CompressFormat.JPEG, 75, baos);
                bmp.recycle();
                byte[] jpeg = baos.toByteArray();
                latestPreviewJpeg = jpeg;
                if (imageStreamRequested && previewListener != null) {
                    previewListener.onPreviewJpegFrame(jpeg);
                }
            } catch (Exception ignored) {
            }
        }

        lastLatencyMs = Math.max(1L, (System.nanoTime() - t0Ns) / 1_000_000L);
        framesInWindow++;
        if (now - windowStartMs >= 1000) {
            currentCaptureFps = (framesInWindow * 1000.0) / (now - windowStartMs);
            framesInWindow = 0;
            windowStartMs = now;
        }
    }

    public byte[] generateFallbackPreviewJpeg(int[] currentLeds) {
        try {
            byte[] cached = latestPreviewJpeg;
            if (cached != null && isCapturing()) {
                return cached;
            }
            int w = 320, h = 180;
            Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

            // Render dynamic LED border preview around the canvas if LED colors are available
            p.setShader(new LinearGradient(0, 0, w, h,
                    new int[]{Color.rgb(18, 26, 42), Color.rgb(30, 48, 72)},
                    null, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, w, h, p);
            p.setShader(null);

            if (currentLeds != null && currentLeds.length >= 12) {
                int ledCount = currentLeds.length / 3;
                for (int i = 0; i < ledCount; i++) {
                    int r = currentLeds[i * 3];
                    int g = currentLeds[i * 3 + 1];
                    int b = currentLeds[i * 3 + 2];
                    p.setColor(Color.rgb(r, g, b));
                    float ratio = (float) i / ledCount;
                    if (ratio < 0.32f) {
                        float x = (ratio / 0.32f) * (w - 16);
                        canvas.drawRect(x, 0, x + 16, 14, p);
                    } else if (ratio < 0.50f) {
                        float y = ((ratio - 0.32f) / 0.18f) * (h - 16);
                        canvas.drawRect(w - 14, y, w, y + 16, p);
                    } else if (ratio < 0.82f) {
                        float x = (1.0f - ((ratio - 0.50f) / 0.32f)) * (w - 16);
                        canvas.drawRect(x, h - 14, x + 16, h, p);
                    } else {
                        float y = (1.0f - ((ratio - 0.82f) / 0.18f)) * (h - 16);
                        canvas.drawRect(0, y, 14, y + 16, p);
                    }
                }
            }

            p.setColor(Color.WHITE);
            p.setTextAlign(Paint.Align.CENTER);
            p.setTextSize(15f);
            canvas.drawText("HyperHDR v24.0.0-beta.0 — Android TV", w / 2f, h / 2f - 10f, p);
            p.setTextSize(12f);
            p.setColor(Color.rgb(180, 220, 255));
            canvas.drawText(isCapturing()
                    ? ("Live Capture Active (" + activeCaptureMode + ")")
                    : "Click 'Start TV Capture' in App Toolbar", w / 2f, h / 2f + 16f, p);

            ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
            bmp.compress(Bitmap.CompressFormat.JPEG, 75, baos);
            bmp.recycle();
            return baos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }
}
