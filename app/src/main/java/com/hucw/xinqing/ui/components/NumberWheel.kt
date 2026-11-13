package com.hucw.xinqing.ui.components

import android.widget.NumberPicker
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import java.util.Locale

@Composable
fun NumberWheel(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            NumberPicker(context).apply {
                minValue = range.first
                maxValue = range.last
                wrapSelectorWheel = true
                setFormatter { String.format(Locale.getDefault(), "%02d", it) }
                this.value = value
                setOnValueChangedListener { _, _, newValue -> onValueChange(newValue) }
            }
        },
        update = { picker ->
            if (picker.value != value) {
                picker.value = value
            }
        },
        modifier = modifier
    )
}
