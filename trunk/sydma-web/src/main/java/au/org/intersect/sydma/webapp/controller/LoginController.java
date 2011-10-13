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
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import au.org.intersect.sydma.webapp.domain.Role;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.domain.UserType;
import au.org.intersect.sydma.webapp.service.ExternalUserService;
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
    private static final String USER_MODEL = "user";

    private static final String CHANGE_PASSWORD_PAGE = "changepassword";

    private static final String REDIRECT_TO_LOGIN = "redirect:/login";

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    private static final String BREADCRUMBS = "breadcrumbs";

    private static List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> breadcrumbsForChangePassword = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> breadcrumbsForForgotPassword = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> breadcrumbsForLogin = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> breadcrumbsForTermsAndConditions = new ArrayList<Breadcrumb>();

    private static Breadcrumb login = new Breadcrumb("Log in", "/login");

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

    static
    {
        breadcrumbs.add(Breadcrumb.getHome());
        breadcrumbs.add(new Breadcrumb("Log in"));

        breadcrumbsForLogin.add(Breadcrumb.getHome());
        breadcrumbsForLogin.add(login);

        breadcrumbsForChangePassword.add(Breadcrumb.getHome());
        breadcrumbsForChangePassword.add(new Breadcrumb("Change Password", "/changepassword"));

        breadcrumbsForForgotPassword.add(Breadcrumb.getHome());
        breadcrumbsForForgotPassword.add(new Breadcrumb("Forgot Password", "/forgotpassword"));

        breadcrumbsForTermsAndConditions.add(Breadcrumb.getHome());
        breadcrumbsForTermsAndConditions.add(new Breadcrumb("Terms and Conditions", "/termsAndConditions"));
    }

    @Autowired
    private ExternalUserService externalUserService;

    @RequestMapping("/signin/**")
    public String index(ModelMap modelMap, HttpServletRequest request)
    {

        StringBuffer url = new StringBuffer("");
        String wasm = wasmProtocol + "://" + wasmUrl + "/login.cgi?appID=" + appId;
        String realm = "&appRealm=" + appRealm;
        String local = "&destURL=" + UrlHelper.getCurrentBaseUrl(request);
        url.append(wasm + realm + local);

        modelMap.addAttribute("wasmUrl", url);
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

        return "redirect:/";
    }

    @RequestMapping(value = "/")
    public String redirect(Principal principal)
    {
        if (principal == null)
        {
            return "redirect:/signin/index";
        }

        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();

        if (!user.hasAcceptedTermsAndConditions())
        {
            return "redirect:/termsAndConditions";
        }

        if (user.getUserType() == UserType.INTERNAL && !user.isActive())
        {
            return "redirect:/changepassword";
        }

        Role researcherRole = Role.findRolesByNameEquals(Role.RoleNames.ROLE_RESEARCHER.toString()).getSingleResult();
        Role supportRole = Role.findRolesByNameEquals(Role.RoleNames.ROLE_ICT_SUPPORT.toString()).getSingleResult();
        boolean canViewHome = user.getRoles().contains(researcherRole) || user.getRoles().contains(supportRole);

        return canViewHome ? "redirect:/home/index" : "redirect:/admin/index";
    }

    @RequestMapping(value = "/login")
    public void externalLogin(Model model)
    {
        model.addAttribute(BREADCRUMBS, breadcrumbsForLogin);
    }

    @RequestMapping(value = "/changepassword", method = RequestMethod.GET)
    public void getPasswordScreen(Model model, Principal principal)
    {
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        model.addAttribute(BREADCRUMBS, breadcrumbsForChangePassword);
        model.addAttribute(USER_MODEL, user);
    }

    @RequestMapping(value = "/changepassword", method = RequestMethod.PUT)
    public String changePassword(@RequestParam(value = "passwordCheck") String password, @Valid User user,
            BindingResult result, Model model, Principal principal)
    {
        User existingUser = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();

        if (existingUser.isActive())
        {
            passwordExpire(result, password);
            model.addAttribute(BREADCRUMBS, breadcrumbsForChangePassword);
            return CHANGE_PASSWORD_PAGE;
        }
        if (!user.getPassword().equals(password))
        {
            passwordError(result, password);
        }

        if (result.hasFieldErrors())
        {
            model.addAttribute(BREADCRUMBS, breadcrumbsForChangePassword);
            model.addAttribute(USER_MODEL, user);
            return CHANGE_PASSWORD_PAGE;
        }
        existingUser.activateUser(password);
        return REDIRECT_TO_LOGIN;
    }

    @RequestMapping(value = "/forgotpassword", method = RequestMethod.GET)
    public void getForgotPasswordPage(User user, Model model)
    {
        model.addAttribute(USER_MODEL, user);
        model.addAttribute(BREADCRUMBS, breadcrumbsForForgotPassword);
    }

    @RequestMapping(value = "/sendpassword", method = RequestMethod.POST)
    public String sendPassword(@RequestParam(value = "email") String email, @Valid User user, BindingResult result,
            Model model, HttpServletRequest request)
    {
        if (!externalUserService.checkIfEmailExists(email))
        {
            emailError(result, email);
            model.addAttribute(BREADCRUMBS, breadcrumbsForForgotPassword);
            model.addAttribute(USER_MODEL, user);
            return "forgotpassword";
        }

        User existingUser = User.findUsersByEmailEqualsAndUserType(email, UserType.INTERNAL).getSingleResult();
        externalUserService.resetExternalUser(existingUser, UrlHelper.getCurrentBaseUrl(request));

        return REDIRECT_TO_LOGIN;
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
        return "redirect:/";
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

    private void emailError(BindingResult result, String email)
    {
        String[] nameErrorCode = {""};
        String[] nameErrorArg = {""};
        FieldError nameError = new FieldError(USER_MODEL, "email", email, true, nameErrorCode, nameErrorArg,
                "This email is not registered in the system");
        result.addError(nameError);
    }

    private void passwordError(BindingResult result, String password)
    {
        String[] nameErrorCode = {""};
        String[] nameErrorArg = {""};
        FieldError nameError = new FieldError(USER_MODEL, "password", password, true, nameErrorCode, nameErrorArg,
                "Password did not match, please check that your typing is correct");
        result.addError(nameError);
    }

    private void passwordExpire(BindingResult result, String password)
    {
        String[] nameErrorCode = {""};
        String[] nameErrorArg = {""};
        FieldError nameError = new FieldError(USER_MODEL, "password", password, true, nameErrorCode, nameErrorArg,
                "The one time password has expired.");
        result.addError(nameError);
    }

}
