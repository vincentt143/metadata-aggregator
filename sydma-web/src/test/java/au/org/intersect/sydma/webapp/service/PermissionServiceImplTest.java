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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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
import au.org.intersect.sydma.webapp.service.PermissionService.ResearchDatasetAction;
import au.org.intersect.sydma.webapp.service.PermissionService.ResearchGroupAction;
import au.org.intersect.sydma.webapp.service.PermissionService.ResearchProjectAction;

//TODO CHECKSTYLE-OFF: ClassFanOutComplexityCheck
@RunWith(PowerMockRunner.class)
@PrepareForTest({ResearchGroup.class, User.class, ResearchProject.class, ResearchDataset.class, PermissionEntry.class})
public class PermissionServiceImplTest
{
    private User ictUser;
    private User researcherUser;
    private User researchDataManagerUser;

    private ResearchGroup group;
    private ResearchProject project;
    private ResearchDataset dataset;

    private PermissionServiceImpl permissionServiceImpl;

    @Before
    public void setUp()
    {
        ictUser = mock(User.class);
        Role ictRole = new Role();
        ictRole.setName(Role.RoleNames.ROLE_ICT_SUPPORT.toString());
        Set<Role> ictRoles = new HashSet<Role>();
        ictRoles.add(ictRole);
        when(ictUser.getRoles()).thenReturn(ictRoles);
        when(ictUser.hasRole(Role.RoleNames.ROLE_ICT_SUPPORT)).thenReturn(true);

        researchDataManagerUser = mock(User.class);
        Role rdmRole = new Role();
        rdmRole.setName(Role.RoleNames.ROLE_RESEARCH_DATA_MANAGER.toString());
        Set<Role> rdmRoles = new HashSet<Role>();
        rdmRoles.add(rdmRole);
        when(researchDataManagerUser.getRoles()).thenReturn(rdmRoles);
        when(researchDataManagerUser.hasRole(Role.RoleNames.ROLE_RESEARCH_DATA_MANAGER)).thenReturn(true);

        researcherUser = mock(User.class);
        Set<PermissionEntry> permissionEntries = new HashSet<PermissionEntry>();
        when(researcherUser.getPermissionEntries()).thenReturn(permissionEntries);

        group = new ResearchGroup();
        group.setId(1L);
        group.setIsPhysical(false);

        project = new ResearchProject();
        project.setId(2L);
        project.setResearchGroup(group);

        dataset = new ResearchDataset();
        dataset.setId(3L);
        dataset.setResearchProject(project);
        dataset.setIsPhysical(false);

        permissionServiceImpl = new PermissionServiceImpl();
    }

    @Test
    public void testIctSupportCanCreateExternalUser()
    {
        assertTrue(permissionServiceImpl.canCreateExternalUser(ictUser));
    }

    @Test
    public void testPrincipalInvestigatorCanCreateExternalUser()
    {
        when(researcherUser.isPrincipalInvestigator()).thenReturn(true);
        assertTrue(permissionServiceImpl.canCreateExternalUser(researcherUser));
    }

    @Test
    public void testGetViewGroupPermissionsWhenIct()
    {
        GroupPermissionQuery query = permissionServiceImpl.getViewGroupPermissions(ictUser);
        assertTrue(query instanceof AllGroupsPermissionsQuery);
    }

    @Test
    public void testGetViewGroupPermissionsWhenResearchDataManagerUser()
    {
        ResearchGroup group = mock(ResearchGroup.class);
        when(group.getId()).thenReturn(4L);
        List<ResearchGroup> researchGroupList = new ArrayList<ResearchGroup>();
        researchGroupList.add(group);

        PowerMockito.mockStatic(ResearchGroup.class);
        TypedQuery<ResearchGroup> groupQuery = mock(TypedQuery.class);
        Mockito.when(ResearchGroup.findResearchGroupsByIsPhysical(true)).thenReturn(groupQuery);

        when(groupQuery.getResultList()).thenReturn(researchGroupList);

        GroupPermissionQuery query = permissionServiceImpl.getViewGroupPermissions(researchDataManagerUser);
        assertTrue(query instanceof AllGroupsPermissionsQuery);
    }

    @Test
    public void testGetViewGroupPermissionsWhenResearcherHasNoPermissions()
    {
        GroupPermissionQuery query = permissionServiceImpl.getViewGroupPermissions(researcherUser);
        assertTrue(query instanceof NoGroupsPermissionsQuery);
    }

    @Test
    public void testGetViewGroupPermissionsWhenResearcherHasExplicitPermissions()
    {
        assignPermissionToUser(researcherUser, "/1/", AccessLevel.VIEWING_ACCESS);
        GroupPermissionQuery query = permissionServiceImpl.getViewGroupPermissions(researcherUser);
        assertTrue(query instanceof ConcreteGroupsPermissionsQuery);
        ConcreteGroupsPermissionsQuery concrete = (ConcreteGroupsPermissionsQuery) query;
        assertTrue(concrete.getIds().size() == 1);
        assertTrue(concrete.getIds().contains(1L));
    }

    @Test
    public void testGetViewGroupPermissionsWhenResearcherHasImplicitPermissions()
    {
        assignPermissionToUser(researcherUser, "/1/2/", AccessLevel.VIEWING_ACCESS);
        GroupPermissionQuery query = permissionServiceImpl.getViewGroupPermissions(researcherUser);
        assertTrue(query instanceof ConcreteGroupsPermissionsQuery);
        ConcreteGroupsPermissionsQuery concrete = (ConcreteGroupsPermissionsQuery) query;
        assertTrue(concrete.getIds().size() == 1);
        assertTrue(concrete.getIds().contains(1L));
    }

    @Test
    public void testGetUploadGroupPermissionsWhenResearcherHasPermissions()
    {
        assignPermissionToUser(researcherUser, "/1/", AccessLevel.EDITING_ACCESS);
        GroupPermissionQuery query = permissionServiceImpl.getUploadGroupPermission(researcherUser);
        assertTrue(query instanceof ConcreteGroupsPermissionsQuery);
        ConcreteGroupsPermissionsQuery concrete = (ConcreteGroupsPermissionsQuery) query;
        assertTrue(concrete.getIds().size() == 1);
        assertTrue(concrete.getIds().contains(1L));
    }

    @Test
    public void testGetUploadGroupPermissionsWhenResearcherDoesNotHavePermissions()
    {
        assignPermissionToUser(researcherUser, "/1/", AccessLevel.VIEWING_ACCESS);
        GroupPermissionQuery query = permissionServiceImpl.getUploadGroupPermission(researcherUser);
        assertTrue(query instanceof NoGroupsPermissionsQuery);
    }

