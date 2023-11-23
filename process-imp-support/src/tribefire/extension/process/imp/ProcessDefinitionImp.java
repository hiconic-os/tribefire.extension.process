// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.imp;
// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================



import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.model.extensiondeployment.Worker;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.product.rat.imp.AbstractImp;
import com.braintribe.product.rat.imp.ImpException;
import com.braintribe.product.rat.imp.impl.model.ModelImpCave;
import com.braintribe.utils.CommonTools;

import tribefire.extension.process.model.deployment.ConditionProcessor;
import tribefire.extension.process.model.deployment.ConditionalEdge;
import tribefire.extension.process.model.deployment.DecoupledInteraction;
import tribefire.extension.process.model.deployment.Edge;
import tribefire.extension.process.model.deployment.Node;
import tribefire.extension.process.model.deployment.ProcessDefinition;
import tribefire.extension.process.model.deployment.ProcessElement;
import tribefire.extension.process.model.deployment.ProcessingEngine;
import tribefire.extension.process.model.deployment.RestartNode;
import tribefire.extension.process.model.deployment.StandardNode;
import tribefire.extension.process.model.deployment.TransitionProcessor;

/**
 * A {@link AbstractImp} specialized in {@link ProcessDefinition}
 */
public class ProcessDefinitionImp extends AbstractImp<ProcessDefinition> {

	public ProcessDefinitionImp(PersistenceGmSession session, ProcessDefinition processDefinition) {
		super(session, processDefinition);
	}

	/**
	 * <b>Adds</b> the passed ProcessElement to this imp's processElements<br>
	 * <b>Prevents duplicate Node states</b>: Makes sure, no Node is added with a state that is already taken by another
	 * Node of this imp's ProcessDefinition
	 */
	public ProcessDefinitionImp addProcessElement(ProcessElement element) {
		// Do nothing if element is already contained (i.e. skip validation)
		if (instance.getElements().contains(element)) {
			return this;
		}

		if (element instanceof Node) {
			String state = (String) ((Node) element).getState();

			// @formatter:off
			findNode(Node.T, state)
				.ifPresent(node -> {
					throw new ImpException("There is already a node with state '" + state + "'. Cannot add another one to process definition " + instance.getName());
				});
			// @formatter:on
		}

		instance.getElements().add(element);

		return this;
	}

	/**
	 * {@link #addEdge(String, String, Node, TransitionProcessor...)} without an errorNode
	 */
	public ProcessDefinitionImp addEdge(String fromState, String toState, TransitionProcessor... onTransit) {
		return addEdge(fromState, toState, null, onTransit);
	}

	/**
	 * Creates a new {@link Edge} and adds it to this imp's process definition
	 * <p>
	 * <b>Attention</b>: null is a valid state for a node, so if you pass null, a node with that very state must already
	 * exist in this imp's process definition
	 * <p>
	 * 1) Makes sure that the two nodes with passed states are already part of this imp's process definition<br>
	 * 2) Makes sure that the node with state 'fromState' is a StandardNode<br>
	 * 3) If an error node was passed: adds it to the edge as error node<br>
	 * 4) Adds all passed transition processors to the onTransit property of the edge
	 *
	 * @param fromState
	 *            state of the node that should be used for the 'from' property of the new edge
	 * @param toState
	 *            state of the node that should be used for the 'to' property of the new edge
	 * @throws ImpException
	 *             if the node with state 'fromState' isn't an instance of StandardNode<br>
	 */
	public ProcessDefinitionImp addEdge(String fromState, String toState, Node errorNode, TransitionProcessor... onTransit) {
		StandardNode fromNode = getNode(StandardNode.T, fromState);
		Node toNode = getNode(Node.T, toState);

		Edge edge = session().create(Edge.T);

		edge.setFrom(fromNode);
		edge.setTo(toNode);
		edge.setOnTransit(CommonTools.getList(onTransit));
		edge.setErrorNode(errorNode);

		addProcessElement(edge);

		return this;
	}

