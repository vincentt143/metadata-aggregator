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
package au.org.intersect.sydma.webapp.util;

import java.util.ArrayList;
import java.util.List;

import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;
import au.org.intersect.sydma.webapp.domain.Vocabulary;

/**
 * Implementation for tokeninput.js plugin
 * 
 * @version $Rev: 29 $
 */
public class TokenInputHelperImpl implements TokenInputHelper
{
    private static final String DELIMITER = ",";
    
    public void setKeywordsForGroup(String keywords, ResearchGroup group)
    {
        if (!keywords.isEmpty())
        {
            List<Vocabulary> result = parseKeywordsToList(keywords, group);
            group.setKeywords(result);
        }
    }

    public void setKeywordsForProject(String keywords, ResearchProject project)
    {
        if (!keywords.isEmpty())
        {
            List<Vocabulary> result = parseKeywordsToList(keywords, project.getResearchGroup());
            project.setKeywords(result);
        }
    }

    public void setKeywordsForDataset(String keywords, ResearchDataset dataset)
    {
        if (!keywords.isEmpty())
        {
            List<Vocabulary> result = parseKeywordsToList(keywords, dataset.getResearchProject().getResearchGroup());
            dataset.setKeywords(result);
        }
    }

    public String buildJsonOnValidationError(String keywords, ResearchGroup group)
    {
        List<Vocabulary> result = new ArrayList<Vocabulary>();
        if (!keywords.isEmpty())
        {
            String[] keywordArray = keywords.split(DELIMITER);
            for (String word : keywordArray)
            {
                Vocabulary keyword = Vocabulary.findVocabularysByResearchGroupAndKeywordEquals(group, word)
                        .getSingleResult();
                result.add(keyword);
            }          
        }
        return appendJson(result);
    }

    public String appendJson(List<Vocabulary> matchResult)
    {
        StringBuffer searchResult = new StringBuffer("[");
        boolean isFirst = true;
        for (Vocabulary keyword : matchResult)
        {
            if (isFirst)
            {
                isFirst = false;
            }
            else
            {
                searchResult.append(DELIMITER);
            }
            searchResult.append("{\"id\":");
            searchResult.append(keyword.getId());
            searchResult.append(",\"name\":\"");
            searchResult.append(keyword.getKeyword());
            searchResult.append("\"}");
        }
        searchResult.append("]");
        return searchResult.toString();
    }

    private List<Vocabulary> parseKeywordsToList(String keywords, ResearchGroup group)
    {
        String[] keywordArray = keywords.split(DELIMITER);
        List<Vocabulary> result = new ArrayList<Vocabulary>();
        for (String word : keywordArray)
        {
            Vocabulary keyword = Vocabulary.findVocabularysByResearchGroupAndKeywordEquals(group, word)
                    .getSingleResult();
            result.add(keyword);
        }
        return result;
    }
}
