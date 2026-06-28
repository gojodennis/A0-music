package elovaire.music.droidbeauty.app.data.playback

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import elovaire.music.droidbeauty.app.A0App

@OptIn(UnstableApi::class)
class A0MediaLibraryService : MediaLibraryService() {
    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo,
    ): MediaLibrarySession? {
        return (application as A0App)
            .container
            .playbackManager
            .mediaLibrarySession
    }
}
