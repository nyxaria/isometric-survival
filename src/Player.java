import javax.imageio.ImageIO;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Player extends Entity {
    Camera camera = new Camera();
    public double movementSpeed = 0.020;
    World world;
    String set = "regular";

    public Player(World world) {
        this.world = world;
        loadImages();
        image = Canvas.tiler.get("regular_6_0");

    }

    private void loadImages() {
        String[] sets = {"archer", "regular"};
        for(String set : sets) {
            for(int i = 0; i < 8; i++) {
                for(int r = 0; r < 32; r++) {
                    Canvas.tiler.add(set + "_" + i + "_" + r, loadImage(set + "_" + i + "_" + r + ".png"));
                }
            }
        }
    }

    public int oldChunkX, oldChunkY, newChunkX, newChunkY;

    @Override
    public void update() {
        camera.goTo(this);
        world.camera.goTo(this);
        move();
        newChunkX = (int) (getX() / (World.CHUNK_WIDTH));
        newChunkY = (int) (getY() / (World.CHUNK_HEIGHT));
        if(newChunkX != oldChunkX || newChunkY != oldChunkY) {
            oldChunkX = newChunkX;
            oldChunkY = newChunkY;

            world.canvas.getRelevantRenderSheetTo(this);
        }

        if(Main.isKeyDown(KeyEvent.VK_SHIFT)) {
            movementSpeed = .024;
        } else {
            movementSpeed = .018;
        }

        updateImage();

        // System.out.println(getY());

    }

    // String[] dirs = {"east", "west", "north", "south", "east_south",
    // "south_west", "north_east", "west_north"};
    int dir = 6;
    long start = System.currentTimeMillis();
    double fps = 3.0;
    private int lastReleasedDir = 6;

    public void updateImage() {
        boolean keyUp = Main.isKeyDown(KeyEvent.VK_W) || Main.isKeyDown(KeyEvent.VK_UP);
        boolean keyDown = Main.isKeyDown(KeyEvent.VK_S) || Main.isKeyDown(KeyEvent.VK_DOWN);
        boolean keyLeft = Main.isKeyDown(KeyEvent.VK_A) || Main.isKeyDown(KeyEvent.VK_LEFT);
        boolean keyRight = Main.isKeyDown(KeyEvent.VK_D) || Main.isKeyDown(KeyEvent.VK_RIGHT);

        if(keyUp && !keyDown) {
            dir = 2;
            if(keyLeft && !keyRight) {
                dir = 1;
            } else if(keyRight && !keyLeft) {
                dir = 3;
            }
        } else if(keyDown && !keyUp) {
            dir = 6;
            if(keyLeft && !keyRight) {
                dir = 7;
            } else if(keyRight && !keyLeft) {
                dir = 5;
            }
        } else if(keyLeft && !keyRight) {
            dir = 0;
        } else if(keyRight && !keyLeft) {
            dir = 4;
        } else { //no key down
            image = Canvas.tiler.get(set + "_" + lastReleasedDir + "_0");
            world.canvas.changed = true;
            return;
        }
        lastReleasedDir = dir;
        fps = (1 / movementSpeed + 0.01) / 71;
        double animationsToDo = 8;
        image = Canvas.tiler.get(set + "_" + dir + "_" + (4 + (int) (((System.currentTimeMillis() - start) / ((fps * 1000) / animationsToDo)) % animationsToDo)));
        world.canvas.changed = true;

    }

    public boolean inBounds(double x, double y) {
        if(x < 0 || y < 0 || x > world.width || y > world.height) {
            return false;
        }

        return !colliding(x, y);

    }

    private boolean colliding(double x, double y) {
        if(getX() / World.CHUNK_WIDTH < world.width && getY() / World.CHUNK_HEIGHT < world.height) {

            /*Tile[] toCheck = new Tile[9];
            for(int i = 0; i < 9; i++) {
                toCheck[i] = world.getTile((int) x - 1 + i, (int) y - 1 + (i) / 3);
            }*/
            Tile t = world.getTile((int) x, (int) y);
            for(Wall wall : t.walls) {
                switch(wall.orientation) {
                    case Wall.ORIENTATION_NORTH_EAST:
                        if(y > t.y && y < t.y + 0.05) {
                            return true;
                        }
                        break;
                    case Wall.ORIENTATION_EAST_SOUTH:
                        if(x > t.x + 0.95 && x < t.x + 1) {
                            return true;
                        }
                        break;
                    case Wall.ORIENTATION_SOUTH_WEST:
                        if(y > t.y + 0.95 && y < t.y + 1) {
                            return true;
                        }

                        break;
                    case Wall.ORIENTATION_WEST_NORTH:
                        if(x > t.x && x < t.x + 0.05) {
                            return true;
                        }
                        break;
                    case Wall.ORIENTATION_CENTER:
                        if(x > t.x + 0.4 && x < t.x + 0.6 && y > t.y + 0.4 && y < t.y + 0.6) {
                            return true;
                        }
                        break;
                    case Wall.ORIENTATION_NORTH:
                        if(x > t.x && x < t.x + 0.2 && y > t.y && y < t.y + 0.2) {
                            return true;
                        }
                        break;
                    case Wall.ORIENTATION_FULL:
                        if(x > t.x && x < t.x + 1 && y > t.y && y < t.y + 1) {
                            return true;
                        }
                        break;
                }
            }
        }

        return false;
    }

    public void move() {
        boolean keyUp = Main.isKeyDown(KeyEvent.VK_W) || Main.isKeyDown(KeyEvent.VK_UP);
        boolean keyDown = Main.isKeyDown(KeyEvent.VK_S) || Main.isKeyDown(KeyEvent.VK_DOWN);
        boolean keyLeft = Main.isKeyDown(KeyEvent.VK_A) || Main.isKeyDown(KeyEvent.VK_LEFT);
        boolean keyRight = Main.isKeyDown(KeyEvent.VK_D) || Main.isKeyDown(KeyEvent.VK_RIGHT);

        if(keyUp) {
            double newX = getX() - movementSpeed;
            double newY = getY() - movementSpeed;
            if(inBounds(newX, newY)) {
                setY(newY);
                setX(newX);
            }
        }
        if(keyDown) {
            double newX = getX() + movementSpeed;
            double newY = getY() + movementSpeed;
            if(inBounds(newX, newY)) {
                setY(newY);
                setX(newX);
            }
        }
        if(keyLeft) {
            double newX = getX() - movementSpeed;
            double newY = getY() + movementSpeed;
            if(inBounds(newX, newY)) {
                setY(newY);
                setX(newX);
            }
        }
        if(keyRight) {
            double newX = getX() + movementSpeed;
            double newY = getY() - movementSpeed;
            if(inBounds(newX, newY)) {
                setY(newY);
                setX(newX);
            }
        }

        // world.canvas.focus = world.canvas.isoToScreen(getX(), getY(), getZ());

    }

    @Override
    public String toString() {
        return "<player>\n" + getX() + "\n" + getY() + "\n/>";
    }

    @Override
    public void keyReleased(int val) {
        switch(val) {
            case KeyEvent.VK_W:
                // lastReleasedDir = "north";
                break;
            case KeyEvent.VK_S:
                // lastReleasedDir = "south";
                break;
            case KeyEvent.VK_A:
                // lastReleasedDir = "west";
                break;
            case KeyEvent.VK_D:
                // lastReleasedDir = "east";
                break;
        }
    }

    public BufferedImage loadImage(String asset) {
        BufferedImage img = null;
        System.out.println(Main.ASSETS + "/" + asset);
        try {
            img = ImageIO.read(getClass().getResource(Main.ASSETS + "/" + asset));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }

}
