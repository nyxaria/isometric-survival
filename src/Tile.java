import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.Serializable;
import java.util.ArrayList;

public class Tile implements Serializable {
	public static final int ORIENTATION_TOP_RIGHT = 0;
	public static final int ORIENTATION_TOP_LEFT = 1;
	public static final int ORIENTATION_BOTTOM_RIGHT = 2;
	public static final int ORIENTATION_BOTTOM_LEFT = 3;

	public static final float SHADE_LEFT = 0.75f;
	public static final float SHADE_RIGHT = 0.6f;
	public static final float SHADE_NONE = 1f;

	int x, y, z = 1;
	int realY, realX;
	ArrayList<String> assets = new ArrayList<String>();
	ArrayList<Wall> walls = new ArrayList<Wall>();
	private boolean highlighted;


	public Tile(int x, int y, int z, String[] assets) {
		this.x = x;
		this.y = y;
		this.z = z;
		for (String s : assets) {
			if(!s.isEmpty())
			this.assets.add(s);
		}
		renderImages();

	}

	public Tile() {
		y = -1;
		x = -1;
		// manual implementation of variables
	}

	public void setAssets(String[] assets) {
		for (String s : assets) {
			this.assets.add(s);
		}
		if (x != -1 && y != -1) {
			renderImages();
		}
	}

	public void setCoords(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		if (assets.size() - 1 > 0) {
			renderImages();
		}
	}

	private void renderImages() {
		for (String asset : assets) {
			if (asset.endsWith("top.png") && !Canvas.tiler.exists(asset)) {
				Canvas.tiler.add(asset, Main.loadImage(asset));
				// System.out.println(Canvas.tiler.get(asset));

			} else if (asset.endsWith("side.png") && !Canvas.tiler.exists(asset)) {
				Canvas.tiler.add(asset, Main.loadImage(asset));
				getSideTile(Tile.ORIENTATION_BOTTOM_RIGHT, Tile.SHADE_RIGHT);
				getSideTile(Tile.ORIENTATION_BOTTOM_LEFT, Tile.SHADE_LEFT);
				getSideTile(Tile.ORIENTATION_TOP_RIGHT, Tile.SHADE_RIGHT);
				getSideTile(Tile.ORIENTATION_TOP_LEFT, Tile.SHADE_LEFT);
				// JOptionPane.showMessageDialog(null, new JLabel(new
				// ImageIcon(Canvas.tiler.get(assets[1]))), "About",
				// JOptionPane.PLAIN_MESSAGE, null);

			} else if (!Canvas.tiler.exists(asset)) {

				Canvas.tiler.add(asset, Main.loadImage(asset));

			}

		}
	}

	public BufferedImage getTopTile() {
		if(highlighted) {
			return defaultTopTile();
		}
		if (!Canvas.tiler.exists(assets.get(0))) {
			Canvas.tiler.add(assets.get(0), Main.loadImage(assets.get(0)));
		}
		if (!Canvas.tiler.exists(assets.get(0) + "_" + z)) {
			BufferedImage changed = Canvas.tiler.get(assets.get(0));
//			float shade = 1.0f + (float)(z*0.04);
//
//
//			float[] scales = new float[] { shade, shade, shade, 1.0f };
//			float[] offsets = new float[4];
//			BufferedImage temp = new BufferedImage(changed.getWidth(), changed.getHeight(), changed.getType());
//			RescaleOp rop = new RescaleOp(scales, offsets, null);
//			Graphics2D g = temp.createGraphics();
//			g.drawImage(changed, rop, 0, 0);
			//changed = temp;
			Canvas.tiler.add(assets.get(0) + "_" + z, changed);
			return changed;
		} else {
			return Canvas.tiler.get(assets.get(0) + "_" + z);
		}
	}

	public BufferedImage getSideTile(int orientation, float darken) {
 		if (!Canvas.tiler.exists(assets.get(1))) {
			Canvas.tiler.add(assets.get(1), Main.loadImage(assets.get(1)));
		}
		if (!Canvas.tiler.exists(assets.get(1) + "_" + orientation + "_" + z + "_" + darken)) {
			BufferedImage changed = Canvas.tiler.get(assets.get(1));

			switch (orientation) { // mirror if left side is needed
			case ORIENTATION_TOP_LEFT:
			case ORIENTATION_BOTTOM_LEFT:
				AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
				tx.translate(-changed.getWidth(null), 0);
				AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
				changed = op.filter(changed, null);
				break;
			}

			switch (orientation) { // darken corresponding to side
			case ORIENTATION_TOP_LEFT:
			case ORIENTATION_BOTTOM_LEFT:
				float shade = darken + (float)(z*0.02);
				float[] scales = new float[] { shade, shade, shade, 1.0f };
				
				float[] offsets = new float[4];
				BufferedImage temp = new BufferedImage(changed.getWidth(), changed.getHeight(), changed.getType());
				RescaleOp rop = new RescaleOp(scales, offsets, null);
				Graphics2D g = temp.createGraphics();
				g.drawImage(changed, rop, 0, 0);
				changed = temp;
				// RescaleOp op = new RescaleOp(SHADE_RIGHT, 0, null);
				// changed = op.filter(changed, null);

				break;
			case ORIENTATION_TOP_RIGHT:
			case ORIENTATION_BOTTOM_RIGHT:
				shade = darken + (float)(z*0.02);
				scales = new float[] { shade, shade, shade, 1.0f };

				temp = new BufferedImage(changed.getWidth(), changed.getHeight(), changed.getType());
				offsets = new float[4];
				rop = new RescaleOp(scales, offsets, null);
				g = temp.createGraphics();
				g.drawImage(changed, rop, 0, 0);
				changed = temp;
				// op = new RescaleOp(SHADE_LEFT, 0, null);
				// changed = op.filter(changed, null);
				break;
			default:
			}

			Canvas.tiler.add(assets.get(1) + "_" + orientation + "_" + z + "_" + darken, changed);

			return changed;
		} else {
			return Canvas.tiler.get(assets.get(1) + "_" + orientation + "_" + z + "_" + darken);
		}
	}


