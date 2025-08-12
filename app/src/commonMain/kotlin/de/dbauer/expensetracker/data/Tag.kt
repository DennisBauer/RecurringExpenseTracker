package de.dbauer.expensetracker.data

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Tag(val title: String, val color: Long, val id: Int = Uuid.random().hashCode())
