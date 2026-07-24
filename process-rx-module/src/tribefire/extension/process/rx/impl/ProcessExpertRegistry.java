package tribefire.extension.process.rx.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import tribefire.extension.process.api.ConditionProcessor;
import tribefire.extension.process.api.TransitionProcessor;
import tribefire.extension.process.rx.api.ProcessExpertResolver;

public class ProcessExpertRegistry implements ProcessExpertResolver {

	private final Map<String, Supplier<? extends TransitionProcessor<?>>> transitionProcessors = new ConcurrentHashMap<>();
	private final Map<String, Supplier<? extends ConditionProcessor<?>>> conditionProcessors = new ConcurrentHashMap<>();

	public void registerTransitionProcessor(String id, Supplier<? extends TransitionProcessor<?>> supplier) {
		registerUnique(transitionProcessors, id, supplier, "transition processor");
	}

	public void registerConditionProcessor(String id, Supplier<? extends ConditionProcessor<?>> supplier) {
		registerUnique(conditionProcessors, id, supplier, "condition processor");
	}

	@Override
	public TransitionProcessor<?> resolveTransitionProcessor(String processorId) {
		return require(transitionProcessors, processorId, "transition processor");
	}

	@Override
	public ConditionProcessor<?> resolveConditionProcessor(String processorId) {
		return require(conditionProcessors, processorId, "condition processor");
	}

	private static <T> void registerUnique(Map<String, Supplier<? extends T>> registry, String id, Supplier<? extends T> supplier, String kind) {
		if (id == null || id.isBlank())
			throw new IllegalArgumentException("A " + kind + " id must not be blank");
		if (supplier == null)
			throw new IllegalArgumentException("A " + kind + " supplier must not be null");
		if (registry.putIfAbsent(id, supplier) != null)
			throw new IllegalStateException("Duplicate " + kind + " id: " + id);
	}

	private static <T> T require(Map<String, Supplier<? extends T>> registry, String id, String kind) {
		Supplier<? extends T> supplier = registry.get(id);
		if (supplier == null)
			throw new IllegalStateException("No " + kind + " registered for id: " + id);
		return supplier.get();
	}
}
