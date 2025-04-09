package com.example.playlistmaker.player.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.playlistmakercompose.R
import com.example.playlistmaker.player.data.impl.MediaPlayerRepositoryImpl
import com.example.playlistmaker.player.domain.api.interact.MediaPlayerInteract
import com.example.playlistmaker.player.domain.imp.MediaPlayerInteractImpl
import com.example.playlistmaker.player.ui.state.PlayerState
import com.example.playlistmaker.player.ui.state.TimeTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class MusicService :Service(),AudioPlayerControl {

    private companion object {
        const val NOTIFICATION_CHANNEL_ID = "music_service_channel"
        const val SERVICE_NOTIFICATION_ID = 100
        private const val DELAY = 300L
    }

    private val binder = MusicServiceBinder()

    private var trackInfo = ""
    private var songUrl = ""

    private lateinit var mediaPlayerInteract: MediaPlayerInteract

    private var timerJob: Job? = null

    private val playerState = MutableStateFlow<PlayerState>(PlayerState.DEFAULT)

    private val _currentPosition = MutableStateFlow<TimeTrack>(TimeTrack.DefauliTime())

    override fun onCreate() {

        super.onCreate()
        createNotificationChannel()
        val repository = MediaPlayerRepositoryImpl(MediaPlayer())
        mediaPlayerInteract = MediaPlayerInteractImpl(repository)
    }
    override fun getPlayerState(): StateFlow<PlayerState> {
        return playerState
    }

    override fun getPlayerTime(): StateFlow<TimeTrack> {
        return _currentPosition
    }

    override fun startPlayer() {
        mediaPlayerInteract.play()
        startPositionUpdates()
    }

    override fun pausePlayer() {
        mediaPlayerInteract.pausePlayer()
        timerJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun preparePlayer() {
        when (mediaPlayerInteract.playerState) {
            PlayerState.DEFAULT -> prepareAndPlay(songUrl)
            PlayerState.PREPARED, PlayerState.PAUSED -> mediaPlayerInteract.play()
            else -> Unit
        }
    }

    private fun prepareAndPlay(url: String) {
        mediaPlayerInteract.preparePlayer(url)
        startPositionUpdates()
    }

    private fun observePlayerTime() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(500)
                if (mediaPlayerInteract.isPlaying()) {
                    startPositionUpdates()
                }
            }
        }
        mediaPlayerInteract.setOnCompletionListener {
            timerJob?.cancel()
            _currentPosition.value = TimeTrack.DefauliTime()
            playerState.value = PlayerState.PREPARED
        }
    }

    private fun startPositionUpdates() {
        timerJob?.cancel()
        timerJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                _currentPosition.value = TimeTrack.CurentTime(getCurrentPlayerPosition())
                delay(DELAY)
            }
        }
    }

    private fun getCurrentPlayerPosition(): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(mediaPlayerInteract.currentPosition()) ?: "00:00"
    }

    override fun provideNotificator() {
        createNotificationChannel()
        ServiceCompat.startForeground(
            this,
            SERVICE_NOTIFICATION_ID,
            createServiceNotification(),
            getForegroundServiceTypeConstant()
        )
    }
    override fun stopNotification() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }

    override fun onBind(intent: Intent?): IBinder {
        songUrl = intent?.getStringExtra("song_url") ?: ""
        val artist = intent?.getStringExtra("artist_name_song") ?: ""
        val track = intent?.getStringExtra("track_name_song") ?: ""
        trackInfo = "$artist - $track"
        preparePlayer()
        observePlayerTime()
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    inner class MusicServiceBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
             NOTIFICATION_CHANNEL_ID,
             "Music service",
             NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Service for playing music"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createServiceNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Playlist Maker")
            .setContentText(trackInfo)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun getForegroundServiceTypeConstant(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        } else {
            0
        }
    }
}