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
package au.org.intersect.sydma.webapp.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import au.org.intersect.sydma.webapp.domain.Building;
import au.org.intersect.sydma.webapp.domain.PublicAccessRight;
import au.org.intersect.sydma.webapp.domain.PubliciseStatus;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;
import au.org.intersect.sydma.webapp.domain.ResearchSubjectCode;
import au.org.intersect.sydma.webapp.domain.User;

/**
 * 
 * 
 * @version $Rev: 29 $
 */
// TODO CHECKSTYLE-OFF: ParameterAssignmentCheck
public class RifCsWriterImplTest
{
    // private static Logger LOG = LoggerFactory.getLogger(RifCsWriterImplTest.class);

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private RifCsWriterImpl writer;
    private ResearchGroup group;
    private User user;
    private ResearchProject project1;
    private ResearchDataset dataset1;
    private Building building;

    @Before
    public void setUp()
    {
        writer = new RifCsWriterImpl();
        writer.setDestinationDirectory(folder.getRoot().getAbsolutePath());

        user = new User();
        user.setEmail("me@sydney.edu.au");
        user.setSurname("Smith");
        user.setGivenname("Fred");

        group = new ResearchGroup();
        group.setName("My group name");
        group.setDescription("My group desc");
        group.setId(5L);
        group.setPrincipalInvestigator(user);
        group.setSubjectCode(new ResearchSubjectCode("555", "555"));
        group.setSubjectCode2(new ResearchSubjectCode("111", "111"));
        group.setSubjectCode3(null);

        project1 = createProject("Proj1", group, 101L);
        ResearchProject project2 = createProject("Proj2", group, 102L);
        ResearchProject project3 = createProject("Proj3", group, 103L);
        project1.setUrl("http://myprojecturl.com");
        project1.setSubjectCode(new ResearchSubjectCode("999", "999"));
        project1.setSubjectCode2(new ResearchSubjectCode("111", "111"));
        project1.setSubjectCode3(null);
        project1.setDescription("This is my project description");

        dataset1 = createDataset("Dataset1", project1, 201L, PubliciseStatus.ADVERTISED);
        ResearchDataset dataset2 = createDataset("Dataset2", project1, 202L,
                PubliciseStatus.MARKED_AS_READY_FOR_ADVERTISING);
        ResearchDataset dataset3 = createDataset("Dataset3", project2, 203L, PubliciseStatus.NOT_ADVERTISED);
        ResearchDataset dataset4 = createDataset("Dataset4", project3, 204L, PubliciseStatus.ADVERTISED);
        dataset1.setDescription("This is my dataset description.");
        dataset1.setSubjectCode(new ResearchSubjectCode("888", "888"));
        dataset1.setSubjectCode2(new ResearchSubjectCode("111", "111"));
        dataset1.setSubjectCode3(null);
        PublicAccessRight publicAccessRight = new PublicAccessRight();
        publicAccessRight.setDescription("These are my rights.");
        dataset1.setPublicAccessRight(publicAccessRight);

        Set<ResearchProject> projects = new HashSet<ResearchProject>();
        projects.add(project1);
        projects.add(project2);
        projects.add(project3);
        group.setResearchProjects(projects);

        Set<ResearchDataset> proj1Datasets = new HashSet<ResearchDataset>();
        proj1Datasets.add(dataset1);
        proj1Datasets.add(dataset2);
        Set<ResearchDataset> proj2Datasets = new HashSet<ResearchDataset>();
        proj2Datasets.add(dataset3);
        Set<ResearchDataset> proj3Datasets = new HashSet<ResearchDataset>();
        proj3Datasets.add(dataset4);
        project1.setResearchDatasets(proj1Datasets);
        project2.setResearchDatasets(proj2Datasets);
        project3.setResearchDatasets(proj3Datasets);

        building = new Building();
        building.setAddressLine1("my addr 1");
        building.setAddressLine2("my addr 2");
        building.setBuildingCode("my code");
        building.setBuildingName("My building name");
        building.setCampus("my campus");
        building.setCity("my suburb");
        building.setCountry("my country");
        building.setPostCode("my postcode");
        building.setStateName("my state");
    }

    @Test
    public void testWritePartyRecord() throws IOException
    {
        group.setUrl("http://www.google.com/agroup");

        writer.writeGroupRifCs(group);

        String actual = readFromTempFolder(group.getKeyForRifCs() + ".xml");
        String expected = readFileFromClasspath("research-group.xml");
        parseAndAssertEquals(actual, expected);
    }

