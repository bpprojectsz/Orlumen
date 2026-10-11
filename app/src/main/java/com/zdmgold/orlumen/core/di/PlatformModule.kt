package com.zdmgold.orlumen.core.di

import android.app.NotificationManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.projection.MediaProjectionManager
import android.media.session.MediaSessionManager
import android.os.PowerManager
import android.view.WindowManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object PlatformModule {
    @Provides
    fun provideAudioManager(@ApplicationContext context: Context): AudioManager =
        context.getSystemService(AudioManager::class.java)

    @Provides
    fun provideMediaProjectionManager(@ApplicationContext context: Context): MediaProjectionManager =
        context.getSystemService(MediaProjectionManager::class.java)

    @Provides
    fun provideMediaSessionManager(@ApplicationContext context: Context): MediaSessionManager =
        context.getSystemService(MediaSessionManager::class.java)

    @Provides
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager =
        context.getSystemService(NotificationManager::class.java)

    @Provides
    fun providePackageManager(@ApplicationContext context: Context): PackageManager = context.packageManager

    @Provides
    fun providePowerManager(@ApplicationContext context: Context): PowerManager =
        context.getSystemService(PowerManager::class.java)

    @Provides
    fun provideUsageStatsManager(@ApplicationContext context: Context): UsageStatsManager =
        context.getSystemService(UsageStatsManager::class.java)

    @Provides
    fun provideWindowManager(@ApplicationContext context: Context): WindowManager =
        context.getSystemService(WindowManager::class.java)
}
