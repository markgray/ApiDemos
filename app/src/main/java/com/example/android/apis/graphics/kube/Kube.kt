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
package com.example.android.apis.graphics.kube

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import java.util.*

/**
 * Animates a Rubic cube, randomly spinning layers one by one.
 */
@Suppress("MemberVisibilityCanBePrivate")
class Kube : AppCompatActivity(), KubeRenderer.AnimationCallback {
    /**
     * [GLSurfaceView] that we use as our content view, if uses [KubeRenderer] field [mRenderer]
     * as its renderer.
     */
    var mView: GLSurfaceView? = null
    /**
     * Renderer which performs the drawing to our [GLSurfaceView] field [mView] using the Rubic cube
     * we construct and initialize in the instance of [GLWorld] we pass to its constructor for
     * its data (see our method [makeGLWorld] for how we do this).
     */
    private var mRenderer: KubeRenderer? = null
    /**
     * The 27 [Cube] objects which represent our Rubic cube
     */
    var mCubes = arrayOfNulls<Cube>(27)
    /**
     * a [Layer] for each possible move, each layer consists of the 9 [Cube] objects in
     * a plane which can be rotated around its center [Cube], 3 rotating around x, 3 rotating
     * around y, and 3 rotating around z.
     */
    var mLayers = arrayOfNulls<Layer>(9)
    /**
     * Permutation that needs to be done after the current rotation is finished (and the initial
     * solved permutation (0, 1, ... ,26) as well).
     */
    lateinit var mPermutation: IntArray
    /**
     * random number generator for random cube movements
     */
    var mRandom = Random(System.currentTimeMillis())
    /**
     * Currently turning layer, set to *null* when the layer reaches its [mEndAngle]. It is set to
     * a random value in our method [animate] if it is *null* and is the same layer as the layer
     * chosen to be permutated from [mLayerPermutations] of course.
     */
    var mCurrentLayer: Layer? = null
    /**
     * Current and final angle for current Layer animation
     */
    var mCurrentAngle = 0f
    var mEndAngle = 0f
    /**
     * Amount to increment angle
     */
    var mAngleIncrement = 0f
    /**
     * Temporary storage for the [mLayerPermutations] chosen for the next rotation which is
     * then used to permutate [mPermutation] to create a new [mPermutation] when the rotation
     * has completed
     */
    lateinit var mCurrentLayerPermutation: IntArray

