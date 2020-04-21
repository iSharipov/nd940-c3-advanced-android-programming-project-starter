package com.udacity.loadapp.widgets

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.LinearInterpolator
import com.udacity.ButtonState
import com.udacity.ButtonState.Completed
import com.udacity.ButtonState.Loading
import com.udacity.R
import com.udacity.R.color
import java.util.Locale
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private lateinit var valueAnimator: ValueAnimator
    private var completedBackgroundColor = context.getColor(color.colorPrimary)
    private var loadingBackgroundColor = context.getColor(color.colorPrimaryDark)
    private var pacManColor = context.getColor(R.color.colorAccent)
    private var textColor = Color.WHITE

    private val cornerPath = Path()
    private var progress = 0
    private var loadingState = 0
    private val loadingRect = Rect()
    private val circleRect = RectF()

    private val paint = Paint().apply {
        isAntiAlias = true
        textAlignment = TEXT_ALIGNMENT_CENTER
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20f, resources.displayMetrics)
        typeface = Typeface.DEFAULT_BOLD
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(
        Completed
    ) { _, old, new ->
        textToDraw = context.getString(new.textId)

        when (buttonState) {
            Loading -> {
                if (old != Loading) {
                    valueAnimator = ValueAnimator.ofInt(0, 360).setDuration(2000).apply {
                        addUpdateListener {
                            progress = it.animatedValue as Int
                            invalidate()
                        }
                        addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                super.onAnimationEnd(animation)
                                this@LoadingButton.buttonState = Completed
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                                super.onAnimationCancel(animation)
                                progress = 0
                                loadingState = 0
                            }

                            override fun onAnimationRepeat(animation: Animator?) {
                                super.onAnimationRepeat(animation)
                                loadingState = loadingState xor 1
                            }
                        })
                        interpolator = LinearInterpolator()
                        repeatCount = ValueAnimator.INFINITE
                        repeatMode = ValueAnimator.RESTART
                        start()
                    }
                }
            }
            Completed -> valueAnimator.cancel()
        }
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

            if (buttonState == Loading) {
                paint.color = loadingBackgroundColor
                if (loadingState == 0) {
                    loadingRect.set(0, 0, width * progress / 360, height)
                } else {
                    loadingRect.set(width * progress / 360, 0, width, height)
                }
                drawRect(loadingRect, paint)

                paint.style = Paint.Style.FILL
                paint.color = pacManColor
                val circleStartX = width / 2f + textRect.width() / 2f
                val circleStartY = height / 2f - 20
                circleRect.set(circleStartX, circleStartY, circleStartX + 40, circleStartY + 40)
                if (loadingState == 0) {
                    drawArc(circleRect, 0f, progress.toFloat(), true, paint)
                } else {
                    drawArc(
                        circleRect,
                        progress.toFloat(),
                        360f - progress.toFloat(),
                        true,
                        paint
                    )
                }
            }

            paint.color = textColor
            drawText(textToDraw, textX - 20, textY, paint)
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