package eu.su.mas.dedaleEtu.mas.agents.yours;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.SelectionTresorCollectifBehaviour;

import jade.core.behaviours.Behaviour;


/**
 * Agent Explorateur
 * Il explore la Map avec un algorithme de DFS.
 */

public class AgentExplorateur extends AgentAbstrait {

	private static final long 	serialVersionUID = 3754479768920213443L;
	
    public static final int	T_ACTION_INTERBLOCAGE_EXPLO  						= 90;  
    public static final int	T_ACTION_COLLECTIF_AFTER_SELECT_TRESOR  			= 91;  
    public static final int	T_TRESOR_COLLECTIF_EXPLO							= 92;
    public static final int	T_TRESOR_COLLECTIF_EXPLO_AFTER_A_INTERBLOCAGE		= 93;
    public static final int	T_CHECK_SIGNAUX_AFTER_A_INTERBLOCAGE				= 94;
    public static final int	T_CHECK_SIGNAUX_AFTER_SELECT_TRESOR_COLLECTIF_EXPLO	= 95;

    
	// Data de l'agent Explorateur :
	protected 	Double 				tauxSociabilite;
	protected   Random					random;

	
    public AgentExplorateur () {
    	this.random				= new Random();
    	this.tauxSociabilite = 1.;
    }
    
	protected void setup(){

		super.setup();
		
		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		this.declaration_FSM_Explorateur();
		
		lb.add(this.fsm);
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		
		addBehaviour(new startMyBehaviours(this,lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");
	}
	
	public void declaration_FSM_Explorateur() {
		// 5) Déclaration des comportements de l'agentExplorateur :
		SelectionTresorCollectifBehaviour	tresorCollectifExplorateur = new SelectionTresorCollectifBehaviour(this);
        
        // 6) Déclaration de la machine d'etat de l'agentExplorateur :
		this.fsm.registerState(tresorCollectifExplorateur, "SELECT_TRESOR_COLLECTIF_EXPLO");
        
		this.fsm.registerTransition("PLANIFICATION"   				, "SELECT_TRESOR_COLLECTIF_EXPLO"  	, T_TRESOR_COLLECTIF_EXPLO);	
		this.fsm.registerTransition("SELECT_TRESOR_COLLECTIF_EXPLO" , "A_INTERBLOCAGE"     				, T_ACTION_INTERBLOCAGE_EXPLO);	
		this.fsm.registerTransition("SELECT_TRESOR_COLLECTIF_EXPLO" , "ACTION_COLLECTIF"   				, T_ACTION_COLLECTIF_AFTER_SELECT_TRESOR);	
		this.fsm.registerTransition("A_INTERBLOCAGE"  				, "SELECT_TRESOR_COLLECTIF_EXPLO"  	, T_TRESOR_COLLECTIF_EXPLO_AFTER_A_INTERBLOCAGE);	
		this.fsm.registerTransition("A_INTERBLOCAGE"  				, "CHECK_SIGNAUX"  					, T_CHECK_SIGNAUX_AFTER_A_INTERBLOCAGE);	
		this.fsm.registerTransition("SELECT_TRESOR_COLLECTIF_EXPLO" , "CHECK_SIGNAUX"  					, T_CHECK_SIGNAUX_AFTER_SELECT_TRESOR_COLLECTIF_EXPLO);	

	}
	
	/**
	 * Utiliser dans la Satisfaction :
	 * @return
	 */
	public List<String> searchChemin_isAbandonTask () {
		System.out.println("*****" + this.getLocalName() + " : ABANDON");

		ArrayList<String> cheminFind = new ArrayList<String>();
		
		// Mettre à jour la satisfaction :
		this.setActionSatisfaction(true);
		// Annuler l'action en cours :
		this.setSearchTresorCollectif(false);
		
		// Ajoute dans les noeuds closes, le noeud faisant pas parti de mon noeud courant :
		ArrayList<String> nodeCloses = new ArrayList<String>();
		for (String node: this.getCarteExploration().getNodesClose()) {
			if (node.equals(this.getCurrentPosition()) == false) 
				nodeCloses.add(node);
		}
		
		// Après le filtre, il me reste des noeuds closes :
		if (nodeCloses.isEmpty() == false) {
			// Recherche tout les chemins allant de ma position à un noeud close :
			List<List<String>> list_chemin_close = this.getCarteExploration().getShortestPathNodes(this.getCurrentPosition(),
					new ArrayList<String>(nodeCloses), (nodeCloses.size() / 2)/*5*/);		
			
			if (this.getNodesBut().isEmpty() == false) {
				// Supprime les chemins passant par le noeud bloqué :
				String nodeBlocked = this.getNodesBut().get(0); // Tanker n'a pas de noeud but.
				for (int i=0; i < list_chemin_close.size() ; i++) {
					for (int j=0; j < list_chemin_close.get(i).size() ; j++) {
						if (list_chemin_close.get(i).get(j).equals(nodeBlocked) == true) {
							list_chemin_close.remove(i);
							i = 0;
							break;
						}
					}
				}
			}
			if (list_chemin_close.isEmpty() == false) {
				// Taille de la liste des noeuds fermées :
				int taille_list_close = list_chemin_close.size();
				// Prendre un chemin de taille de la moitié du pire cas :
				int alea = this.random.nextInt(taille_list_close); //
				cheminFind = new ArrayList<String>(list_chemin_close.get(alea));
			}	
		}
		return cheminFind;
	}

	public Double getTauxSociabilite () {
		return this.tauxSociabilite;
	}
}
