package com.hydr.odeliver

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

enum class DeliveryStatus {
    PENDING,
    ON_THE_WAY,
    DELIVERED,
    CANCELLED
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): java.util.Date? = value?.let { java.util.Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: java.util.Date?): Long? = date?.time

    @TypeConverter
    fun fromStatus(status: DeliveryStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): DeliveryStatus = DeliveryStatus.valueOf(value)
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val shopName: String = "",
    val address: String = "",
    val bio: String = "",
    val budget: Double = 0.0
)

@Entity(
    tableName = "deliveries",
    indices = [Index(value = ["status"]), Index(value = ["uid"])]
)
data class DeliveryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uid: String = "", // Added to tie deliveries to users
    val timestamp: Long = System.currentTimeMillis(),
    val time: String,
    val date: String = "",
    val itemName: String,
    val customerName: String,
    val cost: Double,
    val numberOfProducts: Int = 1,
    val isPricePerItem: Boolean = false,
    val isOutgoing: Boolean = true,
    val notes: String = "",
    val status: DeliveryStatus = DeliveryStatus.PENDING,
    val wasLate: Boolean = false
)

@Entity(
    tableName = "sales",
    indices = [Index(value = ["uid"])]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uid: String,
    val timestamp: Long = System.currentTimeMillis(),
    val customerName: String,
    val productNumber: String,
    val price: Double,
    val quantity: String, // "one", "bulk", or specific amount
    val date: String,
    val time: String
)

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE uid = :uid")
    fun getUserById(uid: String): Flow<UserEntity?>

    @Upsert
    suspend fun upsertUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun clearAllUsers()
}

@Dao
interface DeliveryDao {
    @Query("SELECT * FROM deliveries WHERE uid = :uid ORDER BY timestamp DESC")
    fun getDeliveriesByUser(uid: String): Flow<List<DeliveryEntity>>

    @Query("SELECT * FROM deliveries ORDER BY timestamp DESC")
    fun getAllDeliveries(): Flow<List<DeliveryEntity>>

    @Upsert
    suspend fun upsertDelivery(delivery: DeliveryEntity)

    @Query("DELETE FROM deliveries WHERE id = :id")
    suspend fun deleteDeliveryById(id: Int)

    @Query("DELETE FROM deliveries")
    suspend fun clearAllDeliveries()
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales WHERE uid = :uid ORDER BY timestamp DESC")
    fun getSalesByUser(uid: String): Flow<List<SaleEntity>>

    @Upsert
    suspend fun upsertSale(sale: SaleEntity)

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSaleById(id: Int)
}

@Database(entities = [UserEntity::class, DeliveryEntity::class, SaleEntity::class], version = 9, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun deliveryDao(): DeliveryDao
    abstract fun saleDao(): SaleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "odeliver_database"
                )
                    .fallbackToDestructiveMigration() // For development simplicity
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
