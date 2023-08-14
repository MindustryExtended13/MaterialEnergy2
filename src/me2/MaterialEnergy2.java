package me2;

import arc.Core;
import arc.Events;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import me13.core.logger.LogBinder;
import me2.content.ME2Blocks;
import me2.content.ME2Items;
import me2.mixin.ItemStorageMixin;
import me2.mixin.LiquidStorageMixin;
import me2.util.ME2NetGraph;
import mindustry.Vars;
import mindustry.content.Planets;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.graphics.Layer;
import mindustry.mod.Mod;
import java.util.Objects;

import me2.world.*;
import me2.world.ME2Bridge.ME2BridgeBuild;
import me2.world.ME2Block.ME2Build;
import me2.world.ME2Cable.ME2CableBuild;

import static me2.MaterialEnergy2Vars.*;

@SuppressWarnings("unused")
public class MaterialEnergy2 extends Mod {
    public static boolean debugModeEnabled = false;

    public MaterialEnergy2() {
        LOGGER.info("Loaded constructor");

        Events.on(EventType.ClientLoadEvent.class, (ignored) -> {
            LogBinder binder = LOGGER.atInfo().setPrefix("MIXIN");
            var mixins = ME2Configurator.mixins();
            binder.log("Starting loading {} mixins", mixins.size);
            mixins.each(mixin -> {
                binder.log("Mixin init: {}", mixin.name());
                mixin.init();
            });

            debugModeEnabled = Core.settings.getBool("me2-debug-mode");
            Vars.ui.settings.addCategory(MOD_NAME, Icon.crafting, (t) -> {
                t.checkPref("me2-debug-mode", false, (bool) -> {
                    debugModeEnabled = bool;
                });
            });
        });

        Events.run(EventType.Trigger.draw, () -> {
            if(debugModeEnabled) {
                Seq<ME2NetGraph> graphSeq = new Seq<>();
                Vars.world.tiles.eachTile(tile -> {
                    if(tile.build == null) {
                        return;
                    }
                    Building b = tile.build;
                    if(b instanceof ME2Build) {
                        ME2NetGraph graph = ((ME2Build) b).graph;
                        if(!graphSeq.contains(graph)) {
                            graphSeq.add(graph);
                            Draw.draw(Layer.blockBuilding + 10, () -> {
                                if(graph.debugColor == null) {
                                    graph.netDebugColor();
                                }
                                Draw.color(graph.debugColor);
                                Draw.alpha(0.5f);
                                graph.eachBuilding(build -> {
                                    float s = build.block.size * 8;
                                    Fill.rect(build.tile.worldx(), build.tile.worldy(), s, s);
                                });
                                Draw.reset();
                            });
                        }
                    }
                });
            }
        });
    }

    @Override
    public void loadContent() {
        LOGGER.info("ITEMS", "Loading...");
        ME2Items.load();
        LOGGER.info("BLOCKS", "Loading...");
        ME2Blocks.load();
    }

    @Override
    public void init() {
        ME2Configurator.register(new ItemStorageMixin());
        ME2Configurator.register(new LiquidStorageMixin());

        ME2Configurator.register(new BuildingSettingsMixin() {
            @Override
            public void init() {
            }

            @Override
            public String name() {
                return ME2Cable.class.getSimpleName() + "Mixin";
            }

            @Override
            public Seq<Building> connections(Building building) {
                if (!(building instanceof ME2CableBuild)) {
                    return new Seq<>();
                }

                ME2Cable cable = (ME2Cable) building.block;
                if (cable.isGate && !building.enabled) {
                    return new Seq<>();
                }

                if(cable.isJunction) {
                    return new Seq<>();
                }

                return building.proximity()
                        .select(b -> b instanceof ME2Build)
                        .map(b -> (ME2Build) b)
                        .select(b -> b.canConnectWire((ME2CableBuild) building))
                        .map(b -> getConnectionOf(building, b))
                        .select(Objects::nonNull);
            }
        });

        ME2Configurator.register(new BuildingSettingsMixin() {
            @Override
            public void init() {
            }

            @Override
            public String name() {
                return ME2Bridge.class.getSimpleName() + "Mixin";
            }

            @Override
            public Seq<Building> connections(Building building) {
                if(!(building instanceof ME2BridgeBuild)) {
                    return new Seq<>();
                }
                ME2BridgeBuild build = (ME2BridgeBuild) building;
                Seq<Building> out = new Seq<>();
                if(build.validLink()) {
                    Building link = build.link();
                    out.add(link);
                    out.add(connections(link));
                }
                Building x = MaterialEnergy2Vars.getConnectionOf(build, build.nearby());
                if(x != null) out.add(x);
                return out;
            }
        });

        ME2Configurator.register(new BuildingSettingsMixin() {
            @Override
            public void init() {
            }

            @Override
            public String name() {
                return ME2TransportationBus.class.getSimpleName() + "Mixin";
            }

            @Override
            public int channelsUsage(Building building) {
                return building instanceof ME2TransportationBus.ME2BusBuild ? 1 : 0;
            }
        });

        ME2Configurator.register(new BuildingSettingsMixin() {
            @Override
            public void init() {
            }

            @Override
            public String name() {
                return ME2Block.class.getSimpleName() + "Mixin";
            }

            @Override
            public int channelsGeneration(Building building) {
                if(building instanceof ME2Build) {
                    ME2Block block = (ME2Block) building.block;
                    if(block.typeId == ME2Block.CONTROLLER_TYPE) {
                        return building.enabled && building.canConsume() ? 32 : 0;
                    }
                }
                return 0;
            }

            @Override
            public int channelsUsage(Building building) {
                if(building instanceof ME2Build) {
                    ME2Block build = (ME2Block) building.block;
                    if(
                            build.typeId == ME2Block.ADAPTER_TYPE  ||
                            build.typeId == ME2Block.BALANCER_TYPE ||
                            build.typeId == ME2Block.TERMINAL_TYPE ||
                            build.typeId == ME2Block.SCREEN_TYPE
                    ) {
                        return 1;
                    }
                    if(build.typeId == ME2Block.STORAGE_TYPE) {
                        return build.storageTier;
                    }
                }
                return 0;
            }

            @Override
            public Seq<Building> connections(Building building) {
                if(!(building instanceof ME2Build)) {
                    return new Seq<>();
                }

                ME2Build build = (ME2Build) building;
                ME2Block block = (ME2Block) build.block;

                if(block.typeId == ME2Block.NO_TYPE_NO_C) {
                    return new Seq<>();
                }

                if(block.typeId == ME2Block.ADAPTER_TYPE) {
                    Seq<Building> out = new Seq<>();
                    if(build.enabledChild()) {
                        out.add(build.nearby());
                    }
                    Building x = MaterialEnergy2Vars.getConnectionOf(build, build.reversedNearby());
                    if(x != null) out.add(x);
                    return out;
                }

                return connectionDefault(building);
            }
        });
    }

    public static Seq<Building> connectionDefault(Building building) {
        return building.proximity()
                .select(b -> b instanceof ME2Build)
                .map(b -> (ME2Build) b)
                .map(b -> MaterialEnergy2Vars.getConnectionOf(building, b))
                .select(b -> b instanceof ME2Build);
    }
}