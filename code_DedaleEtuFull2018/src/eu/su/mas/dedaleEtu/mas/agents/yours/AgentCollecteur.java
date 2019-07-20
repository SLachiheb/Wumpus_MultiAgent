package eu.su.mas.dedaleEtu.mas.agents.yours;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.BehaviourCollecteur.ActionGestionTankerBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.BehaviourCollecteur.ActionGestionTresorIndividuelBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.BehaviourCollecteur.CheckPositionSiloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.BehaviourCollecteur.PDMBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.BehaviourCollecteur.RecherchePositionSiloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.BehaviourCollecteur.SendMapEnvironnementSiloBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.ProcessusDecisionMarkovien.Action;
import jade.core.behaviours.Behaviour;

public class AgentCollecteur extends AgentAbstrait{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3617631381011067969L;
	
    public static final int	T_PDM										= 57;
    public static final int	T_CHECK_POSITION_SILO						= 58;
    public static final int	T_CHECK_SIGNAL_AFTER_CHECK_POSITION_SILO	= 59;
    public static final int	T_CHECK_SIGNAL_AFTER_RECHERCHE_POSITION_SILO= 60;
    public static final int	T_RECHERCHE_POSITION_SILO					= 61;
    
    public static final int	T_ACTION_INTERBLOCAGE						= 62;
    public static final int	T_ACTION_TRESOR_INDIVIDUEL					= 63;
    public static final int	T_ACTION_TRESOR_COLLECTIF					= 64;
    public static final int	T_ACTION_TANKER								= 65;
    
    public static final int	T_CHECK_SIGNAL_AFTER_TRESOR_IND				= 66;
    public static final int	T_PDM_AFTER_INTERBLOCAGE					= 67;
    public static final int	T_CHECK_SIGNAL_AFTER_INTERBLOCAGE			= 68;
    
    public static final int	T_SEND_MAP_ENV								= 69;
    public static final int	T_CHECK_SIGNAL_AFTER_A_TANKER				= 70;
    public static final int	T_CHECK_SIGNAL_AFTER_SEND_MAP_ENV			= 71;
    public static final int	T_CHECK_SIGNAUX_AFTER_PDM					= 72;
    
	// Data de l'agent Collectionneur :
	protected 	Double 					tauxSociabilite;
	protected 	ArrayList<String>		nodeOpenSilo;
	protected 	ArrayList<String>		nodeCloseSilo;
	protected   Random					random;
	protected 	Action					actionPDM;
	protected 	Integer					capacitySac;
	protected	boolean					isCollectEnd;
	protected	boolean					attenteTanker;

	
    public AgentCollecteur () {
    	this.tauxSociabilite 	= 1.;
    	this.nodeCloseSilo 		= new ArrayList<String>();
    	this.nodeOpenSilo  		= new ArrayList<String>();
    	this.random				= new Random();
    	this.actionPDM			= null;
    	this.capacitySac		= 0;
    	this.isCollectEnd		= false;
    	this.attenteTanker		= false;
    }
    
