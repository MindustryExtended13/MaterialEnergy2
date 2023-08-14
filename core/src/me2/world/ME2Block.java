package me2.world;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Point2;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import me13.core.block.BlockAngles;
import me13.core.block.instance.AdvancedBlock;
import me13.core.graphics.TextDraw;
import me13.core.items.IllegalItemSelection;
import me13.core.multicraft.ImageInt;
import me2.BuildingSettingsMixin;
import me2.ME2Configurator;
import me2.mixin.ItemStorageMixin;
import me2.mixin.LiquidStorageMixin;
import me2.util.ME2NetGraph;
import me2.util.TerminalDialog;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.Liquid;

public class ME2Block extends AdvancedBlock {
    public static final int
            NO_TYPE_NO_C = -1,
            NO_TYPE = 0,
            ADAPTER_TYPE = 1,
            BALANCER_TYPE = 2,
            TERMINAL_TYPE = 3,
            CONTROLLER_TYPE = 4,
            SCREEN_TYPE = 5;

    public TextureRegion teamRegion;
    public TextureRegion rotatorRegion;
    public int typeId = NO_TYPE_NO_C;
    public float textWidth = -1;

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

    public void storageScreen() {
        configurable = true;

        config(Point2.class, (ME2Build build, Point2 content) -> {
            boolean isItem = content.x == 1;
            if(isItem) {
                build.config = Vars.content.item(content.y);
            } else {
                build.config = Vars.content.liquid(content.y);
            }
        });

        configClear((ME2Build build) -> {
            build.config = null;
        });
    }

    @Override
    public void load() {
        super.load();
        teamRegion = Core.atlas.find(name + "-team");
        rotatorRegion = Core.atlas.find(name + "-rotator");
    }

    @Override
    protected TextureRegion[] icons() {
        if(typeId == TERMINAL_TYPE) {
            return new TextureRegion[] {region, teamRegion};
        }
        return super.icons();
    }

    public class ME2Build extends AdvancedBuild {
        public ME2NetGraph graph = new ME2NetGraph();
        public boolean graphEnabled = true;
        public boolean changed = false;
        public float rotation;
        public int tmp = -1;
        public int timer = 0;
        public UnlockableContent config;

        public float getWidth() {
            return textWidth;
        }

        public void text(String str) {
            TextDraw.text(x, y, getWidth(), Color.cyan, str);
        }

        public void onGraphEdit() {
            graphEnabled = graph.isEnabledTmp;
            changed = true;
        }

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

        public float controllerScl() {
            return (float) graph.totalChannelsUsage() / graph.totalChannelsGeneration();
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

        public String terminalName() {
            return block.name + " Dialog";
        }

        @Override
        public void buildConfiguration(Table table) {
            if(typeId == TERMINAL_TYPE) {
                new TerminalDialog(this).show();
            }
            if(typeId == SCREEN_TYPE) {
                Seq<UnlockableContent> x = new Seq<>();
                x.add(Vars.content.liquids());
                x.add(Vars.content.items());
                IllegalItemSelection.buildTable(table, x, () -> config, (c) -> this.config = c);
            }
        }

        @Override
        public void draw() {
            super.draw();
            if(typeId == ME2Block.SCREEN_TYPE) {
                if(!enabled || !graphEnabled || config == null) {
                    text("off");
                } else {
                    float amount = 0;
                    if(config instanceof Item) {
                        amount = graph.amount(ItemStorageMixin.class, config.id);
                    }
                    if(config instanceof Liquid) {
                        amount = graph.amount(LiquidStorageMixin.class, config.id);
                    }
                    text(config.localizedName + "\n" + ImageInt.formatAmount(amount));
                }
            }
            if(typeId == ME2Block.CONTROLLER_TYPE) {
                Draw.rect(rotatorRegion, x, y, rotation);
            }
            if(typeId == ME2Block.TERMINAL_TYPE) {
                Draw.color(team.color);
                Draw.rect(teamRegion, x, y);
                Draw.reset();
            }
        }

        @Override
        public void updateTile() {
            super.updateTile();
            containingGraphWithoutSelfFix();

            //if(timer % 30 == 0) {
            //    //fix bug No.2 with the nets
            //    graph.proximityUpdate(this);
            //}

            if(graphEnabled && enabled && canConsume()) {
                if(typeId == CONTROLLER_TYPE) {
                    rotation += 10 * controllerScl();
                }

                if(typeId == BALANCER_TYPE && timer % 60 == 59) {
                    Fx.healBlockFull.at(x, y, block.size, Color.cyan, block);
                    Vars.content.liquids().each(liquid -> {
                        graph.balance(LiquidStorageMixin.class, liquid.id, false);
                    });
                    Vars.content.items().each(item -> {
                        graph.balance(ItemStorageMixin.class, item.id, true);
                    });
                }
            }

            if(tmp == -1) {
                tmp = enabled ? 1 : 0;
            }
            if((tmp == 1) != enabled) {
                onEnableChange();
            }
            tmp = enabled ? 1 : 0;
            timer++;

            Core.app.post(() -> {
                if(changed) {
                    changed = false;
                }
            });
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            graph.proximityUpdate(this);
            Core.app.post(this::containingGraphWithoutSelfFix);
        }
    }
}
