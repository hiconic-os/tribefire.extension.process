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
package tribefire.pd.expert.editor.api;

import java.util.List;

import tribefire.extension.process.model.deployment.Edge;
import tribefire.extension.process.model.deployment.TransitionProcessor;

public interface EdgeEditor extends ProcessElementEditor {
	@Override
	Edge element();
	
	void addOnTransit(TransitionProcessor transitionProcessor);
	
	@Override
	List<? extends Edge> elements();
}