    /**
     * Creates, configures and returns a new instance of [GLWorld] which consists of a 27
     * [Cube] cube (3 by 3 by 3) representing a Rubic cube. First we create a new instance
     * for [GLWorld] variable `val world`. Then we initialize some constants to use to create
     * the seven [GLColor] objects we use to color our [Cube] objects, and the six coordinate
     * values we use when we construct the 27 [Cube] objects used in our Rubic cube. We
     * construct the 27 instances of [Cube] in our Rubic cube to initialize our the contents
     * of our [Cube] array field [mCubes], then we loop through [mCubes] using their method
     * `setFaceColor` to set all the faces to the default black. We now go through all of the
     * `Cube` objects setting the color of the [GLFace] facing out as follows:
     *
     *  * top of Rubic cube - orange. This includes all the `Cube.kTop` faces of the 9
     *  [Cube] objects in [mCubes] (0,1,2,3,4,5,6,7,8)
     *
     *  * bottom of Rubic cube - red. This includes all the `Cube.kBottom` faces of the 9
     *  [Cube] objects in [mCubes] (18,19,20,21,22,23,24,25,26)
     *
     *  * left of Rubic cube - yellow. This includes all the `Cube.kLeft` faces of the 9
     *  [Cube] objects in [mCubes] (0,3,6,9,12,15,18,21,24)
     *
     *  * right of Rubic cube - white. This includes all the `Cube.kRight` faces of the 9
     *  [Cube] objects in [mCubes] (2,5,8,11,15,17,20,23,26)
     *
     *  * back of Rubic cube - blue. This includes all the `Cube.kBack` faces of the 9
     *  [Cube] objects in [mCubes] (0,1,2,9,10,11,18,19,20)
     *
     *  * front of Rubic cube - green. This includes all the `Cube.kFront` faces of the 9
     *  [Cube] objects in [mCubes] (6,7,8,15,16,17,24,25,26)
     *
     * Then for each of the 27 [Cube] objects in [mCubes] we call the method
     * `world.addShape` to add the [GLShape] to its list of [Cube] objects in
     * its `ArrayList<GLShape>` field `mShapeList`.
     *
     * Next we initialize our [Int] array field [mPermutation] with the numbers (0,1,...,26)
     * representing an initial "solved" ordering of [Cube] objects. We call our method
     * [createLayers] to allocate and construct the 9 layers initializing the axis they can
     * rotate about, and call our method [updateLayers] to assign each of the [Cube] objects
     * to the [Layer] that it belongs to in the solved initial position that [mPermutation]
     * currently represents.
     *
     * Then we call the method `world.generate` which allocates and fills the direct allocated
     * buffers required by the method `glDrawElements` when it draws our Rubic cube. Finally we
     * return `world` to the caller (in our case the call to the constructor of the
     * [KubeRenderer] that is used to initialize our [KubeRenderer] field [mRenderer] in
     * this activities override of `onCreate`.
     *
     * @return a new instance of [GLWorld] configured with Rubic cube cubes.
     */
    private fun makeGLWorld(): GLWorld {
        val world = GLWorld()
        val one = 0x10000
        val half = 0x08000
        val red = GLColor(one, 0, 0)
        val green = GLColor(0, one, 0)
        val blue = GLColor(0, 0, one)
        val yellow = GLColor(one, one, 0)
        val orange = GLColor(one, half, 0)
        val white = GLColor(one, one, one)
        val black = GLColor(0, 0, 0)
        // coordinates for our cubes
        val c0 = -1.0f
        val c1 = -0.38f
        val c2 = -0.32f
        val c3 = 0.32f
        val c4 = 0.38f
        val c5 = 1.0f
        // top back, left to right
        mCubes[0] = Cube(world, c0, c4, c0, c1, c5, c1)
        mCubes[1] = Cube(world, c2, c4, c0, c3, c5, c1)
        mCubes[2] = Cube(world, c4, c4, c0, c5, c5, c1)
        // top middle, left to right
        mCubes[3] = Cube(world, c0, c4, c2, c1, c5, c3)
        mCubes[4] = Cube(world, c2, c4, c2, c3, c5, c3)
        mCubes[5] = Cube(world, c4, c4, c2, c5, c5, c3)
        // top front, left to right
        mCubes[6] = Cube(world, c0, c4, c4, c1, c5, c5)
        mCubes[7] = Cube(world, c2, c4, c4, c3, c5, c5)
        mCubes[8] = Cube(world, c4, c4, c4, c5, c5, c5)
        // middle back, left to right
        mCubes[9] = Cube(world, c0, c2, c0, c1, c3, c1)
        mCubes[10] = Cube(world, c2, c2, c0, c3, c3, c1)
        mCubes[11] = Cube(world, c4, c2, c0, c5, c3, c1)
        // middle middle, left to right
        mCubes[12] = Cube(world, c0, c2, c2, c1, c3, c3)
        mCubes[13] = null
        mCubes[14] = Cube(world, c4, c2, c2, c5, c3, c3)
        // middle front, left to right
        mCubes[15] = Cube(world, c0, c2, c4, c1, c3, c5)
        mCubes[16] = Cube(world, c2, c2, c4, c3, c3, c5)
        mCubes[17] = Cube(world, c4, c2, c4, c5, c3, c5)
        // bottom back, left to right
        mCubes[18] = Cube(world, c0, c0, c0, c1, c1, c1)
        mCubes[19] = Cube(world, c2, c0, c0, c3, c1, c1)
        mCubes[20] = Cube(world, c4, c0, c0, c5, c1, c1)
        // bottom middle, left to right
        mCubes[21] = Cube(world, c0, c0, c2, c1, c1, c3)
        mCubes[22] = Cube(world, c2, c0, c2, c3, c1, c3)
        mCubes[23] = Cube(world, c4, c0, c2, c5, c1, c3)
        // bottom front, left to right
        mCubes[24] = Cube(world, c0, c0, c4, c1, c1, c5)
        mCubes[25] = Cube(world, c2, c0, c4, c3, c1, c5)
        mCubes[26] = Cube(world, c4, c0, c4, c5, c1, c5)
        // paint the sides
        var j: Int
        // set all faces black by default
        var i = 0
        while (i < 27) {
            val cube = mCubes[i]
            if (cube != null) {
                j = 0
                while (j < 6) {
                    cube.setFaceColor(j, black)
                    j++
                }
            }
            i++
        }
        // paint top
        i = 0
        while (i < 9) {
            mCubes[i]!!.setFaceColor(Cube.kTop, orange)
            i++
        }
        // paint bottom
        i = 18
        while (i < 27) {
            mCubes[i]!!.setFaceColor(Cube.kBottom, red)
            i++
        }
        // paint left
        i = 0
        while (i < 27) {
            mCubes[i]!!.setFaceColor(Cube.kLeft, yellow)
            i += 3
        }
        // paint right
        i = 2
        while (i < 27) {
            mCubes[i]!!.setFaceColor(Cube.kRight, white)
            i += 3
        }
        // paint back
        i = 0
        while (i < 27) {
            j = 0
            while (j < 3) {
                mCubes[i + j]!!.setFaceColor(Cube.kBack, blue)
                j++
            }
            i += 9
        }
        // paint front
        i = 6
        while (i < 27) {
            j = 0
            while (j < 3) {
                mCubes[i + j]!!.setFaceColor(Cube.kFront, green)
                j++
            }
            i += 9
        }
        i = 0
        while (i < 27) {
            if (mCubes[i] != null) world.addShape(mCubes[i])
            i++
        }
        // initialize our permutation to solved position
        mPermutation = IntArray(27)
        i = 0
        while (i < mPermutation.size) {
            mPermutation[i] = i
            i++
        }
        createLayers()
        updateLayers()
        world.generate()
        return world
    }

