package eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;

public class CarteExploration implements Serializable {

	public enum MapAttribute implements Serializable{
		agent,open
	}

	private static final long serialVersionUID = -1333959882640838272L;
	
	/**
	 * Type d'agent concerné par la structure
	 */
	private AgentAbstrait agent;
	
	/**
	 * Structure du graphe en GraphStream
	 */
	private Graph g; 
	
	/**
	 * Reference à l'affichage de grapheStream
	 */
	private Viewer viewer; 
	
	/**
	 * Structure de graphe serializable
	 */
	private SerializableCarteExploration sg;
	
	/**
	 * Noeuds connus mais pas encore visités
	 */
	private List<String> noeudsOuvert;
	
	/**
	 * Noeuds visités
	 */
	private Set<String> noeudsFerme;
	
	/**
	 * Compteur d'identidiant d'arcs
	 */
	private Integer nbEdges;
	
	/**
	 * Contient la position courante de l'explorateur :
	 */
	private String myPosition;
	
	/**
	 * Parameters for graph rendering
	 */
	private String defaultNodeStyle= "node {"+"fill-color: black;"+" size-mode:fit;text-alignment:under;"
			+ " text-size:14;text-color:white;text-background-mode:rounded-box;text-background-color:black;}";
	private String nodeStyle_open = "node.agent {"+"fill-color: forestgreen;"+"}";
	private String nodeStyle_agent = "node.open {"+"fill-color: red;"+"}";
	private String nodeStyle=defaultNodeStyle+nodeStyle_agent+nodeStyle_open;
	
	private boolean isChangeMap;

	/**
	 * Constructeur
	 * @param agentAbstrait
	 */
	public CarteExploration(final AgentAbstrait agentAbstrait) {
		System.setProperty("org.graphstream.ui.renderer","org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		
		this.agent = agentAbstrait;
		this.g= new SingleGraph("Ma vision du monde");
		this.g.setAttribute("ui.stylesheet",nodeStyle);
		
		this.openGui();
		
		this.nbEdges=0;
		
		this.noeudsOuvert		=	new ArrayList<String>();
		this.noeudsFerme		=	new HashSet<String>();
		this.myPosition			=   null;
		this.isChangeMap		=  false;
	}
	
	/**
	 * Ajoute un noeud dans la liste des ouverts (=découverts)
	 * @param position
	 */
	public void addNodeOpen (String position) {
		this.noeudsOuvert.add(position);
	}
	
	/**
	 * Ajoute un noeud dans la liste des fermés (=visité)
	 * @param position
	 */
	public void addNodeClose (String position) {
		this.noeudsFerme.add(position);
	}
	
	/**
	 * Supprime le noeud "position" de la liste des ouverts
	 * @param position
	 */
	public void removeNodeOpen(String position) {
		this.noeudsOuvert.remove(position);
	}
	
	/**
	 * Associe a un noeud un attribut d'open pour identifier le noeud comme ouvert dans le graphe
	 * @param id
	 * @param mapAttribute
	 */
	public void addNode(String id, MapAttribute mapAttribute){
		Node n=this.g.addNode(id);
		n.clearAttributes();
		n.addAttribute("ui.class", mapAttribute.toString());
		n.addAttribute("ui.label",id);
		
	}
	
	/**
	 * Ajoutez l'identifiant du noeud s'il n'existe pas déjà
	 * @param id
	 */
	public void addNode(String id){
		Node n=this.g.getNode(id);
		// Si le noeud n'existe pas dans le graphe
		if(n==null){
			n=this.g.addNode(id);
			n.addAttribute("ui.label",id);
		}else{
			// Si le noeud existe déjà dans le graphe
			if (n.hasAttribute("ui.class")) {
				n.removeAttribute("ui.class");
			}
		}
	}

   /**
    * Ajoutez l'identifiant de l'arc s'il n'existe pas déjà
    * @param idNode1
    * @param idNode2
    */
	public void addEdge(String idNode1,String idNode2){
		try {
			this.nbEdges++;
			this.g.addEdge(this.nbEdges.toString(), idNode1, idNode2);
		}catch (EdgeRejectedException e){
			//Do not add an already existing one
			this.nbEdges--;
		}
		
	}
	
	/**
	 * Calcule le plus court chemin d'idFrom à tout les noeuds ouvert. 
	 *
	 * @param idFrom id of the origin node
	 * @param idTo id of the destination node, say open
	 * @return the list of nodes to follow
	 */
	public List<String> getShortestPath(String idFrom,List<String> lidTo){
		//Data
		List<String> shortestPath=new ArrayList<String>();
		ArrayList<PairShortestPath > sortPath =new ArrayList<PairShortestPath >();
		
		//1) Calcule le plus court chemin de idFrom vers tous les autres noeuds
        //   mais en prenant le nombre d'arc dans le chemin comme longueur
		Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, null);
		dijkstra.init(g);
		dijkstra.setSource(g.getNode(idFrom));
		dijkstra.compute();
		//2) Calcule le plus court chemin sur les noeuds ouverts :
		for(String idTo: lidTo) {
			// Recupere le noeud ouvert du graphe :
			Node n = g.getNode(idTo);
			if (n != null) {
				// Calcule le plus court chemin :
				List<Node> path=dijkstra.getPath(g.getNode(idTo)).getNodePath();
				// Insert les paires dans une liste qui sera ordonnée :
				PairShortestPath  pair = new PairShortestPath (dijkstra.getPathLength(n), path);
				sortPath.add(pair);
			}
		}
		//3) Ordonne la liste de Pair en fonction du chemin le plus court :
		Collections.sort(sortPath, PairShortestPath.ComparatorPair);
		//4) Transforme la liste de node en liste de String (id du noeud) :
		Iterator<Node> iter= sortPath.get(0).getList().iterator();
		while (iter.hasNext()){
			shortestPath.add(iter.next().getId());
		}
		dijkstra.clear();
		shortestPath.remove(0);//Supprime la position courante
		return shortestPath;
	}
	
