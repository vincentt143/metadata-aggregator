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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import au.org.intersect.sydma.webapp.domain.AccessLevel;
import au.org.intersect.sydma.webapp.domain.PermissionEntry;
import au.org.intersect.sydma.webapp.domain.RdsRequest;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;
import au.org.intersect.sydma.webapp.domain.Role;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.permission.PermissionAppliedType;
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
@Transactional("sydmaPU")
public class PermissionServiceImpl implements PermissionService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionServiceImpl.class);

    @Autowired
    private ApplicationTypeService applicationTypeService;

    public void setApplicationTypeService(ApplicationTypeService applicationTypeService)
    {
        this.applicationTypeService = applicationTypeService;
    }

    @Override
    public String canGroup(PermissionType permission, Long id, Principal principal, ResearchGroupAction action)
    {
        ResearchGroup researchGroup = ResearchGroup.findResearchGroup(id);
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        return canGroupAction(permission, researchGroup, user) ? action.act(researchGroup, user) : "accessDenied";
    }

    @Override
    public String canViewGroupPermission(PermissionType permission, Long id, Principal principal,
            ResearchGroupAction action)
    {
        ResearchGroup researchGroup = ResearchGroup.findResearchGroup(id);
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        return canViewPermission(user, researchGroup) ? action.act(researchGroup, user) : "accessDenied";
    }

    @Override
    public String canDeleteGroupPermission(PermissionType permission, Long id, Principal principal,
            ResearchGroupAction action)
    {
        ResearchGroup researchGroup = ResearchGroup.findResearchGroup(id);
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        return canDeletePermission(user, researchGroup) ? action.act(researchGroup, user) : "accessDenied";
    }

    @Override
    public String canProject(PermissionType permission, Long id, Principal principal, ResearchProjectAction action)
    {
        ResearchProject project = ResearchProject.findResearchProject(id);
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        return canProjectAction(permission, project, user) ? action.act(project, user) : "accessDenied";
    }

    @Override
    public String canDataset(PermissionType permission, Long id, Principal principal, ResearchDatasetAction action)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDataset(id);
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        return canDatasetAction(permission, dataset, user) ? action.act(dataset, user) : "accessDenied";
    }

    @Override
    public boolean canDataset(PermissionType permission, Long id, User user)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDataset(id);
        return canDatasetAction(permission, dataset, user);
    }
    
    @Override
    public String canDirectory(PermissionType permission, Path path, Principal principal, 
            ResearchDirectoryAction action)
    {
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        return canDirectoryAction(permission, path, user) ? action.act(path, user) : "accessDenied";
    }

    @Override
    public String canDirectory(PermissionType permission, String path, Principal principal, DirectoryAction action)
    {
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();

        Path virtualPath = PathBuilder.buildFromString(path);

        return canDirectoryAction(permission, virtualPath, user) ? action.act(virtualPath, user) : "accessDenied";
    }

    public boolean hasEditingAccessPermissionForGroup(User user, ResearchGroup researchGroup)
    {
        AccessLevel accessLevel = getGroupLevelFor(researchGroup, user);
        return accessLevel.isAtLeast(AccessLevel.EDITING_ACCESS);
    }

    public boolean hasEditingAccessPermissionForProject(User user, ResearchProject researchProject)
    {
        AccessLevel accessLevel = getProjectLevelFor(researchProject, user);
        return accessLevel.isAtLeast(AccessLevel.EDITING_ACCESS);
    }

    public boolean hasEditingAccessPermissionForDataset(User user, ResearchDataset researchDataset)
    {
        AccessLevel accessLevel = getDatasetLevelFor(researchDataset, user);
        return accessLevel.isAtLeast(AccessLevel.EDITING_ACCESS);
    }

    public boolean hasFullAccessPermissionForGroup(User user, ResearchGroup researchGroup)
    {
        AccessLevel accessLevel = getGroupLevelFor(researchGroup, user);
        return accessLevel.isAtLeast(AccessLevel.FULL_ACCESS);
    }

    public boolean hasFullAccessPermissionForProject(User user, ResearchProject researchProject)
    {
        AccessLevel accessLevel = getProjectLevelFor(researchProject, user);
        return accessLevel.isAtLeast(AccessLevel.FULL_ACCESS);
    }

    public boolean hasFullAccessPermissionForDataset(User user, ResearchDataset researchDataset)
    {
        AccessLevel accessLevel = getDatasetLevelFor(researchDataset, user);
        return accessLevel.isAtLeast(AccessLevel.FULL_ACCESS);
    }

    public boolean hasFullAccessPermissionForDirectory(User user, String virtualPath)
    {
        AccessLevel accessLevel = getDirectoryLevelFor(PathBuilder.buildFromString(virtualPath), user);
        return accessLevel.isAtLeast(AccessLevel.FULL_ACCESS);
    }

    public boolean hasEditingAccessPermissionForDirectory(User user, String virtualPath)
    {
        AccessLevel accessLevel = getDirectoryLevelFor(PathBuilder.buildFromString(virtualPath), user);
        return accessLevel.isAtLeast(AccessLevel.EDITING_ACCESS);
    }

    public String canViewActivityLog(PermissionType permission, Long id, Principal principal, 
            ResearchGroupAction action)
    {
        ResearchGroup researchGroup = ResearchGroup.findResearchGroup(id);
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        return canViewActivityLog(user, researchGroup) ? action.act(researchGroup, user) : "accessDenied";
    }

    private boolean canViewPermission(User user, ResearchGroup researchGroup)
    {
        Path path = PathBuilder.groupPath(researchGroup);
        Map<Path, PermissionEntry> entries = getEntriesAffectingPath(path, user);
        for (PermissionEntry entry : entries.values())
        {
            if (entry.getAccessLevel().isAtLeast(AccessLevel.EDITING_ACCESS))
            {
                return true;
            }
        }
        return false;
    }

    private boolean canViewActivityLog(User user, ResearchGroup researchGroup)
    {
        if (isIctSupportUser(user))
        {
            return true;
        }

        AccessLevel accessLevel = getGroupLevelFor(researchGroup, user);
        return accessLevel.isAtLeast(AccessLevel.EDITING_ACCESS);
    }

    private boolean canDeletePermission(User user, ResearchGroup researchGroup)
    {
        Path path = PathBuilder.groupPath(researchGroup);
        Map<Path, PermissionEntry> entries = getEntriesAffectingPath(path, user);
        for (PermissionEntry entry : entries.values())
        {
            if (entry.getAccessLevel().isAtLeast(AccessLevel.FULL_ACCESS))
            {
                return true;
            }
        }
        return false;
    }

    public GroupPermissionQuery getViewGroupPermissions(User user)
    {
        if (isIctSupportUser(user) || isResearchDataManager(user))
        {
            return new AllGroupsPermissionsQuery();
        }
        else
        {
            return getConcreteGroupPermissions(user, PermissionType.VIEW_GROUP);
        }
    }

    @Override
    public GroupPermissionQuery getUploadGroupPermission(User user)
    {
        return getConcreteGroupPermissions(user, PermissionType.UPLOAD);
    }

    @Override
    public GroupPermissionQuery getDownloadGroupPermission(User user)
    {
        return getConcreteGroupPermissions(user, PermissionType.DOWNLOAD);
    }

    @Override
    public ProjectPermissionQuery getViewProjectPermissions(User user, Path researchGroupPath)
    {
        if (isIctSupportUser(user) || isResearchDataManager(user))
        {
            return new AllProjectsForGroupPermissionQuery(researchGroupPath.getGroupId());
        }
        else
        {
            return getConcreteProjectPermissions(user, researchGroupPath, PermissionType.VIEW_PROJECT);
        }
    }

    @Override
    public ProjectPermissionQuery getUploadProjectPermission(User user, Path researchGroupPath)
    {
        return getConcreteProjectPermissions(user, researchGroupPath, PermissionType.UPLOAD);
    }

    @Override
    public ProjectPermissionQuery getDownloadProjectPermission(User user, Path researchGroupPath)
    {
        return getConcreteProjectPermissions(user, researchGroupPath, PermissionType.DOWNLOAD);
    }

    private DatasetPermissionQuery getConcreteDatasetPermissions(User user, Path projectPath,
            PermissionType permissionType)
    {
        if (isResearchDataManager(user) && permissionType.equals(PermissionType.VIEW_DATASET))
        {
            ResearchGroup group = ResearchGroup.findResearchGroup(projectPath.getGroupId());
            if (group.getIsPhysical())
            {
                return new AllDatasetsForProjectPermissionQuery(projectPath.getProjectId());
            }
        }

        Set<Long> concreteDatasetIds = new HashSet<Long>();
        Map<Path, PermissionEntry> entries = getEntriesAffectingPath(projectPath, user);

        for (Path path : entries.keySet())
        {
            PermissionEntry entry = entries.get(path);
            if (entry.getAccessLevel().isAtLeast(permissionType.getRequiredLevel()))
            {

                // if it is a group or project, then that implies that we add all permissions
                if (path.isGroupPath() || path.isProjectPath())
                {
                    return new AllDatasetsForProjectPermissionQuery(projectPath.getProjectId());
                }
                // Otherwise, add the concrete dataset
                concreteDatasetIds.add(path.getDatasetId());
            }
        }

        if (concreteDatasetIds.isEmpty())
        {
            return new NoDatasetsPermissionQuery();
        }

        return new ConcreteDatasetsPermissionQuery(concreteDatasetIds);

    }

    @Override
    public DatasetPermissionQuery getUploadDatasetPermissions(User user, Path researchProjectPath)
    {
        return getConcreteDatasetPermissions(user, researchProjectPath, PermissionType.UPLOAD);
    }

    @Override
    public DatasetPermissionQuery getDownloadDatasetPermissions(User user, Path researchProjectPath)
    {
        return getConcreteDatasetPermissions(user, researchProjectPath, PermissionType.DOWNLOAD);
    }

    @Override
    public DatasetPermissionQuery getViewDatasetPermissions(User user, Path researchProjectPath)
    {
        if (isIctSupportUser(user) || isResearchDataManager(user))
        {
            return new AllDatasetsForProjectPermissionQuery(researchProjectPath.getProjectId());
        }
        else
        {
            return getConcreteDatasetPermissions(user, researchProjectPath, PermissionType.VIEW_DATASET);
        }
    }

    private GroupPermissionQuery getConcreteGroupPermissions(User user, PermissionType permissionType)
    {
        Collection<PermissionEntry> listPermissions = user.getPermissionEntries();

        Set<Long> ids = new HashSet<Long>();
        if (isResearchDataManager(user) && permissionType.equals(PermissionType.VIEW_GROUP))
        {
            ids.addAll(physicalGroupIds());
        }

        for (PermissionEntry entry : listPermissions)
        {
            if (entry.getAccessLevel().isAtLeast(permissionType.getRequiredLevel()))
            {

                Path path = PathBuilder.buildFromString(entry.getPath());
                // LOGGER.debug("Adding access to group" + path.getGroupId());
                ids.add(path.getGroupId());

            }
        }

        if (ids.isEmpty())
        {
            return new NoGroupsPermissionsQuery();
        }

        return new ConcreteGroupsPermissionsQuery(ids);
    }

    private Set<Long> physicalGroupIds()
    {
        Set<Long> physicalGroupIds = new HashSet<Long>();
        List<ResearchGroup> list = ResearchGroup.findResearchGroupsByIsPhysical(true).getResultList();
        for (ResearchGroup group : list)
        {
            physicalGroupIds.add(group.getId());
        }
        return physicalGroupIds;
    }

    private ProjectPermissionQuery getConcreteProjectPermissions(User user, Path researchGroupPath,
            PermissionType permissionType)
    {
        Set<Long> concreteProjectIds = new HashSet<Long>();
        Map<Path, PermissionEntry> entries = getEntriesAffectingPath(researchGroupPath, user);
        if (isResearchDataManager(user) && permissionType.equals(PermissionType.VIEW_PROJECT))
        {
            ResearchGroup group = ResearchGroup.findResearchGroup(researchGroupPath.getGroupId());
            if (group.getIsPhysical())
            {
                return new AllProjectsForGroupPermissionQuery(researchGroupPath.getGroupId());
            }
        }

        for (Path path : entries.keySet())
        {
            PermissionEntry entry = entries.get(path);
            if (entry.getAccessLevel().isAtLeast(permissionType.getRequiredLevel()))
            {

                // if it is a group, then that implies that we add all permissions
                if (path.isGroupPath())
                {
                    // If we have access to the group, then we can see all its projects
                    return new AllProjectsForGroupPermissionQuery(path.getGroupId());
                }
                // Otherwise add viewing permission to the specific project
                concreteProjectIds.add(path.getProjectId());
            }
        }

        if (concreteProjectIds.isEmpty())
        {
            return new NoProjectsPermissionQuery();
        }

        return new ConcreteProjectsPermissionQuery(concreteProjectIds);
    }

    @Override
    public void changePiTo(RdsRequest rdsRequest, User newPi)
    {
        rdsRequest.setPrincipalInvestigator(newPi);

        ResearchGroup group = rdsRequest.getResearchGroup();
        User currentPi = group.getPrincipalInvestigator();
        if (currentPi.equals(newPi))
        {
            return;
        }

        // Revoke permisison from current PI
        Path groupPath = PathBuilder.groupPath(group);
        PermissionEntry permissionEntry = PermissionEntry.findPermissionEntrysByPathEqualsAndUser(groupPath.getPath(),
                currentPi).getSingleResult();
        permissionEntry.remove();
        cleanChildNodes(newPi.getUsername(), AccessLevel.FULL_ACCESS.toString(), groupPath);
        addPermission(newPi, groupPath, AccessLevel.FULL_ACCESS);
        group.setPrincipalInvestigator(newPi);
        group.merge();
        // Reindex after PI change
        ResearchGroup.indexResearchGroup(group);
    }

    @Override
    public void addPermission(User user, Path path, AccessLevel level)
    {

        for (PermissionEntry permissionEntry : user.getPermissionEntries())
        {
            // override new values if equal
            if (permissionEntry.getPath().equals(path.getPath()))
            {
                if (!permissionEntry.getAccessLevel().isAtLeast(level))
                {
                    permissionEntry.setAccessLevel(level);
                    permissionEntry.merge();
                }
                return;
            }
        }
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
        Long groupId = group.getId();
        AccessLevel accessLevel = getGroupLevelFor(group, user);

        permissionDtos.add(getPermissionDtoFor(PermissionType.EDIT_GROUP, accessLevel));
        permissionDtos.add(getPermissionDtoFor(PermissionType.CREATE_PROJECT, accessLevel));
        permissionDtos.add(new PermissionDto(PermissionType.VIEW_GROUP, canViewGroup(user, group)));
        // TODO: Add check to vocabulary
        permissionDtos.add(getPermissionDtoFor(PermissionType.MANAGE_VOCABULARY, accessLevel));
        if (group.getIsPhysical())
        {
            permissionDtos.add(getNoPermissionDtoFor(PermissionType.ASSIGN_PERMISSION));
        }
        else
        {
            permissionDtos.add(getPermissionDtoFor(PermissionType.ASSIGN_PERMISSION, accessLevel));
        }
        permissionDtos.add(new PermissionDto(PermissionType.VIEW_PERMISSION, canViewPermission(user, group)));
        permissionDtos.add(new PermissionDto(PermissionType.ACTIVITY_LOG, canViewActivityLog(user, group)));
        return permissionDtos;
    }

    public List<PermissionDto> getResearchProjectPermissions(ResearchProject project, User user)
    {
        List<PermissionDto> permissionDtos = new ArrayList<PermissionDto>();
        AccessLevel accessLevel = getProjectLevelFor(project, user);

        permissionDtos.add(new PermissionDto(PermissionType.VIEW_PROJECT, canViewProject(user, project)));
        permissionDtos.add(getPermissionDtoFor(PermissionType.EDIT_PROJECT, accessLevel));
        permissionDtos.add(getPermissionDtoFor(PermissionType.CREATE_DATASET, accessLevel));
        if (project.getResearchGroup().getIsPhysical())
        {
            permissionDtos.add(getNoPermissionDtoFor(PermissionType.ASSIGN_PROJECT_PERMISSION));
        }
        else
        {
            permissionDtos.add(getPermissionDtoFor(PermissionType.ASSIGN_PROJECT_PERMISSION, accessLevel));
        }
        return permissionDtos;
    }

    @Override
    public List<PermissionDto> getResearchDatasetPermissions(ResearchDataset dataset, User user)
    {
        List<PermissionDto> permissionDtos = new ArrayList<PermissionDto>();
        AccessLevel accessLevel = getDatasetLevelFor(dataset, user);

        permissionDtos.add(new PermissionDto(PermissionType.VIEW_DATASET, canViewDataset(user, dataset)));
        permissionDtos.add(getPermissionDtoFor(PermissionType.EDIT_DATASET, accessLevel));
        permissionDtos.add(getPermissionDtoFor(PermissionType.PUBLISH_DATASET, accessLevel));
        permissionDtos.add(getPermissionDtoFor(PermissionType.REJECT_ADVERTISING_DATASET, accessLevel));
        if (dataset.getIsPhysical())
        {
            permissionDtos.add(getNoPermissionDtoFor(PermissionType.ASSIGN_DATASET_PERMISSION));
        }
        else
        {
            permissionDtos.add(getPermissionDtoFor(PermissionType.ASSIGN_DATASET_PERMISSION, accessLevel));
        }

        if (applicationTypeService.applicationIs(ApplicationType.AGR_ENV))
        {
            permissionDtos.add(getPermissionDtoFor(PermissionType.CREATE_DATABASE_INSTANCE, accessLevel));
            permissionDtos.add(getPermissionDtoFor(PermissionType.VIEW_DATABASE_INSTANCE, accessLevel));
            permissionDtos.add(getPermissionDtoFor(PermissionType.CREATE_DATABASE_SQL, accessLevel));
            permissionDtos.add(getPermissionDtoFor(PermissionType.EDIT_DATABASE_SQL, accessLevel));
            permissionDtos.add(getPermissionDtoFor(PermissionType.VIEW_DATABASE_SQL, accessLevel));
            permissionDtos.add(getPermissionDtoFor(PermissionType.BACKUP_DATABASE_INSTANCE, accessLevel));
        }
        return permissionDtos;
    }

    private PermissionDto getPermissionDtoFor(final PermissionType permissionType, final AccessLevel level)
    {
        return new PermissionDto(permissionType, level.isAtLeast(permissionType.getRequiredLevel()));
    }

    private PermissionDto getNoPermissionDtoFor(final PermissionType permissionType)
    {
        return new PermissionDto(permissionType, false);
    }

    protected AccessLevel getDirectoryLevelFor(Path virtualPath, User user)
    {
        return getLevelForPath(virtualPath, user);
    }

    protected AccessLevel getDatasetLevelFor(ResearchDataset dataset, User user)
    {
        if (dataset.getIsPhysical() && isResearchDataManager(user))
        {
            return AccessLevel.FULL_ACCESS;
        }

        return getLevelForPath(PathBuilder.datasetPath(dataset), user);
    }

    protected AccessLevel getProjectLevelFor(ResearchProject project, User user)
    {
        if (project.getResearchGroup().getIsPhysical() && isResearchDataManager(user))
        {
            return AccessLevel.FULL_ACCESS;
        }

        return getLevelForPath(PathBuilder.projectPath(project), user);
    }

    protected AccessLevel getGroupLevelFor(ResearchGroup group, User user)
    {
        if (group.getIsPhysical() && isResearchDataManager(user))
        {
            return AccessLevel.FULL_ACCESS;
        }

        return getLevelForPath(PathBuilder.groupPath(group), user);
    }

    @Override
    public void updatePermissionEntries(Path source, Path destination)
    {
        String destinationDirectory = getDestinationDirectory(source, destination);
        List<PermissionEntry> entries = PermissionEntry.findPermissionEntrysByPathLike(source.getPath())
                .getResultList();
        for (PermissionEntry entry : entries)
        {
            String destinationFile = getDestinationFile(entry.getPath(), destinationDirectory, source.getPath());
            Path destinationFilePath = PathBuilder.buildFromString(destinationFile);
            User user = entry.getUser();
            AccessLevel destinationAccessLevel = getLevelForPath(destinationFilePath, user);
            // If destination access level is lower, update the permission entry path
            // If equal, or greater, delete the entry
            if (destinationAccessLevel.isAtLeast(entry.getAccessLevel()))
            {
                entry.remove();
            }
            else
            {
                removeOverridenEntries(entry.getAccessLevel(), destinationFile, user);
                entry.setPath(destinationFile);
                entry.merge();
            }
        }
    }

    private void removeOverridenEntries(AccessLevel entryLevel, String path, User user)
    {
        List<PermissionEntry> entries = PermissionEntry.findPermissionEntrysByPathLikeAndUser(path, user)
                .getResultList();
        for (PermissionEntry affectedEntry : entries)
        {
            if (entryLevel.isAtLeast(affectedEntry.getAccessLevel()))
            {
                affectedEntry.remove();
            }
        }
    }

    private String getDestinationFile(String entryPath, String destinationDirectory, String sourcePath)
    {
        String relativePath = entryPath.substring(sourcePath.length());
        return destinationDirectory + relativePath;
    }

    private String getDestinationDirectory(Path source, Path destination)
    {
        String[] tokenizedSource = source.getPath().split("/");
        return destination.getPath() + tokenizedSource[tokenizedSource.length - 1] + "/";
    }

    @Override
    public AccessLevel getLevelForPath(Path virtualPath, User user)
    {
        // if ICT, full access
        if (isIctSupportUser(user))
        {
            return AccessLevel.FULL_ACCESS;
        }


        AccessLevel accessLevel = AccessLevel.NO_ACCESS;
        Map<Path, PermissionEntry> permissionEntries = getEntriesAffectingPath(virtualPath, user);

        if (permissionEntries.size() > 0)
        {
            // At least, viewing access
            accessLevel = AccessLevel.VIEWING_ACCESS;
        }

        for (Path permissionEntryPath : permissionEntries.keySet())
        {
            if (permissionEntryPath.getPath().equals(virtualPath.getPath()))
            {
                // There is an explicit entry in the permission table, so return it's access level
                PermissionEntry entry = permissionEntries.get(permissionEntryPath);
                LOGGER.debug("Explicit access level for path {} is {}", virtualPath.getPath(), entry.getAccessLevel().toString());
                return entry.getAccessLevel();
            }
            // else check if the entry is a higher level and has an affect on our path

            else if (virtualPath.getPath().startsWith(permissionEntryPath.getPath()))
            {
                AccessLevel entryLevel = permissionEntries.get(permissionEntryPath).getAccessLevel();
                // Get the highest access level
                accessLevel = accessLevel.isAtLeast(entryLevel) ? accessLevel : entryLevel;
            }

        }

        // the research data manager can edit groups and projects, and advertise datasets
        if (isResearchDataManager(user))
        {
            if ((virtualPath.isGroupPath() || virtualPath.isProjectPath()) && !accessLevel.isAtLeast(AccessLevel.EDITING_ACCESS))
            {
                accessLevel = AccessLevel.EDITING_ACCESS;
            }
            else if (virtualPath.isDatasetPath())
            {
                accessLevel = AccessLevel.FULL_ACCESS;
            }
        }

        LOGGER.debug("Access level for path {} is {}", virtualPath.getPath(), accessLevel.toString());
        return accessLevel;
    }

    private Map<Path, PermissionEntry> getEntriesAffectingPath(Path path, User user)
    {

        Map<Path, PermissionEntry> permissionEntries = new HashMap<Path, PermissionEntry>();
        Set<PermissionEntry> entries = user.getPermissionEntries();
        for (PermissionEntry permissionEntry : user.getPermissionEntries())
        {
            Path pathFromEntryTable = PathBuilder.buildFromString(permissionEntry.getPath());
            if (pathFromEntryTable.isAffectedBy(path))
            {
                permissionEntries.put(pathFromEntryTable, permissionEntry);
            }
            else
            {
                LOGGER.debug("Different paths: Path(" + pathFromEntryTable.getPath() + ") Path(" 
                        + path.getPath() + ")");
            }
        }
        return permissionEntries;
    }

    private boolean isIctSupportUser(User user)
    {
        return user.hasRole(Role.RoleNames.ROLE_ICT_SUPPORT);
    }

    private boolean isResearchDataManager(User user)
    {
        return user.hasRole(Role.RoleNames.ROLE_RESEARCH_DATA_MANAGER);
    }

    @Override
    public boolean canAction(PermissionType permission, Long id, Principal principal)
    {
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();

        if (PermissionAppliedType.GROUP.equals(permission.getAppliedType()))
        {
            ResearchGroup group = ResearchGroup.findResearchGroup(id);
            return canGroupAction(permission, group, user);
        }
        if (PermissionAppliedType.PROJECT.equals(permission.getAppliedType()))
        {
            ResearchProject project = ResearchProject.findResearchProject(id);
            return canProjectAction(permission, project, user);
        }
        if (PermissionAppliedType.DATASET.equals(permission.getAppliedType()))
        {
            ResearchDataset dataset = ResearchDataset.findResearchDataset(id);
            return canDatasetAction(permission, dataset, user);
        }
        throw new IllegalArgumentException("PermissionType " + permission + " not supported");
    }

    public boolean canDirectoryAction(PermissionType permission, Path path, User user)
    {
        AccessLevel accessLevel = getDirectoryLevelFor(path, user);
        return accessLevel.isAtLeast(permission.getRequiredLevel());
    }

    @Override
    public List<PermissionType> restrictDirectoryActions(List<PermissionType> permissions, String pathString, User user)
    {
        Path path = PathBuilder.buildFromString(pathString);
        AccessLevel access = getDirectoryLevelFor(path, user);

        List<PermissionType> allowedActions;
        if (access == AccessLevel.FULL_ACCESS)
        {
            allowedActions = new ArrayList<PermissionType>(permissions);
        }
        else
        {
            allowedActions = new ArrayList<PermissionType>(0);
            for (PermissionType permission : permissions)
            {
                if (access.isAtLeast(permission.getRequiredLevel()))
                {
                    allowedActions.add(permission);
                }
            }
        }

        return allowedActions;
    }

    @Override
    public boolean canCreateExternalUser(User user)
    {
        return user.hasRole(Role.RoleNames.ROLE_ICT_SUPPORT) || user.isPrincipalInvestigator();
    }

    private boolean canDatasetAction(PermissionType permission, ResearchDataset researchDataset, User user)
    {
        if (permission.equals(PermissionType.ASSIGN_DATASET_PERMISSION) && researchDataset.getIsPhysical())
        {
            return false;
        }

        AccessLevel accessLevel = getDatasetLevelFor(researchDataset, user);
        return accessLevel.isAtLeast(permission.getRequiredLevel());
    }

    private boolean canProjectAction(PermissionType permission, ResearchProject researchProject, User user)
    {
        if (permission.equals(PermissionType.ASSIGN_PROJECT_PERMISSION)
                && researchProject.getResearchGroup().getIsPhysical())
        {
            return false;
        }

        AccessLevel accessLevel = getProjectLevelFor(researchProject, user);
        return accessLevel.isAtLeast(permission.getRequiredLevel());
    }

    private boolean canGroupAction(PermissionType permission, ResearchGroup researchGroup, User user)
    {
        if (permission.equals(PermissionType.ASSIGN_PERMISSION) && researchGroup.getIsPhysical())
        {
            return false;
        }

        AccessLevel accessLevel = getGroupLevelFor(researchGroup, user);
        return accessLevel.isAtLeast(permission.getRequiredLevel());
    }

    private boolean canViewGroup(User user, ResearchGroup researchGroup)
    {
        AccessLevel accessLevel = getGroupLevelFor(researchGroup, user);
        return accessLevel.equals(AccessLevel.VIEWING_ACCESS);
    }

    private boolean canViewProject(User user, ResearchProject researchProject)
    {
        AccessLevel accessLevel = getProjectLevelFor(researchProject, user);
        return accessLevel.equals(AccessLevel.VIEWING_ACCESS);
    }

    private boolean canViewDataset(User user, ResearchDataset researchDataset)
    {
        AccessLevel accessLevel = getDatasetLevelFor(researchDataset, user);
        return accessLevel.equals(AccessLevel.VIEWING_ACCESS);
    }

    @Override
    public boolean canDeletePermission(User currentUser, User userToDelete, Path path)
    {
        boolean isSameUser = currentUser.equals(userToDelete);
        ResearchGroup group = ResearchGroup.findResearchGroup(path.getGroupId());
        boolean isEntryUserPi = userToDelete.equals(group.getPrincipalInvestigator());
        boolean hasFA = getLevelForPath(path, currentUser).equals(AccessLevel.FULL_ACCESS);
        return hasFA && !isEntryUserPi && !isSameUser;
    }

    @Override
    public List<User> parentNodeCheck(String usernames, String accessLevel, Path path)
    {
        List<User> affectedEntries = new ArrayList<User>();
        String[] usernameArray = usernames.split(",");
        for (String userAffected : usernameArray)
        {
            User user = User.findUsersByUsernameEquals(userAffected).getSingleResult();
            AccessLevel assignedLevel = AccessLevel.valueOf(accessLevel);

            List<PermissionEntry> parentEntries = PermissionEntry.findParentPathForUser(path.getPath(), user)
                    .getResultList();

            for (PermissionEntry entry : parentEntries)
            {
                AccessLevel parentAccess = entry.getAccessLevel();
                if (parentAccess.isAtLeast(assignedLevel) && !affectedEntries.contains(user))
                {
                    affectedEntries.add(user);
                }
            }
        }
        return affectedEntries;
    }

    @Override
    public void cleanChildNodes(String usernames, String accessLevel, Path path)
    {
        String[] usernameArray = usernames.split(",");
        for (String userAffected : usernameArray)
        {
            User user = User.findUsersByUsernameEquals(userAffected).getSingleResult();
            AccessLevel assignedLevel = AccessLevel.valueOf(accessLevel);
            List<PermissionEntry> childEntries = PermissionEntry.findChildPathForUser(path.getPath(), user)
                    .getResultList();
            for (PermissionEntry entry : childEntries)
            {
                AccessLevel childAccess = entry.getAccessLevel();
                if (assignedLevel.isAtLeast(childAccess))
                {
                    entry.remove();
                }
            }
        }
    }

    @Override
    public String canViewGroup(Long id, Principal principal, ResearchGroupAction action)
    {
        ResearchGroup researchGroup = ResearchGroup.findResearchGroup(id);
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        return canViewGroup(user, researchGroup) ? action.act(researchGroup, user) : "accessDenied";
    }

    @Override
    public String canViewProject(Long id, Principal principal, ResearchProjectAction action)
    {
        ResearchProject researchProject = ResearchProject.findResearchProject(id);
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        return canViewProject(user, researchProject) ? action.act(researchProject, user) : "accessDenied";
    }

    @Override
    public String canViewDataset(Long id, Principal principal, ResearchDatasetAction action)
    {
        ResearchDataset researchDataset = ResearchDataset.findResearchDataset(id);
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        return canViewDataset(user, researchDataset) ? action.act(researchDataset, user) : "accessDenied";
    }
}
