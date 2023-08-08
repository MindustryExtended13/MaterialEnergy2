package me2.util;

import arc.func.Cons;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.struct.Seq;
import me2.ME2Configurator;
import me2.MaterialEnergy2Vars;
import me2.SimpleStorageMixin;
import me2.world.ME2Block;
import mindustry.gen.Building;

/**
 * Graph that have all net blocks for building.<br>
 * The graph net stored in builds to get M.E. net
 */
public class ME2NetGraph {
    private final Seq<Building> buildings = new Seq<>();
    public Color debugColor;

    /** Allows to get elements */
    public void eachBuilding(Cons<Building> cons) {
        buildings.each(cons);
    }

    /** Creates random color for debugger */
    public void netDebugColor() {
        debugColor = new Color(Mathf.random(), Mathf.random(), Mathf.random());
    }

    /** Returns true if building in the net */
    public boolean contains(Building building) {
        return buildings.contains(building);
    }

    /** Used by buildings */
    public void proximityUpdate(Building source) {
        buildings.clear();
        Seq<Building> out = new Seq<>();
        MaterialEnergy2Vars.graphNetRaw(source, out);
        register(out);
        if(!buildings.contains(source)) {
            register(source);
        }
    }

    /** Adds building to the graph if not null */
    public void register(Building building) {
        if(building != null) {
            if(building instanceof ME2Block.ME2Build) {
                ((ME2Block.ME2Build) building).graph = this;
            }
            buildings.add(building);
            netDebugColor();
        }
    }

    /** Adds seq contents to the graph if not null */
    public void register(Seq<Building> buildings) {
        if(buildings == null) {
            return;
        }

        for(Building building : buildings) {
            register(building);
        }
    }

    /** Adds graph to the graph if not null */
    public void register(ME2NetGraph graph) {
        if(graph == null) {
            return;
        }
        register(graph.buildings);
    }

    /** Removes building from the graph */
    public void remove(Building building) {
        buildings.remove(building);
        netDebugColor();
    }

    /** Returns true, if input full, false if not full and can be added */
    public<T extends SimpleStorageMixin> boolean isInputFull(Class<T> cl, int id) {
        T mixin = ME2Configurator.getMixinByClass(cl);
        Seq<Building> receive = receiveBuilds(cl, id);
        return receive.sumf(b -> mixin.amount(b, id)) >= receive.sumf(b -> mixin.maximumAccepted(b, id));
    }

    /** Removes amount value from id for mixin and buildings in the graph */
    public<T extends SimpleStorageMixin> float remove(Class<T> cl, int id, float amount) {
        T mixin = ME2Configurator.getMixinByClass(cl);
        Seq<Building> extract = extractBuilds(cl, id);

        for(Building building : extract) {
            if(amount <= 0) {
                return 0;
            }

            amount = mixin.extract(building, id, amount);
        }

        return amount;
    }

    /** Adds amount value to id for mixin and buildings in the graph */
    public<T extends SimpleStorageMixin> float add(Class<T> cl, int id, float amount) {
        T mixin = ME2Configurator.getMixinByClass(cl);
        Seq<Building> receive = receiveBuilds(cl, id);

        for(Building building : receive) {
            if(amount <= 0) {
                return 0;
            }

            amount = mixin.receive(building, id, amount);
        }

        return amount;
    }

    /**
     * Evenly distributes elements in the system in each block
     * @param roundness true, if type int (example: items), false if float (example: liquids)
     */
    public<T extends SimpleStorageMixin> void balance(Class<T> cl, int id, boolean roundness) {
        T mixin = ME2Configurator.getMixinByClass(cl);
        Seq<Building> extract = extractBuilds(cl, id);
        float total = extract.sumf(b -> {
            float amount = mixin.amount(b, id);
            return amount - mixin.extract(b, id, amount);
        });
        Seq<Building> receive = receiveBuilds(cl, id);
        float cof = total / receive.size;
        if(roundness) {
            cof = Mathf.floor(cof);
        }
        float received = 0;

        for(Building building : receive) {
            received += cof - mixin.receive(building, id, cof);
        }

        if(received < total) {
            float left = total - received;
            for(Building building : receive) {
                if(left <= 0) {
                    return;
                }

                left = mixin.receive(building, id, left);
            }
        }
    }

    public boolean isEnabled() {
        return totalChannelsUsage() <= totalChannelsGeneration();
    }

    public int totalChannelsGeneration() {
        return buildings.sum(MaterialEnergy2Vars::channelsGeneration) + 8;
    }

    public int totalChannelsUsage() {
        return buildings.sum(MaterialEnergy2Vars::channelsUsage);
    }

    /** Returns list of buildings that can receive for mixin and id */
    public<T extends SimpleStorageMixin> Seq<Building> receiveBuilds(Class<T> cl, int id) {
        T mixin = ME2Configurator.getMixinByClass(cl);
        return buildings.select(build -> mixin.canReceive(build, id));
    }

    /** Returns list of buildings that can extract for mixin and id */
    public<T extends SimpleStorageMixin> Seq<Building> extractBuilds(Class<T> cl, int id) {
        T mixin = ME2Configurator.getMixinByClass(cl);
        return buildings.select(build -> mixin.canExtract(build, id));
    }
}