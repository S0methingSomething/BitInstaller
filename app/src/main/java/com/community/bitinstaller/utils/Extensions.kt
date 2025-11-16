package com.community.bitinstaller.utils

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

fun View.showError(message: String, duration: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(this, message, duration).show()
}

fun View.showErrorWithAction(message: String, actionText: String, action: () -> Unit) {
    Snackbar.make(this, message, Snackbar.LENGTH_LONG)
        .setAction(actionText) { action() }
        .show()
}

fun TextView.setColorResource(colorRes: Int) {
    setTextColor(ContextCompat.getColor(context, colorRes))
}
