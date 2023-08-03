package me2.content;

import arc.graphics.Color;
import mindustry.type.Item;

public class ME2Items {
    public static Item
            //ore
            quartzCrystal, chargedQuartzCrystal, shiftingCrystal,
            //pure
            pureQuartzCrystal, chargedPureQuartzCrystal;

    public static void load() {
        Color tmp2 = Color.valueOf("87CEEB");
        Color tmp = Color.valueOf("C2BFFB");

        shiftingCrystal = new Item("shifting-crystal", Color.valueOf("985BB0"));
        pureQuartzCrystal = new Item("pure-quartz-crystal", tmp);
        quartzCrystal = new Item("quartz-crystal", tmp);

        chargedQuartzCrystal = new Item("charged-quartz-crystal", tmp2) {{
            charge = 5f;
        }};

        chargedPureQuartzCrystal = new Item("pure-charged-quartz-crystal", tmp2) {{
            charge = 5f;
        }};
    }
}