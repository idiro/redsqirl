package idm.useful;

import java.util.Comparator;

public class ListStringArrayComparator implements Comparator<String[]> {


	private static int indexToCompare = 0;

	@Override
	public int compare(String[] o1, String[] o2) {
		return o1[indexToCompare].compareTo(o2[indexToCompare]);
	}
	
}