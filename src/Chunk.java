import java.io.Serializable;


public class Chunk implements Serializable {
	public Tile[][] tiles;
	boolean ready = false;
	boolean relevant = false;
	boolean rendered = false;
	int x, y;
	public Chunk(World world) {
		World world1 = world;
		rendered = false;
	}

	public void rendered(boolean b) {
		rendered = b;
	}
	
	public Chunk(int xx, int yy) {
		x = xx;
		y = yy;
		rendered = false;
	}

	public boolean isReady() {
		return ready;                                                             
	}
	
	public boolean isRelevant() {
		return relevant;
	}
	
	public void addTile(Tile t) {
		tiles[t.y][t.x] = t; 
	}
	
	@Override
	public String toString() {
		String toReturn = "<chunk>\n<meta>\n"+x+"\n"+y+"\n/>\n";
		for(Tile[] ts : tiles) {
			for(Tile t : ts) {
				System.out.println(t.toString());
				toReturn += "<tile>\n"+t.toString()+"\n/>\n";
			}
		}
		toReturn += "/>chunk";
		return toReturn;
	}

	public boolean isRendered() {
		return rendered;
	}
}
