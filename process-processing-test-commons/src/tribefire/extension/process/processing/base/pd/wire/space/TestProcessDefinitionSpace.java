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
package tribefire.extension.process.processing.base.pd.wire.space;

import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.process.model.deployment.ConditionProcessor;
import tribefire.extension.process.model.deployment.ConditionalEdge;
import tribefire.extension.process.model.deployment.DecoupledInteraction;
import tribefire.extension.process.model.deployment.ProcessDefinition;
import tribefire.extension.process.model.deployment.StandardNode;
import tribefire.extension.process.model.deployment.TransitionProcessor;
import tribefire.extension.process.processing.base.pd.wire.contract.TestProcessDefinitionContract;
import tribefire.extension.process.processing.base.pd.wire.contract.TestProcessRequirementsContract;
import tribefire.extension.process.processing.definition.AmountProcessState;
import tribefire.extension.process.processing.definition.RoutingMaps;
import tribefire.extension.process.processing.definition.SimpleProcessState;
import tribefire.pd.editor.api.ProcessDefinitionEditor;

@Managed
public class TestProcessDefinitionSpace implements TestProcessDefinitionContract {

	@Import
	private TestProcessRequirementsContract processors;
	
	@Override
	@Managed
	public ProcessDefinition simpleTaggingProcess() {
		ProcessDefinitionEditor ed = processors.newProcessEditor();
		ProcessDefinition definition = ed.definition();
		definition.setName("Simple Tagging Process");
		
		buildSimpleProcess(ed);

		TransitionProcessor tagger = processors.stateTaggingProcessor();
		SimpleProcessState[] states = SimpleProcessState.values();
		ed.acquireNodes(states).forEach(n -> n.getOnEntered().add(tagger));
		
		return definition;
	}
	
	@Override
	@Managed
	public ProcessDefinition failingByProcessorReasonProcess() {
		ProcessDefinitionEditor ed = processors.newProcessEditor();
		ProcessDefinition definition = ed.definition();
		definition.setName("Failing Process");
		
		buildSimpleProcess(ed);
		
		ed.acquireNode(SimpleProcessState.TWO).getOnEntered().add(processors.failingByReasonTransitionProcessor());
		
		return definition;
	}
	
	@Override
	@Managed
	public ProcessDefinition failingByProcessorExceptionProcess() {
		ProcessDefinitionEditor ed = processors.newProcessEditor();
		ProcessDefinition definition = ed.definition();
		definition.setName("Failing Process");
		
		buildSimpleProcess(ed);
		
		ed.acquireNode(SimpleProcessState.TWO).getOnEntered().add(processors.failingByExceptionTransitionProcessor());
		
		return definition;
	}
	
	@Override
	@Managed
	public ProcessDefinition resumableProcess() {
		ProcessDefinitionEditor ed = processors.newProcessEditor();
		ProcessDefinition definition = ed.definition();
		definition.setName("Resumable Process");

		int layers = 3;
		int nodesPerLayer = 2;
		
		buildSelfRoutedProcess(ed, layers, nodesPerLayer);

		DecoupledInteraction di = processors.create(DecoupledInteraction.T);
		di.setUserInteraction(true);
		
		int layer = 1;

		// add DecoupledInteraction on all nodes of layer 1 (2nd layer)
		for (int s = 0; s < nodesPerLayer; s++) {
			String state = RoutingMaps.state(layer ,s);
			ed.acquireNode(state).setDecoupledInteraction(di);
		}
		
		return definition;
	}
	
	private void buildSimpleProcess(ProcessDefinitionEditor ed) {
		SimpleProcessState[] states = SimpleProcessState.values();
		
		for (SimpleProcessState state: states) { 
			ed.node(state, state.name());
		}

		ed.conditionalRootEdge(SimpleProcessState.ONE, null);
		ed.conditionalEdge(SimpleProcessState.ONE, SimpleProcessState.TWO, null);
		ed.conditionalEdge(SimpleProcessState.TWO, SimpleProcessState.THREE, null);
	}
	
	@Override
	@Managed
	public ProcessDefinition selfRoutingProcess() {
		ProcessDefinitionEditor ed = processors.newProcessEditor();
		ProcessDefinition definition = ed.definition();
		definition.setName("Self Routing Process");
		
		buildSelfRoutedProcess(ed, 3 ,3);
		
		return definition;
	}

	private void buildSelfRoutedProcess(ProcessDefinitionEditor ed, int layers, int nodesPerLayer) {
		TransitionProcessor selfRouting = processors.selfRoutingProcessor();
		
		for (int l = 0; l < layers; l++) {
			for (int n = 0; n < nodesPerLayer; n++) {
				String state = RoutingMaps.state(l,n);

				StandardNode node = ed.node(state, state);
				node.getOnEntered().add(selfRouting);
				
				if (l == 0) {
					ed.rootEdge(state, state);
				}

				int nL = l + 1;
				
				if (nL < layers) {
					for (int s = 0; s < nodesPerLayer; s++) {
						String sucessorState = RoutingMaps.state(nL,s);
						ed.edge(state, sucessorState, state + "-" + sucessorState);
					}
				}
			}
		}
	}
	
	@Override
	@Managed
	public ProcessDefinition amountProcess() {
		ProcessDefinitionEditor ed = processors.newProcessEditor();
		ProcessDefinition definition = ed.definition();
		definition.setName("Amount Process");
		
		ed.conditionalRootEdge(AmountProcessState.DECISION, "init");
		ConditionalEdge lt1000 = ed.conditionalEdge(AmountProcessState.DECISION, AmountProcessState.NORMAL, "lt-1000");
		ConditionalEdge lt10000 = ed.conditionalEdge(AmountProcessState.DECISION, AmountProcessState.APPROVAL, "lt-10000");
		ed.conditionalEdge(AmountProcessState.DECISION, AmountProcessState.SPECIAL_APPROVAL, "default");
		
		ConditionProcessor lt1000Condition = processors.lt1000ConditionProcessor();
		ConditionProcessor lt10000Condition = processors.lt10000ConditionProcessor();

		lt1000.setCondition(lt1000Condition);
		lt10000.setCondition(lt10000Condition);
		
		return definition;
	}
	
}
