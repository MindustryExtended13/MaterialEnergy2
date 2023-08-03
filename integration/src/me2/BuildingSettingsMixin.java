package me2;

import arc.struct.Seq;
import mindustry.gen.Building;

public interface BuildingSettingsMixin extends Mixin {
    default Seq<Building> connections(Building building) {
        return new Seq<>();
    }

    default int channelsUsage(Building building) {
        return 0;
    }

    default int channelsGeneration(Building building) {
        return 0;
    }

    default AdapterConnectionType type(Building building) {
        return AdapterConnectionType.IGNORE;
    }

    enum AdapterConnectionType {
        ENABLED, DISABLED, IGNORE
    }
}