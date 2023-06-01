package com.pixel.pixology

import PreferenceManager
import android.app.Application

import androidx.appcompat.app.AppCompatDelegate


import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MainApplication:Application(){
    @Suppress("DEPRECATION")
    @Inject

    override fun onCreate() {
        super.onCreate()





        //dark mode disable
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

}