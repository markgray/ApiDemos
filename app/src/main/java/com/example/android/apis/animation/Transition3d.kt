package com.example.android.apis.animation

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

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
class Transition3d : AppCompatActivity(), OnItemClickListener, View.OnClickListener {
    /**
     * [ListView] with ID android.R.id.list containing names of photos
     */
    private var mPhotosList: ListView? = null

    /**
     * [FrameLayout] with ID R.id.container containing both [ListView] field [mPhotosList] and
     * [ImageView] field [mImageView]
     */
    private var mContainer: ViewGroup? = null

    /**
     * [ImageView] with ID R.id.picture which displays selected photo
     */
    private var mImageView: ImageView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.animations_main_screen.
     * We initialize [ListView] field [mPhotosList] by locating the view with ID android.R.id.list,
     * we initialize [ImageView] field [mImageView] by locating the view with ID R.id.picture, and
     * we initialize [ViewGroup] field [mContainer] by locating the view group with ID R.id.container.
     *
     * We create `ArrayAdapter<String>` `val adapter` using android.R.layout.simple_list_item_1 as
     * the layout file containing a [TextView] to use when instantiating views, and PHOTOS_NAMES as
     * the objects to represent in the [ListView], set it as the adapter for our [ListView] field
     * [mPhotosList], and set "this" as the `OnItemClickListener` for [mPhotosList].
     *
     * We enable click events for [ImageView] field [mImageView], enable it to receive focus, and
     * set "this" as its [View.OnClickListener].
     *
     * Finally we the set the types of drawing caches that should be kept in memory after they have
     * been created for [ViewGroup] field [mContainer] to PERSISTENT_ANIMATION_CACHE (indicates that
     * the animation drawing cache should be kept in memory) Note: this last action is deprecated
     * and rather wasteful in the era of hardware acceleration.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.animations_main_screen)
        mPhotosList = findViewById(android.R.id.list)
        mImageView = findViewById(R.id.picture)
        mContainer = findViewById(R.id.container)

        // Prepare the ListView
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, PHOTOS_NAMES)
        mPhotosList!!.adapter = adapter
        mPhotosList!!.onItemClickListener = this

        // Prepare the ImageView
        mImageView!!.isClickable = true
        mImageView!!.isFocusable = true
        mImageView!!.setOnClickListener(this)

        // Since we are caching large views, we want to keep their cache
        // between each animation
        @Suppress("UsePropertyAccessSyntax", "DEPRECATION")
        mContainer!!.setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE)
    }

    /**
     * Set up a new 3D rotation on the container view. We locate the center of [ViewGroup] field
     * [mContainer] `(centerX,centerY)` by dividing the width and height of [mContainer] by 2
     * respectively. We create [Rotate3dAnimation] `val rotation` using our parameters [start] as
     * the start angle, [end] as the end angle of the 3D rotation, our variables `centerX` as the
     * X center, `centerY` as the Y center of the 3D rotation, 310.0 as the translation on the Z
     * axis when the animation starts, and the flag true to indicate that the translation should
     * be reversed. We set the duration of `rotation` to 500 milliseconds, set its "fill after" to
     * true so that the transformation that this animation performed will persist when it is finished,
     * set its [Interpolator] to a new instance of [AccelerateInterpolator], and set its
     * [Animation.AnimationListener] to a new instance of [DisplayNextView] constructed for the item
     * whose position is our [Int] parameter [position].
     *
     * @param position the item that was clicked to show a picture, or -1 to show the list
     * @param start    the start angle at which the rotation must begin
     * @param end      the end angle of the rotation
     */
    @Suppress("SameParameterValue")
    private fun applyRotation(position: Int, start: Float, end: Float) {
        // Find the center of the container
        val centerX = mContainer!!.width / 2.0f
        val centerY = mContainer!!.height / 2.0f

        // Create a new 3D rotation with the supplied parameter
        // The animation listener is used to trigger the next animation
        val rotation = Rotate3dAnimation(start, end, centerX, centerY, 310.0f, true)
        rotation.duration = 500
        rotation.fillAfter = true
        rotation.interpolator = AccelerateInterpolator()
        rotation.setAnimationListener(DisplayNextView(position))
        mContainer!!.startAnimation(rotation)
    }

    /**
     * Callback method to be invoked when an item in the [AdapterView] of [ListView] field
     * [mPhotosList] has been clicked. First we set the drawable whose resource ID is at [position]
     * in our array [PHOTOS_RESOURCES] as the content of [ImageView] field [mImageView] (this does
     * [Bitmap] reading and decoding on the UI thread, which can cause a latency hiccup). Finally we
     * call our method [applyRotation] to set up a new 3D rotation on the container view for
     * transitioning between [ListView] field [mPhotosList] and [ImageView] field [mImageView] and
     * starts that animation.
     *
     * @param parent   The [AdapterView] where the click happened.
     * @param v        The [View] within the [AdapterView] that was clicked (this will be a [View]
     *                 provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    override fun onItemClick(parent: AdapterView<*>?, v: View, position: Int, id: Long) {
        // Pre-load the image then start the animation
        mImageView!!.setImageResource(PHOTOS_RESOURCES[position])
        applyRotation(position, 0f, 90f)
    }

    /**
     * Called when [ImageView] field [mImageView] has been clicked. We simply call our method
     * [applyRotation] to rotate back from [ImageView] field [mImageView] to [ListView] field
     * [mPhotosList].
     *
     * @param v The [View] that was clicked ([ImageView] field [mImageView] in our case)
     */
    override fun onClick(v: View) {
        applyRotation(-1, 180f, 90f)
    }

    /**
     * This class listens for the end of the first half of the animation. It then posts a new action
     * that effectively swaps the views when the container is rotated 90 degrees and thus invisible.
     */
    private inner class DisplayNextView(
            /**
             * Position in the [ListView] field [mPhotosList] whose photo is being transitioned to,
             * or -1 if we are transitioning back to [mPhotosList].
             */
            private val mPosition: Int

    ) : Animation.AnimationListener {

        /**
         * Notifies us that the animation has started. We ignore it.
         *
         * @param animation The started animation.
         */
        override fun onAnimationStart(animation: Animation) {}

        /**
         * Notifies us that the animation has ended. We add a new instance of [SwapViews] constructed
         * for position [mPosition] to the message queue of [ViewGroup] field [mContainer]. The
         * runnable will be run on the user interface thread.
         *
         * @param animation The animation which reached its end.
         */
        override fun onAnimationEnd(animation: Animation) {
            mContainer!!.post(SwapViews(mPosition))
        }

        /**
         * Notifies us that the animation is being repeated. We ignore it.
         *
         * @param animation The animation which was repeated.
         */
        override fun onAnimationRepeat(animation: Animation) {}

    }

    /**
     * This class is responsible for swapping the views and starting the second half of the animation.
     */
    private inner class SwapViews(
            /**
             * Position in the [ListView] field [mPhotosList] whose photo is being transitioned to,
             * or -1 if we are transitioning back to [mPhotosList].
             */
            private val mPosition: Int

    ) : Runnable {

        /**
         * Starts executing the active part our code. First we determine the center `(centerX,centerY)`
         * of [ViewGroup] field [mContainer] by dividing its width and height by 2.0 respectively. We
         * declare our [Rotate3dAnimation] variable `val rotation`, then if our field [mPosition] is
         * greater than -1, we set the visibility of [ListView] field [mPhotosList] to GONE, and the
         * visibility of [ImageView] field [mImageView] to VISIBLE, then request focus for [mImageView],
         * and set `rotation` to a new instance of [Rotate3dAnimation] for rotating from 90 degrees to
         * 180 degrees, using `(centerX,centerY)` as the center, 310.0 as the Z translation at the
         * start, and false as the `reverse` flag (so the translation is not reversed).
         *
         * If [mPosition] was less than or equal to -1, we set the visibility of [ImageView] field
         * [mImageView] to GONE, and the visibility of [ListView] field [mPhotosList] to VISIBLE,
         * then request focus for [mPhotosList], and set `rotation` to a new instance of
         * [Rotate3dAnimation] for rotating from 90 degrees to 0 degrees, using `(centerX,centerY)`
         * as the center, 310.0 as the Z translation at the start, and false as the `reverse` flag
         * (so the translation is not reversed).
         *
         * In both cases we now set the duration of `rotation` to 500 milliseconds, set its "fill
         * after" to true so that the transformation that this animation performed will persist
         * when it is finished, set its [Interpolator] to a new instance of [DecelerateInterpolator],
         * and start the animation `rotation` running for [ViewGroup] field [mContainer].
         */
        override fun run() {
            val centerX = mContainer!!.width / 2.0f
            val centerY = mContainer!!.height / 2.0f
            val rotation: Rotate3dAnimation
            if (mPosition > -1) {
                mPhotosList!!.visibility = View.GONE
                mImageView!!.visibility = View.VISIBLE
                mImageView!!.requestFocus()
                rotation = Rotate3dAnimation(
                        90f, 180f,
                        centerX, centerY,
                        310.0f, false
                )
            } else {
                mImageView!!.visibility = View.GONE
                mPhotosList!!.visibility = View.VISIBLE
                mPhotosList!!.requestFocus()
                rotation = Rotate3dAnimation(
                        90f, 0f,
                        centerX, centerY,
                        310.0f, false
                )
            }
            rotation.duration = 500
            rotation.fillAfter = true
            rotation.interpolator = DecelerateInterpolator()
            mContainer!!.startAnimation(rotation)
        }

    }

    companion object {
        /**
         * Names of the photos we show in the list
         */
        private val PHOTOS_NAMES = arrayOf(
                "Lyon",
                "Livermore",
                "Tahoe Pier",
                "Lake Tahoe",
                "Grand Canyon",
                "Bodie"
        )

        /**
         * Resource identifiers for the photos we want to display
         */
        private val PHOTOS_RESOURCES = intArrayOf(
                R.drawable.photo1,
                R.drawable.photo2,
                R.drawable.photo3,
                R.drawable.photo4,
                R.drawable.photo5,
                R.drawable.photo6
        )
    }
}