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
import org.springframework.stereotype.Service;

import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;
import au.org.intersect.sydma.webapp.domain.Vocabulary;

/**
 * Service to handle vocabulary of RGs
 * 
 * @version $Rev: 29 $
 */
@Service
public class VocabularyServiceImpl implements VocabularyService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(VocabularyServiceImpl.class);
    
    @Override
    public void editTerm(Long groupId, Vocabulary word)
    {
        word.clean();
        word.merge();
    }
    
    @Override
    public ResearchGroup removeTerm(ResearchGroup researchGroup, Long id)
    {
        Vocabulary word = Vocabulary.findVocabulary(id);
        if (word != null)
        {
            deleteAffectedEntriesFor(researchGroup, word);
            word.remove();
        }
        ResearchGroup.indexResearchGroup(researchGroup);
        return researchGroup;
    }

    @Override
    public ResearchGroup removeVocabulary(ResearchGroup researchGroup)
    {
        deleteAllKeywordsFor(researchGroup);
        
        List<Vocabulary> vocabularyList = Vocabulary.findVocabularysByResearchGroup(researchGroup).getResultList();
        for (Vocabulary vocabulary : vocabularyList)
        {
            vocabulary.remove();
        }
        ResearchGroup.indexResearchGroup(researchGroup);
        return researchGroup;
    }

    private void deleteAffectedEntriesFor(ResearchGroup group, Vocabulary word)
    {
        List<ResearchProject> projects = new ArrayList<ResearchProject>(group.getResearchProjects());
        for (ResearchProject project : projects)
        {
            List<ResearchDataset> datasets = new ArrayList<ResearchDataset>(project.getResearchDatasets());
            for (ResearchDataset dataset : datasets)
            {
                List<Vocabulary> keywords = dataset.getKeywords();
                keywords.remove(word);
                dataset.setKeywords(keywords);
                dataset.merge();
            }
            List<Vocabulary> keywords = project.getKeywords();
            keywords.remove(word);
            project.setKeywords(keywords);
            project.merge();
        }
        List<Vocabulary> keywords = group.getKeywords();
        keywords.remove(word);
        group.setKeywords(keywords);
        group.merge();
    }

    private void deleteAllKeywordsFor(ResearchGroup group)
    {
        List<ResearchProject> projects = new ArrayList<ResearchProject>(group.getResearchProjects());
        for (ResearchProject project : projects)
        {
            List<ResearchDataset> datasets = new ArrayList<ResearchDataset>(project.getResearchDatasets());
            for (ResearchDataset dataset : datasets)
            {
                dataset.setKeywords(new ArrayList<Vocabulary>());
                LOGGER.debug("Dataset Keywords=" + dataset.getKeywords());
                dataset.merge();
            }
            project.setKeywords(new ArrayList<Vocabulary>());
            LOGGER.debug("Project Keywords=" + project.getKeywords());
            project.merge();
        }
        group.setKeywords(new ArrayList<Vocabulary>());
        LOGGER.debug("Group Keywords=" + group.getKeywords());
        group.merge();
    }
}
