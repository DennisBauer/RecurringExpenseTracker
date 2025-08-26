package de.dbauer.expensetracker.ui.whatsnew

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import de.dbauer.expensetracker.model.datastore.IUserPreferencesRepository
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.whats_new_1
import recurringexpensetracker.app.generated.resources.whats_new_1_description
import recurringexpensetracker.app.generated.resources.whats_new_1_title
import recurringexpensetracker.app.generated.resources.whats_new_2
import recurringexpensetracker.app.generated.resources.whats_new_2_description
import recurringexpensetracker.app.generated.resources.whats_new_2_title

data class WhatsNewSlide(
    val image: DrawableResource,
    val title: StringResource,
    val description: StringResource,
)

const val WHATS_NEW_VERSION_CODE = 48

class WhatsNew(
    private val userPreferencesRepository: IUserPreferencesRepository,
) : IWhatsNew {
    override suspend fun shouldShowWhatsNew(): Boolean {
        val lastWhatsNewVersionShown = userPreferencesRepository.whatsNewVersionShown.get().first()
        return lastWhatsNewVersionShown < WHATS_NEW_VERSION_CODE
    }

    override suspend fun markAsShown() {
        userPreferencesRepository.whatsNewVersionShown.save(WHATS_NEW_VERSION_CODE)
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
