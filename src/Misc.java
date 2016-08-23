import java.awt.image.BufferedImage;

/**
 * Created by gedr on 27/05/2016.
 */
public class Misc {
    String asset;
    BufferedImage image;
    double x, y;
    int type;

    public static final int TYPE_TILE = 0;
    public static final int TYPE_ENTITY = 1;
    public static final int TYPE_WALL = 2;

    public Misc(String asset, double x, double y, int type) {
        this.asset = asset;
        this.x = x;
        this.y = y;
        this.type = type;
    }
}
