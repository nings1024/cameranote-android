package com.mnn.cameranote.di

import androidx.room.Room
import com.mnn.cameranote.data.database.AppDatabase
import com.mnn.cameranote.data.database.repository.MessageRepository
import com.mnn.cameranote.screens.camera.CameraViewModel
import com.mnn.cameranote.screens.messagedetail.MessageDetailViewModel
import com.mnn.cameranote.screens.messagelist.MessageViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// di/AppModule.kt
val appModule = module {
// 1. 数据库单例
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "camera_note_db"
        ).build()
    }
    single { get<AppDatabase>().messageDao() }
    single { Dispatchers.IO }

    // 2. Dao 实例
    single { get<AppDatabase>().messageDao() }

    // 3. Repository 实例
    single { MessageRepository(get(),get()) }

    // 4. ViewModel 实例
    viewModel { CameraViewModel(get()) }


    viewModel { MessageViewModel(get()) }

    viewModel { MessageDetailViewModel(get(),get()) }

}

