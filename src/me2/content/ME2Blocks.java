package me2.content;

import me13.core.block.instance.EnumTextureMapping;
import me13.core.block.instance.Layer;
import me2.world.ME2Adapter;
import me2.world.ME2Cable;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;

public class ME2Blocks {
    public static Block cable, cableJunction, cableSwitch, adapter;

    public static void load() {
        cable = new ME2Cable("cable") {{
            drawBase = false;
            requirements(Category.distribution, ItemStack.empty);
            layers.add(new Layer(this, "-", EnumTextureMapping.TF_TYPE) {{
                this.hand = ME2Cable.DEFAULT_HAND;
                this.hand2 = ME2Cable.DEFAULT_SCHEME_HAND;
            }});
        }};

        cableSwitch = new ME2Cable("cable-switch") {{
            isGate = true;
            drawBase = false;
            configurable = true;
            requirements(Category.logic, ItemStack.empty);
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

        adapter = new ME2Adapter("adapter") {{
            drawBase = false;
            requirements(Category.effect, ItemStack.empty);
            layers.add(new Layer(this, "-", EnumTextureMapping.ROT) {{
                this.rotate = false;
            }});
        }};

        cableJunction = new ME2Cable("cable-junction") {{
            isJunction = true;
            requirements(Category.distribution, ItemStack.empty);
        }};
    }
}