	protected void setup(){

		super.setup();
     
		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		declaration_FSM_Collecteur();
		
		lb.add(this.fsm);
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		
		addBehaviour(new startMyBehaviours(this,lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");
	}

	public void declaration_FSM_Collecteur() {
		PDMBehaviour 							pdm 				= new PDMBehaviour(this);
		CheckPositionSiloBehaviour				checkPositionSilo 	= new CheckPositionSiloBehaviour(this);
		RecherchePositionSiloBehaviour			rechercheSilo		= new RecherchePositionSiloBehaviour(this);
		ActionGestionTresorIndividuelBehaviour	actionTresorInd		= new ActionGestionTresorIndividuelBehaviour(this);
		ActionGestionTankerBehaviour			actionTanker		= new ActionGestionTankerBehaviour(this);
		SendMapEnvironnementSiloBehaviour		sendMapEnv			= new SendMapEnvironnementSiloBehaviour(this);
		
		// Déclaration des états de la FSM :
        this.fsm.registerState(pdm					, "PDM");
        this.fsm.registerState(checkPositionSilo	, "CHECK_POSITION_SILO");
        this.fsm.registerState(rechercheSilo		, "RECHERCHE_POSITION_SILO");
        this.fsm.registerState(actionTresorInd		, "A_TRESOR_INDIVIDUEL");
        this.fsm.registerState(actionTanker			, "A_TANKER");
        this.fsm.registerState(sendMapEnv			, "SEND_MAP_ENV");

        // Déclaration des transitions de la FSM :
     	this.fsm.registerTransition("PLANIFICATION"  			, "PDM" 					, T_PDM);
     	this.fsm.registerTransition("CHECK_POSITION_SILO"  		, "CHECK_SIGNAUX" 			, T_CHECK_SIGNAL_AFTER_CHECK_POSITION_SILO);
     	this.fsm.registerTransition("CHECK_POSITION_SILO"  		, "RECHERCHE_POSITION_SILO" , T_RECHERCHE_POSITION_SILO);
     	this.fsm.registerTransition("RECHERCHE_POSITION_SILO"  	, "CHECK_SIGNAUX" 			, T_CHECK_SIGNAL_AFTER_RECHERCHE_POSITION_SILO);
     	this.fsm.registerTransition("PDM"  						, "CHECK_POSITION_SILO" 	, T_CHECK_POSITION_SILO);

     	this.fsm.registerTransition("PDM"					  	, "A_INTERBLOCAGE" 			, T_ACTION_INTERBLOCAGE);
     	this.fsm.registerTransition("PDM"					  	, "A_TRESOR_INDIVIDUEL" 	, T_ACTION_TRESOR_INDIVIDUEL);
     	this.fsm.registerTransition("PDM"					  	, "ACTION_COLLECTIF" 		, T_ACTION_TRESOR_COLLECTIF);
     	this.fsm.registerTransition("PDM"					  	, "A_TANKER" 				, T_ACTION_TANKER);
     	this.fsm.registerTransition("PDM"					  	, "CHECK_SIGNAUX" 			, T_CHECK_SIGNAUX_AFTER_PDM);

     	this.fsm.registerTransition("A_TRESOR_INDIVIDUEL"		, "CHECK_SIGNAUX" 			, T_CHECK_SIGNAL_AFTER_TRESOR_IND);
     	this.fsm.registerTransition("A_INTERBLOCAGE"			, "PDM" 					, T_PDM_AFTER_INTERBLOCAGE);
     	this.fsm.registerTransition("A_INTERBLOCAGE"			, "CHECK_SIGNAUX" 			, T_CHECK_SIGNAL_AFTER_INTERBLOCAGE);

     	this.fsm.registerTransition("A_TANKER"					, "SEND_MAP_ENV" 			, T_SEND_MAP_ENV);
     	this.fsm.registerTransition("A_TANKER"					, "CHECK_SIGNAUX" 			, T_CHECK_SIGNAL_AFTER_A_TANKER);

     	this.fsm.registerTransition("SEND_MAP_ENV"				, "CHECK_SIGNAUX" 			, T_CHECK_SIGNAL_AFTER_SEND_MAP_ENV);
	}
	
	/**
	 * Utiliser dans la Satisfaction :
	 * @return
	 */
	public List<String> searchChemin_isAbandonTask () {
		//System.out.println("*****" + this.getLocalName() + " : ABANDON");

		ArrayList<String> cheminFind = new ArrayList<String>();
		
		// Mettre à jour la satisfaction :
		this.setActionSatisfaction(true);
		// Annuler l'action en cours :
		this.setActionPDM(null);
		
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
					new ArrayList<String>(nodeCloses), (nodeCloses.size()/* / 2*/)/*5*/);		
			
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
		//System.out.println("*****" + this.getLocalName() + " : ABANDON pour aller en " + cheminFind);
		return cheminFind;
	}
	public void					updateCapacitySac(Integer value) {this.capacitySac = value;}
	
	public Double 				getTauxSociabilite () 	{return this.tauxSociabilite;}
	public ArrayList<String>	getNodeCloseSilo()		{return this.nodeCloseSilo;}
	public ArrayList<String>	getNodeOpenSilo()		{return this.nodeOpenSilo;}
	public Action				getActionPDM()			{return this.actionPDM;}
	public Integer				getCapacityBag()		{return this.capacitySac;}
	public boolean				getIsCollectEnd()		{return this.isCollectEnd;}
	public boolean				getAttenteTanker()		{return this.attenteTanker;}
	
	
	public void 				setNodeOpenSilo(ArrayList<String> list) 	{this.nodeOpenSilo = list;} 
	public void					setActionPDM(Action action)				{this.actionPDM = action;}
	public void 				setIsCollectEnd(boolean value)			{this.isCollectEnd = value;}
	public void 				setAttenteTanker(boolean value)			{this.attenteTanker	= value;}
}
