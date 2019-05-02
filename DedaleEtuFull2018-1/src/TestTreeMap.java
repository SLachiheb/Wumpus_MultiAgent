import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TestTreeMap {
	
	public static class Protocole {
		private Integer value;
		private String name;
		
		public Protocole (Integer value, String name) {
			this.name = name;
			this.value = value;
		}
		
		public Integer getValue () {
			return this.value;
		}
		
		public String getName() {
			return this.name;
		}
		
		public String toString () {
			return "(" + this.name + "," + this.getValue() + ")"  ;
		}

	}
	
	public static class CompProtocole implements Comparator<Protocole> 
	{ 
		@Override
		public int compare(Protocole o1, Protocole o2) {
			return o1.getValue().compareTo(o2.getValue());
		}
	} 

	public static void main(String[] args) {
		 // Creating a TreeMap with a Custom comparator (Descending order)
		List<Protocole> fileExtensions = new ArrayList<Protocole>();

		fileExtensions.add(new Protocole(1, "a"));
        fileExtensions.add(new Protocole(5, "b"));
        fileExtensions.add(new Protocole(3, "c"));
        fileExtensions.add(new Protocole(4, "d"));
        fileExtensions.add(new Protocole(-1, "e"));
	       
        // Printing the TreeMap (The keys will be sorted based on the supplied comparator)
        System.out.println(fileExtensions);
       
        Collections.sort(fileExtensions, new CompProtocole());
        
        System.out.println(fileExtensions);


	}

}
