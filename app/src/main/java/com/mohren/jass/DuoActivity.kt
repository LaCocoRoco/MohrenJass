package com.mohren.jass

import android.app.Activity
import android.app.AlertDialog.Builder
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
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
import kotlin.math.roundToInt

class DuoActivity : Activity() {
    companion object {
        private const val LHS = 0
        private const val RHS = 1
        private const val HUNDREDS = 0
        private const val FIFTIES = 1
        private const val TWENTIES = 2
        private const val ODD = 3
        private const val ALL = 4
        private const val MAX_SIZE_REG = 15
        private const val REQUEST_POINTS = 1
    }

    private var inputMethodService: InputMethodManager? = null
    private var imageViewVisualization: ImageView? = null
    private var editTextLhs: EditText? = null
    private var editTextRhs: EditText? = null
    private var preference: SharedPreferences? = null
    private var preferencePointsLimit: Int = 0
    private var preferenceDesign: Boolean = false
    private var preferencePointsTotal: Boolean = false
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

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_duo)

        points = ArrayList()

        inputMethodService = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imageViewVisualization = findViewById(R.id.activity_duo_image_view_visualization)
        editTextLhs = findViewById(R.id.activity_duo_edit_text_lhs)
        editTextRhs = findViewById(R.id.activity_duo_edit_text_rhs)

        val buttonAddPointsLhs = findViewById<View>(R.id.activity_duo_button_add_points_lhs)
        val buttonAddPointsRhs = findViewById<View>(R.id.activity_duo_button_add_points_rhs)
        val relativeLayoutContainer = findViewById<RelativeLayout>(R.id.activity_duo_relative_layout_container)
        val buttonReverse = findViewById<View>(R.id.activity_duo_button_reverse)
        val buttonNewGame = findViewById<View>(R.id.activity_duo_button_new_game)

        preference = getSharedPreferences(getString(R.string.preference_name), 0)
        preferencePointsLimit = preference!!.getInt(getString(R.string.preference_duo_points_limit), 1000)
        preferencePointsTotal = preference!!.getBoolean(getString(R.string.preference_duo_points_total), true)
        preferenceDesign = preference!!.getBoolean(getString(R.string.preference_duo_design), false)

        editTextLhs!!.setText(preference!!.getString(getString(R.string.preference_duo_name_lhs), null))
        editTextRhs!!.setText(preference!!.getString(getString(R.string.preference_duo_name_rhs), null))

        val preferencePointsSize = preference!!.getInt(getString(R.string.preference_duo_points_size), 0)

        for (i in 0 until preferencePointsSize) {
            val p = IntArray(2)
            p[LHS] = preference!!.getInt(getString(R.string.preference_duo_points_lhs) + i, 0)
            p[RHS] = preference!!.getInt(getString(R.string.preference_duo_points_rhs) + i, 0)
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

            val builder = Builder(this)
            builder.setTitle(R.string.activity_duo_dialog_title_end)
            builder.setMessage(winner + " " + getString(R.string.activity_duo_dialog_message_won))
            builder.setPositiveButton(R.string.activity_duo_dialog_button_close) { _, _ ->
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
        preferenceEditor.putString(getString(R.string.preference_duo_name_lhs), editTextLhs!!.text.toString())
        preferenceEditor.putString(getString(R.string.preference_duo_name_rhs), editTextRhs!!.text.toString())
        preferenceEditor.putInt(getString(R.string.preference_duo_points_size), points!!.size)

        for (i in points!!.indices) {
            preferenceEditor.putInt(getString(R.string.preference_duo_points_lhs) + i, points!![i][LHS])
            preferenceEditor.putInt(getString(R.string.preference_duo_points_rhs) + i, points!![i][RHS])
        }

        preferenceEditor.apply()
    }

    private fun drawVisualization(): Drawable {
        val wF = width.toFloat() / 100
        val hF = height.toFloat() / 100

        val n = if (wF > hF) hF else wF

        val p = Array(2) { IntArray(5) }

        p[LHS][ALL] = points!![points!!.size - 1][LHS]
        p[RHS][ALL] = points!![points!!.size - 1][RHS]

        // split points into numerical registers
        for (i in 0..1) {
            p[i][ODD] = p[i][ALL] % 850
            val hundreds = p[i][ALL] / 850 * 5 + p[i][ODD] / 100
            p[i][HUNDREDS] = if (hundreds > MAX_SIZE_REG) MAX_SIZE_REG else hundreds
            p[i][ODD] = p[i][ODD] - (p[i][HUNDREDS] - p[i][ALL] / 850 * 5) * 100
            val fifties = p[i][ALL] / 850 * 5 + p[i][ODD] / 50
            p[i][FIFTIES] = if (fifties > MAX_SIZE_REG) MAX_SIZE_REG else fifties
            p[i][ODD] = p[i][ODD] - (p[i][FIFTIES] - p[i][ALL] / 850 * 5) * 50
            val twenties = p[i][ALL] / 850 * 5 + p[i][ODD] / 20
            p[i][TWENTIES] = if (twenties > MAX_SIZE_REG) MAX_SIZE_REG else twenties
            p[i][ODD] = p[i][ODD] - (p[i][TWENTIES] - p[i][ALL] / 850 * 5) * 20
        }

        // generate line point map
        val list = ArrayList<Int>()
        for (i in 0..1) {
            val ix = if (i == 0) 21 else 51
            for (ii in 0..2) {
                val iy = if (ii == 0) 5 else if (ii == 1) 40 else 75
                for ((z, iii) in (0 until p[i][ii]).withIndex()) {
                    if (iii == 4 || iii == 9 || iii == 14) {
                        list.add(wF.roundToInt() * (ix - 8 + iii + z))
                        list.add(hF.roundToInt() * iy)
                        list.add(wF.roundToInt() * (iii + z + ix))
                        list.add(hF.roundToInt() * (iy + 10))
                    } else {
                        list.add(wF.roundToInt() * (ix + 1 + iii + z))
                        list.add(hF.roundToInt() * iy)
                        list.add(wF.roundToInt() * (ix + 1 + iii + z))
                        list.add(hF.roundToInt() * (iy + 10))
                    }
                }
            }
        }

        val lineMap = FloatArray(list.size)

        for (f in lineMap.indices) lineMap[f] = list[f].toFloat()

        // paint text left
        val ptl = Paint()
        ptl.color = -1
        ptl.isAntiAlias = true
        ptl.style = Paint.Style.FILL_AND_STROKE
        ptl.textSize = 8.0f * n
        ptl.textAlign = Paint.Align.LEFT

        // paint text right
        val ptr = Paint()
        ptr.color = -1
        ptr.isAntiAlias = true
        ptr.style = Paint.Style.FILL_AND_STROKE
        ptr.textSize = 8.0f * n
        ptr.textAlign = Paint.Align.RIGHT

        // paint line
        val pl = Paint()
        pl.color = -1
        pl.strokeWidth = 1.0f * n
        pl.isAntiAlias = true
        pl.style = Paint.Style.FILL_AND_STROKE

        // paint arc
        val pa = Paint()
        pa.color = -1
        pa.strokeWidth = 1.0f * n
        pa.isAntiAlias = true
        pa.style = Paint.Style.STROKE

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas()
        canvas.setBitmap(bitmap)

        if (preferenceDesign) {
            // draw static design
            canvas.drawLine(50.0f * wF, 00.0f * hF, 50.0f * wF, 100.0f * hF, pl)
            canvas.drawLine(25.0f * wF, 10.0f * hF, 75.0f * wF, 010.0f * hF, pl)
            canvas.drawLine(25.0f * wF, 45.0f * hF, 75.0f * wF, 045.0f * hF, pl)
            canvas.drawLine(25.0f * wF, 80.0f * hF, 75.0f * wF, 080.0f * hF, pl)

            canvas.drawText((p[LHS][HUNDREDS] * 100).toString(), 05.0f * wF, 10.0f * hF, ptl)
            canvas.drawText((p[LHS][TWENTIES] * 50).toString(), 05.0f * wF, 45.0f * hF, ptl)
            canvas.drawText((p[LHS][FIFTIES] * 20).toString(), 05.0f * wF, 80.0f * hF, ptl)
            canvas.drawText((p[RHS][HUNDREDS] * 100).toString(), 95.0f * wF, 10.0f * hF, ptr)
            canvas.drawText((p[RHS][TWENTIES] * 50).toString(), 95.0f * wF, 45.0f * hF, ptr)
            canvas.drawText((p[RHS][FIFTIES] * 20).toString(), 95.0f * wF, 80.0f * hF, ptr)
        } else {
            // draw normal design
            val recTop = RectF(05.0f * wF, 10.0f * hF, 35.0f * wF, 45.0f * hF)
            val recBot = RectF(65.0f * wF, 45.0f * hF, 95.0f * wF, 80.0f * hF)

            canvas.drawRGB(0, 110, 59)

            canvas.drawLine(50.0f * wF, 00.0f * hF, 50.0f * wF, 100.0f * hF, pl)
            canvas.drawLine(20.0f * wF, 10.0f * hF, 85.0f * wF, 010.0f * hF, pl)
            canvas.drawLine(20.0f * wF, 45.0f * hF, 80.0f * wF, 045.0f * hF, pl)
            canvas.drawLine(15.0f * wF, 80.0f * hF, 80.0f * wF, 080.0f * hF, pl)

            canvas.drawArc(recBot, 270.0f, 180.0f, false, pa)
            canvas.drawArc(recTop, 090.0f, 180.0f, false, pa)

            canvas.drawLines(lineMap, pl)
        }

        if (preferencePointsTotal) {
            canvas.drawText(p[LHS][ALL].toString(), 45.0f * wF, 98.0f * hF, ptr)
            canvas.drawText(p[RHS][ALL].toString(), 55.0f * wF, 98.0f * hF, ptl)
        }

        canvas.drawText(p[LHS][ODD].toString(), 05.0f * wF, 98.0f * hF, ptl)
        canvas.drawText(p[RHS][ODD].toString(), 95.0f * wF, 98.0f * hF, ptr)

        return BitmapDrawable(resources, bitmap)
    }
}
