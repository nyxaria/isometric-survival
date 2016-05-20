import javax.swing.*;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

class Canvas extends JComponent {
    public static int SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
    public static int SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;

    public static final int TILE_WIDTH = 64;
    public static final int TILE_HEIGHT = (int) (TILE_WIDTH / (2.0));
    public static int PIXEL_WIDTH = (WIDTH * TILE_HEIGHT / 2) + (HEIGHT * TILE_HEIGHT / 2);
    public static int PIXEL_HEIGHT = (HEIGHT * TILE_HEIGHT / 2) - (WIDTH * TILE_HEIGHT / 2);
    Font mainFont;

    World world;
    private boolean inGame;
    static TileGraphicsHandler tiler = new TileGraphicsHandler();
    private BufferedImage previousCompleteRenderSheet;

    public ArrayList<Object[]> imagesFading = new ArrayList<>();

    public Canvas() {
        // setPreferredSize(new Dimension(PIXEL_WIDTH, PIXEL_HEIGHT));

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

    // finsish walls camera

    int xOff = (int) (SCREEN_WIDTH / 2);
    private boolean hideMouse;
    DPoint focus;

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        if (inGame && !world.paused) {
            g2.setColor(Color.black);
            g2.fillRect(0, 0, getToolkit().getScreenSize().width, getToolkit().getScreenSize().height);
            g2.drawImage(getRelevantRenderSheetTo(world.player), 0, 0, null);
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
                g2.drawString(bar.title, bar.x - (g2.getFontMetrics().stringWidth(bar.title) / 2), bar.y + getLabelHeight(g2, bar.title, bar.font) / 2 - 2);

            }
        }

        //if (Main.ruleset.get(Main.RULE_SCREEN) == Main.RULE_SCREEN_WINDOWED) {
        SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height
                - Toolkit.getDefaultToolkit().getScreenInsets(Main.frame.getGraphicsConfiguration()).bottom;
        //}

