package android.content;
import android.os.Parcelable;
public class Intent implements Parcelable {
    public static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    public Intent() {}
    public Intent(Context packageContext, Class<?> cls) {}
    public String getAction() { return null; }
    public Intent setAction(String action) { return this; }
    public int getIntExtra(String name, int defaultValue) { return 0; }
    public <T extends Parcelable> T getParcelableExtra(String name) { return null; }
    public Intent putExtra(String name, int value) { return this; }
    public Intent putExtra(String name, Parcelable value) { return this; }
}
