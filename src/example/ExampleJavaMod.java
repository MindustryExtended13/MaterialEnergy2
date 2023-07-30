package example;

import arc.*;
import arc.graphics.Color;
import arc.util.*;
import example.content.ExampleItems;
import mindustry.mod.*;
import mindustry.type.Item;
import mindustry.ui.dialogs.*;
import mindustry.game.EventType.*;

@SuppressWarnings("unused")
public class ExampleJavaMod extends Mod {
    public ExampleJavaMod() {
        Log.info("Loaded ExampleJavaMod constructor.");

        //listen for game load event
        Events.on(ClientLoadEvent.class, e -> {
            //show dialog upon startup
            Time.runTask(10f, () -> {
                BaseDialog dialog = new BaseDialog("frog");
                dialog.cont.add("behold").row();
                //mod sprites are prefixed with the mod name (this mod is called 'example-java-mod' in its config)
                dialog.cont.image(Core.atlas.find("example-java-mod-frog")).pad(20f).row();
                dialog.cont.button("I see", dialog::hide).size(100f, 50f);
                dialog.show();
            });
        });
    }

    @Override
    public void loadContent() {
        ExampleItems.example = new Item("example", Color.red) {{
            this.hardness = 666;
        }};
    }
}
