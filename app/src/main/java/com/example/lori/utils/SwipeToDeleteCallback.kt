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
abstract class SwipeToDeleteCallback(context: Context) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val icDelete: Drawable =
        ContextCompat.getDrawable(context, R.drawable.ic_delete_white_24dp)!!
    private val clearPaint = Paint()

    init {
        icDelete.setTint(Color.WHITE)
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
                itemView.right + dX,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat(),
                clearPaint
            )
        }

        // todo draw the red background
        val background = ColorDrawable()
        background.color = Color.RED
        background.setBounds(
            itemView.right + dX.toInt(),
            itemView.top,
            itemView.right,
            itemView.bottom
        )
        background.draw(c)

        // todo calculate position of delete icon and draw the delete icon
        val intrinsicWidth = icDelete.intrinsicWidth
        val intrinsicHeight = icDelete.intrinsicHeight
        val height = itemView.bottom - itemView.top
        val margin = (height - intrinsicHeight) / 2

        val top = itemView.top + (height - intrinsicHeight) / 2
        val bottom = top + intrinsicHeight
        val left = itemView.right - margin - intrinsicWidth
        val right = itemView.right - margin

        icDelete.setBounds(left, top, right, bottom)
        icDelete.draw(c)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }
}
