package me2.mixin;

import me2.SimpleStorageMixin;
import me2.world.ME2TransportationBus.ME2BusBuild;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeItems;

import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.production.Drill.DrillBuild;
import mindustry.world.blocks.production.GenericCrafter.GenericCrafterBuild;
import org.jetbrains.annotations.NotNull;

public class ItemStorageMixin implements SimpleStorageMixin.ContentStorageMixin<Item> {
    private boolean _func_120490(@NotNull Building building, Item content) {
        if(building.items == null || !building.block.hasItems || building instanceof ME2BusBuild) {
            return false;
        }
        if(building instanceof GenericCrafterBuild) {
            for(Consume consume : building.block.consumers) {
                if(consume instanceof ConsumeItems) {
                    ConsumeItems i = (ConsumeItems) consume;
                    for(ItemStack stack : i.items) {
                        if(stack.item == content) {
                            return true;
                        }
                    }
                }
            }
            GenericCrafter crafter = (GenericCrafter) building.block;
            if(crafter.outputItems != null) {
                for(ItemStack stack : crafter.outputItems) {
                    if(stack.item == content) {
                        return true;
                    }
                }
            }
            return crafter.outputItem != null && crafter.outputItem.item == content;
        }
        return true;
    }

    @Override
    public void init() {
    }

    @Override
    public float maximumAccepted(Building building, Item content) {
        return _func_120490(building, content) ? building.getMaximumAccepted(content) : 0;
    }

    @Override
    public float amount(Building building, Item content) {
        return _func_120490(building, content) ? building.items.get(content) : 0;
    }

    @Override
    public float extract(Building building, Item content, float amount) {
        int amountI = (int) amount;
        int storedI = (int) amount(building, content);

        if(storedI >= amountI) {
            building.items.remove(content, amountI);
            return 0;
        } else {
            building.items.set(content, 0);
            return amountI - storedI;
        }
    }

    @Override
    public float receive(Building building, Item content, float amount) {
        int amountI = (int) amount;
        int storedI = (int) amount(building, content);
        int capacityI = (int) maximumAccepted(building, content);
        int ost = capacityI - storedI;

        if(ost >= amountI) {
            building.items.add(content, amountI);
            return 0;
        } else {
            building.items.set(content, capacityI);
            return amountI - ost;
        }
    }

    @Override
    public boolean canExtract(Building building, Item content) {
        if(building.items == null) {
            return false;
        }

        for(Consume consume : building.block().consumers) {
            if(consume instanceof ConsumeItems) {
                ConsumeItems items = (ConsumeItems) consume;
                for(ItemStack stack : items.items) {
                    if(stack.item == content) {
                        return false;
                    }
                }
            }
        }

        return amount(building, content) > 0;
    }

    @Override
    public boolean canReceive(Building building, Item content) {
        if(building.items == null) {
            return false;
        }

        if(building instanceof DrillBuild) {
            DrillBuild build = (DrillBuild) building;
            if(build.dominantItem == content) {
                return false;
            }
        }

        if(building instanceof GenericCrafterBuild) {
            for(ItemStack stack : ((GenericCrafter) building.block).outputItems) {
                if(stack.item == content) {
                    return false;
                }
            }
        }

        return amount(building, content) < maximumAccepted(building, content)
                && building.acceptItem(building, content);
    }

    @Override
    public Item toContent(int id) {
        return Vars.content.item(id);
    }
}