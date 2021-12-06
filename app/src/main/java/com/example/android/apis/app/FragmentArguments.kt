/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.example.android.apis.app

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.android.apis.R

/**
 * Demonstrates a fragment that can be configured through both Bundle arguments
 * and layout attributes.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class FragmentArguments : FragmentActivity() {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we set our content view to our layout file R.layout.fragment_arguments. If our
     * [Bundle] parameter [savedInstanceState] is not *null* we are being recreated after an
     * orientation change or other occurrence and need do nothing more. If it is *null*, this is the
     * first time our activity is being started so we fetch the support `FragmentManager` used for
     * with fragments associated with this activity and use it to begin fragment transactions using
     * our `FragmentTransaction` variable `val ft`. We create an instance of [MyFragment] by calling
     * its factory method `newInstance` to initialize our [Fragment] variable `val newFragment`,
     * then we add `newFragment` to `ft` and schedule a commit of `ft`.
     *
     * @param savedInstanceState if null we need to create our fragment and embed it in the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_arguments)

        if (savedInstanceState == null) {
            // First-time init; create fragment to embed in activity.
            val ft = supportFragmentManager.beginTransaction()
            val newFragment = MyFragment.newInstance("From Arguments")
            ft.add(R.id.created, newFragment)
            ft.commit()
        }
    }

    /**
     * Skeleton Fragment which can be inserted in layouts using xml or java code.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    class MyFragment : Fragment() {
        /**
         * text to display in fragment as read from xml attributes in [onInflate] or from arguments
         * [Bundle] during [onCreate] if we were created by [newInstance].
         */
        internal var mLabel: CharSequence? = null

        /**
         * Called when a fragment is being created as part of a view layout inflation, typically
         * from setting the content view of an activity. Here we will parse attributes during
         * inflation of our layout file from a view hierarchy into the arguments we handle.
         *
         * First we call through to our super's implementation of onInflate. A declare-styleable
         * element with the name="FragmentArguments" in the attrs.xml file declares the attribute
         * <attr name="android:label"></attr> which is retrieved using obtainStyledAttributes into the
         * **TypedArray a**. Then using the index generated from our styleable name and
         * the name for our attribute: R.styleable.FragmentArguments_android_label we retrieve the
         * value of this attribute -- android:label="@string/fragment_arguments_embedded" where
         * the String pointed to is "From Attributes". Then we recycle the **TypedArray a**
         * to be re-used by a later caller.
         *
         * @param context The Context that is inflating this fragment.
         * @param attrs The attributes at the tag where the fragment is being created.
         * @param savedInstanceState If the fragment is being re-created from a previous saved
         * state, this is the state, but since we do not override onSaveInstanceState
         * we do not use.
         */
        override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
            super.onInflate(context, attrs, savedInstanceState)

            val a = context.obtainStyledAttributes(attrs, R.styleable.FragmentArguments)
            mLabel = a.getText(R.styleable.FragmentArguments_android_label)
            a.recycle()
        }

        /**
         * Called to do initial creation of a fragment. First we call through to our super's
         * implementation of onCreate, then we fetch our arguments to **Bundle args**.
         * If we are being created from xml this will be null and we are done. If we were created
         * from java code in **newInstance** our arguments were set using setArguments
         * so we initialize our field **CharSequence mLabel** to the value which was
         * stored in our arguments using key "label".
         *
         * @param savedInstanceState we do not override onSaveInstanceState so do not use
         */
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val args = arguments
            if (args != null) {
                mLabel = args.getCharSequence("label", mLabel)
            }
        }

        /**
         * Called to have the fragment instantiate its user interface view. First we inflate our
         * layout file R.layout.hello_world into **View v**, then we locate our text field
         * R.id.text in the layout set its text to our field mLabel (if it got initialized) and set
         * its background to the system drawable android.R.drawable.gallery_thumb (although this is
         * actually a selector so this might be an error). Finally we return **View v** to
         * our caller.
         *
         * @param inflater The LayoutInflater object that can be used to inflate any views in the
         * fragment,
         * @param container If non-null, this is the parent view that the fragment's UI should be
         * attached to.  The fragment should not add the view itself, but this can
         * be used to generate the LayoutParams of the view.
         * @param savedInstanceState since we do not override onSaveInstanceState we do not use
         *
         * @return Return the View for the fragment's UI, or null.
         */
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val v = inflater.inflate(R.layout.hello_world, container, false)
            val tv = v.findViewById<View>(R.id.text)
            (tv as TextView).text = if (mLabel != null) mLabel else "(no label)"
            tv.setBackground(
                ResourcesCompat.getDrawable(
                    resources,
                    android.R.drawable.gallery_thumb,
                    null
                )
            )
            return v
        }

        companion object {

            /**
             * Create a new instance of MyFragment that will be initialized with the given arguments.
             * First we create a new instance of **MyFragment f**, then we create a Bundle b,
             * store our parameter label in it under the key "label", and set the arguments of f to
             * be b. Finally we return **MyFragment f** to the caller.
             *
             * @param label text to display in fragment
             * @return MyFragment instance with arguments set to Bundle containing CharSequence label
             */
            fun newInstance(label: CharSequence): MyFragment {
                val f = MyFragment()
                val b = Bundle()
                b.putCharSequence("label", label)
                f.arguments = b
                return f
            }
        }
    }

}
