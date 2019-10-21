/*
 * Copyright (C) 2013 The Android Open Source Project
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
package com.example.android.apis.animation

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.transition.Scene
import android.transition.TransitionInflater
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * This application demonstrates some of the capabilities and uses of the
 * [transitions][android.transition] APIs. Scenes and a TransitionManager
 * are loaded from resource files and transitions are run between those scenes
 * as well as a dynamically-configured scene.
 */
@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.KITKAT)
class Transitions : AppCompatActivity() {

    /**
     * `Scene` created from the layout file R.layout.transition_scene1
     */
    internal lateinit var mScene1: Scene
    /**
     * `Scene` created from the layout file R.layout.transition_scene2
     */
    internal lateinit var mScene2: Scene
    /**
     * `Scene` created from the layout file R.layout.transition_scene3
     */
    internal lateinit var mScene3: Scene
    /**
     * `LinearLayout` in our layout file which we use to display our "Scenes".
     */
    internal lateinit var mSceneRoot: ViewGroup
    /**
     * [TransitionManager] object loaded from the xml file R.transition.transitions_mgr
     */
    internal lateinit var mTransitionManager: TransitionManager

    /**
     * Called when the activity is starting. First we call through to our super's implementation
     * of onCreate, then we set our content view to our layout file (R.layout.transition). We
     * locate the LinearLayout to be used for our demo (R.id.sceneRoot) and save it in the field
     * ViewGroup mSceneRoot. We create a TransitionInflater inflater using "this" as the context.
     * Next we load the Scene's mScene1, mScene2, mScene3 from layout files R.layout.transition_scene1,
     * R.layout.transition_scene2, and R.layout.transition_scene3 using mSceneRoot as the root of
     * the hierarchy in which scene changes and transitions will take place, and "this" as the
     * context for the Scene's. Finally we initialize the field TransitionManager mTransitionManager
     * with a TransitionManager object from a resource (R.transition.transitions_mgr) using the
     * ViewGroup  mSceneRoot as the root of the hierarchy in which scene changes and transitions
     * will take place.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transition)

        mSceneRoot = findViewById(R.id.sceneRoot)

        val inflater = TransitionInflater.from(this)

        // Note that this is not the only way to create a Scene object, but that
        // loading them from layout resources cooperates with the
        // TransitionManager that we are also loading from resources, and which
        // uses the same layout resource files to determine the scenes to transition
        // from/to.
        mScene1 = Scene.getSceneForLayout(mSceneRoot, R.layout.transition_scene1, this)
        mScene2 = Scene.getSceneForLayout(mSceneRoot, R.layout.transition_scene2, this)
        mScene3 = Scene.getSceneForLayout(mSceneRoot, R.layout.transition_scene3, this)
        mTransitionManager = inflater.inflateTransitionManager(R.transition.transitions_mgr,
                mSceneRoot)
    }

    /**
     * This method is set as the onClickListener using android:onClick="selectScene" for all four
     * of the RadioButton's in the R.layout.transition layout used by this Activity. We decide
     * which RadioButton by using a switch statement to branch based on the value of the id of the
     * RadioButton View which has been clicked. For the first three RadioButton's we instruct our
     * TransitionManager mTransitionManager to change to the specified scene (mScene1, mScene2, or
     * mScene3) using the appropriate transition for the particular scene change as specified by
     * our transitionManager file R.transition.transitions_mgr. For the fourth RadioButton scene4
     * is not an actual 'Scene', but rather a dynamic change in the UI, transitioned to using
     * beginDelayedTransition() to tell the TransitionManager to get ready to run a transition
     * at the next frame. The change is a resizing of the four boxes in the corners of the three
     * Scene layouts implemented by calling our method setNewSize.
     *
     * @param view View (RadioButton) which has been clicked
     */
    fun selectScene(view: View) {
        when (view.id) {
            R.id.scene1 -> mTransitionManager.transitionTo(mScene1)
            R.id.scene2 -> mTransitionManager.transitionTo(mScene2)
            R.id.scene3 -> mTransitionManager.transitionTo(mScene3)
            R.id.scene4 -> {
                // scene4 is not an actual 'Scene', but rather a dynamic change in the UI,
                // transitioned to using beginDelayedTransition() to tell the TransitionManager
                // to get ready to run a transition at the next frame
                TransitionManager.beginDelayedTransition(mSceneRoot)
                setNewSize(R.id.view1, 150, 25)
                setNewSize(R.id.view2, 150, 25)
                setNewSize(R.id.view3, 150, 25)
                setNewSize(R.id.view4, 150, 25)
            }
        }
    }

    /**
     * This method resizes the width and height of the View specified by the resource id. First we
     * find the view that is identified by the id attribute by calling findViewById. We get the
     * LayoutParams associated with this view, and set the field ViewGroup.LayoutParams.width to
     * the width requested, and ViewGroup.LayoutParams.height to the height requested, then we
     * call view.setLayoutParams to update the layout parameters associated with the view.
     *
     * @param id View's id resource
     * @param width new width of View
     * @param height new height of View
     */
    @Suppress("SameParameterValue")
    private fun setNewSize(id: Int, width: Int, height: Int) {
        val view = findViewById<View>(id)
        val params = view.layoutParams
        params.width = width
        params.height = height
        view.layoutParams = params
    }
}
