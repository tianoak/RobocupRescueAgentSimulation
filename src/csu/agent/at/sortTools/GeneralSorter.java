package csu.agent.at.sortTools;

import java.util.Comparator;

/**
 * This file defines the a sorter for AbstractSortElement
 * @author Nale
 * Jun 26, 2014
 */
public class GeneralSorter implements Comparator<AbstractSortElement>{

	@Override
	public int compare(AbstractSortElement o1, AbstractSortElement o2) {
		int data1 = o1.getData();
		int data2 = o2.getData();
		if (data1 < data2)
			return -1;
		else if (data1 > data2)
			return 1;
		else return o1.getIDValue() - o2.getIDValue();
	}


	
}
