// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.sydma.webapp.domain;

import au.org.intersect.sydma.webapp.domain.DBBackup;
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

privileged aspect DBBackup_Roo_Entity {
    
    declare @type: DBBackup: @Entity;
    
    @PersistenceContext(unitName = "sydmaPU")
    transient EntityManager DBBackup.entityManager;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long DBBackup.id;
    
    @Version
    @Column(name = "version")
    private Integer DBBackup.version;
    
    public Long DBBackup.getId() {
        return this.id;
    }
    
    public void DBBackup.setId(Long id) {
        this.id = id;
    }
    
    public Integer DBBackup.getVersion() {
        return this.version;
    }
    
    public void DBBackup.setVersion(Integer version) {
        this.version = version;
    }
    
    @Transactional("sydmaPU")
    public void DBBackup.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional("sydmaPU")
    public void DBBackup.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            DBBackup attached = DBBackup.findDBBackup(this.id);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional("sydmaPU")
    public void DBBackup.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional("sydmaPU")
    public void DBBackup.clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }
    
    @Transactional("sydmaPU")
    public DBBackup DBBackup.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        DBBackup merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
    public static final EntityManager DBBackup.entityManager() {
        EntityManager em = new DBBackup().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long DBBackup.countDBBackups() {
        return entityManager().createQuery("SELECT COUNT(o) FROM DBBackup o", Long.class).getSingleResult();
    }
    
    public static List<DBBackup> DBBackup.findAllDBBackups() {
        return entityManager().createQuery("SELECT o FROM DBBackup o", DBBackup.class).getResultList();
    }
    
    public static DBBackup DBBackup.findDBBackup(Long id) {
        if (id == null) return null;
        return entityManager().find(DBBackup.class, id);
    }
    
    public static List<DBBackup> DBBackup.findDBBackupEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM DBBackup o", DBBackup.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
}
