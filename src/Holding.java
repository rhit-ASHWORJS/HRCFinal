public class Holding implements Predicate{
	String block;
	
	public Holding(String block) {
		this.block = block;
	}
	
	public boolean equals(Holding s) {
		return block.equals(s.block);
	}
	
	public String toString() {
		return "holding: " + block;
	}

}