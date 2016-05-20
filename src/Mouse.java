
public class Mouse {
	public static int x, y;
	public static boolean click;
	
	public Mouse() {
		
	}
	
	public static void set(int newx, int newy) {
		x = newx;
		y = newy;
	}
	
	public static void click() {
		click = true;
	}
}
