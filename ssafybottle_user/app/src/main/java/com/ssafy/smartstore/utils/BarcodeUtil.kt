package com.ssafy.smartstore.utils

import android.graphics.Bitmap
import android.util.TypedValue
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

private val WHITE: Int = 0xFFFFFFFF.toInt()
private val BLACK: Int = 0xFF000000.toInt()

fun Fragment.createBarcode(code: String) : Bitmap {
    val widthPx = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 390f,
        resources.displayMetrics
    )
    val heightPx = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 111f,
        resources.displayMetrics
    )
    val format: BarcodeFormat = BarcodeFormat.CODE_128
    val matrix: BitMatrix = MultiFormatWriter().encode(code, format, widthPx.toInt(), heightPx.toInt())
    val bitmap = createBitmap(matrix)
    return bitmap
}

fun createBitmap(matrix: BitMatrix): Bitmap {
    val width = matrix.width
    val height = matrix.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(x, y, if (matrix.get(x, y)) BLACK else WHITE)
        }
    }
    return bitmap
}