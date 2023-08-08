package me2.mixin;

import me2.SimpleStorageMixin;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeLiquids;

import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.production.GenericCrafter.GenericCrafterBuild;

public class LiquidStorageMixin implements SimpleStorageMixin.ContentStorageMixin<Liquid> {
    @Override
    public void init() {
    }

    @Override
    public float maximumAccepted(Building building, Liquid content) {
        return building.block.liquidCapacity;
    }

    @Override
    public float amount(Building building, Liquid content) {
        return building.liquids == null ? 0 : building.liquids.get(content);
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
        }

        return amount(building, content) > 0;
    }

    @Override
    public boolean canReceive(Building building, Liquid content) {
        if(building.liquids == null) {
            return false;
        }

        if(building instanceof GenericCrafterBuild) {
            for(LiquidStack stack : ((GenericCrafter) building.block).outputLiquids) {
                if(stack.liquid == content) {
                    return false;
                }
            }
        }

        return amount(building, content) < maximumAccepted(building, content)
                && building.acceptLiquid(building, content);
    }

    @Override
    public Liquid toContent(int id) {
        return Vars.content.liquid(id);
    }
}