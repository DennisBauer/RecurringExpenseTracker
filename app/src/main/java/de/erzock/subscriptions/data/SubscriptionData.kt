package de.erzock.subscriptions.data

import de.erzock.subscriptions.toValueString

data class SubscriptionData(
    val name: String,
    val description: String,
    val priceValue: Float,
) {
    val priceString = "${priceValue.toValueString()} €" // TODO: Make currency dynamic
}