import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import com.example.playlistmaker.composestyle.TextTabRowStyle
import com.example.playlistmaker.composestyle.TopAppBarStyle
import com.example.playlistmaker.media.ui.activity.FragmentFavorites
import com.example.playlistmaker.media.ui.activity.FragmentPlaylists
import com.example.playlistmakercompose.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaScreen() {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    val currentPage by remember {
        derivedStateOf { pagerState.currentPage }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background))
    ) {

        TopAppBarStyle(R.string.Mediateka)

        TabRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            selectedTabIndex = currentPage,
            containerColor = colorResource(id = R.color.background),
            divider = { HorizontalDivider(color = Color.Transparent) },
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    height = 2.dp,
                    color = colorResource(id = R.color.blackNight)
                )

            }) {
            Tab(
                selected = currentPage == 0,
                onClick = {
                    scope.launch { pagerState.animateScrollToPage(0) }
                }, text = {
                    TextTabRowStyle(R.string.favorites_tracks_text)
                })

            Tab(selected = currentPage == 1, onClick = {
                scope.launch { pagerState.animateScrollToPage(1) }
            }, text = {
                TextTabRowStyle(R.string.playlists_text)
            })
        }

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            when (page) {
                0 -> FragmentFavoritesCompose()
                1 -> FragmentPlaylistsCompose()
            }
        }
    }
}

@Composable
fun FragmentPlaylistsCompose() {
    val context = LocalContext.current
    val activity = context as? AppCompatActivity
    val fragmentManager = activity?.supportFragmentManager

    AndroidView(
        factory = {
            FragmentContainerView(it).apply {
                id = View.generateViewId()
                fragmentManager?.beginTransaction()
                    ?.add(id, FragmentPlaylists.newInstance())
                    ?.commit()
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun FragmentFavoritesCompose() {
    val context = LocalContext.current
    val activity = context as? AppCompatActivity
    val fragmentManager = activity?.supportFragmentManager

    AndroidView(
        factory = {
            FragmentContainerView(it).apply {
                id = View.generateViewId()
                fragmentManager?.beginTransaction()
                    ?.add(id, FragmentFavorites.newInstance())
                    ?.commit()
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}