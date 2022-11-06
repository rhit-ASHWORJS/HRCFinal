public class UnStackIt implements Rule{
	String block;
	String target;
	
	public UnStackIt(String block, String target) {
		this.block = block;
		this.target = target;				
	}
	
	public boolean equals(UnStackIt s) {
		return block.equals(s.block) && target.equals(s.target);
	}
	
	public String toString() {
		return "unstack: " + block + " from " + target;
	}

}