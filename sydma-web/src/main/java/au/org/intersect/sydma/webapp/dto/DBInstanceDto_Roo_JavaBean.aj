// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.sydma.webapp.dto;

import au.org.intersect.sydma.webapp.domain.DBSchema;
import java.lang.Long;
import java.lang.String;

privileged aspect DBInstanceDto_Roo_JavaBean {
    
    public DBSchema DBInstanceDto.getDbSchema() {
        return this.dbSchema;
    }
    
    public void DBInstanceDto.setDbSchema(DBSchema dbSchema) {
        this.dbSchema = dbSchema;
    }
    
    public Long DBInstanceDto.getDatasetId() {
        return this.datasetId;
    }
    
    public void DBInstanceDto.setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }
    
    public String DBInstanceDto.getDescription() {
        return this.description;
    }
    
    public void DBInstanceDto.setDescription(String description) {
        this.description = description;
    }
    
}