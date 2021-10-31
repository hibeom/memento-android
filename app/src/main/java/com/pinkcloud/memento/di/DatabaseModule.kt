package com.pinkcloud.memento.di

import android.content.Context
import com.pinkcloud.memento.database.MemoDatabase
import com.pinkcloud.memento.database.MemoDatabaseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//In Kotlin, modules that only contain @Provides functions can be object classes. This way, providers get optimized and almost in-lined in generated code.
@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    // The return type of the @Provides-annotated function tells Hilt the binding type, the type that the function provides instances of.. The function parameters are the dependencies of that type.
    @Provides
    fun provideMemoDatabaseDao(database: MemoDatabase): MemoDatabaseDao {
        return database.memoDatabaseDao
    }

    @Provides
    fun provideMemoDatabase(@ApplicationContext context: Context): MemoDatabase {
        return MemoDatabase.getInstance(context)
    }
}