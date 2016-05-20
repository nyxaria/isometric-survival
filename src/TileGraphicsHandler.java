import java.awt.image.BufferedImage;
import java.util.HashMap;


public class TileGraphicsHandler {
	private HashMap<String, BufferedImage> data = new HashMap<String, BufferedImage>();
	
	public TileGraphicsHandler() {
		
	}
	
	public void add(String key, BufferedImage value) {

		data.put(key, value);
	}
	
	public BufferedImage get(String key) {
		if(data.get(key)!= null) {
			return data.get(key);
		} else {
			Main.err("asset '"+key+"' is null");
			return null;
		}
	}
	
	public boolean exists(String key) {
		return data.get(key) != null;
	}

	
}
