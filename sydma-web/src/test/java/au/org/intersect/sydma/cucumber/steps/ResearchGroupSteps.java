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

//TODO CHECKSTYLE-OFF: ImportOrderCheck
//TODO CHECKSTYLE-OFF: MultipleStringLiteralsCheck

import java.io.IOException;
import java.util.List;

import cuke4duke.annotation.I18n.EN.Given;
import cuke4duke.spring.StepDefinitions;

import au.org.intersect.sydma.webapp.domain.Building;
import au.org.intersect.sydma.webapp.domain.PubliciseStatus;
import au.org.intersect.sydma.webapp.domain.RdsRequest;
import au.org.intersect.sydma.webapp.domain.RdsRequestStatus;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;
import au.org.intersect.sydma.webapp.domain.ResearchSubjectCode;
import au.org.intersect.sydma.webapp.domain.User;

@StepDefinitions
public class ResearchGroupSteps
{
    
    
    @Given("I have a research group")
    public void iHaveAResearchGroup() throws IOException
    {
        ResearchGroup group = new ResearchGroup();
        group.setName("Research Group");
        group.setSubjectCode(findOrCreateSubjectCode("10"));
        group.setPrincipalInvestigator(findOrCreateUser());
        group.setDescription("Some description");
        group.merge();
    }
    
    @Given("I have a physical collection research group called \"([^\"]*)\"$")
    public void iHaveAPhysicalResearchGroupCalled(String researchGroupName) throws IOException
    {
        ResearchGroup group = new ResearchGroup();
        group.setName(researchGroupName);
        group.setSubjectCode(findOrCreateSubjectCode("10"));
        group.setDescription("Some description");
        group.setPrincipalInvestigator(findOrCreateUser());
        group.setIsPhysical(true);
        group.merge();
    }

    @Given("^I have a research group called \"([^\"]*)\"$")
    public void iHaveAResearchGroupCalled(String researchGroupName)
    {
        ResearchGroup group = new ResearchGroup();
        group.setName(researchGroupName);
        group.setSubjectCode(findOrCreateSubjectCode("10"));
        group.setDescription("Some description");
        group.setPrincipalInvestigator(findOrCreateUser());
        group.merge();
    }

    @Given("^I have a research group called \"([^\"]*)\" with principal investigator \"([^\"]*)\"$")
    public void iHaveAResearchGroupCalledWithPrincipalInvestigator(String researchGroupName, String pi)
    {
        ResearchGroup group = new ResearchGroup();
        group.setName(researchGroupName);
        group.setSubjectCode(new ResearchSubjectCode("10", "10"));
        group.setDescription("Some description");
        User user = User.findUsersByUsernameEquals(pi).getSingleResult();
        group.setPrincipalInvestigator(user);
        group.merge();
    }
    
    @Given("^the group \"([^\"]*)\" has a project called \"([^\"]*)\"$")
    public void theGroupResearchGroupTestHasAProjectCalled(String researchGroupName, String researchProjectName)
    {
        ResearchGroup group = ResearchGroup.findResearchGroupsByNameEquals(researchGroupName).getSingleResult();
        ResearchProject project = new ResearchProject();
        project.setName(researchProjectName);
        project.setDescription("Group Description");
        project.setSubjectCode(findOrCreateSubjectCode("909099"));
        project.setUrl("http://foo.com");
        project.setResearchGroup(group);
        project.merge();
    }

    @Given("^the project \"([^\"]*)\" has a dataset \"([^\"]*)\"$")
    public void theProjectHasADataset(String researchProjectName, String researchDatasetName)
    {
        ResearchProject project = ResearchProject.findResearchProjectsByNameEquals(researchProjectName)
                .getSingleResult();
        ResearchGroup group = project.getResearchGroup();
        ResearchDataset dataset = new ResearchDataset();
        dataset.setName(researchDatasetName);
        dataset.setDescription("Project Description");
        dataset.setPhysicalLocation(findOrCreateBuilding());
        dataset.setSubjectCode(findOrCreateSubjectCode("909098"));
        dataset.setPubliciseStatus(PubliciseStatus.NOT_ADVERTISED);
        dataset.setResearchProject(project);
        dataset.setIsPhysical(group.getIsPhysical());
        dataset.merge();
    }

    @Given("^the project \"([^\"]*)\" has an advertised dataset \"([^\"]*)\"$")
    public void theProjectHasAdvertisedDataset(String researchProjectName, String researchDatasetName)
    {
        ResearchProject project = ResearchProject.findResearchProjectsByNameEquals(researchProjectName)
                .getSingleResult();
        ResearchDataset dataset = new ResearchDataset();
        dataset.setName(researchDatasetName);
        dataset.setDescription("Dataset Description");
        dataset.setPhysicalLocation(findOrCreateBuilding());
        dataset.setSubjectCode(findOrCreateSubjectCode("909090"));
        dataset.setPubliciseStatus(PubliciseStatus.ADVERTISED);
        dataset.setResearchProject(project);
        dataset.merge();
    }
    
    @Given("^the project \"([^\"]*)\" has a ready for advertising dataset \"([^\"]*)\"$")
    public void theProjectHasAReadyForAdvertisingDataset(String researchProjectName, String researchDatasetName)
    {
        ResearchProject project = ResearchProject.findResearchProjectsByNameEquals(researchProjectName)
                .getSingleResult();
        ResearchDataset dataset = new ResearchDataset();
        dataset.setName(researchDatasetName);
        dataset.setDescription("Dataset Description");
        dataset.setPhysicalLocation(findOrCreateBuilding());
        dataset.setSubjectCode(findOrCreateSubjectCode("909090"));
        dataset.setPubliciseStatus(PubliciseStatus.MARKED_AS_READY_FOR_ADVERTISING);
        dataset.setResearchProject(project);
        dataset.merge();
    }
    
    @Given("^I have an unapproved RDS request called \"([^\"]*)\" with PI \"([^\"]*)\"$")
    public void iHaveAnUnapprovedRDSRequestCalledUnapprovedWithPI(String group, String pi) 
    {
        RdsRequest request = new RdsRequest();
        request.setRequestStatus(RdsRequestStatus.CREATED);
        request.setName(group);
        User piUser = User.findUsersByUsernameEquals(pi).getSingleResult();
        request.setPrincipalInvestigator(piUser);
        request.setAmountOfStorage(100);
        request.setSubjectCode(findOrCreateSubjectCode("99"));
        request.setDescription("Some description");
        request.merge();
    }

    
    private Building findOrCreateBuilding()
    {
        List<Building> firstBuilding = Building.findBuildingEntries(0, 1);
        if (!firstBuilding.isEmpty())
        {
            return firstBuilding.get(0);
        }
        else
        {
            Building building = new Building();
            building.setBuildingCode("code");
            building.setBuildingName("name");
            building.persist();
            return building;
        }
    }

    private User findOrCreateUser()
    {
        boolean isUserlistEmpty = User.findUserEntries(0, 1).isEmpty();
        if (!isUserlistEmpty)
        {
            return User.findUserEntries(0, 1).get(0);
        }
        else
        {
            User user = new User();
            user.setEnabled(true);
            user.setUsername("");
            user.persist();
            return user;
        }
    }
    
    private ResearchSubjectCode findOrCreateSubjectCode(String code)
    {
        ResearchSubjectCode subjectCode = ResearchSubjectCode.findResearchSubjectCode(code);
        if (subjectCode != null)
        {
            return subjectCode;
        }
        subjectCode = new ResearchSubjectCode(code, code);
        subjectCode.persist();
        return subjectCode;
    }



}
