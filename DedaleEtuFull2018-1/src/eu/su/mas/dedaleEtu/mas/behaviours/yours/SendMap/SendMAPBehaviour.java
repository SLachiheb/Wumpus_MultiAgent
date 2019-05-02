package eu.su.mas.dedaleEtu.mas.behaviours.yours.SendMap;

import java.io.IOException;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentExplorateur;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.Strategie;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.Carte;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.SerializableCarteExploration;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Satisfaction.Identifiant;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class SendMAPBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1395134725502394208L;
	private AgentAbstrait 	agent;
	
	public SendMAPBehaviour (final AgentAbstrait agentAbstrait) {
		super(agentAbstrait);
		this.agent = agentAbstrait;
	}
	@Override
	public void action() {
	
		// Modification du comportement :
		this.agent.setStrategie(Strategie.SendMap);
		// 1) L'agent doit envoyer sa Carte d'exploration :
		// Creation d'un message :
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("SEND_MAP");
		// Modifier l'expéditeur du message :
		msg.setSender(this.getAgent().getAID());
		// Ajouter les destinataires :
		// 2) Recherche des agents dans le DFS : 
		DFAgentDescription description = new DFAgentDescription();
		DFAgentDescription[] resultats;
		try {
			resultats = DFService.search(this.getAgent(), description);
		    if (resultats.length > 0) {
		        for (DFAgentDescription agent : resultats) {
		        	// Ne communique pas avec lui même:
		            if (agent.getName().equals(this.getAgent().getAID()) == false ){
		            	msg.addReceiver(agent.getName());
		            }
		        }
		    }
		} catch (FIPAException e1) {
			e1.printStackTrace();
		}
		// Rendre le carteExploration serializable :
		SerializableCarteExploration carteExploSerializable = new SerializableCarteExploration
				(this.agent.getCarteExploration());        

		// Creation de l'identifiant :
		Identifiant id = new Identifiant(this.agent.getLocalName(), this.agent.getIdAgent());

		// Creation de la carte :
		Carte msgCarte = new Carte(id, carteExploSerializable , this.agent.getCarteTresors(),
				this.agent.getCarteDangers());
		try {
			msg.setContentObject(msgCarte);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Envoie de la carte au destinataire qui est le père :
		this.agent.sendMessage(msg);	

		// 2) L'agent vérifie sa boite au lettre :
		// Check les messages de type EchoMAP :
		MessageTemplate msgTemplateEcho = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchProtocol("SEND_MAP"));

		ACLMessage msgRecuMap = this.agent.blockingReceive(msgTemplateEcho, 500);

		while(msgRecuMap != null) {
			//System.out.println("\n************Interblocage Reception Map : " + this.agent.getLocalName());
			// MAJ de la carte :
			try {
				// Extrait le contenu du message dans msgCarte :
				Carte msgCarte1 = (Carte)msgRecuMap.getContentObject();
				
				// Exterieur :
		        //System.out.println("AGENTX " + this.agent.getLocalName() + msgCarte1.getCarteExplorations());
				
				// Avant :
				//SerializableCarteExploration carteExploSerializable1 = new SerializableCarteExploration
		        //		(this.agent.getCarteExploration());
		        //System.out.println("AVANT " + this.agent.getLocalName() + carteExploSerializable1);
				
				// MAJ des cartes par insertion :
				msgCarte1.updateInsertCartes(this.agent.getCarteExploration(),
						this.agent.getCarteTresors(), 
						this.agent.getCarteDangers());
				
				// Apres :
				//SerializableCarteExploration carteExploSerializable2 = new SerializableCarteExploration
		        //		(this.agent.getCarteExploration());
		        //System.out.println("APRES " +this.agent.getLocalName()  + carteExploSerializable2);

				
				// Active Echo MAp seulement si il y a eu des changements dans la carte :
				if (this.agent.getCarteExploration().getChangeMap() == true) {
					//System.out.println("INTERBLOGAGE*************999999999999999999");
					// Declare le partage de Map :
					this.agent.setShareMap(true);
					// L'identidiant du dernier agent ayant partager sa carte :
					this.agent.setIdentifiantShareMap(msgCarte1.getIdentifiant()); //
					// Reboot :
					this.agent.getCarteExploration().setChangeMap(false);
				}
			} catch (UnreadableException e) {
				e.printStackTrace();
			}		
			
			// Next message dans la boite au lettre :
			msgRecuMap = this.agent.receive(msgTemplateEcho);
		} 			
	}/*
		// Modification du comportement :
		this.agent.setStrategie(Strategie.SendMap);

		if (this.agent.getInterblocagePos() != null) {
			// Je n'étais pas en interblocage au temps t-1 :
			ArrayList<AID> expediteur = this.receiveMap(); // Avec agregation des Map
			if (expediteur.isEmpty() == false) {
				// Si j'ai reçu une Map en d'interblocage :
				// J'envoie ma Map à l'expéditeur :
				for (AID exp :expediteur) {
					// J'envois ma Map à ce destinataire :
					ACLMessage msg = this.creationMsgMap();
					// Ajouter les expéditeur :
					this.addExpediteurToSender(msg, exp);
					// J'envois ma Map à ce destinataire :
					this.agent.sendMessage(msg);
				}
			} else {
				// Je n'ai reçu aucun message :
				// J'envoie ma Map à tout le monde :
				ACLMessage msg = this.creationMsgMap();
				// Ajouter les expéditeur :
				this.addExpediteurToAgent(msg);
				// J'envois ma Map à ce destinataire :
				this.agent.sendMessage(msg);				
			}
			// Ajouter ma position d'interblocage dans la variable d'interblocage :
			this.agent.setInterblocagePos(this.agent.getCurrentPosition());
		} else {
			// Je suis déjà en état d'interblocage au temps t-1 :
			// J’ai déjà envoyé ma Map, donc je renvoie ma Map seulement, si j’ai reçu une Map :
			// Si j’ai reçu un message d’inter-blocage :
			ArrayList<AID> expediteur = this.receiveMap();
			if (expediteur.isEmpty() == false) {
				for (AID exp :expediteur) {
					// J'envois ma Map à ce destinataire :
					ACLMessage msg = this.creationMsgMap();
					// Ajouter les expéditeur :
					this.addExpediteurToSender(msg, exp);
					// J'envois ma Map à ce destinataire :
					this.agent.sendMessage(msg);
				}
			} else {
				System.out.println("\n************ Aucun expéditeur : " + this.agent.getLocalName());
			}
		}
	}
	

	private void addExpediteurToSender (ACLMessage msg, AID aidInterblocage) {
		// Ajoute comme destinataire, l'expéditeur du message d'interblocage :
		msg.addReceiver(aidInterblocage);
	}
	
	private void addExpediteurToAgent (ACLMessage msg) {
		// Ajouter en destinataires tout les agents inscrti dans le DFS:
		DFAgentDescription description = new DFAgentDescription();
        DFAgentDescription[] resultats;
        try {
			resultats = DFService.search(this.getAgent(), description);
            if (resultats.length > 0) {
                for (DFAgentDescription agent : resultats) {
                	// Ne communique pas avec lui même:
                    if (agent.getName().equals(this.getAgent().getAID()) == false ){
                    	msg.addReceiver(agent.getName());
                    }
                }
            }
		} catch (FIPAException e1) {
			e1.printStackTrace();
		}
	}
	
	private ArrayList<AID>  receiveMap () {
		// Creation d'une liste d'expéditeur de SendMap en cas d'interblocage :
		ArrayList<AID> expediteur = new ArrayList<AID>();
		
		MessageTemplate msgTemplateEcho = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchProtocol("SEND_MAP"));
		
		ACLMessage msgRecuMap = this.agent.blockingReceive(msgTemplateEcho, 500);
		
		while(msgRecuMap != null) {
			System.out.println("\n************ Reception Interblocage Map : " + this.agent.getLocalName());
			// MAJ de la carte :
			try {
				// Extrait le contenu du message dans msgCarte :
				Carte msgCarte1 = (Carte)msgRecuMap.getContentObject();
				
				// MAJ des cartes par insertion :
				msgCarte1.updateInsertCartes(this.agent.getCarteExploration(),
						this.agent.getCarteTresors(), 
						this.agent.getCarteDangers());
				
				// Maj de la liste :
				if (expediteur.contains(msgRecuMap.getSender()) == false) {
					expediteur.add(msgRecuMap.getSender());
				}

				// Active Echo MAp seulement si il y a eu des changements dans la carte :
				if (this.agent.getCarteExploration().getChangeMap() == true) {
					// Declare le partage de Map :
					this.agent.setShareMap(true);
					// L'identidiant du dernier agent ayant partager sa carte :
					this.agent.setIdentifiantShareMap(msgCarte1.getIdentifiant());
					// Reboot :
					this.agent.getCarteExploration().setChangeMap(false);
				}
			} catch (UnreadableException e) {
				e.printStackTrace();
			}		
			
			// Next message dans la boite au lettre :
			msgRecuMap = this.agent.receive(msgTemplateEcho);
		} 
		return expediteur;
	}
	
	private ACLMessage creationMsgMap () {
		System.out.println("\n************SEND MAP INTERBLOCAGE : " + this.agent.getLocalName());
		// 1) L'agent doit envoyer sa Carte d'exploration :
		// Creation d'un message :
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("SEND_MAP");
		// Modifier l'expéditeur du message :
		msg.setSender(this.getAgent().getAID());
        // Rendre le carteExploration serializable :
        SerializableCarteExploration carteExploSerializable = new SerializableCarteExploration
        		(this.agent.getCarteExploration());        
        
        // Creation de l'identifiant :
        Identifiant id = new Identifiant(this.agent.getLocalName(), this.agent.getIdAgent());
        
        // Creation de la carte :
        Carte msgCarte = new Carte(id, carteExploSerializable , this.agent.getCarteTresors(),
        		this.agent.getCarteDangers());
    	try {
			msg.setContentObject(msgCarte);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return msg;
	}*/
	
	
	@Override
	public int onEnd(){
		//System.out.println("**** " + this.agent.getLocalName() + "----> sort de EgoismeBehaviour\n");
		return AgentExplorateur.P_PLANIFICATION_AFTER_SEND_MAP;
	}
}






		
		