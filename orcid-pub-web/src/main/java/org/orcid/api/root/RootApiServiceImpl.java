package org.orcid.api.root;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.orcid.api.common.T2OrcidApiService;
import org.orcid.core.oauth.OAuthError;
import org.orcid.core.oauth.OAuthErrorUtils;
import org.orcid.core.oauth.OrcidClientCredentialEndPointDelegator;
import org.springframework.http.HttpStatus;

@Path("/")
public class RootApiServiceImpl {

    @Resource
    private OrcidClientCredentialEndPointDelegator orcidClientCredentialEndPointDelegator;

    /**
     * @param formParams
     * @return
     */
    @POST
    @Path(T2OrcidApiService.OAUTH_TOKEN)
    @Produces(value = { MediaType.APPLICATION_JSON })
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response obtainOauth2TokenPost(@HeaderParam("Authorization") @DefaultValue(StringUtils.EMPTY) String authorization, @FormParam("grant_type") String grantType,
            MultivaluedMap<String, String> formParams) {
        try {
            return orcidClientCredentialEndPointDelegator.obtainOauth2Token(authorization, formParams);
        } catch (Exception e) {
            OAuthError error = OAuthErrorUtils.getOAuthError(e);
            HttpStatus status = HttpStatus.valueOf(error.getResponseStatus().getStatusCode());
            return Response.status(status.value()).entity(error).build();
        }
    }
}
