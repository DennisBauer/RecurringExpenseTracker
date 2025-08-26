package de.dbauer.expensetracker.ui.whatsnew

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface IWhatsNew {
    suspend fun shouldShowWhatsNew(): Boolean

    suspend fun markAsShown()

    @Composable
    fun WhatsNewUI(
        onDismissRequest: () -> Unit,
        modifier: Modifier = Modifier,
    )
}
