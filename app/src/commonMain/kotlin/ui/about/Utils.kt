package ui.about

<<<<<<< HEAD
=======
import android.app.Activity
>>>>>>> 4ac3d00 (feat: add about page)
import android.content.Context
import android.content.Intent
import android.net.Uri

fun openLinkInBrowser(context: Context, url: String) {
<<<<<<< HEAD
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(url)
    )
=======
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
>>>>>>> 4ac3d00 (feat: add about page)
    context.startActivity(intent)
}
