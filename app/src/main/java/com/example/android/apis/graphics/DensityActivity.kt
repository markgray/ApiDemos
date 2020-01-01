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
package com.example.android.apis.graphics

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity

//Need the following import to get access to the app resources, since this
//class is in a sub-package.
import com.example.android.apis.R

/**
 * This activity demonstrates various ways density can cause the scaling of
 * bitmaps and drawables. Includes sample code for different ways to get
 * drawables onto the different dpi screens.
 */
class DensityActivity : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we fetch a handle to the system-level service LAYOUT_INFLATER_SERVICE to
     * initialize our [LayoutInflater] variable `val li`. We set our title to R.string.density_title,
     * and the system chooses the title from 5 different strings depending on the screen density:
     *
     *  * values/strings.xml "Density: Unknown Screen"
     *  * values-hdpi/strings.xml "Density: High"
     *  * values-ldpi/strings.xml "Density: Low"
     *  * values-mdpi/strings.xml "Density: Medium"
     *  * values-xhdpi/strings.xml "Density: Extra High"
     *
     * We create an instance of [LinearLayout] for `val root` and set its orientation to VERTICAL.
     * This is the `ViewGroup` we will add 9 rows of [LinearLayout] `var layout`'s to, each row
     * consisting of 120dpi, 160dpi and 240dpi images stored and loaded using different approaches:
     *
     *  * "Pre-scaled bitmap in drawable" uses our method [addBitmapDrawable] to add the resource
     *  images logo120dpi.png, logo160dpi.png, and logo240dpi.png (loaded using scaling) to
     *  [LinearLayout] `var layout` which we then add to `root` using our method [addChildToRoot]     *
     *  * "Auto-scaled bitmap in drawable" uses our method [addBitmapDrawable] to add the resource
     *  images logo120dpi.png, logo160dpi.png, and logo240dpi.png (loaded without scaling) to
     *  [LinearLayout] `layout` which we then add to `root` using our method [addChildToRoot]
     *  * "Pre-scaled resource drawable" uses our method [addResourceDrawable] to load the resource
     *  images logo120dpi.png, logo160dpi.png, and logo240dpi.png, loaded using [getDrawable] and
     *  used to set the background of a view which it adds to [LinearLayout] `layout` which we then
     *  add to `root` using our method [addChildToRoot]
     *  * "Inflated layout" inflates the layout file R.layout.density_image_views which creates a
     *  [LinearLayout] for `layout` containing three `ImageView`'s which use the resource images
     *  logo120dpi.png, logo160dpi.png, and logo240dpi.png as their content which we then add to
     *  `root` using our method [addChildToRoot]
     *  * "Inflated styled layout" inflates the layout file R.layout.density_styled_image_views
     *  which creates a [LinearLayout] for `layout` containing three `ImageView`'s which use
     *  style/ImageView120dpi, style/ImageView160dpi, and style/ImageView240dpi to access images
     *  stylogo120dpi.png, stylogo160dpi.png and stylogo240dpi.png as their content which we then
     *  add to `root` using our method [addChildToRoot]
     *  * "Pre-scaled bitmap" uses our method [addCanvasBitmap] to load the resource images
     *  logo120dpi.png, logo160dpi.png, and logo240dpi.png, (loaded using scaling) into instances
     *  of our custom [View] subclass [ScaledBitmapView] which it adds to [LinearLayout] `layout`
     *  which we then add to `root` using our method [addChildToRoot]
     *  * "Auto-scaled bitmap" uses our method [addCanvasBitmap] to load the resource images
     *  logo120dpi.png, logo160dpi.png, and logo240dpi.png, (loaded without scaling) into instances
     *  of our custom [View] subclass [ScaledBitmapView] which it adds to [LinearLayout] `layout`
     *  which we then add to `root` using our method [addChildToRoot]
     *  * "No-dpi resource drawable" uses our method [addResourceDrawable] to load the resource
     *  images R.drawable.logonodpi120.png, R.drawable.logonodpi160.png, and R.drawable.logonodpi240.png,
     *  loaded using [getDrawable] and used to set the background of a view which it adds to
     *  [LinearLayout] `layout` which we then add to `root` using our method [addChildToRoot]
     *  * "Pre-scaled 9-patch resource drawable" uses our method [addNinePatchResourceDrawable] to
     *  add R.drawable.smlnpatch120dpi.9.png, R.drawable.smlnpatch160dpi.9.png, and
     *  R.drawable.smlnpatch240dpi.9.png to [LinearLayout] `layout` which we then add to `root`
     *  using our method [addChildToRoot]
     *
     * Finally we set our content view to [LinearLayout] `root` wrapped in a [ScrollView] by our
     * method [scrollWrap].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val li = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        this.setTitle(R.string.density_title)
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        var layout = LinearLayout(this)
        addBitmapDrawable(layout, R.drawable.logo120dpi, true)
        addBitmapDrawable(layout, R.drawable.logo160dpi, true)
        addBitmapDrawable(layout, R.drawable.logo240dpi, true)
        addLabelToRoot(root, "Pre-scaled bitmap in drawable")
        addChildToRoot(root, layout)
        layout = LinearLayout(this)
        addBitmapDrawable(layout, R.drawable.logo120dpi, false)
        addBitmapDrawable(layout, R.drawable.logo160dpi, false)
        addBitmapDrawable(layout, R.drawable.logo240dpi, false)
        addLabelToRoot(root, "Auto-scaled bitmap in drawable")
        addChildToRoot(root, layout)
        layout = LinearLayout(this)
        addResourceDrawable(layout, R.drawable.logo120dpi)
        addResourceDrawable(layout, R.drawable.logo160dpi)
        addResourceDrawable(layout, R.drawable.logo240dpi)
        addLabelToRoot(root, "Pre-scaled resource drawable")
        addChildToRoot(root, layout)
        layout = li.inflate(R.layout.density_image_views, root, false) as LinearLayout
        addLabelToRoot(root, "Inflated layout")
        addChildToRoot(root, layout)
        layout = li.inflate(R.layout.density_styled_image_views, root, false) as LinearLayout
        addLabelToRoot(root, "Inflated styled layout")
        addChildToRoot(root, layout)
        layout = LinearLayout(this)
        addCanvasBitmap(layout, R.drawable.logo120dpi, true)
        addCanvasBitmap(layout, R.drawable.logo160dpi, true)
        addCanvasBitmap(layout, R.drawable.logo240dpi, true)
        addLabelToRoot(root, "Pre-scaled bitmap")
        addChildToRoot(root, layout)
        layout = LinearLayout(this)
        addCanvasBitmap(layout, R.drawable.logo120dpi, false)
        addCanvasBitmap(layout, R.drawable.logo160dpi, false)
        addCanvasBitmap(layout, R.drawable.logo240dpi, false)
        addLabelToRoot(root, "Auto-scaled bitmap")
        addChildToRoot(root, layout)
        layout = LinearLayout(this)
        addResourceDrawable(layout, R.drawable.logonodpi120)
        addResourceDrawable(layout, R.drawable.logonodpi160)
        addResourceDrawable(layout, R.drawable.logonodpi240)
        addLabelToRoot(root, "No-dpi resource drawable")
        addChildToRoot(root, layout)
        layout = LinearLayout(this)
        addNinePatchResourceDrawable(layout, R.drawable.smlnpatch120dpi)
        addNinePatchResourceDrawable(layout, R.drawable.smlnpatch160dpi)
        addNinePatchResourceDrawable(layout, R.drawable.smlnpatch240dpi)
        addLabelToRoot(root, "Pre-scaled 9-patch resource drawable")
        addChildToRoot(root, layout)
        setContentView(scrollWrap(root))
    }

    /**
     * Wraps the [View] it is passed inside a [ScrollView], which it returns. First we create
     * [ScrollView] for `val scroller`, then we add our [View] parameter [view] to `scroller`
     * using MATCH_PARENT for the layout parameters for both width and height. Finally we return
     * `scroller` to the caller.
     *
     * @param view [View] we are to add to a [ScrollView] which we return
     * @return a [ScrollView] containing our [View] parameter [view] as its only child
     */
    private fun scrollWrap(view: View): View {
        val scroller = ScrollView(this)
        scroller.addView(view, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT))
        return scroller
    }

    /**
     * Adds a [TextView] displaying the text of [String] parameter [text] to the [LinearLayout]
     * parameter [root]. First we create a new instance of [TextView] for our variable `val label`,
     * set the text of `label` to [String] parameter [text] and add `label` to [LinearLayout]
     * parameter [root] using MATCH_PARENT for the width, and WRAP_CONTENT for the height of its
     * [LinearLayout.LayoutParams] layout parameters.
     *
     * @param root The [LinearLayout] we are to add a label to
     * @param text The text for the [TextView] we will add to our [LinearLayout] parameter [root]
     */
    private fun addLabelToRoot(root: LinearLayout, text: String) {
        val label = TextView(this)
        label.text = text
        root.addView(label, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT))
    }

    /**
     * Adds the [LinearLayout] parameter [layout] to our [LinearLayout] parameter [root] using the
     * layout parameters MATCH_PARENT for width, and WRAP_CONTENT for height
     *
     * @param root   [LinearLayout] parameter `ViewGroup` which we want to add [LinearLayout]
     * parameter [layout] to
     * @param layout [LinearLayout] we are to add to [LinearLayout] parameter [root]
     */
    private fun addChildToRoot(root: LinearLayout, layout: LinearLayout) {
        root.addView(layout, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT))
    }

    /**
     * Adds a [Bitmap] decoded from resource ID parameter [resource] to [LinearLayout] parameter
     * [layout], optionally using scaling if the [Boolean] flag parameeter [scale] is true. First
     * we declare the [Bitmap] variable `val bitmap`, then we use our method [loadAndPrintDpi] to
     * load the resource image with resource ID parameter [resource] using the [Boolean] flag
     * parameter [scale] to determine whether the image should be decoded using the no scaling
     * option. We create [View] variable `val view` create [BitmapDrawable] variable `val d` from
     * `bitmap`, and if `scale` is false (no scaling) we set the density scale that it should be
     * rendered at to the current display metrics. We set the background of `view` to `d`, set the
     * layout parameters of `view` to the intrinsic height and width of `d`, and finally add `view`
     * to [layout].
     *
     * @param layout   [LinearLayout] we are to add the resource `Drawable` to
     * @param resource resource ID of a `Drawable` to read and use from our resources
     * @param scale    flag indicating whether our method [loadAndPrintDpi] should use scaling when
     * decoding the resource image.
     */
    private fun addBitmapDrawable(layout: LinearLayout, resource: Int, scale: Boolean) {
        val bitmap: Bitmap = loadAndPrintDpi(resource, scale)
        val view = View(this)
        val d = BitmapDrawable(resources, bitmap)
        if (!scale) d.setTargetDensity(resources.displayMetrics)
        view.background = d
        view.layoutParams = LinearLayout.LayoutParams(d.intrinsicWidth, d.intrinsicHeight)
        layout.addView(view)
    }

    /**
     * Creates a [View] variable `val view`, loads the resource drawable with resource ID of our
     * parameter [resource] into a `Drawable` variable `val d` and sets the background drawable of
     * `view` to it. Sets the layout parameters of `view` to the intrinsic width and height of `d`
     * then adds `view` to [LinearLayout] parameter [layout].
     *
     * @param layout   [LinearLayout] `ViewGroup` we are to add the resource image with resource ID
     * [resource] to.
     * @param resource resource ID of an resource image to load.
     */
    private fun addResourceDrawable(layout: LinearLayout, resource: Int) {
        val view = View(this)
        @Suppress("DEPRECATION")
        val d = resources.getDrawable(resource)
        view.background = d
        view.layoutParams = LinearLayout.LayoutParams(d.intrinsicWidth, d.intrinsicHeight)
        layout.addView(view)
    }

    /**
     * Adds a [ScaledBitmapView] to our [LinearLayout] parameter [layout], created using a [Bitmap]
     * loaded from the resource image given by the resource ID parameter [resource], and optionally
     * scaled first if our [scale] parameter is *true*.
     *
     * @param layout   [LinearLayout] we are to add the [Bitmap] we load from the resource image
     * given by the resource ID parameter [resource] to.
     * @param resource resource ID of png image we should load into a [Bitmap] and add to [layout]
     * @param scale    if *true* we want to allow [loadAndPrintDpi] to scale the image when loading
     * it into a [Bitmap], *false* if we want it unscaled
     */
    private fun addCanvasBitmap(layout: LinearLayout, resource: Int, scale: Boolean) {
        val bitmap: Bitmap = loadAndPrintDpi(resource, scale)
        val view = ScaledBitmapView(this, bitmap)
        view.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        layout.addView(view)
    }

    /**
     * Loads a nine-patch png image with resource ID of our parameter [resource] from our resources,
     * and adds a [View] using it as the background drawable to the [LinearLayout] parameter [layout]
     *
     * @param layout   [LinearLayout] to which we will add the `Drawable` we load from the resource
     * image with resource ID [resource]
     * @param resource resource ID for a nine-patch png image to load into a `Drawable` and then
     * use as the background for a view which we add to [LinearLayout] parameter [layout]
     */
    private fun addNinePatchResourceDrawable(layout: LinearLayout, resource: Int) {
        val view = View(this)
        @Suppress("DEPRECATION")
        val d = resources.getDrawable(resource)
        view.background = d
        Log.i("foo", "9-patch #" + Integer.toHexString(resource)
                + " w=" + d.intrinsicWidth + " h=" + d.intrinsicHeight)
        view.layoutParams = LinearLayout.LayoutParams(d.intrinsicWidth * 2, d.intrinsicHeight * 2)
        layout.addView(view)
    }

    /**
     * Decodes the resource image with the resource ID [id] into a [Bitmap] which it returns,
     * optionally applying scaling when decoding it if our [Boolean] parameter [scale] is *true*.
     * We declare our [Bitmap] variable `val bitmap` then branch on the value of our parameter
     * scale:
     *  * *true* we set `bitmap` to the [Bitmap] that the [BitmapFactory.decodeResource] method
     *  returns when called with a Resources instance for our application's package and our
     *  resource ID parameter [id].
     *  * *false* we initialize our [BitmapFactory.Options] variable `val opts` to a new instance
     *  then set its `inScaled` field to *true* (when this flag is set the bitmap will be scaled to
     *  match `inTargetDensity` when loaded, rather than relying on the graphics system scaling it
     *  each time it is drawn to a Canvas). Then we set `bitmap` to the [Bitmap] that the
     *  [BitmapFactory.decodeResource] method returns when called with a Resources instance for our
     *  application's package, our resource ID parameter [id] and our `Options` variable `opts`.
     *
     *  In either case we return `bitmap` to the caller.
     *
     * @param id    resource ID of an image to load into the [Bitmap] we return
     * @param scale if *true* we allow `decodeResource` to scale the image, if *false* it's unscaled
     * @return [Bitmap] decoded from resource ID [id] resource image
     */
    private fun loadAndPrintDpi(id: Int, scale: Boolean): Bitmap {
        val bitmap: Bitmap
        if (scale) {
            bitmap = BitmapFactory.decodeResource(resources, id)
        } else {
            val opts = BitmapFactory.Options()
            opts.inScaled = false
            bitmap = BitmapFactory.decodeResource(resources, id, opts)
        }
        return bitmap
    }

    /**
     * Custom [View] which uses the scaled width and height of the [Bitmap] it contains
     * given the target density of the current display metrics.
     */
    private inner class ScaledBitmapView
    /**
     * Simple constructor, first we call through to our super's constructor, then we save our parameter
     * `Bitmap bitmap` in our field `Bitmap mBitmap`
     *
     * @param context `Context` to use for resources, "this" `DensityActivity` in our case
     * Parameter: bitmap `Bitmap` we are to hold and Display.
     */(context: Context?,
        /**
         * `Bitmap` we were created to hold and draw when our `onDraw` override is called.
         */
        private val mBitmap: Bitmap) : View(context) {

        /**
         * Measure the view and its content to determine the measured width and the
         * measured height. This method is invoked by [.measure] and
         * should be overridden by subclasses to provide accurate and efficient
         * measurement of their contents.
         *
         *
         * First we call through to our super's implementation of `onMeasure`, then we set
         * `DisplayMetrics metrics` to the current display metrics that are in effect for
         * the resources associated with this view. We use `metrics` to determine the width
         * and height of `Bitmap mBitmap` given the target density of the current display
         * metrics, which we then pass to the method `setMeasuredDimension` to store as our
         * width and height.
         *
         * @param widthMeasureSpec  horizontal space requirements as imposed by the parent.
         * The requirements are encoded with
         * [android.view.View.MeasureSpec].
         * @param heightMeasureSpec vertical space requirements as imposed by the parent.
         * The requirements are encoded with
         * [android.view.View.MeasureSpec].
         */
        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            val metrics = resources.displayMetrics
            setMeasuredDimension(
                    mBitmap.getScaledWidth(metrics),
                    mBitmap.getScaledHeight(metrics))
        }

        /**
         * We implement this to do our drawing. First we call through to our super's implementation of
         * `onDraw`, then we instruct `Canvas canvas` to draw `Bitmap mBitmap` at
         * (0,0) using a null `Paint`.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawBitmap(mBitmap, 0.0f, 0.0f, null)
        }

    }
}