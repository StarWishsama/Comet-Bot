package io.github.starwishsama.namelessbot.objects;

import io.github.starwishsama.namelessbot.BotConstants;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Data
public class GroupShop {
    private long groupId;
    private List<BotUser> admins;
    private ArrayList<ShopItem> items;

    public GroupShop() {

    }

    public GroupShop(Long groupId) {
        this.groupId = groupId;
    }

    public ShopItem getItemByName(String itemName) {
        AtomicReference<ShopItem> result = new AtomicReference<>(new ShopItem());
        items.forEach(item -> {
            if (item.getItemName().equals(itemName)) {
                result.set(item);
            }
        });
        return result.get();
    }

    public void addAdmin(BotUser user) {
        admins.add(user);
    }

    public void removeAdmin(BotUser user) {
        admins.remove(user);
    }

    public void addNewItem(ShopItem item) {
        if (!items.contains(item)) {
            items.add(item);
        }
    }

    public static GroupShop getShopById(Long groupId) {
        AtomicReference<GroupShop> result = new AtomicReference<>(new GroupShop());
        BotConstants.shop.forEach(shop -> {
            if (shop.getGroupId() == groupId) {
                result.set(shop);
            }
        });
        return result.get();
    }
}
