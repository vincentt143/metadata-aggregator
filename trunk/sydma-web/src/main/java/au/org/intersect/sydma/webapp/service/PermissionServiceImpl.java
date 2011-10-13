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
package au.org.intersect.sydma.webapp.service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import au.org.intersect.sydma.webapp.domain.AccessLevel;
import au.org.intersect.sydma.webapp.domain.PermissionEntry;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;
import au.org.intersect.sydma.webapp.domain.Role;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.permission.PermissionDto;
import au.org.intersect.sydma.webapp.permission.PermissionType;
import au.org.intersect.sydma.webapp.permission.dataset.AllDatasetsForProjectPermissionQuery;
import au.org.intersect.sydma.webapp.permission.dataset.ConcreteDatasetsPermissionQuery;
import au.org.intersect.sydma.webapp.permission.dataset.DatasetPermissionQuery;
import au.org.intersect.sydma.webapp.permission.dataset.NoDatasetsPermissionQuery;
import au.org.intersect.sydma.webapp.permission.group.AllGroupsPermissionsQuery;
import au.org.intersect.sydma.webapp.permission.group.ConcreteGroupsPermissionsQuery;
import au.org.intersect.sydma.webapp.permission.group.GroupPermissionQuery;
import au.org.intersect.sydma.webapp.permission.group.NoGroupsPermissionsQuery;
import au.org.intersect.sydma.webapp.permission.path.Path;
import au.org.intersect.sydma.webapp.permission.path.PathBuilder;
import au.org.intersect.sydma.webapp.permission.project.AllProjectsForGroupPermissionQuery;
import au.org.intersect.sydma.webapp.permission.project.ConcreteProjectsPermissionQuery;
import au.org.intersect.sydma.webapp.permission.project.NoProjectsPermissionQuery;
import au.org.intersect.sydma.webapp.permission.project.ProjectPermissionQuery;

/**
 * Mock Service Implementation Returns true for role Researchers
 * 
 * @version $Rev: 29 $
 */
