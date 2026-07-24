package tribefire.extension.process.model.configuration;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.time.TimeSpan;

@SelectiveInformation("node: ${state}")
public interface Node extends GenericEntity {
	EntityType<Node> T = EntityTypes.T(Node.class);

	String state = "state";
	String name = "name";
	String description = "description";
	String decoupledInteraction = "decoupledInteraction";
	String gracePeriod = "gracePeriod";
	String errorNode = "errorNode";
	String overdueNode = "overdueNode";
	String onEntered = "onEntered";
	String onLeft = "onLeft";
	String onError = "onError";

	String getState();
	void setState(String state);

	String getName();
	void setName(String name);

	String getDescription();
	void setDescription(String description);

	DecoupledInteraction getDecoupledInteraction();
	void setDecoupledInteraction(DecoupledInteraction decoupledInteraction);

	TimeSpan getGracePeriod();
	void setGracePeriod(TimeSpan gracePeriod);

	Node getErrorNode();
	void setErrorNode(Node errorNode);

	Node getOverdueNode();
	void setOverdueNode(Node overdueNode);

	List<TransitionProcessorReference> getOnEntered();
	void setOnEntered(List<TransitionProcessorReference> onEntered);

	List<TransitionProcessorReference> getOnLeft();
	void setOnLeft(List<TransitionProcessorReference> onLeft);

	List<TransitionProcessorReference> getOnError();
	void setOnError(List<TransitionProcessorReference> onError);
}
