package datatype;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class AttributesComparator implements Comparator<String> {
	
	// id, name, class, title, alt and value
	List<String> definedOrder = 
		    Arrays.asList("id", "name", "class", "title", "alt", "value");
	
	@Override
	public int compare(String o1, String o2) {
		return Integer.valueOf(
	            definedOrder.indexOf(o1))
	            .compareTo(
	                Integer.valueOf(
	                    definedOrder.indexOf(o2)));
	}

}