	/**
	 * Creates a new {@link ConditionalEdge} and adds it to this imp's process definition
	 * <p>
	 * <b>Attention</b>: null is a valid state for a node, so if you pass null, a node with that very state must already
	 * exist in this imp's process definition
	 * <p>
	 * 1) Makes sure that the two nodes with passed states are already part of this imp's process definition<br>
	 * 2) Makes sure that the node with state 'fromState' is a StandardNode<br>
	 * 3) Adds the passed condition to the condition property of the edge
	 *
	 * @param fromState
	 *            state of the node that should be used for the 'from' property of the new edge
	 * @param toState
	 *            state of the node that should be used for the 'to' property of the new edge
	 * @throws ImpException
	 *             if the node with state 'fromState' isn't an instance of StandardNode<br>
	 */
	public ProcessDefinitionImp addConditionalEdge(String fromState, String toState, ConditionProcessor condition) {
		StandardNode fromNode = getNode(StandardNode.T, fromState);
		Node toNode = getNode(Node.T, toState);

		ConditionalEdge conditionalEdge = session().create(ConditionalEdge.T);
		conditionalEdge.setCondition(condition);
		conditionalEdge.setFrom(fromNode);
		conditionalEdge.setTo(toNode);

		addProcessElement(conditionalEdge);
		fromNode.getConditionalEdges().add(conditionalEdge);

		return this;
	}

	/**
	 * Creates a {@link StandardNode} for each passed state and adds it to this imp's process definition
	 *
	 * @param states
	 *            the states for the nodes you want to create
	 */
	public ProcessDefinitionImp addStandardNodes(String... states) {
		for (String state : states) {
			StandardNode node = session().create(StandardNode.T);
			node.setState(state);
			addProcessElement(node);
		}

		return this;
	}

	/**
	 * A convenience method that returns all process elements of this imp's process definition that are an instance of
	 * given type.
	 * <p>
	 * <i>Example</i>: If you pass Edge.T you will get a Set of all contained Edges and ConditionalEdges, without the
	 * nodes
	 */
	public <E extends ProcessElement> Set<E> getProcessElementsOfType(EntityType<E> type) {
		// @formatter:off
		return instance.getElements().stream()
				.filter(type::isInstance)
				.map(e -> (E) e)
				.collect(Collectors.toSet());
		// @formatter:on
	}

	private <E extends Node> Optional<E> findNode(EntityType<E> type, String state) {
		// @formatter:off
		Set<E> nodes = getProcessElementsOfType(type).stream()
				.filter(node -> Objects.equals(node.getState(), state))
				.collect(Collectors.toSet());
		// @formatter:on

		switch (nodes.size()) {
			case 0:
				return Optional.empty();
			case 1:
				return Optional.of(nodes.iterator().next());
			default:
				throw new ImpException("There were multiple nodes found with type '" + type.getTypeSignature() + "' and state '" + state + "'");
		}
	}

	/**
	 * Searches for a node with passed type and state in this imp's process definition's elements and returns it
	 *
	 * @throws ImpException
	 *             when no matching node is found
	 */
	public <E extends Node> E getNode(EntityType<E> type, String state) {
		// @formatter:off
		return findNode(type, state)
				.orElseThrow(() -> new ImpException("Couldn't find a node of type '" + type.getTypeSignature() + "' with state '" + state +
													"' in process definition " + instance.getName()));
		// @formatter:on
	}

	/**
	 * Creates a new {@link RestartNode} and adds it to this imp's process elements
	 *
	 * @param state
	 *            state of the new restart node
	 * @param restartEdgeFromNodeState
	 *            state of the "from" node of the restart edge
	 * @param restartEdgeToNodeState
	 *            state of the "to" node of the restart edge
	 * @param maxNumberOfRestarts
	 *            maximum number of restarts for the restart node
	 * @throws ImpException
	 *             if no edge was found between the nodes of the passed state
	 */
	public ProcessDefinitionImp addRestartNode(String state, String restartEdgeFromNodeState, String restartEdgeToNodeState,
			Integer maxNumberOfRestarts) {
		Set<Edge> processElementsOfType = getProcessElementsOfType(Edge.T);

		// @formatter:off
		Set<Edge> edges = processElementsOfType.stream()
			.filter(e -> Objects.equals(e.getFrom().getState(), restartEdgeFromNodeState) && Objects.equals(e.getTo().getState(), restartEdgeToNodeState))
			.collect(Collectors.toSet());
		// @formatter:on

		if (edges.size() != 1) {
			throw new ImpException("Expected to find exactly 1 edge from '" + restartEdgeFromNodeState + "' to '" + restartEdgeToNodeState
					+ "' but found " + edges.size());
		}

		Edge restartEdge = edges.iterator().next();

		RestartNode node = session().create(RestartNode.T);
		node.setState(state);
		node.setRestartEdge(restartEdge);
		node.setMaximumNumberOfRestarts(maxNumberOfRestarts);

		addProcessElement(node);

		return this;
	}

