package com.example.drawingapp

import android.app.Dialog
import android.os.Bundle
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var ibBrush: ImageButton
    private lateinit var drawingView: DrawingVew
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        ibBrush = findViewById(R.id.ib_brush)

        drawingView = findViewById(R.id.drawing_view)
        drawingView.changeBrushSize(20.toFloat())
        ibBrush.setOnClickListener {
            showBrushSizeChooserDialog()
        }

    }

    private fun showBrushSizeChooserDialog() {
        val brushDialog = Dialog(this@MainActivity)
        brushDialog.setContentView(R.layout.dialog_brush)
        val seekBarProcess = brushDialog.findViewById<SeekBar>(R.id.dialog_seek_bar)
        val showProgressTV = brushDialog.findViewById<TextView>(R.id.dialog_text_view_progress)
        seekBarProcess.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekbar: SeekBar?, p1: Int, p2: Boolean) {
                drawingView.changeBrushSize(seekbar!!.progress.toFloat())
                showProgressTV.text = seekBarProcess.progress.toString()
            }


            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }


        })

        brushDialog.show()

    }
}
