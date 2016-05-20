import java.awt.image.BufferedImage;
import java.io.Serializable;


public abstract class Entity implements Serializable{
	private double x = 0;
	private double y = 0;
	private double z = 0;
	
	public BufferedImage image;
	public String asset;
	public boolean drawn;
	
	public double getX() {// do camera - but first setup player and Camera class aswellas entity :)
		return x;
	}
	public double getY() {
		return y;
	}
	public double getZ() {
		return z;
	}
	
	public void setX(double x) {
		this.x = x;
	}
	public void setY(double y) {
		this.y = y;
	}
	public void setZ(double z) {
		this.z = z;
	}
	
	public boolean isKeyDown(int key_code) {
		return Main.isKeyDown(key_code);
	}
	
	public abstract void keyReleased(int val);
	
	public BufferedImage getImage() {
		return image;
	}
	
	public abstract void update();
	
	@Override
	public String toString() {
		return "<entity>\n"+x+"\n"+y+"\n"+asset+"\n/>";
		
	}
}
