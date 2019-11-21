package com.mohren.jass

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputFilter
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Switch
import android.widget.ToggleButton

class SettingsActivity : Activity() {
    companion object {
        private const val POINTS_LIMIT_MAX_DUO = 2500
        private const val POINTS_LIMIT_MAX_SOLO = 2500
    }

    private var preferenceDuoPointsLimit: Int = 0
    private var preferenceDuoPointsTotal: Boolean = false
    private var preferenceDuoDesign: Boolean = false
    private var preferenceSoloPointsLimit: Int = 0
    private var preferenceSoloDesign: Boolean = false
    private var preference: SharedPreferences? = null
    private var inputMethodService: InputMethodManager? = null
    private var editTextDuoPointsLimit: EditText? = null
    private var editTextSoloPointsLimit: EditText? = null


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            assumeSoloTotalPoints()
            assumeDuoTotalPoints()
            finish()
            return true
        }

        return super.onKeyDown(keyCode, event)
    }


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        inputMethodService = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        editTextDuoPointsLimit = findViewById(R.id.activity_settings_edit_text_duo_points_limit)
        editTextSoloPointsLimit = findViewById(R.id.activity_settings_edit_text_solo_points_limit)

        val toggleButtonDuoNormal = findViewById<ToggleButton>(R.id.activity_settings_toggle_button_duo_normal)
        val toggleButtonDuoStatistic = findViewById<ToggleButton>(R.id.activity_settings_toggle_button_duo_statistic)
        val switchDuoTotalPoints = findViewById<Switch>(R.id.activity_settings_switch_duo_total_points)
        val toggleButtonSoloUp= findViewById<ToggleButton>(R.id.activity_settings_toggle_button_solo_up)
        val toggleButtonSoloDown= findViewById<ToggleButton>(R.id.activity_settings_toggle_button_solo_down)
        val relativeLayoutContainer = findViewById<RelativeLayout>(R.id.activity_settings_relative_layout_container)

        preference = getSharedPreferences(getString(R.string.preference_name), 0)
        preferenceDuoPointsLimit = preference!!.getInt(getString(R.string.preference_duo_points_limit), 1000)
        preferenceDuoPointsTotal = preference!!.getBoolean(getString(R.string.preference_duo_points_total), true)
        preferenceDuoDesign = preference!!.getBoolean(getString(R.string.preference_duo_design), false)
        preferenceSoloPointsLimit = preference!!.getInt(getString(R.string.preference_solo_points_limit), 1000)
        preferenceSoloDesign = preference!!.getBoolean(getString(R.string.preference_solo_design), false)

        toggleButtonDuoNormal.isChecked = !preferenceDuoDesign
        toggleButtonDuoStatistic.isChecked = preferenceDuoDesign
        switchDuoTotalPoints.isChecked = preferenceDuoPointsTotal
        editTextDuoPointsLimit!!.hint = preferenceDuoPointsLimit.toString()
        toggleButtonSoloUp.isChecked = !preferenceSoloDesign
        toggleButtonSoloDown.isChecked = preferenceSoloDesign
        editTextSoloPointsLimit!!.hint = preferenceSoloPointsLimit.toString()

        editTextDuoPointsLimit!!.filters = arrayOf(InputFilter { source, start, _, dest, _, _ ->
            if (source.toString().isEmpty() || !Character.isDigit(source[start])) {
                ""
            } else {
                val value = Integer.parseInt(dest.toString() + source)

                if (value > POINTS_LIMIT_MAX_DUO) {
                    editTextDuoPointsLimit!!.setText(POINTS_LIMIT_MAX_DUO.toString())
                    editTextDuoPointsLimit!!.setSelection(editTextDuoPointsLimit!!.text.length)
                }
                
                null
            }
        })

        editTextDuoPointsLimit!!.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                clearFocusEditTextDuoPointsLimit()
                assumeDuoTotalPoints()
                true
            } else {
                false
            }
        }

        editTextDuoPointsLimit!!.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                editTextDuoPointsLimit!!.text = null
                inputMethodService!!.showSoftInput(editTextDuoPointsLimit, 0)
            } else {
                clearFocusEditTextDuoPointsLimit()
                assumeDuoTotalPoints()
            }
        }

        editTextSoloPointsLimit!!.filters = arrayOf(InputFilter { source, start, _, dest, _, _ ->
            if (source.toString().isEmpty() || !Character.isDigit(source[start])) {
                ""
            } else {
                val value = Integer.parseInt(dest.toString() + source)

                if (value > POINTS_LIMIT_MAX_SOLO) {
                    editTextSoloPointsLimit!!.setText(POINTS_LIMIT_MAX_SOLO.toString())
                    editTextSoloPointsLimit!!.setSelection(editTextSoloPointsLimit!!.text.length)
                }

                null
            }
        })

        editTextSoloPointsLimit!!.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                clearFocusEditTextSoloPointsLimit()
                assumeSoloTotalPoints()
                true
            } else {
                false
            }
        }

        editTextSoloPointsLimit!!.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                editTextDuoPointsLimit!!.text = null
                inputMethodService!!.showSoftInput(editTextDuoPointsLimit, 0)
            } else {
                clearFocusEditTextSoloPointsLimit()
                assumeSoloTotalPoints()
            }
        }

        toggleButtonDuoNormal.setOnClickListener {
            preferenceDuoDesign = false
            toggleButtonDuoNormal.isChecked = true
            toggleButtonDuoStatistic.isChecked = false
        }

        toggleButtonDuoStatistic.setOnClickListener {
            preferenceDuoDesign = true
            toggleButtonDuoNormal.isChecked = false
            toggleButtonDuoStatistic.isChecked = true
        }

        switchDuoTotalPoints.setOnClickListener {
            preferenceDuoPointsTotal = !preferenceDuoPointsTotal
        }

        toggleButtonSoloUp.setOnClickListener {
            preferenceSoloDesign = false
            toggleButtonSoloUp.isChecked = true
            toggleButtonSoloDown.isChecked = false
        }

        toggleButtonSoloDown.setOnClickListener {
            preferenceSoloDesign = true
            toggleButtonSoloUp.isChecked = false
            toggleButtonSoloDown.isChecked = true
        }

        relativeLayoutContainer.setOnClickListener {
            clearFocusEditTextDuoPointsLimit()
            clearFocusEditTextSoloPointsLimit()
        }

    }

    public override fun onPause() {
        super.onPause()

        val preferenceEditor = preference!!.edit()
        preferenceEditor.putInt(getString(R.string.preference_duo_points_limit), preferenceDuoPointsLimit)
        preferenceEditor.putBoolean(getString(R.string.preference_duo_points_total), preferenceDuoPointsTotal)
        preferenceEditor.putBoolean(getString(R.string.preference_duo_design), preferenceDuoDesign)
        preferenceEditor.putInt(getString(R.string.preference_solo_points_limit), preferenceSoloPointsLimit)
        preferenceEditor.putBoolean(getString(R.string.preference_solo_design), preferenceSoloDesign)
        preferenceEditor.apply()
    }

    private fun clearFocusEditTextDuoPointsLimit() {
        editTextDuoPointsLimit!!.clearFocus()
        inputMethodService!!.hideSoftInputFromWindow(editTextDuoPointsLimit!!.windowToken, 0)
    }

    private fun clearFocusEditTextSoloPointsLimit() {
        editTextSoloPointsLimit!!.clearFocus()
        inputMethodService!!.hideSoftInputFromWindow(editTextSoloPointsLimit!!.windowToken, 0)
    }

    private fun assumeDuoTotalPoints() {
        if (editTextDuoPointsLimit!!.text.isNotEmpty()) {
            preferenceDuoPointsLimit = editTextDuoPointsLimit!!.text.toString().toInt()
        }

        editTextDuoPointsLimit!!.hint = preferenceDuoPointsLimit.toString()
        editTextDuoPointsLimit!!.text = null
    }

    private fun assumeSoloTotalPoints() {
        if (editTextSoloPointsLimit!!.text.isNotEmpty()) {
            preferenceSoloPointsLimit = editTextSoloPointsLimit!!.text.toString().toInt()
        }

        editTextSoloPointsLimit!!.hint = preferenceSoloPointsLimit.toString()
        editTextSoloPointsLimit!!.text = null
    }
}
