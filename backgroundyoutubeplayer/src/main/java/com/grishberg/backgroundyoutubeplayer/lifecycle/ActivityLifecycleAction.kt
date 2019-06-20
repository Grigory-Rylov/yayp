package com.grishberg.backgroundyoutubeplayer.lifecycle

import com.grishberg.backgroundyoutubeplayer.ServiceProvider

interface ActivityLifecycleAction {
    fun onStarted()
    fun onStop()
    fun onServiceConnected(provider: ServiceProvider)
    fun onServiceDisconnected()
}