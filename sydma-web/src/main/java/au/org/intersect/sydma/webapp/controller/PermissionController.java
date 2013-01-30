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
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import au.org.intersect.sydma.webapp.domain.AccessLevel;
import au.org.intersect.sydma.webapp.domain.Activity;
import au.org.intersect.sydma.webapp.domain.PermissionEntry;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.domain.UserType;
import au.org.intersect.sydma.webapp.permission.PermissionType;
import au.org.intersect.sydma.webapp.permission.path.Path;
import au.org.intersect.sydma.webapp.permission.path.PathBuilder;
import au.org.intersect.sydma.webapp.service.ActivityLogService;
import au.org.intersect.sydma.webapp.service.PermissionService;
import au.org.intersect.sydma.webapp.util.Breadcrumb;
import au.org.intersect.sydma.webapp.util.ModelAndViewRedirectHelper;

/**
 * Permissions Controller.
 */
// TODO CHECKSTYLE-OFF: MultipleStringLiteralsCheck
@Controller
@RequestMapping("/permission/**")
public class PermissionController
{
    private static final String PERMISSION_VIEW_PAGE = "permission/view";
    private static final String PERMISSION_DELETE_PAGE = "permission/delete";
    private static final String PERMISSION_NEW_PAGE = "permission/new";
    private static final Logger LOG = LoggerFactory.getLogger(PermissionController.class);
    private static final String PERMISSION_INDEX = "permission/index";
    private static final String REDIRECT_TO_PERMISSION_VIEW_PAGE = "/permission/view/";
    private static final String SUBMIT_PARAM = "_submit";
    private static final String ACCESS_DENIED = "accessDenied";
    private static final String LEVEL_TYPE = "type";
    private static final String LEVEL_TYPE_NAME = "name";
    private static final String TYPE_FOR_URL = "typeForUrl";

    private static List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> viewPermissionBreadcrumbs = new ArrayList<Breadcrumb>();

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private ModelAndViewRedirectHelper modelAndViewRedirectHelper;

    @Autowired
    private ActivityLogService activityLogService;

    static
    {
        breadcrumbs.add(Breadcrumb.getHome());
        breadcrumbs.add(new Breadcrumb("sections.permissions.grant.title"));

        viewPermissionBreadcrumbs.add(Breadcrumb.getHome());
        viewPermissionBreadcrumbs.add(new Breadcrumb("sections.permissions.view.title"));
    }

    @RequestMapping(value = "/new", params = "type=group", method = RequestMethod.GET)
    public String newPermissionsToGroup(final Model model, @RequestParam("id") String id, Principal principal)
    {
        return permissionService.canGroup(PermissionType.ASSIGN_PERMISSION, Long.decode(id), principal,
                new PermissionService.ResearchGroupAction()
                {
                    @Override
                    public String act(ResearchGroup researchGroup, User user)
                    {
                        boolean hasFullAccess = permissionService.hasFullAccessPermissionForGroup(user, researchGroup);
                        populateGroupModel(model, hasFullAccess, researchGroup, false);
                        return PERMISSION_NEW_PAGE;
                    }
                });
    }

    private void populateGroupModel(final Model model, boolean hasFullAccess, ResearchGroup researchGroup,
            boolean hasError)
    {
        model.addAttribute("hasFullAccess", hasFullAccess);
        model.addAttribute(Breadcrumb.BREADCRUMBS, breadcrumbs);
        model.addAttribute("hasError", hasError);
        model.addAttribute("permError", hasError);
        model.addAttribute(LEVEL_TYPE, "research group");
        model.addAttribute(TYPE_FOR_URL, "group");
        model.addAttribute(LEVEL_TYPE_NAME, researchGroup.getName());
        model.addAttribute("object", researchGroup);
    }

    @RequestMapping(value = "/new", params = "type=project", method = RequestMethod.GET)
    public String newPermissionsToProject(final Model model, @RequestParam("id") String id, Principal principal)
    {
        return permissionService.canProject(PermissionType.ASSIGN_PERMISSION, Long.decode(id), principal,
                new PermissionService.ResearchProjectAction()
                {
                    @Override
                    public String act(ResearchProject researchProject, User user)
                    {
                        boolean hasFullAccess = permissionService.hasFullAccessPermissionForProject(user,
                                researchProject);
                        populateProjectModel(model, hasFullAccess, researchProject);
                        return PERMISSION_NEW_PAGE;
                    }
                });
    }

