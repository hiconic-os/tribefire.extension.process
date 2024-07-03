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
package tribefire.extension.process.processing.mgt.processor;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.service.api.result.Neutral;

import tribefire.extension.process.api.model.ctrl.NotifyProcessActivity;

public class NotifyProcessActivityProcessor extends ProcessRequestProcessor<NotifyProcessActivity, Neutral> {
	@Override
	protected Maybe<Neutral> processValidatedRequest() {
		processManagerContext.notifyProcessListener(getItemReference(), request.getActivity());
		
		return Maybe.complete(Neutral.NEUTRAL);
	}
}