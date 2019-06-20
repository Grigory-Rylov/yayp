package com.grishberg.backgroundyoutubeplayer.lifecycle

interface ActivityLifecycleDelegate {
    fun registerActivityLifeCycleAction(action: ActivityLifecycleAction)
    fun unregisterActivityLifeCycleAction(action: ActivityLifecycleAction)
    fun unbindService()
    fun bindService()
}