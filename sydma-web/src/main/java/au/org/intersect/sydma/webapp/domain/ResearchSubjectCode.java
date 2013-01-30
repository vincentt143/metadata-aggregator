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

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.TypedQuery;

import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.json.RooJson;

/**
 * Research Subject Code Model
 */
@RooJavaBean
@RooJson
@RooEntity(persistenceUnit = "sydmaPU", finders = {"findResearchSubjectCodesBySubjectCodeEquals"})
public class ResearchSubjectCode
{
    private static final String FIND_BY_SUBJECT_CODE_OR_NAME_LIKE_QUERY =
        "SELECT o FROM ResearchSubjectCode AS o WHERE " 
        + "LOWER(o.subjectCode) LIKE LOWER(:subjectCode)"
        + "OR LOWER (o.subjectName) LIKE LOWER(:subjectName)"
        + "ORDER BY o.subjectCode";
        
    @Id
    private String subjectCode;
    
    private String subjectName;    
    
    public ResearchSubjectCode()
    {
        
    }
    
    public ResearchSubjectCode(String subjectCode, String subjectName)
    {
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
    }

    public String getDisplayName()
    {
        return subjectCode + " - " + subjectName;
    }

    public static TypedQuery<ResearchSubjectCode> findBySubjectCodeOrSubjectNameLike(String term)
    {
        if (term == null || term.length() == 0)
        {
            throw new IllegalArgumentException("The term argument is required");
        }
        String searchTerm = term;
        /*
        if (term.charAt(0) != '%')
        {
            searchTerm = "%" + searchTerm;
        }
        */
        
        if (term.charAt(term.length() - 1) != '%')
        {
            searchTerm = searchTerm + "%";
        }
        

        EntityManager em = ResearchSubjectCode.entityManager();
        TypedQuery<ResearchSubjectCode> q = 
            em.createQuery(FIND_BY_SUBJECT_CODE_OR_NAME_LIKE_QUERY, ResearchSubjectCode.class);
        q.setParameter("subjectCode", searchTerm);
        q.setParameter("subjectName", searchTerm);
        return q;
    }
    
    @Override
    public String toString()
    {
        return subjectCode + " " + subjectName;
    }
}
