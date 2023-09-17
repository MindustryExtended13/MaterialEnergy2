package me2.content;

import arc.func.Cons;
import arc.struct.Seq;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.TechTree;
import mindustry.content.TechTree.TechNode;
import mindustry.ctype.UnlockableContent;
import mindustry.game.Objectives.*;
import mindustry.type.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ME2Tech {
    static TechNode context = null;

    public static void load() {
        margeNode(Blocks.titaniumConveyor, () -> {
            node(ME2Blocks.cable, () -> {
                node(ME2Blocks.cableJunction);
                node(ME2Blocks.cableSwitch);
                node(ME2Blocks.bridge);
                node(ME2Blocks.controller);
            });
            node(ME2Blocks.adapter, () -> {
                node(ME2Blocks.exportBus);
                node(ME2Blocks.importBus);
                node(ME2Blocks.balancer);
            });
        });
        margeNode(Blocks.logicDisplay, () -> {
            node(ME2Blocks.screen, () -> {
                node(ME2Blocks.terminal);
            });
        });
        margeNode(Blocks.mechanicalDrill, () -> {
            node(ME2Blocks.quartzMine, () -> {
                node(ME2Blocks.growTurbine);
            });
        });
        margeNode(Blocks.graphitePress, () -> {
            node(ME2Blocks.quartzFurnace);
        });
        margeNode(Blocks.combustionGenerator, () -> {
            node(ME2Blocks.charger);
        });
        margeNode(Blocks.container, () -> {
            nodeStorage(new int[1]);
        });

        //region: items
        margeNode(Items.copper, () -> {
            nodeProduce(ME2Items.quartzCrystal, () -> {
                nodeProduce(ME2Items.shiftingCrystal);
                nodeProduce(ME2Items.chargedQuartzCrystal);
                nodeProduce(ME2Items.pureQuartzCrystal, () -> {
                    nodeProduce(ME2Items.chargedPureQuartzCrystal);
                });
            });
        });
    }

    private static void nodeStorage(int @NotNull[] index) {
        node(ME2Blocks.storages[index[0]], () -> {
            index[0]++;
            if(index[0] == ME2Blocks.storages.length) {
                return;
            }
            nodeStorage(index);
        });
    }

    private static void margeNode(UnlockableContent parent, Runnable children) {
        context = TechTree.all.find(t -> t.content == parent);
        children.run();
    }

    private static void node(UnlockableContent content, ItemStack[] requirements,
                             Seq<Objective> objectives, Runnable children) {
        TechNode node = new TechNode(context, content, requirements);
        if(objectives != null) node.objectives = objectives;

        TechNode prev = context;
        context = node;
        children.run();
        context = prev;
    }

    private static void node(UnlockableContent content, ItemStack[] requirements, Runnable children) {
        node(content, requirements, null, children);
    }

    private static void node(UnlockableContent content, Seq<Objective> objectives, Runnable children) {
        node(content, content.researchRequirements(), objectives, children);
    }

    private static void node(UnlockableContent content, Runnable children) {
        node(content, content.researchRequirements(), children);
    }

    private static void node(UnlockableContent block) {
        node(block, () -> {});
    }

    private static void nodeProduce(UnlockableContent content,
                                    @NotNull Seq<Objective> objectives, Runnable children) {
        node(content, content.researchRequirements(), objectives.add(new Produce(content)), children);
    }

    private static void nodeProduce(UnlockableContent content, Runnable children) {
        nodeProduce(content, Seq.with(), children);
    }

    private static void nodeProduce(UnlockableContent content) {
        nodeProduce(content, Seq.with(), () -> {});
    }
}