package me2.world;

import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Eachable;
import me13.core.block.ConnectionHand;
import me13.core.block.SchemeConnectionHand;
import me13.core.block.instance.EnumTextureMapping;
import me13.core.block.instance.Layer;
import me2.MaterialEnergy2Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.world.Block;

public class ME2Cable extends ME2Block {
    public static ConnectionHand DEFAULT_HAND = (self, other, tile) -> {
        if(other instanceof ME2CableBuild) {
            ME2Cable cable = (ME2Cable) other.block;
            return !cable.isGate || other.enabled;
        }
        return other instanceof ME2Build && MaterialEnergy2Vars.connectionsOf(other).contains(self);
    };

    public static SchemeConnectionHand DEFAULT_SCHEME_HAND = (self, other, tile) -> {
        if(other == null) return false;
        return other.block instanceof ME2Block && ((ME2Block) other.block).canConnect(other, self);
    };

    public boolean isJunction = false;
    public boolean isGate = false;

    public ME2Cable(String name) {
        super(name);
        solid = false;
        underBullets = true;
        conveyorPlacement = true;
    }

    public class ME2CableBuild extends ME2Build {
        public void onProximityUpdateJunction(Building host, Seq<Building> scanned) {
            graph.proximityUpdate(this);

            if(!isJunction) {
                return;
            }

            scanned.add(this);
            proximity().each(b -> {
                if(b instanceof ME2CableBuild && b != host && !scanned.contains(b)) {
                    ((ME2CableBuild) b).onProximityUpdateJunction(this, scanned);
                }
            });
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            onProximityUpdateJunction(null, new Seq<>());
        }

        @Override
        public void buildConfiguration(Table table) {
            table.button(Icon.eyeOff, () -> {
                enabled = !enabled;
            }).size(48);
        }
    }

    public static class SwitchLayer extends Layer {
        public boolean activates;

        public SwitchLayer(String regionName, EnumTextureMapping mapping) {
            super(regionName, mapping);
        }

        public SwitchLayer(Block block, String prefix, EnumTextureMapping mapping) {
            super(block, prefix, mapping);
        }

        @Override
        public void draw(Building building) {
            if(building.enabled == activates) {
                super.draw(building);
            }
        }

        @Override
        public void drawPlan(BuildPlan plan, Eachable<BuildPlan> all) {
            if(activates) {
                super.drawPlan(plan, all);
            }
        }
    }
}