	/**
	 * Calcule le plus court chemin d'idFrom à tout les noeuds ouvert. 
	 *
	 * @param idFrom id of the origin node
	 * @param idTo id of the destination node, say open
	 * @return the list of nodes to follow
	 */
	public List<List<String>> getShortestPathNodes(String idFrom,List<String> lidTo, Integer distance){
		//Data
		ArrayList<PairShortestPath > sortPath =new ArrayList<PairShortestPath >();

		//1) Calcule le plus court chemin de idFrom vers tous les autres noeuds
        //   mais en prenant le nombre d'arc dans le chemin comme longueur
		Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, null);
		dijkstra.init(g);
		dijkstra.setSource(g.getNode(idFrom));
		dijkstra.compute();
		//2) Calcule le plus court chemin sur les noeuds ouverts :
		for(String idTo: lidTo) {
			// Recupere le noeud ouvert du graphe :
			Node n = g.getNode(idTo);
			if (n != null) {
				// Calcule le plus court chemin :
				List<Node> path=dijkstra.getPath(g.getNode(idTo)).getNodePath();
				// Insert les paires dans une liste qui sera ordonnée :
				PairShortestPath  pair = new PairShortestPath (dijkstra.getPathLength(n), path);
				sortPath.add(pair);
			}
		}
		
		List<List<String>> shortestPath=new ArrayList<List<String>>();

		//3) Ordonne la liste de Pair en fonction du chemin le plus court :
		Collections.sort(sortPath, PairShortestPath.ComparatorPair);
		
		for (int i=0; i < sortPath.size() && i < distance; i++) {
			//4) Transforme la liste de node en liste de String (id du noeud) :
			Iterator<Node> iter= sortPath.get(i).getList().iterator();
			
			List<String> chemin = new ArrayList<String>();
			while (iter.hasNext()){
				chemin.add(iter.next().getId());
			}
			chemin.remove(0);//Supprime la position courante // Problème
			shortestPath.add(chemin);
		}
		dijkstra.clear();
		
