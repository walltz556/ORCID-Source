package org.orcid.api.member.root;

import static org.orcid.api.common.T2OrcidApiService.OAUTH_TOKEN;
import static org.orcid.core.api.OrcidApiConstants.ORCID_JSON;
import static org.orcid.core.api.OrcidApiConstants.ORCID_XML;
import static org.orcid.core.api.OrcidApiConstants.VND_ORCID_JSON;
import static org.orcid.core.api.OrcidApiConstants.VND_ORCID_XML;
import static org.orcid.core.api.OrcidApiConstants.WEBHOOKS_PATH;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.orcid.api.member.root.delegator.RootApiServiceDelegator;
import org.orcid.core.oauth.OrcidClientCredentialEndPointDelegator;

@Path("/")
public class RootApiServiceImpl {
    
    @Context
    private UriInfo uriInfo;
    
    @Resource
    private OrcidClientCredentialEndPointDelegator orcidClientCredentialEndPointDelegator;
    
    @Resource
    private RootApiServiceDelegator rootApiServiceDelegator;
    
    /**
     * 
     * @param formParams
     * @return
     */
    @POST
    @Path(OAUTH_TOKEN)
    @Produces(value = { MediaType.APPLICATION_JSON })
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response obtainOauth2TokenPost(@HeaderParam("Authorization") @DefaultValue(StringUtils.EMPTY) String authorization, @FormParam("grant_type") String grantType, MultivaluedMap<String, String> formParams) {
        return orcidClientCredentialEndPointDelegator.obtainOauth2Token(authorization, formParams);
    }

    /**
     * Register a new webhook to a specific client.
     * 
     * @param orcid
     *            the ORCID that corresponds to the user's record
     * @param webhook_uri
     *            the webhook that will be added to the user
     * @return
     * */
    @PUT
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML })
    @Path(WEBHOOKS_PATH)
    public Response registerWebhookXML(@PathParam("orcid") String orcid, @PathParam("webhook_uri") String webhookUri) {
        return rootApiServiceDelegator.registerWebhook(uriInfo, orcid, webhookUri);
    }

    /**
     * Register a new webhook to a specific client.
     * 
     * @param orcid
     *            the ORCID that corresponds to the user's record
     * @param webhook_uri
     *            the webhook that will be added to the user
     * @return
     * */
    @PUT
    @Produces(value = { VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(WEBHOOKS_PATH)
    public Response registerWebhookJson(@PathParam("orcid") String orcid, @PathParam("webhook_uri") String webhookUri) {
        return rootApiServiceDelegator.registerWebhook(uriInfo, orcid, webhookUri);
    }

    /**
     * Unregister a webhook from specific client.
     * 
     * @param orcid
     *            the ORCID that corresponds to the user's record
     * @param webhook_uri
     *            the webhook that will be deleted from the user
     * @return
     * */
    @DELETE
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML })
    @Path(WEBHOOKS_PATH)
    public Response unregisterWebhookXML(@PathParam("orcid") String orcid, @PathParam("webhook_uri") String webhookUri) {
        return rootApiServiceDelegator.unregisterWebhook(uriInfo, orcid, webhookUri);
    }

    /**
     * Unregister a webhook from specific client.
     * 
     * @param orcid
     *            the ORCID that corresponds to the user's record
     * @param webhook_uri
     *            the webhook that will be deleted from the user
     * @return
     * */
    @DELETE
    @Produces(value = { VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(WEBHOOKS_PATH)
    public Response unregisterWebhookJson(@PathParam("orcid") String orcid, @PathParam("webhook_uri") String webhookUri) {
        return rootApiServiceDelegator.unregisterWebhook(uriInfo, orcid, webhookUri);
    }
}
