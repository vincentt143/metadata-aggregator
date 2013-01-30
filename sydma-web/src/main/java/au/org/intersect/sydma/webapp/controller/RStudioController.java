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

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.dto.PasswordDto;
import au.org.intersect.sydma.webapp.service.CannotChangeRstudioPasswordException;
import au.org.intersect.sydma.webapp.service.CannotCreateRstudioUserException;
import au.org.intersect.sydma.webapp.service.RstudioSshService;
import au.org.intersect.sydma.webapp.util.Breadcrumb;
import au.org.intersect.sydma.webapp.validator.RstudioPasswordValidator;

/**
 * R Studio Controller.
 */
@RequestMapping("/rstudio/**")
@Controller
@DC2D
public class RStudioController
{
    private static final String REDIRECT_TO = "redirect:";

    private static final String CURRENT_USER_MODEL_NAME = "currentUser";

    private static final String REDIRECT_TO_RSTUDIO_CREATE_ACCOUNT = "redirect:/rstudio/createAccount";

    private static final String RSTUDIO_CREATE_ACCOUNT_VIEW = "rstudio/createAccount";

    private static final Logger LOG = LoggerFactory.getLogger(RStudioController.class);

    private static final String PASSWORD_DTO = "passwordDto";

    private static final String SUBMIT_URL = "submitUrl";

    private static List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();

    @Autowired
    private RstudioSshService rstudioSshService;

    @Value("#{rstudio[rstudio_url]}")
    private String rstudioUrl;

    @Autowired
    private RstudioPasswordValidator passwordValidator;
    
    static
    {
        breadcrumbs.add(Breadcrumb.getHome());
        breadcrumbs.add(new Breadcrumb("sections.rstudio.title"));
    }


    @RequestMapping(value = "/index/", method = RequestMethod.GET)
    public String indexPage(Model model, Principal principal)
    {
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        model.addAttribute(CURRENT_USER_MODEL_NAME, user);
        model.addAttribute(Breadcrumb.BREADCRUMBS, breadcrumbs);
        return "rstudio/index";
    }

    @RequestMapping(value = "/go", method = RequestMethod.GET)
    public String goToRStudio(Principal principal)
    {
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        if (user.getHasRstudioAccount())
        {
            return REDIRECT_TO + rstudioUrl;
        }
        else
        {
            return REDIRECT_TO_RSTUDIO_CREATE_ACCOUNT;
        }
    }

    @RequestMapping(value = "/createAccount", method = RequestMethod.GET)
    public String getCreateAccount(Model model, Principal principal)
    {
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        model.addAttribute(CURRENT_USER_MODEL_NAME, user);
        model.addAttribute(PASSWORD_DTO, new PasswordDto());
        model.addAttribute(SUBMIT_URL, "/rstudio/createAccount");
        model.addAttribute(Breadcrumb.BREADCRUMBS, breadcrumbs);
        return RSTUDIO_CREATE_ACCOUNT_VIEW;
    }

    @RequestMapping(value = "/changePassword", method = RequestMethod.GET)
    public String changePassword(Model model, Principal principal)
    {
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        model.addAttribute(CURRENT_USER_MODEL_NAME, user);
        model.addAttribute(PASSWORD_DTO, new PasswordDto());
        model.addAttribute(SUBMIT_URL, "/rstudio/changePassword");
        model.addAttribute(Breadcrumb.BREADCRUMBS, breadcrumbs);
        return RSTUDIO_CREATE_ACCOUNT_VIEW;
    }
    
    @RequestMapping(value = "/createAccount", method = RequestMethod.POST)
    public String postCreateAccount(Model model, Principal principal,
            @Valid @ModelAttribute(PASSWORD_DTO) PasswordDto passwordDto, BindingResult result)
    {
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();

        if (!passwordDto.getPassword().equals(passwordDto.getPasswordCheck()))
        {
            result.addError(passwordError());
        }

        if (!passwordValidator.isValidPassword(passwordDto.getPassword()))
        {
            result.addError(invalidPassword());
        }

        if (result.hasErrors())
        {
            model.addAttribute(CURRENT_USER_MODEL_NAME, user);
            return RSTUDIO_CREATE_ACCOUNT_VIEW;
        }

        try
        {
            rstudioSshService.addRstudioUser(user, passwordDto.getPassword());
            // user.setHasRstudioAccount(true); -- not used, removed this to make sure it compiles
            user.merge();
        }
        catch (CannotCreateRstudioUserException e)
        {
            result.addError(cannotCreateUserError());
            model.addAttribute(CURRENT_USER_MODEL_NAME, user);
            return RSTUDIO_CREATE_ACCOUNT_VIEW;
        }
        return REDIRECT_TO + rstudioUrl;
    }

    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    public String postChangePassword(Model model, Principal principal,
            @Valid @ModelAttribute(PASSWORD_DTO) PasswordDto passwordDto, BindingResult result)
    {
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();

        if (!passwordDto.getPassword().equals(passwordDto.getPasswordCheck()))
        {
            result.addError(passwordError());
        }

        if (!passwordValidator.isValidPassword(passwordDto.getPassword()))
        {
            result.addError(invalidPassword());
        }

        if (result.hasErrors())
        {
            model.addAttribute(CURRENT_USER_MODEL_NAME, user);
            return RSTUDIO_CREATE_ACCOUNT_VIEW;
        }

        try
        {
            rstudioSshService.changeRstudioUserPassword(user, passwordDto.getPassword());
        }
        catch (CannotChangeRstudioPasswordException e)
        {
            result.addError(cannotChangeRstudioPasswordError());
            model.addAttribute(CURRENT_USER_MODEL_NAME, user);
            return RSTUDIO_CREATE_ACCOUNT_VIEW;
        }
        return REDIRECT_TO + rstudioUrl;
    }

    private ObjectError cannotCreateUserError()
    {
        String[] nameErrorCode = {""};
        String[] nameErrorArg = {""};
        return new ObjectError(PASSWORD_DTO, nameErrorCode, nameErrorArg,
                "Failed to create R Studio account. Please contact ICT support");
    }

    private ObjectError cannotChangeRstudioPasswordError()
    {
        String[] nameErrorCode = {""};
        String[] nameErrorArg = {""};
        return new ObjectError(PASSWORD_DTO, nameErrorCode, nameErrorArg,
                "Failed to change R Studio password. Please contact ICT support");
    }

    private FieldError passwordError()
    {
        String[] nameErrorCode = {""};
        String[] nameErrorArg = {""};
        FieldError passwordError = new FieldError(PASSWORD_DTO, "password", "", true, nameErrorCode, nameErrorArg,
                "Passwords did not match");
        return passwordError;
    }

    private FieldError invalidPassword()
    {
        String[] nameErrorCode = {""};
        String[] nameErrorArg = {""};
        FieldError passwordError = new FieldError(PASSWORD_DTO, "password", "", true, nameErrorCode, nameErrorArg,
                "Not a valid password. Valid characters are at least six characters long, " + ""
                        + "and composed of alphanumerics and @ # $ % ^ & + =");
        return passwordError;
    }
}
