package org.sitebay.android

import org.sitebay.android.modules.DaggerAppComponentTest

class WordPressTest : WordPress() {
    override fun initDaggerComponent() {
        mAppComponent = DaggerAppComponentTest.builder()
                .application(this)
                .build()
    }
}
