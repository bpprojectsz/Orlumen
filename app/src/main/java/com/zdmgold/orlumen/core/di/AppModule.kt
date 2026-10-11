package com.zdmgold.orlumen.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.zdmgold.orlumen.core.database.ConfigDao
import com.zdmgold.orlumen.core.database.OrlumenDatabase
import com.zdmgold.orlumen.core.database.PaletteDao
import com.zdmgold.orlumen.platform.monetization.BillingAdapter
import com.zdmgold.orlumen.platform.monetization.BillingGateway
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private const val PREFERENCES_FILE = "orlumen_prefs"

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OrlumenDatabase =
        Room.databaseBuilder(context, OrlumenDatabase::class.java, OrlumenDatabase.NAME).build()

    @Provides
    fun provideConfigDao(database: OrlumenDatabase): ConfigDao = database.configDao()

    @Provides
    fun providePaletteDao(database: OrlumenDatabase): PaletteDao = database.paletteDao()

    @Provides
    fun provideBillingGateway(adapter: BillingAdapter): BillingGateway = adapter

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile(PREFERENCES_FILE) }
}
