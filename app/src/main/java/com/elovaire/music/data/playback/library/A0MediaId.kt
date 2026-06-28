package elovaire.music.droidbeauty.app.data.playback.library

import android.net.Uri

internal sealed interface A0MediaId {
    val value: String

    data object Root : A0MediaId { override val value = "a0:root" }
    data object PermissionRequired : A0MediaId { override val value = "a0:info:permission_required" }
    data object EmptyLibrary : A0MediaId { override val value = "a0:info:empty_library" }
    data object Songs : A0MediaId { override val value = "a0:songs" }
    data object Albums : A0MediaId { override val value = "a0:albums" }
    data object Artists : A0MediaId { override val value = "a0:artists" }
    data object Genres : A0MediaId { override val value = "a0:genres" }
    data object Playlists : A0MediaId { override val value = "a0:playlists" }
    data object Favorites : A0MediaId { override val value = "a0:favorites" }
    data object RecentlyAdded : A0MediaId { override val value = "a0:recently_added" }
    data class Song(val songId: Long) : A0MediaId { override val value = "a0:song:$songId" }
    data class Album(val albumId: Long) : A0MediaId { override val value = "a0:album:$albumId" }
    data class Artist(val encodedName: String) : A0MediaId { override val value = "a0:artist:$encodedName" }
    data class Genre(val encodedName: String) : A0MediaId { override val value = "a0:genre:$encodedName" }
    data class Playlist(val playlistId: Long) : A0MediaId { override val value = "a0:playlist:$playlistId" }
}

internal object A0MediaIds {
    fun song(id: Long): String = A0MediaId.Song(id).value
    fun album(id: Long): String = A0MediaId.Album(id).value
    fun artist(name: String): String = A0MediaId.Artist(Uri.encode(name)).value
    fun genre(name: String): String = A0MediaId.Genre(Uri.encode(name)).value
    fun playlist(id: Long): String = A0MediaId.Playlist(id).value
    fun decodeName(encoded: String): String = Uri.decode(encoded).orEmpty()

    fun parse(value: String?): A0MediaId? = when {
        value == A0MediaId.Root.value -> A0MediaId.Root
        value == A0MediaId.PermissionRequired.value -> A0MediaId.PermissionRequired
        value == A0MediaId.EmptyLibrary.value -> A0MediaId.EmptyLibrary
        value == A0MediaId.Songs.value -> A0MediaId.Songs
        value == A0MediaId.Albums.value -> A0MediaId.Albums
        value == A0MediaId.Artists.value -> A0MediaId.Artists
        value == A0MediaId.Genres.value -> A0MediaId.Genres
        value == A0MediaId.Playlists.value -> A0MediaId.Playlists
        value == A0MediaId.Favorites.value -> A0MediaId.Favorites
        value == A0MediaId.RecentlyAdded.value -> A0MediaId.RecentlyAdded
        value?.startsWith(SONG_PREFIX) == true ->
            value.removePrefix(SONG_PREFIX).toLongOrNull()?.let(A0MediaId::Song)
        value?.startsWith(ALBUM_PREFIX) == true ->
            value.removePrefix(ALBUM_PREFIX).toLongOrNull()?.let(A0MediaId::Album)
        value?.startsWith(ARTIST_PREFIX) == true ->
            A0MediaId.Artist(value.removePrefix(ARTIST_PREFIX))
        value?.startsWith(GENRE_PREFIX) == true ->
            A0MediaId.Genre(value.removePrefix(GENRE_PREFIX))
        value?.startsWith(PLAYLIST_PREFIX) == true ->
            value.removePrefix(PLAYLIST_PREFIX).toLongOrNull()?.let(A0MediaId::Playlist)
        else -> null
    }

    private const val SONG_PREFIX = "a0:song:"
    private const val ALBUM_PREFIX = "a0:album:"
    private const val ARTIST_PREFIX = "a0:artist:"
    private const val GENRE_PREFIX = "a0:genre:"
    private const val PLAYLIST_PREFIX = "a0:playlist:"
}
