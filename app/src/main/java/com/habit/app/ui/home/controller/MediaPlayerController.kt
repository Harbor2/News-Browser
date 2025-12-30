package com.habit.app.ui.home.controller

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import com.habit.app.R
import com.habit.app.data.TAG

class MediaPlayerController(val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    var mCallback: MediaPlayCallback? = null
    private var isPause: Boolean = false

    fun tryPlayBeep(isLoop: Boolean = false) {
        // 取消暂停
        isPause = false
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        }
        with(mediaPlayer!!) {
            reset()
            prepareMediaPlayer(this, isLoop)
        }
    }

    /**
     * 获取播放进度
     * @return 播放进度，单位：毫秒
     */
    fun getCurrentPosition(): Int {
        if (mediaPlayer == null) {
            return 0
        }
        return mediaPlayer!!.currentPosition
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    fun isPause(): Boolean {
        return isPause
    }

    fun pausePlay(): Boolean {
        mediaPlayer?.let {
            if (it.isPlaying) {
                isPause = true
                it.pause()
            }
            return true
        } ?: run { return false }
    }

    fun resumePlay(): Boolean {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                isPause = false
                it.start()
            }
            return true
        } ?: run { return false}
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    private fun prepareMediaPlayer(player: MediaPlayer, isLoop: Boolean) {
        try {
            with(player) {
                setAudioAttributes(
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build()
                )
                setVolume(1.0f, 1.0f)
                // raw资源
                val afd = context.resources.openRawResourceFd(R.raw.beep)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                isLooping = isLoop
                setOnPreparedListener {
                    // 准备完毕
                    it.start()
                    mCallback?.onPrepared()
                }
                setOnErrorListener { _, _, _ ->
                    // 播放出错
                    player.pause()
                    player.reset()
                    mCallback?.onPlayError()
                    true
                }
                setOnCompletionListener {
                    // 播放完成
                    mCallback?.onPlayFinish()
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "播放失败：${e.message}")
            mCallback?.onPlayError()
        }
    }

    fun onDestroy() {
        mediaPlayer?.let {
            it.pause()
            it.release()
            mediaPlayer = null
        }
    }

    interface MediaPlayCallback {
        fun onPrepared()

        fun onPlayError()

        fun onPlayFinish()
    }
}