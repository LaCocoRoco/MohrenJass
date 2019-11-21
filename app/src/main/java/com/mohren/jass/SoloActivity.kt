package com.mohren.jass

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import java.util.ArrayList

class SoloActivity : Activity() {
    companion object {
        private const val REQUEST_POINTS = 1
        private const val LHS = 0
        private const val RHS = 1
    }

    private var inputMethodService: InputMethodManager? = null
    private var imageViewVisualization: ImageView? = null
    private var editTextLhs: EditText? = null
    private var editTextRhs: EditText? = null
    private var preference: SharedPreferences? = null
    private var preferencePointsLimit: Int = 0
    private var preferenceDesign: Boolean = false
    private var width: Int = 0
    private var height: Int = 0
    private var initialized: Boolean = false
    private var points: ArrayList<IntArray>? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_POINTS) {
                val bundle = data.extras as Bundle
                val pointsLHS = bundle.getInt(getString(R.string.extra_lhs))
                val pointsRHS = bundle.getInt(getString(R.string.extra_rhs))

                val p = IntArray(2)

                p[LHS] = points!![points!!.size - 1][LHS] + pointsLHS
                p[RHS] = points!![points!!.size - 1][RHS] + pointsRHS

                points!!.add(p)

                comparePoints()
                update()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solo)

        points = ArrayList()

        inputMethodService = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imageViewVisualization = findViewById(R.id.activity_solo_image_view_visualization)
        editTextLhs = findViewById(R.id.activity_solo_edit_text_lhs)
        editTextRhs = findViewById(R.id.activity_solo_edit_text_rhs)

        val buttonAddPointsLhs = findViewById<View>(R.id.activity_solo_button_add_points_lhs)
        val buttonAddPointsRhs = findViewById<View>(R.id.activity_solo_button_add_points_rhs)
        val relativeLayoutContainer = findViewById<RelativeLayout>(R.id.activity_solo_relative_layout_container)
        val buttonReverse = findViewById<View>(R.id.activity_solo_button_reverse)
        val buttonNewGame = findViewById<View>(R.id.activity_solo_button_new_game)

        preference = getSharedPreferences(getString(R.string.preference_name), 0)
        preferencePointsLimit = preference!!.getInt(getString(R.string.preference_solo_points_limit), 1000)
        preferenceDesign = preference!!.getBoolean(getString(R.string.preference_solo_design), false)

        editTextLhs!!.setText(preference!!.getString(getString(R.string.preference_solo_name_lhs), null))
        editTextRhs!!.setText(preference!!.getString(getString(R.string.preference_solo_name_rhs), null))

        val preferencePointsSize = preference!!.getInt(getString(R.string.preference_solo_points_size), 0)

        for (i in 0 until preferencePointsSize) {
            val p = IntArray(2)
            p[LHS] = preference!!.getInt(getString(R.string.preference_solo_points_lhs) + i, 0)
            p[RHS] = preference!!.getInt(getString(R.string.preference_solo_points_rhs) + i, 0)
            points!!.add(p)
        }

        if (points!!.isEmpty()) points!!.add(IntArray(2))

        imageViewVisualization!!.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                imageViewVisualization!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                width = imageViewVisualization!!.width
                height = imageViewVisualization!!.height
                initialized = true
                update()
            }
        })

        editTextLhs!!.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                editTextLhs!!.text = null
                inputMethodService!!.showSoftInput(editTextLhs, 0)
            }
        }

        editTextRhs!!.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                editTextRhs!!.text = null
                inputMethodService!!.showSoftInput(editTextRhs, 0)
            }
        }

        buttonAddPointsLhs.setOnClickListener {
            val intent = Intent(this, InputActivity::class.java)
            intent.putExtra(getString(R.string.extra_group), getString(R.string.extra_lhs))
            startActivityForResult(intent, REQUEST_POINTS)
            overridePendingTransition(R.animator.translate_left_in, R.animator.translate_stay_still)
        }

        buttonAddPointsRhs.setOnClickListener {
            val intent = Intent(this, InputActivity::class.java)
            intent.putExtra(getString(R.string.extra_group), getString(R.string.extra_rhs))
            startActivityForResult(intent, REQUEST_POINTS)
            overridePendingTransition(R.animator.translate_right_in, R.animator.translate_stay_still)
        }

        relativeLayoutContainer.setOnClickListener {
            clearFocus()
        }

        buttonReverse.setOnClickListener {
            removePoints()
            update()
        }

        buttonNewGame.setOnClickListener {
            editTextLhs!!.text = null
            editTextRhs!!.text = null
            clearPoints()
            update()
        }
    }

    private fun update() {
        imageViewVisualization!!.setImageDrawable(drawVisualization())
    }

    private fun clearFocus() {
        editTextLhs!!.clearFocus()
        editTextRhs!!.clearFocus()
        inputMethodService!!.hideSoftInputFromWindow(editTextLhs!!.windowToken, 0)
        inputMethodService!!.hideSoftInputFromWindow(editTextRhs!!.windowToken, 0)
    }

    private fun removePoints() {
        if (points!!.size > 1) points!!.removeAt(points!!.size - 1)
    }

    private fun clearPoints() {
        points!!.clear()
        points!!.add(IntArray(2))
    }

    private fun comparePoints() {
        val index = points!!.size - 1
        if (points!![index][LHS] >= preferencePointsLimit ||
                points!![index][RHS] >= preferencePointsLimit) {

            val winner: String = if (points!![index][LHS] > points!![index][RHS]) {
                if (editTextLhs!!.text.isNotEmpty()) editTextLhs!!.text.toString()
                else editTextLhs!!.hint.toString()
            } else {
                if (editTextRhs!!.text.isNotEmpty()) editTextRhs!!.text.toString()
                else editTextRhs!!.hint.toString()
            }

            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.activity_solo_dialog_title_end)
            builder.setMessage(winner + " " + getString(R.string.activity_solo_dialog_message_won))
            builder.setPositiveButton(R.string.activity_solo_dialog_button_close) { _, _ ->
                clearPoints()
                update()
            }
            builder.show()
        }
    }

    public override fun onResume() {
        super.onResume()

        if (initialized) update()
    }

    public override fun onPause() {
        super.onPause()

        val preferenceEditor = preference!!.edit()
        preferenceEditor.putString(getString(R.string.preference_solo_name_lhs), editTextLhs!!.text.toString())
        preferenceEditor.putString(getString(R.string.preference_solo_name_rhs), editTextRhs!!.text.toString())
        preferenceEditor.putInt(getString(R.string.preference_solo_points_size), points!!.size)

        for (i in points!!.indices) {
            preferenceEditor.putInt(getString(R.string.preference_solo_points_lhs) + i, points!![i][LHS])
            preferenceEditor.putInt(getString(R.string.preference_solo_points_rhs) + i, points!![i][RHS])
        }

        preferenceEditor.apply()
    }

    private fun drawVisualization(): Drawable {
        val wF = width .toFloat()/ 100
        val hF = height.toFloat() / 100

        val n = if (wF > hF) hF else wF

        val lhs = points!![points!!.size - 1][LHS].toFloat()
        val rhs = points!![points!!.size - 1][RHS].toFloat()

        val pointLimitText = getString(R.string.activity_solo_points_limit)
        val pointLimit = preferencePointsLimit.toFloat()

        val lhsHigh = if (preferenceDesign) {
            15.0f + 0.7f * (lhs / (pointLimit / 100.0f))
        } else {
            85.0f - 0.7f * (lhs / (pointLimit / 100.0f))
        }

        val rhsHigh = if (preferenceDesign) {
            15.0f + 0.7f * (rhs / (pointLimit / 100.0f))
        }  else {
            85.0f - 0.7f * (rhs / (pointLimit / 100.0f))
        }

        val lhsText = if (preferenceDesign) {
            (pointLimit - lhs).toInt().toString()
        } else {
            lhs.toInt().toString()
        }

        val rhsText = if (preferenceDesign) {
            (pointLimit - rhs).toInt().toString()
        } else {
            rhs.toInt().toString()
        }

        // paint text center
        val ptc = Paint()
        ptc.color = -1
        ptc.isAntiAlias = true
        ptc.style = Paint.Style.FILL_AND_STROKE
        ptc.textSize = 8.0f * n
        ptc.textAlign = Paint.Align.CENTER

        // paint line
        val pl = Paint()
        pl.color = -1
        pl.strokeWidth = 10.0f * n
        pl.isAntiAlias = true
        pl.style = Paint.Style.FILL_AND_STROKE

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas()
        canvas.setBitmap(bitmap)
        canvas.drawText(pointLimitText + preferencePointsLimit, 50.0f * wF, 10.0f * hF, ptc)
        canvas.drawText(lhsText, 30.0f * wF, 95.0f * hF, ptc)
        canvas.drawText(rhsText, 70.0f * wF, 95.0f * hF, ptc)
        canvas.drawLine(30.0f * wF, 85.0f * hF, 30.0f * wF, hF * lhsHigh, pl)
        canvas.drawLine(70.0f * wF, 85.0f * hF, 70.0f * wF, hF * rhsHigh, pl)

        return BitmapDrawable(resources, bitmap)
    }
}