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
package tribefire.extension.process.test;

import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.resource.Resource;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.product.rat.imp.ImpException;
import com.braintribe.qa.tribefire.test.Child;
import com.braintribe.qa.tribefire.test.Father;
import com.braintribe.qa.tribefire.test.Invoice;
import com.braintribe.qa.tribefire.test.Mother;
import com.braintribe.qa.tribefire.test.Parent;
import com.braintribe.qa.tribefire.test.Task;
import com.braintribe.qa.tribefire.test.TaskState;

public class QaTestHelper {

	public static String GROUP_ID = TaskState.class.getPackage().getName();

	public static GmMetaModel createFamilyModel(ImpApi imp, String familyModelName) {
		/*Object debug = */imp.model().entityType().find(Child.T);

		if (imp.model().entityType().find(Child.T).isPresent()) {
			throw new ImpException("There exists already a FamilyModel or at least some parts of one (checked for Child.T). Can't create a new one");
		}

		imp.model().allLike(familyModelName).deleteRecursively();
		// @formatter:off
		GmMetaModel familyMetaModel = imp.model().create(familyModelName).addDependencies("com.braintribe.gm:basic-resource-model").get();

		imp.commit();

		GmEnumType taskStateType = imp.model().enumType().create(GROUP_ID, TaskState.class.getSimpleName(), familyMetaModel)
						.addConstants("SCHEDULED", "STARTED", "COMPLETED").get();

		imp.commit();
		imp.model().entityType().createPlainTypes(familyMetaModel, Parent.T, Mother.T, Father.T, Child.T, Task.T, Invoice.T);

		imp.commit();

		imp.model().entityType()
	  				.with(Parent.T)
						.addProperty(Parent.name, SimpleType.TYPE_STRING)
						.addProperty(Parent.lastname, SimpleType.TYPE_STRING)
						.addSetProperty(Parent.children, Child.T)
						.addSetProperty(Parent.parents, Parent.T);
		imp.model().entityType().with(Father.T)
						.addProperty(Father.wife, Mother.T)
						.addInheritance(Parent.T);
		imp.model().entityType().with(Mother.T)
						.addProperty(Mother.husband, Father.T)
						.addInheritance(Parent.T);
		imp.model().entityType().with(Child.T)
						.addProperty(Child.name, SimpleType.TYPE_STRING)
						.addListProperty(Child.tasks, Task.T)
						.addProperty(Child.logo, Resource.T);
		imp.model().entityType().with(Invoice.T)
						.addProperty(Invoice.invoiceState, SimpleType.TYPE_STRING)
						.addProperty(Invoice.total, SimpleType.TYPE_DOUBLE);
		imp.model().entityType().with(Task.T)
						.addProperty(Task.name, SimpleType.TYPE_STRING)
						.addProperty(Task.state, taskStateType);
		imp.commit();
		// @formatter:on
		return familyMetaModel;
	}

	public static CollaborativeSmoodAccess createAndDeployTestFamilySmoodAccess(ImpApi imp, String familyModelName, String accessExternalId) {

		GmMetaModel familyModel = createFamilyModel(imp, familyModelName);

		CollaborativeSmoodAccess smood = imp.deployable().access().createCsa(accessExternalId, accessExternalId, familyModel).get();
		imp.commit();
		imp.deployable(smood).deployRequest().call();

		return smood;
	}

	public static void ensureSmoodAccess(ImpApi imp, String domainId, GmMetaModel model) {
		if (imp.deployable().access().find(domainId).isPresent())
			return;
		
		CollaborativeSmoodAccess smood = imp.deployable().access().createCsa(domainId, domainId, model).get();
		imp.commit();
		imp.deployable(smood).deployRequest().call();
	}
}
