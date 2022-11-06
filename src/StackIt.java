
public class StackIt implements Rule{
	String block;
	String target;
	
	public StackIt(String block, String target) {
		this.block = block;
		this.target = target;				
	}
	
	public boolean equals(StackIt s) {
		return block.equals(s.block) && target.equals(s.target);
	}
	
	public String toString() {
		return "stack: " + block + " on " + target;
	}

}