        if (!hideMouse) {
            Polygon poly = new Polygon(new int[]{Mouse.x, Mouse.x + 4, Mouse.x - 4}, new int[]{Mouse.y - 4, Mouse.y + 4, Mouse.y + 4}, 3);

            g2.setColor(new Color(100, 100, 155, 150));
            g2.fill(poly);
            g2.setColor(new Color(240, 230, 180, 150));
            g2.draw(poly);
        }
        boolean debug = true;
        if (debug) {
            //g2.setColor(Color.black);

            // Font font = new Font("Serif", Font.BOLD, 14);

            //
            g2.setColor(Color.red);
            // g2.setFont(g2.getFont().deriveFont(Font.PLAIN).deriveFont(14));
            g2.drawString("fps: " + Main.fps, SCREEN_WIDTH - g2.getFontMetrics().stringWidth("fps: " + Main.fps) - 5,
                    getLabelHeight(g2, "fps: " + Main.fps, g2.getFont()));
        }

    }

    public BufferedImage latestRenderTerrain;
    public BufferedImage lowerWallRenderSheet;
    private boolean updateTerrain;

    public BufferedImage getHalfTransparentImage(BufferedImage img) {
        // img.createGraphics().setComposite(AlphaComposite.Src, 0.5f);

        return img;
    }


    public BufferedImage getRelevantRenderSheetTo(Entity e) {
        return updateCompleteRenderSheet((int) (e.getX()/World.CHUNK_WIDTH), (int) (e.getY()/World.CHUNK_HEIGHT));
    }


    public BufferedImage[][] renderedChunks;
    int yOffChunk = TILE_HEIGHT / 4;

    public void renderChunk(int xx, int yy) {
        if (renderedChunks == null) {
            renderedChunks = new BufferedImage[world.height / World.CHUNK_HEIGHT][world.width / World.CHUNK_WIDTH];
        }
        BufferedImage sheet = new BufferedImage(World.CHUNK_WIDTH * TILE_WIDTH  + xOffWalls, World.CHUNK_HEIGHT * TILE_HEIGHT + yOffChunk, BufferedImage.TYPE_INT_ARGB);
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
                    g2.drawImage(tile.getTopTile(), cur.x + World.CHUNK_WIDTH * TILE_WIDTH / 2, cur.y - (TILE_HEIGHT / 2) + yOffChunk, this);

                }
            }
        }

        renderedChunks[yy][xx] = sheet;
        world.chunks[yy][xx].rendered(true);
    }


    public BufferedImage[][] renderedWalls;
    public int yOffWalls = TILE_HEIGHT * 3;
    public int xOffWalls = TILE_WIDTH;

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
                                    g2.drawImage(wall.getAsset(1f), cur.x + World.CHUNK_WIDTH * TILE_WIDTH / 2, cur.y + yOffChunk- TILE_HEIGHT/2, this);
                                }
                                break;
                        }
                    }
                }
            }
        }

        renderedWalls[yy][xx] = sheet;
    }

    BufferedImage[][] renderedEntities;

    public void renderEntities(int xx, int yy) {
        if (renderedEntities == null) {
            renderedEntities = new BufferedImage[world.height / World.CHUNK_HEIGHT][world.width / World.CHUNK_WIDTH];
        }
        BufferedImage sheet = new BufferedImage(World.CHUNK_WIDTH * TILE_WIDTH, World.CHUNK_HEIGHT * TILE_HEIGHT + yOffWalls, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = sheet.createGraphics();

        for (int y = 0; y < World.CHUNK_HEIGHT; y++) {
            for (int x = 0; x < World.CHUNK_WIDTH; x++) {
                if (world.chunks[yy][xx].isReady()) {
                    for (Entity e : world.entities) {
                        if ((int) e.getY() == xx * World.CHUNK_WIDTH + x) {
                            if ((int) e.getX() == yy * World.CHUNK_HEIGHT + y) {
                                if (e instanceof Player) {
                                    DPoint cur1 = isoToScreen(x + (e.getY() % 1), y + (e.getX() % 1), e.getZ());
                                    g2.drawImage(e.getImage(), (int) -cur1.x + (int) (TILE_WIDTH * 1.5) + (TILE_WIDTH - e.getImage().getWidth()) / 2,
                                            (int) cur1.y - e.getImage().getHeight() + (int) (1.5 * TILE_HEIGHT) + yOffWalls, this);
                                }
                            }
                        }
                    }
                }
            }
        }
        renderedEntities[yy][xx] = sheet;
    }


    public BufferedImage updateCompleteRenderSheet(int xx, int yy) {

        BufferedImage tiles = new BufferedImage(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = tiles.createGraphics();



        BufferedImage wall = new BufferedImage(tiles.getWidth(), tiles.getHeight(), tiles.getType());
        Graphics2D g2w = wall.createGraphics();
        BufferedImage entities = new BufferedImage(tiles.getWidth(), tiles.getHeight(), tiles.getType());
        Graphics2D g2e = entities.createGraphics();

        //int yy = (int) world.player.getY() / World.CHUNK_HEIGHT;
        //int xx = (int) world.player.getX() / World.CHUNK_WIDTH;

        for (int cx = -3; cx <= 3; cx++) {
            for (int cy = -3; cy <= 3; cy++) {
                if (yy + cy >= 0 && xx + cx >= 0 && yy + cy < world.height / World.CHUNK_HEIGHT && xx + cx < world.width / World.CHUNK_WIDTH) {
                    int curyy = yy + cy;
                    int curxx = xx + cx;

                    if (!world.chunks[curxx][curyy].isRendered()) {
                        renderChunk(curyy, curxx);
                        renderWalls(curyy, curxx);
                    }

                    if (world.chunks[curyy][curxx].isReady()) {
                        Point cur = isoToScreen(curxx, curyy, 0);
                        g2.drawImage(renderedChunks[curxx][curyy], (int) (((cur.x) * (World.CHUNK_WIDTH) + (Toolkit.getDefaultToolkit().getScreenSize().width / 2) + (TILE_WIDTH / 2)) - focus.x), (int) ((((cur.y) * World.CHUNK_HEIGHT) + Toolkit.getDefaultToolkit().getScreenSize().height / 2 - (int) (1.85 * TILE_HEIGHT)) - focus.y), null);
                        renderEntities(curyy, curxx);
                        g2w.drawImage(renderedWalls[curxx][curyy], (int) (((cur.x) * (World.CHUNK_WIDTH) + (Toolkit.getDefaultToolkit().getScreenSize().width / 2) + (TILE_WIDTH / 2)) - focus.x), (int) ((((cur.y) * World.CHUNK_HEIGHT) + Toolkit.getDefaultToolkit().getScreenSize().height / 2 - (int) (1.85 * TILE_HEIGHT)) - focus.y - yOffWalls), null);
                        g2e.drawImage(renderedEntities[curxx][curyy], (int) (((cur.x) * (World.CHUNK_WIDTH) + (Toolkit.getDefaultToolkit().getScreenSize().width / 2) + (TILE_WIDTH / 2)) - focus.x), (int) ((((cur.y) * World.CHUNK_HEIGHT) + Toolkit.getDefaultToolkit().getScreenSize().height / 2 - (int) (1.85 * TILE_HEIGHT)) - focus.y - yOffWalls), null);

                    }
                }
            }
        }


        BufferedImage temp = new BufferedImage(tiles.getWidth(), tiles.getHeight(), tiles.getType()); //double buffer
        Graphics2D tempg = temp.createGraphics();
        tempg.drawImage(wall,0,0,null);
        tempg.setComposite(AlphaComposite.Xor);
        tempg.drawImage(entities,0,0,null);
        g2.drawImage(temp, 0, 0, null);
        tempg.dispose();
        g2w.dispose();
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

    public void wallBuilt() {
        ArrayList<Tile> tilesWithWalls = new ArrayList<Tile>();
        for (int yy = 0; yy < world.height / World.CHUNK_HEIGHT; yy++) {
            for (int xx = 0; xx < world.height / World.CHUNK_WIDTH; xx++) {
                for (int y = 0; y < World.CHUNK_HEIGHT; y++) {
                    for (int x = 0; x < World.CHUNK_WIDTH; x++) {
                        if (world.chunks[yy][xx].isReady()) {
                            if (world.chunks[yy][xx].tiles[y][x].walls.size() > 0) {
                                tilesWithWalls.add(world.chunks[yy][xx].tiles[y][x]);
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
        wallBuilt();

        inGame = true;
        focus = isoToScreen(0.0, 0.0, 0.0);

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
        // System.out.println(world.entities.size());
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