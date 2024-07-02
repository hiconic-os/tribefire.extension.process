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

import java.util.List;

import com.braintribe.utils.CollectionTools;

import tribefire.extension.process.model.deployment.Node;
import tribefire.extension.process.model.deployment.ProcessElement;
import tribefire.pd.expert.editor.api.ProcessElementEditor;

public abstract class AbstractProcessElementEditor<T extends ProcessElement> implements ProcessElementEditor {

	private List<T> elements;

	public AbstractProcessElementEditor(List<T> elements) {
		this.elements = elements;
	}

	@Override
	public T element() {
		return CollectionTools.getFirstElement(elements);
	}
	
	@Override
	public List<T> elements() {
		return elements;
	}

	@Override
	public void setErrorNode(Node errorNode) {
		elements.forEach(e -> e.setErrorNode(errorNode));
	}
}
