import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Test3_listeOrdonnee {
	
	public static class  Pair {
		private Integer      length;
		private String list;
		//private List<String> list;
		
		public Pair(Integer length, String list) {
			this.length = length;
			this.list   = list;
		}
		
		public Integer getLength () {
			return this.length;
		}
		
		public String getList() {
			return this.list;
		}
		
		public String toString() {
			return this.length + " ";
		}
		
		public static Comparator<Pair> ComparatorPair = new Comparator<Pair>() {
			@Override
			public int compare(Pair p1, Pair p2) {
		        return p1.getLength().compareTo(p2.getLength());
		    }
		};
			
	}
	
	public void t () {	
		Pair p3 = new Pair(19, "Titi");
		Pair p1 = new Pair(2, "Tata");
		Pair p2 = new Pair(6, "Titi");
		ArrayList<Pair> p = new ArrayList<Pair>();
		p.add(p3);
		p.add(p1);
		p.add(p2);
		
		Collections.sort(p, Pair.ComparatorPair);
		
		/*Collections.sort(p, new Comparator<Pair>() {
			
			@Override
			public int compare(Pair p1, Pair p2) {
		        return p1.getLength().compareTo(p2.getLength());
		    }
		}); */
		
		System.out.println(p);
	}
	
	public static void main(String[] args) {
		Pair p3 = new Pair(19, "Titi");
		Pair p1 = new Pair(2, "Tata");
		Pair p2 = new Pair(6, "Titi");
		ArrayList<Pair> p = new ArrayList<Pair>();
		p.add(p3);
		p.add(p1);
		p.add(p2);
		
		Collections.sort(p, Pair.ComparatorPair);
		
		/*Collections.sort(p, new Comparator<Pair>() {
			
			@Override
			public int compare(Pair p1, Pair p2) {
		        return p1.getLength().compareTo(p2.getLength());
		    }
		}); */
		
		System.out.println(p);
	}

}
