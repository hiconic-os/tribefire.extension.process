package tribefire.extension.process.model.configuration;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@SelectiveInformation("edge: ${from.state} - ${to.state}")
public interface Edge extends GenericEntity {
	EntityType<Edge> T = EntityTypes.T(Edge.class);

	String name = "name";
	String description = "description";
	String from = "from";
	String to = "to";
	String condition = "condition";
	String onTransit = "onTransit";

	@Mandatory
	String getName();
	void setName(String name);

	String getDescription();
	void setDescription(String description);

	Node getFrom();
	void setFrom(Node from);

	Node getTo();
	void setTo(Node to);

	ConditionProcessorReference getCondition();
	void setCondition(ConditionProcessorReference condition);

	List<TransitionProcessorReference> getOnTransit();
	void setOnTransit(List<TransitionProcessorReference> onTransit);
}
