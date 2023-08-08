package me2.world;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import arc.util.io.Reads;
import arc.util.io.Writes;
import me13.core.items.IllegalItemSelection;
import me13.core.items.multiitem.MultiItemData;
import me13.core.items.multiitem.MultiItemSelection;
import me2.mixin.ItemStorageMixin;
import me2.mixin.LiquidStorageMixin;
import me2.world.ME2Block;
import mindustry.Vars;
import mindustry.ctype.ContentType;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.Liquid;

public class ME2TransportationBus extends ME2Block {
    public TextureRegion topRegion;
    public boolean isImport;

    public ME2TransportationBus(String name, boolean isImport) {
        super(name);
        this.isImport = isImport;
        typeId = ME2Block.NO_TYPE;
        hasItems = hasLiquids = true;
        outputsLiquid = !isImport;
        acceptsItems = isImport;
        liquidCapacity = 1;
        itemCapacity = 1;

        config(TransportationBusConfig.class, (ME2BusBuild b, TransportationBusConfig value) -> {
            b.configurationLiquid = Vars.content.liquid(value.liquidID);
            b.itemData.clear();
            for(int i : value.config) {
                b.itemData.enable(Vars.content.item(i));
            }
        });

        configClear((ME2BusBuild build) -> {
            build.configurationLiquid = null;
            build.itemData.clear();
        });
    }

    @Override
    public void load() {
        super.load();
        topRegion = Core.atlas.find(name + "-top");
    }

    @Override
    public boolean outputsItems() {
        return super.outputsItems() && !isImport;
    }

    public class ME2BusBuild extends ME2Build {
        public MultiItemData<Item> itemData = MultiItemData.create(ContentType.item);
        public Liquid configurationLiquid;
        public int index = 0;

        public Item getIndexItem() {
            return Vars.content.item(itemData.config()[index % itemData.length()]);
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return isImport && liquids.get(liquid) < liquidCapacity;
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return isImport && items.get(item) < getMaximumAccepted(item);
        }

        @Override
        public TransportationBusConfig config() {
            return new TransportationBusConfig(this);
        }

        @Override
        public void buildConfiguration(Table table) {
            table.defaults().top().pad(3);
            //ol in bundles - Original Login or Oxygen Liquid or Oxygen Login
            MultiItemSelection.buildTable(table, itemData);
            IllegalItemSelection.buildTable(table, Vars.content.liquids(),
                    () -> configurationLiquid, (liquid) -> configurationLiquid = liquid);
        }

        @Override
        public void write(Writes write) {
            write.i(configurationLiquid == null ? -1 : configurationLiquid.id);
            itemData.write(write);
        }

        @Override
        public void read(Reads read, byte revision) {
            configurationLiquid = Vars.content.liquid(read.i());
            itemData.read(read);
        }

        @Override
        public void draw() {
            super.draw();
            if(!isImport && itemData.length() > 0) {
                Draw.color(getIndexItem().color);
                Draw.rect(topRegion, x, y);
                Draw.color();
            }
        }

        @Override
        public void updateTile() {
            super.updateTile();

            if(!graphEnabled) {
                return;
            }

            if(!isImport) {
                if(itemData.length() > 0) {
                    if(timer % 20 == 19) {
                        index++;
                    }
                    dump(getIndexItem());
                }
                if(configurationLiquid != null) {
                    dumpLiquid(configurationLiquid);
                    liquids.add(configurationLiquid, (int) (1- graph.remove(
                            LiquidStorageMixin.class, configurationLiquid.id, 1)));
                }

                for(int i : itemData.config()) {
                    Item item = Vars.content.item(i);
                    if(item == null || items.get(item) > 0) continue;
                    items.add(item, (int) (1-graph.remove(ItemStorageMixin.class, item.id, 1)));
                }
            }

            if(isImport) {
                items.each((item, count) -> {
                    items.remove(item, (int) (1-graph.add(ItemStorageMixin.class, item.id, 1)));
                });
                liquids.each((liquid, count) -> {
                    liquids.remove(liquid, (int) (1-graph.add(LiquidStorageMixin.class, liquid.id, 1)));
                });
            }
        }
    }

    public static class TransportationBusConfig {
        public final int liquidID;
        public final int[] config;

        public TransportationBusConfig(int liquidID, int[] config) {
            this.liquidID = liquidID;
            this.config = config;
        }

        public TransportationBusConfig(Liquid liquid, int[] config) {
            this(liquid == null ? -1 : liquid.id, config);
        }

        public TransportationBusConfig(ME2BusBuild build) {
            this(build.configurationLiquid, build.itemData.config());
        }
    }
}