/** Copyright (c) 2011, Intersect, Australia
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Intersect, Intersect's partners, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package au.org.intersect.sydma.webapp.filter;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.view.RedirectView;

import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.domain.UserType;
import au.org.intersect.sydma.webapp.service.ApplicationType;
import au.org.intersect.sydma.webapp.service.ApplicationTypeService;
import au.org.intersect.sydma.webapp.service.PermissionService;

/**
 * Filter to indicate if the current user can create external users
 * 
 * @version $Rev: 29 $
 */
public class ExternalUserPermissionFilter extends HandlerInterceptorAdapter
{
    private static final int HTTP_PORT = 80;

    @Autowired
    private PermissionService permissionService;
    
    @Autowired
    private ApplicationTypeService applicationTypeService;
    
    @Value("#{wasm[wasm_protocol]}")
    private String wasmProtocol;

    @Value("#{wasm[wasm_url]}")
    private String wasmUrl;
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView)
    {
        if (modelAndView == null)
        {
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails)
        {
            populateModelWithData((UserDetails) auth.getPrincipal(),
                    contextUrl(request), modelAndView);
        }
    }
    
    private String contextUrl(HttpServletRequest request)
    {
        try
        {
            URL fullUrl = new URL(request.getRequestURL().toString());
            String context = request.getContextPath();
            String portPart = fullUrl.getPort() == HTTP_PORT ? "" : ":" + fullUrl.getPort();
            return fullUrl.getProtocol() + "://" + fullUrl.getHost() + portPart + context;
        }
        catch (MalformedURLException e)
        {
            // shouldn't happen
            return "";
        }
    }

    private void populateModelWithData(UserDetails principal, String appUrl,
            ModelAndView modelAndView)
    {
        if (modelAndView.getView() instanceof RedirectView || modelAndView.getViewName().startsWith("redirect:"))
        {
            return;
        }

        User user = User.findUsersByUsernameEquals(principal.getUsername()).getSingleResult();

        if (user != null)
        {
            boolean permission = permissionService.canCreateExternalUser(user);
            modelAndView.getModel().put("canCreateExternalUser", permission);
            modelAndView.getModel().put("isAgrEnv", applicationTypeService.applicationIs(ApplicationType.AGR_ENV));
            modelAndView.getModel().put("allowExternal", applicationTypeService.getAllowExternal());
            modelAndView.getModel().put("logoutUrl", appUrl + "/logout");
        }
    }

}
