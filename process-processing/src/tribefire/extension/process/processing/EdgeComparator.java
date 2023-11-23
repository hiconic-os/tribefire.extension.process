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
package tribefire.extension.process.processing;

import java.util.Comparator;

import tribefire.extension.process.model.deployment.Edge;

public class EdgeComparator implements Comparator<Edge> {
	public static Comparator<Edge> instance = new EdgeComparator();

	@SuppressWarnings("rawtypes")
	@Override
	public int compare(Edge e1, Edge e2) {
		if (e1 == e2)
			return 0;
		else 
			return e1.<Comparable> getId().compareTo(e2.getId());
	}
	
}
