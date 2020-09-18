package edu.vanderbilt.crawler.extensions

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import java.util.*

/**
 * View extensions.
 */

val View.ctx: Context
    get() = context

@Suppress("unused")
var TextView.textColor: Int
    get() = currentTextColor
    set(v) = setTextColor(v)

fun View.slideExit() {
    if (translationY == 0f) animate().translationY(height.toFloat())
}

fun View.slideEnter() {
    if (translationY < 0f) animate().translationY(0f)
}

fun View.setViewHeight(height: Int) {
    val params = layoutParams
    params.height = height
    requestLayout()
}
/**
 * Search up the view receiver's ancestors looking for the
 * scrolling view and returns that view or null if not found.
 *
 * @return ScrollView or HorizontalScrollView (as ViewGroup)
 * if found, otherwise null.
 */
fun View.getScrollingAncestor(): ViewGroup? {
    // The layout of this view assumes a scrolling ancestor.
    var ancestor = parent
    while (ancestor != null
            && ancestor !is ScrollView
            && ancestor !is HorizontalScrollView) {
        ancestor = ancestor.parent
    }

    return if (ancestor is ScrollView ||
            ancestor is HorizontalScrollView) {
        ancestor as ViewGroup
    } else {
        null
    }
}

/**
 * Enables or disables the receiver view and all its descendants.
 */
fun View.enable(enable: Boolean) {
    isEnabled = enable
    (this as? ViewGroup)?.forAllDescendants {
        it.isEnabled = enable
        true
    }
}

var View.property: Int?
    get() = ExtensionBackingField["${this.javaClass.canonicalName}::property"]
    set(value) {
        ExtensionBackingField["${this.javaClass.canonicalName}::property"] = value
    }

/**
 * Same as View.postDelayed, but with reversed parameters so
 * that the call can be postDelay(1000) { .... }
 */
fun View.postDelayed(delay: Long, action: () -> Unit): Boolean {
    return postDelayed(action, delay)
}

val Activity.contentView: View?
    get() = findViewById<ViewGroup>(android.R.id.content)?.getChildAt(0)

/**
 * Same as View.postDelayed, but with reversed parameters so
 * that the call can be postDelay(1000) { .... }
 */
fun Activity.postDelayed(delay: Long, action: () -> Unit): Boolean {
    return contentView?.postDelayed(action, delay) ?: false
}

/**
 * Returns the view or the first descendant view
 * that matches the passed [predicate]
 */
fun View.findView(predicate: (view: View) -> Boolean): View? {
    if (predicate(this)) {
        return this
    } else if (this is ViewGroup) {
        // Search all descendants for a match.
        (0 until childCount)
                .map { getChildAt(it) }
                .forEach {
                    val view = it.findView(predicate)
                    if (view != null) {
                        return view
                    }
                }
    }

    return null
}

/**
 * Runs the specified [action] on all descendant views until
 * either all views have been visited, or [action] returns
 * false.
 */
fun View.forAllDescendants(action: (view: View) -> Boolean): Boolean {
    if (this is ViewGroup) {
        // Search all descendants for a match.
        (0 until childCount)
                .map { getChildAt(it) }
                .forEach {
                    if (!action(it)) {
                        return false
                    }
                    if (!it.forAllDescendants(action)) {
                        return false
                    }
                }
    }

    return true
}
/**
 * Returns a list of all descendant views (that may include the
 * the receiver view) that match the supplied [predicate].
 */
fun View.findViews(predicate: (view: View) -> Boolean): List<View> {
    val views = ArrayList<View>()

    if (predicate(this)) {
        views.add(this)
    }

    if (this is ViewGroup) {
        (0 until this.childCount)
                .map { getChildAt(it) }
                .forEach {
                    views.addAll(it.findViews(predicate))
                }
    }

    return views
}

/**
 * Finds all descendant views with a specific tag
 * (including the receiver view).
 *
 * @param tag  The tag to match.
 * @return A list of views that have the specified tag set.
 */
@Suppress("unused")
fun View.findTaggedViews(tag: Any): List<View> {
    return findViews { it.tag == tag }
}

/**
 * Finds the first descendant ImageView that has a matching
 * transition name. If the receiver is an ImageView and has
 * a matching transition name, it will be returned as the
 * matching view.
 *
 * @param transitionName The transition name to match.
 * @return The first image view with the specified transition name.
 */
fun View.findImageViewWithTransitionName(transitionName: String): ImageView? {
    val view = findView {
        it is ImageView && ViewCompat.getTransitionName(it) == transitionName
    }

    return if (view != null) view as ImageView else null
}

/**
 * Not sure if this will work or if it has to be a method.
 */
@Suppress("unused")
var View.behavior: () -> CoordinatorLayout.Behavior<View>
    get() = {
        val params = (layoutParams as? CoordinatorLayout.LayoutParams)
                ?: throw IllegalArgumentException("The view is not a child " +
                        "of CoordinatorLayout")
        params.behavior as? CoordinatorLayout.Behavior<View>
                ?: throw IllegalArgumentException("The view is not associated " +
                        "with FloatingActionButton.Behavior")
    }
    set(behavior) {
        (layoutParams as CoordinatorLayout.LayoutParams).behavior =
                behavior as? CoordinatorLayout.Behavior<*>
        requestLayout()
    }

@Suppress("unused")
fun View.setSingleClickListener(interval: Long = 500L, action: () -> Unit) {
    setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(view: View) {
            val now = SystemClock.elapsedRealtime()
            if (now - lastClickTime >= interval) {
                lastClickTime = now
                action()
            }
        }
    })
}

var View.leftPadding: Int
    inline get() = paddingLeft
    set(value) = setPadding(value, paddingTop, paddingRight, paddingBottom)

var View.topPadding: Int
    inline get() = paddingTop
    set(value) = setPadding(paddingLeft, value, paddingRight, paddingBottom)

var View.rightPadding: Int
    inline get() = paddingRight
    set(value) = setPadding(paddingLeft, paddingTop, value, paddingBottom)

var View.bottomPadding: Int
    inline get() = paddingBottom
    set(value) = setPadding(paddingLeft, paddingTop, paddingRight, value)

