// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.sydma.webapp.domain;

import java.lang.String;

privileged aspect ResearchDatabaseQuery_Roo_ToString {
    
    public String ResearchDatabaseQuery.toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Description: ").append(getDescription()).append(", ");
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Name: ").append(getName()).append(", ");
        sb.append("Query: ").append(getQuery()).append(", ");
        sb.append("Version: ").append(getVersion());
        return sb.toString();
    }
    
}