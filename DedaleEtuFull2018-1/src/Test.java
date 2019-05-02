
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;


public class Test {
	
	public enum Objet {
		DIAMOND("Diamond"),
		GOLD("Gold"),
		WELL("Well");
		
		private String name ="";
		
		Objet(String name) {
			this.name = name;
		}
		
		public String toString() {
			return name;
		}
	}
	
	public static void main (String [] args) {
		
		Graph graph = new SingleGraph("Tutoriel 1");
		//graph.setStrict(false); /*Autorise l'utilisation de noeud non instancie*/
		//graph.setAutoCreate(true); /*Creer automatiquement les noeuds non instancie*/
		
		// Creation de noeud
		graph.addNode("V");
		graph.addNode("X");
		graph.addNode("Y");
		graph.addNode("Z");
		
		// Creation d'arc
		graph.addEdge("VX", "V", "X");
		graph.addEdge("VX", "Z", "X");
		
		
		//graph.addEdge("XY", "X", "Y");
		//graph.addEdge("YZ", "Y", "Z");
		//graph.addEdge("ZX", "Z", "X");
		
		
		// Obtenirn une reference sur le noeud :
		Node x = graph.getNode("X");
		
		x.addAttribute("ui.label", "treasure");	
		x.addAttribute("Diamond", 25);
		//x.hasAttribute(key)
		
		
		
		//for (x.getAttributeKeyIterator()
		
		
		x.changeAttribute("Diamond", (int)x.getAttribute("Diamond") - 5);
		System.out.println((int)x.getAttribute("Diamond"));
		
		/*Object[] tab = x.getArray("X");
		for (Object t : tab) {
			System.out.println(t);
		}*/
		
		//x.clearAttributes();
		System.out.println();
		for (String keyName : x.getAttributeKeySet())
			System.out.println(keyName);
		System.out.println();
		/*x.addAttributes(attributes);
		x.changeAttribute(attribute, values);
		x.changeAttribute(attribute, values);
		x.clearAttributes();
		x.*/
		
		System.out.println("Atrribut Perle : "+x.hasAttribute("Perle"));
		
		
		System.out.println("Id unique : " + x.getId());
		System.out.println("Index de x : " + x.getIndex());
		System.out.println("Degré du noeud x : " + x.getDegree());
		System.out.println("Nombre d'attribut stocké : " + x.getAttributeCount());
		
		
		// Obtenirn une reference sur un arc :
		//Edge xy = graph.getEdge("XY");
		
		// itérer sur tous les arcs du graphique et affichera leur identifiant 
		for(Edge arc : graph.getEachEdge()) {
			System.out.print(arc.getId() + " ");
		}
		
		System.out.println();
		
		// itérer sur tous les arcs du graphique et affichera leur identifiant 
		for(Node n : graph.getEachNode()) {
			System.out.print(n.getId() + " ");
		}
		
		
		
		
		// Affichage
		graph.display();
	}

}
