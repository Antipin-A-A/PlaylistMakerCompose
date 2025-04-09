package com.example.playlistmaker.player.ui.customview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.graphics.drawable.toBitmap
import com.example.playlistmakercompose.R
import kotlin.math.min

class PlaybackButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val imageBitmapPlay: Bitmap?
    private val imageBitmapPause: Bitmap?
    private var isPlaying: Boolean = false
    private var imageRect = RectF(0f, 0f, 0f, 0f)

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.PlaybackButtonView,
            defStyleAttr,
            defStyleRes
        ).apply {
            try {

                imageBitmapPlay = getDrawable(R.styleable.PlaybackButtonView_imagePlay)?.toBitmap()
                imageBitmapPause =
                    getDrawable(R.styleable.PlaybackButtonView_imagePause)?.toBitmap()

            } finally {
                recycle()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = min(measuredWidth, measuredHeight)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        imageRect = RectF(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        val bitmapToDraw = if (isPlaying) imageBitmapPause else imageBitmapPlay
        bitmapToDraw?.let {
            canvas.drawBitmap(bitmapToDraw, null, imageRect, null)
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        isPlaying = !isPlaying
        invalidate()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                return true
            }
            MotionEvent.ACTION_UP -> {
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }


    fun setPlaying(playing: Boolean) {
        isPlaying = playing
        invalidate()
    }
}