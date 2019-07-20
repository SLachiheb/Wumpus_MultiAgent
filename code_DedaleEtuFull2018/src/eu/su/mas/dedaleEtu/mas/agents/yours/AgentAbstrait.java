package eu.su.mas.dedaleEtu.mas.agents.yours;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graphstream.graph.Graph;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.PlanificationBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.Strategie;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.BehaviourCollecteur.ActionGestionInterblocageBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.EchoFlowding.AttendreConfirmationDad;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.EchoFlowding.AttendreConfirmationsFils;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.EchoFlowding.CheckEchoBoiteLettre;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.EchoFlowding.EchoFlowdingBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.EchoFlowding.PurgeDataEchoFlowdingBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.EchoFlowding.SendCarteDadBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.EchoFlowding.SendCarteSonsBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.EchoFlowding.SendConfirmerEchoDadBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.EchoFlowding.SendEchoBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.EchoFlowding.WaitCarteDadBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.EchoFlowding.WaitCarteSonsBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.Exploration.ExplorationBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.Exploration.FinExporationBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.ExplorationTresorPerdu.ExplorationTresorPerduBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.ExplorationTresorPerdu.TourTankerBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.OuvertureTresorCollectif.ActionGestionTresorCollectifBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.OuvertureTresorCollectif.CheckConfirmationProposalHelBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.OuvertureTresorCollectif.CheckProposalHelpBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.OuvertureTresorCollectif.SendProposalHelpBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.OuvertureTresorCollectif.SendValidationOuvertureTresorBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.RechercheTanker.CheckPositionSiloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.RechercheTanker.RecherchePositionSiloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.Satisfaction.AltruisteBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.Satisfaction.CheckSignauxBoiteLettreBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.Satisfaction.EgoisteBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.Satisfaction.SendEmissionRepulsionBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.SendMap.SendMAPBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.CarteDangers;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.CarteExploration;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.CarteTresors;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.DegreeNode;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.EchoFlowding.EchoFlowding;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Satisfaction.Identifiant;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Satisfaction.Satisfaction;
import eu.su.mas.dedaleEtu.princ.EntityType;
import jade.core.behaviours.FSMBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class AgentAbstrait extends AbstractDedaleAgent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5957519720175499609L;


	// 0.0) Data planification :
	protected 	CarteExploration 	myCarteExploration;		
	protected	CarteTresors		myCarteTresor;
	protected   CarteDangers        myCarteDanger;
	protected 	FSMBehaviour 		fsm;
	protected	DFAgentDescription  dfd;
	protected 	EntityType          typeAgent;
	/*Identifiant de l'agent*/
	protected 	static Integer		compteurAgent = 1;
	protected 	Integer 			idAgent;
	/*Comportement Exploration*/
	protected 	List<String> 		cheminBut;
	protected	boolean				isMove;
	protected 	Strategie			stategie;
	protected 	Integer				compteurMovement;	
	protected 	boolean				isExploration;
	/*Comportement EchoMap*/
	protected 	EchoFlowding		echoFlowding;
	protected 	Integer				compteurEchoFlowdingMap;
	protected	final Integer	    intervalEchoMAP = 50;
	protected 	boolean 			isCheckEcho;
	/*Comportement SendMap en cas d'interblocage*/
	protected		String			isInterblocagePos;		 	
	/*Comportement d'agregation des Maps*/
	protected 	boolean				shareMap;
	protected 	Identifiant			idShareMap;
	/*Comportement de la satisfaction*/
	protected 	Satisfaction		satisfaction;
	/*Comportement Pdm*/
	protected 	HashMap<String, String> 	positionTanker; // (Position, nomTanker)
	/*Ouverture coffre collectif*/
	protected	boolean				actionStatisfaction;
	protected	boolean				searchTresorCollectif;
	protected	String				posistionTresorCollectif;
	protected	boolean				attenteOpenTresor;
	/*Comportement ExplorationTresorPerdu :*/
	protected	boolean				isExplorationPerdu;
	protected 	boolean				isTourneeTanker;
	protected	Integer				cptNodeVisited;
	protected	Integer				cptTankerVisited;
	protected	Integer				intervalleNodeVisited;
	protected	List<String>		nodeOpenExploPerdu;
	protected	List<String>		nodeCloseExploPerdu;
	protected	Integer				nbNodeVisitedSilo;
	protected 	ArrayList<String>	nodeOpenSiloExploTresor;
	protected 	ArrayList<String>	nodeCloseSiloExploTresor;
	protected 	ArrayList<String>	tourTanker;
	protected 	String				tankerEnCours;
	/*Determiner la position du Silo à coup sûr :*/
	protected 	DegreeNode			degreeNode;
	/*Savoir si on a rencontré un trésor perdu durant l'exploration : */
	protected 	boolean				tresorsPerdus;
	
	protected 	boolean				abandon;
		
	// 1) Déclaration des noms des transitions de la FSM:
    public static final int P_EXPLORATION										= 0;
    public static final int T_EXPLORATEUR										= 1;
    public static final int P_P													= 2;
    public static final int P_SEND_MAP											= 3;
    public static final int P_PLANIFICATION_AFTER_SEND_MAP						= 4;
    // Transition pour EchoFlowding :
    public static final int T_ECHO_FLOWDING										= 5;
    public static final int T_CHECK_ECHO_BOITE_LETTRE							= 6;
    public static final int T_SEND_CONFIRMATION_DAD								= 7;
    public static final int T_ACCEPTER_TO_SEND_ECHO								= 8;
    public static final int A_SEND_ECHO											= 9;
    public static final int T_WAIT_ECHO											= 10;
    public static final int T_ECHO_TO_SEND_CARTE_DAD							= 11;
    public static final int T_WAIT_CARTE_DAD									= 12;
    public static final int T_WAIT_CARTE_SONS									= 13;
    public static final int T_SEND_CARTE_SONS									= 14;
    public static final int T_SEND_CARTE_SONS_INTERNE							= 15;
    public static final int T_SEND_CARTE_DAD									= 16;
    public static final int T_ECHO_TO_PRUGE_ECHOFLOWDING						= 17;
    public static final int T_SEND_CARTE_SONS_TO_PRUGE_ECHOFLOWDING				= 18;
    public static final int T_WAIT_CARTE_DAD_TO_PRUGE_ECHOFLOWDING				= 19;
    public static final int	T_PRUGE_ECHOFLOWDING_TO_PLANIFICATION				= 20;
    public static final int	T_PRUGE_ECHOFLOWDING_TO_END							= 21;
    public static final int	T_PRUGE_ECHOFLOWDING_TO_EXPLORATION					= 22;
    public static final int T_ATTENDRE_CONFIRMATION_FILS						= 23;
    public static final int	T_SEND_ECHO_AFTER_CONFIRMATION_DAD					= 24;
    public static final int T_ATTENDRE_CONFIRMATION_DAD							= 25;
    public static final int T_CHECK_ECHO_SECONDE_CHANCE							= 26;
    public static final int T_ATTENDRE_CONFIRMATION_FILS_AFTER_CHECK_ECHO 		= 27; 
    public static final int T_ATTENDRE_CONFIRMATION_FILS_AFTER_CONFIRMATION_DAD = 28;
    public static final int T_CHECK_ECHO_AFTER_PLANIFICATION 					= 29;
    // Transition pour Satisfaction :
    public static final int	T_CHECK_SIGNAUX_BOITE_LETTRE						= 30;
    public static final int	T_ALTRUISTE											= 31;
    public static final int	T_EGOISTE											= 32;
    public static final int	T_SEND_PROPAGATION_SIGNAL_REPULSIF					= 33;
    public static final int	T_SEND_SIGNAL_REPULSIF								= 34;
    public static final int	T_SEND_SIGNAL_TO_PLANIFICATION						= 35;
    public static final int	T_ATRUISTE_TO_PLANIFICATION							= 36;
    public static final int	T_EGOISTE_TO_PLANIFICATION							= 37;
    // Transition pour Ouverture de coffre colletctif :
    public static final int	T_ACTION_TRESOR_COLLECTIF_AFTER_PLANIFICATION		= 38;
    public static final int	T_CHECK_SIGNAL_AFTER_ACTION_TRESOR_COLLECTIF		= 39;
    public static final int	T_CHECK_PROPOSAL_HELP_AFTER_ACTION_TRESOR_COLLECTIF	= 40;
    public static final int	T_SEND_PROPOSAL_HELP_AFTER_ACTION_TRESOR_COLLECTIF	= 41;
    public static final int	T_SEND_VALIDE_TRESOR_AFTER_ACTION_TRESOR_COLLECTIF	= 42;
    public static final int	T_CHECK_CONFIRMATION_HELP							= 43;
    public static final int	T_CHECK_SIGNAL_AFTER_CHECK_CONFIRMATION_HELP		= 44;
    public static final int	T_CHECK_SIGNAL_AFTER_SEND_VALIDE_TRESOR				= 45;
    public static final int	T_CHECK_SIGNAL_AFTER_CHECK_PROPOSAL_HELP			= 46;
    // Transition pour Exploration Trésor Perdu :
    public static final int	T_EXPLO_TRESOR_PERDU								= 47;
    public static final int	T_TOUR_TANKER										= 48;
    public static final int	T_EXPLO_TRESOR_PERDU_AFTER_TOUR_TANKER				= 49;
    public static final int	T_CHECK_SIGNAUX_AFTER_TOUR_TANKER					= 50;
    
    public static final int	T_CHECK_POS_SILO									= 51;
    public static final int	T_CHECK_SIGNAL_AFTER_RECHERCHE_SILO					= 52;
    public static final int	T_RECHERCHE_POS_SILO								= 53;
    public static final int	T_CHECK_SIGNAUX_AFTER_CHECK_POS_SILO				= 54;
    public static final int	P_RECHERCHE_SILO									= 55;
    public static final int	T_CHECK_SIGNAUX_AFTER_EXPLO_TRESOR_PERDU  			= 56;  


    public AgentAbstrait () {
    	this.idAgent = AgentExplorateur.compteurAgent;
    	AgentExplorateur.compteurAgent++;
    	
    	this.nbNodeVisitedSilo 		= 1; /*************************************/
    	this.intervalleNodeVisited	= 30;/*************************************/
    	this.cptNodeVisited			= 0;
    	this.cptTankerVisited		= 0;
    }
    
	protected void setup(){

		super.setup();
		
		final Object[] args = getArguments();
        if (args[2]!=null){
        	System.out.println(args[0]);
            this.typeAgent = (EntityType) args[2];
        } else {
            System.err.println("Malfunction during parameter's loading of agent"
                    + this.getClass().getName());
            System.exit(-1);
        }
				
		// 3.0) Initialisation des datas Planifications :
		this.isMove					= false;
		this.stategie				= Strategie.Exploration;
		this.compteurMovement		= 0;
		// 3.1) Initialisations des datas connaissances de l'agent :
		this.myCarteExploration			= new CarteExploration(this);
		this.myCarteTresor 				= new CarteTresors();
		this.myCarteDanger				= new CarteDangers();
		// 3.2) Initialisations des datas de communication de l'agent :
		this.echoFlowding = new EchoFlowding(this);
		this.satisfaction = new Satisfaction(this); 
		// Declare que l'agent est en etat d'exploration au debut :
    	this.isExploration = true;
    	// Data pour connaitre si il y partage de carte :
    	this.shareMap = false;
    	this.idShareMap = null;
    	// Data pour connaitre si il y a besoin de check la boite d'echo :
    	this.isCheckEcho = false;
    	this.compteurEchoFlowdingMap = 0;
    	this.cheminBut = new ArrayList<String> ();
    	
    	this.positionTanker = new HashMap<String, String>();
    	// Ouverture coffre collectif :
    	this.actionStatisfaction		= false;
    	this.searchTresorCollectif 		= false;
    	this.posistionTresorCollectif 	= null;
    	this.attenteOpenTresor			= false;
    	// Comportement RechercheSilo + ExplorTresorPerdu :
    	this.nodeOpenExploPerdu  		= new ArrayList<String>();
    	this.nodeCloseExploPerdu		= new ArrayList<String>();
    	this.nodeOpenSiloExploTresor	= new ArrayList<String>();
    	this.nodeCloseSiloExploTresor	= new ArrayList<String>();
    	this.tourTanker					= new ArrayList<String>();
    	this.isExplorationPerdu			= false;
    	this.isTourneeTanker			= false;
    	//
		// Declaration de la FSM la FSM de base pour chaque agent :
		this.declaration_FSM();
		
		// Declaration du DFS :
		this.dfd = new DFAgentDescription();
	    DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd  = new ServiceDescription();
        sd.setType(this.typeAgent.toString());
        sd.setName(getLocalName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        
		System.out.println("the  agent "+this.getLocalName()+ " is started");
	}
	
	public void declaration_FSM() {
		// 5) Déclaration des comportements de l'agentExplorateur :
        ExplorationBehaviour 					exploration 			= new ExplorationBehaviour(this);
        PlanificationBehaviour  				planification   		= new PlanificationBehaviour(this);
        FinExporationBehaviour  				finExploration	 		= new FinExporationBehaviour(this);
        SendMAPBehaviour						sendMap					= new SendMAPBehaviour(this);
        // Comportement EchoFlowding:
        CheckEchoBoiteLettre					checkBoiteLettre		= new CheckEchoBoiteLettre(this);
        SendEchoBehaviour						sendEcho				= new SendEchoBehaviour(this);
        SendConfirmerEchoDadBehaviour			sendConfirmerEchoDad	= new SendConfirmerEchoDadBehaviour(this);
        AttendreConfirmationDad					attendreConfirmationDad	= new AttendreConfirmationDad(this);
        AttendreConfirmationsFils				attendreConfirmationFils= new AttendreConfirmationsFils(this);
        //---
        EchoFlowdingBehaviour					echoFlowding			= new EchoFlowdingBehaviour(this);
        WaitCarteDadBehaviour					waitCarteDad			= new WaitCarteDadBehaviour(this);
        WaitCarteSonsBehaviour 					waitCarteSons			= new WaitCarteSonsBehaviour(this);
        SendCarteDadBehaviour					sendCarteDad			= new SendCarteDadBehaviour(this);
        SendCarteSonsBehaviour					sendCarteSons			= new SendCarteSonsBehaviour(this);
        PurgeDataEchoFlowdingBehaviour			purgeEchoFlowding		= new PurgeDataEchoFlowdingBehaviour(this);
        // Comportement Signaux Interblocage :
        CheckSignauxBoiteLettreBehaviour 		checkSignaux		    = new CheckSignauxBoiteLettreBehaviour(this);
        AltruisteBehaviour				 		altruiste				= new AltruisteBehaviour(this);
        EgoisteBehaviour				 		egoiste					= new EgoisteBehaviour(this);
        SendEmissionRepulsionBehaviour	 		sendRepulsion			= new SendEmissionRepulsionBehaviour(this);
        // Comportement pour Ouverture de coffre colletctif :
		ActionGestionInterblocageBehaviour		actionInterblocage		= new ActionGestionInterblocageBehaviour(this);
        ActionGestionTresorCollectifBehaviour	actionCollectif			= new ActionGestionTresorCollectifBehaviour(this);
        CheckProposalHelpBehaviour				checkProposalHelp		= new CheckProposalHelpBehaviour(this);
        SendProposalHelpBehaviour				sendProposalHelp		= new SendProposalHelpBehaviour(this);
        SendValidationOuvertureTresorBehaviour	sendValideTresor		= new SendValidationOuvertureTresorBehaviour(this);
        CheckConfirmationProposalHelBehaviour	checkConfirmationHelp	= new CheckConfirmationProposalHelBehaviour(this);
        // Comportement Exploration Trésor Perdu :
        ExplorationTresorPerduBehaviour			exploTresorPerdu		= new ExplorationTresorPerduBehaviour(this);
        TourTankerBehaviour						tourTanker				= new TourTankerBehaviour(this);
        RecherchePositionSiloBehaviour			rechercheSilo			= new RecherchePositionSiloBehaviour(this);
        CheckPositionSiloBehaviour				checkPosSilo			= new CheckPositionSiloBehaviour(this);
        
        // 6) Déclaration de la machine d'etat de l'agentExplorateur :
        this.fsm = new FSMBehaviour(this);
        this.fsm.registerFirstState(exploration			, "EXPLORATION");
        this.fsm.registerState(planification			, "PLANIFICATION");
		this.fsm.registerState(finExploration			, "END_EXPLORATION");
		this.fsm.registerState(sendMap					, "SEND_MAP");
        // Comportement EchoFlowding:
        this.fsm.registerState(checkBoiteLettre 		, "CHECK_BOITE_LETTRE");
        this.fsm.registerState(sendConfirmerEchoDad		, "SEND_CONFIRMER_DAD");
        this.fsm.registerState(attendreConfirmationDad	, "ATTENDRE_CONFIRMER_DAD");
        this.fsm.registerState(attendreConfirmationFils	, "ATTENDRE_CONFIRMER_FILS");
        this.fsm.registerState(sendEcho	        		, "SEND_ECHO");
        this.fsm.registerState(echoFlowding	    		, "ECHO_FLOWDING");
        this.fsm.registerState(waitCarteDad	    		, "WAIT_CARTE_DAD");
        this.fsm.registerState(waitCarteSons    		, "WAIT_CARTE_SONS");
        this.fsm.registerState(sendCarteDad    			, "SEND_CARTE_DAD");
        this.fsm.registerState(sendCarteSons    		, "SEND_CARTE_SONS");
        this.fsm.registerState(purgeEchoFlowding   		, "PURGE_ECHO_FLOWDING");
        // Comportement Signaux Interblocage :
        this.fsm.registerState(checkSignaux	        	, "CHECK_SIGNAUX");
        this.fsm.registerState(altruiste	        	, "ALTRUISME");
        this.fsm.registerState(egoiste 		        	, "EGOISTE");
        this.fsm.registerState(sendRepulsion 			, "SEND_REPULSION");
        // Comportement pour Ouverture de coffre colletctif :
        this.fsm.registerState(actionInterblocage		, "A_INTERBLOCAGE");
        this.fsm.registerState(actionCollectif			, "ACTION_COLLECTIF");
        this.fsm.registerState(checkProposalHelp		, "CHECK_PROPOSAL_HELP");
        this.fsm.registerState(sendProposalHelp			, "SEND_PROPOSAL_HELP");
        this.fsm.registerState(sendValideTresor			, "SEND_VALIDE_TRESOR");
        this.fsm.registerState(checkConfirmationHelp	, "CHECK_CONFIRMATION_HELP");
        // Comportement Exploration Trésor Perdu :
        this.fsm.registerState(exploTresorPerdu			, "EXPLO_TRESOR_PERDU");
        this.fsm.registerState(tourTanker				, "TOUR_TANKER");
        this.fsm.registerState(rechercheSilo			, "RECHERCHE_SILO");
        this.fsm.registerState(checkPosSilo				, "CHECK_POS_SILO");

        
		// 7) Déclaration des transitions de la FSM :
		this.fsm.registerTransition("EXPLORATION"  			, "PLANIFICATION"  			, T_EXPLORATEUR);
		this.fsm.registerTransition("PLANIFICATION"			, "EXPLORATION"    			, P_EXPLORATION);
		this.fsm.registerTransition("PLANIFICATION"  		, "PLANIFICATION"			, P_P);
		this.fsm.registerTransition("PLANIFICATION"  		, "SEND_MAP"				, P_SEND_MAP);
		this.fsm.registerTransition("SEND_MAP"  			, "PLANIFICATION"			, P_PLANIFICATION_AFTER_SEND_MAP);
		// Comportement EchoFlowding:
		this.fsm.registerTransition("PLANIFICATION"			, "CHECK_BOITE_LETTRE"  	, T_CHECK_ECHO_BOITE_LETTRE);
		this.fsm.registerTransition("CHECK_BOITE_LETTRE"	, "PLANIFICATION"  			, T_CHECK_ECHO_AFTER_PLANIFICATION);
		this.fsm.registerTransition("CHECK_BOITE_LETTRE"	, "SEND_ECHO"      			, A_SEND_ECHO);
		this.fsm.registerTransition("CHECK_BOITE_LETTRE"	, "SEND_CONFIRMER_DAD"      , T_SEND_CONFIRMATION_DAD);
		this.fsm.registerTransition("SEND_CONFIRMER_DAD"	, "ATTENDRE_CONFIRMER_DAD"  , T_ATTENDRE_CONFIRMATION_DAD);
		this.fsm.registerTransition("ATTENDRE_CONFIRMER_DAD", "SEND_ECHO"  				, T_SEND_ECHO_AFTER_CONFIRMATION_DAD);
		this.fsm.registerTransition("SEND_ECHO"				, "ATTENDRE_CONFIRMER_FILS" , T_ATTENDRE_CONFIRMATION_FILS);
		this.fsm.registerTransition("ATTENDRE_CONFIRMER_FILS", "ECHO_FLOWDING" 			, T_ECHO_FLOWDING);
		this.fsm.registerTransition("ATTENDRE_CONFIRMER_FILS", "CHECK_BOITE_LETTRE" 	, T_CHECK_ECHO_SECONDE_CHANCE);
		this.fsm.registerTransition("CHECK_BOITE_LETTRE"	, "ATTENDRE_CONFIRMER_FILS" , T_ATTENDRE_CONFIRMATION_FILS_AFTER_CHECK_ECHO);
		this.fsm.registerTransition("ATTENDRE_CONFIRMER_DAD", "ATTENDRE_CONFIRMER_FILS" , T_ATTENDRE_CONFIRMATION_FILS_AFTER_CONFIRMATION_DAD);
		this.fsm.registerTransition("ECHO_FLOWDING" 		, "SEND_CARTE_DAD"      	, T_ECHO_TO_SEND_CARTE_DAD);
		this.fsm.registerTransition("ECHO_FLOWDING" 		, "WAIT_CARTE_SONS"      	, T_WAIT_CARTE_SONS);
		this.fsm.registerTransition("ECHO_FLOWDING" 		, "PURGE_ECHO_FLOWDING"     , T_ECHO_TO_PRUGE_ECHOFLOWDING);
		this.fsm.registerTransition("WAIT_CARTE_SONS" 		, "SEND_CARTE_DAD"      	, T_SEND_CARTE_DAD);
		this.fsm.registerTransition("SEND_CARTE_DAD" 		, "WAIT_CARTE_DAD"      	, T_WAIT_CARTE_DAD);
		this.fsm.registerTransition("WAIT_CARTE_SONS" 		, "SEND_CARTE_SONS"      	, T_SEND_CARTE_SONS);
		this.fsm.registerTransition("WAIT_CARTE_DAD"  		, "SEND_CARTE_SONS"      	, T_SEND_CARTE_SONS_INTERNE);
		this.fsm.registerTransition("SEND_CARTE_SONS" 		, "PURGE_ECHO_FLOWDING"     , T_SEND_CARTE_SONS_TO_PRUGE_ECHOFLOWDING);
		this.fsm.registerTransition("WAIT_CARTE_DAD" 		, "PURGE_ECHO_FLOWDING"		, T_WAIT_CARTE_DAD_TO_PRUGE_ECHOFLOWDING);
		this.fsm.registerTransition("PURGE_ECHO_FLOWDING" 	, "PLANIFICATION"      		, T_PRUGE_ECHOFLOWDING_TO_PLANIFICATION);
		this.fsm.registerTransition("PURGE_ECHO_FLOWDING"  	, "EXPLORATION"				, T_PRUGE_ECHOFLOWDING_TO_EXPLORATION);
		// Comportement Signaux Interblocage :
		this.fsm.registerTransition("PLANIFICATION"         , "CHECK_SIGNAUX"           , T_CHECK_SIGNAUX_BOITE_LETTRE);
		this.fsm.registerTransition("CHECK_SIGNAUX"         , "ALTRUISME"			    , T_ALTRUISTE);
		this.fsm.registerTransition("CHECK_SIGNAUX"         , "EGOISTE"    				, T_EGOISTE);
		this.fsm.registerTransition("ALTRUISME"         	, "SEND_REPULSION"			, T_SEND_PROPAGATION_SIGNAL_REPULSIF);
		this.fsm.registerTransition("EGOISTE"         		, "SEND_REPULSION"			, T_SEND_SIGNAL_REPULSIF);
		this.fsm.registerTransition("ALTRUISME"         	, "PLANIFICATION"			, T_ATRUISTE_TO_PLANIFICATION);
		this.fsm.registerTransition("EGOISTE"         		, "PLANIFICATION"			, T_EGOISTE_TO_PLANIFICATION);
		this.fsm.registerTransition("SEND_REPULSION"        , "PLANIFICATION"			, T_SEND_SIGNAL_TO_PLANIFICATION);	
		// Comportement Ouverture collectif :
		this.fsm.registerTransition("PLANIFICATION"         , "ACTION_COLLECTIF"        , T_ACTION_TRESOR_COLLECTIF_AFTER_PLANIFICATION);
		this.fsm.registerTransition("ACTION_COLLECTIF"      , "CHECK_SIGNAUX"       	, T_CHECK_SIGNAL_AFTER_ACTION_TRESOR_COLLECTIF);
		this.fsm.registerTransition("ACTION_COLLECTIF"      , "CHECK_PROPOSAL_HELP"     , T_CHECK_PROPOSAL_HELP_AFTER_ACTION_TRESOR_COLLECTIF);
		this.fsm.registerTransition("ACTION_COLLECTIF"      , "SEND_PROPOSAL_HELP"      , T_SEND_PROPOSAL_HELP_AFTER_ACTION_TRESOR_COLLECTIF);
		this.fsm.registerTransition("ACTION_COLLECTIF"      , "SEND_VALIDE_TRESOR"      , T_SEND_VALIDE_TRESOR_AFTER_ACTION_TRESOR_COLLECTIF);	
		this.fsm.registerTransition("SEND_PROPOSAL_HELP"    , "CHECK_CONFIRMATION_HELP" , T_CHECK_CONFIRMATION_HELP);
		this.fsm.registerTransition("CHECK_CONFIRMATION_HELP", "CHECK_SIGNAUX" 			, T_CHECK_SIGNAL_AFTER_CHECK_CONFIRMATION_HELP);
		this.fsm.registerTransition("SEND_VALIDE_TRESOR"	, "CHECK_SIGNAUX" 			, T_CHECK_SIGNAL_AFTER_SEND_VALIDE_TRESOR);
		this.fsm.registerTransition("CHECK_PROPOSAL_HELP"   , "CHECK_SIGNAUX"     		, T_CHECK_SIGNAL_AFTER_CHECK_PROPOSAL_HELP);
		/*Comportement Exploration Tresor Perdu :*/
		this.fsm.registerTransition("PLANIFICATION"   		, "EXPLO_TRESOR_PERDU"     	, T_EXPLO_TRESOR_PERDU);
		this.fsm.registerTransition("PLANIFICATION"   		, "TOUR_TANKER"     		, T_TOUR_TANKER);
		this.fsm.registerTransition("TOUR_TANKER"   		, "EXPLO_TRESOR_PERDU"     	, T_EXPLO_TRESOR_PERDU_AFTER_TOUR_TANKER);
		this.fsm.registerTransition("TOUR_TANKER"   		, "CHECK_SIGNAUX"     		, T_CHECK_SIGNAUX_AFTER_TOUR_TANKER);
		this.fsm.registerTransition("EXPLO_TRESOR_PERDU"   	, "CHECK_SIGNAUX"     		, T_CHECK_SIGNAUX_AFTER_EXPLO_TRESOR_PERDU);

		
		this.fsm.registerTransition("PLANIFICATION"   		, "CHECK_POS_SILO"     		, P_RECHERCHE_SILO);
		this.fsm.registerTransition("RECHERCHE_SILO"   		, "CHECK_POS_SILO"     		, T_CHECK_POS_SILO);
		this.fsm.registerTransition("RECHERCHE_SILO"   		, "CHECK_SIGNAUX"     		, T_CHECK_SIGNAL_AFTER_RECHERCHE_SILO);
		this.fsm.registerTransition("CHECK_POS_SILO"   		, "RECHERCHE_SILO"     		, T_RECHERCHE_POS_SILO);
		this.fsm.registerTransition("CHECK_POS_SILO"   		, "CHECK_SIGNAUX"     		, T_CHECK_SIGNAUX_AFTER_CHECK_POS_SILO);	
	}
	
	
	public void beforeMove(){
		super.beforeMove();
		System.out.println("Save everything (and kill GUI) before move");
		this.myCarteExploration.prepareMigration(); 
	}
	
	public void afterMove(){
		super.afterMove();
		this.myCarteExploration.loadSavedData();
		System.out.println("Restore data (and GUI) after moving");
	}
	
	public void resetShareMap () {
		this.shareMap = false;
		this.idShareMap = null;
	}
	
	public void rebootInterblocage() {
		if (this.getInterblocagePos() != null) {
			this.setInterblocagePos(null);
		}
	}
	
	public void attendre () {
		/*try {
			this.doWait(400);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}
	
	/**
	 * MaJ de la carte des Tresors et des Dangers à chaque pas :
	 */
	public void updateCarte() {

		//Liste des observables à partir de la position actuelle de l'agent
		List<Couple<String,List<Couple<Observation,Integer>>>> lobs= this.observe();	
		
		//2) Vérification des mises à jour des attributs du noeud + Carte au tresor:
		//Liste des observations associés à la position actuel de l'agent :
		List<Couple<Observation,Integer>> lObservations= lobs.get(0).getRight();
		if (lObservations.isEmpty() == false) {
			for (Couple<Observation,Integer> obs : lObservations) {
				if (obs.getLeft() == Observation.LOCKSTATUS) {
					System.out.println("\n*******************> " + this.getLocalName());
					//Mise à jour de la carte au trésor :
					this.getCarteTresors().addTresors(this.getCurrentPosition(), lObservations);
					// Mise a jour de la carte de Danger 
					this.getCarteDangers().addDangers(this.getCurrentPosition(), lObservations);
				}
			}	
		}		
		
		if (this.getCarteTresors().getCarteTresors().containsKey(this.getCurrentPosition()) == true) {
			System.out.println("\n*******************> " + this.getLocalName());
			//Mise à jour de la carte au trésor :
			this.getCarteTresors().addTresors(this.getCurrentPosition(), lObservations);
		}
	}
	
	/**
	 * GETTER
	 */
	public Strategie getStrategie() {
		return this.stategie;
	}
	public boolean 		 getIsMove() {
		return this.isMove;
	}
	public EntityType getTypeAgent() {
		return this.typeAgent;
	}
	public CarteExploration getCarteExploration() {
		return this.myCarteExploration;
	}
	public CarteTresors getCarteTresors() { 
		return this.myCarteTresor; 
	}
	public CarteDangers getCarteDangers() {
		return this.myCarteDanger;
	}
	public EchoFlowding getEchoFlowding () {
		return this.echoFlowding;
	}
	public Satisfaction getSatisfaction () {
		return this.satisfaction;
	}
	public Integer getCompteurMouvement() {
		return this.compteurMovement;
	}
	public List<String> getNodesBut () {
		return this.cheminBut;
	}
	public Integer getIdAgent() {
		return this.idAgent;
	}
	public boolean getExploration() {
		return this.isExploration;
	}
	public Integer getCptEchoFlowdingMap () {
		return this.compteurEchoFlowdingMap;
	}
	public Integer getIntervalleEchoMap () {
		return this.intervalEchoMAP;
	}
	public boolean getShareMap () {
		return this.shareMap;
	}
	public Identifiant getIdentifiantShareMap () {
		return this.idShareMap;
	}
	public boolean getCheckEcho () {
		return this.isCheckEcho;
	}
	public String getInterblocagePos () { 
		return this.isInterblocagePos;
	}
	public HashMap<String, String> getPositionTanker () {
		return this.positionTanker;
	}
	public boolean				getActionSatisfaction()			{return this.actionStatisfaction;}
	public boolean				getSearchTresorCollectif()		{return this.searchTresorCollectif;}
	public String 				getPosistionTresorCollectif() 	{return this.posistionTresorCollectif;}
	public boolean				getAttenteTresor()				{return this.attenteOpenTresor;}
	public boolean				getExplorationTresorPerdu()		{return this.isExplorationPerdu;}
	public List<String>			getNodeCloseExploTresor() 		{return this.nodeCloseExploPerdu;}
	public List<String>			getNodeOpenExploTresor() 		{return this.nodeOpenExploPerdu;}
	public Integer				getIntervalleNodeExploTresor() 	{return this.intervalleNodeVisited;}
	public Integer				getNbNodeVisitedSilo()  		{return this.nbNodeVisitedSilo;}
	public ArrayList<String>	getNodeCloseSiloExploTresor()	{return this.nodeCloseSiloExploTresor;}
	public ArrayList<String>	getNodeOpenSiloExploTresor()	{return this.nodeOpenSiloExploTresor;}
	public String				getTankerVisited () 			{return this.tankerEnCours;}
	public DegreeNode			getDegreeNode()					{return this.degreeNode;}
	public boolean				getTresorPerdu()				{return this.tresorsPerdus;}
	
	public boolean				getAbandon()					{return this.abandon;}
	public void					setAbandon(boolean value)		{this.abandon = value;}

	
	/**
	 * SETTER
	 */
	public void setIsMove(boolean isMove) {
		this.isMove = isMove;
	}
	public void setCheminBut(List<String> cheminBut) {
		// Mise à jour du chemin dans les donnée de l'agent :
		this.cheminBut = cheminBut;
		// Mise à jour de l'etat but dans la classe de satisfication :
		this.satisfaction.newCheminBut(cheminBut);
		// MaJ de la variable d'interblocage dans SendMap:
		this.rebootInterblocage();
	}	
	public void setCheminButFromSatisfaction (List<String> cheminBut) {
		// Mise à jour du chemin dans les donnée de l'agent :
		this.cheminBut = cheminBut;
		// MaJ de la variable d'interblocage dans SendMap:
		this.rebootInterblocage();
	}
	public void setCheminPlanification (List<String> cheminBut) {
		// Reset des données de la satisfaction sans la base de donnée :
		this.satisfaction.reset();
		// Mise à jour du chemin dans les donnée de l'agent :
		this.setCheminBut(cheminBut);
		// MaJ de la variable d'interblocage dans SendMap:
		this.rebootInterblocage();
	}
	public void cheminButRemove(int index) {
		this.cheminBut.remove(index);
	}
	public void setStrategie(Strategie b) {
		this.stategie = b;
	}
	public void setCompteurMouvement(Integer v) {
		this.compteurMovement = v;
	}
	public void setIsExploration (boolean value) {
		this.isExploration = value;
	}
	public void setCptEchoMapIncrementer () {
		this.compteurEchoFlowdingMap += 1;
	}
	public void setRebootEchoMap () {
		// 1 pour ne pas faire l'echoMap à la prochaine itérarion :
		this.compteurEchoFlowdingMap = 1;
	}
	public void setShareMap (boolean value) {
		this.shareMap = value;
	}
	public void setIdentifiantShareMap (Identifiant value) {
		this.idShareMap = value;
	}
	public void setCheckEcho (boolean value) {
		this.isCheckEcho = value;
	}
	public void setInterblocagePos (String value) {
		this.isInterblocagePos = value;
	}
	public void					setActionSatisfaction(boolean value)	{this.actionStatisfaction = value;}
	public void 				setSearchTresorCollectif(boolean value) {this.searchTresorCollectif = value;}
	public void					setPosistionTresorCollectif (String s)	{this.posistionTresorCollectif = s;}
	public void					setAttenteTresor(boolean value) 		{this.attenteOpenTresor = value;}
	public void					setExplorationTresorPerdu(boolean value) 		{this.isExplorationPerdu = value;}
	public void 				setNodeOpenSiloExploTresor(ArrayList<String> list) 	{this.nodeOpenSiloExploTresor = new ArrayList<String>(list);} 
	public void					setVisitedTanker(String value) 			{this.tankerEnCours = value;}
	public void					setTourTanker (boolean value) 				{this.isTourneeTanker = value;}
	public void					setTresorPerdu(boolean value)				{this.tresorsPerdus = value;}

	
	public Integer getCptNodeVisited () {return this.cptNodeVisited;}	
	public void incrementeCptNodeVisited () {this.cptNodeVisited++;}
	public void rebootCptNodeVisited () {this.cptNodeVisited = 1;}
	
	public List<String>	getTourTanker() {return this.tourTanker;}
	public void	setTourTanker(HashMap<String, String> list) {
		this.tourTanker = new ArrayList<String>();
		for (Map.Entry<String, String> entry : list.entrySet()) {
			this.tourTanker.add(entry.getValue());
		}
	}
	public void setDegreeNode (Graph g) {this.degreeNode = new DegreeNode (g);}
}
