// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.sydma.webapp.domain;

import java.lang.String;

privileged aspect MasterVocabularyTerm_Roo_ToString {
    
    public String MasterVocabularyTerm.toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Keyword: ").append(getKeyword()).append(", ");
        sb.append("Version: ").append(getVersion());
        return sb.toString();
    }
    
}