    @Test
    public void testPartyRecordShouldUseSydneyAsOriginatingSourceWhenNoUrlPresent() throws IOException
    {
        writer.writeGroupRifCs(group);

        String actual = readFromTempFolder(group.getKeyForRifCs() + ".xml");
        String expected = readFileFromClasspath("research-group-no-url.xml");
        parseAndAssertEquals(actual, expected);
    }

    @Test
    public void testPartyRecordShouldNotIncludeDescriptionElementIfNoDescription() throws IOException
    {
        group.setUrl("http://www.google.com/agroup");
        group.setDescription("");
        writer.writeGroupRifCs(group);

        String actual = readFromTempFolder(group.getKeyForRifCs() + ".xml");
        String expected = readFileFromClasspath("research-group-no-desc.xml");
        parseAndAssertEquals(actual, expected);
    }

    @Test
    public void testWritePrincipalInvestigatorRecord() throws IOException
    {
        group.setUrl("http://www.google.com/agroup");
        user.getResearchGroups().add(group);
        writer.writePrincipalInvestigatorRifCs(user, group);

        String actual = readFromTempFolder(user.getKeyForRifCs() + ".xml");
        String expected = readFileFromClasspath("principal-investigator.xml");
        parseAndAssertEquals(actual, expected);
    }

    @Test
    public void testWriteProjectRecord() throws IOException
    {
        writer.writeProjectRifCs(project1);

        String actual = readFromTempFolder(project1.getKeyForRifCs() + ".xml");
        String expected = readFileFromClasspath("activity.xml");
        parseAndAssertEquals(actual, expected);
    }

    @Test
    public void testWriteDatasetRecord() throws IOException
    {
        dataset1.setPhysicalLocation(building);
        dataset1.setAdditionalLocationInformation("additional loc info");
        writer.writeDatasetRifCs(dataset1);

        String actual = readFromTempFolder(dataset1.getKeyForRifCs() + ".xml");
        String expected = readFileFromClasspath("dataset.xml");
        parseAndAssertEquals(actual, expected);
    }

    @Test
    public void testDatasetRecordSkipsLocationBlockIfNoLocation() throws IOException
    {
        dataset1.setPhysicalLocation(null);
        dataset1.setAdditionalLocationInformation("");
        writer.writeDatasetRifCs(dataset1);

        String actual = readFromTempFolder(dataset1.getKeyForRifCs() + ".xml");
        String expected = readFileFromClasspath("dataset-no-location.xml");
        parseAndAssertEquals(actual, expected);
    }

    @Test
    public void testDatasetRecordSkipsEmptyElementsWithinLocationRecord() throws IOException
    {
        building.setAddressLine1("");
        building.setAddressLine2("");
        building.setBuildingCode("my code");
        building.setBuildingName("My building name");
        building.setCity("");
        building.setCountry("");
        building.setPostCode("");
        building.setStateName("");

        dataset1.setPhysicalLocation(building);
        dataset1.setAdditionalLocationInformation("");
        writer.writeDatasetRifCs(dataset1);

        String actual = readFromTempFolder(dataset1.getKeyForRifCs() + ".xml");
        String expected = readFileFromClasspath("dataset-minimal-location.xml");
        parseAndAssertEquals(actual, expected);
    }

    @Test
    public void testTemplateEscapesXmlSpecialCharacters() throws IOException
    {
        group.setDescription("<A desc with \" some > special 'characters' & some normal ones");
        writer.writeGroupRifCs(group);

        String actual = readFromTempFolder(group.getKeyForRifCs() + ".xml");
        String expected = readFileFromClasspath("research-group-special-chars.xml");
        parseAndAssertEquals(actual, expected);
    }

    @Test
    public void testEraseDatasetRecord()
    {
        writer.writeDatasetRifCs(dataset1);
        File datasetFile = new File(folder.getRoot().getAbsolutePath() + File.separator
                + dataset1.getKeyForRifCs().concat(".xml"));

        assertTrue(datasetFile.exists());
        writer.eraseDatasetRifCs(dataset1);
        assertFalse(datasetFile.exists());
    }

    @Test
    public void testEraseProjectRecord()
    {
        writer.writeProjectRifCs(project1);
        File projectFile = new File(folder.getRoot().getAbsolutePath() + File.separator
                + project1.getKeyForRifCs().concat(".xml"));

        assertTrue(projectFile.exists());
        dataset1.setPubliciseStatus(PubliciseStatus.NOT_ADVERTISED);
        writer.eraseProjectRifCs(project1);
        assertFalse(projectFile.exists());
    }

    @Test
    public void testNotEraseProjectRecordIfStillHasOtherAdvertisedDataset()
    {
        writer.writeProjectRifCs(project1);
        File projectFile = new File(folder.getRoot().getAbsolutePath() + File.separator
                + project1.getKeyForRifCs().concat(".xml"));

        assertTrue(projectFile.exists());
        dataset1.setPubliciseStatus(PubliciseStatus.ADVERTISED);
        writer.eraseProjectRifCs(project1);
        assertTrue(projectFile.exists());
    }

