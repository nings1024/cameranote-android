package com.mnn.cameranote

import android.app.Application
import com.mnn.cameranote.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class App() : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)   // 必须
            modules(appModule)         // 你的 module 列表
        }
    }
}
