package me2.world;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import me13.core.block.BlockAngles;
import me13.core.block.instance.AdvancedBlockHelper;
import me13.core.block.instance.EnumTextureMapping;
import mindustry.Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.blocks.distribution.ItemBridge;

import static mindustry.Vars.world;

public class ME2Bridge extends ME2Block {
    @Override
    public boolean canConnect(BuildPlan self, BuildPlan other) {
        return BlockAngles.angleTo(self, other) == self.rotation;
    }

    public TextureRegion[] start;
    public TextureRegion[] mid;
    public int maxLength = 5;

    public ME2Bridge(String name) {
        super(name);
        rotate = true;
        rotateDraw = false;
        quickRotate = true;
        configurable = true;

        config(Point2.class, (ME2BridgeBuild build, Point2 i) -> {
            Point2 point2 = i.cpy().add(build.tileX(), build.tileY());
            build.configure(Vars.world.build(point2.x, point2.y));
            Core.app.post(() -> {
                build.graph.proximityUpdate(build);
            });
        });

        configClear(ME2BridgeBuild::unlink);
    }

    @Override
    public void load() {
        super.load();
        start = AdvancedBlockHelper.loadRegions(this, "-start-", EnumTextureMapping.ROT);
        mid = new TextureRegion[2];
        mid[0] = Core.atlas.find(name + "-mid-0");
        mid[1] = Core.atlas.find(name + "-mid-1");
    }

    public class ME2BridgeBuild extends ME2Build {
        public Building linked = null;

        public Seq<ME2BridgeBuild> bridgeSortedProximity() {
            return bridgeProximity().select(this::bridgeConnectable).map(b -> (ME2BridgeBuild) b);
        }

        public Seq<Building> bridgeProximity() {
            Seq<Building> out = new Seq<>();
            int tx = tileX();
            int ty = tileY();
            for(int i = 0; i < 4; i++) {
                Point2 point2 = Geometry.d4(i);
                for(int j = 1; j <= maxLength; j++) {
                    out.add(Vars.world.build(tx + (point2.x * j), ty  + (point2.y * j)));
                }
            }
            return out;
        }

        public boolean bridgeConnectable(Building building) {
            return building != null && building.team == team &&
                    building.block == ME2Bridge.this && ((ME2BridgeBuild) building).linked != this;
        }

        public boolean validLink() {
            return linked != null && linked.isValid();
        }

        public void unlink() {
            linked = null;
            graph.proximityUpdate(this);
        }

        public void configure(Building other) {
            configure(other, false);
        }

        public void configure(Building other, boolean toggle) {
            if(other != this && bridgeProximity().contains(other) && bridgeConnectable(other)) {
                if(toggle) {
                    if(linked == other) {
                        unlink();
                    } else {
                        linked = other;
                    }
                } else {
                    linked = other;
                }
            }

            graph.proximityUpdate(this);
        }

        @Override
        public Point2 config() {
            if(!validLink()) return null;
            return Point2.unpack(linked.pos()).sub(tileX(), tileY());
        }

        @Override
        public void updateTile() {
            super.updateTile();
            if(!validLink() && linked != null) {
                unlink();
            }
        }

        @Override
        public boolean shouldConsume() {
            return validLink() && enabled;
        }

        @Override
        public boolean onConfigureBuildTapped(Building other) {
            Building link = linked;
            configure(other, true);
            boolean b = super.onConfigureBuildTapped(other);
            if(link != linked) {
                return false;
            } else return b;
        }

        @Override
        public void draw() {
            super.draw();
            if(validLink()) {
                int at = BlockAngles.angleTo(this, linked);
                TextureRegion r = mid[at == 1 || at == 3 ? 1 : 0];
                Draw.draw(Layer.blockBuilding + 10, () -> {
                    Draw.rect(start[at], x, y);
                    int sx = tileX();
                    int sy = tileY();
                    int lx = linked.tileX();
                    int ly = linked.tileY();
                    switch(at) {
                        case 0:
                            for(int x = sx + 1; x < lx; x++) {
                                Draw.rect(r, x * 8, y);
                            }
                            break;
                        case 2:
                            for(int x = sx - 1; x > lx; x--) {
                                Draw.rect(r, x * 8, y);
                            }
                            break;
                        case 1:
                            for(int y = sy + 1; y < ly; y++) {
                                Draw.rect(r, x, y * 8);
                            }
                            break;
                        case 3:
                            for(int y = sy - 1; y > ly; y--) {
                                Draw.rect(r, x, y * 8);
                            }
                            break;
                    }
                    Draw.rect(start[BlockAngles.reverse(at)], linked.x, linked.y);
                });
            }
        }

        @Override
        public boolean canConnectWire(ME2Build other) {
            return other == nearby();
        }

        @Override
        public void drawConfigure() {
            Drawf.select(x, y, size * 8 - 2, Pal.accent);
            bridgeSortedProximity().forEach(bridge -> {
                if(bridge != linked) {
                    Drawf.select(bridge.x, bridge.y, bridge.block.size * 8 - 2, Pal.accent);
                }
            });
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            Drawf.select(x, y, size * 8 - 2, Pal.place);
            if(validLink()) {
                Drawf.line(Pal.place, x, y, linked.x, linked.y);
                Drawf.select(linked.x, linked.y, linked.block.size * 8 - 2, Pal.place);
            }
        }

        @Override
        public void read(Reads read, byte revision) {
            int pos = read.i();
            linked = pos == -1 ? null : Vars.world.build(pos);
        }

        @Override
        public void write(Writes write) {
            write.i(validLink() ? linked.pos() : -1);
        }
    }
}