
public class OnTable implements Predicate{
	String block;
	
	public OnTable(String block) {
		this.block = block;
	}
	
	public boolean equals(OnTable s) {
		return block.equals(s.block);
	}
	
	public String toString() {
		return "onTable: " + block;
	}

}