
public class PutDown implements Rule{
	String block;
	
	public PutDown(String block) {
		this.block = block;
	}
	
	public boolean equals(PutDown s) {
		return block.equals(s.block);
	}

	public String toString() {
		return "putdown: " + block;
	}
}