package com.mohren.jass

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.view.KeyEvent
import android.view.KeyEvent.*
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class InputActivity : Activity() {
    companion object {
        private const val POINTS_MIN = 0
        private const val POINTS_MAX = 157
        private const val POINTS_ABSOLUTE = 257
    }

    private var group: String? = null
    private var editTextEvaluatePlayedPoints: EditText? = null
    private var inputMethodService: InputMethodManager? = null

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KEYCODE_BACK) {
            setResult(RESULT_CANCELED, Intent())
            translate()
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        val bundle = intent.extras as Bundle
        group = bundle.getString(getString(R.string.extra_group))

        inputMethodService = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        editTextEvaluatePlayedPoints = findViewById(R.id.activity_input_edit_text_evaluate_played_points)

        val textViewPointsEnemyValue = findViewById<TextView>(R.id.activity_input_text_view_points_enemy_value)
        val buttonEvaluatePoints = findViewById<Button>(R.id.activity_input_button_evaluate_played_points)

        editTextEvaluatePlayedPoints!!.filters = arrayOf(InputFilter { source, start, _, dest, _, _ ->
            if (source.toString().isEmpty() || !Character.isDigit(source[start])) {
                ""
            } else {
                val points = Integer.parseInt(dest.toString() + source)

                if (points > POINTS_MAX && points != POINTS_ABSOLUTE) {
                    textViewPointsEnemyValue!!.text = POINTS_MIN.toString()
                    editTextEvaluatePlayedPoints!!.setText(POINTS_ABSOLUTE.toString())
                    editTextEvaluatePlayedPoints!!.setSelection(editTextEvaluatePlayedPoints!!.text.length)
                }

                if (points < POINTS_MAX) {
                    textViewPointsEnemyValue!!.text = (POINTS_MAX - points).toString()
                }
                null
            }
        })

        editTextEvaluatePlayedPoints!!.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                addPlayedPoints()
                clearFocusEditTextPlayedPoints()
                true
            } else {
                false
            }
        }

        editTextEvaluatePlayedPoints!!.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                editTextEvaluatePlayedPoints!!.text = null
                textViewPointsEnemyValue!!.text = null
                inputMethodService!!.showSoftInput(editTextEvaluatePlayedPoints, 0)
            }
        }

        buttonEvaluatePoints.setOnClickListener {
            addPlayedPoints()
            clearFocusEditTextPlayedPoints()
        }
    }

    fun onClickActivityInputButtonRemitPoints(view: View) {
        val buttonRemitPoints = view as Button
        val points = buttonRemitPoints.text.toString().toInt()
        setPoints(points, 0)
    }

    private fun clearFocusEditTextPlayedPoints() {
        editTextEvaluatePlayedPoints!!.clearFocus()
        inputMethodService!!.hideSoftInputFromWindow(editTextEvaluatePlayedPoints!!.windowToken, 0)
    }

    private fun addPlayedPoints() {
        if (editTextEvaluatePlayedPoints!!.text.toString().isEmpty()) return
        val pointsWon = Integer.valueOf(editTextEvaluatePlayedPoints!!.text.toString())
        val pointsLos = if (pointsWon < 257) 157 - pointsWon else 0
        setPoints(pointsWon, pointsLos)
    }

    private fun translate() {
        finish()

        if (group == getString(R.string.extra_lhs)) {
            overridePendingTransition(R.animator.translate_stay_still, R.animator.translate_left_out)
        }

        if (group == getString(R.string.extra_rhs)) {
            overridePendingTransition(R.animator.translate_stay_still, R.animator.translate_right_out)
        }
    }

    private fun setPoints(pointsWon: Int, pointsLos: Int) {
        val intent = Intent()

        if (group == getString(R.string.extra_lhs)) {
            intent.putExtra(getString(R.string.extra_lhs), pointsWon)
            intent.putExtra(getString(R.string.extra_rhs), pointsLos)
        }

        if (group == getString(R.string.extra_rhs)) {
            intent.putExtra(getString(R.string.extra_rhs), pointsWon)
            intent.putExtra(getString(R.string.extra_lhs), pointsLos)
        }

        setResult(RESULT_OK, intent)
        translate()
    }
}
