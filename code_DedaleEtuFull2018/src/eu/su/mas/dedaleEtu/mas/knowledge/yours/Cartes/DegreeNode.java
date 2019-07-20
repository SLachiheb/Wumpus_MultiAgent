package eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

public class DegreeNode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7243849934977124500L;
	private Graph	g;
	private Map<String, Integer> degreeNode;
	
	public DegreeNode (Graph g) {
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
}

