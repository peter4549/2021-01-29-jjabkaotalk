package com.grand.duke.elliot.jjabkaotalk.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.grand.duke.elliot.jjabkaotalk.R
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object PhotoManager {

    const val REQUEST_CODE_IMAGE_CAPTURE = 1915
    const val REQUEST_CODE_IMAGE_PICKER = 1916

    var photoUri: Uri? = null

    @SuppressLint("QueryPermissionsNeeded")
    fun dispatchImageCaptureIntent(activity: Activity) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { imageCaptureIntent ->
            imageCaptureIntent.resolveActivity(activity.packageManager)?.also {
                val imageFile: File? = try {
                    createImageFile(activity)
                } catch (e: IOException) {
                    null
                }

                imageFile?.also {
                    val imageUri = FileProvider.getUriForFile(
                        activity,
                        activity.getString(R.string.file_provider_authorities),
                        it
                    )

                    photoUri = imageUri

                    imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    activity.startActivityForResult(
                        imageCaptureIntent,
                        REQUEST_CODE_IMAGE_CAPTURE
                    )
                }
            }
        }
    }

    fun dispatchImagePickerIntent(activity: Activity) {
        Intent(Intent.ACTION_PICK).also { imagePickerIntent ->
            imagePickerIntent.type = "image/*"
            activity.startActivityForResult(
                imagePickerIntent,
                REQUEST_CODE_IMAGE_PICKER
            )
        }
    }

    private fun createImageFile(context: Context): File? {
        return try {
            val timestamp: String = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
            val path = File(context.filesDir, "images")

            if (path.exists().not())
                path.mkdirs()

            File(path, "${timestamp}.jpg")
        } catch (e: FileNotFoundException) {
            Timber.e(e)
            null
        } catch (e: IOException) {
            Timber.e(e)
            null
        }
    }
}