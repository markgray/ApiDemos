/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.apis.graphics.spritetext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.os.SystemClock;
import android.util.Log;

import com.example.android.apis.R;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Renderer for the {@code GLSurfaceView} used for the demo.
 */
@SuppressWarnings("WeakerAccess")
public class SpriteTextRenderer implements GLSurfaceView.Renderer {

    /**
     * Width of the {@code GLSurfaceView} we are rendering to, set using the {@code w} parameter
     * passed to our method {@code onSurfaceChanged}.
     */
    private int mWidth;
    /**
     * Height of the {@code GLSurfaceView} we are rendering to, set using the {@code h} parameter
     * passed to our method {@code onSurfaceChanged}.
     */
    private int mHeight;
    /**
     * {@code Context} to use for accessing resources, set to the parameter {@code Context context}
     * passed to our constructor ("this" when called from the {@code onCreate} method of the activity
     * {@code SpriteTextActivity}).
     */
    private Context mContext;
    /**
     * Rotating {@code Triangle} instance that we render.
     */
    private Triangle mTriangle;
    /**
     * Texture name we use for our texture image robot.png
     */
    private int mTextureID;
    /**
     * Frame counter we use to determine milliseconds per frame value (goes from 0 to SAMPLE_PERIOD_FRAMES)
     */
    private int mFrames;
    /**
     * Current milliseconds per frame value, recalculated every SAMPLE_PERIOD_FRAMES frames
     */
    private int mMsPerFrame;
    /**
     * Number of frames to draw before updating the value of {@code mMsPerFrame}
     */
    private final static int SAMPLE_PERIOD_FRAMES = 12;
    /**
     * Factor to multiply elapsed time for drawing SAMPLE_PERIOD_FRAMES frames to calculate the
     * value of {@code mMsPerFrame}
     */
    private final static float SAMPLE_FACTOR = 1.0f / SAMPLE_PERIOD_FRAMES;
    /**
     * Start time for current counting of frames used to calculate the value of {@code mMsPerFrame}
     */
    private long mStartTime;
    /**
     * {@code LabelMaker} containing labels for the three vertices of our triangle "A", "B", and "C",
     * as well as the label "ms/f"
     */
    private LabelMaker mLabels;
    /**
     * {@code Paint} instance we use for our labels as well as the labels that {@code NumericSprite}
     * draws to display our frame millisecond per frame data.
     */
    private Paint mLabelPaint;
    /**
     * {@code Label} index pointing to the {@code Label} "A" in {@code LabelMaker mLabels}
     */
    private int mLabelA;
    /**
     * {@code Label} index pointing to the {@code Label} "B" in {@code LabelMaker mLabels}
     */
    private int mLabelB;
    /**
     * {@code Label} index pointing to the {@code Label} "C" in {@code LabelMaker mLabels}
     */
    private int mLabelC;
    /**
     * {@code Label} index pointing to the {@code Label} "ms/f" in {@code LabelMaker mLabels}
     */
    private int mLabelMsPF;
    /**
     * {@code Projector} we use to "project" our vertex labels to the correct position on our rotating
     * triangle.
     */
    private Projector mProjector;
    /**
     * {@code NumericSprite} instance we use to draw the digit labels to display our {@code mMsPerFrame}
     * (milliseconds per frame) data at the bottom of the {@code SurfaceView}.
     */
    private NumericSprite mNumericSprite;
    /**
     * Scratch array we use in our call to {@code Projector.project} to calculate the correct location
     * of our triangle vertex labels.
     */
    private float[] mScratch = new float[8];
    /**
     * Start time of our current frame count which we use to calculate the value of {@code mMsPerFrame}
     * (milliseconds per frame)
     */
    private long mLastTime;

