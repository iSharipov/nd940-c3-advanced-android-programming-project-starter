package com.udacity.loadapp.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.udacity.ButtonState
import com.udacity.ButtonState.Completed
import com.udacity.R
import com.udacity.R.color
import java.util.Locale
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private val valueAnimator = ValueAnimator()
    private var completedBackgroundColor = context.getColor(color.colorPrimary)
    private var loadingBackgroundColor = context.getColor(color.colorPrimaryDark)
    private var textColor = Color.WHITE

    private val cornerPath = Path()

    private val paint = Paint().apply {
        isAntiAlias = true
        textAlignment = TEXT_ALIGNMENT_CENTER
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20f, resources.displayMetrics)
        typeface = Typeface.DEFAULT_BOLD
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(
        Completed
    ) { p, old, new ->

    }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LoadingButton,
            defStyleAttr,
            0
        ).apply {
            completedBackgroundColor = getColor(
                R.styleable.LoadingButton_completedStateColor,
                completedBackgroundColor
            )
            loadingBackgroundColor = getColor(
                R.styleable.LoadingButton_loadingStateColor,
                loadingBackgroundColor
            )
            textColor = getColor(
                R.styleable.LoadingButton_text,
                textColor
            )
        }.recycle()
    }

    private val textRect = Rect()
    private var textToDraw = context.getString(buttonState.textId).toUpperCase(Locale.ENGLISH)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {
            save()
            clipPath(cornerPath)
            drawColor(completedBackgroundColor)
            paint.getTextBounds(textToDraw, 0, textToDraw.length, textRect)
            val textX = width / 2f - textRect.width() / 2f
            val textY = height / 2f + textRect.height() / 2f - textRect.bottom
            paint.color = textColor
            drawText(textToDraw, textX, textY, paint)
            restore()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cornerPath.reset()
        cornerPath.addRoundRect(
            0f,
            0f,
            w.toFloat(),
            h.toFloat(),
            0f,
            0f,
            Path.Direction.CW
        )
        cornerPath.close()
    }

    fun setState(buttonState: ButtonState) {
        this.buttonState = buttonState
    }
}