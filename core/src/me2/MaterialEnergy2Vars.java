package me2;

import arc.struct.Seq;
import me13.core.block.BlockAngles;
import me13.core.logger.ILogger;
import me13.core.logger.LoggerFactory;
import me2.world.ME2Cable;
import mindustry.gen.Building;

/** Class where storage all MaterialEnergy2 (ME2) variables */
public class MaterialEnergy2Vars {
    /** ME2 mod name */
    public static final String MOD_NAME = "Material Energy 2";
    /** ME2 main LOGGER that used to print with ME2 logger prefix */
    public static final ILogger LOGGER = LoggerFactory.build("MaterialEnergy2");

    /** Adds to array all building and array was graph content (can be) */
    public static void graphNetRaw(Building building, Seq<Building> buildings) {
        if(building == null || buildings == null) {
            return;
        }

        connectionsOf(building).each(build -> {
            if(!buildings.contains(build)) {
                buildings.add(build);
                graphNetRaw(build, buildings);
            }
        });
    }

    /**
     * Returns building channels generation
     * @param building building to use
     * @return channels
     */
    public static int channelsGeneration(Building building) {
        return ME2Configurator
                .select(BuildingSettingsMixin.class)
                .sum(mixin -> mixin.channelsGeneration(building));
    }

    /**
     * Returns building channels usage
     * @param building building to use
     * @return channels
     */
    public static int channelsUsage(Building building) {
        return ME2Configurator
                .select(BuildingSettingsMixin.class)
                .sum(mixin -> mixin.channelsUsage(building));
    }

    /**
     * Returns buildings connection list for building
     * @param building building to use
     * @return connections
     */
    public static Seq<Building> connectionsOf(Building building) {
        return ME2Configurator
                .select(BuildingSettingsMixin.class)
                .map(mixin -> mixin.connections(building))
                .flatten();
    }

    /** Used to wires logic: junctions, switches */
    public static Building getConnectionOf(Building host, Building building) {
        if(building instanceof ME2Cable.ME2CableBuild) {
            ME2Cable cable = (ME2Cable) building.block;
            if(cable.isJunction) {
                return getConnectionOf(building, building.nearby(BlockAngles.angleTo(host, building)));
            }

            if(cable.isGate && !building.enabled) {
                return null;
            }
        }

        return building;
    }
}