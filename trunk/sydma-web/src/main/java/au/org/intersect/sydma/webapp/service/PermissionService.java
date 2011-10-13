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
import java.util.List;

import au.org.intersect.sydma.webapp.domain.AccessLevel;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.permission.PermissionDto;
import au.org.intersect.sydma.webapp.permission.PermissionType;
import au.org.intersect.sydma.webapp.permission.dataset.DatasetPermissionQuery;
import au.org.intersect.sydma.webapp.permission.group.GroupPermissionQuery;
import au.org.intersect.sydma.webapp.permission.path.Path;
import au.org.intersect.sydma.webapp.permission.project.ProjectPermissionQuery;
/**
 * PermissionDto Service Interface
 * @version $Rev: 29 $
 */

public interface PermissionService
{
    /**
     * The different actions to which we can assign permissions
     *
     * @version $Rev: 29 $
     */
    public enum Action
    {
        CREATE, READ, UPDATE, DELETE;
    }
    
    /**
     * Access control for research groups
     *
     * @version $Rev: 29 $
     */
    public interface ResearchGroupAction 
    {
        String act(ResearchGroup researchGroup, User user);
    }
    
    public String canGroup(PermissionType permission, Long id, Principal principal, ResearchGroupAction action);
    
    /**
     * Access control for research projects
     *
     * @version $Rev: 29 $
     */
    public interface ResearchProjectAction 
    {
        String act(ResearchProject project, User user);
    }
    public String canProject(PermissionType permission, Long id, Principal principal, ResearchProjectAction action);
        
    /**
     * Access control for research datasets
     *
     * @version $Rev: 29 $
     */
    public interface ResearchDatasetAction 
    {
        String act(ResearchDataset dataset, User user);
    }

    public String canDataset(PermissionType permission, Long id, Principal principal, ResearchDatasetAction action);

    /**
     * Returns the list of groups the user has viewing access over
     * @param user
     * @return
     */
    public GroupPermissionQuery getViewGroupPermissions(User user);
    
    /**
     * Returns the list of groups the user has upload access over
     * @param user
     * @return
     */
    public GroupPermissionQuery getUploadGroupPermission(User user);
    
    /**
     * Returns the list of groups the user has permission over
     * @param user
     * @return
     */
    public GroupPermissionQuery getDownloadGroupPermission(User user);
    
    /**
     * Returns the list of projects under a specific group where the user has viewing access over
     * @param user
     * @param researchProject
     * @return
     */
    public ProjectPermissionQuery getViewProjectPermissions(User user, ResearchGroup researchGroup);
    
    /**
     * Returns the list of projects under a specific group where the user has upload access over
     * @param user
     * @param researchProject
     * @return
     */
    public ProjectPermissionQuery getUploadProjectPermission(User user, Path researchGroupPath);
    
    /**
     * Returns the list of projects under a specific group where the user has download access over
     * @param user
     * @param researchProject
     * @return
     */
    public ProjectPermissionQuery getDownloadProjectPermission(User user, Path researchGroupPath);
    
    /**
     * Return the list of datasets under a specific project where the user has viewing access over
     * @param user
     * @param researchProject
     * @return
     */
    public DatasetPermissionQuery getViewDatasetPermissions(User user, ResearchProject researchProject);

    /**
     * Return the list of datasets under a specific project where the user has viewing access over
     * @param user
     * @param researchProject
     * @return
     */
    public DatasetPermissionQuery getUploadDatasetPermissions(User user, Path researchProject);

    /**
     * Return the list of datasets under a specific project where the user has viewing access over
     * @param user
     * @param researchProject
     * @return
     */
    public DatasetPermissionQuery getDownloadDatasetPermissions(User user, Path researchProject);


    /**
     * Adds a permission to a user
     * @param user - the user to give permission to
     * @param path - the path indicating the object
     * @param level - the level of permission the user has
     */
    public void addPermission(User user, Path path, AccessLevel level);

    /**
     * Returns a list of permissions over the group
     * @param user
     * @return
     */
    public List<PermissionDto> getResearchGroupPermissions(ResearchGroup group, User user);

    /**
     * Returns a list of permissions over the project
     * @param project
     * @param user
     * @return
     */
    public List<PermissionDto> getResearchProjectPermissions(ResearchProject project, User user);

    /**
     * Returns a list of permissions over the dataset
     * @param dataset
     * @param user
     * @return
     */
    public List<PermissionDto> getResearchDatasetPermissions(ResearchDataset dataset, User user);

    
}
