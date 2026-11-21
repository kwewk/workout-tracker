package com.workout_tracker.nativeworkout

import com.facebook.react.BaseReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfo
import com.facebook.react.module.model.ReactModuleInfoProvider
import com.workout_tracker.NativeWorkoutSpec

class NativeWorkoutPackage : BaseReactPackage() {

    override fun getModule(name: String, reactContext: ReactApplicationContext): NativeModule? =
        if (name == NativeWorkoutSpec.NAME) {
            NativeWorkoutModule(reactContext)
        } else {
            null
        }

    override fun getReactModuleInfoProvider(): ReactModuleInfoProvider = ReactModuleInfoProvider {
        mapOf(
            NativeWorkoutSpec.NAME to
                    ReactModuleInfo(
                        name = NativeWorkoutSpec.NAME,
                        className = NativeWorkoutSpec.NAME,
                        canOverrideExistingModule = false,
                        needsEagerInit = false,
                        isCxxModule = false,
                        isTurboModule = true,
                    )
        )
    }
}
