package eu.su.mas.dedaleEtu.mas.behaviours.yours.EchoFlowding;


import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentExplorateur;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.Strategie;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.EchoFlowding.ProtocoleEcho;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * Ce comportement vérifie si on a reçu une demande de père et si c'est le cas, il est
 * inseré dans la donnée de la classe EchoFlowding.
 * @author sarah
 */


public class CheckEchoBoiteLettre extends OneShotBehaviour {

	/** 
	 * 
	 */
	private static final long serialVersionUID = 458713412713634226L;
	private AgentAbstrait agent;
	private Integer numTransition;
	
	public CheckEchoBoiteLettre (final AgentAbstrait agentAbstrait) {
		super(agentAbstrait);
		this.agent = agentAbstrait;
		this.numTransition = null;
	}
	
	@Override
	public void action() {
		//System.out.println("**** " + this.agent.getLocalName() + "<---- entre dans CheckEchoBoiteLettre");
		this.agent.setStrategie(Strategie.EchoFlowdingMap);

		// Check les messages de type Echo :
		MessageTemplate msgTemplateEcho = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
				MessageTemplate.MatchProtocol("ECHO"));
		
		ACLMessage msgRecuEcho = this.agent.receive(msgTemplateEcho);
		
		while(msgRecuEcho != null) {
			ProtocoleEcho echo = null;
			try {
				echo = (ProtocoleEcho) msgRecuEcho.getContentObject();
			} catch (UnreadableException e1) {
				//System.out.println("Error : CheckEchoBoitLettre ---> getContentObject()");
				e1.printStackTrace();
			}
					
			// Reception des pères potentiels :
			this.agent.getEchoFlowding().addDadPotentiel(msgRecuEcho.getSender(), echo);

			// Next message dans la boite au lettre :
			msgRecuEcho = this.agent.receive(msgTemplateEcho);
		} 
		
		
		if (this.agent.getEchoFlowding().isDadPotentiel()) { 
			// Si la liste des pères potentielles est vide :
			if (this.agent.getCheckEcho() == true) {
				// Verification de reception de Dad potentielle entre intervalle d'EchoMap :
				this.numTransition = AgentExplorateur.T_CHECK_ECHO_AFTER_PLANIFICATION; 
			} else if (this.agent.getEchoFlowding().isSecondeChance() ==  true) {
				this.numTransition = AgentExplorateur.T_ATTENDRE_CONFIRMATION_FILS_AFTER_CHECK_ECHO; 
			} else {
				// Aucun message Echo reçu :
				//System.out.println(this.agent.getLocalName() + " est la racine de la communication");
				// Maj du Dad et la création d'un nouveau protocole + racine :
				this.agent.getEchoFlowding().initNewProtocolEcho();
				// Aigulage de la transition :
				this.numTransition = AgentExplorateur.A_SEND_ECHO;
			}
		} else {
			// Si la liste des pères potentielles est non vide :
			// Aigulage de la transition :
			/*if (this.agent.getCheckEcho() == true && this.agent.getCptEchoFlowdingMap() < this.agent.getIntervalleEchoMap() /2) {
				this.numTransition = AgentExplorateur.T_CHECK_ECHO_AFTER_PLANIFICATION; 
			} else {
	 			this.numTransition = AgentExplorateur.T_SEND_CONFIRMATION_DAD; 
			}*/
 			this.numTransition = AgentExplorateur.T_SEND_CONFIRMATION_DAD; 
 			this.agent.setCheckEcho(false);
		}
	}

	@Override
	public int onEnd(){
		//System.out.println("**** " + this.agent.getLocalName() + "----> sort de CheckEchoBoiteLettre\n");
		return this.numTransition;
	}
}