	/**
	 * Adds given processor to the onEntered property of the Node with given state
	 *
	 * @param nodeState
	 *            the state of the node where the transition processor should be added.
	 * @param processor
	 *            the transition processor that should be added
	 * @throws ImpException
	 *             if no node with given state could be found in this imp's process definition's elements
	 */
	public ProcessDefinitionImp addOnEntered(String nodeState, TransitionProcessor processor) {
		getNode(Node.T, nodeState).getOnEntered().add(processor);
		return this;
	}

	/**
	 * Adds given processor to the onLeft property of the Node with given state
	 *
	 * @param standardNodeState
	 *            the state of the {@link StandardNode} where the transition processor should be added.
	 * @param processor
	 *            the transition processor that should be added
	 * @throws ImpException
	 *             if no node with given state could be found in this imp's process definition's elements or if found
	 *             Node is no instance of StandardNode
	 */
	public ProcessDefinitionImp addOnLeft(String standardNodeState, TransitionProcessor processor) {
		getNode(StandardNode.T, standardNodeState).getOnLeft().add(processor);
		return this;
	}

	/**
	 * 1) Creates a new instance of DecoupledInteraction and sets it up according to passed parameters<br>
	 * 2) Finds the StandardNode with passed state and sets the created DecoupledInteraction on it
	 *
	 * @param standardNodeState
	 *            the state of the {@link StandardNode} where the decoupled interaction should be set
	 * @param userInteraction
	 *            the value of the userInteraction property for the decoupled interaction that should be created
	 * @param workers
	 *            the workers for the decoupled interaction that should be created
	 * @throws ImpException
	 *             if no node with given state could be found in this imp's process definition's elements or if found
	 *             Node is no instance of StandardNode
	 */
	public ProcessDefinitionImp setDecoupledInteraction(String standardNodeState, boolean userInteraction, Worker... workers) {
		DecoupledInteraction decoupledInteraction = session().create(DecoupledInteraction.T);
		decoupledInteraction.setUserInteraction(userInteraction);
		decoupledInteraction.setWorkers(CommonTools.getSet(workers));

		getNode(StandardNode.T, standardNodeState).setDecoupledInteraction(decoupledInteraction);
		return this;
	}

	public ProcessDefinitionImp setTrigger(EntityType<?> entityType, String propertyName) {
		// @formatter:off
		GmProperty triggerProperty = new ModelImpCave(session())
				.entityType(entityType)
				.getProperty(propertyName);
		// @formatter:on

		instance.setTrigger(triggerProperty);
		return this;
	}

	public ProcessDefinitionImp setTrigger(GmProperty property) {
		instance.setTrigger(property);
		return this;
	}
	
	public ProcessDefinitionImp setTriggerType(GmEntityType type) {
		instance.setTriggerType(type);
		return this;
	}
	
	public ProcessDefinitionImp setTriggerType(EntityType<?> type) {
		GmEntityType gmEntityType = new ModelImpCave(session())
			.entityType(type)
			.get();
		
		instance.setTriggerType(gmEntityType);
		return this;
	}

	public ProcessDefinitionImp addToProcessingEngine(ProcessingEngine engine) {
		// @formatter:off
		new ProcessingEngineImpCave(session())
			.with(engine)
			.addDefinition(instance);

		// @formatter:on

		return this;
	}
}
