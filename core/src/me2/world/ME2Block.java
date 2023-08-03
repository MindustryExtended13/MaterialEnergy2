package me2.world;

import arc.Core;
import me13.core.block.instance.AdvancedBlock;
import me2.MaterialEnergy2Vars;
import me2.net.ME2NetGraph;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;

public class ME2Block extends AdvancedBlock {
    public boolean canConnect(BuildPlan self, BuildPlan other) {
        return true;
    }

    public ME2Block(String name) {
        super(name);
        update = true;
    }

    public class ME2Build extends AdvancedBuild {
        public ME2NetGraph graph = new ME2NetGraph();
        public int tmp = -1;
        //public int timer = 0;

        public void onEnableChange() {
            graph.proximityUpdate(this);
        }

        public boolean canConnectWire(ME2Build other) {
            return true;
        }

        public void containingGraphWithoutSelfFix() {
            if(!graph.contains(this)) {
                graph = new ME2NetGraph();
                graph.proximityUpdate(this);
            }
        }

        @Override
        public void updateTile() {
            super.updateTile();
            containingGraphWithoutSelfFix();

            //if(timer++ % 30 == 0) {
            //    //fix bug No.2 with the nets
            //    graph.proximityUpdate(this);
            //}

            if(tmp == -1) {
                tmp = enabled ? 1 : 0;
            }
            if((tmp == 1) != enabled) {
                onEnableChange();
            }
            tmp = enabled ? 1 : 0;
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            graph.proximityUpdate(this);
            Core.app.post(this::containingGraphWithoutSelfFix);
        }
    }
}