    private void populateProjectModel(final Model model, boolean hasFullAccess, ResearchProject researchProject)
    {
        model.addAttribute("hasFullAccess", hasFullAccess);
        model.addAttribute(Breadcrumb.BREADCRUMBS, breadcrumbs);
        model.addAttribute(LEVEL_TYPE, "research project");
        model.addAttribute(TYPE_FOR_URL, "project");
        model.addAttribute(LEVEL_TYPE_NAME, researchProject.getName());
        model.addAttribute("object", researchProject);
    }

    @RequestMapping(value = "/new", params = "type=dataset", method = RequestMethod.GET)
    public String newPermissionsToDataset(final Model model, @RequestParam("id") String id, Principal principal)
    {
        return permissionService.canDataset(PermissionType.ASSIGN_PERMISSION, Long.decode(id), principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset researchDataset, User user)
                    {
                        boolean hasFullAccess = permissionService.hasFullAccessPermissionForDataset(user,
                                researchDataset);
                        populateDatasetModel(model, hasFullAccess, researchDataset);
                        return PERMISSION_NEW_PAGE;
                    }
                });
    }

    private void populateDatasetModel(final Model model, boolean hasFullAccess, ResearchDataset researchDataset)
    {
        model.addAttribute("hasFullAccess", hasFullAccess);
        model.addAttribute(Breadcrumb.BREADCRUMBS, breadcrumbs);
        model.addAttribute(LEVEL_TYPE, "research dataset");
        model.addAttribute(TYPE_FOR_URL, "dataset");
        model.addAttribute(LEVEL_TYPE_NAME, researchDataset.getName());
        model.addAttribute("object", researchDataset);
    }

    @RequestMapping(value = "/new", params = "type=directory", method = RequestMethod.GET)
    public String newPermissionsToDirectoryOrFile(final Model model, @RequestParam("path") String virtualPath,
            Principal principal)
    {
        return permissionService.canDirectory(PermissionType.ASSIGN_DIRECTORY_PERMISSION,
                PathBuilder.buildFromString(virtualPath), principal, new PermissionService.ResearchDirectoryAction()
                {
                    @Override
                    public String act(Path path, User user)
                    {
                        // EA = FA at directory level
                        boolean hasEditingAccess = permissionService.hasEditingAccessPermissionForDirectory(user,
                                path.getPath());
                        populateDirectoryModel(model, hasEditingAccess, path);
                        return PERMISSION_NEW_PAGE;
                    }
                });

    }

    private void populateDirectoryModel(final Model model, boolean hasFullAccess, Path path)
    {
        model.addAttribute("hasFullAccess", hasFullAccess);
        model.addAttribute(Breadcrumb.BREADCRUMBS, breadcrumbs);
        model.addAttribute("path", path.getPath());
        model.addAttribute("isDirectoryPath", true);
        model.addAttribute(TYPE_FOR_URL, "directory");
        model.addAttribute(LEVEL_TYPE_NAME, path.getDisplayName());
        model.addAttribute(LEVEL_TYPE, "dataset directory");
    }

    @RequestMapping(value = "/assigningPermissions", params = "type=group", method = RequestMethod.POST)
    public ModelAndView assigningPermissionsToGroup(final Model model,
            @RequestParam("usernames") final String usernames, @RequestParam("accessLevel") final String accessLevel,
            @RequestParam("path") String groupId, final Principal principal, HttpServletRequest request)
    {
        ResearchGroup group = ResearchGroup.findResearchGroup(Long.decode(groupId));
        String view = permissionService.canGroup(PermissionType.ASSIGN_PERMISSION, group.getId(), principal,
                new PermissionService.ResearchGroupAction()
                {
                    @Override
                    public String act(ResearchGroup researchGroup, User user)
                    {
                        Path path = PathBuilder.groupPath(researchGroup);
                        boolean successfulAssignment = false;
                        boolean hasFullAccess = permissionService.hasFullAccessPermissionForGroup(user, researchGroup);

                        successfulAssignment = assignPermissionsToTheSelectedUsers(usernames, accessLevel, path,
                                hasFullAccess, principal);
                        if (successfulAssignment)
                        {
                            permissionService.cleanChildNodes(usernames, accessLevel, path);
                            return REDIRECT_TO_PERMISSION_VIEW_PAGE;
                        }
                        else
                        {
                            populateGroupModel(model, hasFullAccess, researchGroup, true);
                            return PERMISSION_NEW_PAGE;
                        }
                    }
                });
        if (view.equals(REDIRECT_TO_PERMISSION_VIEW_PAGE))
        {
            String redirect = REDIRECT_TO_PERMISSION_VIEW_PAGE + group.getId();
            return redirectToViewInstance(redirect, request);
        }
        else
        {
            return new ModelAndView(view);
        }
    }

    @RequestMapping(value = "/assigningPermissions", params = "type=project", method = RequestMethod.POST)
    public ModelAndView assigningPermissionsToProject(final Model model,
            @RequestParam("usernames") final String usernames, @RequestParam("accessLevel") final String accessLevel,
            @RequestParam("path") String projectId, final Principal principal, HttpServletRequest request)
    {
        ResearchProject project = ResearchProject.findResearchProject(Long.decode(projectId));
        String view = permissionService.canProject(PermissionType.ASSIGN_PROJECT_PERMISSION, project.getId(),
                principal, new PermissionService.ResearchProjectAction()
                {
                    @Override
                    public String act(ResearchProject researchProject, User user)
                    {
                        Path path = PathBuilder.projectPath(researchProject);
                        boolean successfulAssignment = false;
                        boolean hasFullAccess = permissionService.hasFullAccessPermissionForProject(user,
                                researchProject);
                        List<User> users = permissionService.parentNodeCheck(usernames, accessLevel, path);
                        if (!users.isEmpty())
                        {
                            model.addAttribute("hasError", true);
                            model.addAttribute("usersInconsistent", users);
                            populateProjectModel(model, hasFullAccess, researchProject);
                            return PERMISSION_NEW_PAGE;
                        }
                        successfulAssignment = assignPermissionsToTheSelectedUsers(usernames, accessLevel, path,
                                permissionService.hasFullAccessPermissionForProject(user, researchProject), principal);
                        if (successfulAssignment)
                        {
                            permissionService.cleanChildNodes(usernames, accessLevel, path);
                            return REDIRECT_TO_PERMISSION_VIEW_PAGE;
                        }
                        else
                        {
                            populateProjectModel(model, hasFullAccess, researchProject);
                            model.addAttribute("hasError", true);
                            model.addAttribute("permError", true);
                            return PERMISSION_NEW_PAGE;
                        }
                    }
                });
        if (view.equals(REDIRECT_TO_PERMISSION_VIEW_PAGE))
        {
            String redirect = REDIRECT_TO_PERMISSION_VIEW_PAGE + project.getResearchGroup().getId();
            return redirectToViewInstance(redirect, request);
        }
        else
        {
            return new ModelAndView(view);
        }

    }

    @RequestMapping(value = "/assigningPermissions", params = "type=dataset", method = RequestMethod.POST)
    public ModelAndView assigningPermissionsToDataset(final Model model,
            @RequestParam("usernames") final String usernames, @RequestParam("accessLevel") final String accessLevel,
            @RequestParam("path") String datasetId, final Principal principal, HttpServletRequest request)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDataset(Long.decode(datasetId));
        String view = permissionService.canDataset(PermissionType.ASSIGN_DATASET_PERMISSION, dataset.getId(),
                principal, new PermissionService.ResearchDatasetAction()
                {

                    @Override
                    public String act(ResearchDataset researchDataset, User user)
                    {
                        Path path = PathBuilder.datasetPath(researchDataset);
                        boolean successfulAssignment = false;
                        boolean hasFullAccess = permissionService.hasFullAccessPermissionForDataset(user,
                                researchDataset);
                        List<User> users = permissionService.parentNodeCheck(usernames, accessLevel, path);
                        if (!users.isEmpty())
                        {
                            populateDatasetModel(model, hasFullAccess, researchDataset);
                            model.addAttribute("hasError", true);
                            model.addAttribute("usersInconsistent", users);
                            return PERMISSION_NEW_PAGE;
                        }
                        successfulAssignment = assignPermissionsToTheSelectedUsers(usernames, accessLevel, path,
                                permissionService.hasFullAccessPermissionForDataset(user, researchDataset), principal);
                        if (successfulAssignment)
                        {
                            permissionService.cleanChildNodes(usernames, accessLevel, path);
                            return REDIRECT_TO_PERMISSION_VIEW_PAGE;
                        }
                        else
                        {
                            populateDatasetModel(model, hasFullAccess, researchDataset);
                            model.addAttribute("hasError", true);
                            model.addAttribute("permError", true);
                            return PERMISSION_NEW_PAGE;
                        }
                    }

                });
        if (view.equals(REDIRECT_TO_PERMISSION_VIEW_PAGE))
        {
            String redirect = REDIRECT_TO_PERMISSION_VIEW_PAGE
                    + dataset.getResearchProject().getResearchGroup().getId();
            return redirectToViewInstance(redirect, request);
        }
        else
        {
            return new ModelAndView(view);
        }
    }

    @RequestMapping(value = "/assigningPermissions", params = "type=directory", method = RequestMethod.POST)
    public ModelAndView assigningPermissionsToDirectory(final Model model,
            @RequestParam("usernames") final String usernames, @RequestParam("accessLevel") final String accessLevel,
            @RequestParam("path") String virtualPath, final Principal principal, HttpServletRequest request)
    {
        String view = permissionService.canDirectory(PermissionType.ASSIGN_DIRECTORY_PERMISSION,
                PathBuilder.buildFromString(virtualPath), principal, new PermissionService.ResearchDirectoryAction()
                {
                    @Override
                    public String act(Path path, User user)
                    {
                        boolean successfulAssignment = false;
                        // EA = FA at directory level
                        boolean hasEditingAccess = permissionService.hasEditingAccessPermissionForDirectory(user,
                                path.getPath());

                        List<User> users = permissionService.parentNodeCheck(usernames, accessLevel, path);
                        if (!users.isEmpty())
                        {
                            model.addAttribute("hasError", true);
                            model.addAttribute("usersInconsistent", users);
                            populateDirectoryModel(model, hasEditingAccess, path);
                            return PERMISSION_NEW_PAGE;
                        }

                        if (!"EDITING_ACCESS".equals(accessLevel))
                        {
                            successfulAssignment = assignPermissionsToTheSelectedUsers(usernames, accessLevel, path,
                                    true, principal);
                        }

                        if (successfulAssignment)
                        {
                            permissionService.cleanChildNodes(usernames, accessLevel, path);
                            return REDIRECT_TO_PERMISSION_VIEW_PAGE;
                        }
                        else
                        {
                            populateDirectoryModel(model, hasEditingAccess, path);
                            model.addAttribute("hasError", true);
                            model.addAttribute("permError", true);
                            return PERMISSION_NEW_PAGE;
                        }
                    }
                });
        if (view.equals(REDIRECT_TO_PERMISSION_VIEW_PAGE))
        {
            Path path = PathBuilder.buildFromString(virtualPath);
            String redirect = REDIRECT_TO_PERMISSION_VIEW_PAGE + path.getGroupId();
            return redirectToViewInstance(redirect, request);
        }
        else
        {
            return new ModelAndView(view);
        }
    }

    public boolean assignPermissionsToTheSelectedUsers(final String usernames, final String accessLevel, Path path,
            boolean currentUserHasFullAccess, Principal principal)
    {
        if ("FULL_ACCESS".equals(accessLevel) && !currentUserHasFullAccess)
        {
            return false;
        }
        else
        {
            String[] usernamesArray = usernames.split(",");
            for (String usernameToBeGrantedPermissions : usernamesArray)
            {
                TypedQuery<User> usersToBeAssignedPermissions = User
                        .findUsersByUsernameEquals(usernameToBeGrantedPermissions);

                permissionService.addPermission(usersToBeAssignedPermissions.getSingleResult(), path,
                        AccessLevel.valueOf(accessLevel));

                User user = User.findUsersByUsernameEquals(usernameToBeGrantedPermissions).getSingleResult();
                activityLogService.log(Activity.PERMISSION_ADDED, AccessLevel.valueOf(accessLevel), path, user,
                        principal);
            }
            return true;
        }
    }

    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
    public String viewPermissions(@PathVariable("id") final Long id, final Model model,
            final java.security.Principal principal)
    {
        return permissionService.canViewGroupPermission(PermissionType.VIEW_PERMISSION, id, principal,
                new PermissionService.ResearchGroupAction()
                {
                    @Override
                    public String act(ResearchGroup group, User user)
                    {
                        Path groupPath = PathBuilder.groupPath(group);

                        List<PermissionEntry> permissionList = PermissionEntry.findPermissionEntrysByPathLike(
                                groupPath.getPath()).getResultList();

                        model.addAttribute("group", group);

                        List<PermissionEntryDTO> permissionEntryList = new ArrayList<PermissionEntryDTO>();
                        for (PermissionEntry entry : permissionList)
                        {
                            Path userPath = PathBuilder.buildFromString(entry.getPath());

                            PermissionEntryDTO permissionEntryDTO = new PermissionEntryDTO();
                            permissionEntryDTO.setId(entry.getId());
                            permissionEntryDTO.setFullName(entry.getUser().getFullname());
                            permissionEntryDTO.setTypeOfUser(entry.getUser().getUserType().toString());
                            permissionEntryDTO.setDisplayPath(userPath.getDisplayName());
                            permissionEntryDTO.setAccessLevel(entry.getAccessLevel().getName());
                            permissionEntryDTO.setCanDelete(permissionService.canDeletePermission(user,
                                    entry.getUser(), userPath));

                            permissionEntryList.add(permissionEntryDTO);
                        }
                        model.addAttribute("permissionEntries", permissionEntryList);

                        model.addAttribute(Breadcrumb.BREADCRUMBS, viewPermissionBreadcrumbs);
                        return PERMISSION_VIEW_PAGE;
                    }
                });
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public String deletePermissions(@RequestParam("group") final Long groupId, @RequestParam("id") final Long id,
            final Model model, java.security.Principal principal)
    {
        return permissionService.canDeleteGroupPermission(PermissionType.DELETE_PERMISSION, groupId, principal,
                new PermissionService.ResearchGroupAction()
                {
                    @Override
                    public String act(ResearchGroup group, User user)
                    {
                        PermissionEntry entry = PermissionEntry.findPermissionEntry(id);
                        Path displayPath = PathBuilder.buildFromString(entry.getPath());
                        if (!permissionService.canDeletePermission(user, entry.getUser(), displayPath))
                        {
                            return ACCESS_DENIED;
                        }

                        model.addAttribute("displayPath", displayPath.getDisplayName());
                        model.addAttribute("permissionEntry", entry);
                        model.addAttribute("group", groupId);
                        model.addAttribute("id", id);
                        return "permission/delete";
                    }
                });
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ModelAndView deletePermissions(final Model model, final HttpServletRequest request,
            final Principal principal, @RequestParam(value = "group") final Long groupId,
            @RequestParam(value = "id") final Long id,
            @RequestParam(value = SUBMIT_PARAM, required = false) final String submit)
    {
        if (!"Delete".equals(submit))
        {
            String redirect = "/permission/view/" + groupId;
            return redirectToViewInstance(redirect, request);
        }

        String view = permissionService.canDeleteGroupPermission(PermissionType.DELETE_PERMISSION, groupId, principal,
                new PermissionService.ResearchGroupAction()
                {
                    @Override
                    public String act(ResearchGroup group, User user)
                    {
                        PermissionEntry entry = PermissionEntry.findPermissionEntry(id);
                        if (entry != null)
                        {
                            PermissionEntry entryLogged = entry;
                            Path path = PathBuilder.buildFromString(entry.getPath());
                            entry.remove();

                            activityLogService.log(Activity.PERMISSION_DELETE, entryLogged.getAccessLevel(), path,
                                    entryLogged.getUser(), principal);
                        }
                        return PERMISSION_DELETE_PAGE;
                    }
                });
        if (PERMISSION_DELETE_PAGE.equals(view))
        {
            String redirect = "/permission/view/" + groupId;
            return redirectToViewInstance(redirect, request);
        }
        else
        {
            return new ModelAndView(view);
        }
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

    public String addAttributeToModel(Model model, Object object, String objectType, String typeForUrl,
            boolean hasFullAccess)
    {
        model.addAttribute(Breadcrumb.BREADCRUMBS, breadcrumbs);
        model.addAttribute("object", object);
        model.addAttribute("objectType", objectType);
        model.addAttribute("typeForUrl", typeForUrl);
        if (!hasFullAccess)
        {
            model.addAttribute("doesNothaveFullAccess", true);
        }
        return PERMISSION_INDEX;
    }

    private ModelAndView redirectToViewInstance(String view, HttpServletRequest request)
    {
        ModelAndView mav = modelAndViewRedirectHelper.generateRedirectView(view, request);
        return mav;
    }

    /**
     * Class to handle the controller to view data passing
     * 
     * @version $Rev: 29 $
     */
    public class PermissionEntryDTO
    {
        private Long id;

        private String fullName;

        private String typeOfUser;

        private String displayPath;

        private String accessLevel;

        private boolean canDelete;

        public String getFullName()
        {
            return fullName;
        }

        public void setFullName(String fullName)
        {
            this.fullName = fullName;
        }

        public String getTypeOfUser()
        {
            return typeOfUser;
        }

        public Long getId()
        {
            return id;
        }

        public void setId(Long id)
        {
            this.id = id;
        }

        public void setTypeOfUser(String typeOfUser)
        {
            this.typeOfUser = typeOfUser;
        }

        public String getDisplayPath()
        {
            return displayPath;
        }

        public void setDisplayPath(String displayPath)
        {
            this.displayPath = displayPath;
        }

        public String getAccessLevel()
        {
            return accessLevel;
        }

        public void setAccessLevel(String accessLevel)
        {
            this.accessLevel = accessLevel;
        }

        public boolean isCanDelete()
        {
            return canDelete;
        }

        public void setCanDelete(boolean canDelete)
        {
            this.canDelete = canDelete;
        }
    }
}
