package de.dbauer.expensetracker.shared.ui.whatsnew

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import de.dbauer.expensetracker.shared.model.datastore.IUserPreferencesRepository
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import recurringexpensetracker.shared.generated.resources.Res
import recurringexpensetracker.shared.generated.resources.whats_new_1
import recurringexpensetracker.shared.generated.resources.whats_new_1_description
import recurringexpensetracker.shared.generated.resources.whats_new_1_title
import recurringexpensetracker.shared.generated.resources.whats_new_2
import recurringexpensetracker.shared.generated.resources.whats_new_2_description
import recurringexpensetracker.shared.generated.resources.whats_new_2_title

data class WhatsNewSlide(
    val image: DrawableResource,
    val title: StringResource,
    val description: StringResource,
)

const val WHATS_NEW_VERSION = 1

class WhatsNew(
    private val userPreferencesRepository: IUserPreferencesRepository,
) : IWhatsNew {
    override suspend fun shouldShowWhatsNew(): Boolean {
        val lastWhatsNewVersionShown = userPreferencesRepository.whatsNewVersionShown.get().first()
        return lastWhatsNewVersionShown < WHATS_NEW_VERSION
    }

    override suspend fun markAsShown() {
        userPreferencesRepository.whatsNewVersionShown.save(WHATS_NEW_VERSION)
    }

    @Composable
    override fun WhatsNewUI(
        onDismissRequest: () -> Unit,
        modifier: Modifier,
    ) {
        val whatsNewSlides =
            remember {
                listOf(
                    WhatsNewSlide(
                        image = Res.drawable.whats_new_1,
                        title = Res.string.whats_new_1_title,
                        description = Res.string.whats_new_1_description,
                    ),
                    WhatsNewSlide(
                        image = Res.drawable.whats_new_2,
                        title = Res.string.whats_new_2_title,
                        description = Res.string.whats_new_2_description,
                    ),
                )
            }

        WhatsNewUI(
            whatsNewSlides = whatsNewSlides,
            modifier = modifier,
            onDismissRequest = onDismissRequest,
        )
    }
}
