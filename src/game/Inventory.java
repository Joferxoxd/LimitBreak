package game;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private final List<Item> items = new ArrayList<>();

    public void addItem(Item item) {
        items.add(item);
    }
    public void removeItem(Item item) {
        items.remove(item);
    }


    public List<Item> getItems() {
        return items;
    }

    public void useItem(Item item, Player player) {
        if (item != null && !item.fueConsumido()) {
            item.usar(player);
            if (item.isConsumible() && item.fueConsumido()) {
                removeItem(item); // ✅ se elimina del inventario
            }
        }
    }
}
