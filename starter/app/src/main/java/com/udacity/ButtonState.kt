package com.udacity

import androidx.annotation.StringRes

sealed class ButtonState(@StringRes internal val textId: Int) {
    object Clicked : ButtonState(R.string.button_loading)
    object Loading : ButtonState(R.string.button_loading)
    object Completed : ButtonState(R.string.button_completed)
}