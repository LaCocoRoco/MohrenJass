package com.mohren.jass

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View

class MainActivity : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonDuo = findViewById<View>(R.id.activity_main_button_duo)
        val buttonSolo = findViewById<View>(R.id.activity_main_button_solo)
        val buttonSettings = findViewById<View>(R.id.activity_main_button_settings)

        buttonDuo.setOnClickListener {
            startActivity(Intent(this, DuoActivity::class.java))
        }

        buttonSolo.setOnClickListener {
            startActivity(Intent(this, SoloActivity::class.java))
        }

        buttonSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}