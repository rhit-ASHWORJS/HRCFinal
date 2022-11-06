
public class PickUp implements Rule{
	String block;
	
	public PickUp(String block) {
		this.block = block;
	}
	
	public boolean equals(PickUp s) {
		return block.equals(s.block);
	}
	
	public String toString() {
		return "pickup: " + block;
	}

}
