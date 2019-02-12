package org.orcid.api.member.root.delegator;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public interface RootApiServiceDelegator {

    Response registerWebhook(UriInfo uriInfo, String orcid, String webhookUri);

    Response unregisterWebhook(UriInfo uriInfo, String orcid, String webhookUri);
}