    /**
     * This initializes our [Layer] array field [mLayers] with [Layer] objects constructed to
     * rotate around their respective axises:
     *
     *  * kUp = 0 Up layer (top 9 cubes) rotates around the y axis.
     *
     *  * kDown = 1 Down layer (bottom 9 cubes) rotates around the y axis.
     *
     *  * kLeft = 2 Left layer (9 cubes on left side) rotates around the x axis.
     *
     *  * kRight = 3 Right layer (9 cubes on right side) rotates around the x axis.
     *
     *  * kFront = 4 Front layer (layer in front of you) rotates around the z axis.
     *
     *  * kBack = 5 Back layer (layer at the back of the Rubic cube) rotates around the z axis.
     *
     *  * kMiddle = 6 Middle layer (layer between left and right) rotates around the x axis.
     *
     *  * kEquator = 7 Equator layer (layer between up and down) rotates around the y axis.
     *
     *  * kSide = 8 Side layer (layer between front and back) rotates around the z axis.
     */
    private fun createLayers() {
        mLayers[kUp] = Layer(Layer.kAxisY)
        mLayers[kDown] = Layer(Layer.kAxisY)
        mLayers[kLeft] = Layer(Layer.kAxisX)
        mLayers[kRight] = Layer(Layer.kAxisX)
        mLayers[kFront] = Layer(Layer.kAxisZ)
        mLayers[kBack] = Layer(Layer.kAxisZ)
        mLayers[kMiddle] = Layer(Layer.kAxisX)
        mLayers[kEquator] = Layer(Layer.kAxisY)
        mLayers[kSide] = Layer(Layer.kAxisZ)
    }