    /**
     * Our constructor. First we save our parameter {@code Context context} in our field
     * {@code Context mContext}, then we initialize our fields {@code Triangle mTriangle},
     * {@code Projector mProjector} and {@code Paint mLabelPaint} with new instances. We set
     * the text size of {@code Paint mLabelPaint} to 32, set its antialias flag, and set its
     * color to black.
     *
     * @param context {@code Context} to use to access resources, "this" when we are called from the
     *                {@code onCreate} method of the activity {@code SpriteTextActivity}.
     */
    public SpriteTextRenderer(Context context) {
        mContext = context;
        mTriangle = new Triangle();
        mProjector = new Projector();
        mLabelPaint = new Paint();
        mLabelPaint.setTextSize(32);
        mLabelPaint.setAntiAlias(true);
        mLabelPaint.setARGB(0xff, 0x00, 0x00, 0x00);
    }

    /**
     * Called when the surface is created or recreated. Called when the rendering thread starts and
     * whenever the EGL context is lost. The EGL context will typically be lost when the Android
     * device awakes after going to sleep. First we disable the server side capability GL_DITHER
     * (color components and indices will not be dithered before they are written to the color buffer).
     * <p>
     * Next we specify the implementation specific hint GL_FASTEST for the GL_PERSPECTIVE_CORRECTION_HINT
     * target (Indicates the quality of color, texture coordinate, and fog coordinate interpolation.
     * GL_FASTEST will result in simple linear interpolation of colors and/or texture coordinates).
     * <p>
     * We set the clear color to gray, set the shade model to GL_SMOOTH (causes the computed colors
     * of vertices to be interpolated as the primitive is rasterized, typically assigning different
     * colors to each resulting pixel fragment), enable the server side capability GL_DEPTH_TEST
     * (do depth comparisons and update the depth buffer), and the server side capability GL_TEXTURE_2D
     * (If enabled and no fragment shader is active, two-dimensional texturing is performed).
     * <p>
     * Next we request that a texture name be generated, and we save the name in our field
     * {@code mTextureID}. We then bind {@code mTextureID} to the target GL_TEXTURE_2D (GL_TEXTURE_2D
     * becomes an alias for {@code mTextureID} which becomes a two dimensional texture. While a texture
     * is bound, GL operations on the target to which it is bound affect the bound texture, and queries
     * of the target to which it is bound return state from the bound texture).
     * <p>
     * We set the texture parameter GL_TEXTURE_MIN_FILTER of GL_TEXTURE_2D to GL_NEAREST (The texture
     * minifying function is used whenever the pixel being textured maps to an area greater than one
     * texture element. GL_NEAREST causes the value of the texture element that is nearest (in Manhattan
     * distance) to the center of the pixel being textured to be used). We set the texture parameter
     * GL_TEXTURE_MAG_FILTER of GL_TEXTURE_2D to GL_LINEAR (The texture magnification function is
     * used when the pixel being textured maps to an area less than or equal to one texture element.
     * GL_LINEAR causes the weighted average of the four texture elements that are closest to the
     * center of the pixel being textured to be used).
     * <p>
     * We set the texture parameters GL_TEXTURE_WRAP_S and GL_TEXTURE_WRAP_T of GL_TEXTURE_2D both to
     * GL_CLAMP_TO_EDGE (causes the color of the pixel at the edges of the texture to be used when
     * the area being textured extends past the size of the texture). We set texture parameter
     * GL_TEXTURE_ENV_MODE of the texture environment target GL_TEXTURE_ENV to GL_REPLACE (causes
     * the colors of the texture to replace whatever colors were there before).
     * <p>
     * We open {@code InputStream is} to read the contents of our raw resource robot.png, declare
     * {@code Bitmap bitmap}, then decode {@code is} into {@code bitmap}. We upload {@code bitmap}
     * to the texture target GL_TEXTURE_2D and recycle {@code bitmap}.
     * <p>
     * If we already have a {@code LabelMaker mLabels} in use (our surface has been recreated), we
     * surface has been recreated), We then instruct {@code mLabels} to begin adding labels and add the
     * four labels "A", "B", "C", and "ms/f" and saving the index number returned in {@code mLabelA},
     * {@code mLabelB}, {@code mLabelC}, and {@code mLabelMsPF} respectively. We then instruct
     * {@code mLabels} to end the adding of labels.
     * <p>
     * Finally, if {@code NumericSprite mNumericSprite} is not null (our surface has been recreated),
     * we instruct it to {@code shutdown}, otherwise we initialize {@code mNumericSprite} with a new
     * instance of {@code NumericSprite}. Then we instruct {@code mNumericSprite} to initialize.
     *
     * @param gl     the GL interface
     * @param config the EGLConfig of the created surface. UNUSED
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        /*
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */
        gl.glDisable(GL10.GL_DITHER);

