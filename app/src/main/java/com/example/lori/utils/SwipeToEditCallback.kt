package com.example.lori.utils

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.lori.R

/**
 * https://medium.com/@kitek/recyclerview-swipe-to-delete-easier-than-you-thought-cff67ff5e5f6
 */
abstract class SwipeToEditCallback(context: Context) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

    private val icEdit: Drawable =
        ContextCompat.getDrawable(context, R.drawable.ic_edit_24dp)!!
    private val clearPaint = Paint()

    init {
        icEdit.setTint(Color.WHITE)
        clearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    /**
     * To disable "swipe" for specific item return 0 here.
     *
     * E.g:
     * - if (viewHolder?.itemViewType == YourAdapter.SOME_TYPE) return 0
     * - if (viewHolder?.adapterPosition == 0) return 0
     */
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return if (viewHolder.adapterPosition == 10) 0 else super.getMovementFlags(
            recyclerView,
            viewHolder
        )
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val itemView = viewHolder.itemView

        if (dX == 0f && !isCurrentlyActive) {
            return c.drawRect(
                itemView.left + dX,
                itemView.top.toFloat(),
                itemView.left.toFloat(),
                itemView.bottom.toFloat(),
                clearPaint
            )
        }

        // todo draw the green background
        val background = ColorDrawable()
        background.color = Color.GREEN
        background.setBounds(
            itemView.left + dX.toInt(),
            itemView.top,
            itemView.left,
            itemView.bottom
        )
        background.draw(c)

        // todo calculate position of edit icon and draw the edit icon
        val intrinsicWidth = icEdit.intrinsicWidth
        val intrinsicHeight = icEdit.intrinsicHeight
        val height = itemView.bottom - itemView.top
        val margin = (height - intrinsicHeight) / 2

        val top = itemView.top + (height - intrinsicHeight) / 2
        val bottom = top + intrinsicHeight
        val left = itemView.left + margin - intrinsicWidth
        val right = itemView.left + margin

        icEdit.setBounds(left, top, right, bottom)
        icEdit.draw(c)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }
}
