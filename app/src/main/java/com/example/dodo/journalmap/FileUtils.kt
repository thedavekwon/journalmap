package com.example.dodo.journalmap

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {
    fun storeImage(context: Context?, image: Bitmap): String {
        val pictureFile = getOutputMediaFile(context)
        if (pictureFile == null) {
            Log.d("permission", "check permission")
            return ""
        }
        try {
            val fos = FileOutputStream(pictureFile)
            image.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return pictureFile.path
    }

    private fun getOutputMediaFile(context: Context?): File? {
        val mediaStorageDir = File(Environment.getExternalStorageDirectory().path
                + "/Android/data/"
                + context?.getPackageName()
                + "/Files")
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdir()) {
                return null
            }
        }
        val timeStamp = SimpleDateFormat("ddMMyyyy_HHmmss").format(Calendar.getInstance().time)
        val mediaFile = File(mediaStorageDir.path + File.separator + "MC" + timeStamp + ".png")
        return mediaFile
    }
}