package me2.util;

import arc.Core;
import arc.scene.ui.layout.Table;
import me2.SimpleStorageMixin;
import me2.mixin.ItemStorageMixin;
import me2.mixin.LiquidStorageMixin;
import me2.world.ME2Block.ME2Build;
import mindustry.Vars;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Icon;
import mindustry.ui.dialogs.BaseDialog;

public class TerminalDialog extends BaseDialog {
    public ME2Build build;

    public TerminalDialog(ME2Build build) {
        super(build.terminalName());
        this.build = build;
        build(true);
        onResize(() -> {
            build(false);
        });
    }

    private<MIXIN extends SimpleStorageMixin> void _func_359023(Table table,
                                                                Class<MIXIN> mixin,
                                                                UnlockableContent cont) {
        table.table(info -> {
            info.image(cont.uiIcon).left();
            info.table(data -> {
                data.add(cont.localizedName).left().row();
                data.add(build.graph.amount(mixin, cont.id) + "/" +
                        build.graph.amountCapacity(mixin, cont.id)).left();
            }).padLeft(6).grow().left();
        }).left().pad(6);
    }

    private void buildHandler() {
        buttons.clearChildren();
        cont.clearChildren();

        buttons.defaults().size(250, 50);
        buttons.button("@exit", Icon.left, this::hide);
        buttons.button("@me2.reload", Icon.rotate, this::buildHandler);

        int rows = Core.graphics.getWidth()/350;
        cont.pane(t -> {
            final int[] j = {0};
            Runnable hand = () -> {
                if(j[0]++ % rows == rows - 1) {
                    t.row();
                }
            };

            Vars.content.items().each(item -> {
                _func_359023(t, ItemStorageMixin.class, item);
                hand.run();
            });

            Vars.content.liquids().each(liquid -> {
                _func_359023(t, LiquidStorageMixin.class, liquid);
                hand.run();
            });
        }).grow();
    }

    public void build(boolean rebuild) {
        buildHandler();
        if(rebuild) {
            buildHandler();
        }
    }
}