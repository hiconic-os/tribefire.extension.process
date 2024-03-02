// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.processing.oracle;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.braintribe.model.time.TimeSpan;

import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.data.model.state.TransitionPhase;
import tribefire.extension.process.model.deployment.Edge;
import tribefire.extension.process.model.deployment.HasErrorNode;
import tribefire.extension.process.model.deployment.Node;
import tribefire.extension.process.model.deployment.ProcessDefinition;
import tribefire.extension.process.model.deployment.RestartNode;
import tribefire.extension.process.model.deployment.StandardNode;
import tribefire.extension.process.model.deployment.TransitionProcessor;

public class TransitionOracle {
	private ProcessDefinition pd;
	private List<TransitionProcessor> resolvedProcessors;
	private Node fromNode;
	private Node toNode;
	private Edge edge;
	private ProcessOracle processOracle;
	
	public TransitionOracle(ProcessOracle processOracle, ProcessDefinition pd, Node fromNode, Node toNode) {
		super();
		this.processOracle = processOracle;
		this.pd = pd;
		this.fromNode = fromNode;
		this.toNode = toNode;
	}
	
	public TransitionOracle(ProcessOracle processOracle, ProcessDefinition pd, Edge edge) {
		this(processOracle, pd, edge.getFrom(), edge.getTo());
		this.edge = edge;
	}

	public String getFromState() {
		return (String) getFrom().getState();
	}
	
	public String getToState() {
		return (String) getTo().getState();
	}
	
	public Node getFrom() {
		return fromNode;
	}
	
	public Node getTo() {
		return toNode;
	}
	
	public Edge getEdge() {
		return edge;
	}
	
	public boolean isRestart() {
		return toNode instanceof RestartNode;
	}
	
	public boolean isTerminal() {
		return processOracle.isTerminal(toNode);
	}
	
	public Node getErrorNode() {
		HasErrorNode[] candidates = {getTo(), pd};
		
		for (HasErrorNode hasErrorNode: candidates) {
			Node errorNode = hasErrorNode.getErrorNode();
			if (errorNode != null)
				return errorNode;
		}
		
		return null;
	}
	
	public void initTransition(ProcessItem processItem) {
		Date now = new Date();

		// state shift
		processItem.setPreviousState(processItem.getState());
		processItem.setState((String) toNode.getState());
		processItem.setLastTransit(now);

		// phase reset
		processItem.setTransitionPhase(TransitionPhase.CHANGED_STATE);
		processItem.setTransitionProcessorId(null);
		processItem.setNextState(null);
		
		// overdue
		processItem.setOverdueAt(determineOverdue());
	}
	
	private Date determineOverdue() {
		if (isRestart())
			return null;

		StandardNode standardNode = (StandardNode)toNode;
		
		if (standardNode.getDecoupledInteraction() == null)
			return null;
		
		TimeSpan gracePeriod = standardNode.getGracePeriod();
		
		if (gracePeriod == null)
			gracePeriod = pd.getGracePeriod();
		
		if (gracePeriod == null)
			return null;
		
		long millies = gracePeriod.toLongMillies();
		return new Date(System.currentTimeMillis() + millies);
	}

	public Iterator<TransitionProcessor> getSuccessiveTransitionProcessors(String transitionProcessorId) {
		List<TransitionProcessor> transitionProcessors = getTransitionProcessors();
		
		Iterator<TransitionProcessor> it = transitionProcessors.iterator();

		if (transitionProcessorId == null)
			return it;
		
		while (it.hasNext()) {
			TransitionProcessor processor = it.next();
			if (transitionProcessorId.equals(processor.getExternalId())) {
				break;
			}
		}
		
		return it;
	}
	
	public TransitionProcessor getPredecessorTransitionProcessor(String transitionProcessorId) {
		List<TransitionProcessor> transitionProcessors = getTransitionProcessors();
		
		int size = transitionProcessors.size();
		for (int i = 0; i < size; i++) {
			TransitionProcessor transitionProcessor = transitionProcessors.get(i);
			if (transitionProcessorId.equals(transitionProcessor.getExternalId())) {
				return i > 0? //
						transitionProcessors.get(i - 1): //
						null; //
			}
		}
		
		return null;
	}
	
	public List<TransitionProcessor> getTransitionProcessors() {
		if (resolvedProcessors == null) {
			resolvedProcessors = new ArrayList<>();
			
			if (edge != null) {
				resolvedProcessors.addAll(pd.getOnTransit());
				resolvedProcessors.addAll(edge.getFrom().getOnLeft());
				if (edge != null)
					resolvedProcessors.addAll(edge.getOnTransit());
			}
			resolvedProcessors.addAll(getTo().getOnEntered());
		}

		return resolvedProcessors;
	}
}
