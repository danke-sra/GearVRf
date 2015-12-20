package org.gearvrf.utility;

import org.gearvrf.GVRObject;

public class GVRByteArray extends GVRObject {
    protected byte[] byteArray;

    private GVRByteArray(byte[] byteArray) {
        this.byteArray = byteArray;
    }

    public byte[] getByteArray() {
        return byteArray;
    }

    public static GVRByteArray wrap(byte[] byteArray) {
        return new GVRByteArray(byteArray);
    }
}
