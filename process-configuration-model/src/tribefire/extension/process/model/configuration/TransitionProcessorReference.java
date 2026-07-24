package tribefire.extension.process.model.configuration;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface TransitionProcessorReference extends GenericEntity {
	EntityType<TransitionProcessorReference> T = EntityTypes.T(TransitionProcessorReference.class);

	String processorId = "processorId";

	@Mandatory
	String getProcessorId();
	void setProcessorId(String processorId);
}
