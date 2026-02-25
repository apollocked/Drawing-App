package com.example.drawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var ibBrush: ImageButton
    private lateinit var drawingView: DrawingView
    private lateinit var undoButton: ImageButton

    private lateinit var ibSave: ImageButton
    private lateinit var ibGallary: ImageButton
    private lateinit var ibColorPicker: ImageButton
    private lateinit var ibEraser: ImageButton

    private lateinit var perpulButton: ImageButton
    private lateinit var redButton: ImageButton
    private lateinit var greenButton: ImageButton
    private lateinit var blueButton: ImageButton
    private lateinit var orangeButton: ImageButton
    private lateinit var saveButton: ImageButton


    private val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            findViewById<ImageView>(
                R.id.gallery_image
            ).setImageURI(result.data?.data)
        }


    val requestPermission: ActivityResultLauncher<Array<String>> = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            val permissionName = it.key
            val isGranted = it.value
            if (isGranted && permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {

                Toast.makeText(this, "$permissionName Granted", Toast.LENGTH_SHORT).show()
                val pickIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                openGalleryLauncher.launch(pickIntent)

            } else if(isGranted && (permissionName == Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                Toast.makeText(this, "$permissionName Granted", Toast.LENGTH_SHORT).show()

                CoroutineScope(IO).launch {
                saveImage(getBitmapFromView(findViewById(R.id.constraint_l3)))}

            }else {
                if (permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
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
        ibColorPicker = findViewById(R.id.ib_color_picker)

        ibBrush = findViewById(R.id.ib_brush)
        perpulButton = findViewById(R.id.perpul_button)
        redButton = findViewById(R.id.red_botton)
        greenButton = findViewById(R.id.green_botton)
        blueButton = findViewById(R.id.blue_botton)
        orangeButton = findViewById(R.id.orange_botton)
        undoButton = findViewById(R.id.ib_undo)
        ibSave = findViewById(R.id.ib_save)
        ibGallary = findViewById(R.id.ib_gallary)
        saveButton = findViewById(R.id.ib_save)




        perpulButton.setOnClickListener(this)
        redButton.setOnClickListener(this)
        greenButton.setOnClickListener(this)
        blueButton.setOnClickListener(this)
        orangeButton.setOnClickListener(this)
        ibBrush.setOnClickListener(this)
        undoButton.setOnClickListener(this)
        ibColorPicker.setOnClickListener(this)
        ibGallary.setOnClickListener(this)
        ibSave.setOnClickListener(this)
        saveButton.setOnClickListener(this)





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

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.perpul_button -> {
                drawingView.setColor("#7A277B")
            }

            R.id.red_botton -> {
                drawingView.setColor("#C62101")
            }

            R.id.green_botton -> {
                drawingView.setColor("#96A900")
            }

            R.id.blue_botton -> {
                drawingView.setColor("#34B5E6")
            }

            R.id.orange_botton -> {
                drawingView.setColor("#EE7722")
            }

            R.id.ib_undo -> {
                drawingView.undoPath()
            }

            R.id.ib_color_picker -> {
                showColorPickerDialog()
            }

            R.id.ib_gallary -> {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                    requestStoragePermission()
                } else {
                    //Get the image
                    val pickIntent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openGalleryLauncher.launch(pickIntent)
                }

            }

            R.id.ib_save -> {

                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestStoragePermission()

                } else {
                    val layout = findViewById<ConstraintLayout>(R.id.constraint_l3)
                    val bitmap = getBitmapFromView(layout)
                    CoroutineScope(IO).launch {
                        saveImage(getBitmapFromView(layout))
                    }


                }
            }


            R.id.ib_brush -> {
                showBrushSizeChooserDialog()
            }

            else -> {
                Toast.makeText(this@MainActivity, "not working", Toast.LENGTH_SHORT).show()
                return
            }

        }


    }

    private fun showColorPickerDialog() {
        val dialog =
            AmbilWarnaDialog(this, Color.GREEN, object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog?) {
                }

                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    drawingView.setColor(color)
                }
            })
        dialog.show()
    }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            showRationalDialog()
        } else {
            requestPermission.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }

    }

    private fun showRationalDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permission Required")
            .setMessage("We need permission to access your storage")
            .setPositiveButton("Yes") { dialog, _ ->
                requestPermission.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
                dialog.dismiss()
            }

        builder.create().show()
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        view.draw(canvas)
        return returnedBitmap
    }

    private suspend fun  saveImage(mBitmap: Bitmap) {
        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
        val myDir = File("$root/saved_images")
        myDir.mkdir()
        val generator = java.util.Random()
        var n = 10000
        n = generator.nextInt(n)
        val fileName = "Image-$n.jpg"

        var outPutFile = File(myDir, fileName)
        if (outPutFile.exists()) {
            outPutFile.delete()

        } else {
            try {
                val out = FileOutputStream(outPutFile)
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
                out.close()
                Toast.makeText(this, "Image Saved Successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.stackTrace
            }
            withContext(Main) {
           Toast.makeText(this@MainActivity, "${outPutFile.absolutePath} Saved", Toast.LENGTH_SHORT).show()
            }
        }


    }

}
