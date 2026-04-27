package com.stephenbrines.packlight.di

import android.content.Context
import androidx.room.Room
import com.stephenbrines.packlight.data.db.PackLightDatabase
import com.stephenbrines.packlight.data.db.dao.GearItemDao
import com.stephenbrines.packlight.data.db.dao.PackListDao
import com.stephenbrines.packlight.data.db.dao.ResupplyDao
import com.stephenbrines.packlight.data.db.dao.TripDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PackLightDatabase =
        Room.databaseBuilder(context, PackLightDatabase::class.java, "packlight.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideGearItemDao(db: PackLightDatabase): GearItemDao = db.gearItemDao()
    @Provides fun provideTripDao(db: PackLightDatabase): TripDao = db.tripDao()
    @Provides fun providePackListDao(db: PackLightDatabase): PackListDao = db.packListDao()
    @Provides fun provideResupplyDao(db: PackLightDatabase): ResupplyDao = db.resupplyDao()
    @Provides fun provideWeightSnapshotDao(db: PackLightDatabase): com.stephenbrines.packlight.data.db.dao.WeightSnapshotDao = db.weightSnapshotDao()
}
