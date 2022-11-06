
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * The world in which this simulation exists. As a base
 * world, this produces a 10x10 room of tiles. In addition,
 * 20% of the room is covered with "walls" (tiles marked as IMPASSABLE).
 * 
 * This object will allow the agent to explore the world and is how
 * the agent will retrieve information about the environment.
 * DO NOT MODIFY.
 * @author Adam Gaweda, Michael Wollowski
 */
public class Environment {
	private Tile[][] tiles;
	private int rows, cols;
	private LinkedList<Block> blocks = new LinkedList<>();
	private ArrayList<Robot> robots;
	private String[][] policy;
	
	public Environment(LinkedList<String> map, LinkedList<Position> targetPositions, ArrayList<Robot> robots) { 
		this.cols = map.get(0).length();
		this.rows = map.size();
		this.tiles = new Tile[rows][cols];
		int robotID = 0;
		for (int row = 0; row < this.rows; row++) {
			for (int col = 0; col < this.cols; col++) {
				char tile = map.get(row).charAt(col);
				switch(tile) {
				case 'R': tiles[row][col] = new Tile(TileStatus.CLEAN); {
					robots.add(new Robot(this, row, col));
					break;
				}
				case 'D': tiles[row][col] = new Tile(TileStatus.DIRTY); break;
				case 'C': tiles[row][col] = new Tile(TileStatus.CLEAN); break;
				case 'W': tiles[row][col] = new Tile(TileStatus.IMPASSABLE); break;
				case 'T': tiles[row][col] = new Tile(TileStatus.TARGET); 
						  Position tempP = new Position(row, col);
						  Block tempB = new Block(robotID++, tempP);
						  blocks.add(tempB);
						  for (Position p: targetPositions) {
							  if (p.equals(tempP)) {
								  Block tempBB = new Block(robotID++, tempP);
								  tempB.add(tempBB);
								  tempB = tempBB;
							  }
						  }
						  break;
				}
			}
		}
		this.robots = robots;
	}
	
	/* Traditional Getters and Setters */
	public Tile[][] getTiles() { return tiles; }
	
	public int getRows() { return this.rows; }
	
	public int getCols() { return this.cols; }

	public LinkedList<Block> getBlocks(){
		return this.blocks;
	}
	
	public ArrayList<Robot> getRobots(){
		return this.robots;
	}
	
//	public void remove(int row, int col) {
//		for (int i = 0; i < targets.size(); i++) {
//			if (targets.get(i).getRow() == row && targets.get(i).getCol() == col) {
//				targets.remove(i);
//			}
//		}
//	}
	
	public String[][]getPolicy(){
		return policy;
	}
	
//	public int getTargetRow() {
//		if (targets.size() == 1) return targets.get(0).getRow();
//		throw new UnsupportedOperationException("Not a valid procedure call. Use getTargets instead.");
//	}
//	
//	public int getTargetCol() {
//		if (targets.size() == 1) return targets.get(0).getCol();
//		throw new UnsupportedOperationException("Not a valid procedure call. Use getTargets instead.");
//	}

	/*
	 * Returns a the status of a tile at a given [row][col] coordinate
	 */
	public TileStatus getTileStatus(int row, int col) {
		if (row < 0 || row >= rows || col < 0 || col >= cols) return TileStatus.IMPASSABLE; 
		else return tiles[row][col].getStatus();
	}

	/* Counts number of tiles that are not walls */
	public int getNumTiles() {
		int count = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (this.tiles[row][col].getStatus() != TileStatus.IMPASSABLE)
                    count++;
            }
        }
        return count;
    }
	
	/* Cleans the tile at coordinate [x][y] */
	public void cleanTile(int x, int y) {
		tiles[x][y].cleanTile();		
	}
	
	/* Soils the tile at coordinate [x][y] */
	public void soilTile(int x, int y) {
		tiles[x][y].soilTile();		
	}
	
	/* Counts number of clean tiles */
	public int getNumCleanedTiles() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (this.tiles[i][j].getStatus() == TileStatus.CLEAN)
                    count++;
            }
        }
        return count;
    }
	
	/* Counts number of dirty tiles */
	public int getNumDirtyTiles() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (this.tiles[i][j].getStatus() == TileStatus.DIRTY)
                    count++;
            }
        }
        return count;
    }

	/* Determines if a particular [row][col] coordinate is within
	 * the boundaries of the environment. This is a rudimentary
	 * "collision detection" to ensure the agent does not walk
	 * outside the world (or through walls).
	 */
	public boolean validPos(int row, int col) {
	    return row >= 0 && row < rows && col >= 0 && col < cols &&
	    		tiles[row][col].getStatus() != TileStatus.IMPASSABLE;
	}
	
	public boolean isTarget(int row, int col) {
		return tiles[row][col].getStatus() == TileStatus.TARGET; 
	}
	
	public boolean isTower(int row, int col) {
		if (isTarget(row, col)) {
			Position tempP = new Position(row, col);
			for (Block b: blocks) {
				if (tempP.equals(b.getPosition())) {
					if (b.getNextBlock() != null) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	// only returns the block if it is clear
	public Block getBlock(int row, int col) {
		if (isTarget(row, col)) {
			Position tempP = new Position(row, col);
			for (Block b: blocks) {
				if (tempP.equals(b.getPosition()) && b.getNextBlock() == null) {
					return b;
				}
			}
		}
		return null;
	}
	
	// returns the top block at given position
	public Block getTopBlock(int row, int col) {
		if (isTarget(row, col)) {
			Position tempP = new Position(row, col);
			for (Block b: blocks) {
				if (tempP.equals(b.getPosition())) {
					Block priorBlock = null;
					Block currentBlock = b;
					while (currentBlock.getNextBlock() != null) {
						priorBlock = currentBlock;
						currentBlock = currentBlock.getNextBlock();
					}
					priorBlock.add(null);
					return currentBlock;
				}
			}
		}
		return null;
	}
	
	public void removeTarget(int row, int col) {
		if (isTarget(row, col)) {
			cleanTile(row, col);
		}
	}
	
	public void addTarget(Robot r) {
		int robotPosRow = r.getPosRow();
		int robotPosCol = r.getPosCol();
		Block b = r.getBlock();
		if (!isTarget(robotPosRow, robotPosCol)) {
			tiles[robotPosRow][robotPosCol] = new Tile(TileStatus.TARGET); // TODO: use setter?
			b.setPosition(new Position(robotPosRow, robotPosCol));

		} else {
			Position tempP = new Position(r.getPosRow(), r.getPosCol());
			for (Block bb: blocks) {
				if (tempP.equals(bb.getPosition())) {
					Block currentBlock = bb;
					while (currentBlock.getNextBlock() != null) {
						currentBlock = currentBlock.getNextBlock();
					}
					currentBlock.add(r.getBlock());
				}
			}
			b.setPosition(tempP);
		}
	}

}