package tribefire.extension.process.rx.test.model;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.process.annotation.ManageProcessWith;

/** Drives the routing tests: conditions, condition expressions, and a state demanded by a transition processor. */
@ManageProcessWith(RoutingTestProcess.DEFINITION_ID)
public interface RoutingTestProcess extends TestProcess {

	EntityType<RoutingTestProcess> T = EntityTypes.T(RoutingTestProcess.class);

	String DEFINITION_ID = "test.routing";

	String approved = "approved";
	String demandedState = "demandedState";

	/** Read by the condition processor of the graph. */
	boolean getApproved();
	void setApproved(boolean approved);

	/** If set, a transition processor demands this state with {@code continueWithState}. */
	String getDemandedState();
	void setDemandedState(String demandedState);
}