	public static BufferedImage defaultTopTile() {

		if (!Canvas.tiler.exists("defaultTop")) {
			int x = 0, y = 0;

			BufferedImage image = new BufferedImage(Canvas.TILE_WIDTH, Canvas.TILE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = image.createGraphics();
			Polygon p = new Polygon();
			p.addPoint((x * Canvas.TILE_WIDTH / 2), ((x + y * 2) * Canvas.TILE_HEIGHT / 2) + (Canvas.TILE_HEIGHT / 2));
			p.addPoint((x * Canvas.TILE_WIDTH / 2) + (Canvas.TILE_WIDTH / 2), ((x + y * 2) * Canvas.TILE_HEIGHT / 2) + (Canvas.TILE_HEIGHT));
			p.addPoint((x * Canvas.TILE_WIDTH / 2) + (Canvas.TILE_WIDTH), ((x + y * 2) * Canvas.TILE_HEIGHT / 2) + (Canvas.TILE_HEIGHT / 2));
			p.addPoint((x * Canvas.TILE_WIDTH / 2) + (Canvas.TILE_WIDTH / 2), ((x + y * 2) * Canvas.TILE_HEIGHT / 2));
			g2.setColor(Color.cyan);
			g2.fill(p);
			g2.setColor(Color.LIGHT_GRAY);
			g2.draw(p);
			Canvas.tiler.add("defaultTop", image);
		}
		return Canvas.tiler.get("defaultTop");

	}

	public static BufferedImage defaultSideTileRight() {
		if (!Canvas.tiler.exists("defaultSideBottomRight")) {

			BufferedImage image = new BufferedImage(Canvas.TILE_WIDTH / 2, Canvas.TILE_HEIGHT + Canvas.TILE_HEIGHT / 2, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = image.createGraphics();
			Polygon p = new Polygon();
			p.addPoint(0, (Canvas.TILE_HEIGHT) / 2);
			p.addPoint((Canvas.TILE_WIDTH / 2), 0);
			p.addPoint((Canvas.TILE_WIDTH / 2), (Canvas.TILE_HEIGHT));
			p.addPoint(0, (Canvas.TILE_HEIGHT) + Canvas.TILE_HEIGHT / 2);

			g2.setColor(Color.DARK_GRAY);
			g2.fill(p);
			g2.setColor(Color.LIGHT_GRAY);
			g2.draw(p);
			Canvas.tiler.add("defaultSideBottomRight", image);
		}
		return Canvas.tiler.get("defaultSideBottomRight");
	}

	public static BufferedImage defaultSideTileLeft() {
		if (!Canvas.tiler.exists("defaultSideBottomLeft")) {

			BufferedImage image = new BufferedImage(Canvas.TILE_WIDTH / 2, Canvas.TILE_HEIGHT + Canvas.TILE_HEIGHT / 2, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = image.createGraphics();
			Polygon p = new Polygon();
			p.addPoint(0, 0);
			p.addPoint((Canvas.TILE_WIDTH / 2), (Canvas.TILE_HEIGHT) / 2);
			p.addPoint((Canvas.TILE_WIDTH / 2), (Canvas.TILE_HEIGHT) + Canvas.TILE_HEIGHT / 2);
			p.addPoint(0, (Canvas.TILE_HEIGHT));

			g2.setColor(Color.GRAY);
			g2.fill(p);
			g2.setColor(Color.LIGHT_GRAY);
			g2.draw(p);
			Canvas.tiler.add("defaultSideBottomLeft", image);
		}
		return Canvas.tiler.get("defaultSideBottomLeft");

	}



	@Override
	public String toString() {

		String toReturn = x + "\n" + y + "\n" + z + "\n";
		for (String s : assets) {
			toReturn += s + "\n";
		}
		for(Wall wall : walls) {
			toReturn += "<wall>\n"+wall.toString()+"\n/>\n";
		}
		toReturn = toReturn.substring(0, toReturn.length() - 1);// remove last
																// char
		return toReturn;
	}

	String retain[] = new String[2];
	public void setData(String data, int i) {
		switch (i) {
		case 0: // x
			x = Integer.parseInt(data);
			break;
		case 1: // y
			y = Integer.parseInt(data);
			break;
		case 2: // z
			z = Integer.parseInt(data);
			break;
		//case 3:// assets
		case 3:
			assets.add(data);
			break;
		default: //walls
			System.out.println(data);
			if(retain[0] == null) {
				retain[0] = data;
			} else if(retain[1] == null) {
				retain[1] = data;
			} else if(retain[1] != null) {
				Wall wall = new Wall(Integer.parseInt(retain[0]), Integer.parseInt(retain[1]), data);
				walls.add(wall);
				System.out.println("size: "+walls.size());

				retain[0] = null;
				retain[1] = null;
			}
			
			//assets.add(components[1]);
		}

	}

	public void highlight(boolean b) {
		highlighted = b;
	}

} 
