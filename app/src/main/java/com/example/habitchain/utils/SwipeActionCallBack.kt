package com.example.habitchain.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.habitchain.R

class SwipeActionCallback(
    private val context: Context,
    private val onDeleteClicked: (Int) -> Unit,
    private val onEditClicked: (Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_white_24)
    private val editIcon = ContextCompat.getDrawable(context, R.drawable.ic_edit_white_24)
    private val intrinsicWidth = deleteIcon?.intrinsicWidth ?: 0
    private val intrinsicHeight = deleteIcon?.intrinsicHeight ?: 0
    private val background = ColorDrawable()
    private val deleteColor = ContextCompat.getColor(context, R.color.delete)
    private val editColor = ContextCompat.getColor(context, R.color.blue)
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        when (direction) {
            ItemTouchHelper.LEFT -> onDeleteClicked(position)
            ItemTouchHelper.RIGHT -> onEditClicked(position)
        }
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
        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top

        val isCanceled = dX == 0f && !isCurrentlyActive

        if (isCanceled) {
            clearCanvas(
                c,
                itemView.right + dX,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat()
            )
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        if (dX < 0) {
            background.color = deleteColor
            background.setBounds(
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )
            background.draw(c)

            val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
            val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
            val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
            val deleteIconRight = itemView.right - deleteIconMargin
            val deleteIconBottom = deleteIconTop + intrinsicHeight

            deleteIcon?.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
            deleteIcon?.draw(c)
        } else if (dX > 0) {
            background.color = editColor
            background.setBounds(
                itemView.left,
                itemView.top,
                itemView.left + dX.toInt(),
                itemView.bottom
            )
            background.draw(c)

            val editIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
            val editIconMargin = (itemHeight - intrinsicHeight) / 2
            val editIconLeft = itemView.left + editIconMargin
            val editIconRight = itemView.left + editIconMargin + intrinsicWidth
            val editIconBottom = editIconTop + intrinsicHeight

            editIcon?.setBounds(editIconLeft, editIconTop, editIconRight, editIconBottom)
            editIcon?.draw(c)
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        c?.drawRect(left, top, right, bottom, clearPaint)
    }
}