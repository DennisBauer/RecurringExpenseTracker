package de.dbauer.expensetracker.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.dbauer.expensetracker.data.Tag

@Entity(tableName = "tags")
data class EntryTag(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "color") val color: String,
) {
    companion object {
        fun fromTag(tag: Tag): EntryTag = EntryTag(title = tag.title, color = tag.color)

        fun Tag.toEntryTag(): EntryTag = EntryTag.fromTag(this)
    }
}