    @Test
    public void testGetDownloadGroupPermissionsWhenResearcherHasPermissions()
    {
        assignPermissionToUser(researcherUser, "/1/", AccessLevel.VIEWING_ACCESS);
        GroupPermissionQuery query = permissionServiceImpl.getDownloadGroupPermission(researcherUser);
        assertTrue(query instanceof ConcreteGroupsPermissionsQuery);
        ConcreteGroupsPermissionsQuery concrete = (ConcreteGroupsPermissionsQuery) query;
        assertTrue(concrete.getIds().size() == 1);
        assertTrue(concrete.getIds().contains(1L));
    }

    @Test
    public void testGetDownloadGroupPermissionsWhenResearcherDoesNotHavePermissions()
    {
        GroupPermissionQuery query = permissionServiceImpl.getDownloadGroupPermission(researcherUser);
        assertTrue(query instanceof NoGroupsPermissionsQuery);
    }

    @Test
    public void testGetGroupLevelForAsIct()
    {
        AccessLevel level = permissionServiceImpl.getGroupLevelFor(group, ictUser);
        assertEquals(level, AccessLevel.FULL_ACCESS);
    }

    @Test
    public void testGetGroupLevelForNoAccess()
    {
        AccessLevel level = permissionServiceImpl.getGroupLevelFor(group, researcherUser);
        assertEquals(level, AccessLevel.NO_ACCESS);
    }

    @Test
    public void testGetGroupLevelForExplicitAccess()
    {
        assignPermissionToUser(researcherUser, "/1/", AccessLevel.FULL_ACCESS);
        AccessLevel level = permissionServiceImpl.getGroupLevelFor(group, researcherUser);
        assertEquals(level, AccessLevel.FULL_ACCESS);
    }

    @Test
    public void testGetGroupLevelForImplicitAccess()
    {
        assignPermissionToUser(researcherUser, "/1/2/", AccessLevel.FULL_ACCESS);
        AccessLevel level = permissionServiceImpl.getGroupLevelFor(group, researcherUser);
        assertEquals(level, AccessLevel.VIEWING_ACCESS);
    }

    @Test
    public void testGetViewProjectPermissionsWhenIct()
    {
        Path groupPath = PathBuilder.groupPath(group);
        ProjectPermissionQuery query = permissionServiceImpl.getViewProjectPermissions(ictUser, groupPath);
        assertTrue(query instanceof AllProjectsForGroupPermissionQuery);
    }

    @Test
    public void testGetViewProjectPermissionsWhenResearcherHasNoPermissions()
    {
        Path groupPath = PathBuilder.groupPath(group);
        ProjectPermissionQuery query = permissionServiceImpl.getViewProjectPermissions(researcherUser, groupPath);
        assertTrue(query instanceof NoProjectsPermissionQuery);
    }

    @Test
    public void testGetViewProjectPermissionsWhenResearcherHasExplicitPermissions()
    {
        assignPermissionToUser(researcherUser, "/1/2/", AccessLevel.VIEWING_ACCESS);
        Path groupPath = PathBuilder.groupPath(group);
        ProjectPermissionQuery query = permissionServiceImpl.getViewProjectPermissions(researcherUser, groupPath);
        assertTrue(query instanceof ConcreteProjectsPermissionQuery);
        ConcreteProjectsPermissionQuery concrete = (ConcreteProjectsPermissionQuery) query;
        assertTrue(concrete.getIds().size() == 1);
        assertTrue(concrete.getIds().contains(2L));
    }

