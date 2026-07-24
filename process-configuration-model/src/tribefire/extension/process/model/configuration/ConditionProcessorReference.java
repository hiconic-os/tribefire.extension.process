package tribefire.extension.process.model.configuration;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface ConditionProcessorReference extends GenericEntity {
	EntityType<ConditionProcessorReference> T = EntityTypes.T(ConditionProcessorReference.class);

	String processorId = "processorId";

	@Mandatory
	String getProcessorId();
	void setProcessorId(String processorId);
}
