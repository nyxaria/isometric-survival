
public class Camera extends Entity {
	public void goTo(Entity e) {
		setX(e.getX());
		setY(e.getY());
		setZ(e.getZ());
	}
	
	public void goTo(int x, int y, int z) {
		setX(x);
		setY(y);
		setZ(z);
	}

	@Override
	public void update() {
		
	}

	@Override
	public void keyReleased(int val) {
		// TODO Auto-generated method stub
		
	}
}
