import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class WorldRunnable implements Runnable {

    String save;
    String content;
    LoadingBar progress;

    public static final int ACTION_SAVE = 0;
    public static final int ACTION_LOAD = 1;

    public int action;
    private World world;

    public WorldRunnable(String save, int action) {
        this.action = action;
        this.save = save;
    }

    public WorldRunnable(World world, int action) { // save
        this.action = action;
        this.world = world;
    }

    public void run() {
        try {
            if (action == ACTION_LOAD) {
                loadWorld(save);
            } else if (action == ACTION_SAVE) {
                saveWorld(world);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadWorld(String name) {
        int index = 0;
        String[] preallocatedArray;
        String path = Main.SAVES + "/" + name + "/";

        // meta
        World world = new World();
        String data = Main.readFile(path + "meta.txt");

        for (String line : data.split("\n")) {
            if (!line.startsWith("#") || !line.equals("")) {
                world.setData(line, index);
                index++;
            }
        }

        // entities

        preallocatedArray = new String[10];
        index = 0;
        String type = "";
        data = Main.readFile(path + "enteties.txt");
        for (String line : data.split("\n")) {

            if (!line.startsWith("#") || !line.equals("")) {
                if (type.equals("") && line.startsWith("<")) {
                    type = line;
                } else if (!type.equals("") && !line.equals("/>")) {
                    preallocatedArray[index] = line;

                    index++;

                } else if (line.equals("/>") && !type.equals("")) {

                    if (type.equals("<player>")) {

                        Player p = new Player(world);
                        p.setX(Double.parseDouble(preallocatedArray[0]));
                        p.setY(Double.parseDouble(preallocatedArray[1]));
                        world.addEntity(p);
                        world.playerAdded(p);
                    }

                    index = 0;
                    preallocatedArray = new String[10];
                    type = "";
                }

            }
        }


        //chunks and tiles
        preallocatedArray = new String[20];
        data = Main.readFile(path + "tiles.txt");
        String chunkData[] = data.split("(<chunk>)|(/>chunk)");
        int nested = 0;

        Chunk[][] chunks = new Chunk[world.height / World.CHUNK_HEIGHT][world.width / World.CHUNK_WIDTH];
        for (String s : chunkData) {
            Chunk chunk = new Chunk(world);
            chunk.tiles = new Tile[World.CHUNK_HEIGHT][World.CHUNK_WIDTH];

            //System.out.println("\n\n"+s+"\n");
            for (String line : s.split("\n")) {
                if (!line.startsWith("#") || !line.equals("")) {
                    if (line.equals("!")) {
                        world.save = name;
                        world.chunks = chunks;
                        Main.canvas.loadWorld(world);
                        world.start();
                    } else if (type.equals("") && line.startsWith("<")) {
                        type = line;
                    } else if (!type.equals("") && line.startsWith("<")) {
                        nested++;
                    } else if (line.equals("/>") && nested > 0) {
                        nested--;
                    } else if (!type.equals("") && !line.equals("/>")) {
                        preallocatedArray[index] = line;
                        index++;

                    } else if (line.equals("/>")) {
                        if (type.equals("<meta>")) {

                            chunk.x = Integer.parseInt(preallocatedArray[0]);
                            chunk.y = Integer.parseInt(preallocatedArray[1]);
                            chunks[chunk.y][chunk.x] = chunk;


                        }
                        if (type.equals("<tile>")) {
                            Tile t = new Tile();
                            int i = 0;
                            for (String data1 : preallocatedArray) {
                                if (data1 != null) {
                                    t.setData(data1, i);
                                    i++;
                                }
                            }

                            chunk.tiles[t.y % World.CHUNK_HEIGHT][t.x % World.CHUNK_WIDTH] = t;
                        }

                        type = "";
                        index = 0;
                        preallocatedArray = new String[20];
                    }
                }
            }
            chunk.ready = true;
        }
        world.save = name;
        world.chunks = chunks;
        Main.canvas.loadWorld(world);
        world.start();

    }


    public void saveWorld(World world) {
        BufferedWriter writer = null;
        //progress.end = world.tiles.length * world.tiles[0].length + 1;
        //int linesWritten = 0;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Main.SAVES + "/" + world.save + "/meta.txt"), "utf-8"));
            writer.write(world.toString());
            writer.close();

            //progress.current = linesWritten++;
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Main.SAVES + "/" + world.save + "/tiles.txt"), "utf-8"));

            for (Chunk[] cs : world.chunks) {
                for (Chunk c : cs) {
                    writer.write(c.toString() + "\n");
                    //progress.current = linesWritten++;
                }
            }
            writer.close();

            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Main.SAVES + "/" + world.save + "/enteties.txt"), "utf-8"));

            for (Entity e : world.entities) {
                writer.write(e.toString() + "\n");
            }

            writer.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
            }
        }
        //Main.loadingBars.remove(progress);
    }
}
