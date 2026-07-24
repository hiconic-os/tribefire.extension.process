package tribefire.extension.process.rx.api;

import hiconic.rx.module.api.service.ModelSymbol;
import tribefire.extension.process._ProcessApiModel_;
import tribefire.extension.process._ProcessDataModel_;

/**
 * Prepared process models that applications can include without knowing how the
 * process manager is implemented or bound.
 */
public interface ProcessModelSymbols {
	ModelSymbol configuredProcessApiModel = ModelSymbol.configured(_ProcessApiModel_.reflection);
	ModelSymbol configuredProcessDataModel = ModelSymbol.configured(_ProcessDataModel_.reflection);
}
