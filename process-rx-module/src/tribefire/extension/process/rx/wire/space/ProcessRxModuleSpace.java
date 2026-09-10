package tribefire.extension.process.rx.wire.space;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.model.meta.data.constraint.Modifiable;
import com.braintribe.model.meta.selector.RoleSelector;
import com.braintribe.model.processing.meta.editor.EntityTypeMetaDataEditor;
import com.braintribe.model.processing.worker.api.WorkerException;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import hiconic.rx.access.module.api.AccessContract;
import hiconic.rx.access.module.api.AccessServiceModelConfiguration;
import hiconic.rx.locking.api.LockingContract;
import hiconic.rx.messaging.api.MessagingContract;
import hiconic.rx.module.api.service.ModelConfiguration;
import hiconic.rx.module.api.service.ModelConfigurations;
import hiconic.rx.module.api.wire.RxModuleContract;
import hiconic.rx.module.api.wire.RxPlatformContract;
import hiconic.rx.worker.api.WorkerContract;
import tribefire.extension.process._ProcessApiModel_;
import tribefire.extension.process._ProcessDataModel_;
import tribefire.extension.process.api.ConditionProcessor;
import tribefire.extension.process.api.TransitionProcessor;
import tribefire.extension.process.api.model.ProcessManagerRequest;
import tribefire.extension.process.data.model.ProcessItem;
import tribefire.extension.process.model.configuration.ProcessDefinitionsConfiguration;
import tribefire.extension.process.model.configuration.ProcessManagerConfiguration;
import tribefire.extension.process.rx.api.ProcessRxContract;
import tribefire.extension.process.rx.impl.ProcessExpertRegistry;
import tribefire.extension.process.rx.impl.CmdProcessDefinitionResolver;
import tribefire.extension.process.rx.impl.ProcessDefinitionsValidator;
import tribefire.extension.process.rx.processing.mgt.ProcessManager;

@Managed
public class ProcessRxModuleSpace implements RxModuleContract, ProcessRxContract {

	@Import private RxPlatformContract platform;
	@Import private AccessContract access;
	@Import private LockingContract locking;
	@Import private MessagingContract messaging;
	@Import private WorkerContract worker;

	@Override
	public void configureModels(ModelConfigurations configurations) {
		ModelConfiguration apiConfiguration = configurations.extendedModel(configuredProcessApiModel, _ProcessApiModel_.reflection);
		AccessServiceModelConfiguration apiAccessConfiguration = access.accessModelConfigurations().serviceModelConfiguration(apiConfiguration);
		apiAccessConfiguration.bindAccessRequest(ProcessManagerRequest.T, this::processManager);

		ModelConfiguration dataConfiguration = configurations.extendedModel(configuredProcessDataModel, _ProcessDataModel_.reflection);
		dataConfiguration.configureModel(editor -> {
			EntityTypeMetaDataEditor processItem = editor.onEntityType(ProcessItem.T);
			String[] privilegedProperties = { ProcessItem.state, ProcessItem.previousState, ProcessItem.activity, ProcessItem.transitionPhase,
					ProcessItem.transitionProcessorId, ProcessItem.startedAt, ProcessItem.lastTransit, ProcessItem.endedAt, ProcessItem.logSequence };
			Modifiable internallyModifiable = internallyModifiable();
			for (String property : privilegedProperties)
				processItem.addPropertyMetaData(property, internallyModifiable);
		});
	}

	@Override
	public void onDeploy() {
		// at this point every module contributed its definitions, so the whole graph can be checked
		ProcessDefinitionsValidator.validate(definitions());

		try {
			worker.manager().deploy(processManager());
		} catch (WorkerException e) {
			throw new IllegalStateException("Could not deploy the process revival worker", e);
		}
	}

	@Override
	public void registerTransitionProcessor(String id, Supplier<? extends TransitionProcessor<?>> supplier) {
		processExpertRegistry().registerTransitionProcessor(id, supplier);
	}

	@Override
	public void registerConditionProcessor(String id, Supplier<? extends ConditionProcessor<?>> supplier) {
		processExpertRegistry().registerConditionProcessor(id, supplier);
	}

	@Managed
	private ProcessExpertRegistry processExpertRegistry() {
		return new ProcessExpertRegistry();
	}

	@Override
	@Managed
	public ProcessDefinitionsConfiguration definitions() {
		return platform.configuration().readConfig(ProcessDefinitionsConfiguration.T).get();
	}

	@Managed
	private ProcessManager processManager() {
		ProcessManager bean = new ProcessManager();
		bean.setEvaluator(platform.serviceProcessing().systemEvaluator());
		bean.setLocking(locking.locking());
		bean.setMessagingSessionProvider(messaging.sessionProvider());
		bean.setSystemSessionFactory(access.systemSessionFactory());
		bean.setTaskScheduler(platform.execution().taskScheduler());
		bean.setMonitoredAccessIdsSupplier(() -> definitions().getMonitoredAccessIds().stream().toList());
		applyIfSet(managerConfiguration().getMonitorInterval(), bean::setMonitorInterval);
		applyIfSet(managerConfiguration().getUnattendedThreshold(), bean::setUnattendedThreshold);
		bean.setProcessDefinitionResolver(processDefinitionResolver());
		bean.setProcessExpertResolver(processExpertRegistry());
		return bean;
	}

	@Managed
	private ProcessManagerConfiguration managerConfiguration() {
		return platform.configuration().readConfig(ProcessManagerConfiguration.T).get();
	}

	/** Keeps the default of the process manager when the configuration says nothing about a timing. */
	private static <V> void applyIfSet(V value, Consumer<V> setter) {
		if (value != null)
			setter.accept(value);
	}

	@Managed
	private CmdProcessDefinitionResolver processDefinitionResolver() {
		return new CmdProcessDefinitionResolver(this::definitions);
	}

	@Managed
	private Modifiable internallyModifiable() {
		Modifiable result = Modifiable.T.create();
		result.setSelector(internalRoleSelector());
		return result;
	}

	@Managed
	private RoleSelector internalRoleSelector() {
		RoleSelector result = RoleSelector.T.create();
		result.getRoles().add("tf-internal");
		return result;
	}
}
