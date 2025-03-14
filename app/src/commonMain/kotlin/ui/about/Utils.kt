package ui.about

<<<<<<< HEAD
<<<<<<< HEAD
=======
import android.app.Activity
>>>>>>> 4ac3d00 (feat: add about page)
=======
import android.app.Activity
>>>>>>> e7ee9921430f1ef42eab5b2143d0765976a01cf2
import android.content.Context
import android.content.Intent
import android.net.Uri

fun openLinkInBrowser(context: Context, url: String) {
<<<<<<< HEAD
<<<<<<< HEAD
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(url)
    )
=======
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
>>>>>>> 4ac3d00 (feat: add about page)
=======
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
>>>>>>> e7ee9921430f1ef42eab5b2143d0765976a01cf2
    context.startActivity(intent)
}
