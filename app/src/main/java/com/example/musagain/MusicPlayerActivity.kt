package com.example.musagain

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MusicPlayerActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var playPauseButton: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var songAdapter: SongAdapter
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvDuration: TextView
    private val songList = listOf(
        Song("Song 1", "Artist 1", R.raw.song1)
    )

    private val handler = Handler() // Handler for updating SeekBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_player)

        playPauseButton = findViewById(R.id.btnPlayPause)
        seekBar = findViewById(R.id.seekBar)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvDuration = findViewById(R.id.tvDuration)
        mediaPlayer = MediaPlayer()

        val rvSongList = findViewById<RecyclerView>(R.id.rvSongList)
        songAdapter = SongAdapter(songList) { song -> playSong(song) }
        rvSongList.adapter = songAdapter
        rvSongList.layoutManager = LinearLayoutManager(this)

        playPauseButton.setOnClickListener { togglePlayPause() }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(updateSeekBarRunnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                updateSeekBar()
            }
        })
    }

    private fun togglePlayPause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            playPauseButton.setImageResource(R.drawable.play)
        } else {
            mediaPlayer.start()
            playPauseButton.setImageResource(R.drawable.pause)
            updateSeekBar()
        }
    }

    private fun playSong(song: Song) {
        mediaPlayer.reset()
        mediaPlayer = MediaPlayer.create(this, song.resourceId)
        mediaPlayer.start()
        playPauseButton.setImageResource(R.drawable.pause)

        // Setup SeekBar for the new song
        seekBar.max = mediaPlayer.duration
        tvDuration.text = formatDuration(mediaPlayer.duration) // Set total duration
        updateSeekBar() // Start updating SeekBar when a new song is played

        mediaPlayer.setOnCompletionListener {
            playPauseButton.setImageResource(R.drawable.play)
            seekBar.progress = 0
            tvCurrentTime.text = formatDuration(0) // Reset current time when song ends
            handler.removeCallbacks(updateSeekBarRunnable)
        }

        mediaPlayer.setOnPreparedListener {
            seekBar.max = mediaPlayer.duration
            tvDuration.text = formatDuration(it.duration) // Set duration when prepared
            updateSeekBar()
        }
    }

    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            if (mediaPlayer.isPlaying) {
                seekBar.progress = mediaPlayer.currentPosition
                tvCurrentTime.text = formatDuration(mediaPlayer.currentPosition) // Update current time
                handler.postDelayed(this, 1000)
            }
        }
    }

    private fun updateSeekBar() {
        seekBar.progress = mediaPlayer.currentPosition
        tvCurrentTime.text = formatDuration(mediaPlayer.currentPosition) // Update current time
        handler.postDelayed(updateSeekBarRunnable, 1000)
    }

    private fun formatDuration(duration: Int): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        handler.removeCallbacks(updateSeekBarRunnable)
    }
}
