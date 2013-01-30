// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.sydma.webapp.domain;

import au.org.intersect.sydma.webapp.domain.Vocabulary;
import java.lang.Integer;
import java.lang.Long;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Version;
import org.springframework.transaction.annotation.Transactional;

privileged aspect Vocabulary_Roo_Entity {
    
    declare @type: Vocabulary: @Entity;
    
    @PersistenceContext(unitName = "sydmaPU")
    transient EntityManager Vocabulary.entityManager;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long Vocabulary.id;
    
    @Version
    @Column(name = "version")
    private Integer Vocabulary.version;
    
    public Long Vocabulary.getId() {
        return this.id;
    }
    
    public void Vocabulary.setId(Long id) {
        this.id = id;
    }
    
    public Integer Vocabulary.getVersion() {
        return this.version;
    }
    
    public void Vocabulary.setVersion(Integer version) {
        this.version = version;
    }
    
    @Transactional("sydmaPU")
    public void Vocabulary.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional("sydmaPU")
    public void Vocabulary.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            Vocabulary attached = Vocabulary.findVocabulary(this.id);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional("sydmaPU")
    public void Vocabulary.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional("sydmaPU")
    public void Vocabulary.clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }
    
    @Transactional("sydmaPU")
    public Vocabulary Vocabulary.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Vocabulary merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
    public static final EntityManager Vocabulary.entityManager() {
        EntityManager em = new Vocabulary().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long Vocabulary.countVocabularys() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Vocabulary o", Long.class).getSingleResult();
    }
    
    public static List<Vocabulary> Vocabulary.findAllVocabularys() {
        return entityManager().createQuery("SELECT o FROM Vocabulary o", Vocabulary.class).getResultList();
    }
    
    public static Vocabulary Vocabulary.findVocabulary(Long id) {
        if (id == null) return null;
        return entityManager().find(Vocabulary.class, id);
    }
    
    public static List<Vocabulary> Vocabulary.findVocabularyEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Vocabulary o", Vocabulary.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
}
