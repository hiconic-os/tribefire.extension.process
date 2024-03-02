// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.process.processing.mgt.processor;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.service.api.result.Neutral;

import tribefire.extension.process.api.model.crtl.NotifyProcessActivity;

public class NotifyProcessActivityProcessor extends ProcessRequestProcessor<NotifyProcessActivity, Neutral> {
	@Override
	protected Maybe<Neutral> processValidatedRequest() {
		processManagerContext.notifyProcessListener(getItemReference(), request.getActivity());
		
		return Maybe.complete(Neutral.NEUTRAL);
	}
}