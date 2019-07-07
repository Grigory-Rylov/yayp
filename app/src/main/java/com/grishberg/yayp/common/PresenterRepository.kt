package com.grishberg.yayp.common

import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PresenterRepository {

    companion object {
        private val presenterMap = ConcurrentHashMap<String, Any>()

        internal fun putPresenter(clazz: Class<*>, presenter: Any) {
            presenterMap[clazz.name] = presenter
        }

        internal fun removePresenter(clazz: Class<*>) {
            presenterMap.remove(clazz.name)
        }
    }
}