package com.community.bitinstaller.di

import android.content.Context
import com.community.bitinstaller.repository.AppRepository
import com.community.bitinstaller.repository.AppRepositoryImpl
import com.community.bitinstaller.repository.GitHubApiServiceFactory
import com.community.bitinstaller.repository.GitHubApiServiceFactoryImpl
import com.community.bitinstaller.utils.ConfigLoader
import com.community.bitinstaller.utils.FileDownloader
import com.community.bitinstaller.utils.ShizukuHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideConfigLoader(@ApplicationContext context: Context): ConfigLoader =
        ConfigLoader(context)

    @Provides
    @Singleton
    fun provideGitHubApiServiceFactory(): GitHubApiServiceFactory =
        GitHubApiServiceFactoryImpl()

    @Provides
    @Singleton
    fun provideAppRepository(
        configLoader: ConfigLoader,
        apiServiceFactory: GitHubApiServiceFactory
    ): AppRepository =
        AppRepositoryImpl(configLoader, apiServiceFactory)

    @Provides
    fun provideFileDownloader(): FileDownloader =
        FileDownloader()

    @Provides
    fun provideShizukuHelper(): ShizukuHelper =
        ShizukuHelper()
}
