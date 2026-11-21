package com.workout_tracker

import android.app.Application
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactHost
import com.facebook.react.ReactNativeApplicationEntryPoint.loadReactNative
import com.facebook.react.defaults.DefaultReactHost.getDefaultReactHost
import com.workout_tracker.nativeworkout.NativeWorkoutPackage
import com.workout_tracker.nativeworkout.NotificationChannels

class MainApplication : Application(), ReactApplication {

  override val reactHost: ReactHost by lazy {
    getDefaultReactHost(
      context = applicationContext,
      packageList =
        PackageList(this).packages.apply {
          add(NativeWorkoutPackage())
        },
    )
  }

  override fun onCreate() {
    super.onCreate()
    NotificationChannels.ensureCreated(this)
    loadReactNative(this)
  }
}
