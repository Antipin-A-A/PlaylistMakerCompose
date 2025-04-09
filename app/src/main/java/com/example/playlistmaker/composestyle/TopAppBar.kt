package com.example.playlistmaker.composestyle

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playlistmakercompose.R

@Composable
fun TopAppBarStyle(stringResource: Int) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(id = stringResource),
                color = colorResource(id = R.color.blackNight),
                fontSize = dimensionResource(id = R.dimen.text_size_22sp).value.sp,
                fontFamily = FontFamily(Font(R.font.ys_display_medium)),
                fontWeight = FontWeight(500),
            )
        },
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = colorResource(id = R.color.background),
        elevation = 0.dp
    )
}