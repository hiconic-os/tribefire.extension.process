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
package tribefire.extension.process.processing.experts;

import tribefire.extension.process.api.TransitionProcessorContext;
import tribefire.extension.process.test.model.FailingByExceptionTestProcess;

public class FailingByExceptionTransitionProcessor extends TestTransitionProcessor<FailingByExceptionTestProcess> {
	
	@Override
	public void process(TransitionProcessorContext<FailingByExceptionTestProcess> context) {
		throw new RuntimeException("Canceled intentionally by transition processor");
	}
	
}
