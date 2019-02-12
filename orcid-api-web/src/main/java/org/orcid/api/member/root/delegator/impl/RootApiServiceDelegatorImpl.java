package org.orcid.api.member.root.delegator.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.orcid.api.member.root.delegator.RootApiServiceDelegator;
import org.orcid.core.exception.OrcidBadRequestException;
import org.orcid.core.exception.OrcidClientNotFoundException;
import org.orcid.core.exception.OrcidNotFoundException;
import org.orcid.core.manager.v3.OrcidSecurityManager;
import org.orcid.jaxb.model.message.ScopePathType;
import org.orcid.persistence.jpa.entities.ClientDetailsEntity;
import org.orcid.persistence.jpa.entities.ProfileEntity;
import org.orcid.persistence.jpa.entities.WebhookEntity;
import org.orcid.persistence.jpa.entities.keys.WebhookEntityPk;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class RootApiServiceDelegatorImpl implements RootApiServiceDelegator {

    @Resource(name = "orcidSecurityManagerV3")
    private OrcidSecurityManager orcidSecurityManager;
    
    /**
     * Register a new webhook to the profile. As with all calls, if the message
     * contains any other elements, a 400 Bad Request will be returned.
     * 
     * @param orcid
     *            the identifier of the profile to add the webhook
     * @param uriInfo
     *            an uri object containing the webhook
     * @return If successful, returns a 2xx.
     * */
    @Override
    public Response registerWebhook(UriInfo uriInfo, String orcid, String webhookUri) {
        orcidSecurityManager.checkScopes(ScopePathType.WEBHOOK);
        @SuppressWarnings("unused")
        URI validatedWebhookUri = null;
        try {
            validatedWebhookUri = new URI(webhookUri);
        } catch (URISyntaxException e) {
                Object params[] = {webhookUri};
            throw new OrcidBadRequestException(localeManager.resolveMessage("apiError.badrequest_incorrect_webhook.exception", params));
        }

        ProfileEntity profile = profileEntityCacheManager.retrieve(orcid);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ClientDetailsEntity clientDetails = null;
        String clientId = null;
        if (OAuth2Authentication.class.isAssignableFrom(authentication.getClass())) {
            OAuth2Request authorizationRequest = ((OAuth2Authentication) authentication).getOAuth2Request();
            clientId = authorizationRequest.getClientId();
            clientDetails = clientDetailsManager.findByClientId(clientId);
        }
        if (profile != null && clientDetails != null) {
            WebhookEntityPk webhookPk = new WebhookEntityPk(profile, webhookUri);
            WebhookEntity webhook = webhookManager.find(webhookPk);
            boolean isNew = webhook == null;
            if (isNew) {
                webhook = new WebhookEntity();
                webhook.setProfile(profile);
                webhook.setDateCreated(new Date());
                webhook.setEnabled(true);
                webhook.setUri(webhookUri);
                webhook.setClientDetails(clientDetails);
            }
            webhookManager.update(webhook);
            return isNew ? Response.created(uriInfo.getAbsolutePath()).build() : Response.noContent().build();
        } else if (profile == null) {
                Map<String, String> params = new HashMap<String, String>();
                params.put("orcid", orcid);
            throw new OrcidNotFoundException(params);
        } else {
                Map<String, String> params = new HashMap<String, String>();
                params.put("client", clientId);
            throw new OrcidClientNotFoundException(params);
        }
    }

    /**
     * Unregister a webhook from a profile. As with all calls, if the message
     * contains any other elements, a 400 Bad Request will be returned.
     * 
     * @param orcid
     *            the identifier of the profile to unregister the webhook
     * @param uriInfo
     *            an uri object containing the webhook that will be unregistred
     * @return If successful, returns a 204 No content.
     * */
    @Override
    public Response unregisterWebhook(UriInfo uriInfo, String orcid, String webhookUri) {
        orcidSecurityManager.checkScopes(ScopePathType.WEBHOOK);
        ProfileEntity profile = profileEntityCacheManager.retrieve(orcid);
        if (profile != null) {
            WebhookEntityPk webhookPk = new WebhookEntityPk(profile, webhookUri);
            WebhookEntity webhook = webhookManager.find(webhookPk);
            if (webhook == null) {
                Map<String, String> params = new HashMap<String, String>();
                params.put("orcid", orcid);
                params.put("uri", webhookUri);
                throw new OrcidWebhookNotFoundException(params);
            } else {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String clientId = null;
                if (OAuth2Authentication.class.isAssignableFrom(authentication.getClass())) {
                    OAuth2Request authorizationRequest = ((OAuth2Authentication) authentication).getOAuth2Request();
                    clientId = authorizationRequest.getClientId();
                }
                // Check if user can unregister this webhook
                if (webhook.getClientDetails().getId().equals(clientId)) {
                    webhookManager.delete(webhookPk);
                    return Response.noContent().build();
                } else {
                    // Throw 403 exception: user is not allowed to unregister
                    // that webhook
                    throw new OrcidForbiddenException(localeManager.resolveMessage("apiError.forbidden_unregister_webhook.exception"));
                }
            }
        } else {
                Map<String, String> params = new HashMap<String, String>();
                params.put("orcid", orcid);
            throw new OrcidNotFoundException(params);
        }
    }

}
