// ************
// Types
// ************

import '@dev.hiconic/gm_meta-model';
import '@dev.hiconic/gm_gm-core-api';

import { T } from '@dev.hiconic/hc-js-base';

export declare namespace meta {
	const groupId: string;
	const artifactId: string;
	const version: string;
}

export import ManageProcessWith = T.tribefire.extension.process.model.meta.ManageProcessWith;

declare module '@dev.hiconic/hc-js-base' {

	namespace T.tribefire.extension.process.model.meta {

		const ManageProcessWith: hc.reflection.EntityType<ManageProcessWith>;
		type ManageProcessWith = T.com.braintribe.model.meta.data.EntityTypeMetaData &
		  Entity<"tribefire.extension.process.model.meta.ManageProcessWith", {
			processDefinitionId: string;
		}>;

	}

}
