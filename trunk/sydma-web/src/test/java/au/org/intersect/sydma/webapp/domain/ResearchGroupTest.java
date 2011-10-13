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
package au.org.intersect.sydma.webapp.domain;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import au.org.intersect.sydma.webapp.util.RifCsWriter;

/**
 * 
 * 
 * @version $Rev: 29 $
 */
public class ResearchGroupTest
{
    private static final String PI_EMAIL = "oldpi@intersect.org.au";
    private RifCsWriter rifCsWriter = mock(RifCsWriter.class);

    @Test
    public void testGetKeyForRifCs()
    {
        ResearchGroup group = new ResearchGroup();
        group.setId(1234L);
        assertEquals("www.sydney.edu.au-metadata-aggregator-research-group-1234", group.getKeyForRifCs());
    }

    @Test
    public void testGetOriginatingSourceReturnsTheUrlOrSydneyIfNoUrl()
    {
        ResearchGroup groupWithUrl = new ResearchGroup();
        groupWithUrl.setUrl("http://project.com");
        assertEquals("http://project.com", groupWithUrl.getOriginatingSource());
        assertEquals("www.sydney.edu.au", new ResearchGroup().getOriginatingSource());
    }

    @Test
    public void testGetAdvertisedResearchProjects()
    {
        ResearchDataset dataset1 = new ResearchDataset();
        dataset1.setPubliciseStatus(PubliciseStatus.ADVERTISED);
        ResearchDataset dataset2 = new ResearchDataset();
        dataset2.setPubliciseStatus(PubliciseStatus.NOT_ADVERTISED);

        ResearchProject projectWithOne = createProject(dataset1, dataset2);
        ResearchProject projectWithNone = createProject(dataset2);
        ResearchProject emptyProject = createProject();

        ResearchGroup groupWithOne = new ResearchGroup();
        Set<ResearchProject> projects = new HashSet<ResearchProject>();
        projects.add(projectWithNone);
        projects.add(projectWithOne);
        projects.add(emptyProject);

        groupWithOne.setResearchProjects(projects);

        assertEquals(1, groupWithOne.getAdvertisedResearchProjects().size());
        assertEquals(projectWithOne, groupWithOne.getAdvertisedResearchProjects().get(0));
    }

    @Test
    public void updateRifCsOnlyUpdatesIfGroupHasAdvertisedProjects()
    {
        ResearchGroup group = createGroupWithOneProjectAndOneDataset(PubliciseStatus.ADVERTISED);

        group.updateRifCsIfNeeded(rifCsWriter, group.getPrincipalInvestigator());
        verify(rifCsWriter).writeGroupRifCs(group);
        //it should update the PI as well, since the PI has some details from the group
        verify(rifCsWriter).writePrincipalInvestigatorRifCs(group.getPrincipalInvestigator(), group);
        verifyNoMoreInteractions(rifCsWriter);
    }

    @Test
    public void updateRifCsDoesNothingIfGroupHasNoAdvertisedProjects()
    {
        ResearchGroup group = createGroupWithOneProjectAndOneDataset(PubliciseStatus.MARKED_AS_READY_FOR_ADVERTISING);
        group.updateRifCsIfNeeded(rifCsWriter, group.getPrincipalInvestigator());
        verifyZeroInteractions(rifCsWriter);
    }
    
    @Test
    public void updateRifCsCreatesNewPiRecordIfPiIsChanged()
    {
        ResearchGroup group = createGroupWithOneProjectAndOneDataset(PubliciseStatus.ADVERTISED);
        User newPi = group.getPrincipalInvestigator();
        User oldPi = new User();
        oldPi.setEmail(PI_EMAIL);
        oldPi.setId(2L);

        group.updateRifCsIfNeeded(rifCsWriter, oldPi);
        verify(rifCsWriter).writeGroupRifCs(group);
        verify(rifCsWriter).writePrincipalInvestigatorRifCs(newPi, group);
        verify(rifCsWriter).deletePrincipalInvestigatorRifCs(oldPi);
        verify(rifCsWriter).deletePrincipalInvestigatorRifCs(oldPi);
        ResearchProject project = group.getResearchProjects().iterator().next();
        ResearchDataset dataset = project.getResearchDatasets().iterator().next();
        verify(rifCsWriter).writeProjectRifCs(project);
        verify(rifCsWriter).writeDatasetRifCs(dataset);
        verifyNoMoreInteractions(rifCsWriter);
    }

