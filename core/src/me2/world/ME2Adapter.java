package me2.world;

import me13.core.block.BlockAngles;
import me2.ME2Configurator;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;

import me2.BuildingSettingsMixin;
import me2.BuildingSettingsMixin.AdapterConnectionType;

public class ME2Adapter extends ME2Block {
    @Override
    public boolean canConnect(BuildPlan self, BuildPlan other) {
        return BlockAngles.angleTo(self, other) == BlockAngles.reverse(self.rotation);
    }

    public ME2Adapter(String name) {
        super(name);
        rotate = true;
        rotateDraw = false;
        quickRotate = true;
    }

    public class ME2AdapterBuild extends ME2Build {
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
                        if(a == AdapterConnectionType.ENABLED) {
                            mass[0]++;
                        } else if(a == AdapterConnectionType.DISABLED) {
                            mass[0]--;
                        }
                    });

            if(mass[0] == 0) {
                return !(building instanceof ME2Build);
            } else return mass[0] > 0;
        }

        @Override
        public boolean canConnectWire(ME2Build other) {
            return other == reversedNearby();
        }
    }
}