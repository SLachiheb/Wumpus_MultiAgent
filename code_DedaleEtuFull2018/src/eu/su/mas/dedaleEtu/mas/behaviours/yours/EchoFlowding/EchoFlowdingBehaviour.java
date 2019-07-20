package eu.su.mas.dedaleEtu.mas.behaviours.yours.EchoFlowding;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentExplorateur;
import jade.core.behaviours.OneShotBehaviour;

public class EchoFlowdingBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1810701380795610718L;
	
	private AgentAbstrait agent;
	private Integer 		numberTransition;
	
	public EchoFlowdingBehaviour (final AgentAbstrait agentAbstrait) {
		super(agentAbstrait);
		this.agent = agentAbstrait;
		this.numberTransition	= null;
	}
	
	@Override
	public void action() {
		
		//System.out.println("**** " + this.agent.getLocalName() + " <----- est dans EchoFlowding : \n"
		//		+ this.agent.getEchoFlowding());
		
		// Son et Non(Dad) and Son et Dad:
		if (this.agent.getEchoFlowding().isSons()) {
			this.numberTransition = AgentExplorateur.T_WAIT_CARTE_SONS;
			//System.out.println("**** " + this.agent.getLocalName() + " <----- est dans EchoFlowding et je vais à T_WAIT_CARTE_SONS");

		}
		
		// Non(Fils) et Non(Dad): 
		if (!this.agent.getEchoFlowding().isSons() && !this.agent.getEchoFlowding().isDad()) {
			this.numberTransition = AgentExplorateur.T_ECHO_TO_PRUGE_ECHOFLOWDING;
			//System.out.println("**** " + this.agent.getLocalName() + " <----- est dans EchoFlowding et je vais à ECHO_TO_PURGE");

		}
		
		// Non(Fils) et Dad :
		if (!this.agent.getEchoFlowding().isSons()  && this.agent.getEchoFlowding().isDad()) {
			this.numberTransition = AgentExplorateur.T_ECHO_TO_SEND_CARTE_DAD;
			//System.out.println("**** " + this.agent.getLocalName() + " <----- est dans EchoFlowding et je vais à ECHO_TO_SEND_CARTE_DAD");
		}
	}
	
	@Override
	public int onEnd() {
		//System.out.println("**** " + this.agent.getLocalName() + " -----> sort de EchoFlowding\n");
		return this.numberTransition;
	}
}
