package org.gearvrf.jassimpmodelloader;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.jassimp2.AiNode;
import org.gearvrf.jassimp2.GVRJassimpAdapter.INodeFactory;
import org.gearvrf.utility.FileNameUtils;
import org.gearvrf.utility.ImageUtils;
import org.gearvrf.utility.ResourceReader;

import android.graphics.Bitmap;

public class TGATextureLoader implements INodeFactory {
    @Override
    public GVRSceneObject createSceneObject(GVRContext ctx, AiNode node) {
        return null;
    }

    @Override
    public Future<GVRTexture> loadTexture(GVRContext ctx, GVRAndroidResource resource) {
        if (!FileNameUtils.getExtension(resource.getResourceFilename())
                .equalsIgnoreCase("tga")) {
            return null;
        }

        // Load the TGA file as a bitmap
        try {
            Bitmap bmp = TargaReader.getImage(resource.getStream());
            GVRTexture texture = new GVRBitmapTexture(ctx, bmp);
            return new FutureWrapper<GVRTexture>(texture);
        } catch (IOException e) {
            return null;
        }
    }
}

class TargaReader {
    public static Bitmap getImage(InputStream inStream) throws IOException {
        byte[] buf = ResourceReader.readStream(inStream);
        return decode(buf);
    }

    private static int offset;

    private static int btoi(byte b) {
        int a = b;
        return (a < 0 ? 256 + a : a);
    }

    private static int read(byte[] buf) {
        return btoi(buf[offset++]);
    }

    public static Bitmap decode(byte[] buf) throws IOException {
        offset = 0;

        // Reading header bytes
        // buf[2]=image type code 0x02=uncompressed BGR or BGRA
        // buf[12]+[13]=width
        // buf[14]+[15]=height
        // buf[16]=image pixel size 0x20=32bit, 0x18=24bit
        // buf{17]=Image Descriptor Byte=0x28 (00101000)=32bit/origin 
        //         upperleft/non-interleaved
        for (int i = 0; i < 12; i++)
            read(buf);
        int width = read(buf) + (read(buf) << 8);   // 00,04=1024
        int height = read(buf) + (read(buf) << 8);  // 40,02=576
        read(buf);
        read(buf);

        int n = width * height;
        int[] pixels = new int[n];
        int idx = 0;

        if (buf[2] == 0x02 && buf[16] == 0x20) { // uncompressed BGRA
            while (n > 0) {
                int b = read(buf);
                int g = read(buf);
                int r = read(buf);
                int a = read(buf);
                int v = (a << 24) | (r << 16) | (g << 8) | b;
                pixels[idx++] = v;
                n -= 1;
            }
        } else if (buf[2] == 0x02 && buf[16] == 0x18) {  // uncompressed BGR
            while (n > 0) {
                int b = read(buf);
                int g = read(buf);
                int r = read(buf);
                int a = 255; // opaque pixel
                int v = (a << 24) | (r << 16) | (g << 8) | b;
                pixels[idx++] = v;
                n -= 1;
            }
        } else {
            // RLE compressed
            while (n > 0) {
                int nb = read(buf); // num of pixels
                if ((nb & 0x80) == 0) { // 0x80=dec 128, bits 10000000
                    for (int i = 0; i <= nb; i++) {
                        int b = read(buf);
                        int g = read(buf);
                        int r = read(buf);
                        pixels[idx++] = 0xff000000 | (r << 16) | (g << 8) | b;
                    }
                } else {
                    nb &= 0x7f;
                    int b = read(buf);
                    int g = read(buf);
                    int r = read(buf);
                    int v = 0xff000000 | (r << 16) | (g << 8) | b;
                    for (int i = 0; i <= nb; i++)
                        pixels[idx++] = v;
                }
                n -= nb + 1;
            }
        }

        /*
        Bitmap bimg = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bimg.setPixels(pixels, 0, width, 0, 0, width, height);
        */

        Bitmap bimg = ImageUtils.generateBitmapFlipV(pixels, width, height);
        return bimg;
    }
}