    /**
     * This method updates all the layers in our [Layer] array field [mLayers] so that their field
     * [GLShape] array field `mShapes` contains the correct [Cube] objects based on the latest
     * [mPermutation] performed (including the initial "solved" [mPermutation] (0.1,...,26).
     *
     * First we declare the variables we will be using:
     *
     *  * [Layer] `var layer` will contain a reference to the [Layer] object from the [Layer] array
     *  field [mLayers] that we are currently working with.
     *
     *  * [GLShape] array variable `var shapes` will contain a reference to the [GLShape] array
     *  field `mShapes` of the current [Layer] `layer`
     *
     *  * `i, j, and k` will be used as indices
     *
     * Next we assign the correct [Cube] objects from our [Cube] array field [mCubes]. This
     * assignment is done for the 9 layers as follows:
     *
     *  * kUp = 0 Up layer (top 9 cubes), its 9 `mShapes` [Cube] objects are chosen
     *  from our [Cube] array field [mCubes] based on [mPermutation] entries (0,1,2,3,4,5,6,7,8)
     *
     *  * kDown = 1 Down layer (bottom 9 cubes), its 9 `mShapes` [Cube] objects are chosen from our
     *  [Cube] array field [mCubes] based on [mPermutation] entries (18,19,20,21,22,23,24,25,26)
     *
     *  * kLeft = 2 Left layer (9 cubes on left side), its 9 `mShapes` [Cube] objects are chosen
     *  from our [Cube] array field [mCubes] based on [mPermutation] entries (0,3,6,9,12,15,18,21,24)
     *
     *  * kRight = 3 Right layer (9 cubes on right side), its 9 `mShapes` [Cube] objects are chosen
     *  from our [Cube] array field [mCubes] based on [mPermutation] entries (2,5,8,11,14,17,20,23,26)
     *
     *  * kFront = 4 Front layer (layer in front of you), its 9 `mShapes` [Cube] objects are chosen
     *  from our [Cube] array field [mCubes] based on [mPermutation] entries (6,7,8,15,16,17,24,25,26)
     *
     *  * kBack = 5 Back layer (layer at the back of the Rubic cube), its 9 `mShapes` [Cube] objects
     *  are chosen from our [Cube] array field [mCubes] based on [mPermutation] entries
     *  (0,1,2,9,10,11,18,19,20)
     *
     *  * kMiddle = 6 Middle layer (layer between left and right), its 9 `mShapes` [Cube] objects are chosen
     *  from our [Cube] array field [mCubes] based on [mPermutation] entries (1,4,7,9,13,16,18,21,24)
     *
     *  * kEquator = 7 Equator layer (layer between up and down), its 9 `mShapes` [Cube] objects are chosen
     *  from our [Cube] array field [mCubes] based on [mPermutation] entries (9,10,11,12,13,14,15,16,17)
     *
     *  * kSide = 8 Side layer (layer between front and back), its 9 `mShapes` [Cube] objects are chosen
     *  from our [Cube] array field [mCubes] based on [mPermutation] entries (3,4,5,12,13,14,21,22,23)
     */
    private fun updateLayers() {
        var shapes: Array<GLShape?>
        var j: Int
        // up layer
        var layer: Layer? = mLayers[kUp]
        shapes = layer!!.mShapes
        var i = 0
        while (i < 9) {
            shapes[i] = mCubes[mPermutation[i]]
            i++
        }
        // down layer
        layer = mLayers[kDown]
        shapes = layer!!.mShapes
        i = 18
        var k = 0
        while (i < 27) {
            shapes[k++] = mCubes[mPermutation[i]]
            i++
        }
        // left layer
        layer = mLayers[kLeft]
        shapes = layer!!.mShapes
        i = 0
        k = 0
        while (i < 27) {
            j = 0
            while (j < 9) {
                shapes[k++] = mCubes[mPermutation[i + j]]
                j += 3
            }
            i += 9
        }
        // right layer
        layer = mLayers[kRight]
        shapes = layer!!.mShapes
        i = 2
        k = 0
        while (i < 27) {
            j = 0
            while (j < 9) {
                shapes[k++] = mCubes[mPermutation[i + j]]
                j += 3
            }
            i += 9
        }
        // front layer
        layer = mLayers[kFront]
        shapes = layer!!.mShapes
        i = 6
        k = 0
        while (i < 27) {
            j = 0
            while (j < 3) {
                shapes[k++] = mCubes[mPermutation[i + j]]
                j++
            }
            i += 9
        }
        // back layer
        layer = mLayers[kBack]
        shapes = layer!!.mShapes
        i = 0
        k = 0
        while (i < 27) {
            j = 0
            while (j < 3) {
                shapes[k++] = mCubes[mPermutation[i + j]]
                j++
            }
            i += 9
        }
        // middle layer
        layer = mLayers[kMiddle]
        shapes = layer!!.mShapes
        i = 1
        k = 0
        while (i < 27) {
            j = 0
            while (j < 9) {
                shapes[k++] = mCubes[mPermutation[i + j]]
                j += 3
            }
            i += 9
        }
        // equator layer
        layer = mLayers[kEquator]
        shapes = layer!!.mShapes
        i = 9
        k = 0
        while (i < 18) {
            shapes[k++] = mCubes[mPermutation[i]]
            i++
        }
        // side layer
        layer = mLayers[kSide]
        shapes = layer!!.mShapes
        i = 3
        k = 0
        while (i < 27) {
            j = 0
            while (j < 3) {
                shapes[k++] = mCubes[mPermutation[i + j]]
                j++
            }
            i += 9
        }
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. Then we request the window feature FEATURE_NO_TITLE. Next we initialize our
     * [GLSurfaceView] field [mView] with an instance of [GLSurfaceView], initialize our [KubeRenderer]
     * field [mRenderer] with an instance of [KubeRenderer] constructed using the [GLWorld] returned
     * by our method [makeGLWorld] and set [mRenderer] as the renderer for [mView]. Finally we set
     * our content view to [mView].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // We don't need a title either.
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        mView = GLSurfaceView(application)
        mRenderer = KubeRenderer(makeGLWorld(), this)
        mView!!.setRenderer(mRenderer)
        setContentView(mView)
    }

