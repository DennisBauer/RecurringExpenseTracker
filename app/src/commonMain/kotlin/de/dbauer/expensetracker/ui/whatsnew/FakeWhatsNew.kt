package de.dbauer.expensetracker.ui.whatsnew

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

class FakeWhatsNew() : IWhatsNew {
    override suspend fun shouldShowWhatsNew(): Boolean {
        return false
    }

    override suspend fun markAsShown() {}

    @Composable
    override fun WhatsNewUI(
        onDismissRequest: () -> Unit,
        modifier: Modifier,
    ) { }
}
