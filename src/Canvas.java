import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

class Canvas extends JComponent {
    public static int SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
    public static int SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;


    public static final int TILE_WIDTH = 64;
    public static final int TILE_HEIGHT = TILE_WIDTH / 2;
    Font mainFont;

    boolean debug =false;
    World world;
    public boolean inGame;
    static TileGraphicsHandler tiler = new TileGraphicsHandler();
    private BufferedImage previousCompleteRenderSheet;

    public ArrayList<Object[]> imagesFading = new ArrayList<>();

    public Canvas() {
        // setPreferredSize(new Dimension(PIXEL_WIDTH, PIXEL_HEIGHT));
        setDoubleBuffered(true);
    }

    public Point screenToIso(int x, int y, int z) {
        x -= xOff - focus.x;
        y -= yOffChunk - focus.y;
        x *= 2;
        y *= 2;
        int newX = ((x) / TILE_WIDTH + (y) / TILE_HEIGHT);
        int newY = (((y) + (z * 2)) / TILE_HEIGHT - ((x) / TILE_WIDTH));
        return new Point(newX / 2, newY / 2);
    }

    public Point isoToScreen(int x, int y, int z) {

        int newX = (x - y) * TILE_WIDTH / 2 - (TILE_WIDTH / 2);
        int newY = (x + y - (z * 2)) * TILE_HEIGHT / 2;
        return new Point(newX, newY);
    }

    public DPoint isoToScreen(double x, double y, double z) {

        double newX = ((x - y) * TILE_WIDTH / 2);
        double newY = ((x + y - (z * 2)) * TILE_HEIGHT / 2);

        return new DPoint(newX, newY);
    }

