import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.io.Serializable;

public class Wall implements Serializable {
	public int orientation;
	String asset;
	public int z;

	public static final int ORIENTATION_NORTH_EAST = 0;
	public static final int ORIENTATION_EAST_SOUTH = 1;
	public static final int ORIENTATION_SOUTH_WEST = 2;
	public static final int ORIENTATION_WEST_NORTH = 3;
	public static final int ORIENTATION_CENTER = 4;
	public static final int ORIENTATION_NORTH = 5;
	public static final int ORIENTATION_FULL = 6;


	public Wall(int z, int orientation, String asset) {
		this.orientation = orientation;
		this.asset = asset;
		this.z = z;
	}

	public BufferedImage getAsset(float opacity) {
		if(asset.equals(""))
			return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

		if (!Canvas.tiler.exists(asset)) {
			Canvas.tiler.add(asset, loadImage(asset));
		}
		if (!Canvas.tiler.exists(asset + "_" + opacity)) {

			BufferedImage old = Canvas.tiler.get(asset);
			if (opacity == 1f) {
				Canvas.tiler.add(asset + "_" + opacity, old);
			} else {
				BufferedImage img = new BufferedImage(old.getWidth(), old.getHeight(), BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2 = img.createGraphics();
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
				g2.drawImage(old, 0, 0, null);
				old = null;
				Canvas.tiler.add(asset + "_" + opacity, img);
			}
		}
		if (orientation == ORIENTATION_CENTER || orientation == ORIENTATION_NORTH) {
			Canvas.tiler.get(asset + "_" + opacity);
		}
		if (!Canvas.tiler.exists(asset + "_" + opacity + "_" + orientation)) {
			BufferedImage changed = Canvas.tiler.get(asset + "_" + opacity);
//			float shade = 1f;
//			switch (orientation) {
//			case ORIENTATION_NORTH_EAST:
//			case ORIENTATION_SOUTH_WEST:
//				AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
//				tx.translate(-changed.getWidth(null), 0);
//				AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
//				changed = op.filter(changed, null);
//				shade = 0.7f;
//				break;
//			case ORIENTATION_EAST_SOUTH:
//			case ORIENTATION_WEST_NORTH:
//				shade = 0.9f;
//				break;
//			}
//
//			float[] scales = new float[] { shade, shade, shade, 1.0f };
//			float[] offsets = new float[4];
//			BufferedImage temp = new BufferedImage(changed.getWidth(), changed.getHeight(), changed.getType());
//			RescaleOp rop = new RescaleOp(scales, offsets, null);
//			Graphics2D g = temp.createGraphics();
//			g.drawImage(changed, rop, 0, 0);

			Canvas.tiler.add(asset + "_" + opacity + "_" + orientation, Canvas.tiler.get(asset + "_" + opacity));
			return Canvas.tiler.get(asset + "_" + opacity);
		} else {
			return Canvas.tiler.get(asset + "_" + opacity + "_" + orientation);
		}

	}

	public BufferedImage getDefaultAsset() {

		BufferedImage changed = Tile.defaultSideTileRight();
		float shade = 1f;
		switch (orientation) {
		case ORIENTATION_NORTH_EAST:
		case ORIENTATION_SOUTH_WEST:
			AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
			tx.translate(-changed.getWidth(null), 0);
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			changed = op.filter(changed, null);
			shade = 0.8f;
			break;
		case ORIENTATION_EAST_SOUTH:
		case ORIENTATION_WEST_NORTH:
			shade = 0.9f;
			break;
		}

		float[] scales = new float[] { shade, shade, shade, 1.0f };
		float[] offsets = new float[4];
		BufferedImage temp = new BufferedImage(changed.getWidth(), changed.getHeight(), changed.getType());
		RescaleOp rop = new RescaleOp(scales, offsets, null);
		Graphics2D g = temp.createGraphics();
		g.drawImage(changed, rop, 0, 0);

		Canvas.tiler.add(asset + "_" + orientation, temp);
		return temp;

	}

	@Override
	public String toString() {
		return z + "\n" + orientation + "\n" + asset;
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
