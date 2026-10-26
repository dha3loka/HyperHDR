package android.media;
import java.nio.ByteBuffer;
public abstract class Image implements AutoCloseable {
    public static abstract class Plane {
        public abstract int getRowStride();
        public abstract int getPixelStride();
        public abstract ByteBuffer getBuffer();
    }
    public abstract int getWidth();
    public abstract int getHeight();
    public abstract Plane[] getPlanes();
    public abstract void close();
}
