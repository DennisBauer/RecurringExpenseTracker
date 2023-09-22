package de.erzock.subscriptions.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import de.erzock.subscriptions.toValueString
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

class MainActivityViewModel : ViewModel() {
    private val _subscriptionsData = mutableStateListOf<SubscriptionData>(
        SubscriptionData(
            name = "Netflix",
            description = "My Netflix description",
            priceValue = 9.99f,
        ),
        SubscriptionData(
            name = "Disney Plus",
            description = "My Disney Plus description",
            priceValue = 5f,
        ),
        SubscriptionData(
            name = "Amazon Prime",
            description = "My Disney Plus description",
            priceValue = 7.95f,
        ),
    )
    val subscriptionData: ImmutableList<SubscriptionData>
        get() = _subscriptionsData.toImmutableList()

    private var _montlyPrice by mutableStateOf("")
    val monthlyPrice: String
        get() = _montlyPrice

    init {
        var price = 0f
        _subscriptionsData.forEach {
            price += it.priceValue
        }
        _montlyPrice = "${price.toValueString()} €" // TODO: Make currency dynamic
    }
}