    @Test
    public void testEraseGroupRecord()
    {
        writer.writeGroupRifCs(group);
        File groupFile = new File(folder.getRoot().getAbsolutePath() + File.separator
                + group.getKeyForRifCs().concat(".xml"));

        assertTrue(groupFile.exists());

        // set all datasets to NOT_ADVERTISED
        List<ResearchProject> researchProjects = group.getAdvertisedResearchProjects();
        for (ResearchProject researchProject : researchProjects)
        {
            List<ResearchDataset> advertisedResearchDatasets = researchProject.getAdvertisedResearchDatasets();

            for (ResearchDataset researchDataset : advertisedResearchDatasets)
            {
                researchDataset.setPubliciseStatus(PubliciseStatus.NOT_ADVERTISED);
            }

        }

        writer.eraseGroupRifCs(group);
        assertFalse(groupFile.exists());
    }

    @Test
    public void testNotEraseGroupRecordIfStillHasOtherAdvertisedDataset()
    {
        writer.writeGroupRifCs(group);
        File groupFile = new File(folder.getRoot().getAbsolutePath() + File.separator
                + group.getKeyForRifCs().concat(".xml"));

        assertTrue(groupFile.exists());

        // set all datasets to NOT_ADVERTISED
        List<ResearchProject> researchProjects = group.getAdvertisedResearchProjects();
        for (ResearchProject researchProject : researchProjects)
        {
            List<ResearchDataset> advertisedResearchDatasets = researchProject.getAdvertisedResearchDatasets();

            for (ResearchDataset researchDataset : advertisedResearchDatasets)
            {
                researchDataset.setPubliciseStatus(PubliciseStatus.ADVERTISED);
            }

        }

        writer.eraseGroupRifCs(group);
        assertTrue(groupFile.exists());
    }

    @Test
    public void testErasePrincipalInvestigatorRecord()
    {
        User pi = mock(User.class);
        when(pi.isPrincipalInvestigatorForAnAdvertisedGroup()).thenReturn(false);
        when(pi.getKeyForRifCs()).thenReturn("pi-key");
        writer.writePrincipalInvestigatorRifCs(pi, group);
        File pIFile = new File(folder.getRoot().getAbsolutePath() + File.separator 
                + pi.getKeyForRifCs().concat(".xml"));

        assertTrue(pIFile.exists());

        writer.erasePrincipalInvestigatorRifCs(pi, group);
        assertFalse(pIFile.exists());
    }

    @Test
    public void testNotErasePrincipalInvestigatorRecordIfStillHasOtherAdvertisedDataset()
    {
        User pi = mock(User.class);
        when(pi.isPrincipalInvestigatorForAnAdvertisedGroup()).thenReturn(true);
        when(pi.getKeyForRifCs()).thenReturn("pi-key");
        writer.writePrincipalInvestigatorRifCs(pi, group);
        File pIFile = new File(folder.getRoot().getAbsolutePath() + File.separator 
                + pi.getKeyForRifCs().concat(".xml"));

        assertTrue(pIFile.exists());

        writer.erasePrincipalInvestigatorRifCs(pi, group);
        assertTrue(pIFile.exists());
    }

    private String readFileFromClasspath(String name) throws IOException
    {
        InputStream resourceAsStream = getClass().getResourceAsStream("/expected-rifcs/" + name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");
        while ((line = reader.readLine()) != null)
        {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }
        String fileContents = stringBuilder.toString();
        return fileContents.substring(0, fileContents.length() - 1);

    }

    private ResearchProject createProject(String name, ResearchGroup group, Long id)
    {
        ResearchProject project = new ResearchProject();
        project.setName(name);
        project.setResearchGroup(group);
        project.setId(id);
        return project;
    }

    private ResearchDataset createDataset(String name, ResearchProject project, long id, 
            PubliciseStatus publiciseStatus)
    {
        ResearchDataset dataset = new ResearchDataset();
        dataset.setName(name);
        dataset.setResearchProject(project);
        dataset.setId(id);
        dataset.setPubliciseStatus(publiciseStatus);
        return dataset;
    }

    private String readFromTempFolder(String fileName) throws IOException
    {
        return FileUtils.readFileToString(new File(folder.getRoot().getAbsolutePath() + File.separator + fileName));
    }

    private void parseAndAssertEquals(String actual, String expected)
    {
        actual = actual.replaceAll("\\s", "");
        expected = expected.replaceAll("\\s", "");
        assertEquals("RIF-CS file did not match expected", expected, actual);
    }

}
