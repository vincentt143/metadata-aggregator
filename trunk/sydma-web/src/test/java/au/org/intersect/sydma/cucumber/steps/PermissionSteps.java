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
package au.org.intersect.sydma.cucumber.steps;

// TODO CHECKSTYLE-OFF: ImportOrderCheck
//TODO CHECKSTYLE-OFF: MultipleStringLiteralsCheck
import static org.junit.Assert.assertEquals;

import org.springframework.transaction.annotation.Transactional;

import cuke4duke.annotation.I18n.EN.Given;
import cuke4duke.annotation.I18n.EN.Then;
import cuke4duke.spring.StepDefinitions;

import au.org.intersect.sydma.webapp.domain.AccessLevel;
import au.org.intersect.sydma.webapp.domain.PermissionEntry;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.permission.path.PathBuilder;

@StepDefinitions
@Transactional
public class PermissionSteps
{

    @Given("^user \"([^\"]*)\" has full access to group \"([^\"]*)\"$")
    public void userResearcherHasFullAccessToGroup(String username, String groupName) throws InterruptedException
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();
        ResearchGroup group = ResearchGroup.findResearchGroupsByNameEquals(groupName).getSingleResult();
        PermissionEntry entry = new PermissionEntry();
        entry.setAccessLevel(AccessLevel.FULL_ACCESS);
        entry.setUser(user);
        entry.setPath(PathBuilder.groupPath(group).getPath());
        entry.merge();
    }
    
    
    @Given("^user \"([^\"]*)\" has editing access to group \"([^\"]*)\"$")
    public void userResearcherHasEditingAccessToGroup(String username, String groupName) throws InterruptedException
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();
        ResearchGroup group = ResearchGroup.findResearchGroupsByNameEquals(groupName).getSingleResult();
        PermissionEntry entry = new PermissionEntry();
        entry.setAccessLevel(AccessLevel.EDITING_ACCESS);
        entry.setUser(user);
        entry.setPath(PathBuilder.groupPath(group).getPath());
        entry.merge();
    }
    
    @Given("^user \"([^\"]*)\" has viewing access to group \"([^\"]*)\"$")
    public void userResearcherHasViewingAccessToGroup(String username, String groupName)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();
        ResearchGroup group = ResearchGroup.findResearchGroupsByNameEquals(groupName).getSingleResult();
        PermissionEntry entry = new PermissionEntry();
        entry.setAccessLevel(AccessLevel.VIEWING_ACCESS);
        entry.setUser(user);
        entry.setPath(PathBuilder.groupPath(group).getPath());
        entry.merge();
    }
    
    @Given("^user \"([^\"]*)\" has full access to project \"([^\"]*)\"$")
    public void grantFullAccessToProject(String username, String projectName)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();
        ResearchProject project = ResearchProject.findResearchProjectsByNameEquals(projectName).getSingleResult();
        PermissionEntry entry = new PermissionEntry();
        entry.setAccessLevel(AccessLevel.FULL_ACCESS);
        entry.setUser(user);
        entry.setPath(PathBuilder.projectPath(project).getPath());
        entry.merge();
    }
    
    @Given("^user \"([^\"]*)\" has editing access to project \"([^\"]*)\"$")
    public void grantEditingAccessToProject(String username, String projectName)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();
        ResearchProject project = ResearchProject.findResearchProjectsByNameEquals(projectName).getSingleResult();
        PermissionEntry entry = new PermissionEntry();
        entry.setAccessLevel(AccessLevel.EDITING_ACCESS);
        entry.setUser(user);
        entry.setPath(PathBuilder.projectPath(project).getPath());
        entry.merge();
    }
    
    @Given("^user \"([^\"]*)\" has viewing access to project \"([^\"]*)\"$")
    public void grantViewingAccessToProject(String username, String projectName)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();
        ResearchProject project = ResearchProject.findResearchProjectsByNameEquals(projectName).getSingleResult();
        PermissionEntry entry = new PermissionEntry();
        entry.setAccessLevel(AccessLevel.VIEWING_ACCESS);
        entry.setUser(user);
        entry.setPath(PathBuilder.projectPath(project).getPath());
        entry.merge();
    }
    
    @Given("^user \"([^\"]*)\" has full access to dataset \"([^\"]*)\"$")
    public void grantFullAccessToDataset(String username, String datasetName)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();
        PermissionEntry entry = new PermissionEntry();
        entry.setAccessLevel(AccessLevel.FULL_ACCESS);
        entry.setUser(user);
        entry.setPath(PathBuilder.datasetPath(dataset).getPath());
        entry.merge();
    }
    
    @Given("^user \"([^\"]*)\" has editing access to dataset \"([^\"]*)\"$")
    public void grantEditingAccessToDataset(String username, String datasetName)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();
        PermissionEntry entry = new PermissionEntry();
        entry.setAccessLevel(AccessLevel.EDITING_ACCESS);
        entry.setUser(user);
        entry.setPath(PathBuilder.datasetPath(dataset).getPath());
        entry.merge();
    }
    
    @Given("^user \"([^\"]*)\" has viewing access to dataset \"([^\"]*)\"$")
    public void grantViewingAccessToDataset(String username, String datasetName)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();
        PermissionEntry entry = new PermissionEntry();
        entry.setAccessLevel(AccessLevel.VIEWING_ACCESS);
        entry.setUser(user);
        entry.setPath(PathBuilder.datasetPath(dataset).getPath());
        entry.merge();
    }
    
    @Then("^user \"([^\"]*)\" should have full access to group \"([^\"]*)\"$")
    public void userUnikeyShouldHaveFullAccessToGroup(String username, String name)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();

        ResearchGroup group = ResearchGroup.findResearchGroupsByNameEquals(name).getSingleResult();
        String path = PathBuilder.groupPath(group).getPath();

        PermissionEntry entry = PermissionEntry.findPermissionEntrysByPathEqualsAndUser(path, user)
                .getSingleResult();
        assertEquals(entry.getAccessLevel(), AccessLevel.FULL_ACCESS);
    }
    
 
    @Then("^user \"([^\"]*)\" should not have access to group \"([^\"]*)\"$")
    public void userUnikeyShouldNotHaveAccessToGroup(String username, String name)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();

        ResearchGroup group = ResearchGroup.findResearchGroupsByNameEquals(name).getSingleResult();
        String path = PathBuilder.groupPath(group).getPath();

        int size = PermissionEntry.findPermissionEntrysByPathEqualsAndUser(path, user).getResultList().size();
        assertEquals(size, 0);
    }
    
    @Then("^user \"([^\"]*)\" should have full access to project \"([^\"]*)\"$")
    public void userUnikeyShouldHaveFullAccessToProject(String username, String name)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();

        ResearchProject project = ResearchProject.findResearchProjectsByNameEquals(name).getSingleResult();
        String path = PathBuilder.projectPath(project).getPath();
   
        PermissionEntry entry = PermissionEntry.findPermissionEntrysByPathEqualsAndUser(path, user)
                .getSingleResult();
        assertEquals(entry.getAccessLevel(), AccessLevel.FULL_ACCESS);

    }
    
    
    @Then("^user \"([^\"]*)\" should have full access to dataset \"([^\"]*)\"$")
    public void userUnikeyShouldHaveFullAccessToDataset(String username, String name)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();

        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(name).getSingleResult();
        String path = PathBuilder.datasetPath(dataset).getPath();

        PermissionEntry entry = PermissionEntry.findPermissionEntrysByPathEqualsAndUser(path, user)
                .getSingleResult();
        assertEquals(entry.getAccessLevel(), AccessLevel.FULL_ACCESS);

    }

    @Then("^user \"([^\"]*)\" should have editing access to group \"([^\"]*)\"$")
    public void userUnikeyShouldHaveEditingAccessToGroup(String username, String name)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();
        ResearchGroup group = ResearchGroup.findResearchGroupsByNameEquals(name).getSingleResult();
        String path = PathBuilder.groupPath(group).getPath();

        PermissionEntry entry = PermissionEntry.findPermissionEntrysByPathEqualsAndUser(path, user)
                .getSingleResult();
        assertEquals(entry.getAccessLevel(), AccessLevel.EDITING_ACCESS);
        
    }
    
    @Then("^user \"([^\"]*)\" should have editing access to project \"([^\"]*)\"$")
    public void userUnikeyShouldHaveEditingAccessToProject(String username, String name)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();

        ResearchProject project = ResearchProject.findResearchProjectsByNameEquals(name).getSingleResult();
        String path = PathBuilder.projectPath(project).getPath();

        PermissionEntry entry = PermissionEntry.findPermissionEntrysByPathEqualsAndUser(path, user)
                .getSingleResult();
        assertEquals(entry.getAccessLevel(), AccessLevel.EDITING_ACCESS);
        
    }
    
    @Then("^user \"([^\"]*)\" should have editing access to dataset \"([^\"]*)\"$")
    public void userUnikeyShouldHaveEditingAccessToDataset(String username, String name)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();

        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(name).getSingleResult();
        String path = PathBuilder.datasetPath(dataset).getPath();

        PermissionEntry entry = PermissionEntry.findPermissionEntrysByPathEqualsAndUser(path, user)
                .getSingleResult();
        assertEquals(entry.getAccessLevel(), AccessLevel.EDITING_ACCESS);
    
    }
    
    @Then("^user \"([^\"]*)\" should have viewing access to group \"([^\"]*)\"$")
    public void userUnikeyShouldHaveViewingAccessToGroup(String username, String name)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();

        ResearchGroup group = ResearchGroup.findResearchGroupsByNameEquals(name).getSingleResult();
        String path = PathBuilder.groupPath(group).getPath();

        PermissionEntry entry = PermissionEntry.findPermissionEntrysByPathEqualsAndUser(path, user)
                .getSingleResult();
        assertEquals(entry.getAccessLevel(), AccessLevel.VIEWING_ACCESS);
    }
    
    @Then("^user \"([^\"]*)\" should have viewing access to project \"([^\"]*)\"$")
    public void userUnikeyShouldHaveViewingAccessToProject(String username, String name)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();

        ResearchProject project = ResearchProject.findResearchProjectsByNameEquals(name).getSingleResult();
        String path = PathBuilder.projectPath(project).getPath();

        PermissionEntry entry = PermissionEntry.findPermissionEntrysByPathEqualsAndUser(path, user)
                .getSingleResult();
        assertEquals(entry.getAccessLevel(), AccessLevel.VIEWING_ACCESS);
        
    }
    
    @Then("^user \"([^\"]*)\" should have viewing access to dataset \"([^\"]*)\"$")
    public void userUnikeyShouldHaveViewingAccessToDataset(String username, String name)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();

        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(name).getSingleResult();
        String path = PathBuilder.datasetPath(dataset).getPath();

        PermissionEntry entry = PermissionEntry.findPermissionEntrysByPathEqualsAndUser(path, user)
                .getSingleResult();
        assertEquals(entry.getAccessLevel(), AccessLevel.VIEWING_ACCESS);
    
    }
    
    @Given("^I remove the permission entries for user \"([^\"]*)\"$")
    public void removePermissionEntry(String username)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();
        for (PermissionEntry entry : user.getPermissionEntries())
        {
            entry.remove();
        }
    }


}
