public class Clear implements Predicate {
	String block;
	
	public Clear(String block) {
		this.block = block;
	}
	
	public boolean equals(Clear s) {
		return block.equals(s.block);
	}
	
	public String toString() {
		return "clear: " + block;
	}
}
