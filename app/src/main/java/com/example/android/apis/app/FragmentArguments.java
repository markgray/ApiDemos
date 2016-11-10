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

package com.example.android.apis.app;

import com.example.android.apis.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Demonstrates a fragment that can be configured through both Bundle arguments
 * and layout attributes.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class FragmentArguments extends Activity {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we set our content view to our layout file R.layout.fragment_arguments. If the
     * parameter <code>Bundle savedInstanceState</code> is not null we are being recreated after an
     * orientation change or other occurrence and need do nothing more. If it is null, this is the
     * first time our activity is being started so we fetch the FragmentManager used for interacting
     * with fragments associated with this activity and use it to begin fragment transactions using
     * <code>FragmentTransaction ft</code>. We create an instance of MyFragment by calling its
     * factory method <code>newInstance</code>, and call it <code>Fragment newFragment</code>, then
     * we add <code>newFragment</code> to <code>FragmentTransaction ft</code> and finally schedule a
     * commit of the <code>FragmentTransaction ft</code>.
     *
     * @param savedInstanceState if null we need to create our fragment and embed it in the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_arguments);

        if (savedInstanceState == null) {
            // First-time init; create fragment to embed in activity.
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment newFragment = MyFragment.newInstance("From Arguments");
            ft.add(R.id.created, newFragment);
            ft.commit();
        }
    }

    /**
     * Skeleton Fragment which can be inserted in layouts using xml or java code.
     */
    public static class MyFragment extends Fragment {
        /**
         *  text to display in fragment as read from xml attributes in onInflate or from arguments
         *  Bundle during onCreate if we were created by newInstance.
         */
        CharSequence mLabel;

        /**
         * Create a new instance of MyFragment that will be initialized with the given arguments.
         * First we create a new instance of <code>MyFragment f</code>, then we create a Bundle b,
         * store our parameter label in it under the key "label", and set the arguments of f to
         * be b. Finally we return <code>MyFragment f</code> to the caller.
         *
         * @param label text to display in fragment
         * @return MyFragment instance with arguments set to Bundle containing CharSequence label
         */
        static MyFragment newInstance(CharSequence label) {
            MyFragment f = new MyFragment();
            Bundle b = new Bundle();
            b.putCharSequence("label", label);
            f.setArguments(b);
            return f;
        }

        /**
         * Called when a fragment is being created as part of a view layout inflation, typically
         * from setting the content view of an activity. Here we will parse attributes during
         * inflation of our layout file from a view hierarchy into the arguments we handle.
         *
         * First we call through to our super's implementation of onInflate. A declare-styleable
         * element with the name="FragmentArguments" in the attrs.xml file declares the attribute
         * <attr name="android:label" /> which is retrieved using obtainStyledAttributes into the
         * <code>TypedArray a</code>. Then using the index generated from our styleable name and
         * the name for our attribute: R.styleable.FragmentArguments_android_label we retrieve the
         * value of this attribute -- android:label="@string/fragment_arguments_embedded" where
         * the String pointed to is "From Attributes". Then we recycle the <code>TypedArray a</code>
         * to be re-used by a later caller.
         *
         * @param context The Context that is inflating this fragment.
         * @param attrs The attributes at the tag where the fragment is being created.
         * @param savedInstanceState If the fragment is being re-created from a previous saved
         *        state, this is the state, but since we do not override onSaveInstanceState
         *        we do not use.
         */
        @Override
        public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
            super.onInflate(context, attrs, savedInstanceState);

            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FragmentArguments);
            mLabel = a.getText(R.styleable.FragmentArguments_android_label);
            a.recycle();
        }

        /**
         * During creation, if arguments have been supplied to the fragment
         * then parse those out.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            Bundle args = getArguments();
            if (args != null) {
                mLabel = args.getCharSequence("label", mLabel);
            }
        }

        /**
         * Create the view for this fragment, using the arguments given to it.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.hello_world, container, false);
            View tv = v.findViewById(R.id.text);
            ((TextView)tv).setText(mLabel != null ? mLabel : "(no label)");
            tv.setBackground(getResources().getDrawable(android.R.drawable.gallery_thumb, null));
            return v;
        }
    }

}
