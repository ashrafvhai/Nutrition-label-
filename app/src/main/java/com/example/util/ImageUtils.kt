package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageUtils {

    fun uriToScaledBitmap(context: Context, uri: Uri, maxDimension: Int = 1024): Bitmap? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            var inSampleSize = 1
            if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= maxDimension && (halfWidth / inSampleSize) >= maxDimension) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            val secondStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(secondStream, null, decodeOptions)
            secondStream?.close()

            if (bitmap != null) {
                rotateBitmapIfRequired(context, uri, bitmap)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun rotateBitmapIfRequired(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return bitmap
            val exif = ExifInterface(input)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            input.close()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }
        } catch (e: Exception) {
            bitmap
        }
    }

    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun saveBitmapToCache(context: Context, bitmap: Bitmap, fileName: String = "scan_${System.currentTimeMillis()}.jpg"): String {
        val file = File(context.cacheDir, fileName)
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        stream.flush()
        stream.close()
        return file.absolutePath
    }

    fun compressBitmap(bitmap: Bitmap, quality: Int = 85): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return stream.toByteArray()
    }
}
