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
import java.util.Collections;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import au.org.intersect.sydma.webapp.domain.Role;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.domain.UserType;
import au.org.intersect.sydma.webapp.json.JsonResponse;
import au.org.intersect.sydma.webapp.service.PermissionService;
import au.org.intersect.sydma.webapp.util.Breadcrumb;

/**
 * User Management Controller.
 */
@RequestMapping("/usermanagement/**")
@Controller
public class UserManagementController
{
    private static final String USER_MANAGEMENT_REQUEST_MODEL = "user";

    private static final String EDIT_ROLES_PAGE = "usermanagement/edit";
    private static final String ACCESS_DENIED_PAGE = "accessDenied";

    private static final String REDIRECT_TO_ASSIGN_ROLE_PAGE = "redirect:/usermanagement/list";

    private static List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> assignRoleBreadcrumbs = new ArrayList<Breadcrumb>();

    private static Breadcrumb index = new Breadcrumb("sections.users.title", "/usermanagement/list");

    static
    {
        breadcrumbs.add(Breadcrumb.getHome());
        breadcrumbs.add(new Breadcrumb("sections.users.title"));

        assignRoleBreadcrumbs.add(Breadcrumb.getHome());
        assignRoleBreadcrumbs.add(index);
        assignRoleBreadcrumbs.add(new Breadcrumb("sections.users.unikey.assignrole.title"));
    }

    @Autowired
    private PermissionService permissionService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String getUnikeyUsers(Model model, Principal principal)
    {
        if (!getUserFromPrincipal(principal).hasAssignRolePermission())
        {
            return ACCESS_DENIED_PAGE;
        }
        // TODO: Sort the list before passing to stop the randomization on the front-end
        List<User> unikeyUsers = User.findUsersByUserType(UserType.UNIKEY).getResultList();
        model.addAttribute(Breadcrumb.BREADCRUMBS, breadcrumbs);
        model.addAttribute("unikeyList", unikeyUsers);

        return "usermanagement/list";
    }

    @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
    public String editRoles(@PathVariable("id") Long id, Model model, Principal principal)
    {
        if (!getUserFromPrincipal(principal).hasAssignRolePermission())
        {
            return ACCESS_DENIED_PAGE;
        }

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

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public String editRoles(@Valid User user, BindingResult result, Model model, Principal principal)
    {
        if (!getUserFromPrincipal(principal).hasAssignRolePermission())
        {
            return ACCESS_DENIED_PAGE;
        }

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
        }
        else
        {
            existingUser.assignRole(user.getRoles());
        }
        return REDIRECT_TO_ASSIGN_ROLE_PAGE;
    }

    @RequestMapping("/searchAll")
    @ResponseBody
    public String indexAll(@RequestParam(value = "q") String search, Model model)
    {
        StringBuffer searchResult = new StringBuffer("[");

        List<User> matchResult = User.findUsersByUsernameLikeOrGivennameLikeOrSurnameLike(search, search, search)
                .getResultList();
        boolean isFirst = true;
        for (User user : matchResult)
        {
            if (isFirst)
            {
                isFirst = false;
            }
            else
            {
                searchResult.append(",");
            }
            searchResult.append("{\"id\":");
            searchResult.append(user.getId());
            searchResult.append(",\"name\":\"");
            searchResult.append(user.getUsername());
            searchResult.append("\",\"fullname\":\"");
            searchResult.append(user.getFullname());
            searchResult.append("\"}");
        }
        searchResult.append("]");
        return searchResult.toString();
    }

    @RequestMapping("/searchUnikey")
    @ResponseBody
    public String indexUnikey(@RequestParam(value = "term") String search, Model model)
    {
        List<UserDTO> searchResult = new ArrayList<UserDTO>();

        List<User> matchResult = User.findUsersByUsernameLikeOrGivennameLikeOrSurnameLike(search, search, search)
                .getResultList();
        for (User user : matchResult)
        {
            if (user.getUserType().equals(UserType.UNIKEY))
            {
                UserDTO userDTO = new UserDTO();
                userDTO.setFullName(user.getFullname());
                userDTO.setUserID(user.getUsername());
                searchResult.add(userDTO);
            }
        }
        Collections.sort(searchResult);
        return new JsonResponse(searchResult, null).toJson();
    }

    @RequestMapping(value = "/index")
    public String index(Model model, Principal principal)
    {
        if (!permissionService.canCreateExternalUser(getUserFromPrincipal(principal)))
        {
            return ACCESS_DENIED_PAGE;
        }

        model.addAttribute(Breadcrumb.BREADCRUMBS, breadcrumbs);
        return "index";
    }

    private User getUserFromPrincipal(Principal principal)
    {
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        return user;
    }

    /**
     * DTO for tokeninput.js
     * 
     * @version $Rev: 29 $
     */
    public class UserJson
    {
        private String id;
        private String name;

        public String getId()
        {
            return id;
        }

        public void setId(String id)
        {
            this.id = id;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }
    }

    /**
     * DTO to handle auto completion of fields
     * 
     * @version $Rev: 29 $
     */
    public class UserDTO implements Comparable<UserDTO>
    {
        private String fullName;

        private String userID;

        public String getFullName()
        {
            return fullName;
        }

        public void setFullName(String fullName)
        {
            this.fullName = fullName;
        }

        public String getUserID()
        {
            return userID;
        }

        public void setUserID(String userID)
        {
            this.userID = userID;
        }

        public int compareTo(UserDTO user)
        {
            int lastCmp = userID.compareTo(user.userID);
            return lastCmp;
        }
    }

}