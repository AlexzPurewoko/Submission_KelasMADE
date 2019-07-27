package id.apwdevs.app.catalogue.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import id.apwdevs.app.catalogue.model.GenreModel
import id.apwdevs.app.catalogue.plugin.PublicContract

@Dao
interface GenreDao {
    companion object {
        const val table = PublicContract.DatabaseContract.TABLE_GENRES
    }

    @Insert(entity = GenreModel::class, onConflict = OnConflictStrategy.REPLACE)
    fun addAll(values: List<GenreModel>)

    @Query("SELECT name from $table WHERE id LIKE :id")
    fun getGenreNameAt(id: Int): String

    @Query("SELECT count(1) from $table")
    fun size(): Int

    @Query("SELECT * FROM $table")
    fun getAll(): List<GenreModel>
}