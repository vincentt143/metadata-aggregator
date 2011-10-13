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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import au.org.intersect.sydma.webapp.domain.Role;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.domain.UserType;
import au.org.intersect.sydma.webapp.service.ExternalUserService;
import au.org.intersect.sydma.webapp.util.Breadcrumb;
import au.org.intersect.sydma.webapp.util.UrlHelper;

/**
 * User Management Controller.
 */
@RequestMapping("/usermanagement/**")
@Controller
public class UserManagementController
{
    private static final String USER_MANAGEMENT_REQUEST_MODEL = "user";
    private static final String NEW_EXTERNAL_USER_PAGE = "usermanagement/new";
    private static final String EDIT_EXTERNAL_USER_PAGE = "usermanagement/edit";
    private static final String EDIT_ROLES_PAGE = "usermanagement/editroles";
    private static final String ACCESS_DENIED_PAGE = "accessDenied";
    
    private static final String REDIRECT_TO_LIST_PAGE = "redirect:/usermanagement/list";
    private static final String REDIRECT_TO_ASSIGN_ROLE_PAGE = "redirect:/usermanagement/assignroles";

    private static List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> existingUsersBreadcrumbs = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> unikeyUsersBreadcrumbs = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> createUserBreadcrumbs = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> editUserBreadcrumbs = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> assignRoleBreadcrumbs = new ArrayList<Breadcrumb>();

    private static Breadcrumb index = new Breadcrumb("User Management", "/usermanagement/index");

    static
    {
        breadcrumbs.add(Breadcrumb.getHome());
        breadcrumbs.add(new Breadcrumb("User Management"));

        createUserBreadcrumbs.add(Breadcrumb.getHome());
        createUserBreadcrumbs.add(index);
        createUserBreadcrumbs.add(new Breadcrumb("Create New External User"));

        editUserBreadcrumbs.add(Breadcrumb.getHome());
        editUserBreadcrumbs.add(index);
        editUserBreadcrumbs.add(new Breadcrumb("Edit External User"));

        existingUsersBreadcrumbs.add(Breadcrumb.getHome());
        existingUsersBreadcrumbs.add(index);
        existingUsersBreadcrumbs.add(new Breadcrumb("External User List"));
        
        unikeyUsersBreadcrumbs.add(Breadcrumb.getHome());
        unikeyUsersBreadcrumbs.add(index);
        unikeyUsersBreadcrumbs.add(new Breadcrumb("Unikey User List"));
        
        assignRoleBreadcrumbs.add(Breadcrumb.getHome());
        assignRoleBreadcrumbs.add(index);
        assignRoleBreadcrumbs.add(new Breadcrumb("Unikey User List", "/usermanagement/assignroles"));
        assignRoleBreadcrumbs.add(new Breadcrumb("Assign Role"));
    }

