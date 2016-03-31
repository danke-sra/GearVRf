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

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRWidgetViewer.R;
import org.gearvrf.plugins.widget.GVRWidgetPlugin;
import org.gearvrf.util.VRTouchPadGestureDetector;
import org.gearvrf.util.VRTouchPadGestureDetector.OnTouchPadGestureListener;
import org.gearvrf.util.VRTouchPadGestureDetector.SwipeDirection;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class GVRWidgetViewer extends GVRActivity implements
        OnTouchPadGestureListener {

	private GVRWidgetPlugin mPlugin = new GVRWidgetPlugin(this);

    private static final int BUTTON_INTERVAL = 500;
    private static final int TAP_INTERVAL = 300;
    private long mLatestButton = 0;
    private long mLatestTap = 0;
    private ViewerScript mScript = null;
    private VRTouchPadGestureDetector mDetector = null;
    public View view = null;
    public Bitmap b = null;
    public Bitmap scene2d = null;
    GLWebView webview;
    public MyGdxWidget widget;
    GLSurfaceView glview;
    float downx, downy;
    float downabsx, downabsy;
    float yangle = 0.0f;
    boolean movestart = false;
    float moveofset = 0.0f;

    // GVRWidgetPlugin mPlugin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // mPlugin = new GVRWidgetPlugin();
        // setcurrentPlugin(mPlugin);
        Point size = new Point();
        // getWindowManager().getDefaultDisplay().getSize(size);
        // width = size.x;
        // height = size.y;
        mPlugin.setViewSize(500, 500);
        // getWindowManager().getDefaultDisplay().getMetrics(null);
        mDetector = new VRTouchPadGestureDetector(this);
        scene2d = WidgetView.getInstance();
        LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.view_layout, null);
        webview = new GLWebView(getApplicationContext());

        String[] stringArray = new String[] { "Bright Mode", "Normal Mode",
                "XFerMode", "some other mode", "some other mode2",
                "some other mode3", "some other mode4", "some other mode5",
                "some other mode6", "some other mode7" };
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1,
                stringArray);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getApplicationContext(), android.R.layout.simple_list_item_1,
                stringArray) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view
                        .findViewById(android.R.id.text1);

                text.setTextColor(Color.WHITE);

                return view;
            }
        };

        webview.setAdapter(adapter);
        // webview.setBackgroundColor(Color.BLACK);
        // webview.setLayoutParams(new LayoutParams( 500, 500 ));
        // webview.measure(500, 500);
        webview.setLayoutMode(LayoutParams.WRAP_CONTENT);
        widget = new MyGdxWidget();
        mPlugin.initializeWidget(widget);

        // game = new SuperJumper();
        // mView =
        // initialize(new MyGdxGame(), new AndroidApplicationConfiguration());

        // webview = (GLWebView) view.findViewById(R.id.glWebView);
        // webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        Bitmap b = null;
        mScript = new ViewerScript(mPlugin,/* graphics.getView() */webview,
                scene2d);
        mPlugin.setCurrentScript(mScript);
        widget.mScript = mScript;

        webview.setViewToGLRenderer(mScript);
        // webview.setWillNotDraw(true);
        webview.setVisibility(View.INVISIBLE);
        //

        setScript(mScript, "gvr_note4.xml");

        // /addContentView( webview, createLayoutParams() );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.select:
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            Uri uri = Uri.parse(Environment.getExternalStorageDirectory()
                    .getPath());
            intent.setDataAndType(uri, "*/*");
            startActivityForResult(intent, 123);
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() > mLatestButton + BUTTON_INTERVAL) {
            mLatestButton = System.currentTimeMillis();
            mScript.onButtonDown();
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mLatestButton = System.currentTimeMillis();
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        // webview.onTouchEvent(event);
        // graphics.getView().onTouchEvent(event);

        /*
         * if(mScript.buttonpointed){
         * 
         * 
         * 
         * float x = 256 * ((mScript.coords[0] +2.5f));
         * 
         * float y = 512 * ((-mScript.coords[1] +1.0f)/2.0f);
         * 
         * 
         * if(event.getAction() == 0){
         * 
         * downabsx = event.getX(); downabsy = event.getY(); downx = x; downy =
         * y; }
         * 
         * 
         * if(event.getAction() == 2 || event.getAction() == 1){
         * 
         * x = event.getX() - downabsx+downx; y = event.getY() - downabsy+downy;
         * 
         * }
         * 
         * event.setLocation(x, y);
         * 
         * getInput().onTouch(getGraphics().getView(),event); }
         * 
         * if(mScript.button2pointed){
         * 
         * 
         * 
         * float x = 512 * ((mScript.coords[0]-0.5f )/2.0f);
         * 
         * float y = 512 * ((-mScript.coords[1] +1.0f)/2.0f);
         * 
         * if(event.getAction() == 0){
         * 
         * downabsx = event.getX(); downabsy = event.getY(); downx = x; downy =
         * y; }
         * 
         * 
         * if(event.getAction() == 2 || event.getAction() == 1){
         * 
         * x = event.getX() - downabsx+downx; y = event.getY() - downabsy+downy;
         * 
         * }
         * 
         * event.setLocation(x, y);
         * 
         * getInput().onTouch(getGraphics().getView(),event); }
         */
        if (mPlugin.getWidgetView() == null)
            return false;
        if (mScript.objectpointed) {
            float x = 0, dx = 0, y = 0, dy = 0.0f;

            if (event.getAction() == 0) {
                movestart = false;
                moveofset = 0.0f;
                x = event.getX();
                y = event.getY();
            }

            if (event.getAction() == 2 || event.getAction() == 1) {

                dx = event.getX() - x;
                dy = event.getY() - y;
                if (event.getAction() == 1) {
                    movestart = false;
                    moveofset = 0.0f;
                    yangle = mScript.rotatey;
                }

            }
            if (dx > dy && event.getAction() == 2) {
                widget.mcheckbox.setChecked(false);
                if (!movestart && moveofset == 0.0f)
                    movestart = true;

                if (movestart) {
                    moveofset = dx / 2;
                    movestart = false;
                    mScript.rotatey = (yangle) % 360;
                } else {
                    mScript.rotatey = (dx / 2 + yangle - moveofset) % 360;
                }

            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTap(MotionEvent e) {
        Log.v("", "onSingleTap");
        if (System.currentTimeMillis() > mLatestTap + TAP_INTERVAL) {
            mLatestTap = System.currentTimeMillis();
            // graphics.getView().onTouchEvent(e);
            // this.getInput().onTouch(graphics.getView(), e);
            mScript.onSingleTap(e);
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.v("", "onLongPress");
    }

    @Override
    public boolean onSwipe(MotionEvent e, SwipeDirection swipeDirection,
            float velocityX, float velocityY) {
        // graphics.getView().onTouchEvent(e);
        // this.getInput().onTouch(graphics.getView(), e);
        // Log.e("datta ","velocityX= "+velocityX);
        return false;
    }

    public Handler handler;
    protected boolean firstResume = true;

    protected boolean useImmersiveMode = false;
    protected boolean hideStatusBar = false;
    private int wasFocusChanged = -1;
    private boolean isWaitingForAudio = false;
}
