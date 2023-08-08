package me2.world;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.Time;
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
import mindustry.input.Placement;
import mindustry.world.Tile;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class ME2Bridge extends ME2Block {
    private static BuildPlan otherReq;

    //for autolink
    public @Nullable ME2BridgeBuild lastBuild;

    public TextureRegion[] start;
    public TextureRegion[] mid;
    public int maxLength = 5;

    public ME2Bridge(String name) {
        super(name);
        rotate = true;
        rotateDraw = false;
        quickRotate = true;
        configurable = true;
        underBullets = true;
        noUpdateDisabled = true;

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

    public Tile findLink(int x, int y){
        Tile tile = world.tile(x, y);
        if(tile != null && lastBuild != null && linkValid(tile, lastBuild.tile)
                && lastBuild.tile != tile && lastBuild.linked == -1){
            return lastBuild.tile;
        }
        return null;
    }

    public boolean linkValid(Tile tile, Tile other){
        return linkValid(tile, other, true);
    }

    public boolean linkValid(Tile tile, Tile other, boolean checkDouble) {
        if(other == null || tile == null || !positionsValid(tile.x, tile.y, other.x, other.y)) return false;

        return ((other.block() == tile.block() && tile.block() == this) ||
                (!(tile.block() instanceof ME2Bridge) && other.block() == this))
                && (other.team() == tile.team() || tile.block() != this)
                && (!checkDouble || ((ME2BridgeBuild)other.build).linked != tile.pos());
    }

    public boolean positionsValid(int x1, int y1, int x2, int y2){
        if(x1 == x2){
            return Math.abs(y1 - y2) <= maxLength;
        }else if(y1 == y2){
            return Math.abs(x1 - x2) <= maxLength;
        }else{
            return false;
        }
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);
        for(int i = 0; i < 4; i++){
            Drawf.dashLine(Pal.placing,
                    x * tilesize + Geometry.d4[i].x * (tilesize / 2f + 2),
                    y * tilesize + Geometry.d4[i].y * (tilesize / 2f + 2),
                    x * tilesize + Geometry.d4[i].x * (maxLength) * tilesize,
                    y * tilesize + Geometry.d4[i].y * (maxLength) * tilesize);
        }
        Draw.reset();
    }

    @Override
    public void init() {
        super.init();
        updateClipRadius((maxLength + 0.5f) * tilesize);
    }

    @Override
    public void handlePlacementLine(Seq<BuildPlan> plans) {
        for(int i = 0; i < plans.size - 1; i++){
            BuildPlan cur = plans.get(i);
            BuildPlan next = plans.get(i + 1);
            if(positionsValid(cur.x, cur.y, next.x, next.y)) {
                cur.config = new Point2(next.x - cur.x, next.y - cur.y);
            }
        }
    }

    @Override
    public boolean canConnect(BuildPlan self, BuildPlan other) {
        return BlockAngles.angleTo(self, other) == self.rotation;
    }

    @Override
    public void drawPlanConfigTop(BuildPlan plan, Eachable<BuildPlan> list) {
        otherReq = null;
        list.each(other -> {
            if(other.block == this && plan != other && plan.config instanceof Point2 &&
                    ((Point2) plan.config).equals(other.x - plan.x, other.y - plan.y)) {
                otherReq = other;
            }
        });

        if(otherReq != null){
            drawBridge(
                    plan.x, plan.y, otherReq.x, otherReq.y,
                    plan.drawx(), plan.drawy(),
                    otherReq.drawx(), otherReq.drawy()
            );
        }
    }

    @Override
    public void load() {
        super.load();
        start = AdvancedBlockHelper.loadRegions(this, "-start-", EnumTextureMapping.ROT);
        mid = new TextureRegion[2];
        mid[0] = Core.atlas.find(name + "-mid-0");
        mid[1] = Core.atlas.find(name + "-mid-1");
    }

    @Override
    public void changePlacementPath(Seq<Point2> points, int rotation){
        Placement.calculateNodes(points, this, rotation, (point, other) ->
                Math.max(Math.abs(point.x - other.x), Math.abs(point.y - other.y)) <= maxLength);
    }

    public class ME2BridgeBuild extends ME2Build {
        public int linked = -1;

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
        public void pickedUp() {
            linked = -1;
        }

        @Override
        public void playerPlaced(Object config) {
            super.playerPlaced(config);

            Tile link = findLink(tile.x, tile.y);
            if(linkValid(tile, link) && this.linked != link.pos() && !proximity.contains(link.build)) {
                link.build.configure(tile.pos());
            }

            lastBuild = this;
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
            Drawf.select(x, y, tile.block().size * tilesize / 2f + 2f, Pal.accent);

            for(int i = 1; i <= maxLength; i++) {
                for(int j = 0; j < 4; j++) {
                    Tile other = tile.nearby(Geometry.d4[j].x * i, Geometry.d4[j].y * i);
                    if(linkValid(tile, other)) {
                        boolean linked = other.pos() == this.linked;

                        Drawf.select(other.drawx(), other.drawy(), other.block().size *
                                tilesize / 2f + 2f + (linked ? 0f : Mathf.absin(Time.time, 4f, 1f)),
                                linked ? Pal.place : Pal.breakInvalid);
                    }
                }
            }
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            if(validLink()) {
                Building linked = link();
                Drawf.line(Pal.place, x, y, linked.x, linked.y);
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