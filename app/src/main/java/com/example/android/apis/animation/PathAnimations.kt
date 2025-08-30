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

import android.animation.ObjectAnimator
import android.animation.TypeConverter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.PointF
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.util.Property
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.RadioGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * This application demonstrates the use of Path animation.
 * Moves a frog around an android.graphics.Path using six different ways to move
 * the View: named_components (Uses the named "x" and "y" properties for individual
 * (x, y) coordinates of the Path and sets them on the view object. The setX(float)
 * and setY(float) methods are called on view. An int version of this method also
 * exists for animating int Properties), property_components (Use two Properties
 * for individual (x, y) coordinates of the Path and set them on the view object.
 * An int version of this method also exists for animating int Properties.),
 * multi_int (Use a multi-int setter to animate along a Path. The method
 * setCoordinates(int x, int y) is called on this during the animation. Either
 * "setCoordinates" or "coordinates" are acceptable parameters because the "set"
 * can be implied.), multi_float (Use a multi-float setter to animate along a Path.
 * The method changeCoordinates(float x, float y) is called on this during the
 * animation), named_setter (Use the named "point" property to animate along the Path.
 * There must be a method setPoint(PointF) on the animated object. Because setPoint
 * takes a PointF parameter, no TypeConverter is necessary. In this case, the animated
 * object is PathAnimations.), and property_setter (Use the POINT_PROPERTY property
 * to animate along the Path. POINT_PROPERTY takes a Point, not a PointF, so the
 * TypeConverter PointFToPointConverter is necessary.). The radio buttons to choose
 * which way to use need to be in an HorizontalScrollView not a ScrollView in order
 * to be seen on narrow screens.
 */
