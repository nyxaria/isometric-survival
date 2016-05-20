import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Main {
	public static int WIDTH;

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
				if(canvas.world != null) {
					for(Entity entity : canvas.world.entities) {
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
				int TICKS_PER_SECOND = 60;
			    int SKIP_TICKS = 1000 / TICKS_PER_SECOND;
			    int MAX_FRAMESKIP = 10;
			    long start = System.currentTimeMillis();
			    long next_game_tick = System.currentTimeMillis();
			    int count = 0;
			    int loops;

			    boolean game_is_running = true;
			    while( game_is_running ) {

			        loops = 0;
			        while( System.currentTimeMillis() > next_game_tick && loops < MAX_FRAMESKIP) {
			            canvas.update();
			            if(System.currentTimeMillis() - start < 1000) {
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
			robot.mouseMove(Toolkit.getDefaultToolkit().getScreenSize().width/2, Toolkit.getDefaultToolkit().getScreenSize().height/2);
		} catch (AWTException e) {
			e.printStackTrace();
		}

		
			int width = 40;
		int height = 40;
		Random r = new Random();
		Chunk[][] chunks = new Chunk[height/World.CHUNK_HEIGHT][width/World.CHUNK_WIDTH];

		for(int yy = 0; yy < height/World.CHUNK_HEIGHT; yy++) {
			for(int xx = 0; xx < width/World.CHUNK_WIDTH; xx++) {
				
				chunks[yy][xx] = new Chunk(xx, yy);
				chunks[yy][xx].tiles = new Tile[World.CHUNK_HEIGHT][World.CHUNK_WIDTH];
				for(int y = 0; y < World.CHUNK_HEIGHT; y++) {
					for(int x = 0; x < World.CHUNK_WIDTH; x++) {
						int random = new Random().nextInt(4) + 1;

						chunks[yy][xx].tiles[y][x] = new Tile(x + (xx*World.CHUNK_WIDTH), y + (yy*World.CHUNK_HEIGHT), 0, new String[]{"grass_"+random+"_top.png", ""});
						if(new Random().nextInt(20) == 4) {
							chunks[yy][xx].tiles[y][x].walls.add(new Wall(0, Wall.ORIENTATION_NORTH, "tree_0"+(new Random().nextInt(8)+1)+"_"+new Random().nextInt(8)+".png"));
						}
							//chunks[yy][xx].tiles[y][x].walls.add(new Wall(0, Wall.ORIENTATION_SOUTH_WEST, "grass_side.png"));
							//chunks[yy][xx].tiles[y][x].highlight(true);

							//chunks[yy][xx].tiles[y][x].walls.add(new Wall(0, Wall.ORIENTATION_EAST_SOUTH, "grass_side.png"));
							//chunks[yy][xx].tiles[y][x].walls.add(new Wall(0, Wall.ORIENTATION_WEST_NORTH, "grass_side.png"));

						//}

						if(y == 1 && x == 1 && yy%2 == 0 && xx%2 == 0) {
							//chunks[yy][xx].tiles[y][x].walls.add(new Wall(0, Wall.ORIENTATION_NORTH, "tree_0"+(new Random().nextInt(8)+1)+"_"+new Random().nextInt(8)+".png"));

						}

						//System.out.println("chunks["+yy+"]["+xx+"].tiles["+y+"]["+x+"] = new Tile("+x + (xx*World.CHUNK_WIDTH)+", "+y + (yy*World.CHUNK_HEIGHT)+")");
					}
				}
				//Tile[][] tiles = new Tile[World.CHUNK_HEIGHT][World.CHUNK_WIDTH];

				//c.tiles = tiles;
			}
		}
		
		World world = new World(width, height, "test", chunks);
		System.out.println(world.chunks[0][0].tiles[0][0].walls.size());
		world.addEntity(new Player(world));
		world.entities.get(0).setX(0.5);
		world.entities.get(0).setY(0.5);
		
		World.saveWorld(world);
		World.loadWorld("test"); // test of functionality of these functions
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
				  () +"/"
				 + ROOT_FOLDER;
	}

	public static void updateRules() throws IOException {
		String path = ROOT + "/rules/";
		List<String> results = new ArrayList<String>();

		File[] files = new File(path).listFiles();
		// If this pathname does not denote a directory, then listFiles()
		// returns null.

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
