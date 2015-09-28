/**
 * =============================================================================
 *
 * ORCID (R) Open Source
 * http://orcid.org
 *
 * Copyright (c) 2012-2014 ORCID, Inc.
 * Licensed under an MIT-Style License (MIT)
 * http://orcid.org/open-source-license
 *
 * This copyright and license information (including a link to the full license)
 * shall be included in its entirety in all copies or substantial portion of
 * the software.
 *
 * =============================================================================
 */
package org.orcid.frontend.web.controllers;

import java.util.HashMap;
import java.util.Locale;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.orcid.core.locale.LocaleManager;
import org.orcid.core.manager.InternalSSOManager;
import org.orcid.core.utils.JsonUtils;
import org.orcid.jaxb.model.message.OrcidProfile;
import org.orcid.pojo.UserStatus;
import org.orcid.pojo.ajaxForm.PojoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;
import org.springframework.web.servlet.support.RequestContextUtils;

@Controller
public class HomeController extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HomeController.class);

    @Resource
    private LocaleManager localeManager;
    
    @Resource
    private InternalSSOManager internalSSOManager;

// @formatter:off
//    @RequestMapping(value = "/")
//    public ModelAndView homeHandler(HttpServletRequest request) {
//        StringBuilder newUri = new StringBuilder(request.getRequestURL());
//        newUri.insert(newUri.indexOf("://") + 3, "about.");
//        newUri.insert(0, "redirect:");
//        return new ModelAndView(newUri.toString());
//    }
// @formatter:on

    // freindly link to allow language switching
    @RequestMapping(value = "/home")
    public ModelAndView homeRedirect(HttpServletRequest request) {
        return new ModelAndView("redirect:/");

    }

    @RequestMapping(value = "/tomcatUp.json")
    @Produces(value = { MediaType.APPLICATION_JSON })
    public @ResponseBody
    String tomcatUp(HttpServletRequest request) throws NoSuchRequestHandlingMethodException {
        request.setAttribute("isMonitoring", true);
        return "{tomcatUp:true}";
    }

    @RequestMapping(value = "/robots.txt")
    public String dynamicRobots(HttpServletRequest request) throws NoSuchRequestHandlingMethodException {
        String requestedDomain = request.getServerName();
        if (domainsAllowingRobots.contains(requestedDomain)) {
            throw new NoSuchRequestHandlingMethodException(request);
        }
        return "robots";
    }

    @RequestMapping(value = "/lang.json")
    @Produces(value = { MediaType.APPLICATION_JSON })
    public @ResponseBody
    org.orcid.pojo.Local langJson(HttpServletRequest request, @RequestParam(value = "lang", required = false) String lang) throws NoSuchRequestHandlingMethodException {
        if (lang != null) {
            String orcid = getRealUserOrcid();
            if (orcid != null) {
                OrcidProfile existingProfile = orcidProfileManager.retrieveOrcidProfile(orcid);
                org.orcid.jaxb.model.message.Locale locale = existingProfile.getOrcidPreferences().getLocale();
                if (!locale.value().equals(lang)) {
                    try {
                        existingProfile.getOrcidPreferences().setLocale(org.orcid.jaxb.model.message.Locale.fromValue(lang));
                        orcidProfileManager.updateOrcidPreferences(existingProfile);
                    } catch (Exception e) {
                        LOGGER.error("langJson exception", e);
                    } catch (Throwable t) {
                        LOGGER.error("langJson Throwable", t);
                    }
                }
            }
        }

        Locale locale = RequestContextUtils.getLocale(request);
        return localeManager.getJavascriptMessages(locale);

    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/userStatus.json")
    @Produces(value = { MediaType.APPLICATION_JSON })
    public @ResponseBody
    Object getUserStatusJson(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "logUserOut", required = false) Boolean logUserOut)
            throws NoSuchRequestHandlingMethodException {

        String orcid = getCurrentUserOrcid();
        
        if (logUserOut != null && logUserOut.booleanValue()) {
            SecurityContextHolder.clearContext();
            
            if(request.getSession(false) != null) {
                request.getSession().invalidate();
            }                       
            
            Cookie [] cookies = request.getCookies();            
            //Delete cookie and token associated with that cookie
            if(cookies != null) {
                for(Cookie cookie : cookies) {
                    if(InternalSSOManager.COOKIE_NAME.equals(cookie.getName())) {
                        try {
                            //If it is a valid cookie, extract the orcid value and remove the token and the cookie                        
                            HashMap<String, String> cookieValues = JsonUtils.readObjectFromJsonString(cookie.getValue(), HashMap.class);
                            if(cookieValues.containsKey(InternalSSOManager.COOKIE_KEY_ORCID) && !PojoUtil.isEmpty(cookieValues.get(InternalSSOManager.COOKIE_KEY_ORCID))) {
                                internalSSOManager.deleteToken(cookieValues.get(InternalSSOManager.COOKIE_KEY_ORCID), request, response);
                            } else {
                                //If it is not valid, just remove the cookie
                                cookie.setValue(StringUtils.EMPTY);
                                cookie.setMaxAge(0);
                                response.addCookie(cookie);
                            }
                        } catch(RuntimeException re) {
                            //If for some reason failed to read the token value, remove the cookie                          
                            cookie.setValue(StringUtils.EMPTY);
                            cookie.setMaxAge(0);
                            response.addCookie(cookie);
                        }
                        break;
                    }                    
                }
            }
            
            if(!PojoUtil.isEmpty(orcid)) {
                //Delete token if exists
                internalSSOManager.deleteToken(orcid);
            }
            
            UserStatus us = new UserStatus();
            us.setLoggedIn(false);
            return us;
        } else {
            UserStatus us = new UserStatus();
            us.setLoggedIn((orcid != null));
            
            Cookie [] cookies = request.getCookies();
            //Update cookie 
            if(cookies != null) {
                for(Cookie cookie : cookies) {
                    if(InternalSSOManager.COOKIE_NAME.equals(cookie.getName())) {
                        //If there are no user, just delete the cookie and token
                        if(PojoUtil.isEmpty(orcid)) {
                            cookie.setMaxAge(0);
                            cookie.setValue(StringUtils.EMPTY);
                            response.addCookie(cookie);
                        } else if(internalSSOManager.verifyToken(orcid, cookie.getValue())) {
                            internalSSOManager.updateCookie(orcid, request, response);
                        } else {
                            //Logout user
                            us.setLoggedIn(false);
                            request.getSession().invalidate();
                            SecurityContextHolder.clearContext();
                        }
                        break;
                    }                    
                }
            }
            return us;
        }                                            
    }

}
