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
package tribefire.pd.expert.editor.impl;

import java.util.Collections;
import java.util.List;

import tribefire.extension.process.model.deployment.Node;
import tribefire.extension.process.model.deployment.TransitionProcessor;
import tribefire.pd.expert.editor.api.NodeEditor;

public class BasicNodeEditor extends AbstractProcessElementEditor<Node> implements NodeEditor {
	
	public BasicNodeEditor(Node node) {
		this(Collections.singletonList(node));
	}
	
	public BasicNodeEditor(List<Node> nodes) {
		super(nodes);
	}
	
	@Override
	public void addOnEnter(TransitionProcessor transitionProcessor) {
		elements().forEach(e -> e.getOnEntered().add(transitionProcessor));
	}

}
