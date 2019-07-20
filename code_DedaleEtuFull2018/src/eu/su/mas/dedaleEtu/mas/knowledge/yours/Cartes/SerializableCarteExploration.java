package eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

public class SerializableCarteExploration implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1850415389886449297L;
	
	/**
	 * Contient chaque noeud du graphe avec les attributs respectifs:
	 */
	private HashMap<String, ArrayList<PairAttribut>> 	serializablNodeG; 
	
	/**
	 * Contient chaque arcs du graphe 
	 */
	private ArrayList<PairEdge> 						serializablEdgeG;
	
	/**
	 * Contient les noeuds ouverts
	 */
	private List<String> 								noeudsOuvert;
	
	/**
	 * Noeuds visités
	 */
	private Set<String> 								noeudsFerme;
	
	/**
	 * Contient le noeuds a atteindre 
	 */
	//private String 										nodeBut;
	
	/**
	 * Contient le noeuds a atteindre 
	 */
	private String 										myPosition;
	
	/**
	 * Contient le chemin au noeud But :
	 */
	//private List<String> 								destinationCurrent;
	
	
	/**
	 * Constructeur initialise par defaut à vide
	 */
	public SerializableCarteExploration (CarteExploration carteExploration) {
		this.serializablNodeG 	= new HashMap<String, ArrayList<PairAttribut>>();
		this.serializablEdgeG 	= new ArrayList<PairEdge>();
		this.noeudsOuvert		= new ArrayList<String>();
		this.noeudsFerme		= new HashSet<String>();
		this.init_MyPosition(carteExploration);
		this.serializationGraphe(carteExploration);
	}
	
	/**
	 * Initialise le nodeBut et le cheminBut
	 * @param carteExploration
	 */
	private void init_MyPosition (CarteExploration carteExploration) {
		this.myPosition = carteExploration.getMyPosition();
	}
	
	/**
	 * Initialisation du graphe serializableG à partir du Graph g :
	 * @param g
	 */
	private boolean serializationGraphe(CarteExploration carteExploration) {
		// PHASE I : Serialization des noeuds :
		// 1) Verifie que le graphe pointe sur un graphe existant
		if (carteExploration.getGraph() == null) {
			return false;
		}
		// 2) Realise la copie de g (non seerializable) dans la hashMap
		for(Node n : carteExploration.getGraph().getEachNode()) {
			// Creation d'une liste d'Attribut :
			ArrayList<PairAttribut> list = new ArrayList<PairAttribut>();
			//Recupere l'ensemble des Attributs pour chaque noeud
			Iterator<String> it = n.getAttributeKeyIterator();
			while(it.hasNext()) {								
				String nameAttribut = it.next();
				list.add(new PairAttribut(nameAttribut, n.getAttribute(nameAttribut)));
			}
			// Ajout la liste d'attribut au noeud
			this.serializablNodeG.put(n.getId(), list);
		}
		
		// PHASE II : Serialization des arcs :
		for(Edge e : carteExploration.getGraph().getEachEdge()) {
			// Creation d'un arc dans la liste 
			this.serializablEdgeG.add(new PairEdge(e.getNode0().toString(), e.getNode1().toString()));
		}
		
		// PHASE III : Sauvegarde des noeuds ouverts et fermés
		this.noeudsOuvert = carteExploration.getNodesOpen();
		this.noeudsFerme  = carteExploration.getNodesClose();
				
		return true;
	}
	
	/**
	 * After Migration  :
	 * @param graphExistant
	 */
	public Integer deSerializationGrapheMigration(Graph graphVide, Integer nbEdges) {
		// PHASE I : Creation des noeuds dans le graphe 
		// Parcours l'ensemble de la HasMap 
		for (Map.Entry<String, ArrayList<PairAttribut>> entry : this.serializablNodeG.entrySet()) {
			// Recupere le noeud de position entry.getKey()
			Node node = graphVide.getNode(entry.getKey());
			// Si le noeud n'existe pas :
			if (node == null) {
				// Creation du noeud
				Node newNode = graphVide.addNode(entry.getKey());
				// Insertion des attributs :
				for (PairAttribut pA: entry.getValue()) {
					newNode.addAttribute(pA.getNameAttribut(), pA.getValueAttribut());
				}
			}
		}
		
		// PHASE II : Creation des arcs
		for (PairEdge e: this.serializablEdgeG) {
			try {
				nbEdges++;
				graphVide.addEdge(nbEdges.toString(), e.getnameEdgeLeft(), e.getnameEdgeRight());
			}catch (EdgeRejectedException ee){
				//Do not add an already existing one
				nbEdges--;
			}
		}
		
		// PHASE III : Mise a jours des nodesOpen et nodesClose : // A voir si necessaire ?
		return nbEdges;
	}
	
	/**
	 * Fusion de deux cartes dans une structure non - serializable :
	 * @param graphExistant
	 */
	public void fusionGrapheNonSerializable(CarteExploration carteToFusion) {
		// PHASE I : Creation des noeuds dans le graphe 
		for (Map.Entry<String, ArrayList<PairAttribut>> entry : this.serializablNodeG.entrySet()) {
			// Recupere le noeud de position entry.getKey()
			Node node = carteToFusion.getGraph().getNode(entry.getKey());
			// Si le noeud n'existe pas :
			if (node == null) {
				carteToFusion.setChangeMap(true);
				Node newNode = carteToFusion.getGraph().addNode(entry.getKey());
								
				// Insertion des attributs :
				for (PairAttribut pA: entry.getValue()) {
					newNode.addAttribute(pA.getNameAttribut(), pA.getValueAttribut());
				}
			} 
		}

		
		// PHASE II : Creation des arcs
		for (PairEdge e: this.serializablEdgeG) {
			try {
				carteToFusion.setIncrementEdge();
				carteToFusion.getGraph().addEdge(carteToFusion.getEdge().toString(), e.getnameEdgeLeft(), e.getnameEdgeRight());
			}catch (EdgeRejectedException ee){
				//Do not add an already existing one
				carteToFusion.setDecrementEdge();
			}
		}
		
		// PHASE III : synchronisation des nodesOpen et nodesClose :
		for (String node : this.noeudsFerme) {
			// Elimine dans carteToFusion de ses noeudsOuverts le node close :
			if (carteToFusion.getNodesOpen().contains(node)) {
				carteToFusion.getNodesOpen().remove(node);
			}
			// Ajoute le node close à la list des Closes de carteToFusion
			if (!carteToFusion.getNodesClose().contains(node)) {
				carteToFusion.getNodesClose().add(node);
				Node nodee = carteToFusion.getGraph().getNode(node);
				if (nodee.hasAttribute("ui.class")) {
					nodee.removeAttribute("ui.class");
				}
			}
		}
		
		for (String node : this.noeudsOuvert) {
			if (!carteToFusion.getNodesClose().contains(node)) {
				if (!carteToFusion.getNodesOpen().contains(node)) {
					carteToFusion.addNodeOpen(node);
				}
			}
		}	
	}
	
	public String toString () {
		String s = "Ensemble de noeud :\n";
		for (Map.Entry<String, ArrayList<PairAttribut>> entry : this.serializablNodeG.entrySet()) {
			s+= entry.getKey() + "\n";
		}
		s += "Ensemble d'arc :\n";
		for (PairEdge e : this.serializablEdgeG) {
			s += e + "\n";
		}
		s += "Noeuds ouverts : " + this.noeudsOuvert + "\n";
		s += "Noeuds fermées : " + this.noeudsFerme + "\n";
		return s;
	}
	
	/**
	 * GETTER
	 */
	public String getMyPosition() {
		return this.myPosition;
	}
	
	/**
	 * Classe Interne 
	 * Conteneur de correspondance entre attribut et valeur :
	 * @author sarah
	 *
	 */
	public class PairEdge implements Serializable {
		private static final long serialVersionUID = 5471415969572649120L;
		private String  nameEdge1;
		private String  nameEdge2;	
		public PairEdge (String nameEdge1, String nameEdge2) {
			this.nameEdge1 	= nameEdge1;
			this.nameEdge2 	= nameEdge2;
		}	
		public String getnameEdgeLeft() {
			return this.nameEdge1;
		}	
		public String getnameEdgeRight() {
			return this.nameEdge2;
		}
		public String toString () {
			return "(" + this.nameEdge1 + ", " + this.nameEdge2 + ")";
		}
	}
	
	/**
	 * Classe Interne 
	 * Conteneur de correspondance entre attribut et sa valeur :
	 * @author sarah
	 *
	 */
	public class PairAttribut implements Serializable {
		private static final long serialVersionUID = 5471415969572649120L;
		private String  nameAttribut;
		private String  valueAttribut;	
		public PairAttribut (String nameAttribut, String valueAttribut) {
			this.nameAttribut 	= nameAttribut;
			this.valueAttribut 	= valueAttribut;
		}	
		public String getNameAttribut() {
			return this.nameAttribut;
		}	
		public String getValueAttribut() {
			return this.valueAttribut;
		}
	}

}
