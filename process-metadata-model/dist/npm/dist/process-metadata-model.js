import '@dev.hiconic/gm_meta-model';
import '@dev.hiconic/gm_gm-core-api';

import {T, hc} from '@dev.hiconic/hc-js-base';

export const meta = {
	groupId: "tribefire.extension.process",
	artifactId: "process-metadata-model",
	version: "1.0.1",
}

function modelAssembler($, P, _) {
//JSE version=4.0
//BEGIN_TYPES
P.a=$.T("com.braintribe.model.meta.GmMetaModel");
P.b=$.T("com.braintribe.model.meta.GmEntityType");
P.c=$.T("com.braintribe.model.meta.GmProperty");
P.d=$.T("com.braintribe.model.meta.GmStringType");
//END_TYPES
P.e=$.P(P.a,'name');P.f=$.P(P.a,'types');P.g=$.P(P.a,'version');P.h=$.P(P.b,'globalId');P.i=$.P(P.b,'isAbstract');P.j=$.P(P.b,'properties');P.k=$.P(P.b,'superTypes');
P.l=$.P(P.b,'typeSignature');P.m=$.P(P.c,'declaringType');P.n=$.P(P.c,'globalId');P.o=$.P(P.c,'name');P.p=$.P(P.c,'nullable');P.q=$.P(P.c,'type');P.r=$.P(P.d,'typeSignature');
P.s=$.C(P.a);P.t=$.C(P.b);P.u=$.C(P.c);P.v=$.C(P.b);P.w=$.C(P.d);
_=P.s;
$.s(_,P.e,"tribefire.extension.process:process-metadata-model");
$.s(_,P.f,$.S([P.t]));
$.s(_,P.g,"1.0.1");
_=P.t;
$.s(_,P.h,"type:tribefire.extension.process.model.meta.ManageProcessWith");
$.s(_,P.i,$.n);
$.s(_,P.j,$.L([P.u]));
$.s(_,P.k,$.L([P.v]));
$.s(_,P.l,"tribefire.extension.process.model.meta.ManageProcessWith");
_=P.u;
$.s(_,P.m,P.t);
$.s(_,P.n,"property:tribefire.extension.process.model.meta.ManageProcessWith/processDefinitionId");
$.s(_,P.o,"processDefinitionId");
$.s(_,P.p,$.y);
$.s(_,P.q,P.w);
_=P.v;
$.s(_,P.i,$.n);
$.s(_,P.l,"com.braintribe.model.meta.data.EntityTypeMetaData");
_=P.w;
$.s(_,P.r,"string");
return P.s;
[1300];
}

hc.reflection.internal.ensureModel(modelAssembler)

export const ManageProcessWith = T.tribefire.extension.process.model.meta.ManageProcessWith;
