package nl.hnogames.domoticz.helpers;

import android.graphics.Canvas;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.ItemTouchHelper.Callback;

public class SimpleItemTouchHelperCallback extends Callback {
    private final ItemMoveAdapter mAdapter;
    private int dragFrom = -1;
    private int dragTo = -1;
    private final boolean isGrid;
    private boolean mOrderChanged = false;
    private int prevState = -1;

    public SimpleItemTouchHelperCallback(ItemMoveAdapter adapter, boolean isGrid) {
        mAdapter = adapter;
        this.isGrid = isGrid;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (this.isGrid)
            return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                    ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END);
        else
            return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                    ItemTouchHelper.DOWN | ItemTouchHelper.UP);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source,
                          RecyclerView.ViewHolder target) {
        dragFrom = source.getAdapterPosition();
        dragTo = target.getAdapterPosition();
        mOrderChanged = true;
        return true;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);

        if (prevState == ItemTouchHelper.ACTION_STATE_DRAG && actionState == ItemTouchHelper.ACTION_STATE_IDLE && mOrderChanged) {
            try {
                Log.i("Drag", "Drag from:" + (dragFrom) + " to:" + (dragTo));
                mAdapter.onItemMove(dragFrom, dragTo);
            } catch (Exception ex) {
            }
            mOrderChanged = false;
        }
        prevState = actionState;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
    }

    @Override
    public int interpolateOutOfBoundsScroll(RecyclerView recyclerView, int viewSize, int viewSizeOutOfBounds, int totalSize, long msSinceStartScroll) {
        final int direction = (int) Math.signum(viewSizeOutOfBounds);
        return 10 * direction;
    }

    @Override
    public void onChildDraw(@NotNull Canvas c, @NotNull RecyclerView recyclerView,
                            @NotNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        float topY = viewHolder.itemView.getTop() + dY;
        float bottomY = topY + viewHolder.itemView.getHeight();

        // Only redraw child if it is inbounds of view
        if (topY > 0 && bottomY < recyclerView.getHeight()) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }
}
