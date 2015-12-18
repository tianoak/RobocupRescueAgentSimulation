package csu.common.test;

public class Enum_Ordinal {
	public enum PassableLevel {
		SURE_PASSABLE, 
		COMMUNICATION_PASSABLE,
		LOGICAL_PASSABLE,
		UNPASSABLE,
		UNKNOWN; 
		
		public boolean isPassable() {
			return this.ordinal() < UNPASSABLE.ordinal();
		}
	}
	
	public static void main(String[] args) {
		Boolean flag = PassableLevel.UNPASSABLE.isPassable();
		System.out.println(flag);
		System.out.println(flag.toString());
		System.out.println("SURE_PASSABLE: " + PassableLevel.SURE_PASSABLE.ordinal());
		System.out.println("COMMUNICATION_PASSABLE: " + PassableLevel.COMMUNICATION_PASSABLE.ordinal());
		System.out.println("LOGICAL_PASSABLE: " + PassableLevel.LOGICAL_PASSABLE.ordinal());
		System.out.println("UNPASSABLE: " + PassableLevel.UNPASSABLE.ordinal());
		System.out.println("UNKNOWN: " + PassableLevel.UNKNOWN.ordinal());
	}
}
