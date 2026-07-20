package com.mariustia.musique.data

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import org.json.JSONArray
import org.json.JSONObject

data class Playlist(
    val name: String,
    val trackIds: List<Long>
)

object PlaylistManager {
    private const val PREFS_NAME = "marius_tia_musique_playlists"
    private const val KEY_DATA = "playlists_json"

    // { "Nom de playlist": [id1, id2, ...], ... }
    val playlists = mutableStateOf<List<Playlist>>(emptyList())

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
        load()
    }

    private fun prefs() = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun load() {
        val raw = prefs().getString(KEY_DATA, null)
        if (raw.isNullOrBlank()) {
            playlists.value = emptyList()
            return
        }
        val obj = JSONObject(raw)
        val result = mutableListOf<Playlist>()
        obj.keys().forEach { name ->
            val arr = obj.getJSONArray(name)
            val ids = (0 until arr.length()).map { arr.getLong(it) }
            result.add(Playlist(name, ids))
        }
        playlists.value = result
    }

    private fun persist() {
        val obj = JSONObject()
        playlists.value.forEach { pl ->
            obj.put(pl.name, JSONArray(pl.trackIds))
        }
        prefs().edit().putString(KEY_DATA, obj.toString()).apply()
    }

    fun createPlaylist(name: String) {
        if (name.isBlank() || playlists.value.any { it.name == name }) return
        playlists.value = playlists.value + Playlist(name, emptyList())
        persist()
    }

    fun deletePlaylist(name: String) {
        playlists.value = playlists.value.filterNot { it.name == name }
        persist()
    }

    fun addTrackToPlaylist(playlistName: String, trackId: Long) {
        playlists.value = playlists.value.map { pl ->
            if (pl.name == playlistName && trackId !in pl.trackIds) {
                pl.copy(trackIds = pl.trackIds + trackId)
            } else pl
        }
        persist()
    }

    fun removeTrackFromPlaylist(playlistName: String, trackId: Long) {
        playlists.value = playlists.value.map { pl ->
            if (pl.name == playlistName) pl.copy(trackIds = pl.trackIds.filterNot { it == trackId })
            else pl
        }
        persist()
    }
}
