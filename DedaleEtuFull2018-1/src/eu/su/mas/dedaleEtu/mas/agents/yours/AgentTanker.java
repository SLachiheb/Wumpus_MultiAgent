package eu.su.mas.dedaleEtu.mas.agents.yours;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.BehaviourTanker.CheckConfirmationPositionSiloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.BehaviourTanker.CheckReceiveMapEnvBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.BehaviourTanker.ItinerairePositionTankerBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.BehaviourTanker.ReceiveConfirmationPositionSiloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.BehaviourTanker.SendPositionSiloBehaviour;
import jade.core.behaviours.Behaviour;

public class AgentTanker extends AgentAbstrait {

	/**
	 * 
	 */
	private static final long serialVersionUID = 193655526301896452L;
	
    public static final int	T_ITINERAIRE_SILO								= 73;
	public static final int T_PLANIFICATION_AFTER_ITINERAIRE_SILO			= 74;
    public static final int	T_CHECK_CONFIRMATION_SILO						= 75;
	public static final int T_PLANIFICATION_AFTER_CHECK_CONFIRMATION_SILO  	= 76;
    public static final int	T_SEND_POSITION_SILO							= 77;
    public static final int	T_PLANIFICATION_AFTER_SEND_POSITION_SILO		= 78;
    
    public static final int	T_RECEIVE_CONFIRMATION_POSITION					= 79;
    public static final int T_PLANIFICATION_AFTER_RECEIVE					= 80;
    public static final int T_CHECK_MAP_ENV									= 81;
    public static final int T_PLANIFICATION_AFTER_CHECK_MAP_ENV				= 82;
    
	// Data de l'agent Explorateur :
	protected 	Integer				idTanker;
	protected 	static Integer		compteurTanker = 0;
	protected 	Double 				tauxSociabilite;
	protected   Random				random;
	
	protected 	String				positionSilo;
	protected 	List<String>		bd_knowledgePositionSilo;
	
	protected 	Integer				compteurSendPosition;
	protected	final Integer		INTERVALLE_SEND_POSITION = 1;				
	
    public AgentTanker () {
    	this.tauxSociabilite 	= 1.;
    	
    	this.random						= new Random();
    	this.idTanker					= AgentTanker.compteurTanker++;
    	this.positionSilo				= null;
    	this.bd_knowledgePositionSilo	= new ArrayList<String> ();
    	this.compteurSendPosition		= 0;
    }
    
	protected void setup(){

		super.setup();
		
		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		declaration_FSM_Tanker();
		
		lb.add(this.fsm);
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		
		addBehaviour(new startMyBehaviours(this,lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");
	}
	
	public void declaration_FSM_Tanker() {
		ItinerairePositionTankerBehaviour			itineraireSilo		= new ItinerairePositionTankerBehaviour(this);
		SendPositionSiloBehaviour					sendPositionSilo	= new SendPositionSiloBehaviour(this);
		CheckConfirmationPositionSiloBehaviour		checkConfirmation	= new CheckConfirmationPositionSiloBehaviour(this);
		ReceiveConfirmationPositionSiloBehaviour	receiveConfirmation	= new ReceiveConfirmationPositionSiloBehaviour(this);
		CheckReceiveMapEnvBehaviour					checkMapEnv			= new CheckReceiveMapEnvBehaviour(this);
		
		// Déclaration des états de la FSM :
        this.fsm.registerState(itineraireSilo				, "ITINERAIRE_SILO");
        this.fsm.registerState(sendPositionSilo				, "SEND_POSITION_SILO");
        this.fsm.registerState(checkConfirmation			, "CHECK_CONFIRMATION_SILO");
        this.fsm.registerState(receiveConfirmation			, "RECEIVE_CONFIRMATION_POSITION");
        this.fsm.registerState(checkMapEnv					, "CHECK_MAP_ENV");
        
        // Déclaration des transitions de la FSM :
     	this.fsm.registerTransition("PLANIFICATION"  			, "ITINERAIRE_SILO" 		, T_ITINERAIRE_SILO);
     	this.fsm.registerTransition("ITINERAIRE_SILO"  			, "PLANIFICATION" 			, T_PLANIFICATION_AFTER_ITINERAIRE_SILO);
     	this.fsm.registerTransition("PLANIFICATION"  			, "CHECK_CONFIRMATION_SILO"	, T_CHECK_CONFIRMATION_SILO);
     	this.fsm.registerTransition("CHECK_CONFIRMATION_SILO"  	, "PLANIFICATION"		 	, T_PLANIFICATION_AFTER_CHECK_CONFIRMATION_SILO);
     	this.fsm.registerTransition("PLANIFICATION"  			, "SEND_POSITION_SILO"		, T_SEND_POSITION_SILO);
     	this.fsm.registerTransition("SEND_POSITION_SILO"  		, "PLANIFICATION" 			, T_PLANIFICATION_AFTER_SEND_POSITION_SILO);
     	this.fsm.registerTransition("PLANIFICATION"  					, "RECEIVE_CONFIRMATION_POSITION"	, T_RECEIVE_CONFIRMATION_POSITION);
     	this.fsm.registerTransition("RECEIVE_CONFIRMATION_POSITION"  	, "PLANIFICATION" 					, T_PLANIFICATION_AFTER_RECEIVE);
     	this.fsm.registerTransition("PLANIFICATION"  					, "CHECK_MAP_ENV"					, T_CHECK_MAP_ENV);
     	this.fsm.registerTransition("CHECK_MAP_ENV"  					, "PLANIFICATION" 					, T_PLANIFICATION_AFTER_CHECK_MAP_ENV);
	
     	
	}

	/**
	 * Utiliser dans la Satisfaction :
	 * @return
	 */
	public List<String> searchChemin_isAbandonTask () {
		ArrayList<String> cheminFind = new ArrayList<String>();
		
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
					new ArrayList<String>(nodeCloses), 10/*5*/);		
			
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
	
	public Integer 	getIdTanker () 					{return this.idTanker;}
	public Double 	getTauxSociabilite () 			{return this.tauxSociabilite;}
	public String	getPositionSilo ()				{return this.positionSilo;}
	public List<String> getKnowledgePositionSilo () {return this.bd_knowledgePositionSilo;}
	public Integer 	getCompteurSendPosition () 		{return this.compteurSendPosition;}
	public Integer  getIntervalleSendPositionSilo () {return this.INTERVALLE_SEND_POSITION;}
	public void  incrementeCompteurSilo () 			{this.compteurSendPosition++;}
	
	public void		setPositionSilo(String value) {this.positionSilo = value;}
	public void 	setResetCompteurSilo ()		  {this.compteurSendPosition = 1;}
	
}
