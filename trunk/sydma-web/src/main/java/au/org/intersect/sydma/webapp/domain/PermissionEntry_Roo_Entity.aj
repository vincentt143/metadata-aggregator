// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.sydma.webapp.domain;

import au.org.intersect.sydma.webapp.domain.PermissionEntry;
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

privileged aspect PermissionEntry_Roo_Entity {
    
    declare @type: PermissionEntry: @Entity;
    
    @PersistenceContext(unitName = "sydmaPU")
    transient EntityManager PermissionEntry.entityManager;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long PermissionEntry.id;
    
    @Version
    @Column(name = "version")
    private Integer PermissionEntry.version;
    
    public Long PermissionEntry.getId() {
        return this.id;
    }
    
    public void PermissionEntry.setId(Long id) {
        this.id = id;
    }
    
    public Integer PermissionEntry.getVersion() {
        return this.version;
    }
    
    public void PermissionEntry.setVersion(Integer version) {
        this.version = version;
    }
    
    @Transactional("sydmaPU")
    public void PermissionEntry.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional("sydmaPU")
    public void PermissionEntry.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            PermissionEntry attached = PermissionEntry.findPermissionEntry(this.id);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional("sydmaPU")
    public void PermissionEntry.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional("sydmaPU")
    public void PermissionEntry.clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }
    
    @Transactional("sydmaPU")
    public PermissionEntry PermissionEntry.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        PermissionEntry merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
    public static final EntityManager PermissionEntry.entityManager() {
        EntityManager em = new PermissionEntry().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long PermissionEntry.countPermissionEntrys() {
        return entityManager().createQuery("SELECT COUNT(o) FROM PermissionEntry o", Long.class).getSingleResult();
    }
    
    public static List<PermissionEntry> PermissionEntry.findAllPermissionEntrys() {
        return entityManager().createQuery("SELECT o FROM PermissionEntry o", PermissionEntry.class).getResultList();
    }
    
    public static PermissionEntry PermissionEntry.findPermissionEntry(Long id) {
        if (id == null) return null;
        return entityManager().find(PermissionEntry.class, id);
    }
    
    public static List<PermissionEntry> PermissionEntry.findPermissionEntryEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM PermissionEntry o", PermissionEntry.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
}
