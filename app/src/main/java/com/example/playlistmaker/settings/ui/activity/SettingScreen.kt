import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playlistmaker.composestyle.TopAppBarStyle
import com.example.playlistmaker.settings.ui.viewmodel.SettingsViewModel
import com.example.playlistmakercompose.R


@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {

    val isChecked by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background)),
    ) {
        TopAppBarStyle(R.string.system_text)

        Column() {
            SwitchWithText(
                isChecked,
                { isChecked -> viewModel.switchTheme(isChecked) }
            )

            SettingItem(
                textId = R.string.share_app,
                endIconId = R.drawable.light_mode_share,
                onClick = { viewModel.shareAppLink() }
            )

            SettingItem(
                textId = R.string.write_service,
                endIconId = R.drawable.light_mode_support,
                onClick = { viewModel.sendSupport() }
            )

            SettingItem(
                textId = R.string.user_agreement,
                endIconId = R.drawable.light_mode_arrow,
                onClick = { viewModel.openTerms() }
            )
        }
    }
}

@Composable
fun SwitchWithText(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(id = R.string.night_theme),
            style = MaterialTheme.typography.body1.copy(
                fontSize = dimensionResource(id = R.dimen.text_size_16sp).value.sp,
                fontFamily = FontFamily(Font(R.font.ys_display_regular)),
                fontWeight = FontWeight(400),
                color = colorResource(id = R.color.blackNight)
            ),
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .weight(1f)
        )
        Switch(
            modifier = Modifier.padding(horizontal = 16.dp),
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colorResource(id = R.color.colorThumbTint),
                checkedTrackColor = colorResource(id = R.color.colorTrackTint),
                uncheckedThumbColor = colorResource(id = R.color.colorThumbTint),
                uncheckedTrackColor = colorResource(id = R.color.colorTrackTint)
            )
        )
    }
}

@Composable
fun SettingItem(textId: Int, endIconId: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = textId),
            style = MaterialTheme.typography.body1.copy(
                fontSize = dimensionResource(id = R.dimen.text_size_16sp).value.sp,
                fontFamily = FontFamily(Font(R.font.ys_display_regular)),
                fontWeight = FontWeight(400),
                color = colorResource(id = R.color.blackNight)
            ),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 20.dp)
        )
        Icon(
            painter = painterResource(id = endIconId),
            contentDescription = null,
            tint = colorResource(id = R.color.gray_only),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}