//package csu.agent;
//
//import csu.util.BitUtil;
//
//public enum AgentState {
//	WORKING(0),
//	RESTING(1),
//	BURIED(2),
//	DEAD(3),
//	STUCK(4),
//	THINKING(5),
//	CRASH(6),
//	SEARCH(7);
//	
//	public int index;
//	
//	private AgentState(int index) {
//		this.index = index;
//	}
//	
//	public static AgentState getAgentState(int index) {
//		AgentState state;
//		if (index < values().length) {
//			state = values()[index];
//		} else {
//			state = null;
//		}
//		return state;
//	}
//	
//	public static int getAgentStateBitNumber() {
//		return BitUtil.needBitSize(values().length);
//	}
//}
