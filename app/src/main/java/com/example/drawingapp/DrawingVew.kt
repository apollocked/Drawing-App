package com.example.drawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.Toast

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    //drawing path
    private lateinit var drawPath: FingerPath

    // defines how to draw
    private lateinit var drawPaint: Paint
    private var color = Color.BLACK
    private lateinit var canvas: Canvas
    private lateinit var canvasBitmap: Bitmap
    private var brushSize: Float = 0.toFloat()
    private var canvasPaint = Paint()
    private var paths = mutableListOf<FingerPath>()

    init {
        setUpDrawing()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(canvasBitmap)

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(canvasBitmap, 0f, 0f, drawPaint)

        for (path in paths) {
            drawPaint.strokeWidth = path.brushThichness
            drawPaint.color = path.color
            canvas.drawPath(path, drawPaint)
        }

        if (!drawPath.isEmpty) {
            drawPaint.strokeWidth = drawPath.brushThichness
            drawPaint.color = drawPath.color
            canvas?.drawPath(drawPath, drawPaint)
        }
    }

    // this fun will be called by system when user is going to touch the screen
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when (event?.action) {
            //this event will be fired when THE USER PUT FINGER on screen
            MotionEvent.ACTION_DOWN -> {
                drawPath.color = color
                drawPath.brushThichness = brushSize.toFloat()

                drawPath.reset()
                drawPath.moveTo(touchX!!, touchY!!)
            }
            // the even will be fired when the user starts to move it's finger; this will
            // fired continually until user pickup the finger

            MotionEvent.ACTION_MOVE -> {
                drawPath.lineTo(touchX!!, touchY!!)
            }
            // this event will be fired when the user will pick up the finger from screen
            MotionEvent.ACTION_UP -> {
                paths.add(drawPath)
                drawPath = FingerPath(color, brushSize)

            }

            else -> return false


        }

        invalidate()
        return true

    }

    fun setUpDrawing() {
        drawPaint = Paint()
        drawPath = FingerPath(color, brushSize)
        drawPaint.color = color
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeJoin = Paint.Join.ROUND
        drawPaint.strokeCap = Paint.Cap.ROUND

        canvasPaint = Paint(Paint.DITHER_FLAG)

        brushSize = 20.toFloat()


    }

    fun changeBrushSize(newSize: Float) {
        brushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newSize,
            resources.displayMetrics
        )
        drawPaint.strokeWidth = brushSize
    }

    fun setColor(newColor: Any) {
        if ( newColor is String){
            color = Color.parseColor(newColor)
            drawPaint.color = color
        }else if (newColor is Int){
            color = newColor
            drawPaint.color = color
        }

        invalidate()
    }

fun undoPath() {
    if (paths.size > 0) {
        paths.removeAt(paths.size - 1)}
    invalidate()
}





    internal inner class FingerPath(var color: Int, var brushThichness: Float) : Path()


}