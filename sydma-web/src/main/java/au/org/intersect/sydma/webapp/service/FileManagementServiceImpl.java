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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.domain.JobItem;
import au.org.intersect.dms.core.errors.TransportException;
import au.org.intersect.dms.core.service.DmsService;
import au.org.intersect.dms.core.service.dto.JobStatus;
import au.org.intersect.sydma.webapp.permission.path.Path;

/**
 * Implementation of FileManagementService
 */
@Service
// do not rollback if an error occurs in Dms as they are not recoverable
@Transactional(value = "sydmaPU", noRollbackFor = {TransportException.class})
public class FileManagementServiceImpl implements FileManagementService
{
    private static final Logger LOG = LoggerFactory.getLogger(FileManagementServiceImpl.class);

    @Autowired
    private DmsService dmsService;

    @Autowired
    private FilePathService filePathService;

    public FileManagementServiceImpl()
    {

    }

    public FileManagementServiceImpl(DmsService dmsService, FilePathService filePathService)
    {
        super();
        this.dmsService = dmsService;
        this.filePathService = filePathService;
    }

    @Override
    public boolean deleteFileOrDirectory(Path path, Integer connectionId)
    {
        String absolutePath = filePathService.resolveToRelativePath(path);

        // delete annotations regardless of file deletion success
        // deleteAnnotation(path);
        return performDelete(connectionId, absolutePath);
    }

    @Override
    public boolean moveFileOrDirectory(final String username, final Path srcPath, final Path destPath,
            final Integer srcConnectionId, final Integer destConnectionId)
    {
        boolean moveSuccess = false;

        String absoluteSrcPath = filePathService.resolveToRelativePath(srcPath);

        String absoluteDestPath = filePathService.resolveToRelativePath(destPath);

        boolean copySuccess = performMoveCopy(username, absoluteSrcPath, absoluteDestPath, srcConnectionId,
                destConnectionId);
        // only if copy success do we proceed to perform delete

        if (copySuccess)
        {
            // performMoveAnnotation(srcPath, destPath);
            moveSuccess = performDelete(srcConnectionId, absoluteSrcPath);
        }

        return moveSuccess;
    }

    private boolean performDelete(Integer connectionId, String absolutePath)
    {
      //we need to request the actual file info as dms relies on more than just the path for deletion
        FileInfo fileToDelete = dmsService.getFileInfo(connectionId, absolutePath); 
        fileToDelete.setAbsolutePath(absolutePath);
        List<FileInfo> fileInfos = new ArrayList<FileInfo>();
        fileInfos.add(fileToDelete);

        return dmsService.delete(connectionId, fileInfos);
    }

    private boolean performMoveCopy(final String username, final String absoluteSrcPath, final String absoluteDestPath,
            final Integer srcConnectionId, final Integer destConnectionId)
    {

        List<String> copySource = new ArrayList<String>(1);
        copySource.add(absoluteSrcPath);

        final Long jobId = dmsService.copy(username, srcConnectionId, copySource, destConnectionId, absoluteDestPath);

        boolean copySuccess = false;

        String copyErrorStatus = null;
        try
        {
            while (true)
            {
                JobItem jobItem = dmsService.getJobStatus(username, jobId);
                String status = jobItem.getStatus();
                if (JobStatus.FINISHED.toString().equals(status))
                {
                    copySuccess = true;
                    break;
                }
                if (JobStatus.ABORTED.toString().equals(status))
                {
                    copyErrorStatus = "Move aborted";
                    break;
                }
                if (JobStatus.CANCELLED.toString().equals(status))
                {
                    copyErrorStatus = "Move cancelled";
                    break;
                }
                // TODO CHECKSTYLE-OFF: Magic Number
                // Since dms runs copy in another process, we have to pause the current thread until copy is complete
                Thread.sleep(100);

            }
        }
        // TODO CHECKSTYLE-OFF: IllegalCatch
        catch (Exception e)
        {
            copyErrorStatus = e.getMessage();
        }
        if (copyErrorStatus != null)
        {
            LOG.error("Move file failed on unusual status during copy phase, {}", copyErrorStatus);
        }
        return copySuccess;
    }

}
