import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;

/**
 * Represents an intelligent agent moving through a particular room. The robot
 * only has one sensor - the ability to get the status of any tile in the
 * environment through the command env.getTileStatus(row, col).
 * 
 * @author Adam Gaweda, Michael Wollowski
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
	Queue<Action> actionQueue = new LinkedList<Action>();
	Queue<Action> lateActionQueue = new LinkedList<Action>();
	Queue<Rule> ruleQueue = new LinkedList<Rule>();

	/**
	 * Initializes a Robot on a specific tile in the environment.
	 */

	public Robot(Environment env, int posRow, int posCol) {
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
		} else if (b == null) {
			this.b = null;
			return true;
		} else {
			return false;
		}
	}

	public boolean getPathFound() {
		return pathFound;
	}

	public int getPathLength() {
		// TODO: modify this procedure to return the actual path length.
		// You will likely have to track it in some counter.
		return 0;
	}

	public int getPosRow() {
		return posRow;
	}

	public int getPosCol() {
		return posCol;
	}

	public void incPosRow() {
		posRow++;
	}

	public void decPosRow() {
		posRow--;
	}

	public void incPosCol() {
		posCol++;
	}

	public void decPosCol() {
		posCol--;
	}

	/**
	 * Returns the next action to be taken by the robot. A support function that
	 * processes the path LinkedList that has been populates by the search
	 * functions.
	 */
	
	public Action getAction() {
		if(!actionQueue.isEmpty())
		{
			Action toDo = actionQueue.poll();
			if(!isActionValid(toDo))
			{
				System.out.println("I'm sorry, I can't " + toDo.toString() + " here");
				return Action.DO_NOTHING;
			}
			return toDo;
		}
		if(!ruleQueue.isEmpty())
		{
			LinkedList<Rule> miniPlan = new LinkedList<Rule>();
			miniPlan.add(ruleQueue.poll());
			LinkedList<Action> actions = planToActions(miniPlan);
			for(Action a : actions)
			{
				actionQueue.add(a);
			}
			//---------------------------------------------------
			Action toDo = actionQueue.poll();
			if(!isActionValid(toDo))
			{
				System.out.println("I'm sorry, I can't " + toDo.toString() + " here");
				return Action.DO_NOTHING;
			}
			return toDo;
		}
		if(!lateActionQueue.isEmpty())
		{
			Action toDo = lateActionQueue.poll();
			if(!isActionValid(toDo))
			{
				System.out.println("I'm sorry, I can't " + toDo.toString() + " here");
				return Action.DO_NOTHING;
			}
			return toDo;
		}
		System.out.print("> ");
		Scanner sc = new Scanner(System.in);
		String name = sc.nextLine();
		Action toDo = null;
		
		if (name.equals("u")) {
			toDo = Action.MOVE_UP;
		}
		else if (name.equals("d")) {
			toDo = Action.MOVE_DOWN;
		}
		else if (name.equals("l")) {
			toDo = Action.MOVE_LEFT;
		}
		else if (name.equals("r")) {
			toDo = Action.MOVE_RIGHT;
		}
		else if (name.equals("us") || name.contains("unstack")) {
			toDo = Action.UNSTACK;
			int indexOfParam = -1;
			if(name.equals("us"))
			{
				indexOfParam = name.indexOf("us") + 3;
			}
			else
			{
				indexOfParam = name.indexOf("unstack") + 8;
			}
			
			if(indexOfParam < name.length())
			{
				Position blockPos = null;
				Block blockToGrab = null;
				if(name.charAt(indexOfParam) == '<')
				{
					String ss = name.substring(indexOfParam);
					
					int posComma = ss.indexOf(',');
					int posEnd = ss.indexOf('>');
					
					String row = ss.substring(1, posComma);
					String col = ss.substring(posComma+1, posEnd);
					
					blockPos = new Position(Integer.parseInt(row), Integer.parseInt(col));
					blockToGrab = env.getBlock(Integer.parseInt(row), Integer.parseInt(col));
				}
				else if(Character.isDigit(name.charAt(indexOfParam)))
				{
					String ss = name.substring(indexOfParam);
					int lastNumeric = 0;
					for(int i=0; i<ss.length(); i++)
					{
						if(Character.isDigit(ss.charAt(i)))
						{
							lastNumeric = i;
						}
						else
						{
							break;
						}
					}
					String id = ss.substring(0, lastNumeric+1);
					blockPos = env.getBlock(Integer.parseInt(id)).getPosition();
					blockToGrab = env.getBlock(Integer.parseInt(id));
				}
				
				if(blockToGrab == null)
				{
					System.out.println("Could not find the specified block");
				}
				
				if(blockPos != null)
				{
					LinkedList<Predicate> goal = new LinkedList<>();
					goal.add(new Holding(blockToGrab.getID()+""));
					
					LinkedList<Rule> plan = new LinkedList<Rule>();
					STRIPS(env.getState(), goal, plan);
					
					for(Rule r : plan)
					{
						ruleQueue.add(r);
					}
					toDo = Action.DO_NOTHING;
				}
			}
		}
		else if (name.equals("s") || name.contains("stack")) {
			toDo = Action.STACK;
			int indexOfParam = -1;
			
			if(b == null)
			{
				System.out.println("I can't stack, I'm not holding anything to stack");
				return Action.DO_NOTHING;
			}
			
			if(name.equals("s"))
			{
				indexOfParam = name.indexOf("s") + 2;
			}
			else
			{
				indexOfParam = name.indexOf("stack") + 6;
			}
			
			if(indexOfParam < name.length())
			{
				Position blockPos = null;
				Block block = null;
				if(name.charAt(indexOfParam) == '<')
				{
					String ss = name.substring(indexOfParam);
					
					int posComma = ss.indexOf(',');
					int posEnd = ss.indexOf('>');
					
					String row = ss.substring(1, posComma);
					String col = ss.substring(posComma+1, posEnd);
					
					blockPos = new Position(Integer.parseInt(row), Integer.parseInt(col));
					block = env.getBlock(Integer.parseInt(row), Integer.parseInt(col));
				}
				else if(Character.isDigit(name.charAt(indexOfParam)))
				{
					String ss = name.substring(indexOfParam);
					int lastNumeric = 0;
					for(int i=0; i<ss.length(); i++)
					{
						if(Character.isDigit(ss.charAt(i)))
						{
							lastNumeric = i;
						}
						else
						{
							break;
						}
					}
					String id = ss.substring(0, lastNumeric+1);
					blockPos = env.getBlock(Integer.parseInt(id)).getPosition();
					block = env.getBlock(Integer.parseInt(id));
				}
				
				if(block == null)
				{
					System.out.println("Could not find the specified block");
				}
				
				if(blockPos != null)
				{
					LinkedList<Predicate> goal = new LinkedList<>();
					
					if(block.getNextBlock() != null)
					{
						System.out.println("Sorry, but block " + block.getID() + " already has something on it.");
						return Action.DO_NOTHING;
					}
					
					goal.add(new On(b.getID()+"", block.getID()+""));
					
					LinkedList<Rule> plan = new LinkedList<Rule>();
					STRIPS(env.getState(), goal, plan);
					
					for(Rule r : plan)
					{
						ruleQueue.add(r);
					}
					toDo = Action.DO_NOTHING;
				}
			}
		}
		else if (name.equals("pd") || name.contains("put down") || name.contains("putdown") || name.contains("put-down")) {
			toDo = Action.PUT_DOWN;
			int indexOfParam = -1;
			
			if(name.equals("pd"))
			{
				indexOfParam = name.indexOf("pd") + 3;
			}
			else if(name.contains("put down"))
			{
				indexOfParam = name.indexOf("put down") + 9;
			}
			else if(name.contains("putdown"))
			{
				indexOfParam = name.indexOf("putdown") + 8;
			}
			else if(name.contains("put-down"))
			{
				indexOfParam = name.indexOf("put-down") + 9;
			}
			
			if(indexOfParam < name.length())
			{
				Position blockPos = null;
				if(name.charAt(indexOfParam) == '<')
				{
					String ss = name.substring(indexOfParam);
					
					int posComma = ss.indexOf(',');
					int posEnd = ss.indexOf('>');
					
					String row = ss.substring(1, posComma);
					String col = ss.substring(posComma+1, posEnd);
					
					blockPos = new Position(Integer.parseInt(row), Integer.parseInt(col));
				}
				
				if(blockPos != null)
				{
					LinkedList<Predicate> goal = new LinkedList<>();
					
					LinkedList<Action> path = astar(blockPos);
					
					for(Action a : path)
					{
						actionQueue.add(a);
					}
					actionQueue.add(toDo);
					toDo = Action.DO_NOTHING;
				}
			}
		}
		else if (name.equals("pu") || name.contains("pick up") || name.contains("pickup") || name.contains("pick-up")) {
			toDo = Action.PICK_UP;
			int indexOfParam = -1;
			
			if(name.equals("pu"))
			{
				indexOfParam = name.indexOf("pu") + 3;
			}
			else if(name.contains("pick up"))
			{
				indexOfParam = name.indexOf("pick up") + 8;
			}
			else if(name.contains("pickup"))
			{
				indexOfParam = name.indexOf("pickup") + 7;
			}
			else if(name.contains("pick-up"))
			{
				indexOfParam = name.indexOf("pick-up") + 8;
			}
			
			if(indexOfParam < name.length())
			{
				Position blockPos = null;
				Block block = null;
				if(name.charAt(indexOfParam) == '<')
				{
					String ss = name.substring(indexOfParam);
					
					int posComma = ss.indexOf(',');
					int posEnd = ss.indexOf('>');
					
					String row = ss.substring(1, posComma);
					String col = ss.substring(posComma+1, posEnd);
					
					blockPos = new Position(Integer.parseInt(row), Integer.parseInt(col));
					block = env.getBlock(Integer.parseInt(row), Integer.parseInt(col));
				}
				else if(Character.isDigit(name.charAt(indexOfParam)))
				{
					String ss = name.substring(indexOfParam);
					int lastNumeric = 0;
					for(int i=0; i<ss.length(); i++)
					{
						if(Character.isDigit(ss.charAt(i)))
						{
							lastNumeric = i;
						}
						else
						{
							break;
						}
					}
					String id = ss.substring(0, lastNumeric+1);
					blockPos = env.getBlock(Integer.parseInt(id)).getPosition();
					block = env.getBlock(Integer.parseInt(id));
				}
				
				if(block == null)
				{
					System.out.println("Could not find the specified block");
				}
				
				if(blockPos != null)
				{
					boolean onTable = false;
					for(Block other : env.getBlocks())
					{
						if(other.getID() == block.getID())
						{
							onTable = true;
						}
					}
					if(!onTable)
					{
						System.out.println("I cannot pick up a block that is not on table.  Try 'unstack'");
						return Action.DO_NOTHING;
					}
					
					LinkedList<Predicate> goal = new LinkedList<>();
					
					goal.add(new Holding(block.getID()+""));
					
					LinkedList<Rule> plan = new LinkedList<Rule>();
					STRIPS(env.getState(), goal, plan);
					
					for(Rule r : plan)
					{
						ruleQueue.add(r);
					}
					toDo = Action.DO_NOTHING;
				}
			}
		}
		else if(name.contains("bring"))
		{
			try {
				int indexOfParam = name.indexOf("bring") + 6;
				Position blockPos = null;
				Block block = null;
				if(name.charAt(indexOfParam) == '<')
				{
					String ss = name.substring(indexOfParam);
					
					int posComma = ss.indexOf(',');
					int posEnd = ss.indexOf('>');
					
					String row = ss.substring(1, posComma);
					String col = ss.substring(posComma+1, posEnd);
					
					blockPos = new Position(Integer.parseInt(row), Integer.parseInt(col));
					block = env.getTopBlockActually(Integer.parseInt(row), Integer.parseInt(col));
				}
				else if(Character.isDigit(name.charAt(indexOfParam)))
				{
					String ss = name.substring(indexOfParam);
					int lastNumeric = 0;
					for(int i=0; i<ss.length(); i++)
					{
						if(Character.isDigit(ss.charAt(i)))
						{
							lastNumeric = i;
						}
						else
						{
							break;
						}
					}
					String id = ss.substring(0, lastNumeric+1);
					blockPos = env.getBlock(Integer.parseInt(id)).getPosition();
					block = env.getBlock(Integer.parseInt(id));
				}
				
				Block destBlock = null;
				Position destPos = null;
				
				int indexOfTo = name.indexOf("to")+3;
				String ss = name.substring(indexOfTo);
				
				int posComma = ss.indexOf(',');
				int posEnd = ss.indexOf('>');
				
				String row = ss.substring(1, posComma);
				String col = ss.substring(posComma+1, posEnd);
				
				destPos = new Position(Integer.parseInt(row), Integer.parseInt(col));
				destBlock = env.getTopBlockActually(Integer.parseInt(row), Integer.parseInt(col));
				System.out.println(destBlock);
				
				if(block.getPosition().equals(destPos))
				{
					System.out.println("The block's already there!");
					return Action.DO_NOTHING;
				}
				
				if(destBlock == null)
				{
					LinkedList<Predicate> want = new LinkedList<Predicate>();
					LinkedList<Rule> plan = new LinkedList<>();
					want.add(new Holding(block.getID()+""));
					STRIPS(env.getState(), want, plan);
					
					for(Rule r : plan)
					{
						ruleQueue.add(r);
					}
					
					int tempx = posRow;
					int tempy = posCol;
					
					posRow = block.getPosition().getRow();
					posCol = block.getPosition().getCol();
					
					LinkedList<Action> path = astar(destPos);
					
					for(Action a : path)
					{
						lateActionQueue.add(a);
					}
					lateActionQueue.add(Action.PUT_DOWN);
					
					posRow = tempx;
					posCol = tempy;
					toDo = Action.DO_NOTHING;
				}
				else
				{
					LinkedList<Predicate> want = new LinkedList<Predicate>();
					LinkedList<Rule> plan = new LinkedList<>();
//					while(block.getNextBlock() != null)
//					{
//						block = block.getNextBlock();
//					}
					want.add(new On(block.getID()+"", destBlock.getID()+""));
					STRIPS(env.getState(), want, plan);
					
					for(Rule r : plan)
					{
						ruleQueue.add(r);
					}
					toDo = Action.DO_NOTHING;
				}
			} 
			catch (Exception E)
			{
//				E.printStackTrace();
				System.out.println("For a 'bring' command, please write 'bring (pos|ID) to <row,col>");
				return Action.DO_NOTHING;
			}
		}
		
		if(toDo != null)
		{
			if(!isActionValid(toDo))
			{
				System.out.println("I'm sorry, I can't " + toDo.toString() + " here");
				return Action.DO_NOTHING;
			}
			else
			{
				return toDo;
			}
		}
		else
		{
			return Action.DO_NOTHING;
		}
	}
	
	private boolean isActionValid(Action a)
	{
		if(a == Action.DO_NOTHING)
		{
			return true;
		}
		if(a == Action.MOVE_UP)
		{
			if(posRow == 0)
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		if(a == Action.MOVE_DOWN)
		{
			if(posRow == env.getRows()-1)
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		if(a == Action.MOVE_LEFT)
		{
			if(posCol == 0)
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		if(a == Action.MOVE_RIGHT)
		{
			if(posCol == env.getCols()-1)
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		
		
		Tile thisTile = env.getTiles()[posRow][posCol];
		if(a == Action.UNSTACK)
		{
			if(b != null)
			{
				return false;
			}
			return env.isTower(posRow, posCol);
		}
		if(a == Action.PICK_UP)
		{
			if(b != null)
			{
				return false;
			}
			if(env.isTower(posRow, posCol))
			{
				return false;
			}
			return env.isTarget(posRow, posCol);
		}
		
		if(a == Action.STACK)
		{
			if(b == null)
			{
				return false;
			}
			return env.isTarget(posRow, posCol);
		}
		if(a == Action.PUT_DOWN)
		{
			if(b == null)
			{
				return false;
			}
			return !env.isTarget(posRow, posCol);
		}
		
		System.out.println("Unrecognized Action");
		return false;
	}

	private LinkedList<Action> planToActions(LinkedList<Rule> plan) {
		LinkedList<Action> actions = new LinkedList<>();

		for (int i = 0; i < plan.size(); i++) {
			LinkedList<Position> targets = new LinkedList<>();

			Rule thisRule = plan.get(i);
			if (thisRule instanceof PickUp) {
				Block b = env.getBlock(Integer.parseInt(((PickUp) thisRule).block));
				targets.add(b.getPosition());
				LinkedList<Action> path = astar(targets.get(0));

				for (Action a : path) {
					actions.add(a);
				}
				actions.add(Action.PICK_UP);
			} else if (thisRule instanceof UnStackIt) {
				Block b = env.getBlock(Integer.parseInt(((UnStackIt) thisRule).block));
				targets.add(b.getPosition());
				LinkedList<Action> path = astar(targets.get(0));

				for (Action a : path) {
					actions.add(a);
				}
				actions.add(Action.UNSTACK);
			} else if (thisRule instanceof StackIt) {
				Block b = env.getBlock(Integer.parseInt(((StackIt) thisRule).target));
				targets.add(b.getPosition());
				LinkedList<Action> path = astar(targets.get(0));

				for (Action a : path) {
					actions.add(a);
				}
				actions.add(Action.STACK);
			} else if (thisRule instanceof PutDown) {
				Position robotPos = new Position(posRow, posCol);
				Position emptyPos = getNearestEmptyPos(robotPos);

				if (emptyPos == null) {
					System.out.println("Entire board is full, aborting");
					return actions;
				}

				LinkedList<Action> path = astar(emptyPos);
				for (Action a : path) {
					actions.add(a);
				}
				actions.add(Action.PUT_DOWN);
			} else {
				System.out.print("Weird rule found");
			}
		}

		return actions;
	}

	private Position getNearestEmptyPos(Position origin) {
		ArrayList<Position> seenPositions = new ArrayList<Position>();
		Queue<Position> positionsToSee = new LinkedList<Position>();
		positionsToSee.add(origin);

		while (!positionsToSee.isEmpty()) {
			Position current = positionsToSee.poll();

			if (env.getTileStatus(current.getRow(), current.getCol()) == TileStatus.CLEAN) {
				return current;
			}

			seenPositions.add(current);

			LinkedList<Position> adj = getAdjacent(current);
			for (Position p : adj) {
				if (!seenPositions.contains(p)) {
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

	public LinkedList<Action> astar(Position p) {
		LinkedList<Position> targets = new LinkedList<Position>();
		targets.add(p);
		return astarM(targets);
	}

	public LinkedList<Action> astarM(LinkedList<Position> targets) {
		PriorityQueue<LinkedList<Position>> paths = new PriorityQueue<LinkedList<Position>>(new comparatorM());
		targetx = targets.get(0).getRow();
		targety = targets.get(0).getCol();

		LinkedList<Position> working = new LinkedList<Position>();
		HashMap<String, ArrayList<Position>> visited = new HashMap<String, ArrayList<Position>>();
		working.add(new Position(posRow, posCol));
		paths.add(working);
		addToMap(working.get(0), targetsVisitedString(working, targets), visited);
		openCount++;

		LinkedList<Position> solution = null;
		if (allTargetsVisited(working, targets)) {
			solution = working;
		}

		while (solution == null) {
			if (paths.size() == 0) {
				System.out.println("I can't do this mate, I checked " + openCount + " routes and they're all bad.");
				this.pathFound = false;
				return null;
			}
			working = paths.poll();
			if (allTargetsVisited(working, targets)) {
//				System.out.println("AAAAAAAAAAAAAAA");
				solution = working;
				break;
			}
			String tvs = targetsVisitedString(working, targets);
//			System.out.println(working.size());
			LinkedList<Position> newLocations = getAdjacent(working.get(working.size() - 1));
//			System.out.println(newLocations.size());
			for (Position p : newLocations) {
				visited.containsKey(tvs);// update visited
				if (!(visited.containsKey(tvs))) {
					visited.put(tvs, new ArrayList<Position>());
				}
				if (positionVisited(visited.get(tvs), p))// avoid loops
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

	private String targetsVisitedString(LinkedList<Position> path, LinkedList<Position> targets) {
		String tvs = "";
		for (int i = 0; i < targets.size(); i++) {
			if (pathContainsPosition(path, targets.get(i))) {
				tvs = tvs + i + ",";
			}
		}
		return tvs;
	}

	private boolean allTargetsVisited(LinkedList<Position> path, LinkedList<Position> targets) {
		for (Position t : targets) {
			if (!pathContainsPosition(path, t)) {
				return false;
			}
		}
		return true;
	}

	private LinkedList<Position> addToPath(LinkedList<Position> path, Position p) {
		LinkedList<Position> newPath = new LinkedList<Position>();
		for (Position p2 : path) {
			newPath.add(p2);
		}
		newPath.add(p);
		return newPath;
	}

	private void addToMap(Position p, String s, HashMap<String, ArrayList<Position>> map) {
		if (map.containsKey(s)) {
			map.get(s).add(p);
		} else {
			map.put(s, new ArrayList<Position>());
			map.get(s).add(p);
		}
	}

	private LinkedList<Position> getAdjacent(Position p) {
		LinkedList<Position> near = new LinkedList<Position>();

		for (int ymod = -1; ymod <= 1; ymod += 2) {
			int newx = p.getRow();
			int newy = p.getCol() + ymod;

//			System.out.println("Checking: " + newx + "," + newy);

			if (env.getTileStatus(newx, newy) != TileStatus.IMPASSABLE) {
				near.add(new Position(newx, newy));
			}
		}
		for (int xmod = -1; xmod <= 1; xmod += 2) {
			int newx = p.getRow() + xmod;
			int newy = p.getCol();

//			System.out.println("Checking: " + newx + "," + newy);

			if (env.getTileStatus(newx, newy) != TileStatus.IMPASSABLE) {
				near.add(new Position(newx, newy));
			}
		}

		return near;
	}

	private boolean positionVisited(ArrayList<Position> visited, Position p) {
		if (visited == null) {
			return false;
		}
		for (Position p2 : visited) {
			if (p2.getRow() == p.getRow() && p2.getCol() == p.getCol()) {
				return true;
			}
		}
		return false;
	}

	private boolean pathContainsPosition(LinkedList<Position> path, Position p) {
		for (Position p2 : path) {
			if (p2.getRow() == p.getRow() && p2.getCol() == p.getCol()) {
				return true;
			}
		}
		return false;
	}

	private LinkedList<Action> pathToRoute(LinkedList<Position> path) {
		LinkedList<Action> route = new LinkedList<Action>();
		for (int i = 0; i < path.size() - 1; i++) {
			Position current = path.get(i);
			Position next = path.get(i + 1);

			if (current.getRow() < next.getRow()) {
				route.add(Action.MOVE_DOWN);
			} else if (current.getRow() > next.getRow()) {
				route.add(Action.MOVE_UP);
			} else if (current.getCol() < next.getCol()) {
				route.add(Action.MOVE_RIGHT);
			} else if (current.getCol() > next.getCol()) {
				route.add(Action.MOVE_LEFT);
			} else {
				route.add(Action.DO_NOTHING);
			}
		}
		return route;
	}

	public class comparatorM implements Comparator<LinkedList<Position>> {

		@Override
		public int compare(LinkedList<Position> o1, LinkedList<Position> o2) {
			if (o1.size() + getHeuristic(o1) > o2.size() + getHeuristic(o2)) {
				return 1;
			} else if (o1.size() + getHeuristic(o1) == o2.size() + getHeuristic(o2)) {
				return 0;
			} else {
				return -1;
			}
		}

	}

	int targetx = 0;
	int targety = 0;

	private double getHeuristic(LinkedList<Position> path) {
		double heu = path.size();
		// add remaning distance to goal
		int goalx = targetx;
		int goaly = targety;
		int lastx = path.get(path.size() - 1).getRow();
		int lasty = path.get(path.size() - 1).getCol();

		heu += Math.sqrt(Math.pow(lastx - goalx, 2) + Math.pow(lasty - goaly, 2));
		return heu;
	}

	private Position getClosestPosition(Position src, ArrayList<Position> dests) {
		Position closest = dests.get(0);
		double dist = distanceBetweenPositions(src, closest);
		for (Position p : dests) {
			if (distanceBetweenPositions(p, src) < dist) {
				closest = p;
				dist = distanceBetweenPositions(p, src);
			}
		}
		return closest;
	}

	private double distanceBetweenPositions(Position p1, Position p2) {
		int dist = 0;
//		return Math.sqrt(Math.pow(p1.getCol()-p2.getCol(), 2)+Math.pow(p1.getRow()-p2.getRow(), 2));
		int startx = p1.getRow();
		int starty = p1.getCol();
		int endx = p2.getRow();
		int endy = p2.getCol();

		int xadj = 0;
		int yadj = 0;
		if (startx > endx) {
			xadj = -1;
		}
		if (startx < endx) {
			xadj = 1;
		}
		if (starty > endy) {
			yadj = -1;
		}
		if (starty < endy) {
			yadj = 1;
		}

		for (int x = startx; x != endx; x += xadj) {
			dist++;
		}
		for (int y = starty; y != endy; y += yadj) {
			dist++;
		}

//		return Math.abs(p1.getCol()-p2.getCol())+Math.abs(p1.getRow()-p2.getRow());
		return dist;
	}
}