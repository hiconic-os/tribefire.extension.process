// ============================================================================
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.model.processdefrep;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface SwimLaneRepresentation extends HasDimension, HasSize, HasColor{

	EntityType<SwimLaneRepresentation> T = EntityTypes.T(SwimLaneRepresentation.class);
	
	public void setText(String text);
	public String getText();
	
	public void setTextPosition(TextPosition textPosition);
	public TextPosition getTextPosition();

}