    @Autowired
    private ExternalUserService externalUserService;

    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String newExternalUser(Model model)
    {
        model.addAttribute(Breadcrumb.BREADCRUMBS, createUserBreadcrumbs);
        model.addAttribute(USER_MANAGEMENT_REQUEST_MODEL, new User());
        return NEW_EXTERNAL_USER_PAGE;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView getExistingUsers(Model model)
    {
        model.addAttribute(Breadcrumb.BREADCRUMBS, existingUsersBreadcrumbs);
        model.addAttribute("externalUserList", User.findUsersByUserType(UserType.INTERNAL).getResultList());
        List<User> userList = User.findUsersByUserType(UserType.INTERNAL).getResultList();
        return new ModelAndView("usermanagement/list", "existingUserList", userList);
    }

    @RequestMapping(value = "/assignroles", method = RequestMethod.GET)
    public ModelAndView getUnikeyUsers(Model model, Principal principal)
    {
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();

        if (!user.hasAssignRolePermission())
        {
            return new ModelAndView(ACCESS_DENIED_PAGE);
        }
        
        model.addAttribute(Breadcrumb.BREADCRUMBS, unikeyUsersBreadcrumbs);
        model.addAttribute("unikeyList", User.findUsersByUserType(UserType.UNIKEY).getResultList());
        List<User> userList = User.findUsersByUserType(UserType.UNIKEY).getResultList();
        return new ModelAndView("usermanagement/assignroles", "unikeyList", userList);
    }
    
    @RequestMapping(value = "/createExternalUser", method = RequestMethod.POST)
    public String createExternalUser(@Valid User user, BindingResult result, Model model, HttpServletRequest request)
    {    
        if (user.isDuplicate())
        {
            duplicateError(result, user.getEmail());
        }
        
        if (result.hasFieldErrors())
        {
            model.addAttribute(Breadcrumb.BREADCRUMBS, createUserBreadcrumbs);
            model.addAttribute(USER_MANAGEMENT_REQUEST_MODEL, user);
            return NEW_EXTERNAL_USER_PAGE;
        }

        externalUserService.createExternalUser(user, UrlHelper.getCurrentBaseUrl(request));
        user.persist();
        return REDIRECT_TO_LIST_PAGE;
    }

    @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
    public String editExternalUser(@PathVariable("id") Long id, Model model)
    {
        List<User> users = User.findUsersByUserTypeAndIdEquals(UserType.INTERNAL, id).getResultList();
        if (users.size() != 1)
        {
            return ACCESS_DENIED_PAGE;
        }
        
        User user = User.findUser(id);
        model.addAttribute(Breadcrumb.BREADCRUMBS, editUserBreadcrumbs);
        model.addAttribute(USER_MANAGEMENT_REQUEST_MODEL, user);
        return EDIT_EXTERNAL_USER_PAGE;
    }

    @RequestMapping(value = "/editroles/{id}", method = RequestMethod.GET)
    public String editRoles(@PathVariable("id") Long id, Model model)
    {
        List<User> users = User.findUsersByUserTypeAndIdEquals(UserType.UNIKEY, id).getResultList();
        if (users.size() != 1)
        {
            return ACCESS_DENIED_PAGE;
        }
        
        model.addAttribute(Breadcrumb.BREADCRUMBS, assignRoleBreadcrumbs);
        model.addAttribute(USER_MANAGEMENT_REQUEST_MODEL, users.get(0));      
        model.addAttribute("roles", Role.getAssignableRoles());
        return EDIT_ROLES_PAGE;
    }
    
    @RequestMapping(value = "/editExternalUser", method = RequestMethod.PUT)
    public String editExternalUser(@Valid User user, BindingResult result, Model model)
    {
        List<User> users = User.findUsersByUserTypeAndIdEquals(UserType.UNIKEY, user.getId()).getResultList();
        if (users.size() != 1)
        {
            return ACCESS_DENIED_PAGE;
        }
        
        if (user.isDuplicate())
        {
            duplicateError(result, user.getEmail());
        }
        
        if (result.hasErrors())
        {
            model.addAttribute(Breadcrumb.BREADCRUMBS, editUserBreadcrumbs);
            model.addAttribute(USER_MANAGEMENT_REQUEST_MODEL, user);
            return EDIT_EXTERNAL_USER_PAGE;
        }

        User existingUser = User.findUser(user.getId());
        existingUser.modify(user);
        return REDIRECT_TO_LIST_PAGE;
    }
    
    @RequestMapping(value = "/editRole", method = RequestMethod.PUT)
    public String editRoles(@Valid User user, BindingResult result, Model model)
    {
        List<User> users = User.findUsersByUserTypeAndIdEquals(UserType.UNIKEY, user.getId()).getResultList();
        if (users.size() != 1)
        {
            return ACCESS_DENIED_PAGE;
        }
        
        if (result.hasFieldErrors())
        {
            model.addAttribute(Breadcrumb.BREADCRUMBS, assignRoleBreadcrumbs);
            model.addAttribute(USER_MANAGEMENT_REQUEST_MODEL, user);
            model.addAttribute("role", Role.getAssignableRoles());
            return EDIT_ROLES_PAGE;
        }
        
        User existingUser = User.findUser(user.getId());
        if (user.getRoles() == null)
        {
            existingUser.assignNoRole();
            return REDIRECT_TO_ASSIGN_ROLE_PAGE;
        }

        existingUser.assignRole(user.getRoles());    

        return REDIRECT_TO_ASSIGN_ROLE_PAGE;
    }
    

    @RequestMapping(value = "/index")
    public void index(Model model)
    {
        model.addAttribute(Breadcrumb.BREADCRUMBS, breadcrumbs);
    }
    
    private void duplicateError(BindingResult result, String email)
    {
        String[] emailErrorCode = {""};
        String[] emailErrorArg = {""};
        FieldError emailError = new FieldError(USER_MANAGEMENT_REQUEST_MODEL, "email", email, true,
                emailErrorCode, emailErrorArg, "Email already exists in the system");
        result.addError(emailError);    
    }
}