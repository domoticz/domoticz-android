package nl.hnogames.domoticz.helpers;

public interface ItemMoveAdapter {
    void onItemDismiss(int position, int direction);

    boolean onItemMove(int fromPosition, int toPosition);
}
