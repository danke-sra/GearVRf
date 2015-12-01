package org.gearvrf.gvrmeshanimationsample;

import java.io.IOException;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRAssimpAnimation;
import org.gearvrf.animation.GVRRepeatMode;

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

    private GVRAnimationEngine mAnimationEngine;
    GVRAnimation mAssimpAnimation = null;
    

    public MeshAnimationScript(GVRActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        mAnimationEngine = gvrContext.getAnimationEngine();

        GVRScene outlineScene = gvrContext.getNextMainScene(new Runnable() {
            @Override
            public void run() {
                	mAssimpAnimation.start(mAnimationEngine);
            }
        });

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

            mAssimpAnimation = new GVRAssimpAnimation(mCharacter, -1);
            mAssimpAnimation.setRepeatMode(GVRRepeatMode.REPEATED).setRepeatCount(-1);

        } catch (IOException e) {
            e.printStackTrace();
            mActivity.finish();
            mActivity = null;
            Log.e(TAG, "One or more assets could not be loaded.");
        }
    }

    @Override
    public void onStep() {
    }
}
