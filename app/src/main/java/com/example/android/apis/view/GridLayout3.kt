/*
 * Copyright (C) 2011 The Android Open Source Project
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
package com.example.android.apis.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.graphics.Utilities.id2p
import com.example.android.apis.view.GridLayout3.Companion.create

/**
 * A form, showing use of the [GridLayout] API from java code. Here we demonstrate use of the
 * row/column order preserved property which allows rows and or columns to pass over each other
 * when needed. The two buttons in the bottom right corner need to be separated from the other
 * UI elements. This can either be done by separating rows or separating columns - but we don't
 * need to do both and may only have enough space to do one or the other.
 * RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
 */
@SuppressLint("SetTextI18n")
class GridLayout3 : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to the [GridLayout] built and configured by our
     * method [create].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = create(this)
        view.setPadding(0, id2p(160), 0, id2p(60))
        setContentView(view)
    }

    companion object {
        /**
         * Creates, builds, configures and returns a [GridLayout] to use for our UI. First we
         * initialize our [GridLayout] variable `val p` with a new instance, set it to use the
         * default margins, and set its alignment mode to ALIGN_BOUNDS. We initialize [Configuration]
         * variable `val configuration` with the current configuration that is in effect for our
         * [context]. If the `orientation` field of `configuration` is ORIENTATION_PORTRAIT we
         * configure `p` to be at liberty to place the horizontal column boundaries in whatever
         * order best fits its constraints. Otherwise we configure `p` to be at liberty to place
         * the vertical row boundaries in whatever order best fits its constraints.
         *
         * Next we define the grid indices and alignment for the rows of `Grid` cells. We define
         * [GridLayout.Spec] variables `val titleRow` to start in row 0, `val introRow` to start
         * row 1, `val emailRow` to start in row 2 with BASELINE alignment, `val passwordRow` to
         * start row 3 with BASELINE alignment, `val button1Row` to start row 5, and `val button2Row`
         * to start row 6.
         *
         * We define the grid indices, size, and alignment of the columns of cells as follows:
         * [GridLayout.Spec] variables `val centerInAllColumns` starts in column 0, with size 4 and
         * CENTER alignment, `val leftAlignInAllColumns` starts in column 0, with size 4 and LEFT
         * alignment, `val labelColumn` starts in column 0, with RIGHT alignment, `val fieldColumn`
         * starts in column 1, with LEFT alignment, `val defineLastColumn` starts in column 3,
         * `val fillLastColumn` starts in column 3 with FILL alignment (Indicates that a view
         * should expanded to fit the boundaries of its cell group).
         *
         * Now we create some views and use the above [GridLayout.Spec] objects to position them.
         * We create [TextView] `val c`, set its text size to 32, set its text to "Email setup" and
         * add it to `p` using a new instance of [GridLayout.LayoutParams] which uses `titleRow` as
         * the row spec, and `centerInAllColumns` as the column spec. We then create a new instance
         * for [TextView] `val c`, set its text size to 16, set its text to "You can configure email
         * in a few simple steps:", and add it to `p` using a new instance of [GridLayout.LayoutParams]
         * which uses `introRow` as the row spec, and `leftAlignInAllColumns` as the column spec.
         * We then create a new instance for [TextView] `val c`, set its text to "Email address:",
         * and add it to `p` using a new instance of [GridLayout.LayoutParams] which uses `emailRow`
         * as the row spec, and `labelColumn` as the column spec. We create a new instance for
         * [EditText] `val c`, set its size to 10 ems, set its input type to TYPE_CLASS_TEXT or'ed
         * with TYPE_TEXT_VARIATION_EMAIL_ADDRESS, and add it to `p` using a new instance of
         * [GridLayout.LayoutParams] which uses `emailRow` as the row spec, and `fieldColumn` as
         * the column spec. We create a new instance for [TextView] `val c`, set its text to
         * "Password:", and add it to `p` using a new instance of [GridLayout.LayoutParams] which
         * uses `passwordRow` as the row spec, and `labelColumn` as the column spec. We create a new
         * instance of [EditText] for [TextView] `val c`, set its size to 8 ems, set its input type
         * to TYPE_CLASS_TEXT or'ed with TYPE_TEXT_VARIATION_PASSWORD, and add it to `p` using a new
         * instance of [GridLayout.LayoutParams] which uses `passwordRow` as the row spec, and
         * `fieldColumn` as the column spec. We create a new instance for [Button] `val c`, set its
         * text to "Manual setup", and add it to `p` using a new instance of [GridLayout.LayoutParams]
         * which uses `button1Row` as the row spec, and `defineLastColumn` as the column spec. We
         * create a new instance for [Button] `val c`, set its text to "Next", and add it to `p`
         * using a new instance of [GridLayout.LayoutParams] which uses `button2Row` as the row spec,
         * and `fillLastColumn` as the column spec.
         *
         * Finally we return `p` to the caller.
         *
         * @param context [Context] to use to access resources, "this" when called from our
         * [onCreate] override.
         * @return a [GridLayout] containing our UI
         */
        fun create(context: Context): View {
            val p = GridLayout(context)
            p.useDefaultMargins = true
            p.alignmentMode = GridLayout.ALIGN_BOUNDS
            val configuration: Configuration = context.resources.configuration
            if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                p.isColumnOrderPreserved = false
            } else {
                p.isRowOrderPreserved = false
            }
            val titleRow = GridLayout.spec(/* start = */ 0)
            val introRow = GridLayout.spec(/* start = */ 1)
            val emailRow = GridLayout.spec(/* start = */ 2, /* alignment = */ GridLayout.BASELINE)
            val passwordRow =
                GridLayout.spec(/* start = */ 3, /* alignment = */ GridLayout.BASELINE)
            val button1Row = GridLayout.spec(/* start = */ 5)
            val button2Row = GridLayout.spec(/* start = */ 6)
            val centerInAllColumns = GridLayout.spec(
                /* start = */ 0,
                /* size = */ 4,
                /* alignment = */ GridLayout.CENTER
            )
            val leftAlignInAllColumns = GridLayout.spec(
                /* start = */ 0,
                /* size = */ 4,
                /* alignment = */ GridLayout.LEFT
            )
            val labelColumn = GridLayout.spec(/* start = */ 0, /* alignment = */ GridLayout.RIGHT)
            val fieldColumn = GridLayout.spec(/* start = */ 1, /* alignment = */ GridLayout.LEFT)
            val defineLastColumn = GridLayout.spec(/* start = */ 3)
            val fillLastColumn = GridLayout.spec(/* start = */ 3, /* alignment = */ GridLayout.FILL)
            var layoutParams: GridLayout.LayoutParams
            run {
                val c = TextView(context)
                c.textSize = 32f
                c.text = "Email setup"
                layoutParams = GridLayout.LayoutParams(
                    /* rowSpec = */ titleRow,
                    /* columnSpec = */ centerInAllColumns
                )
                p.addView(/* child = */ c, /* params = */ layoutParams)
            }
            run {
                val c = TextView(context)
                c.textSize = 16f
                c.text = "You can configure email in a few simple steps:"
                layoutParams = GridLayout.LayoutParams(
                    /* rowSpec = */ introRow,
                    /* columnSpec = */ leftAlignInAllColumns
                )
                p.addView(/* child = */ c, /* params = */ layoutParams)
            }
            run {
                val c = TextView(context)
                c.text = "Email address:"
                layoutParams = GridLayout.LayoutParams(
                    /* rowSpec = */ emailRow,
                    /* columnSpec = */ labelColumn
                )
                p.addView(/* child = */ c, /* params = */ layoutParams)
            }
            run {
                val c = EditText(context)
                c.setEms(10)
                c.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                layoutParams = GridLayout.LayoutParams(
                    /* rowSpec = */ emailRow,
                    /* columnSpec = */ fieldColumn
                )
                p.addView(/* child = */ c, /* params = */ layoutParams)
            }
            run {
                val c = TextView(context)
                c.text = "Password:"
                layoutParams = GridLayout.LayoutParams(
                    /* rowSpec = */ passwordRow,
                    /* columnSpec = */ labelColumn
                )
                p.addView(/* child = */ c, /* params = */ layoutParams)
            }
            run {
                val c: TextView = EditText(context)
                c.setEms(8)
                c.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                layoutParams = GridLayout.LayoutParams(
                    /* rowSpec = */ passwordRow,
                    /* columnSpec = */ fieldColumn
                )
                p.addView(/* child = */ c, /* params = */ layoutParams)
            }
            run {
                val c = Button(context)
                c.text = "Manual setup"
                layoutParams = GridLayout.LayoutParams(
                    /* rowSpec = */ button1Row,
                    /* columnSpec = */ defineLastColumn
                )
                p.addView(/* child = */ c, /* params = */ layoutParams)
            }
            run {
                val c = Button(context)
                c.text = "Next"
                layoutParams = GridLayout.LayoutParams(
                    /* rowSpec = */ button2Row,
                    /* columnSpec = */ fillLastColumn
                )
                p.addView(/* child = */ c, /* params = */ layoutParams)
            }
            return p
        }
    }
}