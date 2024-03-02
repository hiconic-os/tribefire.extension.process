// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
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
