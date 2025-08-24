package de.dbauer.expensetracker.model.database

import de.dbauer.expensetracker.data.Tag

internal fun TagEntry.toTag(): Tag {
    return Tag(title, color, id)
}

internal fun Tag.toTagEntry(): TagEntry {
    return TagEntry(id = id, title = title, color = color)
}

internal fun List<Tag>.toTagEntries(): List<TagEntry> = map { it.toTagEntry() }

internal fun List<TagEntry>.toTags(): List<Tag> = map { it.toTag() }
