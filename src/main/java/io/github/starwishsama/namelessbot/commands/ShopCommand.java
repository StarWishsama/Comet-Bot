package io.github.starwishsama.namelessbot.commands;

import cc.moecraft.icq.command.CommandProperties;
import cc.moecraft.icq.command.interfaces.EverywhereCommand;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.event.events.message.EventMessage;
import cc.moecraft.icq.user.User;
import cc.moecraft.utils.ArrayUtils;
import cc.moecraft.utils.StringUtils;
import io.github.starwishsama.namelessbot.BotConstants;
import io.github.starwishsama.namelessbot.managers.ShopManager;
import io.github.starwishsama.namelessbot.objects.ShopItem;
import io.github.starwishsama.namelessbot.objects.group.GroupShop;
import io.github.starwishsama.namelessbot.objects.user.BotUser;
import io.github.starwishsama.namelessbot.utils.BotUtils;

import java.util.ArrayList;

public class ShopCommand implements EverywhereCommand {
    @Override
    public String run(EventMessage event, User sender, String command, ArrayList<String> args) {
        if (BotUtils.isNoCoolDown(sender.getId())) {
            BotUser user = BotUser.getUser(sender.getId());
            if (user == null) {
                return BotUtils.sendLocalMessage("msg.bot-prefix", "用户不存在");
            }

            final boolean isAdmin = BotUser.isBotAdmin(sender.getId());
            final boolean isGroup = event instanceof EventGroupMessage;
            GroupShop shop;

            if (args.size() > 0) {
                    switch (args.get(0)) {
                        case "create":
                        case "创建":
                            if (isGroup && isAdmin) {
                                shop = GroupShop.getShopById(((EventGroupMessage) event).getGroupId());
                                if (shop == null) {
                                    BotConstants.shop.add(new GroupShop(((EventGroupMessage) event).getGroupId()));
                                    return BotUtils.sendLocalMessage("msg.bot-prefix", "本群暂未创建商店, 已自动创建!");
                                } else {
                                    return BotUtils.sendLocalMessage("msg.bot-prefix", "本群已经创建过商店了!");
                                }
                            }
                            break;
                        case "add":
                        case "上架":
                            if (isAdmin) {
                                if (isGroup) {
                                    if (args.size() > 4) {
                                        shop = GroupShop.getShopById(((EventGroupMessage) event).getGroupId());
                                        if (shop == null) {
                                            return BotUtils.sendLocalMessage("msg.bot-prefix", "本群暂未创建商店!");
                                        } else {
                                            if (StringUtils.isNumeric(args.get(2))) {
                                                shop.addNewItem(new ShopItem(args.get(1), Double.parseDouble(args.get(2)), ArrayUtils.getTheRestArgsAsString(args, 3)));
                                                return BotUtils.sendLocalMessage("msg.bot-prefix", "添加商品成功!");
                                            } else {
                                                return BotUtils.sendLocalMessage("msg.bot-prefix", "请填写有效价格!");
                                            }
                                        }
                                    } else {
                                        return BotUtils.sendLocalMessage("msg.bot-prefix", "/商店 上架 [商品名] [商品价格] [执行命令] <限制次数>");
                                    }
                                } else {
                                    if (args.size() > 4) {
                                        if (StringUtils.isNumeric(args.get(1)) && !args.get(1).contains(".")) {
                                            shop = GroupShop.getShopById(Long.parseLong(args.get(1)));
                                            if (shop != null) {
                                                if (StringUtils.isNumeric(args.get(3))) {
                                                    shop.addNewItem(new ShopItem(args.get(2), Double.parseDouble(args.get(3)), ArrayUtils.getTheRestArgsAsString(args, 4)));
                                                    return BotUtils.sendLocalMessage("msg.bot-prefix", "添加商品成功!");
                                                }
                                            } else {
                                                return BotUtils.getLocalMessage("msg.bot-prefix") + "此群暂未创建商店!";
                                            }
                                        } else {
                                            return BotUtils.sendLocalMessage("msg.bot-prefix", "请填写正确的群号");
                                        }
                                    } else {
                                        return BotUtils.sendLocalMessage("msg.bot-prefix", "/商店 上架 [群号] [商品名] [商品价格] [执行命令] <限制次数>");
                                    }
                                }
                            }
                            break;
                        case "list":
                        case "商品":
                            StringBuilder sb = new StringBuilder();
                            if (isGroup) {
                                shop = GroupShop.getShopById(((EventGroupMessage) event).getGroupId());
                                shop.getItems().forEach(item -> sb.append(item.getItemName()).append(" ").append(item.getPoint()).append("点积分").append("\n"));
                                return "商品列表:" + sb.toString().trim();
                            } else if (args.size() > 1 && StringUtils.isNumeric(args.get(1)) && !args.get(1).contains(".")) {
                                shop = GroupShop.getShopById(Long.parseLong(args.get(1)));
                                if (shop == null) {
                                    return BotUtils.sendLocalMessage("msg.bot-prefix", "指定的群没有商店");
                                } else {
                                    shop.getItems().forEach(item -> sb.append(item.getItemName()).append(" ").append(item.getPoint()).append("点积分").append("\n"));
                                    return "商品列表:" + sb.toString().trim();
                                }
                            }
                            break;
                        case "buy":
                        case "购买":
                            if (isGroup) {
                                shop = GroupShop.getShopById(((EventGroupMessage) event).getGroupId());
                                if (shop == null) {
                                    return BotUtils.sendLocalMessage("msg.bot-prefix", "本群没有商店");
                                } else {
                                    ShopItem item = shop.getItemByName(args.get(1));
                                    if (item == null) {
                                        return BotUtils.sendLocalMessage("msg.bot-prefix", "商店里没有这个商品");
                                    } else {
                                        if (user.getCheckInPoint() > item.getPoint()) {
                                            try {
                                                 return BotUtils.sendLocalMessage("msg.bot-prefix", ShopManager.executeBuy(item, user));
                                            } catch (Exception e) {
                                                return BotUtils.sendLocalMessage("msg.bot-prefix", "在购买时发生了异常, 积分已自动退还.");
                                            }
                                        }
                                    }
                                }
                            } else if (args.size() > 1 && StringUtils.isNumeric(args.get(1)) && !args.get(1).contains(".")) {
                                shop = GroupShop.getShopById(Long.parseLong(args.get(1)));
                                if (shop == null) {
                                    return BotUtils.sendLocalMessage("msg.bot-prefix", "指定的群没有商店");
                                } else {
                                    ShopItem item = shop.getItemByName(args.get(2));
                                    if (item == null) {
                                        return BotUtils.sendLocalMessage("msg.bot-prefix", "商店里没有这个商品");
                                    } else {
                                        if (user.getCheckInPoint() > item.getPoint()) {
                                            try {
                                                return BotUtils.sendLocalMessage("msg.bot-prefix", ShopManager.executeBuy(item, user));
                                            } catch (Exception e) {
                                                return BotUtils.sendLocalMessage("msg.bot-prefix", "在购买时发生了异常, 积分已自动退还.");
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                }
            } else {
                return BotUtils.sendLocalMessage("msg.bot-prefix", "/shop list 商品列表\n/shop buy [商品名] 购买商品");
            }
        }
        return null;
    }

    @Override
    public CommandProperties properties() {
        return new CommandProperties("shop", "商店");
    }
}
