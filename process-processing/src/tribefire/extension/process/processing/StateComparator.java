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

public class StateComparator implements Comparator<Object> {
	public static Comparator<Object> instance = new StateComparator();

	@Override
	public int compare(Object o1, Object o2) {
		if (o1 == o2)
			return 0;
		else if (o1 == null)
			return -1;
		else if (o2 == null)
			return 1;
		else {
			Class<? extends Object> class1 = o1.getClass();
			Class<? extends Object> class2 = o2.getClass();
			if (class1 == class2) {
				@SuppressWarnings("unchecked")
				Comparable<Object> comparable = (Comparable<Object>)o1;
				return comparable.compareTo(o2);
			}
			else {
				return class1.getName().compareTo(class2.getName());
			}
		}
	}
}
