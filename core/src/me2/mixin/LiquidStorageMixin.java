package me2.mixin;

import me2.SimpleStorageMixin;
import me2.world.ME2TransportationBus.ME2BusBuild;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeLiquid;
import mindustry.world.consumers.ConsumeLiquids;

import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.production.GenericCrafter.GenericCrafterBuild;
import org.jetbrains.annotations.NotNull;

public class LiquidStorageMixin implements SimpleStorageMixin.ContentStorageMixin<Liquid> {
    private boolean _func_120490(@NotNull Building building, Liquid content) {
        if(building.liquids == null || !building.block.hasLiquids || building instanceof ME2BusBuild) {
            return false;
        }
        if(building instanceof GenericCrafterBuild) {
            for(Consume consume : building.block.consumers) {
                if(consume instanceof ConsumeLiquids) {
                    ConsumeLiquids i = (ConsumeLiquids) consume;
                    for(LiquidStack stack : i.liquids) {
                        if(stack.liquid == content) {
                            return true;
                        }
                    }
                }
                if(consume instanceof ConsumeLiquid) {
                    ConsumeLiquid i = (ConsumeLiquid) consume;
                    if(i.liquid == content) return true;
                }
            }
            GenericCrafter crafter = (GenericCrafter) building.block;
            if(crafter.outputLiquids != null) {
                for(LiquidStack stack : crafter.outputLiquids) {
                    if(stack.liquid == content) {
                        return true;
                    }
                }
            }
            return crafter.outputLiquid != null && crafter.outputLiquid.liquid == content;
        }
        return true;
    }

    @Override
    public void init() {
    }

    @Override
    public float maximumAccepted(Building building, Liquid content) {
        return _func_120490(building, content) ? building.block.liquidCapacity : 0;
    }

    @Override
    public float amount(Building building, Liquid content) {
        return _func_120490(building, content) ? building.liquids.get(content) : 0;
    }

    @Override
    public float extract(Building building, Liquid content, float amount) {
        float stored = amount(building, content);

        if(stored >= amount) {
            building.liquids.remove(content, amount);
            return 0;
        } else {
            building.liquids.set(content, 0);
            return amount - stored;
        }
    }

    @Override
    public float receive(Building building, Liquid content, float amount) {
        float stored = amount(building, content);
        float capacity = maximumAccepted(building, content);
        float ost = capacity - stored;

        if(ost >= amount) {
            building.liquids.add(content, amount);
            return 0;
        } else {
            building.liquids.set(content, capacity);
            return amount - ost;
        }
    }

    @Override
    public boolean canExtract(Building building, Liquid content) {
        if(building.liquids == null) {
            return false;
        }

        for(Consume consume : building.block.consumers) {
            if(consume instanceof ConsumeLiquids) {
                ConsumeLiquids cons = (ConsumeLiquids) consume;
                for(LiquidStack stack : cons.liquids) {
                    if(stack.liquid == content) {
                        return false;
                    }
                }
            }
            if(consume instanceof ConsumeLiquid) {
                ConsumeLiquid cons = (ConsumeLiquid) consume;
                if(cons.liquid == content) return false;
            }
        }

        return true;
    }

    @Override
    public boolean canReceive(Building building, Liquid content) {
        if(building.liquids == null) {
            return false;
        }

        if(building instanceof GenericCrafterBuild) {
            GenericCrafter crafter = (GenericCrafter) building.block;
            if(crafter.outputLiquids != null) {
                for(LiquidStack stack : crafter.outputLiquids) {
                    if(stack.liquid == content) {
                        return false;
                    }
                }
            }
        }

        return building.acceptLiquid(building, content);
    }

    @Override
    public Liquid toContent(int id) {
        return Vars.content.liquid(id);
    }
}