// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.sydma.webapp.domain;

import java.lang.String;

privileged aspect ResearchGroup_Roo_ToString {
    
    public String ResearchGroup.toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DataManagementContact: ").append(getDataManagementContact()).append(", ");
        sb.append("Description: ").append(getDescription()).append(", ");
        sb.append("DirectoryPath: ").append(getDirectoryPath()).append(", ");
        sb.append("HasAdvertisedProjects: ").append(getHasAdvertisedProjects()).append(", ");
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("IsPhysical: ").append(getIsPhysical()).append(", ");
        sb.append("KeyForRifCs: ").append(getKeyForRifCs()).append(", ");
        sb.append("Keywords: ").append(getKeywords() == null ? "null" : getKeywords().size()).append(", ");
        sb.append("Name: ").append(getName()).append(", ");
        sb.append("OriginatingSource: ").append(getOriginatingSource()).append(", ");
        sb.append("PrincipalInvestigator: ").append(getPrincipalInvestigator()).append(", ");
        sb.append("ResearchProjects: ").append(getResearchProjects() == null ? "null" : getResearchProjects().size()).append(", ");
        sb.append("SubjectCode: ").append(getSubjectCode()).append(", ");
        sb.append("SubjectCode2: ").append(getSubjectCode2()).append(", ");
        sb.append("SubjectCode3: ").append(getSubjectCode3()).append(", ");
        sb.append("Url: ").append(getUrl()).append(", ");
        sb.append("Version: ").append(getVersion());
        return sb.toString();
    }
    
}