package com.salizzai.app.data.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.salizzai.app.data.db.dao.AttachmentDao
import com.salizzai.app.data.db.dao.ConversationDao
import com.salizzai.app.data.db.dao.MessageDao
import com.salizzai.app.data.db.dao.UserDao
import com.salizzai.app.data.db.entity.Attachment
import com.salizzai.app.data.db.entity.Conversation
import com.salizzai.app.data.db.entity.Message
import com.salizzai.app.data.db.entity.User

/**
 * Local Room database — no server needed.
 * All data stored on-device.
 */
@Database(
    entities = [
        User::class,
        Conversation::class,
        Message::class,
        Attachment::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun attachmentDao(): AttachmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sali_zzai.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
