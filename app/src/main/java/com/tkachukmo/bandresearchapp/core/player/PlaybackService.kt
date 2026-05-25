package com.tkachukmo.bandresearchapp.core.player

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.tkachukmo.bandresearchapp.MainActivity // Переконайся, що імпорт співпадає з твоїм пакетом

class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
            super.onCreate()
            val player = ExoPlayer.Builder(this).build()

            // Створюємо Deep Link для відкриття екрану плеєра
            val intent = Intent(
                Intent.ACTION_VIEW,
                android.net.Uri.parse("bandmatch://player")
            ).apply {
                setPackage(packageName)
            }

            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            mediaSession = MediaSession.Builder(this, player)
                .setSessionActivity(pendingIntent)
                .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}