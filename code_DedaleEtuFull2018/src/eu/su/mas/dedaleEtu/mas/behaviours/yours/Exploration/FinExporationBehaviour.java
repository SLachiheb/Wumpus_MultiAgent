package eu.su.mas.dedaleEtu.mas.behaviours.yours.Exploration;


import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import jade.core.behaviours.OneShotBehaviour;

public class FinExporationBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4690279900698060262L;

	public FinExporationBehaviour (final AgentAbstrait agentAbstrait) {
		super(agentAbstrait);
	}
	
	@Override
	public void action() {
		System.out.println("*****************************************************");
		System.out.println(this.getAgent().getLocalName() +  " : Fin de Mision ");
		this.myAgent.doDelete();
		
	}

}
