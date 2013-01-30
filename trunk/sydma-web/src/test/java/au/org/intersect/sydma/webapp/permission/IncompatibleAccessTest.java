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
package au.org.intersect.sydma.webapp.permission;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

import java.security.Principal;

import org.junit.Before;
import org.junit.Test;

import au.org.intersect.sydma.webapp.controller.PermissionController;
import au.org.intersect.sydma.webapp.domain.AccessLevel;
import au.org.intersect.sydma.webapp.domain.PermissionEntry;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.permission.path.PathBuilder;
import au.org.intersect.sydma.webapp.service.PermissionServiceImpl;

public class IncompatibleAccessTest
{
    private static final String FULL_ACCESS = "FULL_ACCESS";
    private User editingAccessUser;
    private User assignee;
    private Principal principal;
    private ResearchGroup group;
    private ResearchProject project;
    private ResearchDataset dataset;
    private PermissionController permissionController;
    private PermissionServiceImpl permissionServiceImpl;
    private PermissionEntry entry;

    @Before
    public void setUp()
    {
        editingAccessUser = new User();
        assignee = mock(User.class);
        principal = mock(Principal.class);
        
        group = new ResearchGroup();
        group.setId(1L);
        project = new ResearchProject();
        project.setId(2L);
        project.setResearchGroup(group);
        dataset = new ResearchDataset();
        dataset.setId(3L);
        dataset.setResearchProject(project);
        permissionServiceImpl = mock(PermissionServiceImpl.class);
        permissionController = mock(PermissionController.class);
        entry = mock(PermissionEntry.class);
    }

    @Test
    public void testAssignFullAccessToGroupWhenYouHaveEditingAccess()
    {
        assignPermission(editingAccessUser, PathBuilder.groupPath(group).getPath(), AccessLevel.EDITING_ACCESS);
        assertFalse(permissionController.assignPermissionsToTheSelectedUsers(assignee.getUsername(), FULL_ACCESS,
                PathBuilder.groupPath(group),
                permissionServiceImpl.hasFullAccessPermissionForGroup(editingAccessUser, group), principal));
    }
    
    @Test
    public void testAssignFullAccessToProjectWhenYouHaveEditingAccess()
    {
        assignPermission(editingAccessUser, PathBuilder.projectPath(project).getPath(), AccessLevel.EDITING_ACCESS);
        assertFalse(permissionController.assignPermissionsToTheSelectedUsers(assignee.getUsername(), FULL_ACCESS,
                PathBuilder.projectPath(project),
                permissionServiceImpl.hasFullAccessPermissionForProject(editingAccessUser, project), principal));
    }
    
    @Test
    public void testAssignFullAccessToDatasetWhenYouHaveEditingAccess()
    {
        assignPermission(editingAccessUser, PathBuilder.groupPath(group).getPath(), AccessLevel.EDITING_ACCESS);
        assertFalse(permissionController.assignPermissionsToTheSelectedUsers(assignee.getUsername(), FULL_ACCESS,
                PathBuilder.datasetPath(dataset),
                permissionServiceImpl.hasFullAccessPermissionForDataset(editingAccessUser, dataset), principal));
    }

    public void assignPermission(User user, String path, AccessLevel level)
    {
        entry.setAccessLevel(level);
        entry.setUser(user);
        entry.setPath(path);
        entry.merge();
    }


}
