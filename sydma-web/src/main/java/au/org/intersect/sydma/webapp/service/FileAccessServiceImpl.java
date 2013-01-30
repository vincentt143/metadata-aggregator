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

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.domain.FileType;
import au.org.intersect.dms.core.service.DmsService;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;

/**
 * 
 * 
 * @version $Rev: 29 $
 */
@Service
public class FileAccessServiceImpl implements FileAccessService
{
    private static final Logger LOG = LoggerFactory.getLogger(FileAccessServiceImpl.class);

    private static final String TEST_WRITABLE_FILE_NAME = "test_writable";

    @Autowired
    private DmsAccessPointService dapService;

    @Override
    public boolean prepareDatasetFileSpace(ResearchDataset researchDataset)
    {
        Long datasetId = researchDataset.getId();

        Integer conn = dapService.connectLocal(DmsAccessPointService.SYSTEM_USER);

        String path = "/" + researchDataset.getResearchProject().getResearchGroup().getDirectoryPath();

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Allocating space [" + path + "] for dataset with id [" + datasetId + "]");
        }

        return dapService.getDmsService().createDir(conn, path, datasetId.toString());
    }

    @Override
    public boolean verifyGroupPath(String filePath)
    {
        Integer conn = dapService.connectLocal(DmsAccessPointService.SYSTEM_USER);
        DmsService dms = dapService.getDmsService();

        try
        {
            FileInfo info = dms.getFileInfo(conn, filePath);
            return info != null && info.getFileType() == FileType.DIRECTORY && testDirectory(conn, filePath);
        }
        catch (au.org.intersect.dms.core.errors.PathNotFoundException e)
        {
            return false;
        }

    }

    private boolean testDirectory(Integer conn, String filePath)
    {
        List<FileInfo> list = dapService.getDmsService().getList(conn, filePath);
        if (list == null || list.size() > 0)
        {
            return false;
        }
        return testCanCreateDir(conn, filePath);
    }

    private boolean testCanCreateDir(Integer conn, String filePath)
    {
        String tempDirName = filePath + "/" + TEST_WRITABLE_FILE_NAME;
        if (dapService.getDmsService().createDir(conn, filePath, TEST_WRITABLE_FILE_NAME))
        {
            FileInfo info = dapService.getDmsService().getFileInfo(conn, tempDirName);
            List<FileInfo> list = new ArrayList<FileInfo>(1);
            list.add(info);
            dapService.getDmsService().delete(conn, list);
            return true;
        }
        return false;
    }

}
