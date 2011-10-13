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
package au.org.intersect.sydma.webapp.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import au.org.intersect.sydma.webapp.util.RifCsWriter;

/**
 * 
 * 
 * @version $Rev: 29 $
 */
public class ResearchProjectTest
{

    private RifCsWriter rifCsWriter = mock(RifCsWriter.class);

    @Test
    public void testGetKeyForRifCs()
    {
        ResearchProject project = new ResearchProject();
        project.setId(1234L);
        assertEquals("www.sydney.edu.au-metadata-aggregator-research-project-1234", project.getKeyForRifCs());
    }

    @Test
    public void testGetOriginatingSourceReturnsTheUrlOrSydneyIfNoUrl()
    {
        ResearchProject projectWithUrl = new ResearchProject();
        projectWithUrl.setUrl("http://project.com");
        assertEquals("http://project.com", projectWithUrl.getOriginatingSource());
        assertEquals("www.sydney.edu.au", new ResearchProject().getOriginatingSource());
    }

    @Test
    public void testGetAdvertisedResearchDatasets()
    {
        ResearchDataset dataset1 = new ResearchDataset();
        dataset1.setPubliciseStatus(PubliciseStatus.ADVERTISED);
        ResearchDataset dataset2 = new ResearchDataset();
        dataset2.setPubliciseStatus(PubliciseStatus.NOT_ADVERTISED);

        ResearchProject projectWithOne = createProject(dataset1, dataset2);
        ResearchProject projectWithNone = createProject(dataset2);
        ResearchProject emptyProject = createProject();

        assertEquals(1, projectWithOne.getAdvertisedResearchDatasets().size());
        assertTrue(projectWithOne.hasAdvertisedDatasets());
        assertEquals(dataset1, projectWithOne.getAdvertisedResearchDatasets().get(0));

        assertEquals(0, projectWithNone.getAdvertisedResearchDatasets().size());
        assertFalse(projectWithNone.hasAdvertisedDatasets());

        assertEquals(0, emptyProject.getAdvertisedResearchDatasets().size());
        assertFalse(emptyProject.hasAdvertisedDatasets());
    }

    @Test
    public void updateRifCsOnlyUpdatesIfProjectHasAdvertisedDatasets()
    {
        ResearchDataset dataset = new ResearchDataset();
        dataset.setPubliciseStatus(PubliciseStatus.ADVERTISED);
        ResearchProject project = createProject(dataset);
        
        project.updateRifCsIfNeeded(rifCsWriter);
        verify(rifCsWriter).writeProjectRifCs(project);
        verifyNoMoreInteractions(rifCsWriter);
    }

    @Test
    public void updateRifCsDoesNothingIfProjectHasNoAdvertisedDatasets()
    {
        ResearchDataset dataset = new ResearchDataset();
        dataset.setPubliciseStatus(PubliciseStatus.MARKED_AS_READY_FOR_ADVERTISING);
        ResearchProject project = createProject(dataset);
        
        project.updateRifCsIfNeeded(rifCsWriter);
        verifyZeroInteractions(rifCsWriter);
    }
    
    private ResearchProject createProject(ResearchDataset... datasets)
    {
        ResearchProject project = new ResearchProject();
        Set<ResearchDataset> datasetSet = new HashSet<ResearchDataset>();
        datasetSet.addAll(Arrays.asList(datasets));

        project.setResearchDatasets(datasetSet);

        return project;

    }
}
