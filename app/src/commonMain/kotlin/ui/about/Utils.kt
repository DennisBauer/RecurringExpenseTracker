package ui.about

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri

fun openLinkInBrowser(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}
