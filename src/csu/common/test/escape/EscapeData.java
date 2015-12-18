package csu.common.test.escape;

import java.util.List;
import java.util.Map;

import csu.model.object.CSUBlockade;
import csu.model.object.CSURoad;

public interface EscapeData {
	
	public List<CSURoad> roadList();
	
	public Map<Integer, CSUBlockade> blockadeList();
	
	public int[] getBlockadeList(int roadId);
}