    /**
     * Called after [onRestoreInstanceState], [onRestart], or [onPause], for our activity to start
     * interacting with the user. First we call through to our super's implementation of `onResume`
     * then we call the `onResume` method of our [GLSurfaceView] field [mView].
     */
    override fun onResume() {
        super.onResume()
        mView!!.onResume()
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed.  The counterpart to [onResume]. First we call through to our
     * super's implementation of `onPause` then we call the `onPause` method of our [GLSurfaceView]
     * field [mView].
     */
    override fun onPause() {
        super.onPause()
        mView!!.onPause()
    }

    /**
     * Called by [KubeRenderer.onDrawFrame] to prepare the Rubic cube for the next frame to be
     * drawn. First we instruct our [KubeRenderer] field `[mRenderer] to add 1.2 degrees to the
     * angle it uses when rotating the entire Rubic cube. Then if [Layer] field [mCurrentLayer] is
     * *null* (the last layer rotation has reached its endpoint, or we are just starting) we set
     * `val layerID` to a random number between 0 and 8, and use it as an index into our [mLayers]
     * field in order to set [Layer] field [mCurrentLayer], and we use it as an index into
     * [mLayerPermutations] in order to set [Int] array field [mCurrentLayerPermutation]. We call
     * the method `mCurrentLayer.startAnimation` which calls `Shape.startAnimation` (which does
     * nothing) for each of the shapes in the layer. We execute some unused code, then set `var count`
     * to 1, and `var direction` to *false* (overriding their initial setting to random values for
     * some reason). We set our field [mCurrentAngle] to 0 then (since `direction` is always *false*)
     * we set [mAngleIncrement] to PI/50 and [mEndAngle] to -PI/2 (since [mCurrentAngle] is 0 at
     * this point, and count is always 1).
     *
     * Now that [mCurrentLayer] is known not to be *null*, we increment [mCurrentAngle] by
     * [mAngleIncrement], and if we have reached our [mEndAngle] we set the angle of
     * [mCurrentLayer] to [mEndAngle], and call the method `mCurrentLayer.endAnimation`
     * which calls the method `Shape.endAnimation` for each of the 9 shapes in the layer in order
     * to update its cumulative transfer matrix `mTransform` with the transform matrix it used for
     * the movement to the angle [mEndAngle]: `mAnimateTransform`. We then set our field
     * [mCurrentLayer] to null (so that a new layer will be chosen then next time [animate]
     * is called).
     *
     * We now have to adjust [mPermutation] so that the next call to [updateLayers] will
     * assign the [Cube] objects to the correct layer based on the just completed layer rotation.
     * To do this we first allocate temporary storage for [Int] array `val newPermutation`, then
     * for each of the current layer assignments for our [Cube] array field [mCubes] which was last
     * specified by the contents of [mPermutation] we assign a new layer based on the contents of
     * the respective index entry contained in [mCurrentLayerPermutation] (the permutation of the
     * just completed rotation). Then we assign our temporary `newPermutation` to [mPermutation]
     * and call our method [updateLayers] to apply this layer assignment to all the `Layer.mShapes`
     * objects in the [Layer] objects in [mLayers].
     *
     * If on the other hand, we have not yet reached the [mEndAngle] we just call the `setAngle`
     * method of [mCurrentLayer] to set the angle of the [Layer] to the new [mCurrentAngle]
     */
    override fun animate() { // change our angle of view
        mRenderer!!.angle = mRenderer!!.angle + 1.2f
        if (mCurrentLayer == null) {
            val layerID = mRandom.nextInt(9)
            mCurrentLayer = mLayers[layerID]
            mCurrentLayerPermutation = mLayerPermutations[layerID]
            mCurrentLayer!!.startAnimation()
            @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")
            var direction = mRandom.nextBoolean()
            @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")
            var count = mRandom.nextInt(3) + 1
            count = 1
            direction = false
            mCurrentAngle = 0f
            if (direction) {
                mAngleIncrement = Math.PI.toFloat() / 50
                mEndAngle = mCurrentAngle + Math.PI.toFloat() * count / 2f
            } else { // This path is always taken.
                mAngleIncrement = (-Math.PI).toFloat() / 50
                mEndAngle = mCurrentAngle - Math.PI.toFloat() * count / 2f
            }
        }
        mCurrentAngle += mAngleIncrement
        if (mAngleIncrement > 0f && mCurrentAngle >= mEndAngle ||
                mAngleIncrement < 0f && mCurrentAngle <= mEndAngle) {
            mCurrentLayer!!.setAngle(mEndAngle)
            mCurrentLayer!!.endAnimation()
            mCurrentLayer = null
            // adjust mPermutation based on the completed layer rotation
            val newPermutation = IntArray(27)
            for (i in 0..26) {
                newPermutation[i] = mPermutation[mCurrentLayerPermutation[i]]
                //    			newPermutation[i] = mCurrentLayerPermutation[mPermutation[i]];
            }
            mPermutation = newPermutation
            updateLayers()
        } else {
            mCurrentLayer!!.setAngle(mCurrentAngle)
        }
    }

    companion object {
        /**
         * permutations corresponding to a pi/2 (90 degree) rotation of each of the layers about its axis.
         * They are chosen at random in our method `animate`, and applied in our method `updateLayers`
         * to assign the `GLShapes` to the layer they belong to, both before any rotations have been
         * done and after the current rotation has finished. Each of these has the same index as the layer
         * it permutates, and represents the layer assignments of each of the `GLShape` objects that
         * need to be made after the rotation completes.
         */
        var mLayerPermutations = arrayOf(intArrayOf(2, 5, 8, 1, 4, 7, 0, 3, 6, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26), intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 20, 23, 26, 19, 22, 25, 18, 21, 24), intArrayOf(6, 1, 2, 15, 4, 5, 24, 7, 8, 3, 10, 11, 12, 13, 14, 21, 16, 17, 0, 19, 20, 9, 22, 23, 18, 25, 26), intArrayOf(0, 1, 8, 3, 4, 17, 6, 7, 26, 9, 10, 5, 12, 13, 14, 15, 16, 23, 18, 19, 2, 21, 22, 11, 24, 25, 20), intArrayOf(0, 1, 2, 3, 4, 5, 24, 15, 6, 9, 10, 11, 12, 13, 14, 25, 16, 7, 18, 19, 20, 21, 22, 23, 26, 17, 8), intArrayOf(18, 9, 0, 3, 4, 5, 6, 7, 8, 19, 10, 1, 12, 13, 14, 15, 16, 17, 20, 11, 2, 21, 22, 23, 24, 25, 26), intArrayOf(0, 7, 2, 3, 16, 5, 6, 25, 8, 9, 4, 11, 12, 13, 14, 15, 22, 17, 18, 1, 20, 21, 10, 23, 24, 19, 26), intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 11, 14, 17, 10, 13, 16, 9, 12, 15, 18, 19, 20, 21, 22, 23, 24, 25, 26), intArrayOf(0, 1, 2, 21, 12, 3, 6, 7, 8, 9, 10, 11, 22, 13, 4, 15, 16, 17, 18, 19, 20, 23, 14, 5, 24, 25, 26))
        // names for our 9 layers (based on notation from http://www.cubefreak.net/notation.html)
        /**
         * Up layer (top 9 cubes)
         */
        const val kUp = 0
        /**
         * Down layer (bottom 9 cubes)
         */
        const val kDown = 1
        /**
         * Left layer (9 cubes on left side)
         */
        const val kLeft = 2
        /**
         * Right layer (9 cubes on right side)
         */
        const val kRight = 3
        /**
         * Front layer (layer in front of you)
         */
        const val kFront = 4
        /**
         * Back layer (layer at the back of the Rubic cube)
         */
        const val kBack = 5
        /**
         * Middle layer (layer between left and right)
         */
        const val kMiddle = 6
        /**
         * Equator layer (layer between up and down)
         */
        const val kEquator = 7
        /**
         * Side layer (layer between front and back)
         */
        const val kSide = 8
    }
}