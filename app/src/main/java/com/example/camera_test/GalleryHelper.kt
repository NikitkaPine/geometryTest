package com.example.camera_test

import android.app.Activity
import android.content.Intent
import android.net.Uri

object GalleryHelper {

    const val PICK_SINGLE_IMAGE = 2001
    const val PICK_MULTIPLE_IMAGES = 2002

    fun openGallery(activity: Activity, single: Boolean) {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, !single)
        }
        activity.startActivityForResult(
            intent,
            if (single) PICK_SINGLE_IMAGE else PICK_MULTIPLE_IMAGES
        )
    }

    fun handleResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        onSingle: (Uri) -> Unit,
        onMultiple: (List<Uri>) -> Unit
    ) {
        if (resultCode != Activity.RESULT_OK || data == null) return

        when (requestCode) {
            PICK_SINGLE_IMAGE -> {
                data.data?.let { onSingle(it) }
            }
            PICK_MULTIPLE_IMAGES -> {
                val uris = mutableListOf<Uri>()
                val clipData = data.clipData
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        uris.add(clipData.getItemAt(i).uri)
                    }
                } else {
                    data.data?.let { uris.add(it) }
                }
                if (uris.isNotEmpty()) onMultiple(uris)
            }
        }
    }
}
