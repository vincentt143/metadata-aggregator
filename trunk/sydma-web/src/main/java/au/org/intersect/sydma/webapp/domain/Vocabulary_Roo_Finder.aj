// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.sydma.webapp.domain;

import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.Vocabulary;
import java.lang.Long;
import java.lang.String;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

privileged aspect Vocabulary_Roo_Finder {
    
    public static TypedQuery<Vocabulary> Vocabulary.findVocabularysByResearchGroupAndKeywordEquals(ResearchGroup researchGroup, String keyword) {
        if (researchGroup == null) throw new IllegalArgumentException("The researchGroup argument is required");
        if (keyword == null || keyword.length() == 0) throw new IllegalArgumentException("The keyword argument is required");
        EntityManager em = Vocabulary.entityManager();
        TypedQuery<Vocabulary> q = em.createQuery("SELECT o FROM Vocabulary AS o WHERE o.researchGroup = :researchGroup AND o.keyword = :keyword", Vocabulary.class);
        q.setParameter("researchGroup", researchGroup);
        q.setParameter("keyword", keyword);
        return q;
    }
    
    public static TypedQuery<Vocabulary> Vocabulary.findVocabularysByResearchGroupAndKeywordEqualsAndIdNotEquals(ResearchGroup researchGroup, String keyword, Long id) {
        if (researchGroup == null) throw new IllegalArgumentException("The researchGroup argument is required");
        if (keyword == null || keyword.length() == 0) throw new IllegalArgumentException("The keyword argument is required");
        if (id == null) throw new IllegalArgumentException("The id argument is required");
        EntityManager em = Vocabulary.entityManager();
        TypedQuery<Vocabulary> q = em.createQuery("SELECT o FROM Vocabulary AS o WHERE o.researchGroup = :researchGroup AND o.keyword = :keyword  AND o.id != :id", Vocabulary.class);
        q.setParameter("researchGroup", researchGroup);
        q.setParameter("keyword", keyword);
        q.setParameter("id", id);
        return q;
    }
    
    public static TypedQuery<Vocabulary> Vocabulary.findVocabularysByResearchGroupAndKeywordLike(ResearchGroup researchGroup, String keyword) {
        if (researchGroup == null) throw new IllegalArgumentException("The researchGroup argument is required");
        if (keyword == null || keyword.length() == 0) throw new IllegalArgumentException("The keyword argument is required");
        keyword = keyword.replace('*', '%');
        if (keyword.charAt(0) != '%') {
            keyword = "%" + keyword;
        }
        if (keyword.charAt(keyword.length() - 1) != '%') {
            keyword = keyword + "%";
        }
        EntityManager em = Vocabulary.entityManager();
        TypedQuery<Vocabulary> q = em.createQuery("SELECT o FROM Vocabulary AS o WHERE o.researchGroup = :researchGroup AND LOWER(o.keyword) LIKE LOWER(:keyword)", Vocabulary.class);
        q.setParameter("researchGroup", researchGroup);
        q.setParameter("keyword", keyword);
        return q;
    }
    
}
