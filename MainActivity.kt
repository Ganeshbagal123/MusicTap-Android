package com.example.musictap

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var xC: EditText
    private lateinit var xE: EditText
    private lateinit var xG: EditText
    private lateinit var y: EditText
    private lateinit var noteDuration: EditText
    private lateinit var gap: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 28, 28, 28)
        }

        fun label(text: String) {
            page.addView(TextView(this).apply {
                this.text = text
                textSize = 15f
                setPadding(0, 12, 0, 4)
            })
        }

        fun field(hint: String, value: String): EditText {
            return EditText(this).apply {
                this.hint = hint
                setSingleLine(true)
                setText(value)
                textSize = 16f
                page.addView(this, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        }

        page.addView(TextView(this).apply {
            text = "MusicTap – Touch prototype"
            textSize = 24f
            gravity = Gravity.CENTER
        })

        status = TextView(this).apply {
            text = "Accessibility service: checking…"
            textSize = 14f
            setPadding(0, 16, 0, 8)
        }
        page.addView(status)

        page.addView(Button(this).apply {
            text = "1. Enable MusicTap accessibility"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        })

        label("Enter the screen X coordinate for each piano key (pixels)")
        xC = field("C4 X coordinate", "250")
        xE = field("E4 X coordinate", "400")
        xG = field("G4 X coordinate", "550")
        label("Shared screen Y coordinate (centre of piano keys)")
        y = field("Y coordinate", "1500")
        label("Touch duration in milliseconds")
        noteDuration = field("Duration", "120")
        label("Gap between notes in milliseconds")
        gap = field("Gap", "350")

        page.addView(Button(this).apply {
            text = "2. Test one C4 tap"
            setOnClickListener {
                val xx = xC.intValue()
                val yy = y.intValue()
                if (xx == null || yy == null) {
                    toast("Enter valid X and Y coordinates")
                } else if (!PianoAccessibilityService.tap(xx, yy, duration())) {
                    toast("Enable MusicTap accessibility first")
                } else {
                    toast("Tap gesture sent")
                }
            }
        })

        page.addView(Button(this).apply {
            text = "3. Play C4 – E4 – G4 – C4"
            setOnClickListener { playTestSequence() }
        })

        page.addView(Button(this).apply {
            text = "STOP"
            setOnClickListener { PianoAccessibilityService.stopSequence() }
        })

        page.addView(TextView(this).apply {
            text = "\nTip: Open Perfect Piano, keep its piano keyboard visible, then return here and enter the centre coordinates of the keys. Coordinates are device screen pixels."
            textSize = 13f
        })

        setContentView(page)
    }

    override fun onResume() {
        super.onResume()
        status.text = if (PianoAccessibilityService.isEnabled) {
            "Accessibility service: ENABLED"
        } else {
            "Accessibility service: NOT ENABLED"
        }
    }

    
private fun playTestSequence() {
    val yy = readInt(y)
    val c = readInt(xC)
    val e = readInt(xE)
    val g = readInt(xG)

    if (yy == null || c == null || e == null || g == null) {
        toast("Enter valid coordinates first")
        return
    }

    val notes = listOf(
        PianoAccessibilityService.Note(c, yy),
        PianoAccessibilityService.Note(e, yy),
        PianoAccessibilityService.Note(g, yy),
        PianoAccessibilityService.Note(c, yy)
    )

    val started = PianoAccessibilityService.play(
        notes,
        duration(),
        (readInt(gap) ?: 350).coerceIn(0, 2000)
    )

    if (!started) toast("Enable MusicTap accessibility first")
}


    private fun duration(): Long = (noteDuration.intValue() ?: 120).coerceIn(40, 1000).toLong()

    private fun EditText.intValue(): Int? = text.toString().trim().toIntOrNull()

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
