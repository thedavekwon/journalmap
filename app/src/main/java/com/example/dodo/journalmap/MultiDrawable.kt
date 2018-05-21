package com.example.dodo.journalmap

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

class MultiDrawable(drawables: List<Drawable>) : Drawable() {
    private val mDrawables = drawables

    override fun draw(canvas: Canvas?) {
        if (mDrawables.size == 1) {
            mDrawables[0].draw(canvas)
            return
        }
        val width = bounds.width()
        val height = bounds.height()

        canvas?.save()
        canvas?.clipRect(0, 0, width, height)

        if (mDrawables.size == 2 || mDrawables.size == 3) {
            canvas?.save()
            canvas?.clipRect(0, 0, width / 2, height)
            canvas?.translate(-width / 4f, 0f)
            mDrawables[0].draw(canvas)
            canvas?.restore()
        }

        if (mDrawables.size == 2) {
            canvas?.save()
            canvas?.clipRect(width / 2, 0, width, height)
            canvas?.translate(width / 4f, 0f)
            mDrawables[1].draw(canvas)
            canvas?.restore()
        } else {
            canvas?.save()
            canvas?.scale(.5f, .5f)
            canvas?.translate(width / 1.0f, 0f)
            mDrawables[1].draw(canvas)

            canvas?.translate(0f, height / 1.0f)
            mDrawables[2].draw(canvas)
            canvas?.restore()
        }

        if (mDrawables.size >= 4) {
            canvas?.save()
            canvas?.scale(.5f, .5f)
            mDrawables[0].draw(canvas)

            canvas?.translate(0f, height / 1.0f)
            mDrawables[3].draw(canvas)
            canvas?.restore()
        }

        canvas?.restore()
    }

    override fun setAlpha(alpha: Int) {
        return
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        return
    }

    override fun getOpacity(): Int {
        return PixelFormat.UNKNOWN
    }

}