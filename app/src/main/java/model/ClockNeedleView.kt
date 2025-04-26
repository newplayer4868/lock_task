package model

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class ClockNeedleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeWidth = 8f
    }

    private var angle: Float = 0f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val needleLength = min(centerX, centerY) * 0.8f

        canvas.save()
        canvas.rotate(angle, centerX, centerY)
        canvas.drawLine(centerX, centerY, centerX, centerY - needleLength, needlePaint)
        canvas.restore()
    }

    // 외부에서 각도 업데이트할 수 있도록
    fun setAngle(angle: Float) {
        this.angle = angle
        invalidate()
    }
}
