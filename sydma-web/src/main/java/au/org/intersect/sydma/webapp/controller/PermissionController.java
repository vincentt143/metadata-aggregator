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

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import au.org.intersect.sydma.webapp.domain.AccessLevel;
import au.org.intersect.sydma.webapp.domain.PermissionEntry;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.domain.UserType;
import au.org.intersect.sydma.webapp.permission.PermissionType;
import au.org.intersect.sydma.webapp.permission.path.Path;
import au.org.intersect.sydma.webapp.permission.path.PathBuilder;
import au.org.intersect.sydma.webapp.service.PermissionService;
import au.org.intersect.sydma.webapp.util.Breadcrumb;

/**
 * Permissions Controller.
 */
@Controller
@RequestMapping("/permission/**")
public class PermissionController
{
    private static final String PERMISSION_VIEW_PAGE = "permission/view";
    private static final Logger LOG = LoggerFactory.getLogger(PermissionController.class);
    private static final String PERMISSION_INDEX = "permission/index";

    private static List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> viewPermissionBreadcrumbs = new ArrayList<Breadcrumb>();

    @Autowired
    private PermissionService permissionService;

    static
    {
        breadcrumbs.add(Breadcrumb.getHome());
        breadcrumbs.add(new Breadcrumb("Grant Permissions"));

        viewPermissionBreadcrumbs.add(Breadcrumb.getHome());
        viewPermissionBreadcrumbs.add(new Breadcrumb("View Permissions"));
    }

    @RequestMapping(value = "/add", params = "type=group", method = RequestMethod.GET)
    public String addPermissionsToGroup(Model model, @RequestParam("id") String id)
    {
        ResearchGroup group = ResearchGroup.findResearchGroup(Long.decode(id));
        model.addAttribute(Breadcrumb.BREADCRUMBS, breadcrumbs);
        model.addAttribute("name", group.getName());
        model.addAttribute("objectType", "Research Group");
        return PERMISSION_INDEX;
    }

    @RequestMapping(value = "/getuser", params = "type=UNIKEY", method = RequestMethod.GET)
    @ResponseBody
    public String findUnikeyResearcher(Model model, @RequestParam("userid") String username)
    {
        UserType userType = UserType.UNIKEY;
        return searchDatabase(userType, username);

    }

    @RequestMapping(value = "/getuser", params = "type=INTERNAL", method = RequestMethod.GET)
    @ResponseBody
    public String findExternalResearcher(Model model, @RequestParam("userid") String username)
    {

        UserType userType = UserType.INTERNAL;
        return searchDatabase(userType, username);

    }

    @RequestMapping(value = "/assigningPermissions", method = RequestMethod.POST)
    public String assigningPermissions(Model model, @RequestParam("usernames") final String usernames,
            @RequestParam("accessLevel") final String accessLevel, @RequestParam("path") String groupName,
            Principal principal)
    {
        TypedQuery<ResearchGroup> researchGroup = ResearchGroup.findResearchGroupsByNameEquals(groupName);
        return permissionService.canGroup(PermissionType.ASSIGN_PERMISSION, researchGroup.getSingleResult().getId(),
                principal, new PermissionService.ResearchGroupAction()
                {
                    @Override
                    public String act(ResearchGroup researchGroup, User user)
                    {
                        // TODO Auto-generated method stub
                        String[] usernamesArray = usernames.split(",");
                        for (String usernameToBeGrantedPermissions : usernamesArray)
                        {
                            TypedQuery<User> usersToBeAssignedPermissions = User
                                    .findUsersByUsernameEquals(usernameToBeGrantedPermissions);

                            permissionService.addPermission(usersToBeAssignedPermissions.getSingleResult(),
                                    PathBuilder.groupPath(researchGroup), AccessLevel.valueOf(accessLevel));
                        }

                        LOG.info("assignment finished");
                        return "redirect:/" + PERMISSION_VIEW_PAGE + "/" + researchGroup.getId();
                    }
                });

    }

    public String searchDatabase(UserType userType, String username)
    {
        TypedQuery<User> usersFound = User.findUsersByUserTypeAndUsernameEquals(userType, username);

        if (usersFound.getResultList().size() != 1)
        {
            LOG.debug("User does not exist or has never logged onto this system");
            return "{ \"error\" : \"User does not exist or has never logged onto this system\"}";
        }

        return usersFound.getSingleResult().toJson();
    }

    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
    public String viewPermissions(@PathVariable("id") final Long id, final Model model,
            java.security.Principal principal)
    {
        return permissionService.canGroup(PermissionType.VIEW_PERMISSION, id, principal,
                new PermissionService.ResearchGroupAction()
                {
                    @Override
                    public String act(ResearchGroup group, User user)
                    {
                        Path groupPath = PathBuilder.groupPath(group);
                        List<PermissionEntry> pathList = PermissionEntry.findPermissionEntrysByPathLike(
                                groupPath.getPath()).getResultList();
                        model.addAttribute("permissionList", pathList);
                        model.addAttribute(Breadcrumb.BREADCRUMBS, viewPermissionBreadcrumbs);
                        return PERMISSION_VIEW_PAGE;
                    }
                });
    }

}
