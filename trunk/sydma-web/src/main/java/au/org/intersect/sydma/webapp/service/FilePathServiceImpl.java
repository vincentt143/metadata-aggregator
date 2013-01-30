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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;
import au.org.intersect.sydma.webapp.dto.FilePathInfo;
import au.org.intersect.sydma.webapp.exception.InvalidFilePathException;
import au.org.intersect.sydma.webapp.permission.path.Path;

/**
 * 
 * 
 * @version $Rev: 29 $
 */
@Service
public class FilePathServiceImpl implements FilePathService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FilePathServiceImpl.class);

    // (?:/\.*?)? matches / followed by minimum matching of anything, the whole group is optional
    private static final String ENTITY_REGEX = "(?:[\\/\\\\](.*?))?";
    // (?:/\.*)? matches / followed by min matching of anything
    // followed by a optional closing /, the whole group is optional
    private static final String DIRECTORY_REGEX = "(?:[\\/\\\\](.*?)\\/?)?";

    private static final String VIRTUAL_PATH_REGEX = "^" + ENTITY_REGEX + ENTITY_REGEX + ENTITY_REGEX + DIRECTORY_REGEX
            + "$";

    private static final String RELATIVE_PATH_REGEX = "^" + ENTITY_REGEX + ENTITY_REGEX + DIRECTORY_REGEX + "$";

    private static final String PATH_SEPARATOR = "/";

    // TODO CHECKSTYLE-OFF: MagicNumber
    @Override
    public FilePathInfo parseVirtualPath(String path)
    {
        FilePathInfo filePath = new FilePathInfo();

        Pattern p = Pattern.compile(VIRTUAL_PATH_REGEX);
        Matcher m = p.matcher(path);
        if (m.find())
        {
            String group = m.group(1);
            String project = m.group(2);
            String dataset = m.group(3);
            String directory = m.group(4);

            if (StringUtils.isNotEmpty(group))
            {
                filePath.setGroupId(Long.valueOf(group));
            }
            if (StringUtils.isNotEmpty(project))
            {
                filePath.setProjectId(Long.valueOf(project));
            }
            if (StringUtils.isNotEmpty(dataset))
            {
                filePath.setDatasetId(Long.valueOf(dataset));
            }
            if (StringUtils.isNotEmpty(directory))
            {
                filePath.setDirectory(directory);
            }
        }
        else
        {
            throw new InvalidFilePathException("Invalid virtual path: " + path);
        }
        return filePath;
    }

    @Override
    public String resolveToRelativePath(Path pathInfo)
    {
        LOGGER.debug(" *** Path info => " + pathInfo);
        Long datasetId = pathInfo.getDatasetId();
        LOGGER.debug(" *** datasetId => " + datasetId);
        LOGGER.debug(" *** getGroupId => " + pathInfo.getGroupId());
        ResearchGroup group = ResearchGroup.findResearchGroup(pathInfo.getGroupId());
        LOGGER.debug(" *** group => " + group);

        if (datasetId == null)
        {
            throw new InvalidFilePathException("No ResearchGroup matching group id in path " + pathInfo.toString());
        }

        String groupPath = group.getDirectoryPath();

        StringBuilder builder = new StringBuilder(PATH_SEPARATOR);
        builder.append(groupPath);
        builder.append(PATH_SEPARATOR);
        builder.append(datasetId);
        if (pathInfo.isFilePath())
        {
            String filePath = pathInfo.getFilePath();
            /* remove trailing slash */
            if (filePath.length() > 0)
            {
                filePath = filePath.substring(0, filePath.length() - 1);
            }
            builder.append(filePath);
        }
        return builder.toString();
    }

    @Override
    public String relativeToVirtualPath(String uploadPath)
    {
        Pattern p = Pattern.compile(RELATIVE_PATH_REGEX);
        Matcher m = p.matcher(uploadPath);
        if (m.find())
        {
            try
            {
                String datasetIdStr = m.group(2);
                String directory = m.group(3);

                ResearchDataset dataset = ResearchDataset.findResearchDataset(Long.valueOf(datasetIdStr));

                if (dataset == null)
                {
                    return null;
                }

                ResearchProject project = dataset.getResearchProject();
                ResearchGroup group = project.getResearchGroup();

                StringBuilder buffer = buildRelativePath(group.getId(), project.getId(), dataset.getId(), directory);

                return buffer.toString();
            }
            catch (Exception e)
            {
                return null;
            }
        }
        return null;
    }

    @Override
    public String relativeToVirtualPath(Path filePath, String uploadPath)
    {
        Pattern p = Pattern.compile(RELATIVE_PATH_REGEX);
        Matcher m = p.matcher(uploadPath);
        if (m.find())
        {
            String directory = m.group(3);

            StringBuilder buffer = buildRelativePath(filePath.getGroupId(), filePath.getProjectId(),
                    filePath.getDatasetId(), directory);

            return buffer.toString();
        }
        throw new InvalidFilePathException("Invalid relative upload file path: " + uploadPath);
    }

    // private String constructAccessPath(String groupDirectoryPath, Long datasetId)
    // {
    // String accessPathToGroup = constructAccessPath(groupDirectoryPath);
    // StringBuilder buffer = new StringBuilder(accessPathToGroup);
    // buffer.append(PATH_SEPARATOR);
    // buffer.append(datasetId);
    // return buffer.toString();
    // }
    //
    // private String constructAccessPath(String groupDirectoryPath)
    // {
    // StringBuilder buffer = new StringBuilder(localRootPath);
    // buffer.append(PATH_SEPARATOR);
    // buffer.append(localServer);
    // buffer.append(PATH_SEPARATOR);
    // buffer.append(groupDirectoryPath);
    // return buffer.toString();
    // }

    @Override
    public String createVirtualPath(Long groupId)
    {
        return buildRelativePath(groupId).toString();
    }

    @Override
    public String createVirtualPath(Long groupId, Long projectId)
    {
        return buildRelativePath(groupId, projectId).toString();
    }

    @Override
    public String createVirtualPath(Long groupId, Long projectId, Long datasetId)
    {
        return buildRelativePath(groupId, projectId, datasetId).toString();
    }

    @Override
    public String createVirtualPath(Long groupId, Long projectId, Long datasetId, String directory)
    {
        return buildRelativePath(groupId, projectId, datasetId, directory).toString();
    }

    private StringBuilder buildRelativePath(Long groupId)
    {
        StringBuilder buffer = new StringBuilder(PATH_SEPARATOR);
        buffer.append(groupId);
        buffer.append(PATH_SEPARATOR);
        return buffer;
    }

    private StringBuilder buildRelativePath(Long groupId, Long projectId)
    {
        StringBuilder buffer = buildRelativePath(groupId);
        buffer.append(projectId);
        buffer.append(PATH_SEPARATOR);
        return buffer;
    }

    private StringBuilder buildRelativePath(Long groupId, Long projectId, Long datasetId)
    {
        StringBuilder buffer = buildRelativePath(groupId, projectId);
        buffer.append(datasetId);
        buffer.append(PATH_SEPARATOR);
        return buffer;
    }

    private StringBuilder buildRelativePath(Long groupId, Long projectId, Long datasetId, String directory)
    {
        StringBuilder buffer = buildRelativePath(groupId, projectId, datasetId);
        buffer.append(directory);
        buffer.append(PATH_SEPARATOR);
        return buffer;
    }

}
