package com.cocido.ramfapp.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.cocido.ramfapp.models.ChartAxisConfig
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import java.text.DecimalFormat

data class AxisOverlayEntry(
    val config: ChartAxisConfig,
    val tickValues: List<Double>,
    val dependency: YAxis.AxisDependency
)

class ChartAxisOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var chart: LineChart? = null
    private var overlayAxes: List<AxisOverlayEntry> = emptyList()

    private val density = resources.displayMetrics.density
    private val scaledDensity = resources.displayMetrics.scaledDensity
    private val axisSpacingPx = 48f * density
    private val labelPaddingPx = 36f * density

    private var requiredWidthPx: Float = 0f

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
        textSize = 11f * scaledDensity
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 11f * scaledDensity
    }

    init {
        if (paddingLeft == 0 && paddingRight == 0 && paddingTop == 0 && paddingBottom == 0) {
            val horizontal = (8f * density).toInt()
            val verticalTop = (8f * density).toInt()
            val verticalBottom = (24f * density).toInt()
            setPadding(horizontal, verticalTop, horizontal, verticalBottom)
        }
    }

    private val formatterCache = mutableMapOf<String, DecimalFormat>()

    fun update(chart: LineChart, axes: List<AxisOverlayEntry>) {
        this.chart = chart
        this.overlayAxes = axes
        requiredWidthPx = requiredWidthFor(axes)
        requestLayout()
        invalidate()
    }

    fun clear() {
        this.overlayAxes = emptyList()
        requiredWidthPx = 0f
        requestLayout()
        invalidate()
    }

    fun requiredWidthFor(entries: List<AxisOverlayEntry>): Float {
        if (entries.isEmpty()) return 0f
        val count = entries.size
        return paddingStart + paddingEnd + (axisSpacingPx * count) + labelPaddingPx
    }

    fun requiredWidthPx(): Float = requiredWidthPx

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = requiredWidthPx.coerceAtLeast(suggestedMinimumWidth.toFloat())
        val resolvedWidth = resolveSize(desiredWidth.toInt(), widthMeasureSpec)
        val resolvedHeight = resolveSize(suggestedMinimumHeight, heightMeasureSpec)
        setMeasuredDimension(resolvedWidth, resolvedHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val chartRef = chart ?: return
        if (overlayAxes.isEmpty()) return

        val contentRect: RectF = chartRef.viewPortHandler.contentRect
        val baseX = paddingStart.toFloat()

        overlayAxes.forEachIndexed { index, entry ->
            val axisX = baseX + axisSpacingPx * index
            drawAxis(canvas, chartRef, entry, axisX, contentRect)
        }
    }

    private fun drawAxis(
        canvas: Canvas,
        chart: LineChart,
        entry: AxisOverlayEntry,
        axisX: Float,
        contentRect: RectF
    ) {
        axisPaint.color = entry.config.color
        axisPaint.strokeWidth = 1.2f * density
        canvas.drawLine(axisX, contentRect.top, axisX, contentRect.bottom, axisPaint)

        val pattern = entry.config.formatPattern.ifBlank { "#0.##" }
        val formatter = formatterCache.getOrPut(pattern) { DecimalFormat(pattern) }
        val transformer = chart.getTransformer(entry.dependency)
        val textOffset = 8f * density
        val baselineOffset = tickPaint.textSize * 0.35f

        entry.tickValues.forEach { tick ->
            val position = floatArrayOf(0f, (tick * entry.config.scaleFactor).toFloat())
            transformer.pointValuesToPixel(position)
            val y = position[1]
            if (y < contentRect.top || y > contentRect.bottom) return@forEach
            tickPaint.color = entry.config.color
            val label = formatter.format(tick)
            canvas.drawText(label, axisX + textOffset, y + baselineOffset, tickPaint)
        }

        val label = buildAxisLabel(entry.config)
        if (label.isNotBlank()) {
            labelPaint.color = entry.config.color
            val centerY = (contentRect.top + contentRect.bottom) / 2f
            val labelX = axisX - 18f * density
            canvas.save()
            canvas.rotate(-90f, labelX, centerY)
            canvas.drawText(label, labelX, centerY, labelPaint)
            canvas.restore()
        }
    }

    private fun buildAxisLabel(config: ChartAxisConfig): String {
        val unit = config.unit.trim()
        return if (unit.isEmpty()) {
            config.label
        } else {
            "${config.label} ($unit)"
        }
    }
}