        /*
         * Some one-time OpenGL initialization can be made here
         * probably based on features of this particular context
         */
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

        gl.glClearColor(.5f, .5f, .5f, 1);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glEnable(GL10.GL_TEXTURE_2D);

        /*
         * Create our texture. This has to be done each time the
         * surface is created.
         */

        int[] textures = new int[1];
        gl.glGenTextures(1, textures, 0);
        mTextureID = textures[0];

        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);

        InputStream is = mContext.getResources().openRawResource(R.raw.robot);
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(is);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // Ignore.
            }
        }

        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();

        if (mLabels != null) {
            mLabels.shutdown(gl);
        } else {
            mLabels = new LabelMaker(true, 256, 64);
        }
        mLabels.initialize(gl);
        mLabels.beginAdding(gl);
        mLabelA = mLabels.add(gl, "A", mLabelPaint);
        mLabelB = mLabels.add(gl, "B", mLabelPaint);
        mLabelC = mLabels.add(gl, "C", mLabelPaint);
        mLabelMsPF = mLabels.add(gl, "ms/f", mLabelPaint);
        mLabels.endAdding(gl);

        if (mNumericSprite != null) {
            mNumericSprite.shutdown(gl);
        } else {
            mNumericSprite = new NumericSprite();
        }
        mNumericSprite.initialize(gl, mLabelPaint);
    }

    /**
     * Called to draw the current frame. First we disable the server side capability GL_DITHER (color
     * components and indices will not be dithered before they are written to the color buffer). Then
     * we set texture parameter  GL_TEXTURE_ENV_MODE of the texture environment target GL_TEXTURE_ENV
     * to GL_MODULATE (causes the colors from the texture units to be multiplied). Next we clear both
     * the color buffer and the depth buffer.
     * <p>
     * To do the drawing we make the model view matrix the current matrix, load it with the identity
     * matrix, then we create a viewing matrix derived from an eye point at (0,0,-2.5), a reference
     * point indicating the center of the scene at (0,0,0), and an UP vector or (0,1,0).
     * <p>
     * We enable the client side capability GL_VERTEX_ARRAY (the vertex array is enabled for writing
     * and used during rendering), and the client side capability GL_TEXTURE_COORD_ARRAY (the texture
     * coordinate array is enabled for writing and used during rendering). We set the active texture
     * unit to GL_TEXTURE0, and bind our texture name {@code mTextureID} to the texture target
     * GL_TEXTURE_2D. We set the texture parameters GL_TEXTURE_WRAP_S and GL_TEXTURE_WRAP_T of
     * GL_TEXTURE_2D both to GL_REPEAT (causes the texture to be repeated when the area being textured
     * is bigger than the texture).
     * <p>
     * We calculate {@code angle} based on the system time since boot modulo 4000, multiplied by a
     * factor of 0.090 (angle goes from 0 degrees to 360 degrees every 4 seconds). We then rotate
     * our model view matrix by {@code angle} degrees around the z axis, and scale it by 2.0 in all
     * three directions (multiply the current matrix by a general scaling matrix using 2.0 for all
     * three scale factors). Then we instruct our field {@code Triangle mTriangle} to draw itself.
     * <p>
     * To add our labels to the {@code SurfaceView} we instruct our {@code Projector mProjector} to
     * load the current model view matrix, tell our {@code LabelMaker mLabels} to begin drawing, then
     * call our method {@code drawLabel} to draw the three vertex labels {@code mLabelA}, {@code mLabelB},
     * and {@code mLabelC}. We calculate {@code float msPFX} to be the x coordinate of our label
     * {@code mLabelMsPF} by subtracting the width of that label from the width {@code mWidth} of our
     * surface view (with an additional pixel for spacing), then instruct {@code mLabels} to draw our
     * label {@code mLabelMsPF} at the xy location (msPFX,0). We then instruct {@code mLabels} to end
     * its drawing state.
     * <p>
     * Finally we call our method {@code drawMsPF} to display the milliseconds per frame data before
     * the {@code mLabelMsPF} label.
     *
     * @param gl the GL interface.
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        /*
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */
        gl.glDisable(GL10.GL_DITHER);

        gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);

        /*
         * Usually, the first thing one might want to do is to clear
         * the screen. The most efficient way of doing this is to use
         * glClear().
         */

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        /*
         * Now we're ready to draw some 3D objects
         */

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        GLU.gluLookAt(gl, 0.0f, 0.0f, -2.5f,
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        gl.glActiveTexture(GL10.GL_TEXTURE0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);

/*      DEBUGGING CODE
        if (false) {
            long time = SystemClock.uptimeMillis();
            if (mLastTime != 0) {
                long delta = time - mLastTime;
                Log.w("time", Long.toString(delta));
            }
            mLastTime = time;
        }
*/

        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int) time);

        gl.glRotatef(angle, 0, 0, 1.0f);
        gl.glScalef(2.0f, 2.0f, 2.0f);

        mTriangle.draw(gl);

        mProjector.getCurrentModelView(gl);
        mLabels.beginDrawing(gl, mWidth, mHeight);
        drawLabel(gl, 0, mLabelA);
        drawLabel(gl, 1, mLabelB);
        drawLabel(gl, 2, mLabelC);
        float msPFX = mWidth - mLabels.getWidth(mLabelMsPF) - 1;
        mLabels.draw(gl, msPFX, 0, mLabelMsPF);
        mLabels.endDrawing(gl);

        drawMsPF(gl, msPFX);
    }

    /**
     * Draws the milliseconds per frame data on the {@code SurfaceView} using our field
     * {@code NumericSprite mNumericSprite} to draw the value using its per digit labels.
     * <p>
     * First we fetch the system time since boot to {@code long time}, and if {@code mStartTime} is
     * 0 (our first time called) we also save it in {@code mStartTime}. We increment our field
     * {@code mFrames} and if we have waited for SAMPLE_PERIOD_FRAMES (12) since our last update,
     * we set {@code mFrames} to 0, calculate {@code long delta} as the number of milliseconds that
     * have passed between {@code time} and {@code mStartTime}, set {@code mStartTime} to {@code time},
     * and set {@code mMsPerFrame} to the number of milliseconds per frame we calculate by multiplying
     * {@code delta} times SAMPLE_FACTOR.
     * <p>
     * Then if {@code mMsPerFrame} is greater than 0, we tell {@code mNumericSprite} to set its value
     * to {@code mMsPerFrame}, retrieve the width need to display this value to {@code float numWidth}
     * and calculate the value of the x coordinate to begin our number display {@code float x} by
     * subtracting {@code numWidth} from our input parameter {@code float rightMargin}.
     * <p>
     * Finally we instruct {@code mNumericSprite} to draw its value at ({@code x}, 0).
     *
     * @param gl          the GL interface
     * @param rightMargin x coordinate of the end of our milliseconds display (the beginning of the
     *                    "ms/f" label).
     */
    private void drawMsPF(GL10 gl, float rightMargin) {
        long time = SystemClock.uptimeMillis();
        if (mStartTime == 0) {
            mStartTime = time;
        }
        if (mFrames++ == SAMPLE_PERIOD_FRAMES) {
            mFrames = 0;
            long delta = time - mStartTime;
            mStartTime = time;
            mMsPerFrame = (int) (delta * SAMPLE_FACTOR);
        }
        if (mMsPerFrame > 0) {
            mNumericSprite.setValue(mMsPerFrame);
            float numWidth = mNumericSprite.width();
            float x = rightMargin - numWidth;
            mNumericSprite.draw(gl, x, 0, mWidth, mHeight);
        }
    }

    /**
     * Draws the vertex label requested in the proper position on the rotating triangle.
     *
     * @param gl             the GL interface
     * @param triangleVertex the index number of the vertex, 0, 1, or 2.
     * @param labelId        the label index we are to draw.
     */
    private void drawLabel(GL10 gl, int triangleVertex, int labelId) {
        float x = mTriangle.getX(triangleVertex);
        float y = mTriangle.getY(triangleVertex);
        mScratch[0] = x;
        mScratch[1] = y;
        mScratch[2] = 0.0f;
        mScratch[3] = 1.0f;
        mProjector.project(mScratch, 0, mScratch, 4);
        float sx = mScratch[4];
        float sy = mScratch[5];
        float height = mLabels.getHeight(labelId);
        float width = mLabels.getWidth(labelId);
        float tx = sx - width * 0.5f;
        float ty = sy - height * 0.5f;
        mLabels.draw(gl, tx, ty, labelId);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int w, int h) {
        mWidth = w;
        mHeight = h;
        gl.glViewport(0, 0, w, h);
        mProjector.setCurrentView(0, 0, w, h);

        /*
        * Set our projection matrix. This doesn't have to be done
        * each time we draw, but usually a new projection needs to
        * be set when the viewport is resized.
        */

        float ratio = (float) w / h;
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
        mProjector.getCurrentProjection(gl);
    }
}

