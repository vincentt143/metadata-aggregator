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
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

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
import au.org.intersect.sydma.webapp.permission.project.AllProjectsForGroupPermissionQuery;
import au.org.intersect.sydma.webapp.permission.project.ConcreteProjectsPermissionQuery;
import au.org.intersect.sydma.webapp.permission.project.NoProjectsPermissionQuery;
import au.org.intersect.sydma.webapp.permission.project.ProjectPermissionQuery;

//TODO CHECKSTYLE-OFF: MultipleStringLiteralsCheck
//TODO CHECKSTYLE-OFF: ClassFanOutComplexityCheck
public class PermissionServiceImplTest
{

    private User ictUser;
    private User researcherUser;

    private ResearchGroup group;

    private ResearchProject project;

    private ResearchDataset dataset;

    private PermissionServiceImpl permissionServiceImpl;

    @Before
    public void setUp()
    {
        ictUser = new User();
        Role ictRole = new Role();
        ictRole.setName(Role.RoleNames.ROLE_ICT_SUPPORT.toString());
        Set<Role> ictRoles = new HashSet<Role>();
        ictRoles.add(ictRole);
        ictUser.setRoles(ictRoles);

        researcherUser = new User();

        group = new ResearchGroup();
        group.setId(1L);

        project = new ResearchProject();
        project.setId(2L);
        project.setResearchGroup(group);

        dataset = new ResearchDataset();
        dataset.setId(3L);
        dataset.setResearchProject(project);

        permissionServiceImpl = new PermissionServiceImpl();
    }

    @Test
    public void testGetViewGroupPermissionsWhenIct()
    {
        GroupPermissionQuery query = permissionServiceImpl.getViewGroupPermissions(ictUser);
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
        assignPermissionToUser(researcherUser, "/1/");
        GroupPermissionQuery query = permissionServiceImpl.getViewGroupPermissions(researcherUser);
        assertTrue(query instanceof ConcreteGroupsPermissionsQuery);
        ConcreteGroupsPermissionsQuery concrete = (ConcreteGroupsPermissionsQuery) query;
        assertTrue(concrete.getIds().size() == 1);
        assertTrue(concrete.getIds().contains(1L));
    }

    @Test
    public void testGetViewGroupPermissionsWhenResearcherHasImplicitPermissions()
    {
        assignPermissionToUser(researcherUser, "/1/2/");
        GroupPermissionQuery query = permissionServiceImpl.getViewGroupPermissions(researcherUser);
        assertTrue(query instanceof ConcreteGroupsPermissionsQuery);
        ConcreteGroupsPermissionsQuery concrete = (ConcreteGroupsPermissionsQuery) query;
        assertTrue(concrete.getIds().size() == 1);
        assertTrue(concrete.getIds().contains(1L));
    }

    @Test
    public void testGetGroupLevelForAsIct()
    {
        AccessLevel level = permissionServiceImpl.getGroupLevelFor(group, ictUser);
        assertEquals(level, AccessLevel.VIEWING_ACCESS);
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
        ProjectPermissionQuery query = permissionServiceImpl.getViewProjectPermissions(ictUser, group);
        assertTrue(query instanceof AllProjectsForGroupPermissionQuery);
    }

    @Test
    public void testGetViewProjectPermissionsWhenResearcherHasNoPermissions()
    {
        ProjectPermissionQuery query = permissionServiceImpl.getViewProjectPermissions(researcherUser, group);
        assertTrue(query instanceof NoProjectsPermissionQuery);
    }

    @Test
    public void testGetViewProjectPermissionsWhenResearcherHasExplicitPermissions()
    {
        assignPermissionToUser(researcherUser, "/1/2/");
        ProjectPermissionQuery query = permissionServiceImpl.getViewProjectPermissions(researcherUser, group);
        assertTrue(query instanceof ConcreteProjectsPermissionQuery);
        ConcreteProjectsPermissionQuery concrete = (ConcreteProjectsPermissionQuery) query;
        assertTrue(concrete.getIds().size() == 1);
        assertTrue(concrete.getIds().contains(2L));
    }

