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

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private FilePathService filePathService;

    @Override
    public boolean prepareDatasetFileSpace(ResearchDataset researchDataset)
    {
        Long datasetId = researchDataset.getId();

        String directoryPath = filePathService.resolveDatasetAbsolutePath(researchDataset);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Allocating space [" + directoryPath + "] for dataset with id [" + datasetId + "]");
        }

        File directory = new File(directoryPath);
        if (!directory.isDirectory())
        {
            return directory.mkdir();
        }
        return true;
    }

    @Override
    public boolean verifyGroupPath(String filePath)
    {
        String groupAccessPath = filePathService.resolveGroupAbsolutePath(filePath);

        File groupDirectory = new File(groupAccessPath);

        return testDirectory(groupDirectory);
    }

    private boolean testDirectory(File directory)
    {
        if (directory.isDirectory())
        {
            try
            {
                if (testDirectoryEmpty(directory) && testDirectoryWritable(directory))
                {
                    return true;
                }
            }
            catch (IOException e)
            {
                // do nothing
            }
        }
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Verification for directory " + directory + " failed");
        }
        return false;
    }

    private boolean testDirectoryEmpty(File directory)
    {
        String[] directoryContent = directory.list();

        return directoryContent.length == 0;
    }

    private boolean testDirectoryWritable(File directory) throws IOException
    {
        File file = File.createTempFile(TEST_WRITABLE_FILE_NAME, null, directory);
        file.delete(); // delete the temp file
        return true;
    }

}
