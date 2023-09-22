package me2.content;

import arc.graphics.Color;
import logicfix.LStatement;
import logicfix.LStatementData;
import logicfix.LStatementEntry;
import logicfix.registry.LStatementRegistry;
import logicfix.registry.RegistryObject;
import me2.MaterialEnergy2Vars;
import me2.world.ME2TransportationBus.ME2BusBuild;
import mindustry.gen.Building;
import mindustry.logic.LCategory;
import mindustry.type.Item;
import mindustry.type.Liquid;

public class ME2Logic {
    public static final LStatementRegistry REGISTRY = new LStatementRegistry().prefixEnabled(true).prefix("me2");
    public static final LCategory ME2 = new LCategory("me2", Color.coral) {
        @Override
        public String localized() {
            return MaterialEnergy2Vars.MOD_NAME;
        }
    };

    public static final RegistryObject<LStatement> SENSOR_LIQUID = REGISTRY.register("sensorliquid", () -> {
        return new LStatement().category(ME2).data(new LStatementData()
                .add(LStatementEntry.field("result", "var"))
                .add(LStatementEntry.literal(" = liquid in "))
                .add(LStatementEntry.field("build", "build"))
        ).executor((statement, exec) -> {
            Building build = exec.building(statement.valueMap.get("build"));

            Liquid out = null;
            if(build instanceof ME2BusBuild busBuild) {
                out = busBuild.configurationLiquid;
            }

            exec.setobj(statement.valueMap.get("var"), out);
        });
    });

    public static final RegistryObject<LStatement> SENSOR_ITEM = REGISTRY.register("sensoritem", () -> {
        return new LStatement().category(ME2).data(new LStatementData()
                .add(LStatementEntry.field("result", "var"))
                .add(LStatementEntry.literal(" = isEnabled "))
                .add(LStatementEntry.field("item", "item"))
                .add(LStatementEntry.literal(" in "))
                .add(LStatementEntry.field("build", "build"))
        ).executor((statement, exec) -> {
            Building build = exec.building(statement.valueMap.get("build"));
            Object config = exec.obj(statement.valueMap.get("item"));

            boolean result = false;
            if(config instanceof Item item) {
                if(build instanceof ME2BusBuild busBuild) {
                    result = busBuild.itemData.isToggled(item);
                }
            }

            exec.setbool(statement.valueMap.get("var"), result);
        });
    });

    public static final RegistryObject<LStatement> CONTROL = REGISTRY.register("control", () -> {
        return new LStatement().category(ME2).data(new LStatementData()
                .add(LStatementEntry.select(Control.values(), Control.LIQUID, "type"))
                .add(LStatementEntry.literal(" build: "))
                .add(LStatementEntry.field("null", "build"))
                .add(LStatementEntry.literal("config: "))
                .add(LStatementEntry.field("null", "config"))).executor((statement, exec) -> {
            Control type = Control.valueOf(String.valueOf(statement.keyMap.get("type")));
            Building build = exec.building(statement.valueMap.get("build"));
            Object config = exec.obj(statement.valueMap.get("config"));

            if(!(build instanceof ME2BusBuild) || config == null) {
                return;
            }

            ME2BusBuild busBuild = (ME2BusBuild) build;
            if(type == Control.LIQUID) {
                if(!(config instanceof Liquid)) {
                    return;
                }

                busBuild.configurationLiquid = (Liquid) config;
            } else {
                if(!(config instanceof Item)) {
                    return;
                }

                Item item = (Item) config;
                if(type == Control.ENABLE) {
                    busBuild.itemData.enable(item);
                } else if(type == Control.DISABLE) {
                    busBuild.itemData.disable(item);
                } else if(type == Control.TOGGLE) {
                    busBuild.itemData.toggle(item);
                }
            }
        });
    });

    public enum Control {
        LIQUID,
        ENABLE,
        DISABLE,
        TOGGLE
    }
}