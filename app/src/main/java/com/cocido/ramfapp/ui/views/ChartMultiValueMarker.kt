package com.cocido.ramfapp.ui.views

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import com.cocido.ramfapp.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.DataSet
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChartMultiValueMarker(
    context: Context
) : MarkerView(context, R.layout.marker_chart_multi_value) {

    private val titleView: TextView = findViewById(R.id.markerTitle)
    private val valuesContainer: LinearLayout = findViewById(R.id.markerValuesContainer)
    private val timeFormatter = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())

    private var seriesInfo: List<MarkerSeriesInfo> = emptyList()
    private val pointOffset = MPPointF()

    fun updateSeries(info: List<MarkerSeriesInfo>) {
        seriesInfo = info
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e == null) return
        titleView.text = context.getString(
            R.string.marker_title_format,
            timeFormatter.format(Date(e.x.toLong()))
        )

        valuesContainer.removeAllViews()

        seriesInfo.forEach { info ->
            val dataSet = info.dataSet
            val entry = dataSet.getEntryForXValue(e.x, Float.NaN)
                ?: dataSet.getEntryForXValue(e.x, Float.NaN, DataSet.Rounding.CLOSEST)

            if (entry != null) {
                val rawValue = (entry.data as? Number)?.toDouble()
                    ?: (entry.y.toDouble() / info.scaleFactor)
                val text = buildValueText(info, rawValue)
                if (text != null) {
                    valuesContainer.addView(createValueTextView(info, text))
                }
            }
        }

        super.refreshContent(e, highlight)
    }

    private fun buildValueText(info: MarkerSeriesInfo, rawValue: Double?): String? {
        rawValue ?: return null
        val unitSuffix = if (info.unit.isBlank()) "" else " ${info.unit}"
        return "${info.label}: ${info.formatter.format(rawValue)}$unitSuffix"
    }

    private fun createValueTextView(info: MarkerSeriesInfo, text: String): TextView {
        val textView = TextView(context)
        val padding = (4 * resources.displayMetrics.density).toInt()
        textView.setPadding(0, padding, 0, padding)
        textView.text = text
        textView.setTextColor(info.color)
        textView.textSize = 13f
        return textView
    }

    override fun getOffset(): MPPointF {
        pointOffset.x = -(width / 2f)
        pointOffset.y = -height.toFloat() - 16f
        return pointOffset
    }

    override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {
        val offset = getOffset()
        val chartView = chartView ?: return offset

        val adjustedX = when {
            posX + offset.x < 0 -> -posX + 8f
            posX + width + offset.x > chartView.width -> chartView.width - posX - width - 8f
            else -> offset.x
        }

        val adjustedY = if (posY + offset.y < 0) {
            -posY + 8f
        } else {
            offset.y
        }

        pointOffset.x = adjustedX
        pointOffset.y = adjustedY
        return pointOffset
    }
}

data class MarkerSeriesInfo(
    val dataSet: LineDataSet,
    val label: String,
    val unit: String,
    val color: Int,
    val formatter: DecimalFormat,
    val scaleFactor: Double
)

