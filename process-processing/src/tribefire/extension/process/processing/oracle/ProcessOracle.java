// ============================================================================
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
package tribefire.extension.process.processing.oracle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.utils.lcd.NullSafe;

import tribefire.extension.process.model.deployment.ConditionalEdge;
import tribefire.extension.process.model.deployment.Edge;
import tribefire.extension.process.model.deployment.Node;
import tribefire.extension.process.model.deployment.ProcessDefinition;
import tribefire.extension.process.model.deployment.ProcessElement;
import tribefire.extension.process.model.deployment.StandardNode;
import tribefire.extension.process.reason.model.EdgeNotFound;
import tribefire.extension.process.reason.model.NodeNotFound;

public class ProcessOracle {
	public ProcessDefinition processDefinition;
	public Map<String, Node> nodeByState = new HashMap<>();
	public Map<String, List<Edge>> standardEdgesFromState = new HashMap<>();
	public Set<StandardNode> fromNodes = new HashSet<>();
	public Map<Pair<String,String>, Edge> edgesByStateChange = new HashMap<>();
	//public MultiMap<Object, ConditionalEdge> conditionalEdgesByLeftState = new ComparatorBasedNavigableMultiMap<Object, ConditionalEdge>(StateComparator.instance, EdgeComparator.instance);
	public Set<StandardNode> drainNodes = new HashSet<>();
	
	public ProcessOracle(ProcessDefinition definition) {
		this.processDefinition = definition;
		
		for (ProcessElement processElement: NullSafe.set(definition.getElements())) {
			if (processElement instanceof Node) {
				Node node = (Node)processElement;
				String state = (String)node.getState();
				
				if (node instanceof StandardNode) {
					StandardNode standardNode = (StandardNode)node;
					if (!standardNode.getConditionalEdges().isEmpty())
						fromNodes.add(standardNode);
				}
				
				nodeByState.put(state, node);
			}
			else if (processElement instanceof Edge) {
				Edge edge = (Edge)processElement;
				StandardNode fromNode = edge.getFrom();
				String fromState = (String)fromNode.getState();
				String toState = (String)edge.getTo().getState();
				Pair<String, String> key = Pair.of(fromState, toState);
				
				fromNodes.add(fromNode);
				edgesByStateChange.put(key, edge);
				
				if (!(edge instanceof ConditionalEdge))
					standardEdgesFromState.computeIfAbsent(fromState, k -> new ArrayList<>()).add(edge);
			}
			
		}
		
		for (Node node: nodeByState.values()) {
			if (node instanceof StandardNode) {
				StandardNode standardNode = (StandardNode)node;
				if (!fromNodes.contains(standardNode))
					drainNodes.add(standardNode);
			}
		}
	}
	
	public boolean hasState(String state) {
		return nodeByState.containsKey(state);
	}
	
	public boolean hasEdge(String fromState, String toState) {
		return edgesByStateChange.get(Pair.of(fromState, toState)) != null;
	}
	
	public Maybe<Edge> getInitialDefaultEdge() {
		StandardNode node = getInitialNode();

		if (node == null) {
			return Reasons.build(NodeNotFound.T).text("Initial process node not found").toMaybe();
		}
		
		List<ConditionalEdge> defaultEdges = node.getConditionalEdges().stream().filter(e -> e.getCondition() == null).collect(Collectors.toList());
		
		switch (defaultEdges.size()) {
		case 0:
			return Reasons.build(EdgeNotFound.T).text("No default conditional edge found on initial node").toMaybe();
		case 1:
			return Maybe.complete(defaultEdges.get(0));
		default:
			return Reasons.build(EdgeNotFound.T).text("Ambiguous default conditional edges on initial node").toMaybe();
		}
	}

	private StandardNode getInitialNode() {
		return (StandardNode)nodeByState.get(null);
	}
	
	public TransitionOracle transitionOracle(String fromState, String toState) {
		return transitionOracle(fromState, toState, false);
	}
	
	public TransitionOracle transitionOracle(String fromState, String toState, boolean allowSpecialEdges) {
		Edge edge = edgesByStateChange.get(Pair.of(fromState, toState));
		
		if (edge == null) {
			if (allowSpecialEdges) {
				
				Node from = nodeByState.get(fromState);
				Node to = nodeByState.get(toState);
				
				return new TransitionOracle(this, processDefinition, from, to);
			}
			else
				throw new NoSuchElementException("No edge found from state " + fromState + " to state " + toState);
		}
		
		return new TransitionOracle(this, processDefinition, edge.getFrom(), edge.getTo());
	}

	public boolean isTerminal(Node node) {
		return drainNodes.contains(node);
	}
}