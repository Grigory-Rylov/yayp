package com.grishberg.yayp.presentation

import android.content.Context
import com.grishberg.yayp.R
import com.grishberg.yayp.domain.Config

class ConfigImpl(
    private val appContext: Context
) : Config {
    override val isTablet: Boolean
        get() = appContext.resources.getBoolean(R.bool.isTablet)
}