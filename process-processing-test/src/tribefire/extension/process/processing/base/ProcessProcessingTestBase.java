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
package tribefire.extension.process.processing.base;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

import tribefire.extension.process.processing.base.wire.ProcessProcessingTestWireModule;
import tribefire.extension.process.processing.base.wire.contract.ProcessProcessingTestContract;
import tribefire.extension.process.processing.base.wire.contract.TestPlatformContract;

public abstract class ProcessProcessingTestBase {

	protected static WireContext<ProcessProcessingTestContract> context;
	protected static Evaluator<ServiceRequest> evaluator;
	protected static ProcessProcessingTestContract testContract;

	@BeforeClass
	public static void beforeClass() {
		context = Wire.context(ProcessProcessingTestWireModule.INSTANCE);
		testContract = context.contract();
		evaluator = testContract.evaluator();
		context.contract(TestPlatformContract.class).startWorkerManager();
	}

	@AfterClass
	public static void afterClass() {
		context.shutdown();
	}

}
