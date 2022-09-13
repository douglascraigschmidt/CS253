package edu.vanderbilt.crawler.ui.screens.webview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import edu.vanderbilt.crawler.R
import edu.vanderbilt.crawler.adapters.MultiSelectAdapter
import edu.vanderbilt.crawler.adapters.WebViewUrlAdapter
import edu.vanderbilt.crawler.databinding.WebviewUrlListBinding
import edu.vanderbilt.crawler.extensions.bottomSheetState
import edu.vanderbilt.crawler.extensions.peekBottomSheet
import edu.vanderbilt.crawler.extensions.postDelayed
import edu.vanderbilt.crawler.extensions.setViewHeight
import edu.vanderbilt.crawler.utils.KtLogger

/**
 * Supports a maximum length item list using push and pop operations to keep
 * the list items withing the maxCount value passed to the push function
 */
class WebViewUrlFragment : Fragment(), MultiSelectAdapter.OnSelectionListener, KtLogger {

    companion object {
        fun newInstance(): WebViewUrlFragment = WebViewUrlFragment()
    }

    /** Initialized in onCreateVew() */
    private lateinit var recyclerView: RecyclerView

    /** Set internal to allow controlling activity to access adapter. */
    internal val adapter by lazy {
        recyclerView.adapter as WebViewUrlAdapter
    }

    private val imagePicker by lazy {
        (activity as WebViewActivity).imagePicker
    }

    private val bottomSheet by lazy {
        (activity as WebViewActivity).binding.bottomSheet
    }

    /**
     * Immutable URL list displayed by the adapter.
     */
    val urls: List<String>
        get() = adapter.items.toList()

    private lateinit var binding: WebviewUrlListBinding

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = WebviewUrlListBinding.inflate(inflater, container, false)
        recyclerView = binding.root

        with(recyclerView) {
            adapter = WebViewUrlAdapter(context, mutableListOf(), imagePicker)

            // Custom layout manager to ensure that the activity bottom
            // sheet peek height adjust to the height of the first list item.
            // Note that this only works if the activity has a bottom sheet
            // with id "bottomSheet".
            layoutManager = object : LinearLayoutManager(context, VERTICAL, false) {
                override fun onLayoutCompleted(state: RecyclerView.State?) {
                    super.onLayoutCompleted(state)
                    val pos: Int = findFirstVisibleItemPosition()
                    if (pos == 0) {
                        val view = findViewByPosition(pos)
                        with(BottomSheetBehavior.from(bottomSheet)) {
                            peekHeight = view!!.height
                        }
                    }
                    val lastPos = findLastVisibleItemPosition()
                    if (pos >= 0) {
                        if ((lastPos - pos) + 1 < 5) {
                            recyclerView.setViewHeight(LinearLayout.LayoutParams.WRAP_CONTENT)
                        } else {
                            recyclerView.setViewHeight(resources.getDimensionPixelSize((R.dimen.web_view_list_height)))
                        }
                    }

                    if (!imagePicker) {
                        this@WebViewUrlFragment.peekBottomSheet()
                    } else if (BottomSheetBehavior.from(bottomSheet).state ==
                            BottomSheetBehavior.STATE_HIDDEN) {
                        this@WebViewUrlFragment.peekBottomSheet()
                    }
                }
            }

            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        return recyclerView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(activity as WebViewActivity) {
            initializeSearchView(this.binding.searchView)
        }
    }

    /**
     * To prevent flashing caused by slow web view loads, posts
     * the peek bottom sheet command after a 500 millisecond delay.
     */
    private fun peekBottomSheet() {
        if (bottomSheet.bottomSheetState == BottomSheetBehavior.STATE_HIDDEN) {
            recyclerView.postDelayed(500) {
                bottomSheet.peekBottomSheet()
            }
        }
    }

    /**
     * Pushes up the maxCount items. If the list size is equal to or greater than
     * the passed [maxCount] value, the oldest items will be popped off the list
     * to ensure that a single available spot will be available to make room for
     * the new item to be pushed to the top of the list.
     */
    fun push(url: String, maxCount: Int = Int.MAX_VALUE) {
        while (adapter.itemCount >= maxCount) {
            adapter.pop()
        }

        adapter.push(url)

        recyclerView.layoutManager!!.scrollToPosition(0)
    }
}
