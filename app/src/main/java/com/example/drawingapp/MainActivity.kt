package com.example.drawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var drawingView: DrawingView
    private lateinit var galleryImage: ImageView

    // 1. Gallery Launcher
    private val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.data?.let { uri ->
                galleryImage.setImageURI(uri)
            }
        }

    // 2. Updated Permission Launcher (Fixes the "Try Again" bug)
    private val requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val readGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
            val readMediaGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions[Manifest.permission.READ_MEDIA_IMAGES] ?: false
            } else false

            val writeGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false

            if (readGranted || readMediaGranted) {
                openGallery() // Auto-open after allow
            }

            if (writeGranted) {
                initiateSave() // Auto-save after allow
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        drawingView = findViewById(R.id.drawing_view)
        galleryImage = findViewById(R.id.gallery_image)

        // Setup click listeners for all buttons
        val buttonIds = listOf(
            R.id.perpul_button, R.id.red_botton, R.id.green_botton,
            R.id.blue_botton, R.id.orange_botton, R.id.ib_brush,
            R.id.ib_undo, R.id.ib_color_picker, R.id.ib_gallary, R.id.ib_save
        )
        buttonIds.forEach { id -> findViewById<View>(id)?.setOnClickListener(this) }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.perpul_button -> drawingView.setColor("#7A277B")
            R.id.red_botton -> drawingView.setColor("#C62101")
            R.id.green_botton -> drawingView.setColor("#96A900")
            R.id.blue_botton -> drawingView.setColor("#34B5E6")
            R.id.orange_botton -> drawingView.setColor("#EE7722")
            R.id.ib_undo -> drawingView.undoPath()
            R.id.ib_color_picker -> showColorPickerDialog()
            R.id.ib_brush -> showBrushSizeChooserDialog()
            R.id.ib_gallary -> handleGalleryRequest()
            R.id.ib_save -> handleSaveRequest()
        }
    }

    private fun handleGalleryRequest() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            requestPermission.launch(arrayOf(permission))
        }
    }

    private fun openGallery() {
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        openGalleryLauncher.launch(pickIntent)
    }

    private fun handleSaveRequest() {
        // Android 10+ (API 29) doesn't strictly need WRITE_EXTERNAL_STORAGE for Pictures folder,
        // but for compatibility we check it here.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            initiateSave()
        } else {
            requestPermission.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
        }
    }

    private fun initiateSave() {
        val layout = findViewById<ConstraintLayout>(R.id.canvas_frame)
        if (layout != null) {
            val bitmap = getBitmapFromView(layout)
            CoroutineScope(IO).launch { saveImage(bitmap) }
        } else {
            Toast.makeText(this, "Canvas frame not found!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private suspend fun saveImage(mBitmap: Bitmap) {
        withContext(IO) {
            try {
                val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
                val myDir = File("$root/DrawingApp")
                if (!myDir.exists()) myDir.mkdirs()

                val file = File(myDir, "Drawing_${System.currentTimeMillis()}.jpg")
                val out = FileOutputStream(file)
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
                out.close()

                // Refresh Gallery so the image shows up
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                mediaScanIntent.data = Uri.fromFile(file)
                sendBroadcast(mediaScanIntent)

                withContext(Main) {
                    Toast.makeText(this@MainActivity, "Saved to Gallery!", Toast.LENGTH_SHORT).show()
                    drawingView.clearCanvas() // Clear drawing
                    galleryImage.setImageURI(null) // Clear background image
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Main) { Toast.makeText(this@MainActivity, "Save Failed", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    // --- Dialogs ---

    private fun showBrushSizeChooserDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush)
        val seekBar = brushDialog.findViewById<SeekBar>(R.id.dialog_seek_bar)
        val progressText = brushDialog.findViewById<TextView>(R.id.dialog_text_view_progress)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, b: Boolean) {
                drawingView.changeBrushSize(p.toFloat())
                progressText.text = p.toString()
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        brushDialog.show()
    }

    private fun showColorPickerDialog() {
        AmbilWarnaDialog(this, Color.GREEN, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(d: AmbilWarnaDialog?) {}
            override fun onOk(d: AmbilWarnaDialog?, color: Int) {
                drawingView.setColor(color)
            }
        }).show()
    }
}