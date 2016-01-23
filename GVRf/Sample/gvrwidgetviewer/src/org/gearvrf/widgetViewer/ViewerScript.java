/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.widgetViewer;

import java.io.File;
import java.io.IOException;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.opengles.GL10;

import org.gearvrf.*;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRRenderData.GVRRenderMaskBit;

import org.gearvrf.plugins.widget.GVRWidgetPluginActivity;
import org.gearvrf.plugins.widget.GVRWidgetSceneObjectMeshInfo;
import org.gearvrf.scene_objects.GVRWidgetSceneObject;
//import org.gearvrf.scene_objects.GVRListViewSceneObject;
import org.gearvrf.scene_objects.GVRWebViewSceneObject;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.transition.Scene;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.MeasureSpec;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;

public class ViewerScript extends GVRScript {

    private static final String TAG = "ViewerScript";

    private GVRContext mGVRContext = null;
    public Context mContext;
    private MetalOnlyShader mMetalOnlyShader = null;
    private GlassShader mGlassShader = null;
    private DiffuseShader mDiffuseShader = null;
    private ReflectionShader mReflectionShader = null;
    private PhongShader mPhongShader = null;

    private MetalShader2 mMetalShader2 = null;
    private GlassShader2 mGlassShader2 = null;
    private DiffuseShader2 mDiffuseShader2 = null;
    private PhongShader2 mPhongShader2 = null;
    private PhongShader3 mPhongShader3 = null;

    private GVRMaterial mMetalMaterial = null;
    private GVRMaterial mGlassMaterial = null;
    private GVRMaterial mDiffuseMaterial = null;
    private GVRMaterial mReflectionMaterial = null;
    private GVRMaterial mPhongMaterial = null;

    private GVRMaterial mCarBodyMaterial = null;
    private GVRMaterial mCarGlassMaterial = null;
    private GVRMaterial mCarTireMaterial = null;
    private GVRMaterial mCarWheelMaterial = null;
    private GVRMaterial mCarGrillMaterial = null;
    private GVRMaterial mCarBackMaterial = null;
    private GVRMaterial mCarLightMaterial = null;
    private GVRMaterial mCarInsideMaterial = null;

    private GVRMaterial mRobotBodyMaterial = null;
    private GVRMaterial mRobotHeadMaterial = null;
    private GVRMaterial mRobotMetalMaterial = null;
    private GVRMaterial mRobotRubberMaterial = null;

    private GVRMaterial mLeafBodyMaterial = null;
    private GVRMaterial mLeafBoxMaterial = null;
    private GVRMaterial mButtonMaterial = null;
    private float THUMBNAIL_ROT = 0.0f;
    private float OBJECT_ROT = 0.0f;
    private final float EYE_TO_OBJECT = 2.4f;
    private float widgetZ = 2.4f;
    private final int THUMBNAIL_NUM = 5;
    public float coords[] = { 0 };
    private GVRSceneObject[] ThumbnailObject = new GVRSceneObject[THUMBNAIL_NUM];
    private GVRSceneObject[] ThumbnailRotation = new GVRSceneObject[THUMBNAIL_NUM];
    private GVRRenderData[] ThumbnailGlasses = new GVRRenderData[THUMBNAIL_NUM];
    private GVRSceneObject[] Thumbnails = new GVRSceneObject[THUMBNAIL_NUM];
    private GVRTexture[] ThumbnailTextures = new GVRTexture[THUMBNAIL_NUM];
    private float[][] ThumbnailTargetPosition = new float[THUMBNAIL_NUM][3];
    private int[] ThumbnailTargetIndex = new int[THUMBNAIL_NUM];
    private float[][] ThumbnailCurrentPosition = new float[THUMBNAIL_NUM][3];
    private int[] ThumbnailOrder = new int[THUMBNAIL_NUM];
    public int ThumbnailSelected = 2;
    private GVRSceneObject[] buttonObject = new GVRSceneObject[1];
    private GVRSceneObject widgetbuttonObject;
    private GVRSceneObject widgetbuttonObject2;

    private boolean SelectionMode = false;
    private boolean SelectionActive = false;
    public boolean buttonpointed = true;
    public boolean button2pointed = true;
    public boolean objectpointed = true;
    private boolean forcerotate = false;

    private boolean mIsButtonDown = false;
    private boolean mIsSingleTapped = false;

    public GVRSceneObject[] Objects = new GVRSceneObject[THUMBNAIL_NUM];

    private GVRWidgetPluginActivity mActivity;
    private GLWebView view = null;

    private Surface mSurface;

    private SurfaceTexture mSurfaceTexture;
    Bitmap bb;
    public float rotatex = 0.0f;
    public float rotatey = 0.0f;
    public float rotatez = 0.0f;

    public float lastrotatex = 0.0f;
    public float lastrotatey = 0.0f;
    public float lastrotatez = 0.0f;

    // public float accrotatex = 0.0f;
    // public float accrotatey = 0.0f;
    // public float accrotatez = 0.0f;
    public boolean resetrotate = false;
    public EGLContext eglContext;
    GVRTexture mBlueTex;
    GVRTexture mBlackTex;
    GVRTexture mGreenTex;
    GVRTexture mSilverTex;
    GVRTexture mDefaultColorTex;
    public int color = 1;
    public boolean mLookInside = false;
    public float zoomlevel = -2.0f;

    ViewerScript(GVRActivity activity, View v, Bitmap b) {
        mActivity = (GVRWidgetPluginActivity) activity;
        view = (GLWebView) v;
        bb = b;
    }

    GVRTexture texture = null;
    GVRMaterial material;
    GVRMaterial widgetmaterial2;
    public int libGDxTexture;
    public javax.microedition.khronos.egl.EGLContext mSharedcontext = null;

