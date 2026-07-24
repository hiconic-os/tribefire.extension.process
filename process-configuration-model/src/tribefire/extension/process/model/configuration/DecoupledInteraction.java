package tribefire.extension.process.model.configuration;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/** Activity performed outside the process manager which eventually resumes the process. */
@SelectiveInformation("${name}")
public interface DecoupledInteraction extends GenericEntity {
	EntityType<DecoupledInteraction> T = EntityTypes.T(DecoupledInteraction.class);

	String name = "name";
	String description = "description";

	String getName();
	void setName(String name);

	String getDescription();
	void setDescription(String description);

}
