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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ResearchGroup.class, ResearchProject.class, ResearchDataset.class})
public class ResearchDatasetPathTest
{
    private Path datasetPath;
    private Long groupId = 1L;
    private Long projectId = 2L;
    private Long datasetId = 3L;

    @Before
    public void setUp()
    {
        datasetPath = new ResearchDatasetPath(groupId, projectId, datasetId);
    }

    @Test
    public void testGetDisplayName()
    {
        ResearchGroup group = mock(ResearchGroup.class);
        when(group.getName()).thenReturn("groupName");

        PowerMockito.mockStatic(ResearchGroup.class);
        Mockito.when(ResearchGroup.findResearchGroup(groupId)).thenReturn(group);

        ResearchProject project = mock(ResearchProject.class);
        when(project.getName()).thenReturn("projectName");

        PowerMockito.mockStatic(ResearchProject.class);
        Mockito.when(ResearchProject.findResearchProject(projectId)).thenReturn(project);

        ResearchDataset dataset = mock(ResearchDataset.class);
        when(dataset.getName()).thenReturn("datasetName");

        PowerMockito.mockStatic(ResearchDataset.class);
        Mockito.when(ResearchDataset.findResearchDataset(datasetId)).thenReturn(dataset);

        assertEquals("/groupName/projectName/datasetName/", datasetPath.getDisplayName());
    }

    @Test
    public void getPath()
    {
        assertEquals("/1/2/3/", datasetPath.getPath());
    }

}