    @Override
    public void onInit(GVRContext gvrContext) {

        mGVRContext = gvrContext;
        mContext = mGVRContext.getContext();
        mMetalOnlyShader = new MetalOnlyShader(mGVRContext);
        mDiffuseShader = new DiffuseShader(mGVRContext);
        mGlassShader = new GlassShader(mGVRContext);
        mReflectionShader = new ReflectionShader(mGVRContext);
        mPhongShader = new PhongShader(mGVRContext);

        mMetalShader2 = new MetalShader2(mGVRContext);
        mDiffuseShader2 = new DiffuseShader2(mGVRContext);
        mGlassShader2 = new GlassShader2(mGVRContext);
        mPhongShader2 = new PhongShader2(mGVRContext);
        mPhongShader3 = new PhongShader3(mGVRContext);

        // R.id.button_normal);
        Matrix matrix = new Matrix();
      //  mEglContext = ((EGL10) EGLContext.getEGL()).eglGetCurrentContext();
        // matrix.postRotate(90);
        /*
         * view.setDrawingCacheEnabled(true);
         * view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
         * MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
         * view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
         * 
         * view.buildDrawingCache();
         */

        // Bitmap buttonbitmap =
        // Bitmap.createBitmap(view.getDrawingCache(false), 0,
        // 0,view.getMeasuredWidth() , view.getMeasuredHeight() , matrix,
        // false);
        // if(buttonbitmap != null)Log.e("datta", "bitmap created!!!");

        GVRScene mainScene = mGVRContext.getNextMainScene();

        mainScene.getMainCameraRig().getLeftCamera()
                .setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);
        mainScene.getMainCameraRig().getRightCamera()
                .setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);

        try {

            mBlueTex = mGVRContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "blue.png"));
            mBlackTex = mGVRContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "black.png"));
            mGreenTex = mGVRContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "green.png"));
            mSilverTex = mGVRContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "silver.png"));

            GVRTexture env_tex = mGVRContext
                    .loadTexture(new GVRAndroidResource(mGVRContext, "env.jpg"));
            mReflectionMaterial = new GVRMaterial(mGVRContext,
                    mReflectionShader.getShaderId());
            mReflectionMaterial.setVec4(ReflectionShader.COLOR_KEY, 1.0f, 1.0f,
                    1.0f, 1.0f);
            mReflectionMaterial.setFloat(ReflectionShader.RADIUS_KEY, 10.0f);
            mReflectionMaterial.setTexture(ReflectionShader.TEXTURE_KEY,
                    env_tex);

            // ------------------------------------------------------ set
            // materials
            // watch
            mMetalMaterial = new GVRMaterial(mGVRContext,
                    mMetalOnlyShader.getShaderId());
            mMetalMaterial.setVec4(MetalOnlyShader.COLOR_KEY, 1.7f, 1.4f, 1.0f,
                    1.0f);
            mMetalMaterial.setFloat(MetalOnlyShader.RADIUS_KEY, 10.0f);
            mMetalMaterial.setTexture(MetalOnlyShader.TEXTURE_KEY, env_tex);

            mGlassMaterial = new GVRMaterial(mGVRContext,
                    mGlassShader.getShaderId());
            mGlassMaterial.setVec4(GlassShader.COLOR_KEY, 1.0f, 1.0f, 1.0f,
                    1.0f);
            mGlassMaterial.setFloat(MetalOnlyShader.RADIUS_KEY, 10.0f);
            mGlassMaterial.setTexture(GlassShader.TEXTURE_KEY, env_tex);

            GVRTexture board_tex = mGVRContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "watch/board.jpg"));
            mDiffuseMaterial = new GVRMaterial(mGVRContext,
                    mDiffuseShader.getShaderId());
            mDiffuseMaterial.setVec4(DiffuseShader.COLOR_KEY, 1.0f, 1.0f, 1.0f,
                    1.0f);
            mDiffuseMaterial.setTexture(DiffuseShader.TEXTURE_KEY, board_tex);

            // jar
            mPhongMaterial = new GVRMaterial(mGVRContext,
                    mPhongShader.getShaderId());
            mPhongMaterial.setVec4(PhongShader.COLOR_KEY, 1.2f, 1.2f, 1.3f,
                    1.0f);
            mPhongMaterial.setFloat(PhongShader.RADIUS_KEY, 10.0f);
            mPhongMaterial.setTexture(PhongShader.TEXTURE_KEY, env_tex);

            // car
            GVRTexture car_body_tex = mGVRContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "car/body.jpg"));
            mDefaultColorTex = car_body_tex;
            mCarBodyMaterial = new GVRMaterial(mGVRContext,
                    mPhongShader3.getShaderId());
            mCarBodyMaterial.setFloat(PhongShader3.RADIUS_KEY, 10.0f);
            mCarBodyMaterial.setTexture(PhongShader3.ENV_KEY, env_tex);
            mCarBodyMaterial.setTexture(PhongShader3.TEXTURE_KEY, car_body_tex);
            // mCarBodyMaterial.setColor(0.5f, 0.2f, 0.3f);

            mCarWheelMaterial = new GVRMaterial(mGVRContext,
                    mMetalShader2.getShaderId());
            mCarWheelMaterial.setVec4(MetalShader2.COLOR_KEY, 1.2f, 1.2f, 1.2f,
                    1.0f);
            mCarWheelMaterial.setFloat(MetalShader2.RADIUS_KEY, 10.0f);
            mCarWheelMaterial.setTexture(MetalShader2.TEXTURE_KEY, env_tex);

            mCarGlassMaterial = new GVRMaterial(mGVRContext,
                    mGlassShader2.getShaderId());
            mCarGlassMaterial.setVec4(GlassShader2.COLOR_KEY, 1.0f, 1.0f, 1.0f,
                    1.0f);
            mCarGlassMaterial.setFloat(GlassShader2.RADIUS_KEY, 10.0f);
            mCarGlassMaterial.setTexture(GlassShader2.TEXTURE_KEY, env_tex);

            GVRTexture default_tex = mGVRContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "car/default.png"));
            mCarTireMaterial = new GVRMaterial(mGVRContext,
                    mDiffuseShader2.getShaderId());
            mCarTireMaterial.setVec4(DiffuseShader2.COLOR_KEY, 0.1f, 0.1f,
                    0.1f, 1.0f);
            mCarTireMaterial
                    .setTexture(DiffuseShader2.TEXTURE_KEY, default_tex);

            GVRTexture back_tex = mGVRContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "car/back.jpg"));
            mCarBackMaterial = new GVRMaterial(mGVRContext,
                    mDiffuseShader2.getShaderId());
            mCarBackMaterial.setVec4(DiffuseShader2.COLOR_KEY, 1.0f, 1.0f,
                    1.0f, 1.0f);
            mCarBackMaterial.setTexture(DiffuseShader2.TEXTURE_KEY, back_tex);

            GVRTexture grill_tex = mGVRContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "car/grill.jpg"));
            mCarGrillMaterial = new GVRMaterial(mGVRContext,
                    mDiffuseShader2.getShaderId());
            mCarGrillMaterial.setVec4(DiffuseShader2.COLOR_KEY, 1.0f, 1.0f,
                    1.0f, 1.0f);
            mCarGrillMaterial.setTexture(DiffuseShader2.TEXTURE_KEY, grill_tex);

            mCarLightMaterial = new GVRMaterial(mGVRContext,
                    mGlassShader2.getShaderId());
            mCarLightMaterial.setVec4(GlassShader2.COLOR_KEY, 2.5f, 2.5f, 2.5f,
                    1.0f);
            mCarLightMaterial.setFloat(GlassShader2.RADIUS_KEY, 10.0f);
            mCarLightMaterial.setTexture(GlassShader2.TEXTURE_KEY, env_tex);

            mCarInsideMaterial = new GVRMaterial(mGVRContext,
                    mPhongShader2.getShaderId());
            mCarInsideMaterial.setVec4(PhongShader2.COLOR_KEY, 0.0f, 0.0f,
                    0.0f, 1.0f);
            mCarInsideMaterial.setFloat(PhongShader2.RADIUS_KEY, 10.0f);
            mCarInsideMaterial.setTexture(PhongShader2.TEXTURE_KEY, env_tex);

            // robot
            GVRTexture robot_head_tex = mGVRContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "robot/head.jpg"));
            mRobotHeadMaterial = new GVRMaterial(mGVRContext,
                    mPhongShader3.getShaderId());
            mRobotHeadMaterial.setFloat(PhongShader3.RADIUS_KEY, 10.0f);
            mRobotHeadMaterial.setTexture(PhongShader3.ENV_KEY, env_tex);
            mRobotHeadMaterial.setTexture(PhongShader3.TEXTURE_KEY,
                    robot_head_tex);

            mRobotMetalMaterial = new GVRMaterial(mGVRContext,
                    mMetalShader2.getShaderId());
            mRobotMetalMaterial.setVec4(MetalShader2.COLOR_KEY, 1.5f, 1.5f,
                    1.5f, 1.0f);
            mRobotMetalMaterial.setFloat(MetalShader2.RADIUS_KEY, 10.0f);
            mRobotMetalMaterial.setTexture(MetalShader2.TEXTURE_KEY, env_tex);

            mRobotBodyMaterial = new GVRMaterial(mGVRContext,
                    mPhongShader2.getShaderId());
            mRobotBodyMaterial.setVec4(PhongShader2.COLOR_KEY, 1.0f, 1.0f,
                    1.0f, 1.0f);
            mRobotBodyMaterial.setFloat(PhongShader2.RADIUS_KEY, 10.0f);
            mRobotBodyMaterial.setTexture(PhongShader2.TEXTURE_KEY, env_tex);

            mRobotRubberMaterial = new GVRMaterial(mGVRContext,
                    mDiffuseShader2.getShaderId());
            mRobotRubberMaterial.setVec4(DiffuseShader2.COLOR_KEY, 0.3f, 0.3f,
                    0.3f, 1.0f);
            mRobotRubberMaterial.setTexture(DiffuseShader2.TEXTURE_KEY,
                    default_tex);

            GVRTexture leaf_box_tex;

            // Bitmap b = (((ViewerActivity)mActivity).game).b;

            // Bitmap b =
            // ((AndroidGraphics)((ViewerActivity2)mActivity).getGraphics()).mBitmap;
            // if(b != null){
            // leaf_box_tex = new GVRBitmapTexture(mGVRContext, b);
            // }else{
            leaf_box_tex = mGVRContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "leaf/box.jpg"));

            // / }
            mLeafBoxMaterial = new GVRMaterial(mGVRContext,
                    mPhongShader3.getShaderId());
            mLeafBoxMaterial.setFloat(PhongShader3.RADIUS_KEY, 10.0f);
            mLeafBoxMaterial.setTexture(PhongShader3.ENV_KEY, env_tex);
            mLeafBoxMaterial.setTexture(PhongShader3.TEXTURE_KEY, leaf_box_tex);

            mLeafBodyMaterial = new GVRMaterial(mGVRContext,
                    mMetalShader2.getShaderId());
            mLeafBodyMaterial.setVec4(MetalShader2.COLOR_KEY, 2.5f, 2.5f, 2.5f,
                    1.0f);
            mLeafBodyMaterial.setFloat(MetalShader2.RADIUS_KEY, 10.0f);
            mLeafBodyMaterial.setTexture(MetalShader2.TEXTURE_KEY, env_tex);

            // ------------------------------------------------------ set
            // objects

            for (int i = 0; i < THUMBNAIL_NUM; i++)
                Objects[i] = new GVRSceneObject(mGVRContext);

            // --------------watch

            GVRSceneObject obj1 = new GVRSceneObject(mGVRContext);
            GVRRenderData renderData1 = new GVRRenderData(mGVRContext);
            GVRMesh mesh1 = mGVRContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "watch/frame.obj"));
            renderData1.setMesh(mesh1);
            renderData1.setMaterial(mMetalMaterial);
            obj1.attachRenderData(renderData1);
            Objects[2].addChildObject(obj1);

            GVRSceneObject obj2 = new GVRSceneObject(mGVRContext);
            GVRRenderData renderData2 = new GVRRenderData(mGVRContext);
            GVRMesh mesh2 = mGVRContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "watch/board.obj"));
            renderData2.setMesh(mesh2);
            renderData2.setMaterial(mDiffuseMaterial);
            obj2.attachRenderData(renderData2);
            Objects[2].addChildObject(obj2);

            GVRSceneObject obj3 = new GVRSceneObject(mGVRContext);
            GVRRenderData renderData3 = new GVRRenderData(mGVRContext);
            GVRMesh mesh3 = mGVRContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "watch/glass.obj"));
            renderData3.setMesh(mesh3);
            renderData3.setMaterial(mGlassMaterial);
            obj3.attachRenderData(renderData3);

            obj3.getRenderData().setRenderingOrder(3000);
            Objects[2].addChildObject(obj3);

            Objects[2].getTransform().setPosition(0.0f, 0.0f, -EYE_TO_OBJECT);
            mainScene.addSceneObject(Objects[2]);

            // --------------jar

            GVRSceneObject obj5 = new GVRSceneObject(mGVRContext);
            GVRRenderData renderData5 = new GVRRenderData(mGVRContext);
            GVRMesh mesh5 = mGVRContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "jar/bike.obj"));
            renderData5.setMesh(mesh5);
            renderData5.setMaterial(mPhongMaterial);
            obj5.attachRenderData(renderData5);
            Objects[1].addChildObject(obj5);

            GVRSceneObject obj4 = new GVRSceneObject(mGVRContext);
            GVRRenderData renderData4 = new GVRRenderData(mGVRContext);
            GVRMesh mesh4 = mGVRContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "jar/edge.obj"));
            renderData4.setMesh(mesh4);
            renderData4.setMaterial(mMetalMaterial);
            obj4.attachRenderData(renderData4);
            obj4.getRenderData().setRenderingOrder(3000);
            // Objects[1].addChildObject(obj4);

            Objects[1].getTransform().setPosition(0.0f, 0.0f,
                    -EYE_TO_OBJECT - 3.0f);
            mainScene.addSceneObject(Objects[1]);

            // --------------car

            GVRSceneObject obj6 = new GVRSceneObject(mGVRContext);
            GVRRenderData renderData6 = new GVRRenderData(mGVRContext);
            GVRMesh mesh6 = mGVRContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "car/body.obj"));
            renderData6.setMesh(mesh6);
            renderData6.setMaterial(mCarBodyMaterial);
            obj6.attachRenderData(renderData6);
            obj6.getRenderData().setCullTest(false);
            Objects[3].addChildObject(obj6);

            GVRSceneObject obj9 = new GVRSceneObject(mGVRContext);
            GVRRenderData renderData9 = new GVRRenderData(mGVRContext);
            GVRMesh mesh9 = mGVRContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "car/tire.obj"));
            renderData9.setMesh(mesh9);
            renderData9.setMaterial(mCarTireMaterial);
            obj9.attachRenderData(renderData9);
            Objects[3].addChildObject(obj9);

            GVRSceneObject obj10 = new GVRSceneObject(mGVRContext);
            GVRRenderData renderData10 = new GVRRenderData(mGVRContext);
            GVRMesh mesh10 = mGVRContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "car/glass.obj"));
            renderData10.setMesh(mesh10);
            renderData10.setMaterial(mCarGlassMaterial);
            obj10.attachRenderData(renderData10);
            obj10.getRenderData().setCullTest(false);
            obj10.getRenderData().setRenderingOrder(3000);
            Objects[3].addChildObject(obj10);

            GVRSceneObject obj11 = new GVRSceneObject(mGVRContext);
            GVRRenderData renderData11 = new GVRRenderData(mGVRContext);
            GVRMesh mesh11 = mGVRContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "car/wheel.obj"));
            renderData11.setMesh(mesh11);
            renderData11.setMaterial(mCarWheelMaterial);
            obj11.attachRenderData(renderData11);
            Objects[3].addChildObject(obj11);

            GVRSceneObject obj12 = new GVRSceneObject(mGVRContext);
            GVRRenderData renderData12 = new GVRRenderData(mGVRContext);
            GVRMesh mesh12 = mGVRContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "car/back.obj"));
            renderData12.setMesh(mesh12);
            renderData12.setMaterial(mCarBackMaterial);
            obj12.attachRenderData(renderData12);
            Objects[3].addChildObject(obj12);

            GVRSceneObject obj13 = new GVRSceneObject(mGVRContext);
            GVRRenderData renderData13 = new GVRRenderData(mGVRContext);
            GVRMesh mesh13 = mGVRContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "car/grill.obj"));
            renderData13.setMesh(mesh13);
            renderData13.setMaterial(mCarGrillMaterial);
            obj13.attachRenderData(renderData13);
            obj10.getRenderData().setRenderingOrder(3000);
            Objects[3].addChildObject(obj13);

            GVRSceneObject obj14 = new GVRSceneObject(mGVRContext);
            GVRRenderData renderData14 = new GVRRenderData(mGVRContext);
            GVRMesh mesh14 = mGVRContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "car/glass2.obj"));
            renderData14.setMesh(mesh14);
            renderData14.setMaterial(mCarLightMaterial);
            obj14.attachRenderData(renderData14);
            obj14.getRenderData().setRenderingOrder(4000);
            Objects[3].addChildObject(obj14);

            GVRSceneObject obj19 = new GVRSceneObject(mGVRContext);
            GVRRenderData renderData19 = new GVRRenderData(mGVRContext);
            GVRMesh mesh19 = mGVRContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "car/inside.obj"));
            renderData19.setMesh(mesh19);
            renderData19.setMaterial(mCarInsideMaterial);
            obj19.attachRenderData(renderData19);
            Objects[3].addChildObject(obj19);

            Objects[3].getTransform().setPosition(0.5f, -1.0f,
                    -EYE_TO_OBJECT - 5.0f);
            /*
             * Objects[3].getTransform().setScaleX(0.23f);
             * Objects[3].getTransform().setScaleY(0.23f);
             * Objects[3].getTransform().setScaleZ(0.23f);
             */
            mainScene.addSceneObject(Objects[3]);
            // Objects[3].getTransform().setc
            // robot

            GVRSceneObject obj15 = new GVRSceneObject(mGVRContext);
            GVRRenderData renderData15 = new GVRRenderData(mGVRContext);
            GVRMesh mesh15 = mGVRContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "robot/body.obj"));
            renderData15.setMesh(mesh15);
            renderData15.setMaterial(mRobotBodyMaterial);
            obj15.attachRenderData(renderData15);
            Objects[4].addChildObject(obj15);

            GVRSceneObject obj16 = new GVRSceneObject(mGVRContext);
            GVRRenderData renderData16 = new GVRRenderData(mGVRContext);
            GVRMesh mesh16 = mGVRContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "robot/head.obj"));
            renderData16.setMesh(mesh16);
            renderData16.setMaterial(mRobotHeadMaterial);
            obj16.attachRenderData(renderData16);
            Objects[4].addChildObject(obj16);

            GVRSceneObject obj17 = new GVRSceneObject(mGVRContext);
            GVRRenderData renderData17 = new GVRRenderData(mGVRContext);
            GVRMesh mesh17 = mGVRContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "robot/metal.obj"));
            renderData17.setMesh(mesh17);
            renderData17.setMaterial(mRobotMetalMaterial);
            obj17.attachRenderData(renderData17);
            obj17.getRenderData().setRenderingOrder(3000);
            Objects[4].addChildObject(obj17);

            GVRSceneObject obj18 = new GVRSceneObject(mGVRContext);
            GVRRenderData renderData18 = new GVRRenderData(mGVRContext);
            GVRMesh mesh18 = mGVRContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "robot/rubber.obj"));
            renderData18.setMesh(mesh18);
            renderData18.setMaterial(mRobotRubberMaterial);
            obj18.attachRenderData(renderData18);
            Objects[4].addChildObject(obj18);

            Objects[4].getTransform().setPosition(0.0f, 0.0f, -EYE_TO_OBJECT);

            mainScene.addSceneObject(Objects[4]);

            // leaf

            GVRSceneObject obj20 = new GVRSceneObject(mGVRContext);
            GVRRenderData renderData20 = new GVRRenderData(mGVRContext);
            GVRMesh mesh20 = mGVRContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "leaf/leaf.obj"));
            renderData20.setMesh(mesh20);
            renderData20.setMaterial(mLeafBodyMaterial);
            obj20.attachRenderData(renderData20);
            Objects[0].addChildObject(obj20);

            GVRSceneObject obj21 = new GVRSceneObject(mGVRContext);
            GVRRenderData renderData21 = new GVRRenderData(mGVRContext);
            GVRMesh mesh21 = mGVRContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "leaf/box.obj"));
            renderData21.setMesh(mesh21);
            renderData21.setMaterial(mLeafBoxMaterial);
            obj21.attachRenderData(renderData21);
            Objects[0].addChildObject(obj21);

            Objects[0].getTransform().setPosition(0.0f, 0.0f, -EYE_TO_OBJECT);
            mainScene.addSceneObject(Objects[0]);

            for (int I = 0; I < THUMBNAIL_NUM; I++)
                for (int i = 0; i < Objects[I].getChildrenCount(); i++)
                    Objects[I].getChildByIndex(i).getRenderData()
                            .setRenderMask(0);
            // ------------------------------------------------------ set
            // thumbnails

            ThumbnailTextures[0] = mGVRContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "leaf/leaf.jpg"));
            ThumbnailTextures[1] = mGVRContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "jar/jar.png"));
            ThumbnailTextures[2] = mGVRContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "watch/watch.png"));
            ThumbnailTextures[3] = mGVRContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "car/car.png"));
            ThumbnailTextures[4] = mGVRContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "robot/robot.jpg"));

            ThumbnailTargetPosition[0][0] = -2.2f;
            ThumbnailTargetPosition[0][1] = 0.0f;
            ThumbnailTargetPosition[0][2] = -EYE_TO_OBJECT - 2.8f;
            ThumbnailTargetPosition[1][0] = -1.0f;
            ThumbnailTargetPosition[1][1] = 0.0f;
            ThumbnailTargetPosition[1][2] = -EYE_TO_OBJECT - 1.5f;
            ThumbnailTargetPosition[2][0] = 0.0f;
            ThumbnailTargetPosition[2][1] = 0.0f;
            ThumbnailTargetPosition[2][2] = -EYE_TO_OBJECT - 0.0f;
            ThumbnailTargetPosition[3][0] = 1.0f;
            ThumbnailTargetPosition[3][1] = 0.0f;
            ThumbnailTargetPosition[3][2] = -EYE_TO_OBJECT - 1.5f;
            ThumbnailTargetPosition[4][0] = 2.4f;
            ThumbnailTargetPosition[4][1] = 0.0f;
            ThumbnailTargetPosition[4][2] = -EYE_TO_OBJECT - 2.8f;

            for (int i = 0; i < THUMBNAIL_NUM; i++)
                for (int j = 0; j < 3; j++)
                    ThumbnailCurrentPosition[i][j] = ThumbnailTargetPosition[i][j];
            for (int i = 0; i < THUMBNAIL_NUM; i++)
                ThumbnailTargetIndex[i] = i;

            ThumbnailOrder[0] = 10000;
            ThumbnailOrder[1] = 10001;
            ThumbnailOrder[2] = 10002;
            ThumbnailOrder[3] = 10001;
            ThumbnailOrder[4] = 10000;

            GVRMesh glass_mesh = mGVRContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "glass.obj"));
            GVRMesh board_mesh = mGVRContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "board.obj"));
            GVRMesh picks_mesh = mGVRContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "pick.obj"));
            GVRMesh button_mesh = mGVRContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "button.obj"));
            GVRMesh button_pick_mesh = mGVRContext
                    .loadMesh(new GVRAndroidResource(mGVRContext, "button.obj"));
            GVRMesh widgetbutton2_mesh = mGVRContext
                    .loadMesh(new GVRAndroidResource(mGVRContext, "button2.obj"));
            /*
             * buttonObject[0] = new GVRSceneObject(mGVRContext);
             * mButtonMaterial = new GVRMaterial(gvrContext); GVRTexture
             * button_tex = new GVRBitmapTexture(mGVRContext, buttonbitmap);
             * //mButtonMaterial.setTexture("texture", button_tex);
             * mButtonMaterial.setMainTexture(button_tex); GVRRenderData
             * buttonrenderdata = new GVRRenderData(mGVRContext);
             * buttonrenderdata.setMesh(button_mesh);
             * buttonrenderdata.setMaterial(mButtonMaterial);
             * buttonObject[0].attachRenderData(buttonrenderdata);
             * buttonObject[0].getTransform().setPosition(0, -1,
             * -EYE_TO_OBJECT);
             * buttonObject[0].getRenderData().setRenderingOrder(100000 - 1);
             * buttonObject[0].getTransform().rotateByAxis(-30, 1.0f, 0.0f,
             * 0.0f); mainScene.addSceneObject(buttonObject[0]);
             * GVREyePointeeHolder eyePointeeHolder2 = new GVREyePointeeHolder(
             * gvrContext); GVRMeshEyePointee eyePointee2 = new
             * GVRMeshEyePointee( gvrContext, button_pick_mesh);
             * eyePointeeHolder2.addPointee(eyePointee2);
             * buttonObject[0].attachEyePointeeHolder(eyePointeeHolder2);
             */
            // ((WebView) view).setBackgroundColor(0);
            // ((WebView) view).setLayerType(View.LAYER_TYPE_SOFTWARE,null);
            // ((WebView)
            // view).setBackground(mGVRContext.getContext().getResources().getDrawable(R.drawable.gearvr_logo));

            // ((WebView) view).loadData("webview", "text/html", "utf-8");
            // view.setBackgroundColor(Color.CYAN);
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // view.setWebViewClient(new WebViewClient());
                    // view.setWebChromeClient(new WebChromeClient());
                    // view.loadUrl("http://stackoverflow.com/questions/12499396/is-it-possible-to-render-an-android-view-to-an-opengl-fbo-or-texture");

                }
            });

            // buttonObject[0] = new GVRWebViewSceneObject(mGVRContext,
            // button_mesh, view, mActivity);

            // GVRTexture ltex = mGVRContext
            // .loadTexture(new GVRAndroidResource(mGVRContext,
            // "w2.png"));
            /*
             * int[] configAttribs = {EGL10.EGL_RED_SIZE, 8,
             * EGL10.EGL_GREEN_SIZE, 8, EGL10.EGL_BLUE_SIZE, 8,
             * EGL10.EGL_ALPHA_SIZE, 8,EGL10.EGL_DEPTH_SIZE,24,
             * EGL10.EGL_STENCIL_SIZE,0,EGL10.EGL_NONE};
             * 
             * 
             * int auxNumConfigs; EGLConfig auxConfig;
             * 
             * EGLConfig[] configs = new EGLConfig[10]; int[] num_config = new
             * int[1]; int[] attrib_list = { 0x3098, 2, EGL10.EGL_NONE };
             * EGLDisplay d =
             * ((EGL10)EGLContext.getEGL()).eglGetCurrentDisplay(); int[]
             * num_conf = new int[1];
             * 
             * ((EGL10)EGLContext.getEGL()).eglChooseConfig(((EGL10)EGLContext.
             * getEGL()).eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY),
             * configAttribs, configs, 1, num_config);
             * 
             * ((EGL10)EGLContext.getEGL()).eglGetConfigs(((EGL10)EGLContext.getEGL
             * ()).eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY), null, 0, num_conf);
             * int configurations = num_conf[0];
             * 
             * //Querying actual configurations EGLConfig[] conf = new
             * EGLConfig[configurations];
             * 
             * ((EGL10)EGLContext.getEGL()).eglGetConfigs(((EGL10)EGLContext.getEGL
             * ()).eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY), conf,
             * configurations, num_conf); EGLContext c =
             * ((EGL10)EGLContext.getEGL
             * ()).eglCreateContext(((EGL10)EGLContext.getEGL
             * ()).eglGetDisplay(EGL10
             * .EGL_DEFAULT_DISPLAY),configs[0],mSharedcontext, attrib_list); //
             * EGLSurface s =
             * ((EGL10)EGLContext.getEGL()).eglGetCurrentSurface(EGL10
             * .EGL_DRAW);
             * 
             * 
             * EGLSurface EglSurface =
             * ((EGL10)EGLContext.getEGL()).eglCreateWindowSurface
             * (((EGL10)EGLContext
             * .getEGL()).eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY), configs[0],
             * mSurfaceTexture, attrib_list); if(EglSurface == null)
             * Log.e("datta","EGL surface null!!!");
             * ((EGL10)EGLContext.getEGL()).eglMakeCurrent(d, EglSurface,
             * EglSurface,c);
             */
            // mSurfaceTexture = new SurfaceTexture(libGDxTexture);
            // while(libGDxTexture == 0){

            // }
            Log.e("datta", "generating texture now=" + libGDxTexture);
            texture = new GVRSharedTexture(gvrContext, mActivity.getTextureId());

            /*
             * GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
             * libGDxTexture);
             * 
             * 
             * GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
             * GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
             * GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
             * GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
             * GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
             * GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
             * GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
             * GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
             */
            GVRWidgetSceneObjectMeshInfo info = new GVRWidgetSceneObjectMeshInfo(-2.5f,1.0f,-1.5f,-1.0f,new int[] { 0, 0 },new int[] { 250, 500 });
          /*  info.topLeftx = -2.5f;
            info.topLefty = 1.0f;
            info.bottomrightx = -1.5f;
            info.bottomrighty = -1.0f;
            info.mBottomRightOfView = new int[] { 250, 500 };
            info.mTopLeftOfView = new int[] { 0, 0 };*/

            GVRWidgetSceneObjectMeshInfo info2 = new GVRWidgetSceneObjectMeshInfo(1.5f,1.0f,2.5f,-1.0f,new int[] { 251, 0 },new int[] { 500, 500 });
           /* info2.topLeftx = 1.5f;
            info2.topLefty = 1.0f;
            info2.bottomrightx = 2.5f;
            info2.bottomrighty = -1.0f;
            info2.mBottomRightOfView = new int[] { 500, 500 };
            info2.mTopLeftOfView = new int[] { 251, 0 };*/
            widgetbuttonObject = new GVRWidgetSceneObject(mGVRContext,
                    mActivity.getTextureId(), info, mActivity.getWidth(),
                    mActivity.getHeight());
            widgetbuttonObject2 = new GVRWidgetSceneObject(mGVRContext,
                    mActivity.getTextureId(), info2, mActivity.getWidth(),
                    mActivity.getHeight());
            GVRRenderData ldata = new GVRRenderData(mGVRContext);
            GVRRenderData ldata2 = new GVRRenderData(mGVRContext);
            // GVRMesh mesh21 = mGVRContext.loadMesh(new GVRAndroidResource(
            // mGVRContext, "leaf/box.obj"));
            // material = new GVRMaterial(mGVRContext,
            // mPhongShader3.getShaderId());
            widgetmaterial2 = new GVRMaterial(mGVRContext,
                    mPhongShader3.getShaderId());
            // ldata.setMesh(button_mesh);
            //
            ldata2.setMesh(widgetbutton2_mesh);
            ldata2.setMaterial(widgetmaterial2);
            float[] light = new float[4];
            light[0] = 6.0f;
            light[1] = 10.0f;
            light[2] = 10.0f;
            light[3] = 1.0f;

            float[] eye = new float[4];
            eye[0] = 0.0f;
            eye[1] = 0.0f;
            eye[2] = 3.0f * EYE_TO_OBJECT;
            eye[3] = 1.0f;

            float[] matO = Objects[ThumbnailSelected].getTransform()
                    .getModelMatrix();
            material = new GVRMaterial(gvrContext, GVRShaderType.UnlitFBO.ID);
            ldata.setMaterial(material);
            material.setMainTexture(texture);
            material.setVec4(PhongShader3.MAT1_KEY, matO[0], matO[4], matO[8],
                    matO[12]);
            material.setVec4(PhongShader3.MAT2_KEY, matO[1], matO[5], matO[9],
                    matO[13]);
            material.setVec4(PhongShader3.MAT3_KEY, matO[2], matO[6], matO[10],
                    matO[14]);
            material.setVec4(PhongShader3.MAT4_KEY, matO[3], matO[7], matO[11],
                    matO[15]);
            material.setVec3(PhongShader3.LIGHT_KEY, light[0], light[1],
                    light[2]);
            material.setVec3(PhongShader3.EYE_KEY, eye[0], eye[1], eye[2]);

            widgetmaterial2 = new GVRMaterial(gvrContext);
            widgetmaterial2.setMainTexture(texture);
            widgetmaterial2.setVec4(PhongShader3.MAT1_KEY, matO[0], matO[4],
                    matO[8], matO[12]);
            widgetmaterial2.setVec4(PhongShader3.MAT2_KEY, matO[1], matO[5],
                    matO[9], matO[13]);
            widgetmaterial2.setVec4(PhongShader3.MAT3_KEY, matO[2], matO[6],
                    matO[10], matO[14]);
            widgetmaterial2.setVec4(PhongShader3.MAT4_KEY, matO[3], matO[7],
                    matO[11], matO[15]);
            widgetmaterial2.setVec3(PhongShader3.LIGHT_KEY, light[0], light[1],
                    light[2]);
            widgetmaterial2.setVec3(PhongShader3.EYE_KEY, eye[0], eye[1],
                    eye[2]);

            // material.setFloat(PhongShader3.RADIUS_KEY, 10.0f);
            // material.setTexture(PhongShader3.ENV_KEY, env_tex);
            // material.setTexture(PhongShader3.TEXTURE_KEY, ltex);
            // widgetbuttonObject.attachRenderData(ldata);
            // widgetbuttonObject2.attachRenderData(ldata2);
            // widgetbuttonObject.getRenderData().setMaterial(material);
            // widgetbuttonObject2.getRenderData().setMaterial(widgetmaterial2);

            // mSurfaceTexture = new SurfaceTexture(texture.getId());
            // mSurface = new Surface(mSurfaceTexture);
            // mSurfaceTexture.setDefaultBufferSize(512,
            // 512);

            widgetbuttonObject.getTransform().setPosition(0, 0,
                    -EYE_TO_OBJECT - 1.5f);
            widgetbuttonObject.getTransform().rotateByAxis(40.0f, 0.0f, 1.0f,
                    0.0f);
            widgetbuttonObject.getRenderData().setRenderingOrder(100000 - 1);

            widgetbuttonObject2.getTransform().setPosition(0, 0,
                    -EYE_TO_OBJECT - 1.5f);
            widgetbuttonObject2.getTransform().rotateByAxis(-40.0f, 0.0f, 1.0f,
                    0.0f);
            widgetbuttonObject2.getRenderData().setRenderingOrder(100000 - 1);
            // widgetbuttonObject2.getRenderData().setRenderMask(0);
            // widgetbuttonObject.getTransform().rotateByAxis(-30,
            // 1.0f, 0.0f, 0.0f);
            mainScene.addSceneObject(widgetbuttonObject);
            mainScene.addSceneObject(widgetbuttonObject2);
            GVREyePointeeHolder eyePointeeHolder2 = new GVREyePointeeHolder(
                    gvrContext);
            GVRMeshEyePointee eyePointee2 = new GVRMeshEyePointee(gvrContext,
                    button_pick_mesh);
            eyePointeeHolder2.addPointee(eyePointee2);
            widgetbuttonObject.attachEyePointeeHolder(eyePointeeHolder2);

            GVREyePointeeHolder eyePointeeHolder3 = new GVREyePointeeHolder(
                    gvrContext);
            GVRMeshEyePointee eyePointee3 = new GVRMeshEyePointee(gvrContext,
                    widgetbutton2_mesh);
            eyePointeeHolder3.addPointee(eyePointee3);
            widgetbuttonObject2.attachEyePointeeHolder(eyePointeeHolder3);

            for (int i = 0; i < THUMBNAIL_NUM; i++) {
                ThumbnailObject[i] = new GVRSceneObject(mGVRContext);
                ThumbnailRotation[i] = new GVRSceneObject(mGVRContext);
                GVRSceneObject obj = new GVRSceneObject(mGVRContext);
                ThumbnailGlasses[i] = new GVRRenderData(mGVRContext);
                ThumbnailGlasses[i].setMesh(glass_mesh);
                ThumbnailGlasses[i].setMaterial(mReflectionMaterial);
                obj.attachRenderData(ThumbnailGlasses[i]);
                obj.getRenderData().setRenderingOrder(ThumbnailOrder[i]);
                ThumbnailRotation[i].addChildObject(obj);
                ThumbnailObject[i].addChildObject(ThumbnailRotation[i]);

                Thumbnails[i] = new GVRSceneObject(mGVRContext, board_mesh,
                        ThumbnailTextures[i]);
                Thumbnails[i].getRenderData().setRenderingOrder(
                        ThumbnailOrder[i] - 100);
                Thumbnails[i].getRenderData().setCullTest(false);
                Thumbnails[i].getTransform().setScale(1.0f, 1.2f, 1.0f);
                ThumbnailRotation[i].addChildObject(Thumbnails[i]);

                ThumbnailObject[i].getTransform().setPosition(
                        ThumbnailTargetPosition[i][0],
                        ThumbnailTargetPosition[i][1],
                        ThumbnailTargetPosition[i][2]);
                mainScene.addSceneObject(ThumbnailObject[i]);

                GVREyePointeeHolder eyePointeeHolder = new GVREyePointeeHolder(
                        gvrContext);
                GVRMeshEyePointee eyePointee = new GVRMeshEyePointee(
                        gvrContext, picks_mesh);
                eyePointeeHolder.addPointee(eyePointee);
                ThumbnailObject[i].attachEyePointeeHolder(eyePointeeHolder);
            }

            GVRTexture m360 = mGVRContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "env.jpg"));
            GVRMesh sphere = mGVRContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "sphere.obj"));

            GVRSceneObject env_object = new GVRSceneObject(mGVRContext, sphere,
                    m360);
            env_object.getRenderData().setCullTest(false);
            mainScene.addSceneObject(env_object);

            GVRSceneObject headTracker = new GVRSceneObject(gvrContext,
                    gvrContext.createQuad(0.1f, 0.1f),
                    gvrContext.loadTexture(new GVRAndroidResource(mGVRContext,
                            "Headtracking_pointer.png")));
            headTracker.getTransform().setPosition(0.0f, 0.0f, -EYE_TO_OBJECT);
            headTracker.getRenderData().setDepthTest(false);
            headTracker.getRenderData().setRenderingOrder(100000);
            mainScene.getMainCameraRig().addChildObject(headTracker);
        } catch (IOException e) {
            e.printStackTrace();
            mActivity.finish();
            Log.e(TAG, "Assets were not loaded. Stopping application!");
        }
        // activity was stored in order to stop the application if the mesh is
        // not loaded. Since we don't need anymore, we set it to null to reduce
        // chance of memory leak.
        // mActivity = null;
    }

    @Override
    public void onStep() {
        FPSCounter.tick();
        // if(((ViewerActivity)mActivity).b==null){
        // b = BitmapFactory.decodeResource(mcontext.getResources(),

        // ((ViewerActivity)mActivity).b =
        // Bitmap.createBitmap(view.getDrawingCache(false), 0, 0,
        // ((ViewerActivity)mActivity).b.getWidth(),
        // ((ViewerActivity)mActivity).b.getHeight(), matrix, false);
        // view.setDrawingCacheEnabled(false);
        // }

        boolean isButtonDown = mIsButtonDown;
        boolean isSingleTapped = mIsSingleTapped;
        mIsButtonDown = false;
        mIsSingleTapped = false;
        // Canvas c = canvas;
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // Canvas glAttachedCanvas = refresh();
        // if(glAttachedCanvas != null) {

        // translate canvas to reflect view scrolling
        // glAttachedCanvas.translate(-getScrollX(), -getScrollY());
        // draw the view to provided canvas
        // glAttachedCanvas.drawColor(0);
        // glAttachedCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
        // Bitmap b = (((ViewerActivity)mActivity).game).b;
        /*
         * Bitmap b =
         * ((AndroidGraphics)((ViewerActivity2)mActivity).getGraphics(
         * )).mBitmap; // if(b!= null){ Paint p = new Paint(); p.setAlpha(255);
         * 
         * 
         * // glAttachedCanvas.drawBitmap(b, new Matrix(), p); }
         */
        // view.draw(glAttachedCanvas);

        // super.draw(glAttachedCanvas);
        // draw(glAttachedCanvas);
        // postrefresh(glAttachedCanvas);
        // }
        if (mSurfaceTexture != null) {
            // mSurfaceTexture.release();
            // mSurfaceTexture.releaseTexImage();
            // mSurfaceTexture.updateTexImage();

        }

        // GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, libGDxTexture);
        if (isButtonDown)
            mGVRContext.getMainScene().getMainCameraRig().resetYaw();

        // ---------------------------------------thumbnail motion
        boolean MoveActive = false;
        if (Math.abs(ThumbnailTargetPosition[ThumbnailTargetIndex[0]][0]
                - ThumbnailCurrentPosition[0][0]) < 0.2f)
            MoveActive = true;

        if ((SelectionActive && MoveActive) || forcerotate) {
            if (ThumbnailCurrentPosition[ThumbnailSelected][0] < -0.5f) {
                for (int i = 0; i < THUMBNAIL_NUM; i++) {
                    ThumbnailTargetIndex[i]++;
                    if (ThumbnailTargetIndex[i] >= THUMBNAIL_NUM)
                        ThumbnailTargetIndex[i] = ThumbnailTargetIndex[i]
                                - THUMBNAIL_NUM;
                    if (ThumbnailTargetIndex[i] < 0)
                        ThumbnailTargetIndex[i] = ThumbnailTargetIndex[i]
                                + THUMBNAIL_NUM;
                }
            } else if (ThumbnailCurrentPosition[ThumbnailSelected][0] > 0.5f) {
                for (int i = 0; i < THUMBNAIL_NUM; i++) {
                    ThumbnailTargetIndex[i]--;
                    if (ThumbnailTargetIndex[i] >= THUMBNAIL_NUM)
                        ThumbnailTargetIndex[i] = ThumbnailTargetIndex[i]
                                - THUMBNAIL_NUM;
                    if (ThumbnailTargetIndex[i] < 0)
                        ThumbnailTargetIndex[i] = ThumbnailTargetIndex[i]
                                + THUMBNAIL_NUM;
                }
            }
        }

        for (int i = 0; i < THUMBNAIL_NUM; i++) {
            float speed = 0.08f;
            if (forcerotate) {
                // Log.e("datta","force rotating");
                speed = 0.25f;
            }
            for (int j = 0; j < 3; j++)
                ThumbnailCurrentPosition[i][j] += speed
                        * (ThumbnailTargetPosition[ThumbnailTargetIndex[i]][j] - ThumbnailCurrentPosition[i][j]);
            ThumbnailObject[i].getTransform().setPosition(
                    ThumbnailCurrentPosition[i][0],
                    ThumbnailCurrentPosition[i][1],
                    ThumbnailCurrentPosition[i][2]);
        }

        // if(
        // Math.abs(ThumbnailTargetPosition[ThumbnailTargetIndex[0]][0]-ThumbnailCurrentPosition[0][0])
        // > 0.02f )
        // {
        // if( THUMBNAIL_ROT > 180.0f )
        // THUMBNAIL_ROT += 0.05f*( 360.0f - THUMBNAIL_ROT );
        // else
        // THUMBNAIL_ROT += 0.05f*( 0.0f - THUMBNAIL_ROT );
        // }
        // else
        // {
        // THUMBNAIL_ROT += -1.0f;
        // if( THUMBNAIL_ROT > 360.0f ) THUMBNAIL_ROT = THUMBNAIL_ROT - 360.0f;
        // }
        THUMBNAIL_ROT = -1.0f;
        for (int i = 0; i < THUMBNAIL_NUM; i++)
            // ThumbnailRotation[i].getTransform().rotateByAxis(THUMBNAIL_ROT,
            // 0.0f, 1.0f, 0.0f);

            // ---------------------------------------object motion

            OBJECT_ROT = -1.0f;
        for (int i = 0; i < THUMBNAIL_NUM; i++) {
            if (rotatex - lastrotatex != 0.0f) {
                // Objects[i].getTransform()
                // .rotateByAxis(rotatex-lastrotatex, 1.0f, 0.0f, 0.0f);

                // Objects[i].getTransform()
                // .setRotationByAxis(rotatex, 1.0f, 0.0f, 0.0f);
                // Log.e("datta","x rotating");

            } else if (rotatey - lastrotatey != 0.0f) {
                // Objects[i].getTransform()
                // .rotateByAxis(rotatey-lastrotatey, 0.0f, 1.0f, 0.0f);

                Objects[i].getTransform().setRotationByAxis(rotatey, 0.0f,
                        1.0f, 0.0f);
                // Log.e("datta","y rotating");
            } else if (rotatez - lastrotatez != 0.0f) {
                // Objects[i].getTransform()
                // .rotateByAxis(rotatez-lastrotatez, 0.0f, 0.0f, 1.0f);

                // Objects[i].getTransform()
                // .setRotationByAxis(rotatez, 0.0f, 0.0f, 1.0f);
                // Log.e("datta","z rotating");
            } else if (resetrotate) {

                /*
                 * Objects[i].getTransform()
                 * .rotateByAxis(-Objects[i].getTransform().getRotationZ(),
                 * 0.0f, 0.0f, 1.0f); Objects[i].getTransform()
                 * .rotateByAxis(-Objects[i].getTransform().getRotationY(),
                 * 0.0f, 1.0f, 0.0f); Objects[i].getTransform()
                 * .rotateByAxis(-Objects[i].getTransform().getRotationX(),
                 * 1.0f, 0.0f, 0.0f);
                 */

                lastrotatez = rotatez = 0.0f;
                lastrotatex = rotatex = 0.0f;
                lastrotatey = rotatey = 0.0f;
                resetrotate = false;
                Objects[i].getTransform().setRotation(0.0f, 0.0f, 0.0f, 0.0f);
            }
            Objects[i].getTransform().setPosition(0.0f, 0.0f,
                    -EYE_TO_OBJECT + zoomlevel);
            if (i == 3 || i == 1) {
                if (i == 1)
                    Objects[i].getTransform().setPosition(0.7f, -1.0f,
                            -EYE_TO_OBJECT - 2.0f + zoomlevel);
                if (i == 3) {
                    if (mLookInside) {
                        Objects[i].getTransform().setPosition(0.0f, -2.0f,
                                -EYE_TO_OBJECT + EYE_TO_OBJECT);
                        // widgetbuttonObject.getRenderData().setRenderMask(0);
                    } else {
                        Objects[i].getTransform().setPosition(2.0f, -1.5f,

                        -EYE_TO_OBJECT - 3.0f + zoomlevel);
                        // widgetbuttonObject.getRenderData().setRenderMask((GVRRenderMaskBit.Left
                        // | GVRRenderMaskBit.Right));
                    }
                }
                switch (color) {
                case 1:
                    mCarBodyMaterial.setTexture(PhongShader3.TEXTURE_KEY,
                            mDefaultColorTex);
                    mPhongMaterial.setTexture(PhongShader3.TEXTURE_KEY,
                            mDefaultColorTex);
                    break;
                case 2:
                    mCarBodyMaterial.setTexture(PhongShader3.TEXTURE_KEY,
                            mBlackTex);
                    mPhongMaterial.setTexture(PhongShader3.TEXTURE_KEY,
                            mBlackTex);
                    break;
                case 3:
                    mCarBodyMaterial.setTexture(PhongShader3.TEXTURE_KEY,
                            mBlueTex);
                    mPhongMaterial.setTexture(PhongShader3.TEXTURE_KEY,
                            mBlueTex);
                    break;
                case 4:
                    mCarBodyMaterial.setTexture(PhongShader3.TEXTURE_KEY,
                            mGreenTex);
                    mPhongMaterial.setTexture(PhongShader3.TEXTURE_KEY,
                            mGreenTex);
                    break;
                case 5:
                    mCarBodyMaterial.setTexture(PhongShader3.TEXTURE_KEY,
                            mSilverTex);
                    mPhongMaterial.setTexture(PhongShader3.TEXTURE_KEY,
                            mSilverTex);
                    break;

                }
            }

        }
        // Objects[i].getTransform().rotate(0.0f, rotatex-lastrotatex,
        // rotatey-lastrotatey, rotatez-lastrotatez);
        // Objects[i].getTransform().rotate(0.0f, .0f, 0.5f, 0.5f);
        lastrotatez = rotatez;
        lastrotatex = rotatex;
        lastrotatey = rotatey;

        float[] light = new float[4];
        light[0] = 6.0f;
        light[1] = 10.0f;
        light[2] = 10.0f;
        light[3] = 1.0f;

        float[] eye = new float[4];
        eye[0] = 0.0f;
        eye[1] = 0.0f;
        eye[2] = 3.0f * EYE_TO_OBJECT;
        eye[3] = 1.0f;

        float[] matT = ThumbnailRotation[0].getTransform().getModelMatrix();
        float[] matO = Objects[ThumbnailSelected].getTransform()
                .getModelMatrix();

        // ---------------------------- watch, jar

        float x = matO[0] * light[0] + matO[1] * light[1] + matO[2] * light[2]
                + matO[3] * light[3];
        float y = matO[4] * light[0] + matO[5] * light[1] + matO[6] * light[2]
                + matO[7] * light[3];
        float z = matO[8] * light[0] + matO[9] * light[1] + matO[10] * light[2]
                + matO[11] * light[3];

        float mag = (float) Math.sqrt(x * x + y * y + z * z);

        mMetalMaterial.setVec3(MetalOnlyShader.LIGHT_KEY, x / mag, y / mag, z
                / mag);
        mDiffuseMaterial.setVec3(DiffuseShader.LIGHT_KEY, x / mag, y / mag, z
                / mag);
        mGlassMaterial
                .setVec3(GlassShader.LIGHT_KEY, x / mag, y / mag, z / mag);
        // mPhongMaterial
        // .setVec3(PhongShader.LIGHT_KEY, x / mag, y / mag, z / mag);

        mPhongMaterial
                .setVec3(PhongShader.LIGHT_KEY, x / mag, y / mag, z / mag);

        x = matO[0] * eye[0] + matO[1] * eye[1] + matO[2] * eye[2] + matO[3]
                * eye[3];
        y = matO[4] * eye[0] + matO[5] * eye[1] + matO[6] * eye[2] + matO[7]
                * eye[3];
        z = matO[8] * eye[0] + matO[9] * eye[1] + matO[10] * eye[2] + matO[11]
                * eye[3];

        mag = (float) Math.sqrt(x * x + y * y + z * z);

        mMetalMaterial.setVec3(MetalOnlyShader.EYE_KEY, x / mag, y / mag, z
                / mag);
        mDiffuseMaterial.setVec3(DiffuseShader.EYE_KEY, x / mag, y / mag, z
                / mag);
        mGlassMaterial.setVec3(GlassShader.EYE_KEY, x / mag, y / mag, z / mag);
        mPhongMaterial.setVec3(PhongShader.EYE_KEY, x / mag, y / mag, z / mag);

        // ---------------------------- robot

        mRobotHeadMaterial.setVec4(PhongShader3.MAT1_KEY, matO[0], matO[4],
                matO[8], matO[12]);
        mRobotHeadMaterial.setVec4(PhongShader3.MAT2_KEY, matO[1], matO[5],
                matO[9], matO[13]);
        mRobotHeadMaterial.setVec4(PhongShader3.MAT3_KEY, matO[2], matO[6],
                matO[10], matO[14]);
        mRobotHeadMaterial.setVec4(PhongShader3.MAT4_KEY, matO[3], matO[7],
                matO[11], matO[15]);
        mRobotHeadMaterial.setVec3(PhongShader3.LIGHT_KEY, light[0], light[1],
                light[2]);
        mRobotHeadMaterial
                .setVec3(PhongShader3.EYE_KEY, eye[0], eye[1], eye[2]);

        mRobotMetalMaterial.setVec4(MetalShader2.MAT1_KEY, matO[0], matO[4],
                matO[8], matO[12]);
        mRobotMetalMaterial.setVec4(MetalShader2.MAT2_KEY, matO[1], matO[5],
                matO[9], matO[13]);
        mRobotMetalMaterial.setVec4(MetalShader2.MAT3_KEY, matO[2], matO[6],
                matO[10], matO[14]);
        mRobotMetalMaterial.setVec4(MetalShader2.MAT4_KEY, matO[3], matO[7],
                matO[11], matO[15]);
        mRobotMetalMaterial.setVec3(MetalShader2.LIGHT_KEY, light[0], light[1],
                light[2]);
        mRobotMetalMaterial.setVec3(MetalShader2.EYE_KEY, eye[0], eye[1],
                eye[2]);

        mRobotBodyMaterial.setVec4(PhongShader2.MAT1_KEY, matO[0], matO[4],
                matO[8], matO[12]);
        mRobotBodyMaterial.setVec4(PhongShader2.MAT2_KEY, matO[1], matO[5],
                matO[9], matO[13]);
        mRobotBodyMaterial.setVec4(PhongShader2.MAT3_KEY, matO[2], matO[6],
                matO[10], matO[14]);
        mRobotBodyMaterial.setVec4(PhongShader2.MAT4_KEY, matO[3], matO[7],
                matO[11], matO[15]);
        mRobotBodyMaterial.setVec3(PhongShader2.LIGHT_KEY, light[0], light[1],
                light[2]);
        mRobotBodyMaterial
                .setVec3(PhongShader2.EYE_KEY, eye[0], eye[1], eye[2]);

        mRobotRubberMaterial.setVec4(DiffuseShader2.MAT1_KEY, matO[0], matO[4],
                matO[8], matO[12]);
        mRobotRubberMaterial.setVec4(DiffuseShader2.MAT2_KEY, matO[1], matO[5],
                matO[9], matO[13]);
        mRobotRubberMaterial.setVec4(DiffuseShader2.MAT3_KEY, matO[2], matO[6],
                matO[10], matO[14]);
        mRobotRubberMaterial.setVec4(DiffuseShader2.MAT4_KEY, matO[3], matO[7],
                matO[11], matO[15]);
        mRobotRubberMaterial.setVec3(DiffuseShader2.LIGHT_KEY, light[0],
                light[1], light[2]);
        mRobotRubberMaterial.setVec3(DiffuseShader2.EYE_KEY, eye[0], eye[1],
                eye[2]);

        // ---------------------------- leaf

        mLeafBodyMaterial.setVec4(MetalShader2.MAT1_KEY, matO[0], matO[4],
                matO[8], matO[12]);
        mLeafBodyMaterial.setVec4(MetalShader2.MAT2_KEY, matO[1], matO[5],
                matO[9], matO[13]);
        mLeafBodyMaterial.setVec4(MetalShader2.MAT3_KEY, matO[2], matO[6],
                matO[10], matO[14]);
        mLeafBodyMaterial.setVec4(MetalShader2.MAT4_KEY, matO[3], matO[7],
                matO[11], matO[15]);
        mLeafBodyMaterial.setVec3(MetalShader2.LIGHT_KEY, light[0], light[1],
                light[2]);
        mLeafBodyMaterial.setVec3(MetalShader2.EYE_KEY, eye[0], eye[1], eye[2]);

        mLeafBoxMaterial.setVec4(PhongShader3.MAT1_KEY, matO[0], matO[4],
                matO[8], matO[12]);
        mLeafBoxMaterial.setVec4(PhongShader3.MAT2_KEY, matO[1], matO[5],
                matO[9], matO[13]);
        mLeafBoxMaterial.setVec4(PhongShader3.MAT3_KEY, matO[2], matO[6],
                matO[10], matO[14]);
        mLeafBoxMaterial.setVec4(PhongShader3.MAT4_KEY, matO[3], matO[7],
                matO[11], matO[15]);
        mLeafBoxMaterial.setVec3(PhongShader3.LIGHT_KEY, light[0], light[1],
                light[2]);
        mLeafBoxMaterial.setVec3(PhongShader3.EYE_KEY, eye[0], eye[1], eye[2]);

        // ---------------------------- car
        eye[0] = 4.0f;
        eye[1] = 0.0f;
        eye[2] = 3.0f * EYE_TO_OBJECT;
        eye[3] = 1.0f;

        mCarBodyMaterial.setVec4(PhongShader3.MAT1_KEY, matO[0], matO[4],
                matO[8], matO[12]);
        mCarBodyMaterial.setVec4(PhongShader3.MAT2_KEY, matO[1], matO[5],
                matO[9], matO[13]);
        mCarBodyMaterial.setVec4(PhongShader3.MAT3_KEY, matO[2], matO[6],
                matO[10], matO[14]);
        mCarBodyMaterial.setVec4(PhongShader3.MAT4_KEY, matO[3], matO[7],
                matO[11], matO[15]);
        mCarBodyMaterial.setVec3(PhongShader3.LIGHT_KEY, light[0], light[1],
                light[2]);
        mCarBodyMaterial.setVec3(PhongShader3.EYE_KEY, eye[0], eye[1], eye[2]);

        mCarTireMaterial.setVec4(DiffuseShader2.MAT1_KEY, matO[0], matO[4],
                matO[8], matO[12]);
        mCarTireMaterial.setVec4(DiffuseShader2.MAT2_KEY, matO[1], matO[5],
                matO[9], matO[13]);
        mCarTireMaterial.setVec4(DiffuseShader2.MAT3_KEY, matO[2], matO[6],
                matO[10], matO[14]);
        mCarTireMaterial.setVec4(DiffuseShader2.MAT4_KEY, matO[3], matO[7],
                matO[11], matO[15]);
        mCarTireMaterial.setVec3(DiffuseShader2.LIGHT_KEY, light[0], light[1],
                light[2]);
        mCarTireMaterial
                .setVec3(DiffuseShader2.EYE_KEY, eye[0], eye[1], eye[2]);

        mCarGlassMaterial.setVec4(GlassShader2.MAT1_KEY, matO[0], matO[4],
                matO[8], matO[12]);
        mCarGlassMaterial.setVec4(GlassShader2.MAT2_KEY, matO[1], matO[5],
                matO[9], matO[13]);
        mCarGlassMaterial.setVec4(GlassShader2.MAT3_KEY, matO[2], matO[6],
                matO[10], matO[14]);
        mCarGlassMaterial.setVec4(GlassShader2.MAT4_KEY, matO[3], matO[7],
                matO[11], matO[15]);
        mCarGlassMaterial.setVec3(GlassShader2.LIGHT_KEY, light[0], light[1],
                light[2]);
        mCarGlassMaterial.setVec3(GlassShader2.EYE_KEY, eye[0], eye[1], eye[2]);

        mCarWheelMaterial.setVec4(MetalShader2.MAT1_KEY, matO[0], matO[4],
                matO[8], matO[12]);
        mCarWheelMaterial.setVec4(MetalShader2.MAT2_KEY, matO[1], matO[5],
                matO[9], matO[13]);
        mCarWheelMaterial.setVec4(MetalShader2.MAT3_KEY, matO[2], matO[6],
                matO[10], matO[14]);
        mCarWheelMaterial.setVec4(MetalShader2.MAT4_KEY, matO[3], matO[7],
                matO[11], matO[15]);
        mCarWheelMaterial.setVec3(MetalShader2.LIGHT_KEY, light[0], light[1],
                light[2]);
        mCarWheelMaterial.setVec3(MetalShader2.EYE_KEY, eye[0], eye[1], eye[2]);

        mCarBackMaterial.setVec4(DiffuseShader2.MAT1_KEY, matO[0], matO[4],
                matO[8], matO[12]);
        mCarBackMaterial.setVec4(DiffuseShader2.MAT2_KEY, matO[1], matO[5],
                matO[9], matO[13]);
        mCarBackMaterial.setVec4(DiffuseShader2.MAT3_KEY, matO[2], matO[6],
                matO[10], matO[14]);
        mCarBackMaterial.setVec4(DiffuseShader2.MAT4_KEY, matO[3], matO[7],
                matO[11], matO[15]);
        mCarBackMaterial.setVec3(DiffuseShader2.LIGHT_KEY, light[0], light[1],
                light[2]);
        mCarBackMaterial
                .setVec3(DiffuseShader2.EYE_KEY, eye[0], eye[1], eye[2]);

        mCarGrillMaterial.setVec4(DiffuseShader2.MAT1_KEY, matO[0], matO[4],
                matO[8], matO[12]);
        mCarGrillMaterial.setVec4(DiffuseShader2.MAT2_KEY, matO[1], matO[5],
                matO[9], matO[13]);
        mCarGrillMaterial.setVec4(DiffuseShader2.MAT3_KEY, matO[2], matO[6],
                matO[10], matO[14]);
        mCarGrillMaterial.setVec4(DiffuseShader2.MAT4_KEY, matO[3], matO[7],
                matO[11], matO[15]);
        mCarGrillMaterial.setVec3(DiffuseShader2.LIGHT_KEY, light[0], light[1],
                light[2]);
        mCarGrillMaterial.setVec3(DiffuseShader2.EYE_KEY, eye[0], eye[1],
                eye[2]);

        mCarLightMaterial.setVec4(GlassShader2.MAT1_KEY, matO[0], matO[4],
                matO[8], matO[12]);
        mCarLightMaterial.setVec4(GlassShader2.MAT2_KEY, matO[1], matO[5],
                matO[9], matO[13]);
        mCarLightMaterial.setVec4(GlassShader2.MAT3_KEY, matO[2], matO[6],
                matO[10], matO[14]);
        mCarLightMaterial.setVec4(GlassShader2.MAT4_KEY, matO[3], matO[7],
                matO[11], matO[15]);
        mCarLightMaterial.setVec3(GlassShader2.LIGHT_KEY, light[0], light[1],
                light[2]);
        mCarLightMaterial.setVec3(GlassShader2.EYE_KEY, eye[0], eye[1], eye[2]);

        mCarInsideMaterial.setVec4(PhongShader2.MAT1_KEY, matO[0], matO[4],
                matO[8], matO[12]);
        mCarInsideMaterial.setVec4(PhongShader2.MAT2_KEY, matO[1], matO[5],
                matO[9], matO[13]);
        mCarInsideMaterial.setVec4(PhongShader2.MAT3_KEY, matO[2], matO[6],
                matO[10], matO[14]);
        mCarInsideMaterial.setVec4(PhongShader2.MAT4_KEY, matO[3], matO[7],
                matO[11], matO[15]);
        mCarInsideMaterial.setVec3(PhongShader2.LIGHT_KEY, light[0], light[1],
                light[2]);
        mCarInsideMaterial
                .setVec3(PhongShader2.EYE_KEY, eye[0], eye[1], eye[2]);

        // ---------------------------- thumbnail glasses

        eye[0] = 0.0f;
        eye[1] = 0.0f;
        eye[2] = EYE_TO_OBJECT;
        eye[3] = 1.0f;

        mReflectionMaterial.setVec4(ReflectionShader.MAT1_KEY, matT[0],
                matT[4], matT[8], matT[12]);
        mReflectionMaterial.setVec4(ReflectionShader.MAT2_KEY, matT[1],
                matT[5], matT[9], matT[13]);
        mReflectionMaterial.setVec4(ReflectionShader.MAT3_KEY, matT[2],
                matT[6], matT[10], matT[14]);
        mReflectionMaterial.setVec4(ReflectionShader.MAT4_KEY, matT[3],
                matT[7], matT[11], matT[15]);
        mReflectionMaterial.setVec3(ReflectionShader.LIGHT_KEY, light[0],
                light[1], light[2]);
        mReflectionMaterial.setVec3(ReflectionShader.EYE_KEY, eye[0], eye[1],
                eye[2]);

        GVREyePointeeHolder pickedHolders[] = GVRPicker.pickScene(mGVRContext
                .getMainScene());
        if (/* SelectionMode && */pickedHolders.length > 0) {
            GVRSceneObject pickedObject = pickedHolders[0].getOwnerObject();
            coords = pickedObject.getEyePointeeHolder().getHit();
            mActivity.setPickedObject(pickedObject);
            for (int i = 0; i < THUMBNAIL_NUM; ++i) {
                if (ThumbnailObject[i].equals(pickedObject)) {
                    // ThumbnailSelected = i;
                    // buttonpointed = false;
                    // button2pointed = false;
                    // forcerotate = false;
                    // Log.e("datta", "button not pointed to!!!");
                    // break;
                    objectpointed = true;
                }
            }
            if (widgetbuttonObject.equals(pickedObject)) {
                objectpointed = false;
                // Log.e("datta", "button pointed to!!!");
                // ThumbnailSelected = 1+(ThumbnailSelected%4);
                long downTime = SystemClock.uptimeMillis();
                long eventTime = SystemClock.uptimeMillis() + 100;
                float x1 = 0.0f;
                float y1 = 0.0f;

                // ((ViewerActivity2)mActivity).game.setX(512 * ((coords[0]
                // +1.0f)/2.0f));

                // List of meta states found here:
                // developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
                int metaState = 0;
                /*
                 * MotionEvent motionEvent = MotionEvent.obtain( downTime,
                 * eventTime, MotionEvent.ACTION_MOVE, x1, y1, metaState );
                 * ((ViewerActivity
                 * )mActivity).getInput().onTouch(((ViewerActivity
                 * )mActivity).graphics.getView(), motionEvent);
                 */
                buttonpointed = true;
                button2pointed = false;
                // break;
            } else if (widgetbuttonObject2.equals(pickedObject)/*
                                                                * || (coords[0]
                                                                * > 1.0f &&
                                                                * coords[0] <
                                                                * 2.0f &&
                                                                * coords[1] >
                                                                * -1.0f &&
                                                                * coords[1] <
                                                                * 1.0f)
                                                                */) {
                objectpointed = false;
                // Log.e("datta",
                // "coords+"+coords[0]+" "+coords[1]+" "+coords[2]+"ThumbnailSelected"+ThumbnailSelected);
                // ThumbnailSelected = 1+(ThumbnailSelected%4);
                // long downTime = SystemClock.uptimeMillis();
                // long eventTime = SystemClock.uptimeMillis() + 100;
                // float x1 = 0.0f;
                // float y1 = 0.0f;
                // / coords = pickedObject.getEyePointeeHolder().getHit();
                // ((ViewerActivity2)mActivity).game.setX(512 * ((coords[0]
                // +1.0f)/2.0f));
                // Log.e("datta",
                // "coords+"+coords[0]+" "+coords[1]+" "+coords[2]);
                // List of meta states found here:
                // developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
                // int metaState = 0;
                /*
                 * MotionEvent motionEvent = MotionEvent.obtain( downTime,
                 * eventTime, MotionEvent.ACTION_MOVE, x1, y1, metaState );
                 * ((ViewerActivity
                 * )mActivity).getInput().onTouch(((ViewerActivity
                 * )mActivity).graphics.getView(), motionEvent);
                 */
                button2pointed = true;
                buttonpointed = false;
                // break;
            } else {

                buttonpointed = false;
                button2pointed = false;
            }
            // }
            SelectionActive = true;
        } else {
            objectpointed = false;
            SelectionActive = false;
            buttonpointed = false;
            button2pointed = false;
        }

        if (isSingleTapped || true) {
            // if(widgetZ <= -1.0f)
            // widgetZ += 0.05f;
            // widgetbuttonObject.getTransform().setPosition(0, -1, -widgetZ);
            // forcerotate = !forcerotate;
            if (buttonpointed || button2pointed) {

                SelectionMode = false;
            } else
                SelectionMode = !SelectionMode;
            SelectionMode = false;// forcefully setting false
            if (SelectionMode) {
                for (int i = 0; i < THUMBNAIL_NUM; ++i) {
                    ThumbnailGlasses[i].setRenderMask(GVRRenderMaskBit.Left
                            | GVRRenderMaskBit.Right);
                    Thumbnails[i].getRenderData().setRenderMask(
                            GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);
                }
                for (int I = 0; I < THUMBNAIL_NUM; I++)
                    for (int i = 0; i < Objects[I].getChildrenCount(); i++)
                        Objects[I].getChildByIndex(i).getRenderData()
                                .setRenderMask(0);
            } else {
                for (int i = 0; i < THUMBNAIL_NUM; ++i) {
                    ThumbnailGlasses[i].setRenderMask(0);
                    Thumbnails[i].getRenderData().setRenderMask(0);
                }
                for (int I = 0; I < THUMBNAIL_NUM; I++)
                    for (int i = 0; i < Objects[I].getChildrenCount(); i++)
                        Objects[I].getChildByIndex(i).getRenderData()
                                .setRenderMask(0);
                for (int i = 0; i < Objects[ThumbnailSelected]
                        .getChildrenCount(); i++)
                    Objects[ThumbnailSelected]
                            .getChildByIndex(i)
                            .getRenderData()
                            .setRenderMask(
                                    GVRRenderMaskBit.Left
                                            | GVRRenderMaskBit.Right);
            }
        } else {
            // if(widgetZ >= -2.4f)
            // widgetZ -= 0.05f;
            // widgetbuttonObject.getTransform().setPosition(0, -1, -widgetZ);
        }
    }

    public void onButtonDown() {
        mIsButtonDown = true;
    }

    public void onSingleTap(MotionEvent e) {
        mIsSingleTapped = true;
        if (ThumbnailSelected == 3 && mLookInside)
            mLookInside = false;
    }

    public Canvas refresh() {
        if (widgetbuttonObject == null)
            return null;
        Canvas canvas = null;
        try {

            canvas = mSurface.lockCanvas(null);

        } catch (Surface.OutOfResourcesException t) {
            Log.e("GVRWebBoardObject", "lockCanvas failed");
        }
        return canvas;

    }

    public void postrefresh(Canvas c) {
        mSurface.unlockCanvasAndPost(c);
        synchronized (this) {
            //
        }
    }
}