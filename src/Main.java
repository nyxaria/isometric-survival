import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Main {
    public static int WIDTH;
// ||
    public static int HEIGHT;

    public static final int RULE_SCREEN = 1;
    public static final int RULE_SCREEN_FULLSCREEN = 2;
    public static final int RULE_SCREEN_BORDERLESS = 3;
    public static final int RULE_SCREEN_WINDOWED = 4;

    private static final int ERROR_SETTINGS = 100;

    public static int fps = 0;

    static ArrayList<String> rulelist = new ArrayList<String>();

    private static final String ROOT_FOLDER = "isometry";
    public static final String ROOT = "isometry";
    public static final String RULES = ROOT + "/rules";
    public static final String ASSETS = ROOT + "/assets";
    public static final String BIN = ROOT + "/bin";
    public static final String SAVES = ROOT + "/saves";

    public static HashMap<Integer, Integer> ruleset = new HashMap<Integer, Integer>();

    static JFrame frame;
    public static Canvas canvas;

    static Main main;

    static GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];

    public static ArrayList<Notification> notifications = new ArrayList<Notification>();

    public static ArrayList<LoadingBar> loadingBars = new ArrayList<LoadingBar>();

    public static HashMap<Integer, Boolean> keysDown = new HashMap<Integer, Boolean>();


    public Color meadow = new Color(255, 255, 255), water = new Color(255, 255, 254), forest = new Color(255, 255, 253), sand = new Color(255, 255, 252), tallgrass = new Color(255, 255, 249), river = new Color(255,255,246);
    public Color transition_meadow_forest = new Color(255, 255, 251), transition_sand_water = new Color(255, 255, 250), transition_sand_forest = new Color(255, 255, 249), transition_sand_meadow = new Color(255, 255, 248), transition_meadow_tallgrass = new Color(255, 255, 247);

    public Main() {

    }

    public void start() {

//		try {
//			updateRules();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
        frame = createFrame();

        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

        // Create a new blank cursor.
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                cursorImg, new Point(0, 0), "blank cursor");

        // Set the blank cursor to the JFrame.
        frame.getContentPane().setCursor(blankCursor);

        frame.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {
                keysDown.put(e.getKeyCode(), true);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                keysDown.put(e.getKeyCode(), false);
                if (canvas.world != null) {
                    for (Entity entity : canvas.world.entities) {
                        entity.keyReleased(e.getKeyCode());
                    }
                }
            }

            @Override
            public void keyTyped(KeyEvent arg0) {
            }

        });

        frame.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                Mouse.click();
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {

            }

            @Override
            public void mouseExited(MouseEvent arg0) {

            }

            @Override
            public void mousePressed(MouseEvent arg0) {

            }

            @Override
            public void mouseReleased(MouseEvent arg0) {

            }

        });

        frame.addMouseMotionListener(new MouseMotionListener() {


            @Override
            public void mouseDragged(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Mouse.set(e.getXOnScreen(), e.getYOnScreen());
            }

        });

        canvas = new Canvas();
        frame.add(canvas);

        Thread loop = new Thread(new Runnable() {
            public void run() {
                // Main.loadingBars.get(0).percentage++;
                int TICKS_PER_SECOND = 80;
                int SKIP_TICKS = 1000 / TICKS_PER_SECOND;
                int MAX_FRAMESKIP = 10;
                long start = System.currentTimeMillis();
                long next_game_tick = System.currentTimeMillis();
                int count = 0;
                int loops;

                boolean game_is_running = true;
                while (game_is_running) {

                    loops = 0;
                    while (System.currentTimeMillis() > next_game_tick && loops < MAX_FRAMESKIP) {
                        canvas.update();
                        if (System.currentTimeMillis() - start < 1000) {
                            count++;
                        } else {
                            fps = count;
                            count = 0;
                            start = System.currentTimeMillis();
                        }
                        next_game_tick += SKIP_TICKS;
                        loops++;
                    }

                    canvas.repaint();

                }
            }
        });
        // new Thread(task).run();
        //ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        //executor.scheduleAtFixedRate(task, 0, 100, TimeUnit.MILLISECONDS);


        frame.setVisible(true);
        Robot robot = null;
        try {
            robot = new Robot();
            robot.mouseMove(Toolkit.getDefaultToolkit().getScreenSize().width / 2, Toolkit.getDefaultToolkit().getScreenSize().height / 2);
        } catch (AWTException e) {
            e.printStackTrace();
        }


        int width = 60 * World.CHUNK_WIDTH;
        int height = 60 * World.CHUNK_HEIGHT;
        Random r = new Random();
        Chunk[][] chunks = new Chunk[height / World.CHUNK_HEIGHT][width / World.CHUNK_WIDTH];

        BufferedImage biomeMap = createBiomeMap(width, height);
        JDialog dialog = new JDialog();
        JLabel label = new JLabel(new ImageIcon(biomeMap));
        dialog.add(label);
        dialog.pack();
        dialog.setVisible(true);
        //Raster ras = biomeMap.getData();

        // DataBuffer data = ras.getDataBuffer();

        for (int yy = 0; yy < height / World.CHUNK_HEIGHT; yy++) {
            for (int xx = 0; xx < width / World.CHUNK_WIDTH; xx++) {

                chunks[yy][xx] = new Chunk(xx, yy);
                chunks[yy][xx].tiles = new Tile[World.CHUNK_HEIGHT][World.CHUNK_WIDTH];
                chunks[yy][xx].ready = true;
                for (int y = 0; y < World.CHUNK_HEIGHT; y++) {
                    for (int x = 0; x < World.CHUNK_WIDTH; x++) {
                        int pixel = biomeMap.getRGB(xx * World.CHUNK_WIDTH + x, yy * World.CHUNK_HEIGHT + y);
                        Color color = new Color(pixel);
                        if (color.equals(forest)) { //forest
                            int random = new Random().nextInt(4) + 1;
                            Tile tile = new Tile(x + (xx * World.CHUNK_WIDTH), y + (yy * World.CHUNK_HEIGHT), 0, new String[]{"grass_05_top.png", ""});
                            chunks[yy][xx].tiles[y][x] = tile;

                            for (int i = 0; i < 4; i++)
                                tile.addMisc(new Misc("misc_forestgrass_0" + (new Random().nextInt(2) + 8) + ".png", Math.random() * 1.1, Math.random() * 1.1, Misc.TYPE_TILE));
                            tile.addMisc(new Misc("misc_grass_0" + (new Random().nextInt(4) + 1) + "_0" + (new Random().nextInt(2) + 3) + "_top.png", Math.random() - 0.2, Math.random() - 0.2, Misc.TYPE_TILE));
                            tile.addMisc(new Misc("misc_grass_0" + (new Random().nextInt(4) + 1) + "_0" + (new Random().nextInt(2) + 3) + "_top.png", Math.random() + 0.2, Math.random() + 0.2, Misc.TYPE_TILE));
                            tile.addMisc(new Misc("misc_grass_0" + (new Random().nextInt(4) + 1) + "_0" + (new Random().nextInt(2) + 3) + "_top.png", Math.random(), Math.random(), Misc.TYPE_TILE));
                            tile.addMisc(new Misc("misc_grass_0" + (new Random().nextInt(4) + 1) + "_0" + (new Random().nextInt(2) + 3) + "_top.png", Math.random(), Math.random(), Misc.TYPE_TILE));



                            if (new Random().nextInt(20) == 0) {
                                int ran = new Random().nextInt(3);
                                switch (ran) {
                                    case 0:
                                        tile.addMisc(new Misc("misc_mushroom_0" + (new Random().nextInt(4) + 1) + ".png", Math.random(), Math.random(), Misc.TYPE_TILE));
                                        break;
                                    case 1:
                                    case 2:
                                        //tile.addMisc(new Misc("misc_sapling_0" + (new Random().nextInt(2) + 1) + ".png", Math.random(), Math.random()));
                                }
                            }

                            if (new Random().nextInt(20) == 4) {
                                chunks[yy][xx].tiles[y][x].walls.add(new Wall(0, Wall.ORIENTATION_NORTH, "tree_0" + (new Random().nextInt(8) + 1) + "_" + new Random().nextInt(8) + ".png"));
                            } else if(new Random().nextInt(500) == 0){
                                chunks[yy][xx].tiles[y][x].walls.add(new Wall(0, Wall.ORIENTATION_FULL, "rock_" + (new Random().nextInt(1) + 1) + ".png"));
                            } else if (new Random().nextInt(1000) == 0) {
                                //chunks[yy][xx].tiles[y][x].walls.add(new Wall(0, Wall.ORIENTATION_FULL, "object_forestrock_" + (new Random().nextInt(1) + 1) + ".png"));

                            }


                        } else if (color.equals(water) || color.equals(river)) {
                            //complete water same as with edges
                            Tile tile = new Tile(x + (xx * World.CHUNK_WIDTH), y + (yy * World.CHUNK_HEIGHT), 0, new String[]{"water_0" + (4 + new Random().nextInt(2)) + ".png", ""});

                            if(color.equals(river)) {
                                tile = new Tile(x + (xx * World.CHUNK_WIDTH), y + (yy * World.CHUNK_HEIGHT), 0, new String[]{"", ""});
                                tile.walls.add(new Wall(0, Wall.ORIENTATION_FULL, ""));
                            }

                            Color top;
                            Color bottom;
                            Color right;
                            Color left;

                            if (xx * World.CHUNK_WIDTH + x == 0) {
                                left = color;
                            } else {
                                left = new Color(biomeMap.getRGB(xx * World.CHUNK_WIDTH + x - 1, yy * World.CHUNK_HEIGHT + y));
                            }
                            if (xx * World.CHUNK_WIDTH + x >= biomeMap.getWidth() - 1) {
                                right = color;
                            } else {
                                right = new Color(biomeMap.getRGB(xx * World.CHUNK_WIDTH + x + 1, yy * World.CHUNK_HEIGHT + y));
                            }
                            if (yy * World.CHUNK_HEIGHT + y == 0) {
                                top = color;
                            } else {
                                top = new Color(biomeMap.getRGB(xx * World.CHUNK_WIDTH + x, yy * World.CHUNK_HEIGHT + y - 1));
                            }
                            if (yy * World.CHUNK_HEIGHT + y >= biomeMap.getHeight() - 1) {
                                bottom = color;
                            } else {
                                bottom = new Color(biomeMap.getRGB(xx * World.CHUNK_WIDTH + x, yy * World.CHUNK_HEIGHT + y + 1));
                            }
                            //edges of grass
                            double xOffRight = 0;
                            double xOffLeft = 0;
                            double yOffTop = 0;
                            double yOffBottom = 0;
                            if(color.equals(river)) {
                                xOffRight = 0.1;
                                xOffLeft = 0;
                                yOffTop = +.05;
                                yOffBottom = -.2;
                            }
                            if (top.equals(water) || top.equals(river)) {
                                if (bottom.equals(water) || bottom.equals(river)) {
                                    if (right.equals(water) || right.equals(river)) {
                                        if (left.equals(water) || left.equals(river)) {
                                            //tile = new Tile(x + (xx*World.CHUNK_WIDTH), y + (yy*World.CHUNK_HEIGHT), 0, new String[]{"tallgrass_00.png", ""});
                                            if(new Random().nextInt(400) == 0)
                                            tile.addMisc(new Misc("water_20.png", 0.5, 1, Misc.TYPE_TILE)); // top bottom right left
                                        } else {
                                            tile.addMisc(new Misc("water_07.png", 0.5 + xOffRight, 1, Misc.TYPE_TILE)); // top bottom right
                                        }
                                    } else {
                                        if (left.equals(water) || left.equals(river)) {
                                            tile.addMisc(new Misc("water_02.png", 0.5 + xOffLeft, 1, Misc.TYPE_TILE)); //top bottom left
                                        } else {
                                            tile.addMisc(new Misc("water_12.png", 0.5, 0.8, Misc.TYPE_TILE)); //right left

//                                            tile.addMisc(new Misc("water_02.png", 0.8, 1.25, Misc.TYPE_TILE)); //top bottom
//                                            tile.addMisc(new Misc("water_02.png", 0.8, .75, Misc.TYPE_TILE));
//
//                                            tile.addMisc(new Misc("water_07.png", 0.2, 1.25, Misc.TYPE_TILE));
//                                            tile.addMisc(new Misc("water_07.png", 0.2, .75, Misc.TYPE_TILE));

                                        }
                                    }
                                } else {
                                    if (right.equals(water) || right.equals(river)) {
                                        if (left.equals(water) || left.equals(river)) {
                                            tile.addMisc(new Misc("water_01.png", 0.5, 1 + yOffTop  , Misc.TYPE_TILE)); //top right left
                                        } else {
                                            tile.addMisc(new Misc("water_03.png", 0.5 + xOffRight, 1 + yOffTop, Misc.TYPE_TILE)); //top right
                                        }
                                    } else {
                                        if (left.equals(water) || left.equals(river)) {
                                            tile.addMisc(new Misc("water_00.png", 0.5 + xOffLeft, 1 + yOffTop, Misc.TYPE_TILE)); //top left
                                        } else {
                                            tile.addMisc(new Misc("water_21.png", 0.5, 1, Misc.TYPE_TILE)); //top
                                        }
                                    }
                                }
                            } else {
                                if (bottom.equals(water) || bottom.equals(river)) {
                                    if (right.equals(water) || right.equals(river)) {
                                        if (left.equals(water) || left.equals(river)) {
                                            tile.addMisc(new Misc("water_08.png", 0.5, 1 + yOffBottom, Misc.TYPE_TILE)); //bottom right left
                                        } else {
                                            tile.addMisc(new Misc("water_15.png", 0.5 + xOffRight - 0.1, 1 + yOffBottom - 0.1, Misc.TYPE_TILE)); //bottom right
                                        }
                                    } else {
                                        if (left.equals(water) || left.equals(river)) {
                                            tile.addMisc(new Misc("water_06.png", 0.5 + xOffLeft - 0.1, 1 + yOffBottom + 0.1, Misc.TYPE_TILE)); //bottom left
                                        } else {
                                            tile.addMisc(new Misc("water_03.png", 0.5, 1, Misc.TYPE_TILE)); //bottom
                                        }
                                    }
                                } else {
                                    if (right.equals(water) || right.equals(river)) {
                                        if (left.equals(water) || left.equals(river)) {
                                            tile.addMisc(new Misc("water_11.png", 0.55, 0.95, Misc.TYPE_TILE)); //right left
//                                            tile.addMisc(new Misc("water_01.png", 0.55, 1.3, Misc.TYPE_TILE));
//                                            tile.addMisc(new Misc("water_01.png", 0.15, 1.3, Misc.TYPE_TILE));
//
//                                            tile.addMisc(new Misc("water_08.png", 0.55, .8, Misc.TYPE_TILE));
//                                            tile.addMisc(new Misc("water_08.png", 0.15, .8, Misc.TYPE_TILE));


                                        } else {
                                            tile.addMisc(new Misc("water_21.png", 0.5 + xOffRight, 1, Misc.TYPE_TILE)); //right
                                        }
                                    } else {
                                        if (left.equals(water) || left.equals(river)) {
                                            tile.addMisc(new Misc("water_21.png", 0.5 + xOffLeft, 1, Misc.TYPE_TILE)); //left
                                        } else {
                                            tile.addMisc(new Misc("water_21.png", 0.5, 1, Misc.TYPE_TILE)); //none

                                        }
                                    }
                                }
                            }
//                            if(!tile.miscs.get(0).asset.endsWith("04.png") && !tile.miscs.get(0).asset.endsWith("03.png")) {
//                                Misc temp = tile.miscs.get(0);
//                                tile.miscs.remove(0);
//                                tile.miscs.trimToSize();
//                                for (int i = 0; i < 2; i++)
//                                    tile.addMisc(new Misc("misc_desertgrass_0" + (new Random().nextInt(4) + 1) + ".png", Math.random(), Math.random(), Misc.TYPE_TILE));
//                                for (int i = 0; i < 3; i++)
//                                    tile.addMisc(new Misc("misc_meadowgrass_0" + (new Random().nextInt(4) + 1) + ".png", Math.random(), Math.random(), Misc.TYPE_TILE));
//                                tile.addMisc(temp);
//                            }
                            chunks[yy][xx].tiles[y][x] = tile;
                        } else if (color.equals(meadow)) {
                            Tile tile = new Tile(x + (xx * World.CHUNK_WIDTH), y + (yy * World.CHUNK_HEIGHT), 0, new String[]{"grass_01_top.png", ""});
                            chunks[yy][xx].tiles[y][x] = tile;
                            for (int i = 0; i < 4; i++)
                                tile.addMisc(new Misc("misc_forestgrass_0" + (new Random().nextInt(5) + 3) + ".png", Math.random(), Math.random(), Misc.TYPE_TILE));
                            if (new Random().nextInt(22) == 0) {


                                //add so that once teh chance happens it spawns a certain color group, not group of random color
                                tile.addMisc(new Misc("misc_flower_09.png", Math.random(), Math.random(), Misc.TYPE_TILE));
                                tile.addMisc(new Misc("misc_flower_09.png", Math.random(), Math.random(), Misc.TYPE_TILE));
                                tile.addMisc(new Misc("misc_flower_09.png", Math.random(), Math.random(), Misc.TYPE_TILE));
                                tile.addMisc(new Misc("misc_flower_09.png", Math.random(), Math.random(), Misc.TYPE_TILE));
                                tile.addMisc(new Misc("misc_flower_09.png", Math.random(), Math.random(), Misc.TYPE_TILE));
                                tile.addMisc(new Misc("misc_flower_09.png", Math.random(), Math.random(), Misc.TYPE_TILE));

                            } else if (new Random().nextInt(10) == 0) {
                                tile.addMisc(new Misc("misc_flower_09.png", Math.random(), Math.random(), Misc.TYPE_TILE));
                                tile.addMisc(new Misc("misc_flower_09.png", Math.random(), Math.random(), Misc.TYPE_TILE));

                            } else if (new Random().nextInt(8) == 0) {
                                tile.addMisc(new Misc("misc_flower_09.png", Math.random(), Math.random(), Misc.TYPE_TILE));
                            }

                            //tile.addMisc(new Misc("misc_flower_" + (ran > 9 ? "" : "0") + (ran) + ".png", Math.random(), Math.random()));
                        } else if (color.equals(tallgrass)) {
                            Tile tile = new Tile(x + (xx * World.CHUNK_WIDTH), y + (yy * World.CHUNK_HEIGHT), 0, new String[]{"grass_02_top.png", ""});

                            Color top;
                            Color bottom;
                            Color right;
                            Color left;

                            if (xx * World.CHUNK_WIDTH + x == 0) {
                                left = color;
                            } else {
                                left = new Color(biomeMap.getRGB(xx * World.CHUNK_WIDTH + x - 1, yy * World.CHUNK_HEIGHT + y));
                            }
                            if (xx * World.CHUNK_WIDTH + x >= biomeMap.getWidth() - 1) {
                                right = color;
                            } else {
                                right = new Color(biomeMap.getRGB(xx * World.CHUNK_WIDTH + x + 1, yy * World.CHUNK_HEIGHT + y));
                            }
                            if (yy * World.CHUNK_HEIGHT + y == 0) {
                                top = color;
                            } else {
                                top = new Color(biomeMap.getRGB(xx * World.CHUNK_WIDTH + x, yy * World.CHUNK_HEIGHT + y - 1));
                            }
                            if (yy * World.CHUNK_HEIGHT + y >= biomeMap.getHeight() - 1) {
                                bottom = color;
                            } else {
                                bottom = new Color(biomeMap.getRGB(xx * World.CHUNK_WIDTH + x, yy * World.CHUNK_HEIGHT + y + 1));
                            }
                            //edges of grass
                            if (top.equals(tallgrass)) {
                                if (bottom.equals(tallgrass)) {
                                    if (right.equals(tallgrass)) {
                                        if (left.equals(tallgrass)) {
                                            //tile = new Tile(x + (xx*World.CHUNK_WIDTH), y + (yy*World.CHUNK_HEIGHT), 0, new String[]{"tallgrass_00.png", ""});
                                            tile.addMisc(new Misc("tallgrass_00.png", 0, 0, Misc.TYPE_ENTITY));
                                        } else {
                                            tile.addMisc(new Misc("tallgrass_06.png", 0, .1, Misc.TYPE_ENTITY));
                                        }
                                    } else {
                                        if (left.equals(tallgrass)) {
                                            tile.addMisc(new Misc("tallgrass_06.png", -.2, 0, Misc.TYPE_ENTITY));
                                        } else {
                                            tile.addMisc(new Misc("tallgrass_02.png", 0, -.1, Misc.TYPE_ENTITY));
                                        }
                                    }
                                } else {
                                    if (right.equals(tallgrass)) {
                                        if (left.equals(tallgrass)) {
                                            tile.addMisc(new Misc("tallgrass_03.png", 0, -.1, Misc.TYPE_ENTITY));
                                        } else {
                                            tile.addMisc(new Misc("tallgrass_01.png", 0, -.45, Misc.TYPE_ENTITY));
                                        }
                                    } else {
                                        if (left.equals(tallgrass)) {
                                            tile.addMisc(new Misc("tallgrass_04.png", -.20, -.37, Misc.TYPE_ENTITY));
                                        } else {
                                            tile.addMisc(new Misc("tallgrass_08.png", -.2, -.37, Misc.TYPE_ENTITY));
                                        }
                                    }
                                }
                            } else {
                                if (bottom.equals(tallgrass)) {
                                    if (right.equals(tallgrass)) {
                                        if (left.equals(tallgrass)) {
                                            tile.addMisc(new Misc("tallgrass_07.png", 0, 0, Misc.TYPE_ENTITY));
                                        } else {
                                            tile.addMisc(new Misc("tallgrass_08.png", .22, 0, Misc.TYPE_ENTITY));
                                        }
                                    } else {
                                        if (left.equals(tallgrass)) {
                                            tile.addMisc(new Misc("tallgrass_05.png", -.1, 0, Misc.TYPE_ENTITY));
                                        } else {
                                            tile.addMisc(new Misc("tallgrass_03.png", .2, -.3, Misc.TYPE_ENTITY));
                                        }
                                    }
                                } else {
                                    if (right.equals(tallgrass)) {
                                        if (left.equals(tallgrass)) {
                                            tile.addMisc(new Misc("tallgrass_03.png", 0, -.1, Misc.TYPE_ENTITY));
                                        } else {
                                            tile.addMisc(new Misc("tallgrass_03.png", .5, -.1, Misc.TYPE_ENTITY));
                                        }
                                    } else {
                                        if (left.equals(tallgrass)) {
                                            tile.addMisc(new Misc("tallgrass_06.png", -.5, 0, Misc.TYPE_ENTITY));
                                        } else {
                                            tile.addMisc(new Misc("tallgrass_04.png", 0, 0, Misc.TYPE_ENTITY));

                                        }
                                    }
                                }
                            }
                            if(!tile.miscs.get(0).asset.endsWith("00.png")) {
                                Misc temp = tile.miscs.get(0);
                                tile.miscs.remove(0);
                                tile.miscs.trimToSize();
                                for (int i = 0; i < 2; i++)
                                    tile.addMisc(new Misc("misc_desertgrass_0" + (new Random().nextInt(4) + 1) + ".png", Math.random(), Math.random(), Misc.TYPE_TILE));
                                for (int i = 0; i < 3; i++)
                                    tile.addMisc(new Misc("misc_meadowgrass_0" + (new Random().nextInt(4) + 1) + ".png", Math.random(), Math.random(), Misc.TYPE_TILE));
                                tile.addMisc(temp);
                            }
                            chunks[yy][xx].tiles[y][x] = tile;

                        } else if (color.equals(sand)) {

                            Tile tile = new Tile(x + (xx * World.CHUNK_WIDTH), y + (yy * World.CHUNK_HEIGHT), 0, new String[]{"grass_01_top.png", ""});
                            chunks[yy][xx].tiles[y][x] = tile;
                            if (new Random().nextInt(5) == 0) {
                                int ran = new Random().nextInt(3);
                                switch (ran) {
                                    case 0:
                                        if (new Random().nextInt(5) == 0)
                                            tile.addMisc(new Misc("misc_shell_0" + (new Random().nextInt(3) + 1) + ".png", Math.random(), Math.random(), Misc.TYPE_TILE));
                                        break;
                                    case 1:
                                        if (new Random().nextInt(2) == 0)
                                            tile.addMisc(new Misc("misc_rock_0" + (new Random().nextInt(5) + 1) + ".png", Math.random(), Math.random(), Misc.TYPE_TILE));
                                        break;
                                    case 2:
                                        ran = new Random().nextInt(2);
                                        switch (ran) {
                                            case 0:
                                                tile.addMisc(new Misc("misc_tallgrass_01.png", Math.random(), Math.random(), Misc.TYPE_TILE));
                                                break;
                                            case 1:
                                                tile.addMisc(new Misc("misc_desertgrass_0" + (new Random().nextInt(7) + 1) + ".png", Math.random(), Math.random(), Misc.TYPE_TILE));
                                                break;
                                        }
                                }
                            }
                        } else if (color.equals(transition_meadow_forest)) {
                            Tile tile = new Tile(x + (xx * World.CHUNK_WIDTH), y + (yy * World.CHUNK_HEIGHT), 0, new String[]{"grass_04_top.png", ""});
                            chunks[yy][xx].tiles[y][x] = tile;

                            for (int i = 0; i < 7; i++)
                                tile.addMisc(new Misc("misc_forestgrass_0" + (new Random().nextInt(2) +7) + ".png", Math.random() * 1.5, Math.random() * 1.5, Misc.TYPE_TILE));


                            tile.addMisc(new Misc("misc_grass_0" + (new Random().nextInt(2) + 1) + "_0" + (new Random().nextInt(2) + 1) + "_top.png", Math.random(), Math.random(), Misc.TYPE_TILE));
                            tile.addMisc(new Misc("misc_grass_0" + (new Random().nextInt(2) + 1) + "_0" + (new Random().nextInt(3) + 1) + "_top.png", Math.random(), Math.random(), Misc.TYPE_TILE));



                        } else if (color.equals(transition_meadow_tallgrass)) {
                            Tile tile = new Tile(x + (xx * World.CHUNK_WIDTH), y + (yy * World.CHUNK_HEIGHT), 0, new String[]{"grass_01_top.png", ""});
                            chunks[yy][xx].tiles[y][x] = tile;

                            for (int i = 0; i < 2; i++)
                                tile.addMisc(new Misc("misc_desertgrass_0" + (new Random().nextInt(4) + 1) + ".png", Math.random(), Math.random(), Misc.TYPE_TILE));
                            for (int i = 0; i < 2; i++)
                                tile.addMisc(new Misc("misc_meadowgrass_0" + (new Random().nextInt(4) + 1) + ".png", Math.random(), Math.random(), Misc.TYPE_TILE));
//                            for(int i = 0; i<2; i++)
//                                tile.addMisc(new Misc("misc_forestgrass_02.png", Math.random(), Math.random()));
                            if(new Random().nextInt(10) == 0 )
                            tile.addMisc(new Misc("misc_grass_0" + 5 + "_0" + (new Random().nextInt(1) + 1) + "_top.png", Math.random(), Math.random(), Misc.TYPE_TILE));

                            if(new Random().nextInt(30) == 0)
                            tile.addMisc(new Misc("misc_tallgrass_0"+(new Random().nextInt(3)+2)+".png", Math.random(), Math.random(), Misc.TYPE_TILE));

                            chunks[yy][xx].tiles[y][x] = tile;
                            //tile.addMisc(new Misc("misc_forestgrass.png", Math.random(), Math.random()));

                        } else {
                            chunks[yy][xx].tiles[y][x] = new Tile(x + (xx * World.CHUNK_WIDTH), y + (yy * World.CHUNK_HEIGHT), 0, new String[]{"grass_01_top.png", ""});
                        }
                    }
                }
            }
        }

        World world = new World(width, height, "test", chunks);
        world.addEntity(new Player(world));
        world.entities.get(0).setX(width / 2);
        world.entities.get(0).setY(height / 2);
        world.player = (Player) world.entities.get(0);
        world.playerAdded(world.player);

        //World.saveWorld(world);
        //World.loadWorld("test"); // test of functionality of these functions

        Main.canvas.loadWorld(world);
        world.start();
        loop.run();

    }


    public JFrame createFrame() {
        frame = new JFrame("Isometry");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

//		try {
//			switch (ruleset.get(RULE_SCREEN)) {
//			case RULE_SCREEN_FULLSCREEN:
//				if (device.isFullScreenSupported()) {
//					frame.setUndecorated(true);
//					device.setFullScreenWindow(frame);
//				} else {
//					frame.setLocation(0, 0);
//					frame.setSize(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height
//							- Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration()).bottom);
//				}
//				break;
//			case RULE_SCREEN_BORDERLESS:
//				if (device.isFullScreenSupported()) {
//					frame.setUndecorated(true);
//					frame.addFocusListener(new FocusListener() {
//
//						@Override
//						public void focusGained(FocusEvent arg0) {
//							frame.setAlwaysOnTop(true);
//						}
//
//						@Override
//						public void focusLost(FocusEvent arg0) {
//							frame.setAlwaysOnTop(false);
//						}
//					});
//					device.setFullScreenWindow(frame);
//				} else {
//					frame.setLocation(0, 0);
//					frame.setSize(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height
//							- Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration()).bottom);
//				}
//				break;
//			case RULE_SCREEN_WINDOWED:
//				frame.setLocation(0, 0);
//				frame.setSize(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height
//						- Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration()).bottom);
//				break;
//			default:
//				if (device.isFullScreenSupported()) {
//					frame.setUndecorated(true);
//					device.setFullScreenWindow(frame);
//				} else {
//					frame.setLocation(0, 0);
//					frame.setSize(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height
//							- Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration()).bottom);
//				}
//				break;
//			}
//		} catch (NullPointerException e) {
//			// ignore - no setting assigned. go to default.
//			if (device.isFullScreenSupported()) {
//				frame.setUndecorated(true);
//				device.setFullScreenWindow(frame);
//			} else {
//				frame.setLocation(0, 0);
//				frame.setSize(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height
//						- Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration()).bottom);
//				frame.toFront();
//			}
//
//		}

        //frame.setUndecorated(true);


        frame.setLocation(0, 0);
        frame.setSize(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height
                - Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration()).bottom);

        frame.setResizable(false);
        // frame.setVisible(true);
        WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
        HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
        return frame;
    }

    public BufferedImage createBiomeMap(int width, int height) {
        int[] oceans = {1};
        int rand = new Random().nextInt(10);
        if (rand == 0) {
            if (new Random().nextInt(1) == 0) {
                int r = new Random().nextInt(4);
                oceans = new int[]{r, r + 1, r - 1};
            }
        } else if (rand > 7) {
            int r = new Random().nextInt(4);
            oceans = new int[]{r, r + (new Random().nextBoolean() ? -1 : 1)};
        } else if (rand == 5) {
            oceans = new int[]{0, 1, 2, 3};
        } else {
            oceans = new int[]{new Random().nextInt(4)};
        }
        //0 = north, 1 = east, 2 = south, 3 = west
        //ocean = 1;
        //width = 400;
        //height = 400;
        BufferedImage sheet = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = sheet.createGraphics();


        //grass as default biome
        g2.setColor(forest);
        g2.fillRect(0, 0, width, height);

        // create meadows

        int maxRadius = 130;
        int radius = (int) (((4 + new Random().nextInt(2)) / 15.0) * maxRadius);

        Path2D path = new Path2D.Double();
        int variance = width / 16;
        int xCenter = (int) (width / 2 + ((new Random().nextBoolean() ? -1 : 1) * Math.random() * variance));
        int yCenter = (int) (height / 2 + ((new Random().nextBoolean() ? -1 : 1) * Math.random() * variance));


        ArrayList<Path2D> meadows = new ArrayList<>();
        ArrayList<Path2D> talls = new ArrayList<>();

        path.moveTo(xCenter + radius, yCenter);
        path.curveTo(xCenter + radius, yCenter, xCenter + radius * Math.random() * 2, yCenter + radius * Math.random() * 2, xCenter, yCenter + radius);
        path.curveTo(xCenter, yCenter + radius, xCenter - radius * Math.random() * 2, yCenter + radius * Math.random() * 2, xCenter - radius, yCenter);
        path.curveTo(xCenter - radius, yCenter, xCenter - radius * Math.random() * 2, yCenter - radius * Math.random() * 2, xCenter, yCenter - radius);
        path.curveTo(xCenter, yCenter - radius, xCenter + radius * Math.random() * 2, yCenter - radius * Math.random() * 2, xCenter + radius, yCenter);
        path.closePath();
        g2.setColor(transition_meadow_forest);
        BasicStroke stroke = new BasicStroke(3.0f,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND);
        g2.setStroke(stroke);
        g2.draw(path);
        g2.setColor(meadow);
        //g2.fill(path);

        meadows.add(path);


        Path2D tallpath = new Path2D.Double();
        radius = (int) (((4 + new Random().nextInt(2)) / 15.0) * maxRadius) / 2;

        tallpath.moveTo(xCenter + radius, yCenter);
        tallpath.curveTo(xCenter + radius, yCenter, xCenter + radius * Math.random() * 2, yCenter + radius * Math.random() * 2, xCenter, yCenter + radius);
        tallpath.curveTo(xCenter, yCenter + radius, xCenter - radius * Math.random() * 2, yCenter + radius * Math.random() * 2, xCenter - radius, yCenter);
        tallpath.curveTo(xCenter - radius, yCenter, xCenter - radius * Math.random() * 2, yCenter - radius * Math.random() * 2, xCenter, yCenter - radius);
        tallpath.curveTo(xCenter, yCenter - radius, xCenter + radius * Math.random() * 2, yCenter - radius * Math.random() * 2, xCenter + radius, yCenter);
        tallpath.closePath();

        g2.setColor(transition_meadow_tallgrass);
        stroke = new BasicStroke(5.0f,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND);
        g2.setStroke(stroke);
        //g2.draw(tallpath);
        g2.setColor(tallgrass);
        //g2.fill(tallpath);

        talls.add(tallpath);


        for (int i = 0; i < 8 + new Random().nextInt(2); i++) {
            path = new Path2D.Double();
            variance = width / 2;
            maxRadius = 100;
            radius = (int) (((2 + new Random().nextInt(3)) / 15.0) * maxRadius);
            xCenter = (int) (width / 2 + ((new Random().nextBoolean() ? -1 : 1) * Math.random() * variance * 1.1));
            yCenter = (int) (height / 2 + ((new Random().nextBoolean() ? -1 : 1) * Math.random() * variance * 1.1));
            path.moveTo(xCenter + radius, yCenter);
            //bezier curves
            path.curveTo(xCenter + radius, yCenter, xCenter + radius * Math.random() * 2, yCenter + radius * Math.random() * 2, xCenter, yCenter + radius);
            path.curveTo(xCenter, yCenter + radius, xCenter - radius * Math.random() * 2, yCenter + radius * Math.random() * 2, xCenter - radius, yCenter);
            path.curveTo(xCenter - radius, yCenter, xCenter - radius * Math.random() * 2, yCenter - radius * Math.random() * 2, xCenter, yCenter - radius);
            path.curveTo(xCenter, yCenter - radius, xCenter + radius * Math.random() * 2, yCenter - radius * Math.random() * 2, xCenter + radius, yCenter);

            path.closePath();
            if (i % 2 == 0) {
                tallpath = new Path2D.Double();
                radius = (int) (((4 + new Random().nextInt(2)) / 15.0) * maxRadius) / 2;

                tallpath.moveTo(xCenter + radius, yCenter);
                tallpath.curveTo(xCenter + radius, yCenter, xCenter + radius * Math.random() * 2, yCenter + radius * Math.random() * 2, xCenter, yCenter + radius);
                tallpath.curveTo(xCenter, yCenter + radius, xCenter - radius * Math.random() * 2, yCenter + radius * Math.random() * 2, xCenter - radius, yCenter);
                tallpath.curveTo(xCenter - radius, yCenter, xCenter - radius * Math.random() * 2, yCenter - radius * Math.random() * 2, xCenter, yCenter - radius);
                tallpath.curveTo(xCenter, yCenter - radius, xCenter + radius * Math.random() * 2, yCenter - radius * Math.random() * 2, xCenter + radius, yCenter);

                g2.setColor(transition_meadow_tallgrass);
                stroke = new BasicStroke(4.0f,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_ROUND);
                g2.setStroke(stroke);
                //g2.draw(tallpath);
                g2.setColor(tallgrass);
                //g2.fill(tallpath);
                talls.add(tallpath);

            }

            g2.setColor(transition_meadow_forest);
            stroke = new BasicStroke(5.0f,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_ROUND);
            g2.setStroke(stroke);
            g2.draw(path);
            g2.setColor(meadow);
            //g2.fill(path);
            meadows.add(path);
        }


        for (Path2D p : meadows) {
            g2.setColor(meadow);
            g2.fill(p);
        }
        for (Path2D p : talls) {
            g2.setColor(transition_meadow_tallgrass);
            stroke = new BasicStroke(5.0f,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_ROUND);
            g2.setStroke(stroke);
            g2.draw(p);
        }
        for (Path2D p : talls) {
            g2.setColor(tallgrass);
            g2.fill(p);
        }

        //draw sand

        int resolution = width / 40;

        Polygon[] polys = new Polygon[oceans.length];
        int index = 0;

        for (int ocean : oceans) {
            int baseY = (ocean == 2 ? height - 20 : 0) + (ocean == 1 ? width - 20 : 0) + (ocean == 3 || ocean == 0 ? 20 : 0);
            int deviation = 7;
            int dy = 1;
            int curY = baseY;

            GeneralPath gp = new GeneralPath();
            if (ocean % 2 == 0) {
                gp.moveTo(0, baseY);
            } else {
                gp.moveTo(baseY, 0);
            }
            for (int y = 1; y < width / resolution; y++) {
                if (curY > baseY + deviation) {
                    dy = -(1 + new Random().nextInt(3));
                } else if (curY < baseY - deviation) {
                    dy = (1 + new Random().nextInt(2));
                } else {
                    dy += 1.5 * (1 - new Random().nextInt(3));
                }
                curY += dy;
                if (ocean == 2 || ocean == 0) {
                    gp.lineTo(y * resolution, curY);
                } else {
                    gp.lineTo(curY, y * resolution);
                }
            }
            curY += 2 * (1 - new Random().nextInt(3));
            switch (ocean) {
                case 0:
                    gp.lineTo(width, curY);
                    gp.lineTo(width, 0);
                    gp.lineTo(0, 0);
                    gp.lineTo(0, baseY);
                    gp.closePath();
                    break;
                case 2:
                    gp.lineTo(width, curY);
                    gp.lineTo(width, height);
                    gp.lineTo(0, height);
                    gp.lineTo(0, baseY);
                    gp.closePath();
                    break;
                case 1:
                    gp.lineTo(curY, height);
                    gp.lineTo(width, height);
                    gp.lineTo(width, 0);
                    gp.lineTo(baseY, 0);
                    gp.closePath();
                    break;
                case 3:
                    gp.lineTo(curY, height);
                    gp.lineTo(0, height);
                    gp.lineTo(0, 0);
                    gp.lineTo(baseY, 0);
                    gp.closePath();
                    break;
            }

            gp.closePath();
            double[][] bottomPoints = getPoints(gp);
            int[] xBot = new int[bottomPoints.length];
            int[] yBot = new int[bottomPoints.length];

            for (int i = 0; i < bottomPoints.length; i++) {
                xBot[i] = (int) bottomPoints[i][0];
                yBot[i] = (int) bottomPoints[i][1];
            }

            Polygon poly = new Polygon();
            poly.xpoints = xBot;
            poly.ypoints = yBot;
            poly.npoints = xBot.length;

            stroke = new BasicStroke(12.0f,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_ROUND);
            g2.setStroke(stroke);
            g2.setColor(sand);
            g2.drawPolygon(poly);

            polys[index++] = poly;
        }

        //draw ocean
        g2.setColor(water);
        for (Polygon poly : polys) {

            g2.fillPolygon(poly);
        }

        tallpath = new Path2D.Double();
//        if(new Random().nextBoolean()) {
//            double h = height*Math.random();
//            tallpath.moveTo(0, h);
//            tallpath.curveTo(0, h, width, height*Math.random(), width*Math.random(), height*Math.random());
//        } else {
//            double h = height*Math.random();
//            tallpath.moveTo(h, width);
//            Point end = new Point((int) (width*Math.random()*0.25) + width/8, height - (int) (height*Math.random()*0.25) + height/8);
//            tallpath.curveTo(h,0,width/2,height /2, end.x, end.y );
        //}

        int x = (int) (width*Math.random()/2) + width/4;
        Random random = new Random();
        int xInclination = (x >= width/2 ? 1 : -1);

        int oldY=0, oldX=x;

        ArrayList<Point> points = new ArrayList<>();
        for(int y = 0; y < height;) {
            if(random.nextInt(height/8) ==0) {
                if(random.nextInt(6)==0) {
                    xInclination = (x >= width/2 ? 1 : -1);
                } else {
                    xInclination = -xInclination;
                }
            }
            if(random.nextInt(4)==0) {
                y++;
            } else {
                x += -xInclination;
            }
            if(oldX == x && y!=0) {
                y++;
            }
            points.add(new Point(x, y));


            oldX = x;
            oldY = y;
        }

        int[] xBot = new int[points.size()];
        int[] yBot = new int[points.size()];
        Point[] pointsarray = points.toArray(new Point[0]);
        stroke = new BasicStroke(1.0f,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND);
        g2.setStroke(stroke);
        g2.setColor(river);

        for (int i = 0; i < points.size()-1; i++) {
            g2.drawLine((int) pointsarray[i].getX(), (int) pointsarray[i].getY(), (int) pointsarray[i+1].getX(), (int) pointsarray[i+1].getY());
        }

        stroke = new BasicStroke(1.0f,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND);
        g2.setStroke(stroke);
        g2.setColor(Color.black);
        //g2.drawPolygon(poly);
        //g2.fill(tallpath);

        return sheet;
    }

    public static double[] centroid(int[] x, int[] y) {
        double[] centroid = {0, 0};

        for (int i = 0; i < x.length; i++) {
            centroid[0] += x[i];
            centroid[1] += y[i];
        }

        int totalPoints = x.length;
        centroid[0] = centroid[0] / totalPoints;
        centroid[1] = centroid[1] / totalPoints;

        return centroid;
    }

    static double[][] getPoints(Path2D path) {
        List<double[]> pointList = new ArrayList<double[]>();
        double[] coords = new double[6];
        int numSubPaths = 0;
        for (PathIterator pi = path.getPathIterator(null);
             !pi.isDone();
             pi.next()) {
            switch (pi.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO:
                    pointList.add(Arrays.copyOf(coords, 2));
                    ++numSubPaths;
                    break;
                case PathIterator.SEG_LINETO:
                    pointList.add(Arrays.copyOf(coords, 2));
                    break;
                case PathIterator.SEG_CLOSE:
                    if (numSubPaths > 1) {
                        throw new IllegalArgumentException("Path contains multiple subpaths");
                    }
                    return pointList.toArray(new double[pointList.size()][]);
                default:
                    throw new IllegalArgumentException("Path contains curves");
            }
        }
        throw new IllegalArgumentException("Unclosed path");
    }


    public static void main(String[] args) {
        main = new Main();

        main.start();
    }

    public static boolean isKeyDown(int key) {
        if (keysDown.get(key) != null) {
            return keysDown.get(key);
        } else {
            return false;
        }
    }

    public static String getRootPath() {
        return
                new
                        JFileChooser().getFileSystemView().getDefaultDirectory().toString
                        () + "/"
                        + ROOT_FOLDER;
    }

    public static void updateRules() throws IOException {
        String path = ROOT + "/rules/";
        List<String> results = new ArrayList();

        File[] files = new File(path).listFiles();

        for (File file : files) {
            if (file.isFile()) {
                results.add(file.getName());
            }
        }

        for (File f : files) {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            int i = 1;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) { // comment

                    String[] variable = line.split("="); // [0] = name, [1] =
                    // value
                    try {
                        ruleset.put(Main.class.getField(variable[0]).getInt(main), Main.class.getField(variable[1]).getInt(main));

                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (NoSuchFieldException e) {
                        notify(ERROR_SETTINGS, i, variable[0] + "=" + variable[1]);
                    }
                }
            }
        }

    }

    public int countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean endsWithoutNewLine = false;
            while ((readChars = is.read(c)) != -1) {
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n')
                        ++count;
                }
                endsWithoutNewLine = (c[readChars - 1] != '\n');
            }
            if (endsWithoutNewLine) {
                ++count;
            }
            return count;
        } finally {
            is.close();
        }
    }

    public static String readFile(String path) {
        // progress = 0;
        // progress_title = "reading file";

        InputStream ins = null; // raw byte-stream
        Reader r = null; // cooked reader
        BufferedReader br = null; // buffered for readLine()
        String data = "";
        try {
            String line;

            ins = new FileInputStream(path);

            r = new InputStreamReader(ins, "UTF-8"); // leave charset out for
            // default
            br = new BufferedReader(r);

            while ((line = br.readLine()) != null) {
                data += line + "\n";
            }
        } catch (Exception e) {
            System.err.println(e.getMessage()); // handle exception
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Throwable t) { /* ensure close happens */
                }
            }
            if (r != null) {
                try {
                    r.close();
                } catch (Throwable t) { /* ensure close happens */
                }
            }
            if (ins != null) {
                try {
                    ins.close();
                } catch (Throwable t) { /* ensure close happens */
                }
            }
        }
        if (data.charAt(data.length() - 1) == '\n') {
            data = data.substring(0, data.length() - 1);

        }
        return data;
    }

    public static BufferedImage loadImage(String asset) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(ASSETS + "/" + asset));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }

    private static void notify(int flag, int i, String message) {
        notifications.add(Notification.createNotification(flag, i, message));
    }

    public static void err(String string) {
        System.err.println(string);

    }

}
