package id.apwdevs.app.catalogue.dao

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import id.apwdevs.app.catalogue.entity.FavoriteEntity
import id.apwdevs.app.catalogue.plugin.PublicContract

@Dao
interface FavoriteDao {
    companion object {
        private const val tableName = PublicContract.DatabaseContract.TABLE_FAVORITES
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addToFavorites(entity: List<FavoriteEntity>)

    @Query("SELECT * FROM $tableName")
    fun getAllFavorites(): List<FavoriteEntity>

    @Query("DELETE FROM $tableName WHERE id LIKE :id")
    fun removeAt(id: Int)

    @Query("SELECT * FROM $tableName WHERE contentType LIKE :displayType")
    fun getAsType(displayType: Int): List<FavoriteEntity>

    @Query("SELECT CASE WHEN EXISTS (SELECT * FROM $tableName WHERE id LIKE :id)THEN CAST(1 AS BIT)ELSE CAST(0 AS BIT) END AS BOOL")
    fun isAnyColumnIn(id: Int): Boolean

    @Query("SELECT * FROM $tableName WHERE id LIKE :id")
    fun getItemAt(id: Int): FavoriteEntity?

    @Query("SELECT * FROM $tableName")
    fun getAllByCursor(): Cursor?

    @Query("SELECT * FROM $tableName WHERE id LIKE :id")
    fun getItemAtByCursor(id: Int): Cursor?

    @Query("SELECT * FROM $tableName WHERE contentType LIKE :displayType")
    fun getAsTypeByCursor(displayType: Int): Cursor?
}