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
import androidx.core.content.ContextCompat
import com.cocido.ramfapp.R
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
    private val axisSpacingPx = 14f * density
    private val tickSpacingPx = 6f * density
    private val axisLineWidthPx = 1f * density
    private val labelOffsetPx = 16f * density
    private val labelAreaPx = (labelOffsetPx + 12f * density)

    private var requiredWidthPx: Float = 0f

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.chart_axis_color)
        strokeWidth = axisLineWidthPx
    }

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.RIGHT
        textSize = 11f * scaledDensity
        color = ContextCompat.getColor(context, R.color.text_secondary_color)
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 11.5f * scaledDensity
    }

    init {
        if (paddingLeft == 0 && paddingRight == 0 && paddingTop == 0 && paddingBottom == 0) {
            val verticalTop = (6f * density).toInt()
            val verticalBottom = (20f * density).toInt()
            setPadding(0, verticalTop, 0, verticalBottom)
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

        var totalWidth = 0f

        entries.forEachIndexed { index, entry ->
            if (index > 0) {
                totalWidth += axisSpacingPx
            }
            totalWidth += requiredWidthForAxis(entry)
        }

        return totalWidth
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
        var currentX = baseX

        overlayAxes.forEachIndexed { index, entry ->
            if (index > 0) currentX += axisSpacingPx
            val blockWidth = requiredWidthForAxis(entry)
            val axisX = currentX + blockWidth
            drawAxis(canvas, chartRef, entry, axisX, contentRect)
            currentX += blockWidth
        }
    }

    private fun requiredWidthForAxis(entry: AxisOverlayEntry): Float {
        val pattern = entry.config.formatPattern.ifBlank { "#0.##" }
        val formatter = formatterCache.getOrPut(pattern) { DecimalFormat(pattern) }
        val tickValues = entry.tickValues.ifEmpty {
            val config = entry.config
            val min = config.min ?: 0.0
            val max = config.max ?: min + 1.0
            val count = config.labelCount?.takeIf { it > 1 } ?: 5
            (0 until count).map { idx ->
                val fraction = idx.toDouble() / (count - 1)
                min + (max - min) * fraction
            }
        }
        val maxTickWidth = tickValues
            .map { formatter.format(it) }
            .maxOfOrNull { tickPaint.measureText(it) } ?: 0f
        return axisLineWidthPx + tickSpacingPx + maxTickWidth + labelAreaPx
    }

    private fun drawAxis(
        canvas: Canvas,
        chart: LineChart,
        entry: AxisOverlayEntry,
        axisX: Float,
        contentRect: RectF
    ) {
        canvas.drawLine(axisX, contentRect.top, axisX, contentRect.bottom, axisPaint)

        val pattern = entry.config.formatPattern.ifBlank { "#0.##" }
        val formatter = formatterCache.getOrPut(pattern) { DecimalFormat(pattern) }
        val transformer = chart.getTransformer(entry.dependency)
        val fontMetrics = tickPaint.fontMetrics

        entry.tickValues.forEach { tick ->
            val position = floatArrayOf(0f, (tick * entry.config.scaleFactor).toFloat())
            transformer.pointValuesToPixel(position)
            val y = position[1]
            if (y < contentRect.top || y > contentRect.bottom) return@forEach
            val label = formatter.format(tick)
            canvas.drawText(label, axisX - tickSpacingPx, y - fontMetrics.descent, tickPaint)
        }

        val label = buildAxisLabel(entry.config)
        if (label.isNotBlank()) {
            labelPaint.color = entry.config.color
            val centerY = (contentRect.top + contentRect.bottom) / 2f
            val labelX = axisX + labelOffsetPx
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

