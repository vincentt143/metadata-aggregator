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

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.staticmock.AnnotationDrivenStaticEntityMockingControl;
import org.springframework.mock.staticmock.MockStaticEntityMethods;

import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;
import au.org.intersect.sydma.webapp.dto.FilePathInfo;
import au.org.intersect.sydma.webapp.permission.path.FilePath;
import au.org.intersect.sydma.webapp.permission.path.Path;
import au.org.intersect.sydma.webapp.permission.path.ResearchDatasetPath;

@MockStaticEntityMethods
public class FilePathServiceImplTest
{
    private FilePathServiceImpl filePathService;

    @Before
    public void setUp()
    {
        filePathService = new FilePathServiceImpl();
    }

    @Test
    public void testParseVirtualPath()
    {
        String virtualPathWithSlash = "/1/2/3/dir1/file1.txt/";

        FilePathInfo relativePathWithSlash = filePathService.parseVirtualPath(virtualPathWithSlash);

        assertEquals("Parsed out groupId is incorrect", new Long(1), relativePathWithSlash.getGroupId());
        assertEquals("Parsed out projectId is incorrect", new Long(2), relativePathWithSlash.getProjectId());
        assertEquals("Parsed out datasetId is incorrect", new Long(3), relativePathWithSlash.getDatasetId());
        assertEquals("Parsed out directory path is incorrect", "dir1/file1.txt", relativePathWithSlash.getDirectory());

        //the method should work with or without trailing slash
        String virtualPathWithOutSlash = "/1/2/3/dir1/file1.txt";

        FilePathInfo relativePathWithoutSlash = filePathService.parseVirtualPath(virtualPathWithOutSlash);

        assertEquals("Parsed out groupId is incorrect", new Long(1), relativePathWithoutSlash.getGroupId());
        assertEquals("Parsed out projectId is incorrect", new Long(2), relativePathWithoutSlash.getProjectId());
        assertEquals("Parsed out datasetId is incorrect", new Long(3), relativePathWithoutSlash.getDatasetId());
        assertEquals("Parsed out directory path is incorrect", "dir1/file1.txt",
                relativePathWithoutSlash.getDirectory());

    }

    @Test
    public void testRelativeToVirtualWithPathAssist()
    {
        String fileUploadPath = "/gp1/1/dir1/file1.txt";

        Long datasetId = new Long(1);
        Long projectId = new Long(2);
        Long groupId = new Long(3);

        Path filePath = new FilePath(groupId, projectId, datasetId, new String[] {"dir1", "file1.txt"});

        String virtualPath = filePathService.relativeToVirtualPath(filePath, fileUploadPath);

        assertEquals("/3/2/1/dir1/file1.txt/", virtualPath);
    }

    @Test
    public void testRelativeToVirtual()
    {
        String fileUploadPath = "/gp1/1/dir1/file1.txt";

        Long datasetId = new Long(1);
        Long projectId = new Long(2);
        Long groupId = new Long(3);

        ResearchGroup group = new ResearchGroup();
        group.setId(groupId);

        ResearchProject project = new ResearchProject();
        project.setId(projectId);
        project.setResearchGroup(group);

        ResearchDataset dataset = new ResearchDataset();
        dataset.setId(datasetId);
        dataset.setResearchProject(project);

        ResearchDataset.findResearchDataset(datasetId);
        AnnotationDrivenStaticEntityMockingControl.expectReturn(dataset);
        AnnotationDrivenStaticEntityMockingControl.playback();

        String virtualPath = filePathService.relativeToVirtualPath(fileUploadPath);

        assertEquals("/3/2/1/dir1/file1.txt/", virtualPath);
    }
    
    @Test
    public void testResolveToRelativePathWithFilePath()
    {
        String fileUploadPath = "/gp1/1/dir1/file1.txt";

        Long datasetId = new Long(1);
        Long projectId = new Long(2);
        Long groupId = new Long(3);

        Path filePath = new FilePath(groupId, projectId, datasetId, new String[] {"dir1", "file1.txt"});
        
        ResearchGroup group = new ResearchGroup();
        group.setDirectoryPath("gp1");

        ResearchGroup.findResearchGroup(groupId);
        AnnotationDrivenStaticEntityMockingControl.expectReturn(group);
        AnnotationDrivenStaticEntityMockingControl.playback();
        
        String resolvedRelative = filePathService.resolveToRelativePath(filePath);
        
        assertEquals(fileUploadPath, resolvedRelative);
        
        
    }
    

    @Test
    public void testResolveToRelativePathWithDatasetPath()
    {
        String fileUploadPath = "/gp1/1";

        Long datasetId = new Long(1);
        Long projectId = new Long(2);
        Long groupId = new Long(3);

        Path filePath = new ResearchDatasetPath(groupId, projectId, datasetId);
        
        ResearchGroup group = new ResearchGroup();
        group.setDirectoryPath("gp1");

        ResearchGroup.findResearchGroup(groupId);
        AnnotationDrivenStaticEntityMockingControl.expectReturn(group);
        AnnotationDrivenStaticEntityMockingControl.playback();
        
        String resolvedRelative = filePathService.resolveToRelativePath(filePath);
        
        assertEquals(fileUploadPath, resolvedRelative);
                
    }
    
    @Test
    public void testCreateVirtualPath()
    {
        Long datasetId = new Long(3);
        Long projectId = new Long(2);
        Long groupId = new Long(1);
        
        String filePath = "dir1/file/txt";
        
        String groupVirtualPath = filePathService.createVirtualPath(groupId);
        String projectVirtualPath = filePathService.createVirtualPath(groupId, projectId);
        String datasetVirtualPath = filePathService.createVirtualPath(groupId, projectId, datasetId);
        String fileVirtualPath = filePathService.createVirtualPath(groupId, projectId, datasetId, filePath);
        
        assertEquals("/1/", groupVirtualPath);
        assertEquals("/1/2/", projectVirtualPath);
        assertEquals("/1/2/3/", datasetVirtualPath);
        assertEquals("/1/2/3/" + filePath + "/", fileVirtualPath);
        
    }
}
