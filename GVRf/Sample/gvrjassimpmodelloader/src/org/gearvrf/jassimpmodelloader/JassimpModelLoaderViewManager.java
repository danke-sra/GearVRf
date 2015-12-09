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

package org.gearvrf.jassimpmodelloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.keyframe.GVRKeyFrameAnimation;
import org.gearvrf.jassimp2.GVRJassimpSceneObject;
import org.gearvrf.utility.Log;

import android.graphics.Color;

public class JassimpModelLoaderViewManager extends GVRScript {

    @SuppressWarnings("unused")
    private static final String TAG = Log
            .tag(JassimpModelLoaderViewManager.class);

    private GVRAnimationEngine mAnimationEngine;
    private GVRScene mMainScene;

    @Override
    public void onInit(GVRContext gvrContext) throws IOException {

        mAnimationEngine = gvrContext.getAnimationEngine();

        mMainScene = gvrContext.getNextMainScene(new Runnable() {

            @Override
            public void run() {
                for (GVRAnimation animation : mAnimations) {
                    animation.start(mAnimationEngine);
                }
                mAnimations = null;
            }
        });

        // Apply frustum culling
        mMainScene.setFrustumCulling(true);

        GVRCameraRig mainCameraRig = mMainScene.getMainCameraRig();
        mainCameraRig.getLeftCamera().setBackgroundColor(Color.BLACK);
        mainCameraRig.getRightCamera().setBackgroundColor(Color.BLACK);
        mainCameraRig.getTransform().setPosition(0.0f, 0.0f, 0.0f);

        // Model with texture and animation
        GVRJassimpSceneObject astroBoyModel = (GVRJassimpSceneObject) gvrContext.loadJassimpModel("astro_boy.dae");
        List<GVRKeyFrameAnimation> animations = astroBoyModel.getAnimations();
        if (animations.size() >= 1) {
            setup(animations.get(0));
        }

        ModelPosition astroBoyModelPosition = new ModelPosition();
        astroBoyModelPosition.setPosition(0.0f, -0.4f, -0.5f);

        astroBoyModel.getTransform().setRotationByAxis(45.0f, 0.0f, 1.0f, 0.0f);
        astroBoyModel.getTransform().setScale(3, 3, 3);
        astroBoyModel.getTransform().setPosition(astroBoyModelPosition.x,
                astroBoyModelPosition.y, astroBoyModelPosition.z);

        // Model with color
        GVRSceneObject benchModel = gvrContext.loadJassimpModel("bench.dae");

        ModelPosition benchModelPosition = new ModelPosition();
        benchModelPosition.setPosition(0.0f, -4.0f, -30.0f);

        benchModel.getTransform().setPosition(benchModelPosition.x,
                benchModelPosition.y, benchModelPosition.z);
        benchModel.getTransform()
        .setRotationByAxis(180.0f, 0.0f, 1.0f, 0.0f);

        mMainScene.addSceneObject(astroBoyModel);
        mMainScene.addSceneObject(benchModel);
    }

    @Override
    public void onStep() {
    }

    void onTap() {
        // toggle whether stats are displayed.
        boolean statsEnabled = mMainScene.getStatsEnabled();
        mMainScene.setStatsEnabled(!statsEnabled);
    }

    private List<GVRAnimation> mAnimations = new ArrayList<GVRAnimation>();

    private void setup(GVRAnimation animation) {
        animation.setRepeatMode(GVRRepeatMode.REPEATED).setRepeatCount(-1);
        mAnimations.add(animation);
    }
}

class ModelPosition {
    float x;
    float y;
    float z;

    void setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