		return shortestPath;
	}
	
	/**
	 * Calcule le plus court chemin d'un noeud idFrom à idTo :
	 */
	public ArrayList<String> getShortestPath(String idFrom, String idTo){
		//Data
		ArrayList<String> shortestPath=new ArrayList<String>();
		
		// 1) Calcule le plus court chemin de idFrom vers tous les autres noeuds
        //   mais en prenant le nombre d'arc dans le chemin comme longueur
		Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, null);
		dijkstra.init(g);
		dijkstra.setSource(g.getNode(idFrom));
		dijkstra.compute();

		// 2) Calcule le plus court chemin :
		List<Node> path=dijkstra.getPath(g.getNode(idTo)).getNodePath();
		
		// 3) Transforme la liste de node en liste de String (id du noeud) :
		Iterator<Node> iter= path.iterator();
		while (iter.hasNext()){
			shortestPath.add(iter.next().getId());
		}
		dijkstra.clear();
		shortestPath.remove(0);//Supprime la position courante
		return shortestPath;
	}
	
	
	
	/**
	 * Affichage de chaque noeud avec leurs attribut :
	 */
	public void printMapRepresentationNode () {
		for(Node n: g.getEachNode()) {
			System.out.println();
			System.out.println(this.agent.getLocalName() + " => " +"Id Node : " + n.getId());
			System.out.println(this.agent.getLocalName()  + " => " +"Ensemble d'attributs : ");
			Iterator<String> att = n.getAttributeKeyIterator();
			while(att.hasNext()) {
				String name = att.next();
				System.out.println(this.agent.getLocalName()  + " => " + name + " : " + n.getAttribute(name));
			}
		}
		System.out.println();
	}
	
	/**
	 * Rends l'objet CarteExploration Serializable en supprimant tout trace de GraphStream:
	 */
	public void prepareMigration(){
		// Creation d'une structure serializable du graphe
		this.sg =  new SerializableCarteExploration(this);
		// Fermeture des objets de GraphStream 
		if (this.viewer!=null){
			try {
				this.viewer.close();
			} catch (NullPointerException e) {}
	       this.viewer=null;
		}
		this.g=null;
	}
	
	/**
	 * Rends l'objet CarteExploration non Serializable :
	 */
	public void loadSavedData(){
	  this.g= new SingleGraph("My world vision");
	  this.g.setAttribute("ui.stylesheet",nodeStyle);
	  // MaJ du viewer :
	  this.openGui();
	  this.viewer.addDefaultView(true);
	  
	  Integer nbEd=0;
	  nbEd = this.sg.deSerializationGrapheMigration(this.g, nbEd);
	  System.out.println("Loading done");
	}
	
	public void openGui () {
	  this.viewer = new Viewer(this.g, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
	  this.viewer.enableAutoLayout();
	  this.viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.CLOSE_VIEWER);
	}
	
	/**
	 * Classe interne PairShortestPath
	 * @author sarah
	 */
	public static class PairShortestPath {
		private Double      length;
		private List<Node> list;
		
		public PairShortestPath (Double length, List<Node> list) {
			this.length = length;
			this.list   = list;
		}
		
		public Double getLength () {
			return this.length;
		}
		
		public List<Node> getList() {
			return this.list;
		}
		
		public String toString() {
			return this.length + " ";
		}
		
		public static Comparator<PairShortestPath > ComparatorPair = new Comparator<PairShortestPath >() {
			@Override
			public int compare(PairShortestPath  p1, PairShortestPath  p2) {
		        return p1.getLength().compareTo(p2.getLength());
		    }
		};	
	}
	
	/**
	 * GETTER
	 */
	public Graph getGraph() {
		return this.g;
	}
	
	public Integer getEdge() {
		return this.nbEdges;
	}
	
	public Set<String> getNodesClose () {
		return this.noeudsFerme;
	}
	
	public List<String> getNodesOpen () {
		return this.noeudsOuvert;
	}
	
	public String getMyPosition () {
		return this.myPosition;
	}
	public boolean getChangeMap () {
		return this.isChangeMap;
	}
	
	/**
	 * SETTER
	 */

	public void setIncrementEdge () {
		this.nbEdges += 1;
	}
	
	public void setDecrementEdge () {
		this.nbEdges -= 1;
	}
	
	public void setMyPosition (String newPosition) {
		this.myPosition = newPosition;
	}
	public void setChangeMap (boolean value) {
		this.isChangeMap = value;
	}
}
