package me2.world;

import arc.Core;
import arc.graphics.Color;
import me13.core.block.BlockAngles;
import me13.core.block.instance.AdvancedBlock;
import me2.BuildingSettingsMixin;
import me2.ME2Configurator;
import me2.mixin.ItemStorageMixin;
import me2.mixin.LiquidStorageMixin;
import me2.util.ME2NetGraph;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.world.blocks.defense.MendProjector;

import java.awt.*;

public class ME2Block extends AdvancedBlock {
    public static final int
            NO_TYPE_NO_C = -1,
            NO_TYPE = 0,
            ADAPTER_TYPE = 1,
            BALANCER_TYPE = 2;

    /**
     * 0 - no type
     * 1 - adapter
     * 2 - balancer
     */
    public int typeId = NO_TYPE_NO_C;

    public boolean noType() {
        return typeId == NO_TYPE || typeId == NO_TYPE_NO_C;
    }

    public boolean canConnect(BuildPlan self, BuildPlan other) {
        if(typeId == ADAPTER_TYPE) {
            return BlockAngles.angleTo(self, other) == BlockAngles.reverse(self.rotation);
        }
        return true;
    }

    public ME2Block(String name) {
        super(name);
        update = true;
    }

    public class ME2Build extends AdvancedBuild {
        public ME2NetGraph graph = new ME2NetGraph();
        public int tmp = -1;
        public int timer = 0;

        public void onEnableChange() {
            graph.proximityUpdate(this);
        }

        public boolean canConnectWire(ME2Build other) {
            if(typeId == ADAPTER_TYPE) {
                return other == reversedNearby();
            }
            return true;
        }

        public void containingGraphWithoutSelfFix() {
            if(!graph.contains(this)) {
                graph = new ME2NetGraph();
                graph.proximityUpdate(this);
            }
        }

        public boolean enabledChild() {
            Building building = nearby();
            if(building == null || !enabled) {
                return false;
            }
            int[] mass = new int[] {0};
            ME2Configurator
                    .select(BuildingSettingsMixin.class)
                    .map(mixin -> mixin.type(building))
                    .each(a -> {
                        if(a == BuildingSettingsMixin.AdapterConnectionType.ENABLED) {
                            mass[0]++;
                        } else if(a == BuildingSettingsMixin.AdapterConnectionType.DISABLED) {
                            mass[0]--;
                        }
                    });

            if(mass[0] == 0) {
                return !(building instanceof ME2Build);
            } else return mass[0] > 0;
        }

        @Override
        public void updateTile() {
            super.updateTile();
            containingGraphWithoutSelfFix();

            //if(timer % 30 == 0) {
            //    //fix bug No.2 with the nets
            //    graph.proximityUpdate(this);
            //}

            if(typeId == BALANCER_TYPE && timer % 60 == 59 && enabled && graph.isEnabled()) {
                Fx.healBlockFull.at(x, y, block.size, Color.cyan, block);
                Vars.content.liquids().each(liquid -> {
                    graph.balance(LiquidStorageMixin.class, liquid.id, false);
                });
                Vars.content.items().each(item -> {
                    graph.balance(ItemStorageMixin.class, item.id, true);
                });
            }

            if(tmp == -1) {
                tmp = enabled ? 1 : 0;
            }
            if((tmp == 1) != enabled) {
                onEnableChange();
            }
            tmp = enabled ? 1 : 0;
            timer++;
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            graph.proximityUpdate(this);
            Core.app.post(this::containingGraphWithoutSelfFix);
        }
    }
}
