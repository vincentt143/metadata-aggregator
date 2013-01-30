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

import org.apache.commons.lang.StringUtils;

import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;

/**
 * Path to a file in a dataset
 *
 * @version $Rev: 29 $
 */
public class FilePath extends AbstractPath
{
    private Long datasetId;
    private Long projectId;
    private Long groupId;
    private String filePath;

    FilePath(final ResearchDataset dataset, String[] filePathTokens)
    {
        datasetId = dataset.getId();
        if (datasetId == null)
        {
            throw new IllegalArgumentException("Non persisted dataset");
        }

        projectId = dataset.getResearchProject().getId();
        groupId = dataset.getResearchProject().getResearchGroup().getId();

        this.filePath = parseFilePath(filePathTokens);

    }

    public FilePath(Long groupId, Long projectId, Long datasetId, String[] filePathTokens)
    {
        this.groupId = groupId;
        this.projectId = projectId;
        this.datasetId = datasetId;
        this.filePath = parseFilePath(filePathTokens);

    }

    private String parseFilePath(String[] filePathTokens)
    {
        StringBuffer fullPath = new StringBuffer(Path.SEPARATOR);
        for (String token : filePathTokens)
        {
            if (StringUtils.isNotEmpty(token))
            {
                fullPath.append(token + Path.SEPARATOR);
            }
        }
        return fullPath.toString();
    }

    @Override
    public String getPath()
    {
        return Path.SEPARATOR + groupId + Path.SEPARATOR + projectId + Path.SEPARATOR + datasetId + filePath;
    }

    @Override
    public Long getGroupId()
    {
        return groupId;
    }

    @Override
    public Long getProjectId()
    {
        return projectId;
    }

    @Override
    public Long getDatasetId()
    {
        return datasetId;
    }

    @Override
    public String getFilePath()
    {
        return filePath;
    }
    
    @Override
    public boolean isFilePath()
    {
        return true;
    }

    @Override
    public String getDisplayName()
    {
        ResearchGroup group = ResearchGroup.findResearchGroup(groupId);
        ResearchProject project = ResearchProject.findResearchProject(projectId);
        ResearchDataset dataset = ResearchDataset.findResearchDataset(datasetId);
        return Path.SEPARATOR + group.getName() + Path.SEPARATOR + project.getName() + Path.SEPARATOR
                + dataset.getName() + filePath;
    }

}