    int xOff = (int) (SCREEN_WIDTH / 2);
    private boolean hideMouse;
    DPoint focus;
    long lastpaint = System.currentTimeMillis();
    int frameCount = 0;
    int fps;
    long startFpsCounter;
    boolean changed = true;

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);


        if (inGame && !world.paused) {
                g2.setColor(Color.black);
                g2.fillRect(0, 0, getToolkit().getScreenSize().width, getToolkit().getScreenSize().height);
                long start = System.currentTimeMillis();
                if(changed) {
                    latestRenderTerrain = getRelevantRenderSheetTo(world.player);
                    changed = false;
                    System.out.println("Tick " + (System.currentTimeMillis() - start));
                }
                g2.drawImage(latestRenderTerrain, 0, 0, null);

                if(System.currentTimeMillis() - startFpsCounter >= 1000) {
                    lastpaint = System.currentTimeMillis();
                    fps = frameCount;
                    frameCount = 0;
                    startFpsCounter = System.currentTimeMillis();
                } else {
                    frameCount++;
                }
//

        } else {
            g.setColor(Color.black);
            g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        }

        if (!Main.loadingBars.isEmpty()) {
            for (LoadingBar bar : Main.loadingBars.toArray(new LoadingBar[0])) {
                if (bar == null) {
                    return;
                }
                g2.setColor(bar.background);
                g2.fillRect(bar.x - bar.width / 2, bar.y - bar.height / 2, bar.width, bar.height);

                g2.setColor(bar.filled);
                g2.fillRect(bar.x - bar.width / 2, bar.y - bar.height / 2, (int) (bar.getPercentage() * bar.width), bar.height);
                g2.setColor(bar.border);
                g2.drawRect(bar.x - bar.width / 2, bar.y - bar.height / 2, bar.width, bar.height);

                g2.setColor(bar.getTitleColor());
                g2.setFont(bar.font);
                g2.drawString(bar.title, (int) (bar.x - (g2.getFontMetrics().stringWidth(bar.title) / 2.0)), bar.y + getLabelHeight(g2, bar.title, bar.font) / 2 - 2);

            }
        }

        //if (Main.ruleset.get(Main.RULE_SCREEN) == Main.RULE_SCREEN_WINDOWED) {
        SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height
                - Toolkit.getDefaultToolkit().getScreenInsets(Main.frame.getGraphicsConfiguration()).bottom;

        SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
        //}

        if (!hideMouse) {
            Polygon poly = new Polygon(new int[]{Mouse.x, Mouse.x + 4, Mouse.x - 4}, new int[]{Mouse.y - 4, Mouse.y + 4, Mouse.y + 4}, 3);

            g2.setColor(new Color(100, 100, 155, 150));
            g2.fill(poly);
            g2.setColor(new Color(240, 230, 180, 150));
            g2.draw(poly);
        }
        if (debug) {
            //g2.setColor(Color.black);

            // Font font = new Font("Serif", Font.BOLD, 14);

            //
            g2.setColor(Color.red);
            // g2.setFont(g2.getFont().deriveFont(Font.PLAIN).deriveFont(14));

        }

        g2.drawString("fps: " + fps, SCREEN_WIDTH - 42 - 5,
                14);


    }

    public BufferedImage latestRenderTerrain;
    public BufferedImage lowerWallRenderSheet;
    private boolean updateTerrain;



    public BufferedImage getRelevantRenderSheetTo(Entity e) {
        return updateCompleteRenderSheet((int) (e.getX() / World.CHUNK_WIDTH), (int) (e.getY() / World.CHUNK_HEIGHT));
    }


    public BufferedImage[][] renderedChunks;
    int yOffChunk = TILE_HEIGHT;


    public void renderChunk(int xx, int yy) {
        ArrayList<Misc> toDraw = new ArrayList<>();
        ArrayList<Point> points = new ArrayList<>();
        if (renderedChunks == null) {
            renderedChunks = new BufferedImage[world.height / World.CHUNK_HEIGHT][world.width / World.CHUNK_WIDTH];
        }
        BufferedImage sheet = new BufferedImage(World.CHUNK_WIDTH * TILE_WIDTH + xOffWalls, World.CHUNK_HEIGHT * TILE_HEIGHT + yOffChunk, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = sheet.createGraphics();
        for (int y = 0; y < World.CHUNK_HEIGHT; y++) {
            for (int x = 0; x < World.CHUNK_WIDTH; x++) {
                if (world.chunks[yy][xx].isReady()) {
                    Tile tile = world.chunks[yy][xx].tiles[y][x];
                    Point cur;
                    for (int z = 1; z <= tile.z; z++) {
                        cur = isoToScreen(x, y, z);
                        g2.drawImage(Tile.defaultSideTileLeft(), cur.x, cur.y - TILE_HEIGHT / 2 + yOffChunk, null);
                        g2.drawImage(Tile.defaultSideTileRight(), cur.x + TILE_WIDTH / 2, cur.y - TILE_HEIGHT / 2 + yOffChunk, null);
                    }
                    cur = isoToScreen(x, y, tile.z);
                    if (debug) {
                        if (tile.walls.size() > 0) {
                            tile.highlight(true);
                        }
                    }
                    g2.drawImage(tile.getTopTile(), cur.x + World.CHUNK_WIDTH * TILE_WIDTH / 2, cur.y - (TILE_HEIGHT / 2) + yOffChunk - (tile.getTopTile().getHeight() - TILE_HEIGHT), this);
                    for (Misc misc : tile.miscs) {
                        if (misc.type == Misc.TYPE_TILE) {
                            if (misc.asset.contains("water")) {
                               // toDraw.add(misc);
                                //points.add(new Point(x, y));
                            } else {
                                double xOff = -1;
                                double yOff = -.5;
                                DPoint p = isoToScreen(x + misc.x + xOff, y + misc.y + yOff, 0);

                                g2.drawImage(misc.image, (int) p.x + World.CHUNK_WIDTH * TILE_WIDTH / 2, (int) p.y - (TILE_HEIGHT / 2) + yOffChunk - (tile.getTopTile().getHeight() - TILE_HEIGHT), null);
                            }
                        }
                    }

                    if (debug) {
                        g2.drawString(tile.x + ", " + tile.y, TILE_WIDTH / 4 + cur.x + World.CHUNK_WIDTH * TILE_WIDTH / 2, cur.y + yOffChunk - (tile.getTopTile().getHeight() - TILE_HEIGHT * 3 / 2));

                    }
                }
            }
        }

        for (int i = 0; i < toDraw.size(); i++) {

            double xOff = -1;
            double yOff = -.5;
            DPoint p = isoToScreen(points.get(i).x + toDraw.get(i).x + xOff, points.get(i).y + toDraw.get(i).y + yOff, 0);
            Tile tile = world.chunks[yy][xx].tiles[points.get(i).y][points.get(i).x];

            g2.drawImage(toDraw.get(i).image, (int) p.x + World.CHUNK_WIDTH * TILE_WIDTH / 2, (int) p.y - (TILE_HEIGHT / 2) + yOffChunk - (tile.getTopTile().getHeight() - TILE_HEIGHT), null);

        }

        g2.dispose();
        renderedChunks[yy][xx] = sheet;
        world.chunks[yy][xx].rendered(true);
    }


    public BufferedImage[][] renderedWalls;
    public int yOffWalls = TILE_HEIGHT * 6;
    public int xOffWalls = TILE_WIDTH * 2;

    public void renderWalls(int xx, int yy) {
        if (renderedWalls == null) {
            renderedWalls = new BufferedImage[world.height / World.CHUNK_HEIGHT][world.width / World.CHUNK_WIDTH];
        }
        BufferedImage sheet = new BufferedImage(World.CHUNK_WIDTH * TILE_WIDTH + xOffWalls, World.CHUNK_HEIGHT * TILE_HEIGHT + yOffWalls, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = sheet.createGraphics();

        for (int y = 0; y < World.CHUNK_HEIGHT; y++) {
            for (int x = 0; x < World.CHUNK_WIDTH; x++) {
                if (world.chunks[yy][xx].isReady()) {
                    Tile tile = world.chunks[yy][xx].tiles[y][x];
                    Point cur;
                    for (Misc misc : tile.miscs) {
                        if (misc.type == Misc.TYPE_WALL || misc.asset.contains("water")) {
                            double xOff = -1;
                            double yOff = -.5;
                            DPoint p = isoToScreen(x + misc.x + xOff, y + misc.y + yOff, 0);

                            g2.drawImage(misc.image, (int) p.x + World.CHUNK_WIDTH * TILE_WIDTH / 2, (int) p.y - (TILE_HEIGHT / 2) + yOffChunk - (tile.getTopTile().getHeight() - TILE_HEIGHT) + yOffWalls, null);
                        }
                    }
                    for (Wall wall : tile.walls) {
                        cur = isoToScreen(x, y, tile.z + wall.z);


                        switch (wall.orientation) {
                            case Wall.ORIENTATION_EAST_SOUTH:
                                g2.drawImage(wall.getDefaultAsset(), cur.x + World.CHUNK_WIDTH * TILE_WIDTH / 2 + TILE_WIDTH / 2, cur.y - TILE_HEIGHT / 2 + yOffWalls
                                        , this);
                                break;
                            case Wall.ORIENTATION_SOUTH_WEST:
                                g2.drawImage(wall.getDefaultAsset(), cur.x + World.CHUNK_WIDTH * TILE_WIDTH / 2, cur.y - TILE_HEIGHT / 2 + yOffWalls, this);
                                break;
                            case Wall.ORIENTATION_CENTER:
                                g2.drawImage(wall.getAsset(1f), cur.x + World.CHUNK_WIDTH * TILE_WIDTH / 2 + (TILE_WIDTH - wall.getAsset(1f).getWidth()) / 2, cur.y
                                        - wall.getAsset(1f).getHeight() + TILE_HEIGHT / 2 + 4 + yOffWalls, this);
                                break;
                            case Wall.ORIENTATION_NORTH:
                                boolean drawn = false;
                                if (!drawn) {
                                    int xOff = 0;
                                    int yOff = 0;
                                    if (wall.asset.startsWith("tree_03")) {
                                        xOff = 6;
                                    } else if (wall.asset.startsWith("tree_04")) {
                                        xOff = 4;
                                    } else if (wall.asset.startsWith("tree_05")) {
                                        yOff = 5;
                                    } else if (wall.asset.startsWith("tree_06")) {
                                        xOff = -3;
                                        yOff = 5;
                                    } else if (wall.asset.startsWith("tree_07")) {
                                        xOff = 4;
                                    } else if (wall.asset.startsWith("tree_08")) {
                                        xOff = 3;
                                    }

                                    g2.drawImage(wall.getAsset(1f), cur.x + World.CHUNK_WIDTH * TILE_WIDTH / 2 + TILE_WIDTH / 2 - wall.getAsset(1f).getWidth() / 2 + xOff, cur.y + wall.getAsset(1f).getHeight() / 2 + TILE_HEIGHT / 2 + yOff, this);
                                }
                                break;
                            case Wall.ORIENTATION_FULL:
                                g2.drawImage(wall.getAsset(1f), cur.x + World.CHUNK_WIDTH * TILE_WIDTH / 2, cur.y + (int) (wall.getAsset(1f).getHeight() * 4.5), this);

                        }
                    }
                }
            }
        }

        g2.dispose();
        renderedWalls[yy][xx] = sheet;
    }

    BufferedImage[][] renderedEntities;

    int yOffEntitiesBottom = TILE_HEIGHT * 2;
    int i = 0;

    int xOffEntitiesLeft = TILE_WIDTH;

    public void renderEntities(int yy, int xx, DPoint oldFocus) {
        if (renderedEntities == null) {
            renderedEntities = new BufferedImage[world.height / World.CHUNK_HEIGHT][world.width / World.CHUNK_WIDTH];
        }
        BufferedImage sheet = new BufferedImage(World.CHUNK_WIDTH * TILE_WIDTH + xOffWalls + xOffEntitiesLeft, World.CHUNK_HEIGHT * TILE_HEIGHT + yOffWalls + yOffEntitiesBottom, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = sheet.createGraphics();

        for (int y = 0; y < World.CHUNK_HEIGHT; y++) {
            for (int x = 0; x < World.CHUNK_WIDTH; x++) {
                if (world.chunks[yy][xx].isReady()) {


                    for (Entity e : world.entities) {
                        if ((int) e.getY() == xx * World.CHUNK_WIDTH + x) {
                            if ((int) e.getX() == yy * World.CHUNK_HEIGHT + y) {
                                if (e instanceof Player) {
                                    DPoint cur1 = isoToScreen(x + (e.getY() % 1), y + (e.getX() % 1), e.getZ());
                                    int offsetPlayerY = TILE_HEIGHT;
                                    g2.drawImage(e.getImage(), (int) -cur1.x - (int) (focus.x - oldFocus.x) + xOffEntitiesLeft + TILE_WIDTH * World.CHUNK_WIDTH / 2 - e.getImage().getWidth() / 2, (int) cur1.y - (int) (focus.y - oldFocus.y) + yOffWalls + 13 - e.getImage().getHeight() + offsetPlayerY, this);
                                }
                            }
                        }


                    }
                    for (Misc misc : world.chunks[xx][yy].tiles[x][y].miscs) {
                        if (misc.type == Misc.TYPE_ENTITY) {
                            double xOff = -1;
                            double yOff = -.5;
                            DPoint p = isoToScreen(x + misc.y, y - 1 + misc.x, 0);
                            //g2.drawImage(misc.image, (int) -p.x + World.CHUNK_WIDTH * TILE_WIDTH / 2, (int) p.y - (TILE_HEIGHT / 2) + yOffChunk - (tile.getTopTile().getHeight() - TILE_HEIGHT), null);

                            g2.drawImage(misc.image, (int) -p.x + TILE_WIDTH * World.CHUNK_WIDTH / 2 + xOffEntitiesLeft, (int) p.y + yOffWalls + TILE_HEIGHT / 2, this);

                        }
                    }
                }
            }
        }

        g2.dispose();
        renderedEntities[yy][xx] = sheet;
    }

    //add loading of surrounding patches
    int visibility = 4;
    long last = System.currentTimeMillis();

    public BufferedImage updateCompleteRenderSheet(int xx, int yy) {

        BufferedImage tiles = new BufferedImage(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = tiles.createGraphics();


        BufferedImage wall = new BufferedImage(tiles.getWidth(), tiles.getHeight(), tiles.getType());
        Graphics2D g2w = wall.createGraphics();
        BufferedImage entities = new BufferedImage(tiles.getWidth(), tiles.getHeight(), tiles.getType());
        Graphics2D g2e = entities.createGraphics();
        DPoint focus = this.focus; //!!!!!!!
        System.out.println("Start " + System.currentTimeMillis());
        for (int cx = -visibility; cx <= visibility; cx++) {
            for (int cy = -visibility; cy <= visibility; cy++) {
                if (yy + cy >= 0 && xx + cx >= 0 && yy + cy < world.height / World.CHUNK_HEIGHT && xx + cx < world.width / World.CHUNK_WIDTH) {
                    int curyy = xx + cx;
                    int curxx = yy + cy;

                    if (!world.chunks[curxx][curyy].isRendered()) {
                        System.out.println("Start Chunk" + System.currentTimeMillis());
                        renderChunk(curyy, curxx);
                        renderWalls(curyy, curxx);
                        System.out.println("End Chunk " + System.currentTimeMillis());
                    }

                    if (world.chunks[curyy][curxx].isReady()) {
                        Point cur = isoToScreen(curyy, curxx, 0);
                        g2.drawImage(renderedChunks[curxx][curyy], (int) (((cur.x) * (World.CHUNK_WIDTH) + (Toolkit.getDefaultToolkit().getScreenSize().width / 2)) - (int) (focus.x + 0.5)),
                                (int) ((((cur.y) * World.CHUNK_HEIGHT) + Toolkit.getDefaultToolkit().getScreenSize().height / 2) - TILE_HEIGHT / 2 - (int) (focus.y + 0.5)), null);
                        g2.drawImage(renderedWalls[curxx][curyy], (int) (((cur.x) * (World.CHUNK_WIDTH) + (Toolkit.getDefaultToolkit().getScreenSize().width / 2)) - (int) (focus.x + 0.5)),
                                (int) ((((cur.y) * World.CHUNK_HEIGHT) + Toolkit.getDefaultToolkit().getScreenSize().height / 2) - TILE_HEIGHT / 2 - (int) (focus.y + 0.5) - yOffWalls), null);
                        renderEntities(curyy, curxx, focus);
                        g2.drawImage(renderedEntities[curyy][curxx], (int) (((cur.x) * (World.CHUNK_WIDTH) + (Toolkit.getDefaultToolkit().getScreenSize().width / 2)) - (int) (focus.x + 0.5)) - xOffEntitiesLeft,
                                (int) ((((cur.y) * World.CHUNK_HEIGHT) + Toolkit.getDefaultToolkit().getScreenSize().height / 2) - TILE_HEIGHT / 2 - (int) (focus.y + 0.5) - yOffWalls), null);
                    }
                }
            }
        }
        System.out.println("End " + System.currentTimeMillis());

        g2.dispose();
        latestRenderTerrain = tiles;

        return tiles;
    }

    protected float getLabelHeight(Graphics2D g2, String label, Font font) {
        FontRenderContext frc = g2.getFontRenderContext();
        return font.getLineMetrics(label, frc).getHeight();
    }

    public void exitWorld() {
        world = null;
        inGame = false;
    }

    public ArrayList<String> tilesWithWalls;

    public void wallBuilt() {
        tilesWithWalls = new ArrayList<>();
        for (int yy = 0; yy < world.height / World.CHUNK_HEIGHT; yy++) {
            for (int xx = 0; xx < world.height / World.CHUNK_WIDTH; xx++) {
                for (int y = 0; y < World.CHUNK_HEIGHT; y++) {
                    for (int x = 0; x < World.CHUNK_WIDTH; x++) {
                        if (world.chunks[yy][xx].isReady()) {
                            if (world.chunks[yy][xx].tiles[y][x].walls.size() > 0) {
                                tilesWithWalls.add((xx * World.CHUNK_WIDTH + x) + "," + (yy * World.CHUNK_HEIGHT + y));
                            }
                        }
                    }
                }
            }
        }
    }

    public void loadWorld(World world) {
        this.world = world;
        world.canvas = this;
        focus = isoToScreen(0.0, 0.0, 0.0);

        inGame = true;
        wallBuilt();
        initialize();
        //getRelevantRenderSheetTo(world.player);

    }

    private void initialize() {
        /*
         * try { mainFont = Font.createFont(Font.TRUETYPE_FONT, new
		 * File(Main.ASSETS + "/PoisonHope-Regular.ttf")); GraphicsEnvironment
		 * ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		 * ge.registerFont(mainFont); } catch (IOException e) {
		 * 
		 * } catch (FontFormatException e) { e.printStackTrace(); }
		 */
    }

    boolean once = true;

    public void update() {
        if (world == null) {
            return;
        }
        if (once) {

        }
        if (updateTerrain) {
            getRelevantRenderSheetTo(world.player);

        }
        focus = isoToScreen(world.camera.getX(), world.camera.getY(), world.camera.getZ());
        if (world.entities != null || !world.entities.isEmpty()) {
            for (Entity e : world.entities) {
                e.update();
            }

        }

        // Point p = screenToIso(Mouse.x, Mouse.y, 0);
        // if(p.x <= 9 && p.y <= 9 && p.x >= 0 && p.y >= 0) {
        // world.tiles[p.y][p.x].highlight();
        // }
    }

}

class DPoint {
    double x, y;

    public DPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }
}