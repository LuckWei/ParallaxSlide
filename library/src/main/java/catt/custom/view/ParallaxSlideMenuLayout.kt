package catt.custom.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log.e
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import java.lang.IllegalArgumentException

class ParallaxSlideMenuLayout
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : HorizontalScrollView(context, attrs, defStyleAttr) {
    private val _TAG: String = ParallaxSlideMenuLayout::class.java.simpleName

    private val _slidingDrag = 500
    private val _gestureDetector: GestureDetector by lazy {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                return when {
                    !whetherShowMenu && velocityX > 0 && _slidingDrag < Math.abs(velocityX) -> {
                        openMenu()
                        true
                    }
                    whetherShowMenu && velocityX < 0 && _slidingDrag < Math.abs(velocityX) -> {
                        closeMenu()
                        true
                    }
                    else -> false
                }
            }
        })
    }

    private val _widthPixels: Int
        get() = DisplayMetrics().run {
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(this)
            widthPixels
        }

    private val _layoutInflater: LayoutInflater by lazy { LayoutInflater.from(context) }

    private val _viewRoot: LinearLayout by lazy {
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }
    }
    private val _menuLayout: ViewGroup get() = _viewRoot.getChildAt(0) as ViewGroup
    private val _contentLayout: ViewGroup get() = _viewRoot.getChildAt(1) as ViewGroup
    private val _coverView: View get() = _contentLayout.getChildAt(1)


    private val _menuWidth: Int get() = _widthPixels - menuMarginEnd

    private var menuMarginEnd: Int = 0
    private var overlayPercentage: Float = 0F
    private var whetherIntercept: Boolean = false
    private var whetherShowMenu: Boolean = false

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ParallaxSlideMenuLayout).apply {
            removeAllViews()
            menuMarginEnd = getFloat(R.styleable.ParallaxSlideMenuLayout_catt_ContentContraction_MarginEndPercentage, 0.1F).run marginEndPercentage@{
                Math.floor(_widthPixels.toDouble() * this@marginEndPercentage).toInt()
            }
            overlayPercentage = getFloat(R.styleable.ParallaxSlideMenuLayout_catt_OverlayPercentage, 0.3F)
            _viewRoot.addView(getResourceId(R.styleable.ParallaxSlideMenuLayout_catt_MenuLayout, 0).run menuLayout@{
                if (this@menuLayout == 0) {
                    throw IllegalArgumentException("Attributes cannot be 0 for app:catt13_MenuLayout  ")
                }
                (_layoutInflater.inflate(this@menuLayout, _viewRoot, false) as ViewGroup).apply {
                    elevation = 0F
                }
            }, 0, LayoutParams(_widthPixels - menuMarginEnd, LayoutParams.MATCH_PARENT))


            val contentLayoutParams = LayoutParams(_widthPixels, LayoutParams.MATCH_PARENT)
            _viewRoot.addView(getResourceId(R.styleable.ParallaxSlideMenuLayout_catt_ContentLayout, 0).run contentLayout@{
                if (this@contentLayout == 0) {
                    throw IllegalArgumentException("Attributes cannot be 0 for app:catt13_ContentLayout  ")
                }
                FrameLayout(context).apply frameLayout@{
                    layoutParams = contentLayoutParams
                    outlineProvider = ViewOutlineProvider.BOUNDS
                    elevation = getDimensionPixelSize(R.styleable.ParallaxSlideMenuLayout_catt_ContentLayout_Elevation, 16).run contentElevation@{
                        convertPx(TypedValue.COMPLEX_UNIT_DIP, this@contentElevation)
                    }
                    addView(View(context).apply {
                        layoutParams = contentLayoutParams
                        setBackgroundColor(Color.parseColor("#A0333333"))
                        alpha = 0F
                        isFocusable = false
                        isClickable = false
                    }, -1)
                    addView(_layoutInflater.inflate(this@contentLayout, this@frameLayout, false) as ViewGroup, 0)
                }
            }, -1, contentLayoutParams)
            addView(_viewRoot, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }.recycle()
        overScrollMode = View.OVER_SCROLL_NEVER
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        _menuLayout.apply {
            translationX = overlayPercentage * l
            e(_TAG, "translationX = $translationX")
        }
        _contentLayout.apply {
            e(_TAG, "p = ${1 - l / _menuWidth.toFloat()}")
            _coverView.apply {
                alpha = 1 - l / _menuWidth.toFloat()
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        scrollTo(_menuWidth, 0)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        whetherIntercept = when {
            ev.x > _menuWidth -> {
                closeMenu()
                true
            }
            else -> super.onInterceptTouchEvent(ev)
        }
        return whetherIntercept
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return ev.run {
            when {
                whetherIntercept -> true
                _gestureDetector.onTouchEvent(ev) -> true
                ev.action == MotionEvent.ACTION_UP && x > 0 -> {
                    e(_TAG, "scrollX = $scrollX")
                    if (scrollX > _menuWidth / 2) {
                        closeMenu()
                    } else {
                        openMenu()
                    }
                    false
                }
                else -> super.onTouchEvent(ev)
            }
        }
    }

    fun closeMenu() {
        whetherShowMenu = false
        smoothScrollTo(_menuWidth, 0)
    }

    fun openMenu() {
        whetherShowMenu = true
        smoothScrollTo(0, 0)
    }

    private fun convertPx(unit: Int, value: Int) = TypedValue.applyDimension(unit, value.toFloat(), resources.displayMetrics)
}