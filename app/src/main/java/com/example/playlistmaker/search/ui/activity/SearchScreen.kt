import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.isVisible
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.playlistmaker.composestyle.TopAppBarStyle
import com.example.playlistmaker.search.domain.modeles.Track
import com.example.playlistmaker.search.ui.state.TrackListState
import com.example.playlistmaker.search.ui.viewmodel.SearchActivityViewModel
import com.example.playlistmakercompose.R
import com.google.android.material.bottomnavigation.BottomNavigationView


@Composable
fun SearchScreen(
    state: TrackListState,
    viewModel: SearchActivityViewModel,
    navController: NavController,
    bottomNavigationView: BottomNavigationView,
    clickDebounce: Boolean,
) {
    var searchText by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    val isSearchButtonVisible by remember { derivedStateOf { searchText.text.isNotEmpty() } }
    var isTextFieldFocused by remember { mutableStateOf(false) }
    var isHistoryVisible by remember { mutableStateOf(true) }
    var isTrackListVisible by remember { mutableStateOf(false) }
    val onClick: (Track) -> Unit = { track ->
        if (clickDebounce) {
            viewModel.saveHistoryTrack(track)
            viewModel.saveTrack(track)
           navController.navigate(R.id.musicFragment)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
    ) {
        TopAppBarStyle(R.string.search_text)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = dimensionResource(id = R.dimen.indent_16dp),
                    top = dimensionResource(id = R.dimen.indent_8dp),
                    end = dimensionResource(id = R.dimen.indent_16dp),
                    bottom = dimensionResource(id = R.dimen.indent_8dp)
                ),
            contentAlignment = Alignment.Center

        ) {
            BasicTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    viewModel.searchDebounce(it.text)
                    viewModel.getHistoryTrackList()
                    isHistoryVisible = it.text.isEmpty()
                    isTrackListVisible = it.text.isNotEmpty()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .onFocusChanged { focusState ->
                        isTextFieldFocused = focusState.isFocused
                    },
                textStyle = TextStyle(
                    fontFamily = FontFamily(Font(R.font.ys_display_regular)),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W400,
                    color = colorResource(id = R.color.black)
                ),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .background(
                                color = colorResource(id = R.color.fonEdit),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.search_16),
                            contentDescription = null,
                            tint = colorResource(id = R.color.color_hint_gray_black)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchText.text.isEmpty()) {
                                Text(
                                    text = stringResource(id = R.string.search_text),
                                    color = colorResource(id = R.color.color_hint_gray_black),
                                    style = TextStyle(fontSize = 16.sp)
                                )
                            }
                            innerTextField()
                        }
                        if (isSearchButtonVisible) {
                            IconButton(
                                onClick = {
                                    searchText = TextFieldValue("")
                                    viewModel.searchDebounce("")
                                    //  viewModel
                                    isTextFieldFocused = true
                                    isHistoryVisible = true
                                    isTrackListVisible = false
                                },
                                modifier = Modifier
                                    .size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.clear_16),
                                    contentDescription = null,
                                    tint = colorResource(id = R.color.color_hint_gray_black)
                                )
                            }
                        }
                    }
                },
                singleLine = true,
            )
        }

        when (state) {
            is TrackListState.Loading -> {
                bottomNavigationView.isVisible = true
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colorResource(R.color.color_cursor))
                }
            }

            is TrackListState.Content -> {
                bottomNavigationView.isVisible = false
                val tracks = state.track
                TrackListFound(tracks = tracks, onClick = onClick)
            }

            is TrackListState.Empty -> {
                bottomNavigationView.isVisible = true
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = dimensionResource(id = R.dimen.indent_102dp)
                        ),

                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.search_error_mode),
                            contentDescription = "Empty Search",
                        )
                        Text(
                            text = stringResource(id = R.string.error_is_empty),
                            fontSize = dimensionResource(id = R.dimen.text_size_19sp).value.sp,
                            fontFamily = FontFamily(Font(R.font.ys_display_medium)),
                            fontWeight = FontWeight(400),
                            color = colorResource(id = R.color.blackNight),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            is TrackListState.Error -> {
                bottomNavigationView.isVisible = true
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = dimensionResource(id = R.dimen.indent_24dp),
                            top = dimensionResource(id = R.dimen.indent_102dp),
                            end = dimensionResource(id = R.dimen.indent_24dp),
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.intent_mode),
                            contentDescription = "Error",
                        )
                        Text(
                            text = stringResource(id = R.string.errorWifi),
                            fontSize = dimensionResource(id = R.dimen.text_size_19sp).value.sp,
                            fontFamily = FontFamily(Font(R.font.ys_display_medium)),
                            fontWeight = FontWeight(400),
                            color = colorResource(id = R.color.blackNight),
                            textAlign = TextAlign.Center,
                        )
                        Button(
                            onClick = {
                                viewModel.iTunesServiceSearch(searchText.text)
                            }, modifier = Modifier
                                .wrapContentSize()
                                .padding(top = dimensionResource(id = R.dimen.indent_24dp)),
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.indent_54dp)),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = colorResource(id = R.color.blackNight),
                                contentColor = colorResource(id = R.color.background)
                            )
                        ) {
                            Text(
                                text = stringResource(id = R.string.Update),
                                fontSize = dimensionResource(id = R.dimen.text_size_14sp).value.sp,
                                fontFamily = FontFamily(Font(R.font.ys_display_medium)),
                                fontWeight = FontWeight(500)
                            )
                        }
                    }
                }
            }

            is TrackListState.GetHistoryList -> {
                bottomNavigationView.isVisible = true
                if (isTextFieldFocused && isHistoryVisible) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (state.track.isNotEmpty()) {
                            val tracks = state.track
                            HistoryTrackList(viewModel,
                                tracks = tracks,
                                onClick = onClick,
                                onClearHistory = {
                                    isHistoryVisible = false
                                })
                        }

                    }

                }
            }
        }
    }
}

