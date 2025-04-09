package com.example.playlistmaker.composestyle

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.playlistmakercompose.R

@Composable
fun TextTabRowStyle(stringResource: Int) {
    Text(
        text = stringResource(id = stringResource),
        fontFamily = FontFamily(Font(R.font.ys_display_medium)),
        fontWeight = FontWeight(500),
        color = colorResource(id = R.color.blackNight)
    )
}