import java.awt.event.KeyEvent;

public class Player extends Entity {
    Camera camera = new Camera();
    public double movementSpeed = 0.020;
    World world;
    String set = "archer";

    public Player(World world) {
        this.world = world;
        loadImages();
        image = Canvas.tiler.get("regular_6_0");

    }

    private void loadImages() {
        String[] sets = {"archer", "regular"};
        for (String set : sets) {
            for (int i = 0; i < 8; i++) {
                for (int r = 0; r < 32; r++) {
                    Canvas.tiler.add(set + "_" + i + "_" + r, Main.loadImage(set + "_" + i + "_" + r + ".png"));
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
        if (newChunkX != oldChunkX || newChunkY != oldChunkY) {
            oldChunkX = newChunkX;
            oldChunkY = newChunkY;

            world.canvas.getRelevantRenderSheetTo(this);
        }


        if (Main.isKeyDown(KeyEvent.VK_SHIFT)) {
            movementSpeed = .1;
        } else {
            movementSpeed = .02;
        }

        updateImage();
        // System.out.println(getY());

    }

    // String[] dirs = {"east", "west", "north", "south", "east_south",
    // "south_west", "north_east", "west_north"};
    int dir = 6;
    long start = System.currentTimeMillis();
    double fps = 2.0;
    private int lastReleasedDir = 6;

    public void updateImage() {
        if (Main.isKeyDown(KeyEvent.VK_W) && !Main.isKeyDown(KeyEvent.VK_S)) {
            dir = 2;
            if (Main.isKeyDown(KeyEvent.VK_A) && !Main.isKeyDown(KeyEvent.VK_D)) {
                dir = 1;
            } else if (Main.isKeyDown(KeyEvent.VK_D) && !Main.isKeyDown(KeyEvent.VK_A)) {
                dir = 3;
            }
        } else if (Main.isKeyDown(KeyEvent.VK_S) && !Main.isKeyDown(KeyEvent.VK_W)) {
            dir = 6;
            if (Main.isKeyDown(KeyEvent.VK_A) && !Main.isKeyDown(KeyEvent.VK_D)) {
                dir = 7;
            } else if (Main.isKeyDown(KeyEvent.VK_D) && !Main.isKeyDown(KeyEvent.VK_A)) {
                dir = 5;
            }
        } else if (Main.isKeyDown(KeyEvent.VK_A) && !Main.isKeyDown(KeyEvent.VK_D)) {
            dir = 0;
        } else if (Main.isKeyDown(KeyEvent.VK_D) && !Main.isKeyDown(KeyEvent.VK_A)) {
            dir = 4;
        } else { //no key down
            image = Canvas.tiler.get(set + "_" + lastReleasedDir + "_0");
            return;
        }
        lastReleasedDir = dir;
        fps = (1 / movementSpeed) / 40;
        if (System.currentTimeMillis() - start <= start + 1000) {
            double animationsToDo = 8;
            image = Canvas.tiler.get(set + "_" + dir + "_" + (4 + (int) (((System.currentTimeMillis() - start) / ((fps * 1000) / animationsToDo)) % animationsToDo)));
        } else {
            start = System.currentTimeMillis();
        }

    }

    public boolean inBounds(double x, double y) {
        if (x < 0 || y < 0 || x > world.width || y > world.height) {
            return false;
        }

        return !colliding(x, y);

    }

    private boolean colliding(double x, double y) {
        if (getX() / World.CHUNK_WIDTH < world.width && getY() / World.CHUNK_HEIGHT < world.height) {

            Tile t = world.getTile((int) x, (int) y);
            for (Wall wall : t.walls) {
                switch (wall.orientation) {
                    case Wall.ORIENTATION_NORTH_EAST:
                        if (y > t.y && y < t.y + 0.05) {
                            return true;
                        }
                        break;
                    case Wall.ORIENTATION_EAST_SOUTH:
                        if (x > t.x + 0.95 && x < t.x + 1) {
                            return true;
                        }
                        break;
                    case Wall.ORIENTATION_SOUTH_WEST:
                        if (y > t.y + 0.95 && y < t.y + 1) {
                            return true;
                        }

                        break;
                    case Wall.ORIENTATION_WEST_NORTH:
                        if (x > t.x && x < t.x + 0.05) {
                            return true;
                        }
                        break;
                    case Wall.ORIENTATION_CENTER:
                        if (x > t.x + 0.4 && x < t.x + 0.6 && y > t.y + 0.4 && y < t.y + 0.6) {
                            return true;
                        }
                        break;
                    case Wall.ORIENTATION_NORTH:
                        if (x > t.x && x < t.x + 0.2 && y > t.y && y < t.y + 0.2) {
                            return true;
                        }
                        break;
                    case Wall.ORIENTATION_FULL:
                        if (x > t.x && x < t.x + 1 && y > t.y && y < t.y + 1) {
                            return true;
                        }
                        break;
                }
            }
        }

        return false;
    }

    public void move() {
        if (isKeyDown(KeyEvent.VK_W)) {
            double newX = getX() - movementSpeed;
            double newY = getY() - movementSpeed;
            if (inBounds(newX, newY)) {
                setY(newY);
                setX(newX);
            }
        }
        if (isKeyDown(KeyEvent.VK_S)) {
            double newX = getX() + movementSpeed;
            double newY = getY() + movementSpeed;
            if (inBounds(newX, newY)) {
                setY(newY);
                setX(newX);
            }
        }
        if (isKeyDown(KeyEvent.VK_A)) {
            double newX = getX() - movementSpeed;
            double newY = getY() + movementSpeed;
            if (inBounds(newX, newY)) {
                setY(newY);
                setX(newX);
            }
        }
        if (isKeyDown(KeyEvent.VK_D)) {
            double newX = getX() + movementSpeed;
            double newY = getY() - movementSpeed;
            if (inBounds(newX, newY)) {
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
        switch (val) {
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

}
