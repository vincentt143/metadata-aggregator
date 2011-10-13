// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.sydma.webapp.domain;

import au.org.intersect.sydma.webapp.domain.RdsRequestStatus;
import au.org.intersect.sydma.webapp.domain.ResearchSubjectCode;
import au.org.intersect.sydma.webapp.domain.User;
import java.lang.Integer;
import java.lang.String;

privileged aspect RdsRequest_Roo_JavaBean {
    
    public String RdsRequest.getName() {
        return this.name;
    }
    
    public void RdsRequest.setName(String name) {
        this.name = name;
    }
    
    public ResearchSubjectCode RdsRequest.getSubjectCode() {
        return this.subjectCode;
    }
    
    public void RdsRequest.setSubjectCode(ResearchSubjectCode subjectCode) {
        this.subjectCode = subjectCode;
    }
    
    public ResearchSubjectCode RdsRequest.getSubjectCode2() {
        return this.subjectCode2;
    }
    
    public void RdsRequest.setSubjectCode2(ResearchSubjectCode subjectCode2) {
        this.subjectCode2 = subjectCode2;
    }
    
    public ResearchSubjectCode RdsRequest.getSubjectCode3() {
        return this.subjectCode3;
    }
    
    public void RdsRequest.setSubjectCode3(ResearchSubjectCode subjectCode3) {
        this.subjectCode3 = subjectCode3;
    }
    
    public User RdsRequest.getPrincipalInvestigator() {
        return this.principalInvestigator;
    }
    
    public void RdsRequest.setPrincipalInvestigator(User principalInvestigator) {
        this.principalInvestigator = principalInvestigator;
    }
    
    public Integer RdsRequest.getAmountOfStorage() {
        return this.amountOfStorage;
    }
    
    public void RdsRequest.setAmountOfStorage(Integer amountOfStorage) {
        this.amountOfStorage = amountOfStorage;
    }
    
    public String RdsRequest.getDescription() {
        return this.description;
    }
    
    public void RdsRequest.setDescription(String description) {
        this.description = description;
    }
    
    public User RdsRequest.getDataManagementContact() {
        return this.dataManagementContact;
    }
    
    public void RdsRequest.setDataManagementContact(User dataManagementContact) {
        this.dataManagementContact = dataManagementContact;
    }
    
    public RdsRequestStatus RdsRequest.getRequestStatus() {
        return this.requestStatus;
    }
    
    public void RdsRequest.setRequestStatus(RdsRequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }
    
}
