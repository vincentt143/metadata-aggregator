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
package au.org.intersect.sydma.webapp.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.domain.UserType;
import au.org.intersect.sydma.webapp.util.Breadcrumb;
import au.org.intersect.sydma.webapp.util.UrlHelper;
import au.org.intersect.sydma.webapp.wasm.WASMAuth;
import au.org.intersect.sydma.webapp.wasm.WASMService;

/**
 * Controller to handle login page and authentication through UniKey
 * 
 * @version $Rev: 29 $
 */
@Controller
public class LoginController
{
    private static final String REDIRECT_TO_ROOT = "redirect:/";

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    private static final String BREADCRUMBS = "breadcrumbs";

    private static List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> breadcrumbsForLogin = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> breadcrumbsForTermsAndConditions = new ArrayList<Breadcrumb>();

    @Autowired
    private WASMService wasmService;

    @Value("#{wasm[wasm_protocol]}")
    private String wasmProtocol;

    @Value("#{wasm[wasm_url]}")
    private String wasmUrl;

    @Value("#{wasm[app_realm]}")
    private String appRealm;

    @Value("#{wasm[app_id]}")
    private String appId;
    @Autowired
	private UrlHelper urlHelper;

    static
    {
        breadcrumbs.add(Breadcrumb.getHome());
        breadcrumbs.add(new Breadcrumb("sections.login.title"));

        breadcrumbsForLogin.add(Breadcrumb.getHome());
        breadcrumbsForLogin.add(new Breadcrumb("sections.external.user"));

        breadcrumbsForTermsAndConditions.add(Breadcrumb.getHome());
        breadcrumbsForTermsAndConditions.add(new Breadcrumb("sections.login.terms.title", "/termsAndConditions"));
    }

    private String wasmUrlString(boolean toLogin, String destUrl)
    {
        String what = toLogin ? "login.cgi" : "logout.cgi";
        String wasm = wasmProtocol + "://" + wasmUrl + "/" + what + "?";
        if (toLogin) 
        {
            wasm = wasm + "appID=" + appId + "&appRealm=" + appRealm + "&";
        }
        wasm = wasm + (toLogin ? "destUrl=" : "destURL=") + destUrl;
        return wasm;
    }

    @RequestMapping("/signin/**")
    public String index(ModelMap modelMap, HttpServletRequest request)
    {
        modelMap.addAttribute("wasmUrl", wasmUrlString(true, urlHelper.getCurrentBaseUrl(request)));
        modelMap.addAttribute(Breadcrumb.BREADCRUMBS, breadcrumbs);
        return "login/index";
    }

    @RequestMapping(value = "/", params = "wasmIkey")
    public String index(@RequestParam(value = "wasmIkey") String wasmIkey, HttpServletResponse response)
    {

        WASMAuth auth = wasmService.getAuth(wasmIkey, null, WASMService.IKEY_MODE);
        if (auth.isSuccessful() && auth.getSKey() != null)
        {
            String sKey = auth.getSKey();
            response.addCookie(new Cookie(wasmService.getSKeyCookieName(), sKey));
        }

        return REDIRECT_TO_ROOT;
    }

    @RequestMapping(value = "/")
    public String redirect(Principal principal)
    {
        if (principal == null)
        {
            return "redirect:/signin/index";
        }

        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();

        if (user == null)
        {
            return "redirect:/signin/index";
        }

        if (!user.hasAcceptedTermsAndConditions())
        {
            return "redirect:/termsAndConditions";
        }

        return "redirect:/home/index";
    }

    @RequestMapping(value = "/login")
    public void externalLogin(Model model)
    {
        model.addAttribute(BREADCRUMBS, breadcrumbsForLogin);
    }

    @RequestMapping(value = "/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response)
    {
        request.getSession().invalidate();
        Cookie cookie = new Cookie(wasmService.getSKeyCookieName(), "");
        response.addCookie(cookie);
        return "redirect:" + wasmUrlString(false,"http://www.sydney.edu.au/");
    }

    @RequestMapping(value = "/termsAndConditions", method = RequestMethod.GET)
    public void viewTermsAndConditions(Model model)
    {
        model.addAttribute(BREADCRUMBS, breadcrumbsForTermsAndConditions);
    }

    @RequestMapping(value = "/acceptTerms", method = RequestMethod.PUT)
    public String acceptTermsAndConditions(Principal principal, Model model, HttpServletRequest request)
    {
        model.addAttribute(BREADCRUMBS, breadcrumbsForTermsAndConditions);
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        user.acceptTermsAndConditions();

        if (user.getUserType() == UserType.UNIKEY)
        {
            HttpSession session = request.getSession();
            if (session != null)
            {
                // SKey will automatically refresh the session
                session.invalidate();
            }
        }
        return REDIRECT_TO_ROOT;
    }

    @RequestMapping(value = "/rejectTerms")
    public String rejectTermsAndConditions(Model model, HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        if (session != null)
        {
            session.invalidate();
        }
        model.addAttribute("reject", true);
        return "redirect:/signin/index";
    }
}