@SuppressWarnings("WeakerAccess")
class Triangle {
    public Triangle() {

        // Buffers to be passed to gl*Pointer() functions
        // must be direct, i.e., they must be placed on the
        // native heap where the garbage collector cannot
        // move them.
        //
        // Buffers with multi-byte data types (e.g., short, int, float)
        // must have their byte order set to native order

        ByteBuffer vbb = ByteBuffer.allocateDirect(VERTS * 3 * 4);
        vbb.order(ByteOrder.nativeOrder());
        mFVertexBuffer = vbb.asFloatBuffer();

        ByteBuffer tbb = ByteBuffer.allocateDirect(VERTS * 2 * 4);
        tbb.order(ByteOrder.nativeOrder());
        mTexBuffer = tbb.asFloatBuffer();

        ByteBuffer ibb = ByteBuffer.allocateDirect(VERTS * 2);
        ibb.order(ByteOrder.nativeOrder());
        mIndexBuffer = ibb.asShortBuffer();

        for (int i = 0; i < VERTS; i++) {
            for (int j = 0; j < 3; j++) {
                mFVertexBuffer.put(sCoords[i * 3 + j]);
            }
        }

        for (int i = 0; i < VERTS; i++) {
            for (int j = 0; j < 2; j++) {
                mTexBuffer.put(sCoords[i * 3 + j] * 2.0f + 0.5f);
            }
        }

        for (int i = 0; i < VERTS; i++) {
            mIndexBuffer.put((short) i);
        }

        mFVertexBuffer.position(0);
        mTexBuffer.position(0);
        mIndexBuffer.position(0);
    }

    public void draw(GL10 gl) {
        gl.glFrontFace(GL10.GL_CCW);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mFVertexBuffer);
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer);
        gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, VERTS, GL10.GL_UNSIGNED_SHORT, mIndexBuffer);
    }

    public float getX(int vertex) {
        return sCoords[3 * vertex];
    }

    public float getY(int vertex) {
        return sCoords[3 * vertex + 1];
    }

    private final static int VERTS = 3;

    private FloatBuffer mFVertexBuffer;
    private FloatBuffer mTexBuffer;
    private ShortBuffer mIndexBuffer;
    // A unit-sided equilateral triangle centered on the origin.
    private final static float[] sCoords = {
            // X, Y, Z
            -0.5f, -0.25f, 0,
            0.5f, -0.25f, 0,
            0.0f, 0.559016994f, 0
    };
}