    @Test
    public void testGetViewProjectPermissionsWhenResearcherHasImplicitChildPermissions()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.VIEWING_ACCESS);
        Path groupPath = PathBuilder.groupPath(group);
        ProjectPermissionQuery query = permissionServiceImpl.getViewProjectPermissions(researcherUser, groupPath);
        assertTrue(query instanceof ConcreteProjectsPermissionQuery);
        ConcreteProjectsPermissionQuery concrete = (ConcreteProjectsPermissionQuery) query;
        assertTrue(concrete.getIds().size() == 1);
        assertTrue(concrete.getIds().contains(2L));
    }

    @Test
    public void testGetViewProjectPermissionsWhenResearcherHasImplicitParentPermissions()
    {
        assignPermissionToUser(researcherUser, "/1/", AccessLevel.VIEWING_ACCESS);
        Path groupPath = PathBuilder.groupPath(group);
        ProjectPermissionQuery query = permissionServiceImpl.getViewProjectPermissions(researcherUser, groupPath);
        assertTrue(query instanceof AllProjectsForGroupPermissionQuery);
    }

    @Test
    public void testGetUploadProjectPermissionsWhenResearcherHasPermissions()
    {
        Path groupPath = PathBuilder.buildFromString("/1/");
        assignPermissionToUser(researcherUser, "/1/2/", AccessLevel.EDITING_ACCESS);
        ProjectPermissionQuery query = permissionServiceImpl.getUploadProjectPermission(researcherUser, groupPath);
        assertTrue(query instanceof ConcreteProjectsPermissionQuery);
        ConcreteProjectsPermissionQuery concrete = (ConcreteProjectsPermissionQuery) query;
        assertTrue(concrete.getIds().size() == 1);
        assertTrue(concrete.getIds().contains(2L));
    }

    @Test
    public void testGetUploadProjectPermissionsWhenResearcherDoesNotHavePermissions()
    {
        Path groupPath = PathBuilder.buildFromString("/1/");
        assignPermissionToUser(researcherUser, "/1/2/", AccessLevel.VIEWING_ACCESS);
        ProjectPermissionQuery query = permissionServiceImpl.getUploadProjectPermission(researcherUser, groupPath);
        assertTrue(query instanceof NoProjectsPermissionQuery);
    }

    @Test
    public void testGetDownloadProjectPermissionsWhenResearcherHasPermissions()
    {
        Path groupPath = PathBuilder.buildFromString("/1/");
        assignPermissionToUser(researcherUser, "/1/2/", AccessLevel.VIEWING_ACCESS);
        ProjectPermissionQuery query = permissionServiceImpl.getDownloadProjectPermission(researcherUser, groupPath);
        assertTrue(query instanceof ConcreteProjectsPermissionQuery);
        ConcreteProjectsPermissionQuery concrete = (ConcreteProjectsPermissionQuery) query;
        assertTrue(concrete.getIds().size() == 1);
        assertTrue(concrete.getIds().contains(2L));
    }

    @Test
    public void testGetDownloadProjectPermissionsWhenResearcherDoesNotHavePermissions()
    {
        Path groupPath = PathBuilder.buildFromString("/1/");
        ProjectPermissionQuery query = permissionServiceImpl.getDownloadProjectPermission(researcherUser, groupPath);
        assertTrue(query instanceof NoProjectsPermissionQuery);
    }

    @Test
    public void testGetProjectLevelForAsIct()
    {
        AccessLevel level = permissionServiceImpl.getProjectLevelFor(project, ictUser);
        assertEquals(level, AccessLevel.FULL_ACCESS);
    }

    @Test
    public void testGetProjectLevelForNoAccess()
    {
        AccessLevel level = permissionServiceImpl.getProjectLevelFor(project, researcherUser);
        assertEquals(level, AccessLevel.NO_ACCESS);
    }

    @Test
    public void testGetProjectLevelForExplicitAccess()
    {
        assignPermissionToUser(researcherUser, "/1/2/", AccessLevel.FULL_ACCESS);
        AccessLevel level = permissionServiceImpl.getProjectLevelFor(project, researcherUser);
        assertEquals(level, AccessLevel.FULL_ACCESS);
    }

    @Test
    public void testGetProjectLevelForImplicitChildAccess()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.FULL_ACCESS);
        AccessLevel level = permissionServiceImpl.getProjectLevelFor(project, researcherUser);
        assertEquals(level, AccessLevel.VIEWING_ACCESS);
    }

    @Test
    public void testGetProjectLevelForImplicitParentAccess()
    {
        assignPermissionToUser(researcherUser, "/1/", AccessLevel.FULL_ACCESS);
        AccessLevel level = permissionServiceImpl.getProjectLevelFor(project, researcherUser);
        assertEquals(level, AccessLevel.FULL_ACCESS);
    }

    @Test
    public void testGetViewDatasetPermissionsWhenIct()
    {
        Path projectPath = PathBuilder.buildFromString("/1/2/");
        DatasetPermissionQuery query = permissionServiceImpl.getViewDatasetPermissions(ictUser, projectPath);
        assertTrue(query instanceof AllDatasetsForProjectPermissionQuery);
    }

    @Test
    public void testGetViewDatasetPermissionsWhenResearcherHasNoPermissions()
    {
        Path projectPath = PathBuilder.buildFromString("/1/2/");
        DatasetPermissionQuery query = permissionServiceImpl.getViewDatasetPermissions(researcherUser, projectPath);
        assertTrue(query instanceof NoDatasetsPermissionQuery);
    }

    @Test
    public void testGetViewDatasetPermissionsWhenResearcherHasExplicitPermissions()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.VIEWING_ACCESS);
        Path projectPath = PathBuilder.buildFromString("/1/2/");
        DatasetPermissionQuery query = permissionServiceImpl.getViewDatasetPermissions(researcherUser, projectPath);
        assertTrue(query instanceof ConcreteDatasetsPermissionQuery);
        ConcreteDatasetsPermissionQuery concrete = (ConcreteDatasetsPermissionQuery) query;
        assertTrue(concrete.getIds().size() == 1);
        assertTrue(concrete.getIds().contains(3L));
    }

    @Test
    public void testGetViewDatasetPermissionsWhenResearcherHasImplicitChildPermissions()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/folder/", AccessLevel.VIEWING_ACCESS);
        Path projectPath = PathBuilder.buildFromString("/1/2/");
        DatasetPermissionQuery query = permissionServiceImpl.getViewDatasetPermissions(researcherUser, projectPath);
        assertTrue(query instanceof ConcreteDatasetsPermissionQuery);
        ConcreteDatasetsPermissionQuery concrete = (ConcreteDatasetsPermissionQuery) query;
        assertTrue(concrete.getIds().size() == 1);
        assertTrue(concrete.getIds().contains(3L));
    }

    @Test
    public void testGetViewDatasetPermissionsWhenResearcherHasImplicitGroupPermissions()
    {
        assignPermissionToUser(researcherUser, "/1/", AccessLevel.VIEWING_ACCESS);
        Path projectPath = PathBuilder.buildFromString("/1/2/");
        DatasetPermissionQuery query = permissionServiceImpl.getViewDatasetPermissions(researcherUser, projectPath);
        assertTrue(query instanceof AllDatasetsForProjectPermissionQuery);
    }

    @Test
    public void testGetViewDatasetPermissionsWhenResearcherHasImplicitProjectPermissions()
    {
        assignPermissionToUser(researcherUser, "/1/2/", AccessLevel.VIEWING_ACCESS);
        Path projectPath = PathBuilder.buildFromString("/1/2/");
        DatasetPermissionQuery query = permissionServiceImpl.getViewDatasetPermissions(researcherUser, projectPath);
        assertTrue(query instanceof AllDatasetsForProjectPermissionQuery);
    }

    @Test
    public void testGetUploadDatasetPermissionsWhenResearcherHasPermissions()
    {
        Path datasetPath = PathBuilder.buildFromString("/1/2/3/");
        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.EDITING_ACCESS);
        DatasetPermissionQuery query = permissionServiceImpl.getUploadDatasetPermissions(researcherUser, datasetPath);
        assertTrue(query instanceof ConcreteDatasetsPermissionQuery);
        ConcreteDatasetsPermissionQuery concrete = (ConcreteDatasetsPermissionQuery) query;
        assertTrue(concrete.getIds().size() == 1);
        assertTrue(concrete.getIds().contains(3L));
    }

    @Test
    public void testGetUploadDatasetPermissionsWhenResearcherDoesNotHavePermissions()
    {
        Path datasetPath = PathBuilder.buildFromString("/1/2/3/");
        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.VIEWING_ACCESS);
        DatasetPermissionQuery query = permissionServiceImpl.getUploadDatasetPermissions(researcherUser, datasetPath);
        assertTrue(query instanceof NoDatasetsPermissionQuery);
    }

    @Test
    public void testGetDownloadDatasetPermissionsWhenResearcherHasPermissions()
    {
        Path datasetPath = PathBuilder.buildFromString("/1/2/3/");
        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.VIEWING_ACCESS);
        DatasetPermissionQuery query = permissionServiceImpl.getDownloadDatasetPermissions(researcherUser, datasetPath);
        assertTrue(query instanceof ConcreteDatasetsPermissionQuery);
        ConcreteDatasetsPermissionQuery concrete = (ConcreteDatasetsPermissionQuery) query;
        assertTrue(concrete.getIds().size() == 1);
        assertTrue(concrete.getIds().contains(3L));
    }

    @Test
    public void testGetDownloadDatasetPermissionsWhenResearcherDoesNotHavePermissions()
    {
        Path datasetPath = PathBuilder.buildFromString("/1/2/3/");
        DatasetPermissionQuery query = permissionServiceImpl.getDownloadDatasetPermissions(researcherUser, datasetPath);
        assertTrue(query instanceof NoDatasetsPermissionQuery);
    }

    @Test
    public void testGetDatasetLevelForAsIct()
    {
        AccessLevel level = permissionServiceImpl.getDatasetLevelFor(dataset, ictUser);
        assertEquals(level, AccessLevel.FULL_ACCESS);
    }

    @Test
    public void testGetDatasetLevelForNoAccess()
    {
        AccessLevel level = permissionServiceImpl.getDatasetLevelFor(dataset, researcherUser);
        assertEquals(level, AccessLevel.NO_ACCESS);
    }

    @Test
    public void testGetDatasetLevelForExplicitAccess()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.FULL_ACCESS);
        AccessLevel level = permissionServiceImpl.getDatasetLevelFor(dataset, researcherUser);
        assertEquals(level, AccessLevel.FULL_ACCESS);
    }

    @Test
    public void testGetDatasetLevelForImplicitChildAccess()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/file/", AccessLevel.FULL_ACCESS);
        AccessLevel level = permissionServiceImpl.getDatasetLevelFor(dataset, researcherUser);
        assertEquals(level, AccessLevel.VIEWING_ACCESS);
    }

    @Test
    public void testGetDatasetLevelForImplicitGroupAccess()
    {
        assignPermissionToUser(researcherUser, "/1/", AccessLevel.FULL_ACCESS);
        AccessLevel level = permissionServiceImpl.getDatasetLevelFor(dataset, researcherUser);
        assertEquals(level, AccessLevel.FULL_ACCESS);
    }

    @Test
    public void testGetDatasetLevelForImplicitProjectAccess()
    {
        assignPermissionToUser(researcherUser, "/1/2/", AccessLevel.FULL_ACCESS);
        AccessLevel level = permissionServiceImpl.getDatasetLevelFor(dataset, researcherUser);
        assertEquals(level, AccessLevel.FULL_ACCESS);
    }

    @Test
    public void testGetDatasetLevelForImplicitAccessOverriden()
    {
        assignPermissionToUser(researcherUser, "/1/", AccessLevel.VIEWING_ACCESS);
        assignPermissionToUser(researcherUser, "/1/2/", AccessLevel.FULL_ACCESS);
        AccessLevel level = permissionServiceImpl.getDatasetLevelFor(dataset, researcherUser);
        assertEquals(level, AccessLevel.FULL_ACCESS);
    }

    @Test
    public void testGetDatasetLevelForImplicitAccessOverridenReverse()
    {
        assignPermissionToUser(researcherUser, "/1/2/", AccessLevel.FULL_ACCESS);
        assignPermissionToUser(researcherUser, "/1/", AccessLevel.VIEWING_ACCESS);
        AccessLevel level = permissionServiceImpl.getDatasetLevelFor(dataset, researcherUser);
        assertEquals(level, AccessLevel.FULL_ACCESS);
    }

    @Test
    public void testGetResearchGroupPermissionsWhenViewer()
    {
        assignPermissionToUser(researcherUser, "/1/", AccessLevel.VIEWING_ACCESS);
        List<PermissionDto> permissionDtos = permissionServiceImpl.getResearchGroupPermissions(group, researcherUser);
        assertDtoContains(permissionDtos, PermissionType.EDIT_GROUP, false);
        assertDtoContains(permissionDtos, PermissionType.CREATE_PROJECT, false);
        assertDtoContains(permissionDtos, PermissionType.ASSIGN_PERMISSION, false);
        assertDtoContains(permissionDtos, PermissionType.VIEW_PERMISSION, false);
    }

    private void assertDtoContains(List<PermissionDto> permissionDtos, PermissionType type, boolean value)
    {
        PermissionDto permission = new PermissionDto(type, value);
        assertTrue(permissionDtos.contains(permission));
    }

    @Test
    public void testGetResearchGroupPermissionsWhenEditor()
    {
        assignPermissionToUser(researcherUser, "/1/", AccessLevel.EDITING_ACCESS);
        List<PermissionDto> permissionDtos = permissionServiceImpl.getResearchGroupPermissions(group, researcherUser);
        assertDtoContains(permissionDtos, PermissionType.EDIT_GROUP, true);
        assertDtoContains(permissionDtos, PermissionType.CREATE_PROJECT, false);
        assertDtoContains(permissionDtos, PermissionType.ASSIGN_PERMISSION, true);
        assertDtoContains(permissionDtos, PermissionType.VIEW_PERMISSION, true);
    }

    @Test
    public void testGetResearchGroupPermissionsWhenFull()
    {
        assignPermissionToUser(researcherUser, "/1/", AccessLevel.FULL_ACCESS);
        List<PermissionDto> permissionDtos = permissionServiceImpl.getResearchGroupPermissions(group, researcherUser);
        assertDtoContains(permissionDtos, PermissionType.EDIT_GROUP, true);
        assertDtoContains(permissionDtos, PermissionType.CREATE_PROJECT, true);
        assertDtoContains(permissionDtos, PermissionType.ASSIGN_PERMISSION, true);
        assertDtoContains(permissionDtos, PermissionType.VIEW_PERMISSION, true);
    }

    @Test
    public void testGetResearchGroupPermissionsCanViewPermissionIfEditorAtProjectLevel()
    {
        assignPermissionToUser(researcherUser, "/1/2/", AccessLevel.EDITING_ACCESS);
        List<PermissionDto> permissionDtos = permissionServiceImpl.getResearchGroupPermissions(group, researcherUser);
        assertDtoContains(permissionDtos, PermissionType.VIEW_PERMISSION, true);
    }

    @Test
    public void testGetResearchGroupPermissionsCannotViewPermissionIfViewerAtProjectLevel()
    {
        assignPermissionToUser(researcherUser, "/1/2/", AccessLevel.VIEWING_ACCESS);
        List<PermissionDto> permissionDtos = permissionServiceImpl.getResearchGroupPermissions(group, researcherUser);
        assertDtoContains(permissionDtos, PermissionType.VIEW_PERMISSION, false);
    }

    @Test
    public void testGetResearchProjectPermissionsWhenViewer()
    {
        assignPermissionToUser(researcherUser, "/1/2/", AccessLevel.VIEWING_ACCESS);
        List<PermissionDto> permissionDtos = permissionServiceImpl.getResearchProjectPermissions(project,
                researcherUser);
        assertDtoContains(permissionDtos, PermissionType.EDIT_PROJECT, false);
        assertDtoContains(permissionDtos, PermissionType.CREATE_DATASET, false);
        assertDtoContains(permissionDtos, PermissionType.ASSIGN_PROJECT_PERMISSION, false);
    }

    @Test
    public void testGetResearchProjectPermissionsWhenEditor()
    {
        assignPermissionToUser(researcherUser, "/1/2/", AccessLevel.EDITING_ACCESS);
        List<PermissionDto> permissionDtos = permissionServiceImpl.getResearchProjectPermissions(project,
                researcherUser);
        assertDtoContains(permissionDtos, PermissionType.EDIT_PROJECT, true);
        assertDtoContains(permissionDtos, PermissionType.CREATE_DATASET, false);
        assertDtoContains(permissionDtos, PermissionType.ASSIGN_PROJECT_PERMISSION, true);
    }

    @Test
    public void testGetResearchProjectPermissionsWhenFull()
    {
        assignPermissionToUser(researcherUser, "/1/2/", AccessLevel.FULL_ACCESS);
        List<PermissionDto> permissionDtos = permissionServiceImpl.getResearchProjectPermissions(project,
                researcherUser);
        assertDtoContains(permissionDtos, PermissionType.EDIT_PROJECT, true);
        assertDtoContains(permissionDtos, PermissionType.CREATE_DATASET, true);
        assertDtoContains(permissionDtos, PermissionType.ASSIGN_PROJECT_PERMISSION, true);
    }

    @Test
    public void testGetResearchDatasetPermissionsWhenViewer()
    {
        ApplicationTypeService applicationTypeService = mock(ApplicationTypeService.class);
        when(applicationTypeService.applicationIs(ApplicationType.AGR_ENV)).thenReturn(true);
        permissionServiceImpl.setApplicationTypeService(applicationTypeService);

        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.VIEWING_ACCESS);
        List<PermissionDto> permissionDtos = permissionServiceImpl.getResearchDatasetPermissions(dataset,
                researcherUser);
        assertDtoContains(permissionDtos, PermissionType.EDIT_DATASET, false);
        assertDtoContains(permissionDtos, PermissionType.PUBLISH_DATASET, false);
        assertDtoContains(permissionDtos, PermissionType.REJECT_ADVERTISING_DATASET, false);
        assertDtoContains(permissionDtos, PermissionType.CREATE_DATABASE_INSTANCE, false);
        assertDtoContains(permissionDtos, PermissionType.VIEW_DATABASE_INSTANCE, true);
        assertDtoContains(permissionDtos, PermissionType.ASSIGN_DATASET_PERMISSION, false);
        assertDtoContains(permissionDtos, PermissionType.CREATE_DATABASE_SQL, false);
        assertDtoContains(permissionDtos, PermissionType.EDIT_DATABASE_SQL, false);
        assertDtoContains(permissionDtos, PermissionType.VIEW_DATABASE_SQL, true);
    }

    @Test
    public void testGetResearchDatasetPermissionsWhenEdit()
    {
        ApplicationTypeService applicationTypeService = mock(ApplicationTypeService.class);
        when(applicationTypeService.applicationIs(ApplicationType.AGR_ENV)).thenReturn(true);
        permissionServiceImpl.setApplicationTypeService(applicationTypeService);

        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.EDITING_ACCESS);
        List<PermissionDto> permissionDtos = permissionServiceImpl.getResearchDatasetPermissions(dataset,
                researcherUser);
        assertDtoContains(permissionDtos, PermissionType.EDIT_DATASET, true);
        assertDtoContains(permissionDtos, PermissionType.PUBLISH_DATASET, false);
        assertDtoContains(permissionDtos, PermissionType.REJECT_ADVERTISING_DATASET, false);
        assertDtoContains(permissionDtos, PermissionType.CREATE_DATABASE_INSTANCE, false);
        assertDtoContains(permissionDtos, PermissionType.VIEW_DATABASE_INSTANCE, true);
        assertDtoContains(permissionDtos, PermissionType.ASSIGN_DATASET_PERMISSION, true);
        assertDtoContains(permissionDtos, PermissionType.CREATE_DATABASE_SQL, false);
        assertDtoContains(permissionDtos, PermissionType.EDIT_DATABASE_SQL, true);
        assertDtoContains(permissionDtos, PermissionType.VIEW_DATABASE_SQL, true);
    }

    @Test
    public void testGetResearchDatasetPermissionsWhenFull()
    {
        ApplicationTypeService applicationTypeService = mock(ApplicationTypeService.class);
        when(applicationTypeService.applicationIs(ApplicationType.AGR_ENV)).thenReturn(true);
        permissionServiceImpl.setApplicationTypeService(applicationTypeService);

        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.FULL_ACCESS);
        List<PermissionDto> permissionDtos = permissionServiceImpl.getResearchDatasetPermissions(dataset,
                researcherUser);
        assertDtoContains(permissionDtos, PermissionType.EDIT_DATASET, true);
        assertDtoContains(permissionDtos, PermissionType.PUBLISH_DATASET, true);
        assertDtoContains(permissionDtos, PermissionType.REJECT_ADVERTISING_DATASET, true);
        assertDtoContains(permissionDtos, PermissionType.CREATE_DATABASE_INSTANCE, true);
        assertDtoContains(permissionDtos, PermissionType.VIEW_DATABASE_INSTANCE, true);
        assertDtoContains(permissionDtos, PermissionType.ASSIGN_DATASET_PERMISSION, true);
        assertDtoContains(permissionDtos, PermissionType.CREATE_DATABASE_SQL, true);
        assertDtoContains(permissionDtos, PermissionType.EDIT_DATABASE_SQL, true);
        assertDtoContains(permissionDtos, PermissionType.VIEW_DATABASE_SQL, true);
    }

    @Test
    public void testCanGroupEditGroupSuccess()
    {
        assignPermissionToUser(researcherUser, "/1/", AccessLevel.EDITING_ACCESS);
        assertEquals("success", setupGroupAndCheckPermission(PermissionType.EDIT_GROUP));
    }

    @Test
    public void testCanGroupEditGroupFailure()
    {
        assignPermissionToUser(researcherUser, "/1/", AccessLevel.VIEWING_ACCESS);
        assertEquals("accessDenied", setupGroupAndCheckPermission(PermissionType.EDIT_GROUP));
    }

    @Test
    public void testCanGroupCreateProjectSuccess()
    {
        assignPermissionToUser(researcherUser, "/1/", AccessLevel.FULL_ACCESS);
        assertEquals("success", setupGroupAndCheckPermission(PermissionType.CREATE_PROJECT));
    }

    @Test
    public void testCanGroupCreateProjectFailure()
    {
        assignPermissionToUser(researcherUser, "/1/", AccessLevel.EDITING_ACCESS);
        assertEquals("accessDenied", setupGroupAndCheckPermission(PermissionType.CREATE_PROJECT));
    }

    @Test
    public void testCanGroupAssignPermissionSuccess()
    {
        assignPermissionToUser(researcherUser, "/1/", AccessLevel.EDITING_ACCESS);
        assertEquals("success", setupGroupAndCheckPermission(PermissionType.ASSIGN_PERMISSION));
    }

    @Test
    public void testCanGroupAssignPermissionFailure()
    {
        assignPermissionToUser(researcherUser, "/1/", AccessLevel.VIEWING_ACCESS);
        assertEquals("accessDenied", setupGroupAndCheckPermission(PermissionType.ASSIGN_PERMISSION));
    }

    @Test
    public void testCanGroupViewGroupSuccess()
    {
        assignPermissionToUser(researcherUser, "/1/", AccessLevel.EDITING_ACCESS);
        assertEquals("success", setupGroupAndCheckPermission(PermissionType.VIEW_PERMISSION));
    }

    @Test
    public void testCanGroupViewGroupFailure()
    {
        assignPermissionToUser(researcherUser, "/1/", AccessLevel.VIEWING_ACCESS);
        assertEquals("accessDenied", setupGroupAndCheckPermission(PermissionType.VIEW_PERMISSION));
    }

    @Test
    public void testCanProjectEditProjectSuccess()
    {
        assignPermissionToUser(researcherUser, "/1/2/", AccessLevel.EDITING_ACCESS);
        assertEquals("success", setupProjectAndCheckPermission(PermissionType.EDIT_PROJECT));
    }

    @Test
    public void testCanProjectEditProjectFailure()
    {
        assignPermissionToUser(researcherUser, "/1/2/", AccessLevel.VIEWING_ACCESS);
        assertEquals("accessDenied", setupProjectAndCheckPermission(PermissionType.EDIT_PROJECT));
    }

    @Test
    public void testCanProjectCreateDatasetSuccess()
    {
        assignPermissionToUser(researcherUser, "/1/2/", AccessLevel.FULL_ACCESS);
        assertEquals("success", setupProjectAndCheckPermission(PermissionType.CREATE_DATASET));
    }

    @Test
    public void testCanProjectCreateDatasetFailure()
    {
        assignPermissionToUser(researcherUser, "/1/2/", AccessLevel.EDITING_ACCESS);
        assertEquals("accessDenied", setupProjectAndCheckPermission(PermissionType.CREATE_DATASET));
    }

    @Test
    public void testCanProjectAssignProjectPermissionSuccess()
    {
        assignPermissionToUser(researcherUser, "/1/2/", AccessLevel.EDITING_ACCESS);
        assertEquals("success", setupProjectAndCheckPermission(PermissionType.ASSIGN_PROJECT_PERMISSION));
    }

    @Test
    public void testCanProjectAssignProjectPermissionFailure()
    {
        assignPermissionToUser(researcherUser, "/1/2/", AccessLevel.VIEWING_ACCESS);
        assertEquals("accessDenied", setupProjectAndCheckPermission(PermissionType.ASSIGN_PROJECT_PERMISSION));
    }

    @Test
    public void testCanDatasetEditDatasetSuccess()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.EDITING_ACCESS);
        assertEquals("success", setupDatasetAndCheckPermission(PermissionType.EDIT_DATASET));
    }

    @Test
    public void testCanDatasetEditDatasetFailure()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.VIEWING_ACCESS);
        assertEquals("accessDenied", setupDatasetAndCheckPermission(PermissionType.EDIT_DATASET));
    }

    @Test
    public void testCanDatasetAssignPermissionSuccess()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.EDITING_ACCESS);
        assertEquals("success", setupDatasetAndCheckPermission(PermissionType.ASSIGN_DATASET_PERMISSION));
    }

    @Test
    public void testCanDatasetAssignPermissionFailure()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.VIEWING_ACCESS);
        assertEquals("accessDenied", setupDatasetAndCheckPermission(PermissionType.ASSIGN_DATASET_PERMISSION));
    }

    @Test
    public void testCanDatasetCreateDatabaseSuccess()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.FULL_ACCESS);
        assertEquals("success", setupDatasetAndCheckPermission(PermissionType.CREATE_DATABASE_INSTANCE));
    }

    @Test
    public void testCanDatasetCreateDatabaseFailure()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.EDITING_ACCESS);
        assertEquals("accessDenied", setupDatasetAndCheckPermission(PermissionType.CREATE_DATABASE_INSTANCE));
    }

    @Test
    public void testCanDatasetViewDatabaseSuccess()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.VIEWING_ACCESS);
        assertEquals("success", setupDatasetAndCheckPermission(PermissionType.VIEW_DATABASE_INSTANCE));
    }

    @Test
    public void testCanDatasetViewDatabaseFailure()
    {
        assertEquals("accessDenied", setupDatasetAndCheckPermission(PermissionType.VIEW_DATABASE_INSTANCE));
    }

    @Test
    public void testCanDatasetCreateDatabaseSqlSuccess()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.FULL_ACCESS);
        assertEquals("success", setupDatasetAndCheckPermission(PermissionType.CREATE_DATABASE_SQL));
    }

    @Test
    public void testCanDatasetCreateDatabaseSqlFailure()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.EDITING_ACCESS);
        assertEquals("accessDenied", setupDatasetAndCheckPermission(PermissionType.CREATE_DATABASE_SQL));
    }

    @Test
    public void testCanDatasetEditDatabaseSqlSuccess()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.EDITING_ACCESS);
        assertEquals("success", setupDatasetAndCheckPermission(PermissionType.EDIT_DATABASE_SQL));
    }

    @Test
    public void testCanDatasetEditDatabaseSqlFailure()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.VIEWING_ACCESS);
        assertEquals("accessDenied", setupDatasetAndCheckPermission(PermissionType.EDIT_DATABASE_SQL));
    }

    @Test
    public void testCanDatasetViewDatabaseSqlSuccess()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.VIEWING_ACCESS);
        assertEquals("success", setupDatasetAndCheckPermission(PermissionType.VIEW_DATABASE_SQL));
    }

    @Test
    public void testCanDatasetViewDatabaseSqlFailure()
    {
        assertEquals("accessDenied", setupDatasetAndCheckPermission(PermissionType.VIEW_DATABASE_SQL));
    }

    @Test
    public void testGetDirectoryLevelWhenDirectoryFullAndDatasetViewing()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.VIEWING_ACCESS);
        assignPermissionToUser(researcherUser, "/1/2/3/Directory/", AccessLevel.FULL_ACCESS);
        AccessLevel level = permissionServiceImpl.getDirectoryLevelFor(
                PathBuilder.buildFromString("/1/2/3/Directory/"), researcherUser);
        assertEquals(AccessLevel.FULL_ACCESS, level);
    }

    @Test
    public void testGetDirectoryLevelWhenOneSubDirectoryFullAndAnotherSubDirectoryViewing()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/Directory/Child1", AccessLevel.FULL_ACCESS);
        assignPermissionToUser(researcherUser, "/1/2/3/Directory/Child2", AccessLevel.VIEWING_ACCESS);
        AccessLevel level = permissionServiceImpl.getDirectoryLevelFor(
                PathBuilder.buildFromString("/1/2/3/Directory/"), researcherUser);
        AccessLevel firstChildLevel = permissionServiceImpl.getDirectoryLevelFor(
                PathBuilder.buildFromString("/1/2/3/Directory/Child1/"), researcherUser);
        AccessLevel secondChildLevel = permissionServiceImpl.getDirectoryLevelFor(
                PathBuilder.buildFromString("/1/2/3/Directory/Child2/"), researcherUser);

        assertEquals(AccessLevel.VIEWING_ACCESS, level);
        assertEquals(AccessLevel.FULL_ACCESS, firstChildLevel);
        assertEquals(AccessLevel.VIEWING_ACCESS, secondChildLevel);
    }

    @Test
    public void testSuccessfulUserCanDeletePermission()
    {
        User principalInvestigator = new User();

        ResearchGroup group = mock(ResearchGroup.class);
        when(group.getPrincipalInvestigator()).thenReturn(principalInvestigator);

        assertTrue(setupPermissionsAndCheckDelete(AccessLevel.FULL_ACCESS, researcherUser, principalInvestigator));
    }

    @Test
    public void testUserCannotDeleteOwnPermission()
    {
        assertFalse(setupPermissionsAndCheckDelete(AccessLevel.FULL_ACCESS, researcherUser, researcherUser));
    }

    @Test
    public void testUserCannotDeletePermissionIfEditingAccess()
    {
        User principalInvestigator = new User();

        ResearchGroup group = mock(ResearchGroup.class);
        when(group.getPrincipalInvestigator()).thenReturn(principalInvestigator);

        assertFalse(setupPermissionsAndCheckDelete(AccessLevel.EDITING_ACCESS, researcherUser, principalInvestigator));
    }

    @Test
    public void testUserCannotDeletePrincipalInvestigatorPermission()
    {
        User principalInvestigator = new User();

        ResearchGroup group = mock(ResearchGroup.class);
        when(group.getPrincipalInvestigator()).thenReturn(principalInvestigator);

        assertFalse(setupPermissionsAndCheckDelete(AccessLevel.FULL_ACCESS, principalInvestigator,
                principalInvestigator));
    }
    
    @Test
    public void testPermissionParentNodeCheck()
    {
        PowerMockito.mockStatic(User.class);
        Path path = PathBuilder.buildFromString("/1/1/1/");
        
        User user = mock(User.class);
        
        List<PermissionEntry> permissionList = new ArrayList<PermissionEntry>();

        PermissionEntry groupEntry = mock(PermissionEntry.class);
        when(groupEntry.getAccessLevel()).thenReturn(AccessLevel.EDITING_ACCESS);
        permissionList.add(groupEntry);
        
        PermissionEntry projectEntry = mock(PermissionEntry.class);
        when(projectEntry.getAccessLevel()).thenReturn(AccessLevel.FULL_ACCESS);
        permissionList.add(projectEntry);
                
        TypedQuery<User> userQuery = mock(TypedQuery.class);
        when(userQuery.getSingleResult()).thenReturn(user);
        Mockito.when(User.findUsersByUsernameEquals("user")).thenReturn(userQuery);

        PowerMockito.mockStatic(PermissionEntry.class);
        TypedQuery<PermissionEntry> permissionQuery = mock(TypedQuery.class);
        when(permissionQuery.getResultList()).thenReturn(permissionList);
        Mockito.when(PermissionEntry.findParentPathForUser(path.getPath(), user)).thenReturn(permissionQuery);
        
        List<User> affectedUsers = permissionServiceImpl.parentNodeCheck("user", "VIEWING_ACCESS", path);
        assertTrue(affectedUsers.contains(user));
    }
    
    @Test
    public void testPermissionChildNodeCheck()
    {
        Path path = PathBuilder.buildFromString("/1/");
        
        List<PermissionEntry> permissionList = new ArrayList<PermissionEntry>();

        PermissionEntry projectEntry = mock(PermissionEntry.class);
        when(projectEntry.getAccessLevel()).thenReturn(AccessLevel.VIEWING_ACCESS);
        permissionList.add(projectEntry);
        
        PermissionEntry datasetEntry = mock(PermissionEntry.class);
        when(datasetEntry.getAccessLevel()).thenReturn(AccessLevel.EDITING_ACCESS);
        permissionList.add(datasetEntry);
        
        PowerMockito.mockStatic(User.class);
        when(researcherUser.getUsername()).thenReturn("researcherUser");

        TypedQuery<User> userQuery = mock(TypedQuery.class);
        when(userQuery.getSingleResult()).thenReturn(researcherUser);
        Mockito.when(User.findUsersByUsernameEquals("researcherUser")).thenReturn(userQuery);

        TypedQuery<PermissionEntry> permissionQuery = mock(TypedQuery.class);
        when(permissionQuery.getResultList()).thenReturn(permissionList);
        PowerMockito.mockStatic(PermissionEntry.class);
        Mockito.when(PermissionEntry.findChildPathForUser(path.getPath(), researcherUser)).thenReturn(permissionQuery);

        permissionServiceImpl.cleanChildNodes(researcherUser.getUsername(), "FULL_ACCESS", path);
        Mockito.verify(projectEntry).remove();
        Mockito.verify(datasetEntry).remove();
    }
    
    @Test
    public void testPermissionMoveWithoutPermissionInDestination()
    {
        Path sourceDirectory = PathBuilder.buildFromString("/1/2/3/src/");
        Path destinationDirectory = PathBuilder.buildFromString("/1/2/3/dest/");
        
        List<PermissionEntry> permissionList = new ArrayList<PermissionEntry>();
        PermissionEntry permissionEntry = mock(PermissionEntry.class);
        permissionEntry = setupPermissionEntryToList(AccessLevel.VIEWING_ACCESS, "/1/2/3/src/file/", permissionList);

        PowerMockito.mockStatic(PermissionEntry.class);
        
        setupMockFindPermissionEntryByPathLike(permissionList, sourceDirectory.getPath());
        setupMockFindPermissionEntryByPathLikeAndUser(new ArrayList<PermissionEntry>(), "/1/2/3/dest/src/file/");

        permissionServiceImpl.updatePermissionEntries(sourceDirectory, destinationDirectory);
        Mockito.verify(permissionEntry).setPath("/1/2/3/dest/src/file/");
        Mockito.verify(permissionEntry).merge();
    }

    @Test
    public void testPermissionMoveWhenAlreadyPermissionInDestination()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.FULL_ACCESS);
        
        Set<PermissionEntry> entries = researcherUser.getPermissionEntries();
        
        Path sourceDirectory = PathBuilder.buildFromString("/1/2/3/src/");
        Path destinationDirectory = PathBuilder.buildFromString("/1/2/3/dest/");
        
        List<PermissionEntry> permissionList = new ArrayList<PermissionEntry>();

        PermissionEntry permissionEntry = mock(PermissionEntry.class);
        permissionEntry = setupPermissionEntryToList(AccessLevel.VIEWING_ACCESS, "/1/2/3/src/file/", permissionList);

        PowerMockito.mockStatic(PermissionEntry.class);
        setupMockFindPermissionEntryByPathLike(permissionList, sourceDirectory.getPath());

        permissionServiceImpl.updatePermissionEntries(sourceDirectory, destinationDirectory);
        Mockito.verify(permissionEntry).remove();
    }

    @Test
    public void testPermissionMoveWhenDestinationHasLowerPermission()
    {
        Path sourceDirectory = PathBuilder.buildFromString("/1/2/3/dir2/");
        Path destinationDirectory = PathBuilder.buildFromString("/1/2/3/dir1/"); 

        PowerMockito.mockStatic(PermissionEntry.class);
        
        List<PermissionEntry> srcPermissionList = new ArrayList<PermissionEntry>();
        PermissionEntry srcPermissionEntry = mock(PermissionEntry.class);
        srcPermissionEntry = setupPermissionEntryToList(AccessLevel.FULL_ACCESS, "/1/2/3/dir2/", srcPermissionList);
        setupMockFindPermissionEntryByPathLike(srcPermissionList, sourceDirectory.getPath());

        List<PermissionEntry> destPermissionList = new ArrayList<PermissionEntry>();
        PermissionEntry destPermissionEntry = mock(PermissionEntry.class);
        destPermissionEntry = setupPermissionEntryToList(AccessLevel.VIEWING_ACCESS, "/1/2/3/dir1/dir2/dir3/",
                destPermissionList);
        setupMockFindPermissionEntryByPathLikeAndUser(destPermissionList, "/1/2/3/dir1/dir2/");

        permissionServiceImpl.updatePermissionEntries(sourceDirectory, destinationDirectory);
        Mockito.verify(srcPermissionEntry).setPath("/1/2/3/dir1/dir2/");
        Mockito.verify(srcPermissionEntry).merge();
        Mockito.verify(destPermissionEntry).remove();
    }

    private PermissionEntry setupPermissionEntryToList(AccessLevel accessLevel, String returnPath,
            List<PermissionEntry> permissionList)
    {
        PermissionEntry permissionEntry = mock(PermissionEntry.class);
        when(permissionEntry.getAccessLevel()).thenReturn(accessLevel);
        when(permissionEntry.getPath()).thenReturn(returnPath);
        when(permissionEntry.getUser()).thenReturn(researcherUser);
        permissionList.add(permissionEntry);
        return permissionEntry;
    }

    private void setupMockFindPermissionEntryByPathLikeAndUser(List<PermissionEntry> returnedEntries,
            String returnedPath)
    {
        TypedQuery<PermissionEntry> permissionQuery = mock(TypedQuery.class);
        when(permissionQuery.getResultList()).thenReturn(returnedEntries);
        Mockito.when(PermissionEntry.findPermissionEntrysByPathLikeAndUser(returnedPath, researcherUser)).thenReturn(
                permissionQuery);

    }

    private void setupMockFindPermissionEntryByPathLike(List<PermissionEntry> returnedEntries, String returnedPath)
    {
        TypedQuery<PermissionEntry> permissionQuery = mock(TypedQuery.class);
        when(permissionQuery.getResultList()).thenReturn(returnedEntries);
        Mockito.when(PermissionEntry.findPermissionEntrysByPathLike(returnedPath)).thenReturn(permissionQuery);
    }
    
    private boolean setupPermissionsAndCheckDelete(AccessLevel accessLevel, User currentUser, User userToDelete)
    {
        Long groupId = 1L;

        PowerMockito.mockStatic(ResearchGroup.class);
        Mockito.when(ResearchGroup.findResearchGroup(groupId)).thenReturn(group);

        assignPermissionToUser(researcherUser, "/1/", accessLevel);
        Path path = PathBuilder.buildFromString("/1/");

        boolean canDelete = permissionServiceImpl.canDeletePermission(currentUser, userToDelete, path);
        return canDelete;
    }

    private String setupDatasetAndCheckPermission(PermissionType type)
    {
        ResearchGroup group = mock(ResearchGroup.class);
        when(group.getId()).thenReturn(1L);

        ResearchProject project = mock(ResearchProject.class);
        when(project.getId()).thenReturn(2L);
        when(project.getResearchGroup()).thenReturn(group);

        ResearchDataset dataset = mock(ResearchDataset.class);
        when(dataset.getId()).thenReturn(3L);
        when(dataset.getResearchProject()).thenReturn(project);

        PowerMockito.mockStatic(ResearchDataset.class);
        Mockito.when(ResearchDataset.findResearchDataset(3L)).thenReturn(dataset);

        PowerMockito.mockStatic(User.class);
        TypedQuery<User> userQuery = mock(TypedQuery.class);
        when(userQuery.getSingleResult()).thenReturn(researcherUser);
        Mockito.when(User.findUsersByUsernameEquals("principal")).thenReturn(userQuery);
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("principal");

        ResearchDatasetAction action = new ResearchDatasetAction()
        {

            @Override
            public String act(ResearchDataset dataset, User user)
            {
                return "success";
            }
        };

        String value = permissionServiceImpl.canDataset(type, 3L, principal, action);
        return value;
    }

    private String setupProjectAndCheckPermission(PermissionType type)
    {
        ResearchGroup group = mock(ResearchGroup.class);
        when(group.getId()).thenReturn(1L);

        ResearchProject project = mock(ResearchProject.class);
        when(project.getId()).thenReturn(2L);
        when(project.getResearchGroup()).thenReturn(group);

        PowerMockito.mockStatic(ResearchProject.class);
        Mockito.when(ResearchProject.findResearchProject(2L)).thenReturn(project);

        PowerMockito.mockStatic(User.class);
        TypedQuery<User> userQuery = mock(TypedQuery.class);
        when(userQuery.getSingleResult()).thenReturn(researcherUser);
        Mockito.when(User.findUsersByUsernameEquals("principal")).thenReturn(userQuery);
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("principal");

        ResearchProjectAction action = new ResearchProjectAction()
        {

            @Override
            public String act(ResearchProject project, User user)
            {
                return "success";
            }
        };

        String value = permissionServiceImpl.canProject(type, 2L, principal, action);
        return value;
    }

    private String setupGroupAndCheckPermission(PermissionType permissionType)
    {
        Long groupId = 1L;

        ResearchGroup group = mock(ResearchGroup.class);
        when(group.getId()).thenReturn(groupId);

        PowerMockito.mockStatic(ResearchGroup.class);
        Mockito.when(ResearchGroup.findResearchGroup(groupId)).thenReturn(group);

        PowerMockito.mockStatic(User.class);
        TypedQuery<User> userQuery = mock(TypedQuery.class);
        when(userQuery.getSingleResult()).thenReturn(researcherUser);
        Mockito.when(User.findUsersByUsernameEquals("principal")).thenReturn(userQuery);
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("principal");

        ResearchGroupAction action = new ResearchGroupAction()
        {
            @Override
            public String act(ResearchGroup researchGroup, User user)
            {
                return "success";
            }

        };

        String value = permissionServiceImpl.canGroup(permissionType, groupId, principal, action);
        return value;
    }

    private PermissionEntry assignPermissionToUser(User user, String path, AccessLevel level)
    {
        PermissionEntry entry = mock(PermissionEntry.class);
        when(entry.getPath()).thenReturn(path);
        when(entry.getAccessLevel()).thenReturn(level);
        when(entry.getUser()).thenReturn(user);
        user.getPermissionEntries().add(entry);
        return entry;
    }

}
