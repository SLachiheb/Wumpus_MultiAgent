package eu.su.mas.dedaleEtu.mas.behaviours.yours.Satisfaction;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentExplorateur;
import jade.core.behaviours.OneShotBehaviour;

public class EgoisteBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3636291027688941173L;

	private AgentAbstrait 	agent;
	private Integer 			numTransition;
	
	public EgoisteBehaviour (final AgentAbstrait agentAbstrait) {
		super(agentAbstrait);
		this.agent = agentAbstrait;
		this.numTransition = null;
	}
	@Override
	public void action() {
		//System.out.println("**** " + this.agent.getLocalName() + " <---- entre dans EgoismeBehaviour\n");

		// A Faire la pousuite de tache et l'abondon de tache !!
		
		// Agent ayant une statisfaction personnelle négative :
		if (this.agent.getSatisfaction().getSignalP() < 0) {
			// Envoie un message pour repouser l'agent gêneur :
			//System.out.println("**** " + this.agent.getLocalName() + " <---- envoit un message repulsif\n");
			this.numTransition = AgentExplorateur.T_SEND_SIGNAL_REPULSIF;
		} else {
			// Continue de poursuivre son but car sa satisfaction est positif :
			//System.out.println("**** " + this.agent.getLocalName() + " <---- continue en planification\n");
			this.numTransition = AgentExplorateur.T_EGOISTE_TO_PLANIFICATION;
		}
		
	}
	
	@Override
	public int onEnd(){
		//System.out.println("**** " + this.agent.getLocalName() + "----> sort de EgoismeBehaviour\n");
		return this.numTransition;
	}

}
