package com.mnn.cameranote.di

import androidx.room.Room
import com.mnn.cameranote.data.local.AppDatabase
import com.mnn.cameranote.data.manager.FileStore
import com.mnn.cameranote.data.manager.GallerySaver
import com.mnn.cameranote.data.repository.MessageRepository
import com.mnn.cameranote.screens.camera.CameraViewModel
import com.mnn.cameranote.screens.messagedetail.MessageDetailViewModel
import com.mnn.cameranote.screens.messagelist.MessageViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** 命名限定符，避免以后再加 dispatcher 时按类型注入错对象。 */
const val IO_DISPATCHER = "io_dispatcher"

val appModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            AppDatabase.NAME,
        )
            .addMigrations(*AppDatabase.ALL_MIGRATIONS)
            .addCallback(AppDatabase.FOREIGN_KEY_CALLBACK)
            .build()
    }

    single { get<AppDatabase>().messageDao() }

    single<CoroutineDispatcher>(named(IO_DISPATCHER)) { Dispatchers.IO }

    single { FileStore(androidContext()) }
    single { GallerySaver(androidContext()) }

    single {
        MessageRepository(
            messageDao = get(),
            database = get(),
            fileStore = get(),
            ioDispatcher = get(named(IO_DISPATCHER)),
        )
    }

    viewModel { CameraViewModel(get(), get()) }
    viewModel { MessageViewModel(get()) }
    viewModel { MessageDetailViewModel(get(), get(), get(), get()) }
}
