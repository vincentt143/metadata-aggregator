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
import java.util.Collection;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;
import au.org.intersect.sydma.webapp.domain.Vocabulary;

/**
 * Workaround for the asynchronous issues...
 *
 * @version $Rev: 29 $
 */
@Service
@Transactional(value = "sydmaPU", propagation = Propagation.REQUIRES_NEW, readOnly = true)
public class IndexingServiceImpl implements IndexingService
{
    @Override
    public void indexResearchGroups(Collection<ResearchGroup> researchGroups)
    {
        if (researchGroups.size() > 0)
        {
            Collection<ResearchGroup> refetched = new ArrayList<ResearchGroup>();
            for (ResearchGroup rg : researchGroups)
            {
                refetched.add(ResearchGroup.findResearchGroup(rg.getId()));
            }
            ResearchGroup.indexResearchGroupsAsync(refetched);
        }
    }
    
    @Override
    public void indexResearchProjects(Collection<ResearchProject> researchProjects)
    {
        if (researchProjects.size() > 0)
        {
            Collection<ResearchProject> refetched = new ArrayList<ResearchProject>();
            for (ResearchProject rp : researchProjects)
            {
                refetched.add(ResearchProject.findResearchProject(rp.getId()));
            }
            ResearchProject.indexResearchProjectsAsync(refetched);

        }
    }
    
    @Override
    public void indexResearchDatasets(Collection<ResearchDataset> researchDatasets)
    {
        if (researchDatasets.size() > 0)
        {
            Collection<ResearchDataset> refetched = new ArrayList<ResearchDataset>();
            for (ResearchDataset dt : researchDatasets)
            {
                refetched.add(ResearchDataset.findResearchDataset(dt.getId()));
            }
            ResearchDataset.indexResearchDatasetsAsync(refetched);
        }
    }
    
    @Override
    public void reindexResearchGroupKeyword(ResearchGroup researchGroup, Vocabulary term)
    {
        ResearchGroup refetch = ResearchGroup.findResearchGroup(researchGroup.getId());
        ResearchGroup.reindexResearchGroupForKeywordAsync(refetch, term);
    }
    
    @Override
    public void reindexResearchProjectKeyword(ResearchProject researchProject, Vocabulary term)
    {
        ResearchProject refetch = ResearchProject.findResearchProject(researchProject.getId());
        ResearchProject.reindexResearchProjectForKeywordAsync(refetch, term);
    }
    
    @Override
    public void reindexResearchDatasetKeyword(ResearchDataset researchDataset, Vocabulary term)
    {
        ResearchDataset refetch = ResearchDataset.findResearchDataset(researchDataset.getId());
        ResearchDataset.reindexResearchDatasetForKeywordAsync(refetch, term);
    }
}