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
package com.braintribe.qa.tribefire.test;

import java.util.Set;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public abstract interface Parent extends StandardIdentifiable {

	EntityType<Parent> T = EntityTypes.T(Parent.class);
	public static final String children = "children";
	public static final String name = "name";
	public static final String lastname = "lastname";
	public static final String parents = "parents";

	public abstract Set<Child> getChildren();

	public abstract void setChildren(Set<Child> paramSet);

	public abstract Set<Parent> getParents();

	public abstract void setParents(Set<Parent> paramSet);

	public abstract String getName();

	public abstract void setName(String paramString);

	public abstract String getLastname();

	public abstract void setLastname(String paramString);
}