    @Test
    public void testGetViewProjectPermissionsWhenResearcherHasImplicitChildPermissions()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/");
        ProjectPermissionQuery query = permissionServiceImpl.getViewProjectPermissions(researcherUser, group);
        assertTrue(query instanceof ConcreteProjectsPermissionQuery);
        ConcreteProjectsPermissionQuery concrete = (ConcreteProjectsPermissionQuery) query;
        assertTrue(concrete.getIds().size() == 1);
        assertTrue(concrete.getIds().contains(2L));
    }

    @Test
    public void testGetViewProjectPermissionsWhenResearcherHasImplicitParentPermissions()
    {
        assignPermissionToUser(researcherUser, "/1/");
        ProjectPermissionQuery query = permissionServiceImpl.getViewProjectPermissions(researcherUser, group);
        assertTrue(query instanceof AllProjectsForGroupPermissionQuery);
    }

    @Test
    public void testGetProjectLevelForAsIct()
    {
        AccessLevel level = permissionServiceImpl.getProjectLevelFor(project, ictUser);
        assertEquals(level, AccessLevel.VIEWING_ACCESS);
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
        DatasetPermissionQuery query = permissionServiceImpl.getViewDatasetPermissions(ictUser, project);
        assertTrue(query instanceof AllDatasetsForProjectPermissionQuery);
    }

    @Test
    public void testGetViewDatasetPermissionsWhenResearcherHasNoPermissions()
    {
        DatasetPermissionQuery query = permissionServiceImpl.getViewDatasetPermissions(researcherUser, project);
        assertTrue(query instanceof NoDatasetsPermissionQuery);
    }

    @Test
    public void testGetViewDatasetPermissionsWhenResearcherHasExplicitPermissions()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/");
        DatasetPermissionQuery query = permissionServiceImpl.getViewDatasetPermissions(researcherUser, project);
        assertTrue(query instanceof ConcreteDatasetsPermissionQuery);
        ConcreteDatasetsPermissionQuery concrete = (ConcreteDatasetsPermissionQuery) query;
        assertTrue(concrete.getIds().size() == 1);
        assertTrue(concrete.getIds().contains(3L));
    }

    @Test
    public void testGetViewDatasetPermissionsWhenResearcherHasImplicitChildPermissions()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/folder/");
        DatasetPermissionQuery query = permissionServiceImpl.getViewDatasetPermissions(researcherUser, project);
        assertTrue(query instanceof ConcreteDatasetsPermissionQuery);
        ConcreteDatasetsPermissionQuery concrete = (ConcreteDatasetsPermissionQuery) query;
        assertTrue(concrete.getIds().size() == 1);
        assertTrue(concrete.getIds().contains(3L));
    }

    @Test
    public void testGetViewDatasetPermissionsWhenResearcherHasImplicitGroupPermissions()
    {
        assignPermissionToUser(researcherUser, "/1/");
        DatasetPermissionQuery query = permissionServiceImpl.getViewDatasetPermissions(researcherUser, project);
        assertTrue(query instanceof AllDatasetsForProjectPermissionQuery);
    }

    @Test
    public void testGetViewDatasetPermissionsWhenResearcherHasImplicitProjectPermissions()
    {
        assignPermissionToUser(researcherUser, "/1/2/");
        DatasetPermissionQuery query = permissionServiceImpl.getViewDatasetPermissions(researcherUser, project);
        assertTrue(query instanceof AllDatasetsForProjectPermissionQuery);
    }

    @Test
    public void testGetDatasetLevelForAsIct()
    {
        AccessLevel level = permissionServiceImpl.getDatasetLevelFor(dataset, ictUser);
        assertEquals(level, AccessLevel.VIEWING_ACCESS);
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
        PermissionDto edit = new PermissionDto(PermissionType.EDIT_GROUP, false);
        assertTrue(permissionDtos.contains(edit));
        PermissionDto create = new PermissionDto(PermissionType.CREATE_PROJECT, false);
        assertTrue(permissionDtos.contains(create));
        PermissionDto assign = new PermissionDto(PermissionType.ASSIGN_PERMISSION, false);
        assertTrue(permissionDtos.contains(assign));
    }

    @Test
    public void testGetResearchGroupPermissionsWhenEditor()
    {
        assignPermissionToUser(researcherUser, "/1/", AccessLevel.EDITING_ACCESS);
        List<PermissionDto> permissionDtos = permissionServiceImpl.getResearchGroupPermissions(group, researcherUser);
        PermissionDto edit = new PermissionDto(PermissionType.EDIT_GROUP, true);
        assertTrue(permissionDtos.contains(edit));
        PermissionDto create = new PermissionDto(PermissionType.CREATE_PROJECT, false);
        assertTrue(permissionDtos.contains(create));
        PermissionDto assign = new PermissionDto(PermissionType.ASSIGN_PERMISSION, true);
        assertTrue(permissionDtos.contains(assign));
    }

    @Test
    public void testGetResearchGroupPermissionsWhenFull()
    {
        assignPermissionToUser(researcherUser, "/1/", AccessLevel.FULL_ACCESS);
        List<PermissionDto> permissionDtos = permissionServiceImpl.getResearchGroupPermissions(group, researcherUser);
        PermissionDto edit = new PermissionDto(PermissionType.EDIT_GROUP, true);
        assertTrue(permissionDtos.contains(edit));
        PermissionDto create = new PermissionDto(PermissionType.CREATE_PROJECT, true);
        assertTrue(permissionDtos.contains(create));
        PermissionDto assign = new PermissionDto(PermissionType.ASSIGN_PERMISSION, true);
        assertTrue(permissionDtos.contains(assign));
    }

    @Test
    public void testGetResearchProjectPermissionsWhenViewer()
    {
        assignPermissionToUser(researcherUser, "/1/2/", AccessLevel.VIEWING_ACCESS);
        List<PermissionDto> permissionDtos = permissionServiceImpl.getResearchProjectPermissions(project,
                researcherUser);
        PermissionDto edit = new PermissionDto(PermissionType.EDIT_PROJECT, false);
        assertTrue(permissionDtos.contains(edit));
        PermissionDto create = new PermissionDto(PermissionType.CREATE_DATASET, false);
        assertTrue(permissionDtos.contains(create));
    }

    @Test
    public void testGetResearchProjectPermissionsWhenEditor()
    {
        assignPermissionToUser(researcherUser, "/1/2/", AccessLevel.EDITING_ACCESS);
        List<PermissionDto> permissionDtos = permissionServiceImpl.getResearchProjectPermissions(project,
                researcherUser);
        PermissionDto edit = new PermissionDto(PermissionType.EDIT_PROJECT, true);
        assertTrue(permissionDtos.contains(edit));
        PermissionDto create = new PermissionDto(PermissionType.CREATE_DATASET, false);
        assertTrue(permissionDtos.contains(create));
    }

    @Test
    public void testGetResearchProjectPermissionsWhenFull()
    {
        assignPermissionToUser(researcherUser, "/1/2/", AccessLevel.FULL_ACCESS);
        List<PermissionDto> permissionDtos = permissionServiceImpl.getResearchProjectPermissions(project,
                researcherUser);
        PermissionDto edit = new PermissionDto(PermissionType.EDIT_PROJECT, true);
        assertTrue(permissionDtos.contains(edit));
        PermissionDto create = new PermissionDto(PermissionType.CREATE_DATASET, true);
        assertTrue(permissionDtos.contains(create));
    }

    @Test
    public void testGetResearchDatasetPermissionsWhenViewer()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.VIEWING_ACCESS);
        List<PermissionDto> permissionDtos = permissionServiceImpl.getResearchDatasetPermissions(dataset,
                researcherUser);
        PermissionDto edit = new PermissionDto(PermissionType.EDIT_DATASET, false);
        assertTrue(permissionDtos.contains(edit));
        PermissionDto publish = new PermissionDto(PermissionType.PUBLISH_DATASET, false);
        assertTrue(permissionDtos.contains(publish));
        PermissionDto advertise = new PermissionDto(PermissionType.REJECT_ADVERTISING_DATASET, false);
        assertTrue(permissionDtos.contains(advertise));
    }

    @Test
    public void testGetResearchDatasetPermissionsWhenEdit()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.EDITING_ACCESS);
        List<PermissionDto> permissionDtos = permissionServiceImpl.getResearchDatasetPermissions(dataset,
                researcherUser);
        PermissionDto edit = new PermissionDto(PermissionType.EDIT_DATASET, true);
        assertTrue(permissionDtos.contains(edit));
        PermissionDto publish = new PermissionDto(PermissionType.PUBLISH_DATASET, false);
        assertTrue(permissionDtos.contains(publish));
        PermissionDto advertise = new PermissionDto(PermissionType.REJECT_ADVERTISING_DATASET, false);
        assertTrue(permissionDtos.contains(advertise));
    }

    @Test
    public void testGetResearchDatasetPermissionsWhenFull()
    {
        assignPermissionToUser(researcherUser, "/1/2/3/", AccessLevel.FULL_ACCESS);
        List<PermissionDto> permissionDtos = permissionServiceImpl.getResearchDatasetPermissions(dataset,
                researcherUser);
        PermissionDto edit = new PermissionDto(PermissionType.EDIT_DATASET, true);
        assertTrue(permissionDtos.contains(edit));
        PermissionDto publish = new PermissionDto(PermissionType.PUBLISH_DATASET, true);
        assertTrue(permissionDtos.contains(publish));
        PermissionDto advertise = new PermissionDto(PermissionType.REJECT_ADVERTISING_DATASET, true);
        assertTrue(permissionDtos.contains(advertise));
    }

    private void assignPermissionToUser(User user, String path)
    {
        PermissionEntry entry = new PermissionEntry();
        entry.setPath(path);
        user.getPermissionEntries().add(entry);
    }

    private void assignPermissionToUser(User user, String path, AccessLevel level)
    {
        PermissionEntry entry = new PermissionEntry();
        entry.setPath(path);
        entry.setAccessLevel(level);
        user.getPermissionEntries().add(entry);
    }

}
