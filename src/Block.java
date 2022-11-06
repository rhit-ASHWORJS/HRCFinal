import java.util.LinkedList;

public class Block {
	private int blockID;
	private Position position;
	private Block nextBlock;
	
	public Block(int blockID, Position p) {
		this.blockID = blockID;
		this.position = p;
		this.nextBlock = null;
	}
	
	public void add(Block b) {
		nextBlock = b;
	}
	
	public Position getPosition() {
		return position;
	}
	
	public void setPosition(Position p) {
		this.position = p;
	}
	
	public int getID() {
		return blockID;
	}
	
	public Block getNextBlock() {
		return nextBlock;
	}

}
