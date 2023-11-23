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
package tribefire.pd.expert.editor.impl;

import java.util.Collections;
import java.util.List;

import tribefire.extension.process.model.deployment.StandardNode;
import tribefire.extension.process.model.deployment.TransitionProcessor;
import tribefire.pd.expert.editor.api.StandardNodeEditor;

public class BasicStandardNodeEditor extends AbstractProcessElementEditor<StandardNode> implements StandardNodeEditor {

	public BasicStandardNodeEditor(StandardNode element) {
		this(Collections.singletonList(element));
	}
	
	public BasicStandardNodeEditor(List<StandardNode> elements) {
		super(elements);
	}

	@Override
	public void addOnEnter(TransitionProcessor transitionProcessor) {
		elements().forEach(e -> e.getOnEntered().add(transitionProcessor));
	}

	@Override
	public void addOnLeft(TransitionProcessor transitionProcessor) {
		elements().forEach(e -> e.getOnLeft().add(transitionProcessor));
	}

}
