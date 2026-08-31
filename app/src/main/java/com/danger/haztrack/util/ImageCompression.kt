package com.danger.haztrack.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.InputStream

private const val MAX_DIMENSION_PX = 1280
private const val JPEG_QUALITY = 85

/**
 * Downsamples and re-encodes an arbitrary picked image into a bounded JPEG before it's uploaded.
 * This is client-side defense-in-depth and a bandwidth saving only — the backend independently
 * re-validates, strips metadata from, and re-encodes every upload regardless (see
 * `docs/backend-image-upload-spec.md`). Reusable by any future feature that uploads images
 * (e.g. hazard reports), not just the profile picture.
 */
fun compressImageForUpload(inputStream: InputStream): ByteArray {
    val bytes = inputStream.use { it.readBytes() }

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)

    val decodeOptions = BitmapFactory.Options().apply {
        inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, MAX_DIMENSION_PX)
    }
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
        ?: error("Could not decode the picked image")

    return ByteArrayOutputStream().use { output ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
        bitmap.recycle()
        output.toByteArray()
    }
}

private fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int {
    var sampleSize = 1
    var currentWidth = width
    var currentHeight = height
    while (currentWidth / 2 >= maxDimension && currentHeight / 2 >= maxDimension) {
        currentWidth /= 2
        currentHeight /= 2
        sampleSize *= 2
    }
    return sampleSize
}
