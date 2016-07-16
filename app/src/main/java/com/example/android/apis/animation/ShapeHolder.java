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

package com.example.android.apis.animation;

import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;

/**
 * A data structure that holds a Shape and various properties that can be used to define
 * how the shape is drawn.
 */
public class ShapeHolder {
    private float x = 0, y = 0;
    private ShapeDrawable shape;
    private int color;
    private RadialGradient gradient;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private float alpha = 1f;
    private Paint paint;

    /**
     * Set the ShapeHolder Paint
     *
     * @param value Paint to use for this ShapeHolder
     */
    public void setPaint(Paint value) {
        paint = value;
    }

    /**
     * Get the ShapeHolder Paint
     *
     * @return Paint used in this ShapeHolder
     */
    public Paint getPaint() {
        return paint;
    }

    /**
     * Set the x coordinate of this ShapeHolder
     *
     * @param value x coordinate to use
     */
    public void setX(float value) {
        x = value;
    }

    /**
     * Get the x coordinate of this ShapeHolder
     *
     * @return x coordinate of the ShapeHolder
     */
    public float getX() {
        return x;
    }

    /**
     * Set the y coordinate of this ShapeHolder
     *
     * @param value y coordinate to use
     */
    public void setY(float value) {
        y = value;
    }

    /**
     * Get the y coordinate of the ShapeHolder
     *
     * @return y coordinate of the ShapeHolder
     */
    public float getY() {
        return y;
    }

    /**
     * Set the ShapeDrawable of the ShapeHolder
     *
     * @param value ShapeDrawable to use
     */
    public void setShape(ShapeDrawable value) {
        shape = value;
    }

    /**
     * Get the ShapeDrawable of the ShapeHolder
     *
     * @return ShapeDrawable of the ShapeHolder
     */
    public ShapeDrawable getShape() {
        return shape;
    }

    /**
     * Get the color of the ShapeHolder
     *
     * @return color of the ShapeHolder
     */
    public int getColor() {
        return color;
    }

    /**
     * Set the color of the ShapeHolder's Shape's Paint
     *
     * @param value color to use
     */
    public void setColor(int value) {
        shape.getPaint().setColor(value);
        color = value;
    }

    /**
     * Set the RadialGradient gradient of the ShapeHolder
     *
     * @param value RadialGradient to use
     */
    public void setGradient(RadialGradient value) {
        gradient = value;
    }

    /**
     * Get the RadialGradient gradient of the ShapeHolder
     *
     * @return RadialGradient gradient of the ShapeHolder
     */
    public RadialGradient getGradient() {
        return gradient;
    }

    /**
     * Set the alpha value of the ShapeHolder and the Shape it contains
     *
     * @param alpha alpha value to use
     */
    public void setAlpha(float alpha) {
        this.alpha = alpha;
        shape.setAlpha((int)((alpha * 255f) + .5f));
    }

    /**
     * Get the width of the Shape contained in the ShapeHolder
     *
     * @return width of the Shape shape
     */
    public float getWidth() {
        return shape.getShape().getWidth();
    }

    /**
     * Resize the width of the Shape contained in the ShapeHolder
     *
     * @param width new width to use
     */
    public void setWidth(float width) {
        Shape s = shape.getShape();
        s.resize(width, s.getHeight());
    }

    /**
     * Get the height of the Shape contained in the ShapeHolder
     *
     * @return height of the Shape shape
     */
    public float getHeight() {
        return shape.getShape().getHeight();
    }

    /**
     * Set the height of the Shape contained in the ShapeHolder
     *
     * @param height new height to use
     */
    public void setHeight(float height) {
        Shape s = shape.getShape();
        s.resize(s.getWidth(), height);
    }

    /**
     * Constructor which initializes a ShapeHolder instance's Shape shape with a ShaperDrawable s
     *
     * @param s ShapeDrawable that the ShapeHolder will contain
     */
    public ShapeHolder(ShapeDrawable s) {
        shape = s;
    }
}
