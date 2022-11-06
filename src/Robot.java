import java.util.Scanner;

/**
	Represents an intelligent agent moving through a particular room.	
	The robot only has one sensor - the ability to get the status of any  
	tile in the environment through the command env.getTileStatus(row, col).
	@author Adam Gaweda, Michael Wollowski
*/

public class Robot {
	private Environment env;
	private int posRow;
	private int posCol;
	private boolean toCleanOrNotToClean;
	private boolean pathFound;
	private Block b;
	
	/**
	    Initializes a Robot on a specific tile in the environment. 
	*/

	
	public Robot (Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		this.toCleanOrNotToClean = false;
		this.b = null;
	}
	
	public void init() {
		
	}
	
	public Block getBlock() {
		return this.b;
	}
	
	public boolean setBlock(Block b) {
		if (this.b == null) {
			this.b = b;
			return true;
		}
		return false;
	}
	
	public boolean getPathFound() {
		return pathFound;
	}
	
	public int getPathLength() {
		// TODO: modify this procedure to return the actual path length.
		// You will likely have to track it in some counter.
		return 0;
	}
	
	public int getPosRow() { return posRow; }
	public int getPosCol() { return posCol; }
	public void incPosRow() { posRow++; }
	public void decPosRow() { posRow--; }
	public void incPosCol() { posCol++; }
    public void decPosCol() { posCol--; }
	
	/**
	   Returns the next action to be taken by the robot. A support function 
	   that processes the path LinkedList that has been populates by the
	   search functions.
	*/
	public Action getAction () {
	    System.out.print("> ");
	    Scanner sc = new Scanner(System.in); 
        String name = sc.nextLine(); 
		if (name.equals("u")) {
			return Action.MOVE_UP;
		}
		if (name.equals("d")) {
			return Action.MOVE_DOWN;
		}
		if (name.equals("l")) {
			return Action.MOVE_LEFT;
		}
		if (name.equals("r")) {
			return Action.MOVE_RIGHT;
		}
		if (name.equals("us")) {
			return Action.UNSTACK;
		}
		if (name.equals("s")) {
			return Action.STACK;
		}
		if (name.equals("pd")) {
			return Action.PUT_DOWN;
		}
		if (name.equals("pu")) {
			return Action.PICK_UP;
		}		

		
		return Action.DO_NOTHING;
		
	}


}