// TODO CHECKSTYLE-OFF: ClassFanOutComplexityCheck
// TODO CHECKSTYLE-OFF: MultipleStringLiteralsCheck
// TODO CHECKSTYLE-OFF: ReturnCountCheck
@Service
public class PermissionServiceImpl implements PermissionService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionServiceImpl.class);

    @Override
    public String canGroup(PermissionType permission, Long id, Principal principal, ResearchGroupAction action)
    {
        ResearchGroup researchGroup = ResearchGroup.findResearchGroup(id);
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();

        if (permission.equals(PermissionType.EDIT_GROUP) && !canEditGroup(user, researchGroup))
        {
            return "accessDenied";
        }

        if (permission.equals(PermissionType.CREATE_PROJECT) && !canCreateProject(user, researchGroup))
        {
            return "accessDenied";
        }

        if (permission.equals(PermissionType.ASSIGN_PERMISSION) && !canAssignPermission(user, researchGroup))
        {
            return "accessDenied";
        }

        if (permission.equals(PermissionType.VIEW_PERMISSION) && !canViewPermission(user, researchGroup))
        {
            return "accessDenied";
        }

        return action.act(researchGroup, user);
    }

    @Override
    public String canProject(PermissionType permission, Long id, Principal principal, ResearchProjectAction action)
    {
        ResearchProject project = ResearchProject.findResearchProject(id);
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();

        if (permission.equals(PermissionType.EDIT_PROJECT) && !canEditProject(user, project))
        {
            return "accessDenied";
        }
        if (permission.equals(PermissionType.CREATE_DATASET) && !canCreateDataset(user, project))
        {
            return "accessDenied";
        }

        return action.act(project, user);
    }

    @Override
    public String canDataset(PermissionType permission, Long id, Principal principal, ResearchDatasetAction action)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDataset(id);
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();

        if (permission.equals(PermissionType.EDIT_DATASET) && !canEditDataset(user, dataset))
        {
            return "accessDenied";
        }

        return action.act(dataset, user);
    }

    public boolean canEditGroup(User user, ResearchGroup researchGroup)
    {
        AccessLevel accessLevel = getGroupLevelFor(researchGroup, user);
        return accessLevel.isAtLeast(AccessLevel.EDITING_ACCESS);
    }

    public boolean canEditProject(User user, ResearchProject researchProject)
    {
        AccessLevel accessLevel = getProjectLevelFor(researchProject, user);
        return accessLevel.isAtLeast(AccessLevel.EDITING_ACCESS);
    }

    public boolean canCreateProject(User user, ResearchGroup researchGroup)
    {
        AccessLevel accessLevel = getGroupLevelFor(researchGroup, user);
        return accessLevel.isAtLeast(AccessLevel.FULL_ACCESS);
    }

    private boolean canAssignPermission(User user, ResearchGroup researchGroup)
    {
        AccessLevel accessLevel = getGroupLevelFor(researchGroup, user);
        return accessLevel.isAtLeast(AccessLevel.EDITING_ACCESS);
    }

    private boolean canViewPermission(User user, ResearchGroup researchGroup)
    {
        Path path = PathBuilder.groupPath(researchGroup);
        Map<Path, PermissionEntry> entries = getEntriesAffectingGroup(path, user);

        for (PermissionEntry entry : entries.values())
        {
            if (entry.getAccessLevel().isAtLeast(AccessLevel.EDITING_ACCESS))
            {
                return true;
            }
        }
        return false;
    }

    private boolean canEditDataset(User user, ResearchDataset researchDataset)
    {
        AccessLevel accessLevel = getDatasetLevelFor(researchDataset, user);
        return accessLevel.isAtLeast(AccessLevel.EDITING_ACCESS);
    }

    private boolean canCreateDataset(User user, ResearchProject researchProject)
    {
        AccessLevel accessLevel = getProjectLevelFor(researchProject, user);
        return accessLevel.isAtLeast(AccessLevel.FULL_ACCESS);
    }

    public GroupPermissionQuery getViewGroupPermissions(User user)
    {
        LOGGER.info("Principal: " + user.getUsername());
        if (isIctSupportUser(user))
        {
            LOGGER.info("ICT support > Access to view all groups");
            return new AllGroupsPermissionsQuery();
        }
        else
        {
            return getConcreteGroupPermissions(user);
        }
    }

    public GroupPermissionQuery getUploadGroupPermission(User user)
    {
        LOGGER.info("Getting concrete group UPLOAD permissions");
        Collection<PermissionEntry> listPermissions = user.getPermissionEntries();

        Set<Long> ids = new HashSet<Long>();
        for (PermissionEntry entry : listPermissions)
        {
            if (entry.getAccessLevel().isAtLeast(AccessLevel.EDITING_ACCESS))
            {
                Path path = PathBuilder.buildFromString(entry.getPath());
                LOGGER.info("Adding access to group" + path.getGroupId());
                ids.add(path.getGroupId());
            }
        }

        if (ids.isEmpty())
        {
            return new NoGroupsPermissionsQuery();
        }

        return new ConcreteGroupsPermissionsQuery(ids);

    }

    @Override
    public GroupPermissionQuery getDownloadGroupPermission(User user)
    {
        LOGGER.info("Getting concrete group UPLOAD permissions");
        Collection<PermissionEntry> listPermissions = user.getPermissionEntries();

        Set<Long> ids = new HashSet<Long>();
        for (PermissionEntry entry : listPermissions)
        {
            Path path = PathBuilder.buildFromString(entry.getPath());
            LOGGER.info("Adding access to group" + path.getGroupId());
            ids.add(path.getGroupId());
        }

        if (ids.isEmpty())
        {
            return new NoGroupsPermissionsQuery();
        }

        return new ConcreteGroupsPermissionsQuery(ids);

    }

    public ProjectPermissionQuery getViewProjectPermissions(User user, ResearchGroup researchGroup)
    {
        if (isIctSupportUser(user))
        {
            LOGGER.info("ICT support > Access to view all projects");
            return new AllProjectsForGroupPermissionQuery(researchGroup.getId());
        }
        else
        {
            return getConcreteProjectPermissions(user, researchGroup);
        }
    }

    @Override
    public ProjectPermissionQuery getUploadProjectPermission(User user, Path researchGroupPath)
    {
        LOGGER.info("Getting concrete project permissions");
        Set<Long> concreteProjectIds = new HashSet<Long>();
        Map<Path, PermissionEntry> entries = getEntriesAffectingGroup(researchGroupPath, user);

        for (Path path : entries.keySet())
        {
            PermissionEntry entry = entries.get(path);
            if (entry.getAccessLevel().isAtLeast(AccessLevel.EDITING_ACCESS))
            {
                // if it is a group, then that implies that we add all permissions
                if (path.isGroupPath())
                {
                    LOGGER.info("Grants access to all projects");
                    // If we have access to the group, then we can see all its projects
                    return new AllProjectsForGroupPermissionQuery(researchGroupPath.getGroupId());
                }
                // Otherwise add viewing permission to the specific project
                LOGGER.info("Adding access to dataset" + path.getProjectId());
                concreteProjectIds.add(path.getProjectId());

            }
        }

        if (concreteProjectIds.isEmpty())
        {
            LOGGER.info("It does not have access to any project");
            return new NoProjectsPermissionQuery();
        }

        return new ConcreteProjectsPermissionQuery(concreteProjectIds);
    }

    @Override
    public ProjectPermissionQuery getDownloadProjectPermission(User user, Path researchGroupPath)
    {
        LOGGER.info("Getting concrete project permissions");
        Set<Long> concreteProjectIds = new HashSet<Long>();
        Map<Path, PermissionEntry> entries = getEntriesAffectingGroup(researchGroupPath, user);

        for (Path path : entries.keySet())
        {
            // if it is a group, then that implies that we add all permissions
            if (path.isGroupPath())
            {
                LOGGER.info("Grants access to all projects");
                // If we have access to the group, then we can see all its projects
                return new AllProjectsForGroupPermissionQuery(researchGroupPath.getGroupId());
            }
            // Otherwise add viewing permission to the specific project
            LOGGER.info("Adding access to dataset" + path.getProjectId());
            concreteProjectIds.add(path.getProjectId());
        }

        if (concreteProjectIds.isEmpty())
        {
            LOGGER.info("It does not have access to any project");
            return new NoProjectsPermissionQuery();
        }

        return new ConcreteProjectsPermissionQuery(concreteProjectIds);
    }

    @Override
    public DatasetPermissionQuery getUploadDatasetPermissions(User user, Path researchProjectPath)
    {
        LOGGER.info("Getting concrete dataset permissions");
        Set<Long> concreteDatasetIds = new HashSet<Long>();
        Map<Path, PermissionEntry> entries = getEntriesAffectingProject(researchProjectPath, user);

        for (Path path : entries.keySet())
        {
            PermissionEntry entry = entries.get(path);
            if (entry.getAccessLevel().isAtLeast(AccessLevel.EDITING_ACCESS))
            {
                // if it is a group, then that implies that we add all permissions
                if (path.isGroupPath() || path.isProjectPath())
                {
                    LOGGER.info("Grants access to all projects");
                    // If we have access to the group, then we can see all its projects
                    return new AllDatasetsForProjectPermissionQuery(researchProjectPath.getGroupId());
                }
                // Otherwise add viewing permission to the specific project
                LOGGER.info("Adding access to dataset" + path.getProjectId());
                concreteDatasetIds.add(path.getDatasetId());

            }
        }

        if (concreteDatasetIds.isEmpty())
        {
            LOGGER.info("It does not have access to any project");
            return new NoDatasetsPermissionQuery();
        }

        return new ConcreteDatasetsPermissionQuery(concreteDatasetIds);
    }

    @Override
    public DatasetPermissionQuery getDownloadDatasetPermissions(User user, Path researchProjectPath)
    {
        LOGGER.info("Getting concrete dataset permissions");
        Set<Long> concreteDatasetIds = new HashSet<Long>();
        Map<Path, PermissionEntry> entries = getEntriesAffectingProject(researchProjectPath, user);

        for (Path path : entries.keySet())
        {
            // if it is a group, then that implies that we add all permissions
            if (path.isGroupPath() || path.isProjectPath())
            {
                LOGGER.info("Grants access to all projects");
                // If we have access to the group, then we can see all its projects
                return new AllDatasetsForProjectPermissionQuery(researchProjectPath.getGroupId());
            }
            // Otherwise add viewing permission to the specific project
            LOGGER.info("Adding access to dataset" + path.getProjectId());
            concreteDatasetIds.add(path.getDatasetId());

        }

        if (concreteDatasetIds.isEmpty())
        {
            LOGGER.info("It does not have access to any project");
            return new NoDatasetsPermissionQuery();
        }

        return new ConcreteDatasetsPermissionQuery(concreteDatasetIds);
    }

    public DatasetPermissionQuery getViewDatasetPermissions(User user, ResearchProject researchProject)
    {
        if (isIctSupportUser(user))
        {
            LOGGER.info("ICT support > Access to view all datasets");
            return new AllDatasetsForProjectPermissionQuery(researchProject.getId());
        }
        else
        {
            return getConcreteDatasetPermissions(user, researchProject);
        }
    }

    private DatasetPermissionQuery getConcreteDatasetPermissions(User user, ResearchProject researchProject)
    {
        LOGGER.info("Getting concrete dataset permissions");
        Set<Long> concreteDatasetIds = new HashSet<Long>();
        Path projectPath = PathBuilder.projectPath(researchProject);
        Map<Path, PermissionEntry> entries = getEntriesAffectingProject(projectPath, user);

        for (Path path : entries.keySet())
        {
            LOGGER.info("PermissionDto Entry Path" + path.getPath());
            // if it is a group or project, then that implies that we add all permissions
            if (path.isGroupPath() || path.isProjectPath())
            {
                LOGGER.info("Grants access to all datasets");
                return new AllDatasetsForProjectPermissionQuery(researchProject.getId());
            }
            // Otherwise, add the concrete dataset
            LOGGER.info("Adding access to dataset" + path.getDatasetId());
            concreteDatasetIds.add(path.getDatasetId());
        }

        if (concreteDatasetIds.isEmpty())
        {
            LOGGER.info("It does not have access to any dataset");
            return new NoDatasetsPermissionQuery();
        }

        return new ConcreteDatasetsPermissionQuery(concreteDatasetIds);

    }

    private ProjectPermissionQuery getConcreteProjectPermissions(User user, ResearchGroup researchGroup)
    {
        LOGGER.info("Getting concrete project permissions");
        Set<Long> concreteProjectIds = new HashSet<Long>();
        Path researchGroupPath = PathBuilder.groupPath(researchGroup);
        Map<Path, PermissionEntry> entries = getEntriesAffectingGroup(researchGroupPath, user);

        for (Path path : entries.keySet())
        {
            LOGGER.info("PermissionDto Entry Path" + path.getPath());
            // if it is a group, then that implies that we add all permissions
            if (path.isGroupPath())
            {
                LOGGER.info("Grants access to all projects");
                // If we have access to the group, then we can see all its projects
                return new AllProjectsForGroupPermissionQuery(researchGroup.getId());
            }
            // Otherwise add viewing permission to the specific project
            LOGGER.info("Adding access to dataset" + path.getProjectId());
            concreteProjectIds.add(path.getProjectId());
        }

        if (concreteProjectIds.isEmpty())
        {
            LOGGER.info("It does not have access to any project");
            return new NoProjectsPermissionQuery();
        }

        return new ConcreteProjectsPermissionQuery(concreteProjectIds);
    }

    private GroupPermissionQuery getConcreteGroupPermissions(User user)
    {
        LOGGER.info("Getting concrete group permissions");
        Collection<PermissionEntry> listPermissions = user.getPermissionEntries();

        if (listPermissions.isEmpty())
        {
            LOGGER.info("User has no permissions over the group");
            return new NoGroupsPermissionsQuery();
        }

        LOGGER.info("User has concrete permissions over the group");
        Set<Long> ids = new HashSet<Long>();
        for (PermissionEntry entry : listPermissions)
        {
            Path path = PathBuilder.buildFromString(entry.getPath());
            LOGGER.info("Adding access to group" + path.getGroupId());
            ids.add(path.getGroupId());
        }

        return new ConcreteGroupsPermissionsQuery(ids);
    }

    @Override
    public void addPermission(User user, Path path, AccessLevel level)
    {
        PermissionEntry entry = new PermissionEntry();
        entry.setUser(user);
        entry.setPath(path.getPath());
        entry.setAccessLevel(level);
        entry.merge();
    }

    @Override
    public List<PermissionDto> getResearchGroupPermissions(ResearchGroup group, User user)
    {
        List<PermissionDto> permissionDtos = new ArrayList<PermissionDto>();
        AccessLevel accessLevel = getGroupLevelFor(group, user);
        LOGGER.info("PermissionDto level for group" + group.getId() + ": " + accessLevel);
        permissionDtos.add(new PermissionDto(PermissionType.EDIT_GROUP, accessLevel
                .isAtLeast(AccessLevel.EDITING_ACCESS)));
        permissionDtos.add(new PermissionDto(PermissionType.CREATE_PROJECT, accessLevel
                .isAtLeast(AccessLevel.FULL_ACCESS)));
        permissionDtos.add(new PermissionDto(PermissionType.ASSIGN_PERMISSION, accessLevel
                .isAtLeast(AccessLevel.EDITING_ACCESS)));
        permissionDtos.add(new PermissionDto(PermissionType.VIEW_PERMISSION, canViewPermission(user, group)));
        return permissionDtos;
    }

    public List<PermissionDto> getResearchProjectPermissions(ResearchProject project, User user)
    {
        List<PermissionDto> permissionDtos = new ArrayList<PermissionDto>();
        AccessLevel accessLevel = getProjectLevelFor(project, user);
        LOGGER.info("PermissionDto level for project" + project.getId() + ": " + accessLevel);
        permissionDtos.add(new PermissionDto(PermissionType.EDIT_PROJECT, accessLevel
                .isAtLeast(AccessLevel.EDITING_ACCESS)));
        permissionDtos.add(new PermissionDto(PermissionType.CREATE_DATASET, accessLevel
                .isAtLeast(AccessLevel.FULL_ACCESS)));
        return permissionDtos;
    }

    @Override
    public List<PermissionDto> getResearchDatasetPermissions(ResearchDataset dataset, User user)
    {
        List<PermissionDto> permissionDtos = new ArrayList<PermissionDto>();
        AccessLevel accessLevel = getDatasetLevelFor(dataset, user);
        LOGGER.info("PermissionDto level for dataset" + dataset.getId() + ": " + accessLevel);
        permissionDtos.add(new PermissionDto(PermissionType.EDIT_DATASET, accessLevel
                .isAtLeast(AccessLevel.EDITING_ACCESS)));
        // TODO: Confirm how this works now
        permissionDtos.add(new PermissionDto(PermissionType.PUBLISH_DATASET, accessLevel
                .isAtLeast(AccessLevel.FULL_ACCESS)));
        permissionDtos.add(new PermissionDto(PermissionType.REJECT_ADVERTISING_DATASET, accessLevel
                .isAtLeast(AccessLevel.FULL_ACCESS)));
        permissionDtos.add(new PermissionDto(PermissionType.CREATE_DATABASE_INSTANCE, accessLevel
                .isAtLeast(AccessLevel.FULL_ACCESS)));
        permissionDtos.add(new PermissionDto(PermissionType.VIEW_DATABASE_INSTANCE, accessLevel
                .isAtLeast(AccessLevel.FULL_ACCESS)));
        return permissionDtos;
    }

    private Map<Path, PermissionEntry> getEntriesAffectingProject(Path projectPath, User user)
    {
        Map<Path, PermissionEntry> permissionEntries = new HashMap<Path, PermissionEntry>();
        for (PermissionEntry permissionEntry : user.getPermissionEntries())
        {
            Path path = PathBuilder.buildFromString(permissionEntry.getPath());
            LOGGER.info("getEntriesAffectingProject: " + path.getPath());
            if (path.isGroupPath())
            {
                if (path.getGroupId().equals(projectPath.getGroupId()))
                {
                    LOGGER.info("Added");
                    permissionEntries.put(path, permissionEntry);
                }
            }
            else if (path.getProjectId().equals(projectPath.getProjectId()))
            {
                LOGGER.info("Added");
                permissionEntries.put(path, permissionEntry);
            }
        }
        return permissionEntries;
    }

    private Map<Path, PermissionEntry> getEntriesAffectingDataset(ResearchDataset dataset, User user)
    {

        Map<Path, PermissionEntry> permissionEntries = new HashMap<Path, PermissionEntry>();
        for (PermissionEntry permissionEntry : user.getPermissionEntries())
        {
            Path path = PathBuilder.buildFromString(permissionEntry.getPath());
            LOGGER.info("getEntriesAffectingDataset: " + path.getPath());
            if (path.isGroupPath())
            {
                if (path.getGroupId().equals(dataset.getResearchProject().getResearchGroup().getId()))
                {
                    LOGGER.info("Added");
                    permissionEntries.put(path, permissionEntry);
                }
            }
            else if (path.isProjectPath())
            {
                if (path.getProjectId().equals(dataset.getResearchProject().getId()))
                {
                    LOGGER.info("Added");
                    permissionEntries.put(path, permissionEntry);
                }
            }
            else if (path.getDatasetId().equals(dataset.getId()))
            {
                permissionEntries.put(path, permissionEntry);
            }
        }
        return permissionEntries;
    }

    protected AccessLevel getDatasetLevelFor(ResearchDataset dataset, User user)
    {
        AccessLevel accessLevel = AccessLevel.NO_ACCESS;
        Map<Path, PermissionEntry> permissionEntries = getEntriesAffectingDataset(dataset, user);

        LOGGER.info("getDatasetLevelFor: " + dataset.getId());
        if (permissionEntries.size() > 0)
        {
            // At least, viewing access
            accessLevel = AccessLevel.VIEWING_ACCESS;
        }

        for (Path permissionEntryPath : permissionEntries.keySet())
        {
            // Check for explicit access on the dataset
            if (permissionEntryPath.isDatasetPath())
            {
                // There is an explicit access level to the project, so return it
                PermissionEntry entry = permissionEntries.get(permissionEntryPath);
                LOGGER.info(entry.getAccessLevel().toString());
                return entry.getAccessLevel();
            }

            if (permissionEntryPath.isGroupPath() || permissionEntryPath.isProjectPath())
            {
                AccessLevel entryLevel = permissionEntries.get(permissionEntryPath).getAccessLevel();
                // Get the highest access level
                accessLevel = accessLevel.isAtLeast(entryLevel) ? accessLevel : entryLevel;
            }
        }

        // if not, check if ICT
        if (accessLevel == AccessLevel.NO_ACCESS && isIctSupportUser(user))
        {
            accessLevel = AccessLevel.VIEWING_ACCESS;
        }

        LOGGER.info(accessLevel.toString());
        return accessLevel;
    }

    protected AccessLevel getProjectLevelFor(ResearchProject project, User user)
    {
        AccessLevel accessLevel = AccessLevel.NO_ACCESS;
        Path projectPath = PathBuilder.projectPath(project);
        Map<Path, PermissionEntry> permissionEntries = getEntriesAffectingProject(projectPath, user);

        LOGGER.info("getProjectLevelFor: " + project.getId());
        if (permissionEntries.size() > 0)
        {
            // At least, viewing access
            accessLevel = AccessLevel.VIEWING_ACCESS;
        }

        for (Path permissionEntryPath : permissionEntries.keySet())
        {
            // Check for explicit access on the project
            if (permissionEntryPath.isProjectPath())
            {
                // There is an explicit access level to the project, so return it
                PermissionEntry entry = permissionEntries.get(permissionEntryPath);
                LOGGER.info(entry.getAccessLevel().toString());
                return entry.getAccessLevel();
            }
            if (permissionEntryPath.isGroupPath())
            {
                PermissionEntry entry = permissionEntries.get(permissionEntryPath);
                accessLevel = entry.getAccessLevel();
            }
        }

        // if not, check if ICT
        if (accessLevel == AccessLevel.NO_ACCESS && isIctSupportUser(user))
        {
            accessLevel = AccessLevel.VIEWING_ACCESS;
        }

        LOGGER.info(accessLevel.toString());
        return accessLevel;
    }

    protected AccessLevel getGroupLevelFor(ResearchGroup group, User user)
    {
        AccessLevel accessLevel = AccessLevel.NO_ACCESS;
        Path researchGroupPath = PathBuilder.groupPath(group);
        Map<Path, PermissionEntry> permissionEntries = getEntriesAffectingGroup(researchGroupPath, user);
        LOGGER.info("getGroupLevelFor: " + group.getId());
        for (Path permissionEntryPath : permissionEntries.keySet())
        {
            if (permissionEntryPath.isGroupPath())
            {
                // There is an explicit access level to the group, so return it
                PermissionEntry entry = permissionEntries.get(permissionEntryPath);
                LOGGER.info(entry.getAccessLevel().toString());
                return entry.getAccessLevel();
            }
            // Implicit viewing access
            accessLevel = AccessLevel.VIEWING_ACCESS;
        }

        // if not, check if ICT
        if (accessLevel == AccessLevel.NO_ACCESS && isIctSupportUser(user))
        {
            accessLevel = AccessLevel.VIEWING_ACCESS;
        }

        LOGGER.info(accessLevel.toString());
        return accessLevel;
    }

    private Map<Path, PermissionEntry> getEntriesAffectingGroup(Path groupPath, User user)
    {

        Map<Path, PermissionEntry> permissionEntries = new HashMap<Path, PermissionEntry>();
        LOGGER.info("getEntriesAffectingGroup: " + groupPath.getGroupId());
        for (PermissionEntry permissionEntry : user.getPermissionEntries())
        {
            Path path = PathBuilder.buildFromString(permissionEntry.getPath());
            LOGGER.info("Entry path: " + path.getPath());
            if (path.getGroupId().equals(groupPath.getGroupId()))
            {
                LOGGER.info("Added");
                permissionEntries.put(path, permissionEntry);
            }
            else
            {
                LOGGER.info("Different ids: Path(" + path.getGroupId() + ") Group(" + groupPath.getGroupId() + ")");
            }
        }
        return permissionEntries;
    }

    private boolean isIctSupportUser(User user)
    {
        LOGGER.info("isIctSupportUser?");
        for (Role role : user.getRoles())
        {
            if (role.getName().equals(Role.RoleNames.ROLE_ICT_SUPPORT.toString()))
            {
                LOGGER.info("true");
                return true;
            }
        }
        LOGGER.info("false");
        return false;
    }

}
