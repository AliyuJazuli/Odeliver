package com.hydr.odeliver.di

import android.content.Context
import com.hydr.odeliver.AppDatabase
import com.hydr.odeliver.DeliveryDao
import com.hydr.odeliver.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideDeliveryDao(database: AppDatabase): DeliveryDao {
        return database.deliveryDao()
    }
}
