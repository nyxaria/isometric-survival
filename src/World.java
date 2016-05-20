import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class World implements Serializable {

	public  static final int CHUNK_WIDTH = 4;
	public static final int CHUNK_HEIGHT = 4;
	public int width, height;
	public String save;
	//Tile[][] tiles;
	Chunk[][] chunks;
	List<Entity> entities = new ArrayList<Entity>();
	public Canvas canvas;
	boolean paused = true;
	Player player;
	Camera camera = new Camera();

	public World(int width, int height, String save, Chunk[][] chunks) {
		this.width = width;
		this.height = height;
		this.save = save;

		this.chunks = chunks;


		//entities = new ArrayList<Entity>();
	}

	public World() {
		//entities = new ArrayList<Entity>();
	}

	public void setDims(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public void addEntity(Entity e) {
		entities.add(e);
	}

	public void setEntites(Entity[] newList) {
		entities.clear();

		for (Entity e : newList) {
			entities.add(e);
		}
	}

	public Tile getTile(int x, int y) {
		if (x >= 0 && x < width && y >= 0 && y < height) {
			return chunks[y/CHUNK_HEIGHT][x/CHUNK_WIDTH].tiles[y%CHUNK_HEIGHT][x%CHUNK_WIDTH];
		}
		return null;
	}
	
	public Chunk getChunk(int x, int y) {
		if (x >= 0 && x < chunks[0].length && y >= 0 && y < chunks.length) {
			return chunks[y][x];
		}
		return null;
	}

	public void start() {
		
		/*player = new Player(this);
		player.setX((double)0);
		player.setY((double)0);
		player.camera = new Camera();
		player.camera.goTo(player);
		addEntity(player);
		camera = player.camera;*/
		paused = false;

	}

	public static void loadWorld(String name) {
		LoadingBar progress = new LoadingBar(Canvas.SCREEN_WIDTH/2, Canvas.SCREEN_HEIGHT/2, 200, 15);
		//Main.loadingBars.add(progress);
		
		Thread task = new Thread(new WorldRunnable(name, WorldRunnable.ACTION_LOAD));
		task.run();

	}

	public void setData(String data, int i) {
		switch(i) {
		case 0: //width
			width = Integer.parseInt(data);
			break;
		case 1: //height
			height = Integer.parseInt(data);
			break;
		default:
			break;
		}
		
	}
	
	public void update() {
	}


	@Override
	public String toString() {
		return width + "\n" + height;
	}

	public static void saveWorld(World world) {
		//LoadingBar progress = new LoadingBar(Canvas.SCREEN_WIDTH/2, Canvas.SCREEN_HEIGHT/2, 200, 15);
		//progress.title = "Saving";
		//Main.loadingBars.add(progress);
		
		Thread task = new Thread(new WorldRunnable(world, WorldRunnable.ACTION_SAVE));
		task.run();
		//Main.canvas.refresh();
		
	}

	public void playerAdded(Player p) {
		player = p;
		camera = p.camera;
	}

}
