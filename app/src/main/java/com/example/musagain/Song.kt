package com.example.musagain

data class Song(
    val title: String,
    val artist: String,
    val resourceId: Int,
)
val songs = listOf(
    Song("Song 1", "Artist 1", R.raw.song1),
)

