package id.apwdevs.app.catalogue.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import id.apwdevs.app.catalogue.dao.FavoriteDao
import id.apwdevs.app.catalogue.dao.GenreDao
import id.apwdevs.app.catalogue.entity.FavoriteEntity
import id.apwdevs.app.catalogue.model.GenreModel
import id.apwdevs.app.catalogue.plugin.PublicContract

@Database(entities = [FavoriteEntity::class, GenreModel::class], version = 1)
abstract class FavoriteDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun genreDao(): GenreDao

    companion object {
        @Volatile
        private var instance: FavoriteDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): FavoriteDatabase =
            instance ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FavoriteDatabase::class.java,
                    PublicContract.DatabaseContract.DATABASE_FAVORITE_NAME
                ).build()
                this.instance = instance
                return@synchronized instance
            }
    }
}