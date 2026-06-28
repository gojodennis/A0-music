package elovaire.music.droidbeauty.app.data.playback.library

import androidx.media3.common.MediaItem
import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.search.NormalizedSearchQuery
import elovaire.music.droidbeauty.app.domain.search.normalizeSearchText
import elovaire.music.droidbeauty.app.domain.search.searchAlbumsForPicker
import elovaire.music.droidbeauty.app.domain.search.searchArtistsForPicker
import elovaire.music.droidbeauty.app.domain.search.searchPlaylists
import elovaire.music.droidbeauty.app.domain.search.searchSongsForPicker

internal class A0MediaTree(
    private val libraryRepository: LibraryRepository,
    private val preferenceStore: PreferenceStore,
) {
    fun rootChildren(): List<MediaItem> {
        val snapshot = snapshot()
        return when {
            !snapshot.permissionGranted -> listOf(A0MediaItems.permissionRequiredInfo())
            snapshot.songs.isEmpty() -> listOf(A0MediaItems.emptyLibraryInfo())
            else -> listOf(
                A0MediaItems.recentlyAddedRoot(),
                A0MediaItems.favoritesRoot(),
                A0MediaItems.albumsRoot(),
                A0MediaItems.artistsRoot(),
                A0MediaItems.playlistsRoot(),
                A0MediaItems.songsRoot(),
            )
        }
    }

    fun childrenOf(id: A0MediaId): List<MediaItem> {
        val snapshot = snapshot()
        if (!snapshot.permissionGranted) {
            return if (id == A0MediaId.Root) listOf(A0MediaItems.permissionRequiredInfo()) else emptyList()
        }
        if (snapshot.songs.isEmpty()) {
            return if (id == A0MediaId.Root) listOf(A0MediaItems.emptyLibraryInfo()) else emptyList()
        }
        return when (id) {
            A0MediaId.Root -> rootChildren()
            A0MediaId.PermissionRequired,
            A0MediaId.EmptyLibrary,
            -> emptyList()
            A0MediaId.Songs -> snapshot.songs.sortedSongsByTitle().map(A0MediaItems::song)
            A0MediaId.Albums -> snapshot.albums.sortedAlbumsByTitle().map(A0MediaItems::album)
            A0MediaId.Artists -> snapshot.artistNames().map(A0MediaItems::artist)
            A0MediaId.Genres -> snapshot.genreNames().map(A0MediaItems::genre)
            A0MediaId.Playlists -> snapshot.playlists
                .filter { it.songIds.isNotEmpty() }
                .sortedBy { it.name.lowercase() }
                .map(A0MediaItems::playlist)
            A0MediaId.Favorites -> snapshot.favoriteSongs().sortedSongsByTitle().map(A0MediaItems::song)
            A0MediaId.RecentlyAdded -> snapshot.recentlyAddedSongs().map(A0MediaItems::song)
            is A0MediaId.Song -> emptyList()
            is A0MediaId.Album -> snapshot.albums.firstOrNull { it.id == id.albumId }
                ?.songs.orEmpty()
                .map(A0MediaItems::song)
            is A0MediaId.Artist -> snapshot.songs
                .filter { it.artist.equals(A0MediaIds.decodeName(id.encodedName), ignoreCase = true) }
                .sortedSongsForContext()
                .map(A0MediaItems::song)
            is A0MediaId.Genre -> snapshot.songs
                .filter { it.genre.equals(A0MediaIds.decodeName(id.encodedName), ignoreCase = true) }
                .sortedSongsForContext()
                .map(A0MediaItems::song)
            is A0MediaId.Playlist -> snapshot.playlistSongs(id.playlistId).map(A0MediaItems::song)
        }
    }

    fun item(mediaId: String): MediaItem? {
        val parsed = A0MediaIds.parse(mediaId) ?: return null
        val snapshot = snapshot()
        return when (parsed) {
            A0MediaId.Root -> A0MediaItems.root()
            A0MediaId.PermissionRequired -> A0MediaItems.permissionRequiredInfo()
            A0MediaId.EmptyLibrary -> A0MediaItems.emptyLibraryInfo()
            A0MediaId.Songs -> A0MediaItems.songsRoot()
            A0MediaId.Albums -> A0MediaItems.albumsRoot()
            A0MediaId.Artists -> A0MediaItems.artistsRoot()
            A0MediaId.Genres -> A0MediaItems.genresRoot()
            A0MediaId.Playlists -> A0MediaItems.playlistsRoot()
            A0MediaId.Favorites -> A0MediaItems.favoritesRoot()
            A0MediaId.RecentlyAdded -> A0MediaItems.recentlyAddedRoot()
            is A0MediaId.Song -> snapshot.songs.firstOrNull { it.id == parsed.songId }
                ?.let(A0MediaItems::song)
            is A0MediaId.Album -> snapshot.albums.firstOrNull { it.id == parsed.albumId }
                ?.let(A0MediaItems::album)
            is A0MediaId.Artist -> A0MediaItems.artist(A0MediaIds.decodeName(parsed.encodedName))
            is A0MediaId.Genre -> A0MediaItems.genre(A0MediaIds.decodeName(parsed.encodedName))
            is A0MediaId.Playlist -> snapshot.playlists.firstOrNull { it.id == parsed.playlistId }
                ?.let(A0MediaItems::playlist)
        }
    }

    fun resolvePlayableQueue(mediaId: String): ResolvedPlayableQueue? {
        val parsed = A0MediaIds.parse(mediaId) ?: return null
        val snapshot = snapshot()
        if (!snapshot.permissionGranted || snapshot.songs.isEmpty()) return null
        return when (parsed) {
            A0MediaId.Songs -> snapshot.songs.sortedSongsByTitle().toQueue("Songs")
            A0MediaId.Favorites -> snapshot.favoriteSongs().sortedSongsByTitle().toQueue("Favorites")
            A0MediaId.RecentlyAdded -> snapshot.recentlyAddedSongs().toQueue("Recently added")
            is A0MediaId.Song -> {
                val song = snapshot.songs.firstOrNull { it.id == parsed.songId } ?: return null
                val album = snapshot.albums.firstOrNull { it.id == song.albumId }
                if (album != null) {
                    ResolvedPlayableQueue(song, album.songs, album.title, null)
                } else {
                    ResolvedPlayableQueue(song, snapshot.songs.sortedSongsByTitle(), song.album, null)
                }
            }
            is A0MediaId.Album -> {
                val album = snapshot.albums.firstOrNull { it.id == parsed.albumId } ?: return null
                album.songs.toQueue(album.title)
            }
            is A0MediaId.Artist -> {
                val artist = A0MediaIds.decodeName(parsed.encodedName)
                snapshot.songs
                    .filter { it.artist.equals(artist, ignoreCase = true) }
                    .sortedSongsForContext()
                    .toQueue(artist)
            }
            is A0MediaId.Genre -> {
                val genre = A0MediaIds.decodeName(parsed.encodedName)
                snapshot.songs
                    .filter { it.genre.equals(genre, ignoreCase = true) }
                    .sortedSongsForContext()
                    .toQueue(genre)
            }
            is A0MediaId.Playlist -> {
                val playlist = snapshot.playlists.firstOrNull { it.id == parsed.playlistId } ?: return null
                snapshot.playlistSongs(playlist.id).toQueue(playlist.name, playlist.id)
            }
            A0MediaId.PermissionRequired,
            A0MediaId.EmptyLibrary,
            A0MediaId.Root,
            A0MediaId.Albums,
            A0MediaId.Artists,
            A0MediaId.Genres,
            A0MediaId.Playlists,
            -> null
        }
    }

    fun search(query: String, limit: Int = SEARCH_RESULT_LIMIT): List<MediaItem> {
        val normalizedQuery = NormalizedSearchQuery.from(query)
        if (normalizedQuery.value.isBlank()) return emptyList()
        val snapshot = snapshot()
        if (!snapshot.permissionGranted || snapshot.songs.isEmpty()) return emptyList()
        val artistRows = snapshot.artistNames().map { name ->
            NamedSongs(name = name, songs = snapshot.songs.filter { it.artist.equals(name, ignoreCase = true) })
        }
        val genreRows = snapshot.genreNames().map { name ->
            NamedSongs(name = name, songs = snapshot.songs.filter { it.genre.equals(name, ignoreCase = true) })
        }
        val exactAndStrongTitleSongs = snapshot.songs
            .filter {
                val normalizedTitle = normalizeSearchText(it.title)
                normalizedTitle == normalizedQuery.value || normalizedTitle.startsWith(normalizedQuery.value)
            }
            .sortedSongsByTitle()
        val broaderSongs = searchSongsForPicker(snapshot.songs, normalizedQuery)
        return mutableListOf<MediaItem>().apply {
            addDistinctItems(exactAndStrongTitleSongs, limit, A0MediaItems::song)
            addDistinctItems(searchAlbumsForPicker(snapshot.albums, normalizedQuery), limit, A0MediaItems::album)
            addDistinctItems(
                searchArtistsForPicker(
                    artists = artistRows,
                    query = normalizedQuery,
                    name = NamedSongs::name,
                    songs = NamedSongs::songs,
                    songCount = { it.songs.size },
                ),
                limit,
            ) { A0MediaItems.artist(it.name) }
            addDistinctItems(
                searchPlaylists(snapshot.playlists.filter { it.songIds.isNotEmpty() }, normalizedQuery),
                limit,
                A0MediaItems::playlist,
            )
            addDistinctItems(
                searchArtistsForPicker(
                    artists = genreRows,
                    query = normalizedQuery,
                    name = NamedSongs::name,
                    songs = NamedSongs::songs,
                    songCount = { it.songs.size },
                ),
                limit,
            ) { A0MediaItems.genre(it.name) }
            addDistinctItems(broaderSongs, limit, A0MediaItems::song)
        }
    }

    private fun snapshot(): MediaTreeSnapshot {
        val content = libraryRepository.contentState.value
        val scan = libraryRepository.scanState.value
        return MediaTreeSnapshot(
            permissionGranted = scan.permissionGranted,
            songs = content.songs,
            albums = content.albums,
            playlists = preferenceStore.playlists.value,
            favoriteSongIds = preferenceStore.favoriteSongIds.value.toSet(),
        )
    }

    private fun List<Song>.toQueue(
        sourceLabel: String,
        sourcePlaylistId: Long? = null,
    ): ResolvedPlayableQueue? {
        val startSong = firstOrNull() ?: return null
        return ResolvedPlayableQueue(startSong, this, sourceLabel, sourcePlaylistId)
    }

    private fun List<Song>.sortedSongsByTitle(): List<Song> = sortedBy { it.title.lowercase() }
    private fun List<Song>.sortedSongsForContext(): List<Song> = sortedWith(
        compareBy<Song>(
            { it.album.lowercase() },
            { it.discNumber },
            { it.trackNumber },
            { it.title.lowercase() },
            { it.id },
        ),
    )
    private fun List<Album>.sortedAlbumsByTitle(): List<Album> = sortedBy { it.title.lowercase() }

    private fun MutableList<MediaItem>.addDistinct(item: MediaItem) {
        if (none { it.mediaId == item.mediaId }) {
            add(item)
        }
    }

    private inline fun <T> MutableList<MediaItem>.addDistinctItems(
        items: List<T>,
        limit: Int,
        itemFactory: (T) -> MediaItem,
    ) {
        for (item in items) {
            if (size >= limit) return
            addDistinct(itemFactory(item))
        }
    }

    private data class MediaTreeSnapshot(
        val permissionGranted: Boolean,
        val songs: List<Song>,
        val albums: List<Album>,
        val playlists: List<Playlist>,
        val favoriteSongIds: Set<Long>,
    ) {
        fun favoriteSongs(): List<Song> = songs.filter { it.id in favoriteSongIds }
        fun recentlyAddedSongs(): List<Song> = songs.sortedByDescending(Song::dateAddedSeconds)
        fun artistNames(): List<String> = songs
            .map { it.artist.ifBlank { UNKNOWN_ARTIST } }
            .distinct()
            .sortedBy(String::lowercase)
        fun genreNames(): List<String> = songs
            .map { it.genre.ifBlank { UNKNOWN_GENRE } }
            .distinct()
            .sortedBy(String::lowercase)
        fun playlistSongs(playlistId: Long): List<Song> {
            val playlist = playlists.firstOrNull { it.id == playlistId } ?: return emptyList()
            val songsById = songs.associateBy(Song::id)
            return playlist.songIds.mapNotNull(songsById::get)
        }
    }

    private data class NamedSongs(
        val name: String,
        val songs: List<Song>,
    )

    private companion object {
        const val SEARCH_RESULT_LIMIT = 50
        const val UNKNOWN_ARTIST = "Unknown Artist"
        const val UNKNOWN_GENRE = "Unknown Genre"
    }
}

internal data class ResolvedPlayableQueue(
    val startSong: Song,
    val queue: List<Song>,
    val sourceLabel: String,
    val sourcePlaylistId: Long?,
)