@Composable
fun HistoryTrackList( viewModel: SearchActivityViewModel,
    tracks: List<Track>,
    onClick: (Track) -> Unit,
    onClearHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier
                .height(24.dp)
                .fillMaxSize(),
            text = stringResource(R.string.find_you),
            fontSize = dimensionResource(id = R.dimen.text_size_19sp).value.sp,
            color = colorResource(R.color.blackNight),
            textAlign = TextAlign.Center
        )
        LazyColumn{
            items(tracks) { track ->
                TrackItem(track = track, onClick = onClick)
            }
        }
        if (!tracks.isEmpty()) {
            ClearHistoryButton(
                onClick = {
                    viewModel.removeHistoryTrackList()
                    onClearHistory()
                }
            )
        }
    }
}


@Composable
fun ClearHistoryButton(
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .wrapContentSize(),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.indent_54dp)),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(id = R.color.blackNight),
            contentColor = colorResource(id = R.color.background)
        ),

        ) {
        Text(
            text = stringResource(id = R.string.clear_hictory_text_button),
            fontSize = dimensionResource(id = R.dimen.text_size_14sp).value.sp,
            fontFamily = FontFamily(Font(R.font.ys_display_medium)),
            fontWeight = FontWeight(500),
        )
    }
}

@Composable
fun TrackListFound(tracks: List<Track>, onClick: (Track) -> Unit) {
    LazyColumn(
    ) {
        items(tracks) { track ->
            TrackItem(track = track, onClick = onClick)
        }
    }
}

@Composable
fun TrackItem(track: Track, onClick: (Track) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(track) },
        elevation = 0.dp,
        backgroundColor = colorResource(R.color.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = dimensionResource(id = R.dimen.indent_13dp),
                    top = dimensionResource(id = R.dimen.indent_8dp),
                    end = dimensionResource(id = R.dimen.indent_8dp),
                    bottom = dimensionResource(id = R.dimen.indent_8dp)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(
                            data = track.artworkUrl100
                        ).apply(block = fun ImageRequest.Builder.() {
                            placeholder(R.drawable.empty_image_group)
                            crossfade(true)
                        }).build()
                ),
                contentDescription = "Music Album Cover",
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.indent_45dp))
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = dimensionResource(id = R.dimen.indent_16dp))
            ) {
                Text(
                    text = track.trackName.toString(),
                    fontSize = dimensionResource(id = R.dimen.text_size_16sp).value.sp,
                    fontFamily = FontFamily(Font(R.font.ys_display_medium)),
                    fontWeight = FontWeight(500),
                    color = colorResource(id = R.color.colorTextTrack),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    textAlign = TextAlign.Start
                )
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    val artistName = if (track.artistName?.length!! > 20) {
                        track.artistName.take(17) + "..."
                    } else {
                        track.artistName
                    }
                    Text(
                        text = stringResource(
                            id = R.string.name_artist_time,
                            artistName,
                            track.trackTimeMillis.toString()
                        ),
                        fontSize = dimensionResource(id = R.dimen.text_size_11sp).value.sp,
                        fontFamily = FontFamily(Font(R.font.ys_display_medium)),
                        fontWeight = FontWeight(500),
                        color = colorResource(id = R.color.colorTextNameTime),
                        textAlign = TextAlign.Start,
                    )
                }
            }

            Icon(
                painter = painterResource(id = R.drawable.light_mode_arrow),
                contentDescription = "Forward Arrow",
                tint = colorResource(id = R.color.gray_only),
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.indent_24dp))
            )
        }
    }

    // с этим я еще не разобрался
//    if (track.isFavorite) {
//        Image(
//            painter = painterResource(id = R.drawable.haed_red),
//            contentDescription = "Favorite Icon",
//            modifier = Modifier
//                .size(dimensionResource(id = R.dimen.indent_14dp))
//                // .align(Alignment.TopStart)
//                .padding(
//                    start = dimensionResource(id = R.dimen.indent_50dp),
//                    top = dimensionResource(id = R.dimen.indent_21dp)
//                )
//        )
//    }
}

