// ************
// Types
// ************

import '@dev.hiconic/gm_root-model';
import '@dev.hiconic/gm_meta-model';
import '@dev.hiconic/gm_time-model';
import '@dev.hiconic/gm_gm-core-api';

import { T } from '@dev.hiconic/hc-js-base';

export declare namespace meta {
	const groupId: string;
	const artifactId: string;
	const version: string;
}

export import ConditionProcessorReference = T.tribefire.extension.process.model.configuration.ConditionProcessorReference;
export import DecoupledInteraction = T.tribefire.extension.process.model.configuration.DecoupledInteraction;
export import Edge = T.tribefire.extension.process.model.configuration.Edge;
export import Node = T.tribefire.extension.process.model.configuration.Node;
export import ProcessDefinition = T.tribefire.extension.process.model.configuration.ProcessDefinition;
export import ProcessDefinitionsConfiguration = T.tribefire.extension.process.model.configuration.ProcessDefinitionsConfiguration;
export import TransitionProcessorReference = T.tribefire.extension.process.model.configuration.TransitionProcessorReference;

declare module '@dev.hiconic/hc-js-base' {

	namespace T.tribefire.extension.process.model.configuration {

		const ConditionProcessorReference: hc.reflection.EntityType<ConditionProcessorReference>;
		type ConditionProcessorReference = T.com.braintribe.model.generic.GenericEntity &
		  Entity<"tribefire.extension.process.model.configuration.ConditionProcessorReference", {
			processorId: string;
		}>;

		const DecoupledInteraction: hc.reflection.EntityType<DecoupledInteraction>;
		type DecoupledInteraction = T.com.braintribe.model.generic.GenericEntity &
		  Entity<"tribefire.extension.process.model.configuration.DecoupledInteraction", {
			description: string;
			name: string;
		}>;

		const Edge: hc.reflection.EntityType<Edge>;
		type Edge = T.com.braintribe.model.generic.GenericEntity &
		  Entity<"tribefire.extension.process.model.configuration.Edge", {
			condition: ConditionProcessorReference;
			description: string;
			from: Node;
			name: string;
			onTransit: list<TransitionProcessorReference>;
			to: Node;
		}>;

		const Node: hc.reflection.EntityType<Node>;
		type Node = T.com.braintribe.model.generic.GenericEntity &
		  Entity<"tribefire.extension.process.model.configuration.Node", {
			decoupledInteraction: DecoupledInteraction;
			description: string;
			errorNode: Node;
			gracePeriod: T.com.braintribe.model.time.TimeSpan;
			name: string;
			onEntered: list<TransitionProcessorReference>;
			onError: list<TransitionProcessorReference>;
			onLeft: list<TransitionProcessorReference>;
			overdueNode: Node;
			state: string;
		}>;

		const ProcessDefinition: hc.reflection.EntityType<ProcessDefinition>;
		type ProcessDefinition = T.com.braintribe.model.generic.GenericEntity &
		  Entity<"tribefire.extension.process.model.configuration.ProcessDefinition", {
			edges: list<Edge>;
			errorNode: Node;
			gracePeriod: T.com.braintribe.model.time.TimeSpan;
			name: string;
			nodes: list<Node>;
			onError: list<TransitionProcessorReference>;
			onTransit: list<TransitionProcessorReference>;
		}>;

		const ProcessDefinitionsConfiguration: hc.reflection.EntityType<ProcessDefinitionsConfiguration>;
		type ProcessDefinitionsConfiguration = T.com.braintribe.model.generic.GenericEntity &
		  Entity<"tribefire.extension.process.model.configuration.ProcessDefinitionsConfiguration", {
			definitions: map<string, ProcessDefinition>;
			monitoredAccessIds: set<string>;
		}>;

		const TransitionProcessorReference: hc.reflection.EntityType<TransitionProcessorReference>;
		type TransitionProcessorReference = T.com.braintribe.model.generic.GenericEntity &
		  Entity<"tribefire.extension.process.model.configuration.TransitionProcessorReference", {
			processorId: string;
		}>;

	}

}
