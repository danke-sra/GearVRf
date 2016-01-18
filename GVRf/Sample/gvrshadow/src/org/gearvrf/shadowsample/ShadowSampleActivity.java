package org.gearvrf.shadowsample;

import org.gearvrf.GVRActivity;

import android.os.Bundle;

public class ShadowSampleActivity extends GVRActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScript(new ShadowSampleViewManager(), "gvr_note4.xml");
    }

}