@SuppressLint("ObsoleteSdkInt")
@Suppress("MemberVisibilityCanBePrivate")
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class PathAnimations : AppCompatActivity(), RadioGroup.OnCheckedChangeListener,
    View.OnLayoutChangeListener {

    /**
     * The `CanvasView` with id R.id.canvas in our layout file, it contains the smiley face
     * path and our frog.
     */
    private var mCanvasView: CanvasView? = null

    /**
     * The `ObjectAnimator` which moves our frog.
     */
    private var mAnimator: ObjectAnimator? = null

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to our layout file R.layout.path_animations. We initialize our field
     * `CanvasView mCanvasView` by finding the view with id R.id.canvas and add this to its
     * `OnLayoutChangeListener` list (so that our override of `onLayoutChange` will be
     * called when the layout bounds of our view changes due to layout processing). Finally we locate
     * the RadioGroup which selects one of six ways the frog is moved and set its
     * `OnCheckedChangeListener` to "this" (so our override of `onCheckedChanged` will be
     * called when the user clicks a different RadioButton)
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.path_animations)

        mCanvasView = findViewById(R.id.canvas)
        mCanvasView!!.addOnLayoutChangeListener(this)
        (findViewById<View>(R.id.path_animation_type) as RadioGroup).setOnCheckedChangeListener(this)
    }

    /**
     * Set the coordinates to the int (x,y) coordinates, used by the "Property" animation. It
     * simply delegates this to changeCoordinates(float x, float y)
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    fun setCoordinates(x: Int, y: Int) {
        changeCoordinates(x.toFloat(), y.toFloat())
    }

    /**
     * Used by both setPoint(PointF point), and setCoordinates(int x, int y), this method
     * does the actual moving of the R.id.moved_item View.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    fun changeCoordinates(x: Float, y: Float) {
        val v = findViewById<View>(R.id.moved_item)
        v.x = x
        v.y = y
    }

    /**
     * Used by the "Named Property" (R.id.named_setter) RadioButton
     *
     * @param point new position for moving View
     */
    @Suppress("unused") // Used behind our back.
    fun setPoint(point: PointF) {
        changeCoordinates(point.x, point.y)
    }

    /**
     * Called when the checked radio button has changed, it uses the checkedId to change to
     * the newly selected animation method by calling startAnimator(checkedId)
     *
     * @param group the group in which the checked radio button has changed
     * @param checkedId the group in which the checked radio button has changed
     */
    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        startAnimator(checkedId)
    }

    /**
     * Called when the layout bounds of our view changes due to layout processing. It locates the
     * RadioGroup containing the choice of animation type (R.id.path_animation_type), determines
     * whether a RadioButton is already selected and if so, starts that animation by calling
     * startAnimator(checkedId).
     *
     * @param v The view whose bounds have changed.
     * @param left The new value of the view's left property.
     * @param top The new value of the view's top property.
     * @param right The new value of the view's right property.
     * @param bottom The new value of the view's bottom property.
     * @param oldLeft The previous value of the view's left property.
     * @param oldTop The previous value of the view's top property.
     * @param oldRight The previous value of the view's right property.
     * @param oldBottom The previous value of the view's bottom property.
     */
    override fun onLayoutChange(
        v: View, left: Int, top: Int, right: Int, bottom: Int,
        oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
    ) {
        val checkedId =
            (findViewById<View>(R.id.path_animation_type) as RadioGroup).checkedRadioButtonId
        if (checkedId != RadioGroup.NO_ID) {
            startAnimator(checkedId)
        }
    }

    /**
     * This method switches between path animation types whenever a different RadioButton is
     * selected. It sets ObjectAnimator mAnimator one of six different types of ObjectAnimator
     * based on a switch. It first cancels and "null's" any ObjectAnimator mAnimator which might
     * be already running. It then locates the View of our moving ImageView (R.id.moved_item) and
     * saves it for later. It fetches the Path path from our CanvasView mCanvasView, and if it is
     * empty returns (it is not empty because the PathAnimations Activity has already initialized
     * sTraversalPath when it is first instantiated, and our onLayout callback uses sTraversalPath
     * to set mPath using the method sTraversalPath.transform(scale, mPath))
     *
     * Then comes the big switch:
     *  - R.id.named_components: "Named Components" Uses the named "x" and "y" properties for
     * individual (x, y) coordinates of the Path and sets them on the view object. The
     * setX(float) and setY(float) methods are called on view. An int version of this
     * method also exists for animating int Properties. This is accomplished by creating
     * an ObjectAnimator mAnimator by calling the method:
     * ObjectAnimator.ofFloat (Object view, String xPropertyName, String yPropertyName, Path path)
     * that animates coordinates along Path path using the two properties. A Path animation
     * moves in two dimensions, animating coordinates (x, y) together to follow the line.
     * In this variation, the coordinates are floats that are set to separate properties
     * designated by xPropertyName and yPropertyName.
     *  - R.id.property_components: "Property Components" Use two Properties for individual (x, y)
     * coordinates of the Path and sets them on the view object. An int version of this method
     * also exists for animating int Properties. This is accomplished by creating an
     * ObjectAnimator mAnimator by calling the method:
     * ObjectAnimator.ofFloat(T target, android.util.Property<T></T>, java.lang.Float> xProperty,
     * android.util.Property<T></T>, java.lang.Float> yProperty, android.graphics.Path path)
     * which returns an ObjectAnimator that animates coordinates of the target along a Path
     * using the two properties. A Path animation moves in two dimensions, animating coordinates
     * (x, y) together to follow the line. In this variation, the coordinates are floats that
     * are set to separate properties, xProperty and yProperty.
     *  - R.id.multi_int: "Multi-int" Use a multi-int setter to animate along a Path. The method
     * setCoordinates(int x, int y) is called on this during the animation. Either
     * "setCoordinates" or "coordinates" are acceptable parameters because the "set" can
     * be implied. This is accomplished by creating an ObjectAnimator mAnimator by calling
     * the method: ObjectAnimator.ofMultiInt (Object target, String propertyName, Path path)
     * which returns an ObjectAnimator that animates the target using a multi-int setter along
     * the given Path. A Path animation moves in two dimensions, animating coordinates (x, y)
     * together to follow the line. In this variation, the coordinates are integer x and y
     * coordinates used in the first and second parameter of the setter, respectively.
     *  - R.id.multi_float: "Multi-float" Use a multi-float setter to animate along a Path. The method
     * changeCoordinates(float x, float y) is called on this during the animation. This is
     * accomplished by creating an ObjectAnimator mAnimator by calling the method:
     * ObjectAnimator.ofMultiFloat(Object target, String propertyName, Path path) which
     * returns an ObjectAnimator that animates the target using a multi-float setter along
     * the given Path. A Path animation moves in two dimensions, animating coordinates
     * (x, y) together to follow the line. In this variation, the coordinates are float x
     * and y coordinates used in the first and second parameter of the setter, respectively.
     *  - R.id.named_setter: "Named Property" Use the named "point" property to animate along the
     * Path. There must be a method setPoint(PointF) on the animated object. Because setPoint
     * takes a PointF parameter, no TypeConverter is necessary. In this case, the animated
     * object is PathAnimations. This is accomplished by creating an ObjectAnimator mAnimator
     * by calling the method:
     * `ObjectAnimator.ofObject(Object target, String propertyName, TypeConverter<PointF>, ?> converter, Path path)`
     * which returns an ObjectAnimator that animates the target using a PointF to follow
     * the Path. If the Property associated with propertyName uses a type other than PointF,
     * converter can be used to change from PointF to the type associated with the Property.
     *  - R.id.property_setter: "Property" Use the POINT_PROPERTY property to animate along the Path.
     * POINT_PROPERTY takes a Point, not a PointF, so the TypeConverter PointFToPointConverter
     * is necessary. This is accomplished by creating an ObjectAnimator mAnimator  by calling
     * the method:
     * `ObjectAnimator.ofObject(T target, Property<T></T>, V> property, TypeConverter<PointF>, V> converter, Path path)`
     * which returns an ObjectAnimator that animates the target using a PointF to follow the
     * Path. Since property uses a type other than PointF, TypeConverter PointFToPointConverter
     * is used to change from PointF to the type Point associated with the Property.
     *
     * Having created an `ObjectAnimator mAnimator` of the desired type based on the RadioButton
     * selected, we set the duration of `mAnimator` to 10000 milliseconds, its repeat mode to
     * RESTART, its repeat count to INFINITE, and its Interpolator to an instance of LinearInterpolator
     * then start the animation running.
     *
     * @param checkedId RadioButton for animation type which is selected
     */
    @SuppressLint("ObjectAnimatorBinding")
    private fun startAnimator(checkedId: Int) {
        if (mAnimator != null) {
            mAnimator!!.cancel()
            mAnimator = null
        }

        val view = findViewById<View>(R.id.moved_item)
        val path = mCanvasView!!.path
        if (path.isEmpty) {
            return
        }

        @Suppress("CAST_NEVER_SUCCEEDS")
        when (checkedId) {
            R.id.named_components ->
                // Use the named "x" and "y" properties for individual (x, y)
                // coordinates of the Path and set them on the view object.
                // The setX(float) and setY(float) methods are called on view.
                // An int version of this method also exists for animating
                // int Properties.
                mAnimator = ObjectAnimator.ofFloat(view, "x", "y", path)

            R.id.property_components ->
                // Use two Properties for individual (x, y) coordinates of the Path
                // and set them on the view object.
                // An int version of this method also exists for animating
                // int Properties.
                mAnimator = ObjectAnimator.ofFloat(view, View.X, View.Y, path)

            R.id.multi_int ->
                // Use a multi-int setter to animate along a Path. The method
                // setCoordinates(int x, int y) is called on this during the animation.
                // Either "setCoordinates" or "coordinates" are acceptable parameters
                // because the "set" can be implied.
                mAnimator = ObjectAnimator.ofMultiInt(this, "setCoordinates", path)

            R.id.multi_float ->
                // Use a multi-float setter to animate along a Path. The method
                // changeCoordinates(float x, float y) is called on this during the animation.
                mAnimator = ObjectAnimator.ofMultiFloat(
                    this,
                    "changeCoordinates",
                    path
                )

            R.id.named_setter ->
                // Use the named "point" property to animate along the Path.
                // There must be a method setPoint(PointF) on the animated object.
                // Because setPoint takes a PointF parameter, no TypeConverter is necessary.
                // In this case, the animated object is PathAnimations.

                mAnimator = ObjectAnimator.ofObject(
                    this,
                    "point",
                    hack, // a null TypeConverter<PointF, *> (pitiful!)
                    path
                )

            R.id.property_setter ->
                // Use the POINT_PROPERTY property to animate along the Path.
                // POINT_PROPERTY takes a Point, not a PointF, so the TypeConverter
                // PointFToPointConverter is necessary.
                mAnimator = ObjectAnimator.ofObject(
                    this, POINT_PROPERTY,
                    PointFToPointConverter(), path
                )
        }

        mAnimator!!.duration = 10000
        mAnimator!!.repeatMode = ValueAnimator.RESTART
        mAnimator!!.repeatCount = ValueAnimator.INFINITE
        mAnimator!!.interpolator = LinearInterpolator()
        mAnimator!!.start()
    }

    /**
     * This is the View which contains our demo.
     */
    class CanvasView : FrameLayout {

        /**
         * Getter for our field Path mPath
         *
         * @return the value of the field Path mPath.
         */
        var path: Path = Path()
            internal set

        /**
         * `Paint` we use to draw `Path mPath`.
         */
        internal var mPathPaint = Paint()

        /**
         * Construct a new CanvasView and initialize it. (Not used since our CanvasView comes from
         * the reference in xml not our code)
         *
         * @param context Context of our View
         */
        constructor(context: Context) : super(context) {
            init()
        }

        /**
         * Construct a new CanvasView and initialize it. This is the constructor which is used by
         * the xml processor of our layout file path_animations.xml
         *
         * @param context Context of our View
         * @param attrs   Attributes defined in xml
         */
        constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
            init()
        }

        /**
         * Construct a new CanvasView and initialize it. This constructor is not used since our
         * layout file does not specify an android:style attribute for our CanvasView
         *
         * @param context  Context of our View
         * @param attrs    Attributes defined in xml
         * @param defStyle android:style attribute
         */
        constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
            context,
            attrs,
            defStyle
        ) {
            init()
        }

        /**
         * Initialize this instance of CanvasView. First call View.setWillNotDraw(false) to signal
         * that we will handle our own drawing in our draw(Canvas) override, then set the color
         * (red with and alpha of 0xFF), stroke width, and style of our Paint mPathPaint.
         */
        private fun init() {
            setWillNotDraw(false)
            mPathPaint.color = -0x10000
            mPathPaint.strokeWidth = 2.0f
            mPathPaint.style = Paint.Style.STROKE
        }

        /**
         * Called from layout when this view should assign a size and position to each of
         * its children. If the layout size or position has changed we calculate the scale
         * width and scale height to use to fit our demo in our assigned size. Then we create
         * a transform matrix to scale our traversal path using Matrix.setScale, and
         * Transform the points in this path by this matrix, and write the answer into mPath.
         *
         * @param changed This is a new size or position for this view
         * @param left Left position, relative to parent
         * @param top Top position, relative to parent
         * @param right Right position, relative to parent
         * @param bottom Bottom position, relative to parent
         */
        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            super.onLayout(changed, left, top, right, bottom)
            if (changed) {
                @SuppressLint("DrawAllocation") val scale = Matrix()
                val scaleWidth = (right - left) / TRAVERSE_PATH_SIZE
                val scaleHeight = (bottom - top) / TRAVERSE_PATH_SIZE
                scale.setScale(scaleWidth, scaleHeight)
                sTraversalPath.transform(scale, path)
            }
        }

        /**
         * Draw our Path mPath using the Paint mPathPaint. The path will be filled or framed
         * based on the Style in the paint, then call our super's version View.draw
         *
         * @param canvas The Canvas to which the View is rendered.
         */
        override fun draw(canvas: Canvas) {
            canvas.drawPath(path, mPathPaint)
            super.draw(canvas)
        }
    }

    /**
     * Class used to provide a `TypeConverter<PointF, Point>` for our property setter ("Property")
     * animation type.
     */
    private class PointFToPointConverter : TypeConverter<PointF, Point>(
        PointF::class.java, Point::class.java
    ) {
        var mPoint = Point()

        /**
         * Converts a value from PointF to Point by rounding the float (x,y) coordinates of the
         * PointF to int. Saves the value in the field Point mPoint and returns it.
         *
         * @param value The PointF Object to convert.
         * @return A value of type Point, converted from PointF
         */
        override fun convert(value: PointF): Point {
            mPoint.set(value.x.roundToInt(), value.y.roundToInt())
            return mPoint
        }
    }

    companion object {

        /**
         * A silly hack to help the kotlin compiler disambiguate between ambiguous overloads.
         */
        val hack: TypeConverter<PointF, *>? = null

        /**
         * Smiley face path that our frog traces.
         */
        internal val sTraversalPath = Path()

        /**
         * Scaling factor our `CanvasView` uses when scaling `Path sTraversalPath` into the
         * `Path mPath` which it draws.
         */
        internal const val TRAVERSE_PATH_SIZE = 7.0f

        /**
         * This static field is used for the animation when the "Property" RadioButton is selected.
         */
        internal val POINT_PROPERTY: Property<PathAnimations, Point> =
            object : Property<PathAnimations, Point>(Point::class.java, "point") {
                /**
                 * Returns the current value that this property represents on the given **object**.
                 *
                 * @param object the PathAnimations instance in question ("this" essentially)
                 * @return a Point containing the current (x,y) coordinates of the animation
                 */
                override fun get(`object`: PathAnimations): Point {
                    val v = `object`.findViewById<View>(R.id.moved_item)
                    return Point(v.x.roundToInt(), v.y.roundToInt())
                }

                /**
                 * Sets the value on "object" which this property represents. If the method is unable to
                 * set the value on the target object it will throw an UnsupportedOperationException
                 * exception.
                 *
                 * @param targetObject the PathAnimations instance in question ("this" essentially)
                 * @param value a Point containing the (x,y) coordinates to set out animation to
                 */
                override fun set(targetObject: PathAnimations, value: Point) {
                    targetObject.setCoordinates(value.x, value.y)
                }
            }

        /*
         * Here we set up the path of `Path sTraversalPath`
         */
        init {
            val inverseSqrt8 = sqrt(0.125).toFloat()
            val bounds = RectF(1f, 1f, 3f, 3f)
            sTraversalPath.addArc(bounds, 45f, 180f)
            sTraversalPath.addArc(bounds, 225f, 180f)

            bounds.set(
                1.5f + inverseSqrt8, 1.5f + inverseSqrt8, 2.5f + inverseSqrt8,
                2.5f + inverseSqrt8
            )
            sTraversalPath.addArc(bounds, 45f, 180f)
            sTraversalPath.addArc(bounds, 225f, 180f)

            bounds.set(4f, 1f, 6f, 3f)
            sTraversalPath.addArc(bounds, 135f, -180f)
            sTraversalPath.addArc(bounds, -45f, -180f)

            bounds.set(
                4.5f - inverseSqrt8,
                1.5f + inverseSqrt8,
                5.5f - inverseSqrt8,
                2.5f + inverseSqrt8
            )
            sTraversalPath.addArc(bounds, 135f, -180f)
            sTraversalPath.addArc(bounds, -45f, -180f)

            sTraversalPath.addCircle(3.5f, 3.5f, 0.5f, Path.Direction.CCW)

            sTraversalPath.addArc(RectF(1f, 2f, 6f, 6f), 0f, 180f)
        }
    }
}