    @Test
    public void updateRifCsUpdatesRelatedProjectsAndDatasetsIfPiIsChanged()
    {
        ResearchDataset dataset1 = createDataset(PubliciseStatus.ADVERTISED);
        ResearchDataset dataset2 = createDataset(PubliciseStatus.NOT_ADVERTISED);
        ResearchDataset dataset3 = createDataset(PubliciseStatus.ADVERTISED);
        ResearchDataset dataset4 = createDataset(PubliciseStatus.MARKED_AS_READY_FOR_ADVERTISING);
        ResearchProject project1 = createProject(dataset1, dataset2);
        ResearchProject project2 = createProject(dataset3);
        ResearchProject project3 = createProject(dataset4);
        ResearchGroup group = createGroup(project1, project2, project3);
        User newPi = new User();
        User oldPi = new User();
        oldPi.setEmail(PI_EMAIL);
        oldPi.setId(2L);
        group.setPrincipalInvestigator(newPi);

        group.updateRifCsIfNeeded(rifCsWriter, oldPi);
        verify(rifCsWriter).writeGroupRifCs(group);
        verify(rifCsWriter).writePrincipalInvestigatorRifCs(newPi, group);
        verify(rifCsWriter).deletePrincipalInvestigatorRifCs(oldPi);
        verify(rifCsWriter).writeProjectRifCs(project1);
        verify(rifCsWriter).writeProjectRifCs(project2);
        verify(rifCsWriter).writeDatasetRifCs(dataset1);
        verify(rifCsWriter).writeDatasetRifCs(dataset3);
        verifyNoMoreInteractions(rifCsWriter);
    }

    @Test
    public void updateRifCsDoesNotCreateNewPiRecordIfPiIsChangedButDatasetNotAdvertised()
    {
        ResearchGroup group = createGroupWithOneProjectAndOneDataset(PubliciseStatus.NOT_ADVERTISED);
        User oldPi = new User();
        oldPi.setEmail(PI_EMAIL);
        oldPi.setId(2L);

        group.updateRifCsIfNeeded(rifCsWriter, oldPi);
        verifyZeroInteractions(rifCsWriter);
    }

    private ResearchDataset createDataset(PubliciseStatus status)
    {
        ResearchDataset dataset = new ResearchDataset();
        dataset.setPubliciseStatus(status);
        dataset.setId(1L);
        return dataset;
    }

    private ResearchGroup createGroup(ResearchProject... projects)
    {
        ResearchGroup group = new ResearchGroup();
        Set<ResearchProject> projectSet = new HashSet<ResearchProject>();
        projectSet.addAll(Arrays.asList(projects));

        group.setResearchProjects(projectSet);

        return group;
    }

    private ResearchGroup createGroupWithOneProjectAndOneDataset(PubliciseStatus datasetStatus)
    {
        ResearchDataset dataset = new ResearchDataset();
        dataset.setPubliciseStatus(datasetStatus);
        ResearchProject project = createProject(dataset);
        ResearchGroup group = new ResearchGroup();
        Set<ResearchProject> projects = new HashSet<ResearchProject>();
        projects.add(project);
        group.setResearchProjects(projects);
        User pi = new User();
        pi.setEmail("pi@intersect.org.au");
        pi.setId(1L);
        group.setPrincipalInvestigator(pi);
        return group;
    }

    private ResearchProject createProject(ResearchDataset... datasets)
    {
        ResearchProject project = new ResearchProject();
        Set<ResearchDataset> datasetSet = new HashSet<ResearchDataset>();
        datasetSet.addAll(Arrays.asList(datasets));
        project.setId(1L);
        project.setResearchDatasets(datasetSet);

        return project;

    }
}
