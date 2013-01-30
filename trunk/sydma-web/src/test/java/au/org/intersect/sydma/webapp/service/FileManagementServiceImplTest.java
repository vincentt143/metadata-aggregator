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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.domain.JobItem;
import au.org.intersect.dms.core.service.DmsService;
import au.org.intersect.dms.core.service.dto.JobStatus;
import au.org.intersect.sydma.webapp.domain.FileAnnotation;
import au.org.intersect.sydma.webapp.permission.path.FilePath;

//TODO CHECKSTYLE-OFF: ExecutableStatementCount
//TODO CHECKSTYLE-OFF: NCSS

public class FileManagementServiceImplTest
{
    private static final Logger LOG = LoggerFactory.getLogger(FileManagementServiceImplTest.class);

    private FileManagementServiceImpl fileManagementService;

    private DmsService dmsService;
    private FilePathService filePathService;

    @Captor
    private ArgumentCaptor<List<FileInfo>> fileInfosCaptor;

    @Captor
    private ArgumentCaptor<List<String>> filePathsCaptor;
    

    @Before
    public void setUp()
    {
        dmsService = Mockito.mock(DmsService.class);
        filePathService = Mockito.mock(FilePathService.class);

        fileManagementService = new FileManagementServiceImpl(dmsService, filePathService);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDeleteFile()
    {
        Integer connectionId = 1;

        Long groupId = 1L;
        Long projectId = 1L;
        Long datasetId = 1L;
        String[] filePathToken = new String[] {"dirA", "dirB"}; // dirA/dirB

        FilePath pathToDelete = new FilePath(groupId, projectId, datasetId, filePathToken);

        String virtualPathToDelete = "/1/1/1/dirA/dirB/";
        String absolutePathToDelete = "/grpDir/1/dirA/dirB/";

        // setup existing annotation
        List<FileAnnotation> annotationList = new ArrayList<FileAnnotation>();
        FileAnnotation annotation1 = Mockito.mock(FileAnnotation.class);
        FileAnnotation annotation2 = Mockito.mock(FileAnnotation.class);
        annotationList.add(annotation1);
        annotationList.add(annotation2);

        FileInfo fileInfo = new FileInfo();

        Mockito.when(filePathService.resolveToRelativePath(pathToDelete)).thenReturn(absolutePathToDelete);
        Mockito.when(dmsService.getFileInfo(connectionId, absolutePathToDelete)).thenReturn(fileInfo);
        // run
        fileManagementService.deleteFileOrDirectory(pathToDelete, connectionId);

        // verify calls

        verify(filePathService).resolveToRelativePath(pathToDelete);
        
        verify(dmsService).delete(eq(connectionId), fileInfosCaptor.capture());

        List<FileInfo> capturedFile = fileInfosCaptor.getValue();

        assertEquals("Number of files sent for delete does not match", 1, capturedFile.size());

        FileInfo fileToDelete = capturedFile.get(0);

        assertEquals("File sent for delete does not have expected path", absolutePathToDelete,
                fileToDelete.getAbsolutePath());

    }

    @Test
    public void testMoveFile()
    {
        PowerMockito.mockStatic(FileAnnotation.class);
        
        //define variables        
        Integer srcConnectionId = 1;
        Integer destConnectionId = 2;
        String username = "user";

        Long groupId = 1L;
        Long projectId = 1L;
        Long datasetId = 1L;
        String[] srcFilePathToken = new String[] {"dirA", "dirB"}; // dirA/dirB
        String[] destFilePathToken = new String[] {"dirC"}; // dirA/dirB

        FilePath srcPath = new FilePath(groupId, projectId, datasetId, srcFilePathToken);
        FilePath destPath = new FilePath(groupId, projectId, datasetId, destFilePathToken);

        String srcVirtualPath = "/1/1/1/dirA/dirB/";
        String srcAbsolutePath = "/grpDir/1/dirA/dirB/";

        String destVirtualPath = "/1/1/1/dirC/";
        String destAbsolutePath = "/grpDir/1/dirC/";

        
        Mockito.when(filePathService.resolveToRelativePath(srcPath)).thenReturn(srcAbsolutePath);
        Mockito.when(filePathService.resolveToRelativePath(destPath)).thenReturn(destAbsolutePath);
        

        //return FINISHED status when job is enquired
        JobItem successJobItem = new JobItem(); 
        successJobItem.setStatus(JobStatus.FINISHED.toString());
        
        FileInfo fileInfo =  new FileInfo();
        
        Mockito.when(dmsService.getJobStatus(Matchers.anyString(), Matchers.anyLong())).thenReturn(successJobItem);
        Mockito.when(dmsService.getFileInfo(srcConnectionId, srcAbsolutePath)).thenReturn(fileInfo);
        // run
        fileManagementService.moveFileOrDirectory(username, srcPath, destPath, srcConnectionId, destConnectionId);

        // verify calls
        verify(filePathService).resolveToRelativePath(srcPath);

        verify(dmsService).copy(eq(username), eq(srcConnectionId), filePathsCaptor.capture(), eq(destConnectionId),
                eq(destAbsolutePath));
        
        List<String> capturedCopySrc = filePathsCaptor.getValue();
        
        assertEquals("Number of files sent for copy phase does not match", 1, capturedCopySrc.size());

        String fileToMove = capturedCopySrc.get(0);

        assertEquals("File sent for copy phase does not have expected path", srcAbsolutePath,
                fileToMove);
        
        
    }

}
