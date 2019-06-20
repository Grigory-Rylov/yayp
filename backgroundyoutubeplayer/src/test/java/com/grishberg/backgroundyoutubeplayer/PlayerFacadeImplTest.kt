package com.grishberg.backgroundyoutubeplayer

import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.MediaController
import com.grishberg.backgroundyoutubeplayer.lifecycle.ActivityLifecycleAction
import com.grishberg.backgroundyoutubeplayer.lifecycle.ActivityLifecycleDelegate
import com.nhaarman.mockitokotlin2.*
import org.junit.Test

class PlayerFacadeImplTest {
    lateinit var lifecycleAction: ActivityLifecycleAction
    val activityLifecycleDelegate = mock<ActivityLifecycleDelegate> {
        doAnswer { iom -> lifecycleAction = iom.getArgument(0) }
            .whenever(it).registerActivityLifeCycleAction(any())
    }

    val mediaController = mock<MediaControllerFacade>()
    val surfaceHolder = mock<SurfaceHolder>()
    val surfaceView = mock<SurfaceView> {
        on { holder } doReturn surfaceHolder
    }

    val mediaPlayerControl = mock<MediaController.MediaPlayerControl>()
    val playerService = mock<Player>{
        on{mediaController} doReturn mediaPlayerControl
    }
    val serviceProvider = mock<ServiceProvider> {
        on { getPlayer() } doReturn playerService
    }

    val playerFacadeImpl = PlayerFacadeImpl(
        activityLifecycleDelegate,
        mediaController,
        surfaceView
    )

    @Test
    fun `set mediaController when connected to service`() {
        lifecycleAction.onServiceConnected(serviceProvider)

        verify(mediaController).setMediaPlayer(mediaPlayerControl)
    }
}