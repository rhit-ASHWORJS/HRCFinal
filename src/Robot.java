import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;

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
	
	private boolean pathFound;
	private long openCount;
	private int pathLength;
	
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
	
	private LinkedList<Action> planToActions(LinkedList<Rule> plan)
	{
		LinkedList<Action> actions = new LinkedList<>();
		
		for(int i=0; i<plan.size(); i++)
		{
			LinkedList<Position> targets = new LinkedList<>();
			
			Rule thisRule = plan.get(i);
			if(thisRule instanceof PickUp)
			{
				Block b = env.getBlock(Integer.parseInt(((PickUp) thisRule).block));
				targets.add(b.getPosition());
				LinkedList<Action> path = astar(targets);
				
				for(Action a : path)
				{
					actions.add(a);
				}
				actions.add(Action.PICK_UP);
			}
			else if(thisRule instanceof UnStackIt)
			{
				Block b = env.getBlock(Integer.parseInt(((UnStackIt) thisRule).block));
				targets.add(b.getPosition());
				LinkedList<Action> path = astar(targets);
				
				for(Action a : path)
				{
					actions.add(a);
				}
				actions.add(Action.UNSTACK);
			}
			else if(thisRule instanceof StackIt)
			{
				Block b = env.getBlock(Integer.parseInt(((StackIt) thisRule).target));
				targets.add(b.getPosition());
				LinkedList<Action> path = astar(targets);
				
				for(Action a : path)
				{
					actions.add(a);
				}
				actions.add(Action.STACK);
			}
			else if(thisRule instanceof PutDown)
			{
				Position robotPos = new Position(posRow, posCol);
				Position emptyPos = getNearestEmptyPos(robotPos);
				if(emptyPos==null)
				{
					System.out.println("Entire board is full, aborting");
					return actions;
				}
				
				targets.add(emptyPos);
				LinkedList<Action> path = astar(targets);
				
				for(Action a : path)
				{
					actions.add(a);
				}
				actions.add(Action.PUT_DOWN);
			}
			else
			{
				System.out.print("Weird rule found");
			}
		}
		
		return actions;
	}
	
	private Position getNearestEmptyPos(Position origin)
	{
		ArrayList<Position> seenPositions = new ArrayList<Position>();
		Queue<Position> positionsToSee = new LinkedList<Position>();
		positionsToSee.add(origin);
		
		while(!positionsToSee.isEmpty())
		{
			Position current = positionsToSee.poll();
			
			if(env.getTileStatus(current.getRow(), current.getCol()) == TileStatus.CLEAN)
			{
				return current;
			}
			
			seenPositions.add(current);
			
			LinkedList<Position> adj = getAdjacent(current);
			for(Position p : adj)
			{
				if(!seenPositions.contains(p))
				{
					positionsToSee.add(p);
				}
			}
		}
		
		return null;
	}
	
	private static void STRIPS(LinkedList<Predicate> state, LinkedList<Predicate> goals, LinkedList<Rule> plan) {
		Stack<Object> goalStack = new Stack<>();
		for (Predicate p : goals) {
			goalStack.push(p);
		}
		while (!goalStack.isEmpty()) {

//			// Prints intermediate states for debugging purposes
//			System.out.println("\nIntermediate State:");
//			for (Predicate p : state) {
//				System.out.println(p);
//			}
//			System.out.println("\n");

			// This check prevents either the goalStack or the plan from going in circles.
			if (goalStack.size() > 100 | plan.size() > 100) {
				plan.clear();
				System.out.println("The Planner Is Stuck In A Loop");
				return;
			}
			Object p = goalStack.pop();
			if ((p instanceof Predicate) && myContains(state, ((Predicate) p))) {
				continue;
			}
			if (p instanceof Rule) {
				updateStateAndPlan((Rule) p, state, plan, goals, goalStack);
				continue;
			}
			if (p instanceof Predicate) {
				chooseAction((Predicate) p, state, goalStack);
				continue;
			}
		}
		// Tries the reverse order of goals if a goal has been undone.
		for (Predicate goal : goals) {
			if (!myContains(state, goal)) {
				LinkedList<Predicate> newGoals = new LinkedList<Predicate>();
				newGoals.add(goal);
				newGoals.addLast(goals.getFirst());
				System.out.println("A goal is missing in the initial plan, retrying...");
				STRIPS(state, newGoals, plan);
				break;
			}
		}
		System.out.println("All goals have been completed");
		removeCircularLogic(plan);
	}

	/*
	 * Removes any circular laogic (recursively)
	 */
	private static void removeCircularLogic(LinkedList<Rule> plan) {
		ArrayList<Rule> actions = new ArrayList<Rule>();
		actions.addAll(plan);
		if (actions.size() < 2) {
			return;
		}
		LinkedList<Rule> toRemove = new LinkedList<Rule>();
		for (int i = 0; i < actions.size() - 1; i++) {
			Rule current = actions.get(i);
			Rule next = actions.get(i + 1);
			if (current instanceof PickUp && next instanceof PutDown) {
				if (((PickUp) current).block.equals(((PutDown) next).block)) {
					toRemove.add(current);
					toRemove.add(next);
					break;
				}
			} else if (current instanceof PutDown && next instanceof PickUp) {
				if (((PutDown) current).block.equals(((PickUp) next).block)) {
					toRemove.add(current);
					toRemove.add(next);
					break;
				}
			} else if (current instanceof StackIt && next instanceof UnStackIt) {
				if (((StackIt) current).block.equals(((UnStackIt) next).block)
						&& ((StackIt) current).target.equals(((UnStackIt) next).target)) {
					toRemove.add(current);
					toRemove.add(next);
					break;
				}
			} else if (current instanceof UnStackIt && next instanceof StackIt) {
				if (((UnStackIt) current).block.equals(((StackIt) next).block)
						&& ((UnStackIt) current).target.equals(((StackIt) next).target)) {
					toRemove.add(current);
					toRemove.add(next);
					break;
				}
			}
		}
		if (!toRemove.isEmpty()) {
			for (Rule act : toRemove) {
				plan.remove(act);
			}
			removeCircularLogic(plan);
		}
	}

	private static void chooseAction(Predicate p, LinkedList<Predicate> state, Stack<Object> goalStack) {
		if (p instanceof Clear) {
			String block = ((Clear) p).block;
			String toPickUp = null;
			for (Predicate item : state) {
				if (item instanceof On) {
					if (((On) item).bottom.equals(block)) {
						toPickUp = ((On) item).top;
					}
				}
			}
			goalStack.push(new UnStackIt(toPickUp, block));
			goalStack.push(new Handempty());
			goalStack.push(new Clear(toPickUp));
		} else if (p instanceof Handempty) {
			String toDrop = null;
			for (Predicate item : state) {
				if (item instanceof Holding) {
					toDrop = ((Holding) item).block;
				}
			}
			goalStack.push(new PutDown(toDrop));
		} else if (p instanceof Holding) {
			String block = ((Holding) p).block;
			for (Predicate item : state) {
				// Checks to see if the desired block to be held is on the table
				if (item instanceof OnTable) {
					if (((OnTable) item).block.equals(block)) {
						goalStack.push(new PickUp(block));
						goalStack.push(new Handempty());
						goalStack.push(new Clear(block));
					}
				}
				// Checks to see if the desired block to be held is on another block
				else if (item instanceof On) {
					if (((On) item).top.equals(block)) {
						goalStack.push(new UnStackIt(block, ((On) item).bottom));
						goalStack.push(new Handempty());
						goalStack.push(new Clear(block));
					}
				}
			}
		} else if (p instanceof On) {
			String block = ((On) p).top;
			String target = ((On) p).bottom;
			goalStack.push(new StackIt(block, target));
			goalStack.push(new Holding(block));
			goalStack.push(new Clear(target));
		} else if (p instanceof OnTable) {
			String block = ((OnTable) p).block;
			goalStack.push(new PutDown(block));
			goalStack.push(new Holding(block));
		}
	}

	private static void updateStateAndPlan(Rule p, LinkedList<Predicate> state, LinkedList<Rule> plan,
			LinkedList<Predicate> goals, Stack<Object> goalStack) {
		if (p instanceof PickUp) {
			handlePickup((PickUp) p, state, plan, goals, goalStack);
		} else if (p instanceof PutDown) {
			handlePutdown((PutDown) p, state, plan, goals, goalStack);
		} else if (p instanceof StackIt) {
			handleStack((StackIt) p, state, plan, goals, goalStack);
		} else {
			handleUnstack((UnStackIt) p, state, plan, goals, goalStack);
		}
		plan.add(p);
	}

	private static void handlePickup(PickUp p, LinkedList<Predicate> state, LinkedList<Rule> plan,
			LinkedList<Predicate> goals, Stack<Object> goalStack) {
		String block = p.block;
		LinkedList<Predicate> toRemove = new LinkedList<Predicate>();
		LinkedList<Predicate> toAdd = new LinkedList<Predicate>();
		for (Predicate item : state) {
			// Handles clearing the on table state
			if (item instanceof OnTable) {
				OnTable usefulitem = (OnTable) item;
				if (usefulitem.block == block) {
					toRemove.add(item);
				}
			}
			// Handles clearing the clear state for this block
			else if (item instanceof Clear) {
				Clear usefulitem = (Clear) item;
				if (usefulitem.block.equals(block)) {
					toRemove.add(item);
				}
			}
			// Handles clearing the hand empty state
			else if (item instanceof Handempty) {
				toRemove.add(item);
			}
		}
		// Adds the state of holding the new block
		toAdd.add(new Holding(block));
		for (Predicate deletion : toRemove) {
			state.remove(deletion);
		}
		for (Predicate addition : toAdd) {
			state.add(addition);
		}
	}

	private static void handlePutdown(PutDown p, LinkedList<Predicate> state, LinkedList<Rule> plan,
			LinkedList<Predicate> goals, Stack<Object> goalStack) {
		String block = p.block;
		LinkedList<Predicate> toRemove = new LinkedList<Predicate>();
		LinkedList<Predicate> toAdd = new LinkedList<Predicate>();
		for (Predicate item : state) {
			// Handles clearing the holding state
			if (item instanceof Holding) {
				toRemove.add(item);
			}
		}
		// Adds the state of holding the new block
		toAdd.add(new OnTable(block));
		// Makes the newly placed block clear
		toAdd.add(new Clear(block));
		// Adds the empty hand state
		toAdd.add(new Handempty());
		for (Predicate deletion : toRemove) {
			state.remove(deletion);
		}
		for (Predicate addition : toAdd) {
			state.add(addition);
		}
	}

	private static void handleStack(StackIt p, LinkedList<Predicate> state, LinkedList<Rule> plan,
			LinkedList<Predicate> goals, Stack<Object> goalStack) {
		String block = p.block;
		String target = p.target;
		LinkedList<Predicate> toRemove = new LinkedList<Predicate>();
		LinkedList<Predicate> toAdd = new LinkedList<Predicate>();
		for (Predicate item : state) {
			// Handles clearing the holding state
			if (item instanceof Holding) {
				toRemove.add(item);
			}
			// Handles making the target block unclear
			else if (item instanceof Clear) {
				Clear usefulitem = (Clear) item;
				if (usefulitem.block.equals(target)) {
					toRemove.add(item);
				}
			}
		}
		// Adds the state of an empty hand
		toAdd.add(new Handempty());
		// Adds the new block on target state.
		toAdd.add(new On(block, target));
		// Makes the block clear
		toAdd.add(new Clear(block));
		for (Predicate deletion : toRemove) {
			state.remove(deletion);
		}
		for (Predicate addition : toAdd) {
			state.add(addition);
		}
	}

	private static void handleUnstack(UnStackIt p, LinkedList<Predicate> state, LinkedList<Rule> plan,
			LinkedList<Predicate> goals, Stack<Object> goalStack) {
		String block = p.block;
		String target = p.target;
		LinkedList<Predicate> toRemove = new LinkedList<Predicate>();
		LinkedList<Predicate> toAdd = new LinkedList<Predicate>();
		for (Predicate item : state) {
			// Handles clearing the empty hand state
			if (item instanceof Handempty) {
				toRemove.add(item);
			}
			// Handles making this block unclear
			else if (item instanceof Clear) {
				Clear usefulitem = (Clear) item;
				if (usefulitem.block.equals(block)) {
					toRemove.add(item);
				}
			}
			// Handles removal of the state with this block on the target
			else if (item instanceof On) {
				On usefulitem = (On) item;
				if (usefulitem.top.equals(p.block)) {
					toRemove.add(item);
				}
			}
		}
		// Adds the state of holding the new block
		toAdd.add(new Holding(block));
		// Adds a clear state for the target
		toAdd.add(new Clear(target));
		for (Predicate deletion : toRemove) {
			state.remove(deletion);
		}
		for (Predicate addition : toAdd) {
			state.add(addition);
		}
	}

	private static boolean myContains(LinkedList<Predicate> state, Predicate predicate) {
		for (Predicate pred : state) {
			if (predicate instanceof Clear && pred instanceof Clear) {
				if (((Clear) predicate).equals((Clear) pred)) {
					return true;
				}
			} else if (predicate instanceof Handempty && pred instanceof Handempty) {
				return true;
			} else if (predicate instanceof Holding && pred instanceof Holding) {
				if (((Holding) predicate).equals((Holding) pred)) {
					return true;
				}
			} else if (predicate instanceof On && pred instanceof On) {
				if (((On) predicate).equals((On) pred)) {
					return true;
				}
			} else if (predicate instanceof OnTable && pred instanceof OnTable) {
				if (((OnTable) predicate).equals((OnTable) pred)) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	public LinkedList<Action> astar(LinkedList<Position> targets) {
		return astarM(targets);
	}
	
	public LinkedList<Action> astarM(LinkedList<Position> targets) {
		PriorityQueue<LinkedList<Position>> paths = new PriorityQueue<LinkedList<Position>>(new comparatorM());
		targetx = targets.get(0).getRow();
		targety = targets.get(0).getCol();
		
		LinkedList<Position> working = new LinkedList<Position>();
		HashMap<String,ArrayList<Position>> visited = new HashMap<String,ArrayList<Position>>();
		working.add(new Position(posRow, posCol));
		paths.add(working);
		addToMap(working.get(0), targetsVisitedString(working, targets), visited);
		openCount++;
		
		LinkedList<Position> solution = null;
		if(allTargetsVisited(working, targets))
		{
			solution = working;
		}
		
		while(solution == null)
		{
			if(paths.size()==0)
			{
				System.out.println("I can't do this mate, I checked " + openCount + " routes and they're all bad.");
				this.pathFound = false;
				return null;
			}
			working=paths.poll();
			if(allTargetsVisited(working, targets))
			{
//				System.out.println("AAAAAAAAAAAAAAA");
				solution = working;
				break;
			}
			String tvs = targetsVisitedString(working, targets);
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
		
		return pathToRoute(solution);

	}
	
	private String targetsVisitedString(LinkedList<Position> path, LinkedList<Position> targets)
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
	
	private boolean allTargetsVisited(LinkedList<Position> path, LinkedList<Position> targets)
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
			if(o1.size() + getHeuristic(o1) > o2.size() + getHeuristic(o2))
			{
				return 1;
			}
			else if(o1.size() + getHeuristic(o1) == o2.size() + getHeuristic(o2))
			{
				return 0;
			}
			else
			{
				return -1;
			}
		}
		
	}

	int targetx = 0;
	int targety = 0;
	private double getHeuristic(LinkedList<Position> path)
	{
		double heu = path.size();
		//add remaning distance to goal
		int goalx = targetx;
		int goaly = targety;
		int lastx=path.get(path.size()-1).getRow();
		int lasty=path.get(path.size()-1).getCol();
		
		heu += Math.sqrt(Math.pow(lastx-goalx, 2)+Math.pow(lasty-goaly, 2));
		System.out.println( heu);
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