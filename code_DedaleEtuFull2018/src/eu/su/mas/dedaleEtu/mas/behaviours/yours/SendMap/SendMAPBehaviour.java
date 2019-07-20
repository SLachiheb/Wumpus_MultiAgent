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
			// MAJ de la carte :
			try {
				// Extrait le contenu du message dans msgCarte :
				Carte msgCarte1 = (Carte)msgRecuMap.getContentObject();
				// MAJ des cartes par insertion :
				msgCarte1.updateInsertCartes(this.agent.getCarteExploration(),
						this.agent.getCarteTresors(), 
						this.agent.getCarteDangers());
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
	}

	@Override
	public int onEnd(){
		return AgentExplorateur.P_PLANIFICATION_AFTER_SEND_MAP;
	}
}






		
		