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

// TODO CHECKSTYLE-OFF: ImportOrderCheck
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import flexjson.JSONSerializer;

import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.permission.PermissionDto;
import au.org.intersect.sydma.webapp.permission.dataset.DatasetPermissionQuery;
import au.org.intersect.sydma.webapp.permission.group.GroupPermissionQuery;
import au.org.intersect.sydma.webapp.permission.path.Path;
import au.org.intersect.sydma.webapp.permission.path.PathBuilder;
import au.org.intersect.sydma.webapp.permission.project.ProjectPermissionQuery;
import au.org.intersect.sydma.webapp.service.PermissionService;
import au.org.intersect.sydma.webapp.util.Breadcrumb;

/**
 * Home Controller.
 */
@RequestMapping("/home/**")
@Controller
public class HomeController
{
    private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);
    
    private static final String EXCLUDE_CLASS = "*.class";
    
    private static List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
    @Autowired
    private PermissionService permissionService;

    static
    {
        breadcrumbs.add(new Breadcrumb("application.title"));
    }

    @RequestMapping
    public String index(Model model, java.security.Principal principal)
    {
        model.addAttribute("breadcrumbs", breadcrumbs);
        
        return "home/index";
    }
    

    @RequestMapping("/getGroups")
    @ResponseBody
    public String getGroups(java.security.Principal principal)
    {
        List<ActionLinks> rgLinkList = new ArrayList<ActionLinks>();
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        
        GroupPermissionQuery permissionQuery = permissionService.getViewGroupPermissions(user);
        for (ResearchGroup group : ResearchGroup.findResearchGroupWithPermission(permissionQuery))
        {
            ActionLinks link = new ActionLinks();
            link.setName(group.getName());
            link.setId(group.getId());
            link.addPermissions(permissionService.getResearchGroupPermissions(group, user));
            rgLinkList.add(link);
        }
        JSONSerializer serializer = new JSONSerializer();
        return serializer.exclude(EXCLUDE_CLASS).serialize(rgLinkList);
    }


    @RequestMapping("/getProjectsForGroup/{id}")
    @ResponseBody
    public String getProjectsForGroup(@PathVariable("id") Long groupId, java.security.Principal principal)
    {
        ResearchGroup researchGroup = ResearchGroup.findResearchGroup(groupId);
        if (researchGroup == null)
        {
            throw new EntityNotFoundException("Group with id [" + groupId + "] could not be found");
        }

        List<ActionLinks> rpLinkList = new ArrayList<ActionLinks>();
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        
        Path researchGroupPath = PathBuilder.groupPath(researchGroup);
        ProjectPermissionQuery permissionQuery = permissionService.getViewProjectPermissions(user, researchGroupPath);
        
        for (ResearchProject project : ResearchProject.findProjectWithPermission(permissionQuery))
        {

            ActionLinks link = new ActionLinks();
            link.setName(project.getName());
            link.setId(project.getId());
            link.addPermissions(permissionService.getResearchProjectPermissions(project, user));
            rpLinkList.add(link);
        }
        JSONSerializer serializer = new JSONSerializer();
        return serializer.exclude(EXCLUDE_CLASS).serialize(rpLinkList);
    }

    @RequestMapping("/getDatasetsForProject/{id}")
    @ResponseBody
    public String getDatasetsForProject(@PathVariable("id") Long projectId, java.security.Principal principal)
    {
        ResearchProject researchProject = ResearchProject.findResearchProject(projectId);
        if (researchProject == null)
        {
            throw new EntityNotFoundException("Project with id [" + projectId + "] could not be found");
        }

        List<ActionLinks> rdLinkList = new ArrayList<HomeController.ActionLinks>();
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();

        Path projectPath = PathBuilder.projectPath(researchProject);
        DatasetPermissionQuery permissionQuery = permissionService.getViewDatasetPermissions(user, projectPath);
        for (ResearchDataset dataset : ResearchDataset.findDatasetsWithPermission(permissionQuery))
        {
            boolean showAdvertiseLink = dataset.canBeAdvertisedBy(user);
            boolean showRejectLink = dataset.canBeRejectedBy(user);
            boolean showUnadvertiseLink = dataset.canBeUnadvertisedBy(user);
            
            ActionLinks link = new ActionLinks();
            link.setName(dataset.getName());
            link.setId(dataset.getId());
            link.setShowAdvertiseLink(showAdvertiseLink);
            link.setShowRejectAdvertisingLink(showRejectLink);
            link.setShowUnadvertiseLink(showUnadvertiseLink);
            link.setAdvertiseStatus(dataset.getPubliciseStatus().getCode());
            link.setPhysicalCollection(BooleanUtils.toBoolean(dataset.getIsPhysical()));    
            if (dataset.getDatabaseInstance() != null)
            {
                link.setResearchDatasetDBSchemaId(dataset.getDatabaseInstance().getId());
            }
            
            link.addPermissions(permissionService.getResearchDatasetPermissions(dataset, user));
            
            //link.addPermission("", permissionService.can(user, PermissionService.Action.UPDATE, ""));
            //link.addPermission("", permissionService.can(user, PermissionService.Action.UPDATE, ""));
            //link.addPermission("",
            //        permissionService.can(user, PermissionService.Action.UPDATE, ""));
            link.addPermission("unadvertiseDataset", showUnadvertiseLink);
            rdLinkList.add(link);
        }
        JSONSerializer serializer = new JSONSerializer();
        return serializer.exclude(EXCLUDE_CLASS).serialize(rdLinkList);
    }

    /**
     * Represents the actions that can be taken in the different Groups, Projects, and Datasets, together with the
     * corresponding user permission
     * 
     * @version $Rev: 29 $
     */
    public class ActionLinks
    {
        private Long id;
        private String name;
        private Map<String, Boolean> permissionMap = new HashMap<String, Boolean>();
        private boolean showAdvertiseLink;
        private boolean showRejectAdvertisingLink;
        private boolean showUnadvertiseLink;
        private String advertiseStatus;
        private boolean physicalCollection;
        private Long researchDatasetDBSchemaId;

        public String getAdvertiseStatus()
        {
            return advertiseStatus;
        }

        public void setAdvertiseStatus(String advertiseStatus)
        {
            this.advertiseStatus = advertiseStatus;
        }

        public boolean isShowRejectAdvertisingLink()
        {
            return showRejectAdvertisingLink;
        }

        public void setShowRejectAdvertisingLink(boolean showRejectAdvertisingLink)
        {
            this.showRejectAdvertisingLink = showRejectAdvertisingLink;
        }

        public void setId(Long id)
        {
            this.id = id;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public void addPermissions(List<PermissionDto> permissionDtos)
        {
            for (PermissionDto permissionDto : permissionDtos)
            {
                permissionMap.put(permissionDto.getPermission(), permissionDto.getAllowed());                
            }
        }
        
        public void addPermission(String permission, boolean allowed)
        {
            permissionMap.put(permission, allowed);
        }

        public Long getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        public Map<String, Boolean> getPermissionMap()
        {
            return permissionMap;
        }

        public boolean isShowAdvertiseLink()
        {
            return showAdvertiseLink;
        }

        public void setShowAdvertiseLink(boolean showAdvertiseLink)
        {
            this.showAdvertiseLink = showAdvertiseLink;
        }
        
        public boolean isShowUnadvertiseLink()
        {
            return showUnadvertiseLink;
        }

        public void setShowUnadvertiseLink(boolean showUnadvertiseLink)
        {
            this.showUnadvertiseLink = showUnadvertiseLink;
        }

        public void setPhysicalCollection(boolean physicalCollection)
        {
            this.physicalCollection = physicalCollection;
        }

        public boolean isPhysicalCollection()
        {
            return physicalCollection;
        }

        public void setResearchDatasetDBSchemaId(Long researchDatasetDBSchemaId)
        {
            this.researchDatasetDBSchemaId = researchDatasetDBSchemaId;
        }

        public Long getResearchDatasetDBSchemaId()
        {
            return researchDatasetDBSchemaId;
        }


    }
}
