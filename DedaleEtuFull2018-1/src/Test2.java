import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

public class Test2 {
	
	private Graph	g;
	private Map<String, Integer> degreeNode;
	
	public Test2 (Graph g) {
		this.g	= g;
		this.degreeNode = new HashMap<String, Integer>();
		this.init_DegreeNode();
	}

	private void init_DegreeNode () {
		for (Node node: this.g) {
			degreeNode.put(node.getId(), node.getDegree());
		}
	}
	
	public String positionTanker (Integer positionTanker) {
		
		// Ajout des entrées de la map à une liste
		List<Entry<String, Integer>> entries = new ArrayList<Entry<String, Integer>>(this.degreeNode.entrySet());
		
		// Tri de la liste sur la valeur de l'entrée
		Collections.sort(entries, new Comparator<Entry<String, Integer>>() {
			public int compare(final Entry<String, Integer> e1, final Entry<String, Integer> e2) {
				if (e1.getValue().compareTo(e2.getValue()) == 1) {
					return -1;
				} else if (e1.getValue().compareTo(e2.getValue()) == -1) {
					return 1;
				} else {
					return e1.getValue().compareTo(e2.getValue());
				}
			}
		});
		
		// Récupérer une liste sans doublons des différentes degrés présents dans le graphe :
		Set<Integer> setList = new HashSet<Integer>();
		for (Entry<String, Integer> entry : entries) {
			setList.add(entry.getValue());
		}

		// Trier la liste sans doublons de manière décroissante :
		List<Integer> list	= new ArrayList<Integer>(setList);
		Collections.sort(list, Collections.reverseOrder());
		
		// Pour chaque degrée de node récuperer l'ensemble des noeuds de ce degrée :
		List<Entry<String, Integer>> resultatPosition = new ArrayList<Entry<String, Integer>>();

		for (Integer degree : list) {
			System.out.println(degree);
			List<Entry<String, Integer>> entries2 = new ArrayList<Entry<String, Integer>>(entries);
			List<Entry<String, Integer>> match = new ArrayList<Entry<String, Integer>>();

			// Supprimer tout les noeuds n'étant pas de ce degrée :
			for (Entry<String, Integer> entry : entries2) {
				if (degree == entry.getValue()) {
					match.add(entry);
				}
			}

			if (match.size() > 0) {
				// Trier selon le nom :
				Collections.sort(match, new Comparator<Entry<String, Integer>>() {
					public int compare(final Entry<String, Integer> e1, final Entry<String, Integer> e2) {
						return e1.getKey().compareTo(e2.getKey());
					}
				});
				
				for (Entry<String, Integer> entry : match) {
					resultatPosition.add(entry);
				}
			}
		}	
		return resultatPosition.get(positionTanker).getKey();
	}
	

	public static void main(String[] args) {
		Graph g = new SingleGraph("example");
		g.display(true);
		
		
		g.addNode("B").addAttribute("xy", 1, 2);
		g.addNode("A").addAttribute("xy", 0, 1);
		g.addNode("C").addAttribute("xy", 1, 1);
		g.addNode("D").addAttribute("xy", 1, 0);
		g.addNode("E").addAttribute("xy", 2, 2);
		g.addNode("F").addAttribute("xy", 2, 1);
		g.addNode("G").addAttribute("xy", 2, 0);
		g.addEdge("AC", "A", "C");
		g.addEdge("AD", "A", "D");
		g.addEdge("AB", "A", "B");
		g.addEdge("BC", "B", "C");
		g.addEdge("CD", "C", "D");
		g.addEdge("BE", "B", "E");
		g.addEdge("CF", "C", "F");
		g.addEdge("DF", "D", "F");
		g.addEdge("EF", "E", "F");
		
		
		Test2 t = new Test2(g);
		System.out.println(t.positionTanker(1));

		for (Node n : g) {
			n.addAttribute("label", n.getId());
			System.out.println(n.getId() + " : " + n.getDegree());
		}
				
		for (Edge e : g.getEachEdge())
			e.addAttribute("label", "" + (int) e.getNumber("length"));
		
		Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, null);
		dijkstra.init(g);
		dijkstra.setSource(g.getNode("A"));
		dijkstra.compute();
		
		// Print the lengths of the new shortest paths
		for (Node node : g)
			System.out.printf("%s->%s:%10.2f%n", dijkstra.getSource(), node, dijkstra.getPathLength(node));
		
		System.out.println();
		// voisin
		/*Iterator<? extends Node> nodes = g.getNode("A").getNeighborNodeIterator();
		while(nodes.hasNext()) {
			Node node = nodes.next();
			System.out.println(node.getId());
		}*/
		
		//System.out.println(Toolkit.degreeMap(g));

	}
}
