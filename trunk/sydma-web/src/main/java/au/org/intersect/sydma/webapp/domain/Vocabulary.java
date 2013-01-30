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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;

import au.org.intersect.sydma.webapp.service.IndexingService;

/**
 * Entity to manage
 * 
 * @version $Rev: 29 $
 */
@RooJavaBean
@RooToString(excludeFields = {"researchGroup"})
@RooJson
@RooEntity(persistenceUnit = "sydmaPU", finders = {"findVocabularysByResearchGroup",
        "findVocabularysByResearchGroupAndKeywordEquals", 
        "findVocabularysByResearchGroupAndKeywordEqualsAndIdNotEquals",
        "findVocabularysByResearchGroupAndKeywordLike"})
// TODO CHECKSTYLE-OFF: MagicNumber
public class Vocabulary
{
    @Autowired
    private transient IndexingService indexingService;
    
    @NotNull
    @Pattern(regexp = "\\s*\\S+[\\s\\S]*", message = "Keyword is a required field")
    @Size(max = 255)
    private String keyword;

    @NotNull
    @ManyToOne
    private ResearchGroup researchGroup;

    public void clean()
    {
        if (!keyword.isEmpty())
        {
            keyword = keyword.trim();
        }
    }

    public boolean isDuplicate()
    {
        if (!keyword.isEmpty())
        {
            if (getId() == null)
            {
                return !findVocabularysByResearchGroupAndKeywordEquals(researchGroup, keyword).getResultList()
                        .isEmpty();
            }
            else
            {
                return !findVocabularysByResearchGroupAndKeywordEqualsAndIdNotEquals(researchGroup, keyword, getId())
                        .getResultList().isEmpty();
            }
        }
        return false;
    }

    public static TypedQuery<Vocabulary> findVocabularysByResearchGroup(ResearchGroup researchGroup)
    {
        if (researchGroup == null)
        {
            throw new IllegalArgumentException("The researchGroup argument is required");
        }
        EntityManager em = Vocabulary.entityManager();
        TypedQuery<Vocabulary> q = em.createQuery(
                "SELECT o FROM Vocabulary AS o WHERE o.researchGroup = :researchGroup ORDER BY o.keyword",
                Vocabulary.class);
        q.setParameter("researchGroup", researchGroup);
        return q;
    }

    public static String toSolrText(List<Vocabulary> keywords)
    {
        StringBuffer sb = new StringBuffer();
        for (Vocabulary kw : keywords)
        {
            sb.append(kw.keyword);
            sb.append(' ');
        }
        return sb.toString();
    }
}
