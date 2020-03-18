package com.example.android.apis.animation;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.apis.R;

/**
 * This sample application shows how to use layout animation and various
 * transformations on views. The result is a 3D transition between a
 * ListView and an ImageView. When the user clicks the list, it flips to
 * show the picture. When the user clicks the picture, it flips to show the
 * list. The animation is made of two smaller animations: the first half
 * rotates the list by 90 degrees on the Y axis and the second half rotates
 * the picture by 90 degrees on the Y axis. When the first half finishes, the
 * list is made invisible and the picture is set visible.
 */
public class Transition3d extends AppCompatActivity implements
        AdapterView.OnItemClickListener, View.OnClickListener {
    /**
     * {@code ListView} with ID android.R.id.list containing names of photos
     */
    private ListView mPhotosList;
    /**
     * {@code FrameLayout} with ID R.id.container containing both {@code ListView mPhotosList} and
     * {@code ImageView mImageView}
     */
    private ViewGroup mContainer;
    /**
     * {@code ImageView} with ID R.id.picture which displays selected photo
     */
    private ImageView mImageView;

    /**
     * Names of the photos we show in the list
     */
    private static final String[] PHOTOS_NAMES = new String[]{
            "Lyon",
            "Livermore",
            "Tahoe Pier",
            "Lake Tahoe",
            "Grand Canyon",
            "Bodie"
    };

    /**
     * Resource identifiers for the photos we want to display
     */
    private static final int[] PHOTOS_RESOURCES = new int[]{
            R.drawable.photo1,
            R.drawable.photo2,
            R.drawable.photo3,
            R.drawable.photo4,
            R.drawable.photo5,
            R.drawable.photo6
    };

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.animations_main_screen.
     * We initialize {@code ListView mPhotosList} by locating the view with ID android.R.id.list, we
     * initialize {@code ImageView mImageView} by locating the view with ID R.id.picture, and we
     * initialize {@code ViewGroup mContainer} by locating the view group with ID R.id.container.
     * <p>
     * We create {@code ArrayAdapter<String> adapter} using android.R.layout.simple_list_item_1 as
     * the layout file containing a TextView to use when instantiating views, and PHOTOS_NAMES as
     * the objects to represent in the ListView, set it as the adapter for our {@code ListView}
     * {@code ListView mPhotosList}, and set "this" as the {@code OnItemClickListener} for
     * {@code ListView mPhotosList}.
     * <p>
     * We enable click events for {@code ImageView mImageView}, enable it to receive focus, and set
     * "this" as its {@code OnItemClickListener}.
     * <p>
     * Finally we the set types of drawing caches should be kept in memory after they have been
     * created for {@code ViewGroup mContainer} to PERSISTENT_ANIMATION_CACHE (indicates that the
     * animation drawing cache should be kept in memory).
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.animations_main_screen);

        mPhotosList = findViewById(android.R.id.list);
        mImageView = findViewById(R.id.picture);
        mContainer = findViewById(R.id.container);

        // Prepare the ListView
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, PHOTOS_NAMES);

        mPhotosList.setAdapter(adapter);
        mPhotosList.setOnItemClickListener(this);

        // Prepare the ImageView
        mImageView.setClickable(true);
        mImageView.setFocusable(true);
        mImageView.setOnClickListener(this);

        // Since we are caching large views, we want to keep their cache
        // between each animation
        mContainer.setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE);
    }

    /**
     * Set up a new 3D rotation on the container view. We locate the center of {@code ViewGroup mContainer}
     * {@code (centerX,centerY)} by dividing the width and height of {@code mContainer} by 2 respectively.
     * We create {@code Rotate3dAnimation rotation} using our parameters {@code start} the start angle,
     * {@code end} as the end angle of the 3D rotation, our variables {@code centerX} as the X center,
     * {@code centerY} as the Y center of the 3D rotation, 310.0 as the translation on the Z axis when
     * the animation starts, and the flag true to indicate that the translation should be reversed.
     * We set the duration of {@code rotation} to 500 milliseconds, set its "fill after" to true so
     * that the transformation that this animation performed will persist when it is finished, set
     * its {@code Interpolator} to a new instance of {@code AccelerateInterpolator}, and set its
     * {@code AnimationListener} to a new instance of {@code DisplayNextView(position)}
     *
     * @param position the item that was clicked to show a picture, or -1 to show the list
     * @param start    the start angle at which the rotation must begin
     * @param end      the end angle of the rotation
     */
    @SuppressWarnings("SameParameterValue")
    private void applyRotation(int position, float start, float end) {
        // Find the center of the container
        final float centerX = mContainer.getWidth() / 2.0f;
        final float centerY = mContainer.getHeight() / 2.0f;

        // Create a new 3D rotation with the supplied parameter
        // The animation listener is used to trigger the next animation
        final Rotate3dAnimation rotation =
                new Rotate3dAnimation(start, end, centerX, centerY, 310.0f, true);
        rotation.setDuration(500);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        rotation.setAnimationListener(new DisplayNextView(position));

        mContainer.startAnimation(rotation);
    }

    /**
     * Callback method to be invoked when an item in the AdapterView of {@code ListView mPhotosList}
     * has been clicked. First we set the drawable whose resource ID is at {@code position} in our
     * array {@code PHOTOS_RESOURCES} as the content of {@code ImageView mImageView} (this does
     * Bitmap reading and decoding on the UI thread, which can cause a latency hiccup). Finally we
     * call our method {@code applyRotation} to set up a new 3D rotation on the container view for
     * transitioning between {@code ListView mPhotosList} and {@code ImageView mImageView} and starts
     * that animation.
     *
     * @param parent   The AdapterView where the click happened.
     * @param v        The view within the AdapterView that was clicked (this will be a view provided
     *                 by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        // Pre-load the image then start the animation
        mImageView.setImageResource(PHOTOS_RESOURCES[position]);
        applyRotation(position, 0, 90);
    }

    /**
     * Called when {@code ImageView mImageView} has been clicked. We simply call our method
     * {@code applyRotation} to rotate back from {@code ImageView mImageView} to
     * {@code ListView mPhotosList}.
     *
     * @param v The view that was clicked ({@code ImageView mImageView} in our case)
     */
    @Override
    public void onClick(View v) {
        applyRotation(-1, 180, 90);
    }

    /**
     * This class listens for the end of the first half of the animation.
     * It then posts a new action that effectively swaps the views when the container
     * is rotated 90 degrees and thus invisible.
     */
    private final class DisplayNextView implements Animation.AnimationListener {
        /**
         * Position in the {@code ListView mPhotosList} whose photo is being transitioned to, or -1
         * if we are transitioning back to {@code ListView mPhotosList}.
         */
        private final int mPosition;

        /**
         * Our constructor, we simply save our parameter in our field {@code int mPosition}.
         *
         * @param position Position in the {@code ListView mPhotosList} whose photo is being
         *                 transitioned to, or -1 if we are transitioning back to {@code ListView mPhotosList}.
         */
        private DisplayNextView(int position) {
            mPosition = position;
        }

        /**
         * Notifies us that the animation has started. We ignore it.
         *
         * @param animation The started animation.
         */
        @Override
        public void onAnimationStart(Animation animation) {
        }

        /**
         * Notifies us that the animation has ended. We add a new instance of {@code SwapViews(mPosition)}
         * to the message queue of {@code ViewGroup mContainer}. The runnable will be run on the user
         * interface thread.
         *
         * @param animation The animation which reached its end.
         */
        @Override
        public void onAnimationEnd(Animation animation) {
            mContainer.post(new SwapViews(mPosition));
        }

        /**
         * Notifies us that the animation is being repeated. We ignore it.
         *
         * @param animation The animation which was repeated.
         */
        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }

    /**
     * This class is responsible for swapping the views and starting the second half of the animation.
     */
    private final class SwapViews implements Runnable {
        /**
         * Position in the {@code ListView mPhotosList} whose photo is being transitioned to, or -1
         * if we are transitioning back to {@code ListView mPhotosList}.
         */
        private final int mPosition;

        /**
         * Our constructor, we simply save our parameter in our field {@code int mPosition}.
         *
         * @param position Position in the {@code ListView mPhotosList} whose photo is being transitioned
         *                 to, or -1 if we are transitioning back to {@code ListView mPhotosList}.
         */
        @SuppressWarnings("WeakerAccess")
        public SwapViews(int position) {
            mPosition = position;
        }

        /**
         * Starts executing the active part our code. First we determine the center {@code (centerX,centerY)}
         * of {@code ViewGroup mContainer} by dividing its width and height by 2.0 respectively. We declare
         * our variable {@code Rotate3dAnimation rotation}, then if our field {@code mPosition} is greater
         * than -1, we set the visibility of {@code ListView mPhotosList} to GONE, and the visibility of
         * {@code ImageView mImageView} to VISIBLE, then request focus for {@code mImageView}, and set
         * {@code rotation} to a new instance of {@code Rotate3dAnimation} for rotating from 90 degrees to
         * 180 degrees, using {@code (centerX,centerY)} as the center, 310.0 as the Z translation at the
         * start, and false as the {@code reverse} flag (so the translation is not reversed).
         * <p>
         * If {@code mPosition} was less than or equal to -1, we set the visibility of {@code ImageView mImageView}
         * to GONE, and the visibility of {@code ListView mPhotosList} to VISIBLE, then request focus for {@code mPhotosList},
         * and set {@code rotation} to a new instance of {@code Rotate3dAnimation} for rotating from 90 degrees to
         * 0 degrees, using {@code (centerX,centerY)} as the center, 310.0 as the Z translation at the start, and false
         * as the {@code reverse} flag (so the translation is not reversed).
         * <p>
         * In both cases we now set the duration of {@code rotation} to 500 milliseconds, set its "fill after"
         * to true so that the transformation that this animation performed will persist when it is finished, set
         * its {@code Interpolator} to a new instance of {@code DecelerateInterpolator}, and start the
         * animation {@code rotation} running for {@code ViewGroup mContainer}.
         */
        @Override
        public void run() {
            final float centerX = mContainer.getWidth() / 2.0f;
            final float centerY = mContainer.getHeight() / 2.0f;
            Rotate3dAnimation rotation;

            if (mPosition > -1) {
                mPhotosList.setVisibility(View.GONE);
                mImageView.setVisibility(View.VISIBLE);
                mImageView.requestFocus();

                rotation = new Rotate3dAnimation(90, 180, centerX, centerY, 310.0f, false);
            } else {
                mImageView.setVisibility(View.GONE);
                mPhotosList.setVisibility(View.VISIBLE);
                mPhotosList.requestFocus();

                rotation = new Rotate3dAnimation(90, 0, centerX, centerY, 310.0f, false);
            }

            rotation.setDuration(500);
            rotation.setFillAfter(true);
            rotation.setInterpolator(new DecelerateInterpolator());

            mContainer.startAnimation(rotation);
        }
    }

}
