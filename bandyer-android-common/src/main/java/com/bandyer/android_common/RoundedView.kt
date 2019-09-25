package com.bandyer.android_common

import android.graphics.*
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.RequiresApi
import kotlin.math.min

/**
 * Generic interface referring to roundable objects
 */
interface Roundable

/**
 * View that can be rounded
 *
 * !--- IMPORTANT ---!
 * On devices below LOLLIPOP the round can not be applied to backgrounds!!
 *
 * To be used in java you need to implement all the abstract methods and forward them by RoundedView.DefaultImpl.methods
 *
 *
 * For example:
 *
 *```
 * public class TestButton extends AppCompatImageButton implements RoundedView {
 *
 *      public TestButton(Context context) {
 *           this(context, null);
 *      }
 *
 *      public TestButton(Context context, AttributeSet attrs) {
 *          this(context, attrs, 0);
 *      }
 *
 *      public TestButton(Context context, AttributeSet attrs, int defStyleAttr) {
 *          super(context, attrs, defStyleAttr);
 *          round(this, true);
 *      }
 *
 *      @Override
 *      protected void onDraw(Canvas canvas) {
 *          setRoundClip(this, canvas);
 *          super.onDraw(canvas); // IMPORTANT call this after setRoundClip
 *      }
 *
 *      @Override
 *      public <T extends View & Roundable> void setRoundClip(T $receiver, Canvas canvas) {
 *          RoundedView.DefaultImpls.setRoundClip(this, this, canvas);
 *      }
 *
 *      @Override
 *      public <T extends View & Roundable> void round(T $receiver, boolean rounded) {
 *          RoundedView.DefaultImpls.round(this, this, rounded);
 *      }
 *
 *      @Override
 *      public <T extends View & Roundable> void setCornerRadius(T $receiver, float radius) {
 *          RoundedView.DefaultImpls.setCornerRadius(this, this, radius);
 *      }
 *
 *      @Override
 *      public <T extends View & Roundable> void round(boolean rounded) {
 *          RoundedView.DefaultImpls.round(this, rounded);
 *      }
 *
 *      @Override
 *      public <T extends View & Roundable> void setCornerRadius(float radius) {
 *          RoundedView.DefaultImpls.setCornerRadius(this, radius);
 *      }
 *```
 *
 * @author kristiyan
 */
interface RoundedView : Roundable {

    /**
     * Method to call in onDraw(canvas) of the view implementing this interface
     * Remember to call super.onDraw(canvas); after it!
     *
     * @suppress
     * @receiver View class implementing this interface
     * @param canvas Canvas to modify
     */
    fun <T> T.setRoundClip(canvas: Canvas?) where T : View, T : Roundable {
        if (radius < 0)
            return
        clipPath?.let { canvas?.clipPath(it) }
    }

    /**
     * Enable circular clipping on canvas for RoundedView
     *
     * @param rounded enable or disable circular clipping
     */
    fun <T> T.round(rounded: Boolean) where T : View, T : Roundable {
        this@round.isRounded = rounded
        post {
            setCornerRadius(if (!rounded) 0f else min(width, height) / 2f)
        }
    }

    /**
     * Enable radius clipping on canvas for RoundedView
     *
     * @param radius radius value in pixels
     */
    fun <T> T.setCornerRadius(radius: Float) where T : View, T : Roundable {
        this.radius = radius
        addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            calculateClippingRectAndApplyClipPath()
        }
        setOutlineRadius(radius)
    }

    /**
     * Enable circular clipping on canvas for RoundedView
     *
     * @param rounded enable or disable circular clipping
     */
    fun <T> round(rounded: Boolean) where T : View, T : Roundable {
        (this as T).round(rounded)
    }

    /**
     * Enable radius clipping on canvas for RoundedView
     *
     * @param radius radius value in pixels
     */
    fun <T> setCornerRadius(radius: Float) where T : View, T : Roundable {
        (this as T).setCornerRadius(radius)
    }

}

/**
 * Property specifying the rounding factor
 */
private var <T> T.radius: Float where T : View, T : Roundable  by FieldProperty { -1f }

/**
 * Make the view rounded like a circle
 */
private var <T> T.isRounded: Boolean where T : View, T : Roundable  by FieldProperty { false }

/**
 * Radius based on measured width/height
 */
private val <T> T.measuredRadius: Float where T : android.view.View, T : Roundable
    get() = if (isRounded) min(width, height) / 2f else radius

/**
 * Enable clipping for VideoStreamView
 * @param radius radius value in pixels
 */
private fun <T> T.setOutlineRadius(radius: Float) where T : View, T : Roundable {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        setOutlineRadiusApi21(radius)
    else
        setOutlineRadiusCompat(radius)
}

/**
 * Enable clipping for VideoStreamView for api level >= 21
 * @param radius radius value in pixels
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
private fun <T> T.setOutlineRadiusApi21(radius: Float) where T : android.view.View, T : Roundable {
    if (radius == 0f) {
        clipToOutline = false
        return
    }
    updateOutlineProvider(radius)
    clipToOutline = true
}

/**
 * Enable clipping for VideoStreamView for api level < 21
 * @param radius radius value in pixels
 */
private fun <T> T.setOutlineRadiusCompat(radius: Float) where T : View, T : Roundable {
    if (background != null) Log.e("RoundedView", "RoundedView does not support backgrounds on api below LOLLIPOP!")
    calculateClippingRectAndApplyClipPathCompat(radius)
}


/**
 * Calculates clipping rect and clipping path based on actual width and height
 */
private fun <T> T.calculateClippingRectAndApplyClipPath() where T : View, T : Roundable {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        calculateClippingRectAndApplyClipPathApi21(measuredRadius)
    else
        calculateClippingRectAndApplyClipPathCompat(measuredRadius)
}

private val <T> T.clipPath: Path? where T : android.view.View, T : Roundable   by cached {
    return@cached if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) Path() else null
}

/**
 * clipPath?.addRoundRect(rect, outlineRadius, outlineRadius, Path.Direction.CW)
 * Calculates clipping rect and clipping path based on actual width and height for api level < 21
 */
private fun <T> T.calculateClippingRectAndApplyClipPathCompat(outlineRadius: Float) where T : View, T : Roundable {
    val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
    clipPath?.reset()
    clipPath?.addRoundRect(rect, outlineRadius, outlineRadius, Path.Direction.CW)
}

/**
 * Calculates clipping rect and clipping path based on actual width and height for api level >= 21
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
private fun <T> T.calculateClippingRectAndApplyClipPathApi21(outlineRadius: Float) where T : View, T : Roundable {
    outlineProvider?.let {
        updateOutlineProvider(outlineRadius)
    }
}

/**
 * Updates outline provider for api level >21
 * @param radius round corners factor in pixels
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
private fun <T> T.updateOutlineProvider(radius: Float) where T : View, T : Roundable {
    outlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, width, height, radius)
        }
    }
}