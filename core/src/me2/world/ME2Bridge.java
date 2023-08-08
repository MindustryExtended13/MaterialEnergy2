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
import me2.MaterialEnergy2Vars;
import mindustry.Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;

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
            build.linked = i.cpy().add(build.tileX(), build.tileY()).pack();
            Core.app.post(() -> {
                build.graph.proximityUpdate(build);
            });
        });

        configClear(ME2BridgeBuild::unlink);
    }

    public void drawBridge(int tx, int ty, int ltx, int lty, float x1, float y1, float x2, float y2) {
        int at = BlockAngles.angleTo(new Point2(tx, ty), new Point2(ltx, lty));
        TextureRegion r = mid[at == 1 || at == 3 ? 1 : 0];
        Draw.draw(Layer.blockBuilding + 10, () -> {
            Draw.rect(start[at], x1, y1);
            switch(at) {
                case 0:
                    for(int x = tx + 1; x < ltx; x++) {
                        Draw.rect(r, x * 8, y1);
                    }
                    break;
                case 2:
                    for(int x = tx - 1; x > ltx; x--) {
                        Draw.rect(r, x * 8, y1);
                    }
                    break;
                case 1:
                    for(int y = ty + 1; y < lty; y++) {
                        Draw.rect(r, x1, y * 8);
                    }
                    break;
                case 3:
                    for(int y = ty - 1; y > lty; y--) {
                        Draw.rect(r, x1, y * 8);
                    }
                    break;
            }
            Draw.rect(start[BlockAngles.reverse(at)], x2, y2);
        });
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
        public int linked = -1;

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
                    building.block == ME2Bridge.this && ((ME2BridgeBuild) building).linked != pos();
        }

        public Building link() {
            return Vars.world.build(linked);
        }

        public boolean validLink() {
            return linked != -1 && link() != null && link().isValid();
        }

        public void unlink() {
            linked = -1;
            graph.proximityUpdate(this);
        }

        public void configure(Building other, boolean toggle) {
            if(other != this && bridgeProximity().contains(other) && bridgeConnectable(other)) {
                int pos = other.pos();
                if(toggle) {
                    if(linked == pos) {
                        unlink();
                    } else {
                        linked = pos;
                    }
                } else {
                    linked = pos;
                }
            }

            graph.proximityUpdate(this);
        }

        public void dispose() {
            unlink();
            int p = pos();
            bridgeProximity().each(b -> {
                if(b instanceof ME2BridgeBuild) {
                    ME2BridgeBuild build = (ME2BridgeBuild) b;
                    if(build.linked == p) {
                        build.unlink();
                    }
                }
            });
        }

        @Override
        public void updateTile() {
            super.updateTile();
        }

        @Override
        public void remove() {
            super.remove();
            dispose();
        }

        @Override
        public Point2 config() {
            if(!validLink()) return null;
            return Point2.unpack(linked).sub(tileX(), tileY());
        }

        @Override
        public boolean shouldConsume() {
            return validLink() && enabled;
        }

        @Override
        public boolean onConfigureBuildTapped(Building other) {
            int link = linked;
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
                Building l = link();
                drawBridge(tileX(), tileY(), l.tileX(), l.tileY(), x, y, l.x, l.y);
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
                if(bridge.pos() != linked) {
                    Drawf.select(bridge.x, bridge.y, bridge.block.size * 8 - 2, Pal.accent);
                }
            });
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            Drawf.select(x, y, size * 8 - 2, Pal.place);
            if(validLink()) {
                Building linked = link();
                Drawf.line(Pal.place, x, y, linked.x, linked.y);
                Drawf.select(linked.x, linked.y, linked.block.size * 8 - 2, Pal.place);
            }
        }

        @Override
        public void read(Reads read, byte revision) {
            linked = read.i();
        }

        @Override
        public void write(Writes write) {
            write.i(linked);
        }
    }
}