package org.gearvrf.utility;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ResourceReader {
    private static final int BUFFER_SIZE = 8192; /* bytes */

    public static byte[] readStream(InputStream stream) {
        ByteArrayOutputStream ostream = null;
        try {
            ostream = new ByteArrayOutputStream(stream.available());
            byte data[] = new byte[BUFFER_SIZE];
            int count;
            while ((count = stream.read(data)) != -1) {
                ostream.write(data, 0, count);
            }
            return ostream.toByteArray();
        } catch (IOException e) {
            return null;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }

            if (ostream != null) {
                try {
                    ostream.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
