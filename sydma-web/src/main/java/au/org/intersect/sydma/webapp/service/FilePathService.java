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

import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.dto.FilePathInfo;
import au.org.intersect.sydma.webapp.permission.path.Path;


/**
 *
 * @version $Rev: 29 $
 */

public interface FilePathService
{
    /**
     * Parse virtual path which is in format /groupId/projectId/datasetId/directory
     * into the 4 component values
     */
    FilePathInfo parseVirtualPath(String path);

    /**
     * Absolute path to the dataset
     * uploadRoot/serverPath(usually upload)/groupDirectory/datasetId
     */
    String resolveDatasetAbsolutePath(ResearchDataset dataset);

    /**
     * Absolute path to the dataset
     * uploadRoot/serverPath(usually upload)/groupDirectory
     */
    String resolveGroupAbsolutePath(String groupDirectoryPath);

    /**
     * Convert a upload path of format /groupPath/datasetId/directory     
     * to virtual path of format /groupId/projectId/datasetId/directory
     * The FilePathInfo is used to get the group, project and dataset id. 
     * The directory in FilePathInfo is not used as it is
     * presumed to be a part of uploadPath
     */
    String relativeToVirtualPath(Path filePath, String uploadPath);

    /**
     * Relative path is a path under server root, ie. groupPath/datasetId(/directory)
     * 
     * It expects the groupId and datasetId in pathInfo to be not null and valid,
     * If directory is not null in pathInfo it will add it as part of the relative path
     */
    String resolveRelativePath(Path pathInfo);

    String createVirtualPath(Long groupId);

    String createVirtualPath(Long groupId, Long projectId);
    
    String createVirtualPath(Long groupId, Long projectId, Long datasetId);
    
    String resolveVirtualPath(Long groupId, Long projectId, Long datasetId, String directory);
}
