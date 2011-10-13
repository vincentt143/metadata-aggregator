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
package au.org.intersect.sydma.webapp.permission.path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;

public class PathBuilderTest
{
    private static final String NO_ILLEGAL_EXCEPTION = "Expected Illegal Argument Exception";
    private ResearchGroup group;
    private ResearchProject project;
    private ResearchDataset dataset;
    
    @Before
    public void setUp()
    {
        group = new ResearchGroup();
        group.setId(1L);

        project = new ResearchProject();
        project.setId(2L);
        project.setResearchGroup(group);
        
        dataset = new ResearchDataset();
        dataset.setId(3L);
        dataset.setResearchProject(project);
    }
    
    @Test
    public void testRootPath()
    {
        Path rootPath = PathBuilder.rootPath();
        assertEquals(rootPath.getPath(), "/"); 
    }
    
    @Test
    public void testGroupPath()
    {
        Path groupPath = PathBuilder.groupPath(group);
        assertEquals(groupPath.getPath(), "/1/");
        assertTrue(groupPath.isGroupPath());
    }
    
    @Test
    public void testProjectPath()
    {
        Path projectPath = PathBuilder.projectPath(project);
        assertEquals(projectPath.getPath(), "/1/2/");
        assertTrue(projectPath.isProjectPath());
    }
    
    @Test
    public void testDatasetPath()
    {
        Path projectPath = PathBuilder.datasetPath(dataset);
        assertEquals(projectPath.getPath(), "/1/2/3/");
        assertTrue(projectPath.isDatasetPath());
    }
    
    @Test
    public void testFilePathDelimitersBoth()
    {
        Path projectPath = PathBuilder.filePath(dataset, "/dir/dor/");
        assertEquals(projectPath.getPath(), "/1/2/3/dir/dor/");
        assertTrue(projectPath.isFilePath());
    }
    
    @Test
    public void testFilePathDelimitersNone()
    {
        Path projectPath = PathBuilder.filePath(dataset, "dir/dor");
        assertEquals(projectPath.getPath(), "/1/2/3/dir/dor/");
    }
    
    @Test
    public void testBuildFromGroupString()
    {
        Path groupPath = PathBuilder.buildFromString("/1/");
        Long expected = 1L;
        assertEquals(expected, groupPath.getGroupId());
        try
        {
            groupPath.getProjectId();
            fail(NO_ILLEGAL_EXCEPTION);
        }
        catch (IllegalArgumentException ex)
        {
            
        }
    }

    @Test
    public void testBuildFromProjectString()
    {
        Path projectPath = PathBuilder.buildFromString("/1/2/");
        Long expectedGroup = 1L;
        Long expectedProject = 2L;
        assertEquals(expectedGroup, projectPath.getGroupId());
        assertEquals(expectedProject, projectPath.getProjectId());
        try
        {
            projectPath.getDatasetId();
            fail(NO_ILLEGAL_EXCEPTION);
        }
        catch (IllegalArgumentException ex)
        {
            
        }
    }

    @Test
    public void testBuildFromDatasetString()
    {
        Path datasetPath = PathBuilder.buildFromString("/1/2/3/");
        Long expectedGroup = 1L;
        Long expectedProject = 2L;
        Long expectedDataset = 3L;
        assertEquals(expectedGroup, datasetPath.getGroupId());
        assertEquals(expectedProject, datasetPath.getProjectId());
        assertEquals(expectedDataset, datasetPath.getDatasetId());
        try
        {
            datasetPath.getFilePath();
            fail(NO_ILLEGAL_EXCEPTION);
        }
        catch (IllegalArgumentException ex)
        {
            
        }
    }

    @Test
    public void testBuildFromFileString()
    {
        Path filePath = PathBuilder.buildFromString("/1/2/3/other/unnecessary/long/path/just/to/be/sure/");
        Long expectedGroup = 1L;
        Long expectedProject = 2L;
        Long expectedDataset = 3L;
        assertEquals(expectedGroup, filePath.getGroupId());
        assertEquals(expectedProject, filePath.getProjectId());
        assertEquals(expectedDataset, filePath.getDatasetId());
        assertEquals("/other/unnecessary/long/path/just/to/be/sure/", filePath.getFilePath());
    }

}
