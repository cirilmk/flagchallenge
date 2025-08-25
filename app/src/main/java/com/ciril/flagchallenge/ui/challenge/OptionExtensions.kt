package com.ciril.flagchallenge.ui.challenge

import android.content.Context
import android.widget.TextView
import com.ciril.flagchallenge.R
import com.google.android.material.button.MaterialButton

fun MaterialButton.applyOptionState(
    state: OptionState,
    labelView: TextView,
    context: Context
) {
    when (state) {
        OptionState.DEFAULT -> {
            setBackgroundResource(R.drawable.bg_option_default)
            labelView.text = ""
        }
        OptionState.SELECTED -> {
            setBackgroundResource(R.drawable.bg_option_selected)
            labelView.text = ""
        }
        OptionState.CORRECT -> {
            setBackgroundResource(R.drawable.bg_option_correct)
            labelView.text = context.getString(R.string.correct)
            labelView.setTextColor(context.getColor(R.color.correctGreen))
        }
        OptionState.WRONG -> {
            setBackgroundResource(R.drawable.bg_option_selected)
            labelView.text = context.getString(R.string.wrong)
            labelView.setTextColor(context.getColor(R.color.wrongRed))
        }
    }
}