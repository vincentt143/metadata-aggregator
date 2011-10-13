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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;

/**
 * Provides static methods to build paths in different manners
 *
 * @version $Rev: 29 $
 */
public class PathBuilder
{
    public static Path rootPath()
    {
        return new RootPath();
    }

    public static Path groupPath(final ResearchGroup group)
    {
        return new ResearchGroupPath(group);
    }

    public static Path projectPath(final ResearchProject project)
    {
        return new ResearchProjectPath(project);
    }

    public static Path datasetPath(final ResearchDataset dataset)
    {
        return new ResearchDatasetPath(dataset);        
    }

    public static Path filePath(final ResearchDataset dataset, final String path)
    {
        return new FilePath(dataset, path.split(Path.SEPARATOR));        
    }
    
    public static Path buildFromString(String pathString)
    {
        String[] pathTokens = pathSplit(pathString);
        Long groupId;
        Long projectId;
        Long datasetId;
        Path path;
        //TODO CHECKSTYLE-OFF: MagicNumber
        switch (pathTokens.length)
        {
            case 0:
                path = new RootPath();
                break;
            case 1: 
                groupId = Long.parseLong(pathTokens[0]);
                path = new ResearchGroupPath(groupId);
                break;
            case 2: 
                groupId = Long.parseLong(pathTokens[0]);
                projectId = Long.parseLong(pathTokens[1]);
                path = new ResearchProjectPath(groupId, projectId);
                break;
            case 3: 
                groupId = Long.parseLong(pathTokens[0]);
                projectId = Long.parseLong(pathTokens[1]);
                datasetId = Long.parseLong(pathTokens[2]);
                path = new ResearchDatasetPath(groupId, projectId, datasetId);
                break;
            default:
                groupId = Long.parseLong(pathTokens[0]);
                projectId = Long.parseLong(pathTokens[1]);
                datasetId = Long.parseLong(pathTokens[2]);
                String[] rest = Arrays.copyOfRange(pathTokens, 3, pathTokens.length);
                path = new FilePath(groupId, projectId, datasetId, rest);
                break;
        }
        //TODO CHECKSTYLE-ON: MagicNumber
        return path;
    }
    
    private static String[] pathSplit(String path)
    {
        String[] tokens = path.split(Path.SEPARATOR);
        List<String> results = new ArrayList<String>();
        for (String token : tokens)
        {
            if (StringUtils.isNotBlank(token))
            {
                results.add(token);
            }
        }
        return results.toArray(new String[results.size()]);
    }

}
