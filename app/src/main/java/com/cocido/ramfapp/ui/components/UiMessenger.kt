package com.cocido.ramfapp.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.cocido.ramfapp.R
import com.google.android.material.snackbar.Snackbar

enum class UiMessageStyle {
    SUCCESS,
    ERROR,
    INFO
}

fun Context.showUiMessage(
    message: String,
    style: UiMessageStyle = UiMessageStyle.INFO,
    duration: Int = Snackbar.LENGTH_SHORT,
    anchorView: View? = null
) {
    val activity = anchorView?.context?.findActivity() ?: findActivity()
    val targetView = anchorView ?: activity?.findViewById(android.R.id.content)

    if (targetView == null) {
        Log.w("UiMessenger", "No se pudo mostrar el mensaje porque no hay vista disponible: $message")
        return
    }

    val displayAction = {
        Snackbar.make(targetView, message, duration).apply {
            setBackgroundTint(ContextCompat.getColor(targetView.context, style.backgroundColor()))
            setTextColor(ContextCompat.getColor(targetView.context, R.color.white))
            view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)?.apply {
                textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                maxLines = 4
            }
        }.show()
    }

    if (Looper.myLooper() == Looper.getMainLooper()) {
        displayAction()
    } else {
        Handler(Looper.getMainLooper()).post { displayAction() }
    }
}

fun Context.showSuccessMessage(
    message: String,
    duration: Int = Snackbar.LENGTH_SHORT,
    anchorView: View? = null
) = showUiMessage(message, UiMessageStyle.SUCCESS, duration, anchorView)

fun Context.showErrorMessage(
    message: String,
    duration: Int = Snackbar.LENGTH_LONG,
    anchorView: View? = null
) = showUiMessage(message, UiMessageStyle.ERROR, duration, anchorView)

fun Context.showInfoMessage(
    message: String,
    duration: Int = Snackbar.LENGTH_SHORT,
    anchorView: View? = null
) = showUiMessage(message, UiMessageStyle.INFO, duration, anchorView)

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@ColorRes
private fun UiMessageStyle.backgroundColor(): Int = when (this) {
    UiMessageStyle.SUCCESS -> R.color.success_green
    UiMessageStyle.ERROR -> R.color.error_red
    UiMessageStyle.INFO -> R.color.formosa_blue_accent
}

