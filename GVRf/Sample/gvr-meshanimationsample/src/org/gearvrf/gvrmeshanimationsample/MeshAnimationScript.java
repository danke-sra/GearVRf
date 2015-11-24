package org.gearvrf.gvrmeshanimationsample;

import java.io.IOException;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMaterial.GVRShaderType;

import android.os.SystemClock;
import android.util.Log;

public class MeshAnimationScript extends GVRScript {

    private GVRContext mGVRContext;
    private GVRSceneObject mCharacter;

    private final String mModelPath = "TRex_NoGround.fbx";
    private final String mDiffuseTexturePath = "t_rex_texture_diffuse.png";

    private GVRActivity mActivity;
    private GVRMesh characterMesh;

    private static final String TAG = "MeshAnimationSample";

    private float animationTime;

    public MeshAnimationScript(GVRActivity activity) {
        mActivity = activity;
        animationTime = (float) SystemClock.elapsedRealtime() / 1000.0f;
    }

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;

        GVRScene outlineScene = gvrContext.getNextMainScene();

        try {
            characterMesh = mGVRContext.loadMesh(new GVRAndroidResource(mGVRContext, mModelPath));

            characterMesh.animate(0.0f);
            GVRTexture texture = gvrContext.loadTexture(
                    new GVRAndroidResource(mGVRContext, mDiffuseTexturePath));

            mCharacter = new GVRSceneObject(mGVRContext, characterMesh, texture,
                    GVRShaderType.MeshAnimation.ID);
            mCharacter.getTransform().setPosition(0.0f, -10.0f, -10.0f);
            mCharacter.getTransform().setRotationByAxis(90.0f, 1.0f, 0.0f, 0.0f);
            mCharacter.getTransform().setRotationByAxis(40.0f, 0.0f, 1.0f, 0.0f);
            mCharacter.getTransform().setScale(1.5f, 1.5f, 1.5f);

            outlineScene.addSceneObject(mCharacter);

        } catch (IOException e) {
            e.printStackTrace();
            mActivity.finish();
            mActivity = null;
            Log.e(TAG, "One or more assets could not be loaded.");
        }
    }

    @Override
    public void onStep() {
        animationTime = (float) SystemClock.elapsedRealtime() / 1000.0f;
        characterMesh.animate(animationTime);
    }
}
