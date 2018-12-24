package nl.hnogames.domoticz.Helpers;

import android.graphics.Canvas;

import androidx.recyclerview.widget.RecyclerView;
import github.nisrulz.recyclerviewhelper.RVHAdapter;
import github.nisrulz.recyclerviewhelper.RVHViewHolder;

import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_IDLE;
import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE;
import static androidx.recyclerview.widget.ItemTouchHelper.Callback;
import static androidx.recyclerview.widget.ItemTouchHelper.DOWN;
import static androidx.recyclerview.widget.ItemTouchHelper.END;
import static androidx.recyclerview.widget.ItemTouchHelper.START;
import static androidx.recyclerview.widget.ItemTouchHelper.UP;

/**
 * The type Rvh item touch helper callback.
 */
public class RVHItemTouchHelperCallback extends Callback {

    private final boolean isItemViewSwipeEnabledLeft;

    private final boolean isItemViewSwipeEnabledRight;

    private final boolean isLongPressDragEnabled;

    private final RVHAdapter mAdapter;

    /**
     * Instantiates a new Rvh item touch helper callback.
     *
     * @param adapter                     the adapter
     * @param isLongPressDragEnabled      the is long press drag enabled
     * @param isItemViewSwipeEnabledLeft  the is item view swipe enabled left
     * @param isItemViewSwipeEnabledRight the is item view swipe enabled right
     */
    public RVHItemTouchHelperCallback(RVHAdapter adapter, boolean isLongPressDragEnabled,
                                      boolean isItemViewSwipeEnabledLeft, boolean isItemViewSwipeEnabledRight) {
        mAdapter = adapter;
        this.isItemViewSwipeEnabledLeft = isItemViewSwipeEnabledLeft;
        this.isItemViewSwipeEnabledRight = isItemViewSwipeEnabledRight;
        this.isLongPressDragEnabled = isLongPressDragEnabled;
    }

    @Override
    public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder current,
                               RecyclerView.ViewHolder target) {
        return current.getItemViewType() == target.getItemViewType();
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);

        if (viewHolder instanceof RVHViewHolder) {
            // Tell the view holder it's time to restore the idle state
            RVHViewHolder itemViewHolder = (RVHViewHolder) viewHolder;
            itemViewHolder.onItemClear();
        }
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final int dragFlags = UP | DOWN | START | END;
        final int swipeFlags;
        if (isItemViewSwipeEnabledLeft && isItemViewSwipeEnabledRight) {
            swipeFlags = START | END;
        } else if (isItemViewSwipeEnabledRight) {
            swipeFlags = START;
        } else {
            swipeFlags = END;
        }

        return Callback.makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return isItemViewSwipeEnabledLeft || isItemViewSwipeEnabledRight;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return isLongPressDragEnabled;
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ACTION_STATE_SWIPE) {
            // Fade out the view as it is swiped out of the parent's bounds
            viewHolder.itemView.setTranslationX(dX);
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source,
                          RecyclerView.ViewHolder target) {
        // Notify the adapter of the move
        mAdapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        // We only want the active item to change
        if (actionState != ACTION_STATE_IDLE && viewHolder instanceof RVHViewHolder) {
            // Let the view holder know that this item is being moved or dragged
            RVHViewHolder itemViewHolder = (RVHViewHolder) viewHolder;
            itemViewHolder.onItemSelected(actionState);
        }

        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mAdapter.onItemDismiss(viewHolder.getAdapterPosition(), direction);
    }
}
