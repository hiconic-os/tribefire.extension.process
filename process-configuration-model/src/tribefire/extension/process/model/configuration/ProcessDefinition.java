package tribefire.extension.process.model.configuration;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.time.TimeSpan;

@SelectiveInformation("${name}")
public interface ProcessDefinition extends GenericEntity {
	EntityType<ProcessDefinition> T = EntityTypes.T(ProcessDefinition.class);

	String name = "name";
	String nodes = "nodes";
	String edges = "edges";
	String errorNode = "errorNode";
	String gracePeriod = "gracePeriod";
	String onTransit = "onTransit";
	String onError = "onError";

	@Mandatory
	String getName();
	void setName(String name);

	List<Node> getNodes();
	void setNodes(List<Node> nodes);

	List<Edge> getEdges();
	void setEdges(List<Edge> edges);

	Node getErrorNode();
	void setErrorNode(Node errorNode);

	TimeSpan getGracePeriod();
	void setGracePeriod(TimeSpan gracePeriod);

	List<TransitionProcessorReference> getOnTransit();
	void setOnTransit(List<TransitionProcessorReference> onTransit);

	List<TransitionProcessorReference> getOnError();
	void setOnError(List<TransitionProcessorReference> onError);
}
