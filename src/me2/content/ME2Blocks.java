package me2.content;

import arc.struct.Seq;
import me13.core.block.instance.EnumTextureMapping;
import me13.core.block.instance.Layer;
import me13.core.multicraft.DrawRecipe;
import me13.core.multicraft.IOEntry;
import me13.core.multicraft.MultiCrafter;
import me13.core.multicraft.Recipe;
import me2.world.ME2Block;
import me2.world.ME2Bridge;
import me2.world.ME2Cable;
import me2.world.ME2TransportationBus;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.entities.Effect;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.draw.*;

public class ME2Blocks {
    public static Block cable, cableJunction, cableSwitch, adapter, bridge, balancer, exportBus, importBus,
            controller, terminal, quartzFurnace, charger, growTurbine, quartzORe, quartzMine, screen;

    public static void load() {
        quartzORe = new OreBlock(ME2Items.quartzCrystal) {{
            oreDefault = true;
            oreThreshold = 0.81f;
            oreScale = 23.47619f;
        }};

        screen = new ME2Block("storage-screen") {{
            size = 2;
            storageScreen();
            typeId = ME2Block.SCREEN_TYPE;
            textWidth = 8*2-((9/64f)*(8*2))*2;
            requirements(Category.distribution, ItemStack.with(
                    ME2Items.chargedPureQuartzCrystal, 12,
                    ME2Items.quartzCrystal, 10,
                    Items.copper, 75,
                    Items.graphite, 50,
                    Items.silicon, 25
            ));
        }};

        cable = new ME2Cable("cable") {{
            drawBase = false;
            requirements(Category.distribution, ItemStack.with(
                    ME2Items.chargedQuartzCrystal, 1,
                    ME2Items.pureQuartzCrystal, 1
            ));
            layers.add(new Layer(this, "-", EnumTextureMapping.TF_TYPE) {{
                this.hand = ME2Cable.DEFAULT_HAND;
                this.hand2 = ME2Cable.DEFAULT_SCHEME_HAND;
            }});
        }};

        Effect X = ((GenericCrafter) Blocks.siliconSmelter).craftEffect;
        quartzMine = new GenericCrafter("quartz-mine") {{
            size = 2;
            craftTime = 300;
            craftEffect = X;
            outputItems = ItemStack.with(ME2Items.quartzCrystal, 2);
            consumePower(1);
            requirements(Category.production, ItemStack.with(
                    Items.copper, 175, Items.graphite, 125
            ));
            drawer = new DrawMulti(new DrawDefault(), new DrawRegion("-rotator") {{
                this.rotateSpeed = 5;
                this.spinSprite = true;
            }}, new DrawRegion("-top"));
        }};

        quartzFurnace = new GenericCrafter("quartz-furnace") {{
            size = 2;
            craftTime = 60;
            craftEffect = X;
            outputItems = ItemStack.with(ME2Items.shiftingCrystal, 4);
            consumeItems(ItemStack.with(
                    ME2Items.chargedPureQuartzCrystal, 1,
                    ME2Items.chargedQuartzCrystal, 1,
                    Items.blastCompound, 1,
                    Items.coal, 1
            ));
            requirements(Category.crafting, ItemStack.with(
                    Items.copper, 75, Items.graphite, 25
            ));
            drawer = new DrawMulti(new DrawDefault(), new DrawFlame());
        }};

        growTurbine = new GenericCrafter("grow-turbine") {{
            size = 2;
            craftTime = 600;
            craftEffect = X;
            hasLiquids = true;
            liquidCapacity = 48;
            outputItems = ItemStack.with(ME2Items.pureQuartzCrystal, 1);
            consumeItems(ItemStack.with(ME2Items.quartzCrystal, 1, Items.sand, 2));
            requirements(Category.crafting, ItemStack.with(
                    Items.copper, 175, Items.graphite, 75, Items.silicon, 25
            ));
            consumeLiquid(Liquids.water, 0.4f);
            drawer = new DrawMulti(
                    new DrawRegion("-bottom"),
                    new DrawLiquidRegion(Liquids.water),
                    new DrawDefault()
            );
        }};

        charger = new MultiCrafter("charger") {{
            craftEffect = X;
            showNameTooltip = true;
            drawer = new DrawRecipe() {{
                drawers = new DrawBlock[] {
                        new DrawMulti(
                                new DrawDefault(),
                                new DrawRegion("-top1")
                        ),
                        new DrawMulti(
                                new DrawDefault(),
                                new DrawRegion("-top2")
                        )
                };
            }};
            recipes = new Recipe[] {
                    new Recipe(
                            new IOEntry(ItemStack.list(ME2Items.quartzCrystal, 1), Seq.with(), 2),
                            new IOEntry(ItemStack.list(ME2Items.chargedQuartzCrystal, 1))
                    ),
                    new Recipe(
                            new IOEntry(ItemStack.list(ME2Items.pureQuartzCrystal, 1), Seq.with(), 1),
                            new IOEntry(ItemStack.list(ME2Items.chargedPureQuartzCrystal, 1))
                    )
            };
            requirements(Category.crafting, ItemStack.with(
                    Items.copper, 50, Items.graphite, 15
            ));
        }};

        terminal = new ME2Block("terminal") {{
            size = 3;
            configurable = true;
            typeId = ME2Block.TERMINAL_TYPE;
            requirements(Category.effect, ItemStack.with(
                    ME2Items.shiftingCrystal, 24,
                    Items.silicon, 50,
                    Items.copper, 25
            ));
        }};

        controller = new ME2Block("me-controller") {{
            requirements(Category.effect, ItemStack.with(
                    ME2Items.shiftingCrystal, 8,
                    ME2Items.quartzCrystal, 12,
                    Items.copper, 50,
                    Items.lead, 25
            ));
            typeId = ME2Block.CONTROLLER_TYPE;
            consumePowerDynamic((ME2Build b) -> {
                return b.controllerScl() * 360;
            });
        }};

        importBus = new ME2TransportationBus("import-bus", true) {{
            requirements(Category.distribution, ItemStack.with(
                    ME2Items.shiftingCrystal, 12,
                    ME2Items.chargedQuartzCrystal, 8,
                    ME2Items.pureQuartzCrystal, 7,
                    Items.copper, 25
            ));
        }};

        exportBus = new ME2TransportationBus("export-bus", false) {{
            requirements(Category.distribution, ItemStack.with(
                    ME2Items.shiftingCrystal, 12,
                    ME2Items.chargedQuartzCrystal, 8,
                    ME2Items.pureQuartzCrystal, 7,
                    Items.copper, 25
            ));
            configurable = true;
        }};

        cableSwitch = new ME2Cable("cable-switch") {{
            isGate = true;
            drawBase = false;
            configurable = true;
            requirements(Category.logic, ItemStack.with(
                    ME2Items.chargedQuartzCrystal, 6,
                    ME2Items.pureQuartzCrystal, 6,
                    ME2Items.shiftingCrystal, 4
            ));
            layers.add(
                    new SwitchLayer(this, "-enabled", EnumTextureMapping.REGION) {{
                        this.activates = true;
                        this.hand = ME2Cable.DEFAULT_HAND;
                        this.hand2 = ME2Cable.DEFAULT_SCHEME_HAND;
                    }},
                    new SwitchLayer(this, "-disabled", EnumTextureMapping.REGION) {{
                        this.activates = false;
                        this.hand = ME2Cable.DEFAULT_HAND;
                        this.hand2 = ME2Cable.DEFAULT_SCHEME_HAND;
                    }}
            );
        }};

        balancer = new ME2Block("balancer") {{
            requirements(Category.effect, ItemStack.with(
                    ME2Items.chargedQuartzCrystal, 10,
                    ME2Items.pureQuartzCrystal, 10,
                    ME2Items.shiftingCrystal, 8,
                    Items.graphite, 45,
                    Items.copper, 25
            ));
            typeId = ME2Block.BALANCER_TYPE;
        }};

        adapter = new ME2Block("adapter") {{
            rotate = true;
            rotateDraw = false;
            quickRotate = true;
            drawBase = false;
            requirements(Category.effect, ItemStack.with(
                    ME2Items.chargedQuartzCrystal, 10,
                    ME2Items.pureQuartzCrystal, 10,
                    ME2Items.shiftingCrystal, 8,
                    Items.graphite, 45,
                    Items.copper, 25
            ));
            layers.add(new Layer(this, "-", EnumTextureMapping.ROT) {{
                this.rotate = false;
            }});
            typeId = ME2Block.ADAPTER_TYPE;
        }};

        cableJunction = new ME2Cable("cable-junction") {{
            isJunction = true;
            requirements(Category.distribution, ItemStack.with(
                    ME2Items.chargedQuartzCrystal, 10,
                    ME2Items.pureQuartzCrystal, 10,
                    Items.copper, 25
            ));
        }};

        bridge = new ME2Bridge("bridge") {{
            drawBase = false;
            requirements(Category.distribution, ItemStack.with(
                    ME2Items.chargedQuartzCrystal, 12,
                    ME2Items.pureQuartzCrystal, 12,
                    Items.copper, 35
            ));
            layers.add(new Layer(this, "-", EnumTextureMapping.ROT) {{
                this.rotate = false;
            }});
        }};
    }
}