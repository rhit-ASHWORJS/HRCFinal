import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
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
	private Block b;
	
	private LinkedList<Action> path;
	private boolean pathFound;
	private long openCount;
	private int pathLength;
	private LinkedList<Position> targets;
	
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
	
	public void astar() {
		astarM();
	}
	
	public void astarM() {
		PriorityQueue<LinkedList<Position>> paths = new PriorityQueue<LinkedList<Position>>(new comparatorM());
		
		LinkedList<Position> working = new LinkedList<Position>();
		HashMap<String,ArrayList<Position>> visited = new HashMap<String,ArrayList<Position>>();
		working.add(new Position(posRow, posCol));
		paths.add(working);
		addToMap(working.get(0), targetsVisitedString(working), visited);
		openCount++;
		
		LinkedList<Position> solution = null;
		if(allTargetsVisited(working))
		{
			solution = working;
		}
		
		while(solution == null)
		{
			if(paths.size()==0)
			{
				System.out.println("I can't do this mate, I checked " + openCount + " routes and they're all bad.");
				this.pathFound = false;
				return;
			}
			working=paths.poll();
			if(allTargetsVisited(working))
			{
//				System.out.println("AAAAAAAAAAAAAAA");
				solution = working;
				break;
			}
			String tvs = targetsVisitedString(working);
//			System.out.println(working.size());
			LinkedList<Position> newLocations = getAdjacent(working.get(working.size()-1));
//			System.out.println(newLocations.size());
			for(Position p : newLocations)
			{
				visited.containsKey(tvs);//update visited
				if(!(visited.containsKey(tvs)))
				{
					visited.put(tvs, new ArrayList<Position>());
				}
				if(positionVisited(visited.get(tvs), p))//avoid loops
				{
					continue;
				}
				LinkedList<Position> newPath = addToPath(working, p);
				addToMap(p, tvs, visited);
				paths.add(newPath);
				openCount++;
			}
		}
		
		this.path = pathToRoute(solution);
		this.pathLength = this.path.size();
		this.pathFound = true;
	}
	
	private String targetsVisitedString(LinkedList<Position> path)
	{
		String tvs = "";
		for(int i=0; i<targets.size(); i++)
		{
			if(pathContainsPosition(path, targets.get(i)))
			{
				tvs = tvs + i + ",";
			}
		}
		return tvs;
	}
	
	private boolean allTargetsVisited(LinkedList<Position> path)
	{
		for(Position t : targets)
		{
			if(!pathContainsPosition(path, t))
			{
				return false;
			}
		}
		return true;
	}
	
	private LinkedList<Position> addToPath(LinkedList<Position> path, Position p)
	{
		LinkedList<Position> newPath = new LinkedList<Position>();
		for(Position p2:path)
		{
			newPath.add(p2);
		}
		newPath.add(p);
		return newPath;
	}
	
	private void addToMap(Position p, String s, HashMap<String, ArrayList<Position>> map)
	{
		if(map.containsKey(s))
		{
			map.get(s).add(p);
		}
		else
		{
			map.put(s, new ArrayList<Position>());
			map.get(s).add(p);
		}
	}
	
	private LinkedList<Position> getAdjacent(Position p) {
		LinkedList<Position> near = new LinkedList<Position>();
		
		for(int ymod=-1; ymod<=1; ymod+=2)
		{
			int newx=p.getRow();
			int newy=p.getCol()+ymod;
			
//			System.out.println("Checking: " + newx + "," + newy);
			
			if(env.getTileStatus(newx, newy) != TileStatus.IMPASSABLE)
			{
				near.add(new Position(newx, newy));
			}
		}
		for(int xmod=-1; xmod<=1; xmod+=2)
		{
			int newx=p.getRow()+xmod;
			int newy=p.getCol();
			
//			System.out.println("Checking: " + newx + "," + newy);
			
			if(env.getTileStatus(newx, newy) != TileStatus.IMPASSABLE)
			{
				near.add(new Position(newx, newy));
			}
		}
		
		return near;
	}
	
	private boolean positionVisited(ArrayList<Position> visited, Position p)
	{
		if(visited == null)
		{
			return false;
		}
		for(Position p2:visited)
		{
			if(p2.getRow()==p.getRow() && p2.getCol()==p.getCol())
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean pathContainsPosition(LinkedList<Position> path, Position p)
	{
		for(Position p2:path)
		{
			if(p2.getRow()==p.getRow() && p2.getCol()==p.getCol())
			{
				return true;
			}
		}
		return false;
	}
	
	private LinkedList<Action> pathToRoute(LinkedList<Position> path)
	{
		LinkedList<Action> route = new LinkedList<Action>();
		for(int i=0; i<path.size()-1; i++)
		{
			Position current = path.get(i);
			Position next = path.get(i+1);
			
			if(current.getRow()<next.getRow())
			{
				route.add(Action.MOVE_DOWN);
			}
			else if(current.getRow()>next.getRow())
			{
				route.add(Action.MOVE_UP);
			}
			else if(current.getCol()<next.getCol())
			{
				route.add(Action.MOVE_RIGHT);
			}
			else if(current.getCol()>next.getCol())
			{
				route.add(Action.MOVE_LEFT);
			}
			else
			{
				route.add(Action.DO_NOTHING);
			}
		}
		return route;
	}

	public class comparatorM implements Comparator<LinkedList<Position>> {

		@Override
		public int compare(LinkedList<Position> o1, LinkedList<Position> o2) {
			if(o1.size() + getHeuristicM(o1) > o2.size() + getHeuristicM(o2))
			{
				return 1;
			}
			else if(o1.size() + getHeuristicM(o1) == o2.size() + getHeuristicM(o2))
			{
				return 0;
			}
			else
			{
				return -1;
			}
		}
		
	}
	
	boolean mapM6FunnyBusiness = false;
	int bestNumReached = 0;
	private double getHeuristicM(LinkedList<Position> path)
	{
		double heu = 0;//path.size();
		//add remaning distance to goal
		ArrayList<Position> notVisited = new ArrayList<Position>();
		for(Position p : targets)
		{
			if(!pathContainsPosition(path, p))
			{
				notVisited.add(p);
			}
		}
		
//		for(Position p : path)
		int numGoalsReached = targets.size() - notVisited.size();
		if(mapM6FunnyBusiness)
		{
			if(numGoalsReached > bestNumReached)
			{
				bestNumReached = numGoalsReached;
			}
			if(bestNumReached - numGoalsReached > 1)
			{
				return 999999;
			}
		}

		Position comingFrom = path.get(path.size()-1);
		while(notVisited.size() > 0)
		{
			Position closest = getClosestPosition(comingFrom, notVisited);
			notVisited.remove(closest);
			heu += distanceBetweenPositions(comingFrom, closest);
			comingFrom = closest;
		}
		

		if(heu < 0)
		{
			heu = 0;
		}
		
		
		return heu;
	}
	
	private Position getClosestPosition(Position src, ArrayList<Position> dests)
	{
		Position closest = dests.get(0);
		double dist = distanceBetweenPositions(src, closest);
		for(Position p : dests)
		{
			if(distanceBetweenPositions(p, src) < dist)
			{
				closest = p;
				dist = distanceBetweenPositions(p, src);
			}
		}
		return closest;
	}
	
	private double distanceBetweenPositions(Position p1, Position p2)
	{
		int dist = 0;
//		return Math.sqrt(Math.pow(p1.getCol()-p2.getCol(), 2)+Math.pow(p1.getRow()-p2.getRow(), 2));
		int startx = p1.getRow();
		int starty = p1.getCol();
		int endx = p2.getRow();
		int endy = p2.getCol();
		
		int xadj=0;
		int yadj=0;
		if(startx>endx)
		{
			xadj=-1;
		}
		if(startx<endx)
		{
			xadj=1;
		}
		if(starty>endy)
		{
			yadj=-1;
		}
		if(starty<endy)
		{
			yadj=1;
		}
		
		for(int x=startx; x!=endx; x+=xadj)
		{
			dist++;
		}
		for(int y=starty; y!=endy; y+=yadj)
		{
			dist++;
		}
		
		
//		return Math.abs(p1.getCol()-p2.getCol())+Math.abs(p1.getRow()-p2.getRow());
		return dist;
	}
}