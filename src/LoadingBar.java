import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;


public class LoadingBar extends Rectangle{
	
	int end, current;
	Color filled = new Color(240,230,140), background = Color.GRAY, border = Color.DARK_GRAY;
	String title = "Loading";
	Font font = new Font("Arial", Font.PLAIN, 12);
	
	public LoadingBar(int x, int y, int width, int height) {
		super(x, y, width, height);
	}
	
	public void setColorScheme(Color filled, Color background, Color border) {
		this.filled = filled;
		this.background = background;
		this.border = border;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public Color getTitleColor() {
		  double y = (299 * background.getRed() + 587 * background.getGreen() + 114 * background.getBlue()) / 1000;
		  return y >= 128 ? Color.black : Color.white;
	}
	
	public double getPercentage() {
		return (double)current / (double)end;
	}
	
}
