// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.sydma.webapp.domain;

import au.org.intersect.sydma.webapp.domain.AccessLevel;
import au.org.intersect.sydma.webapp.domain.User;
import java.lang.String;

privileged aspect PermissionEntry_Roo_JavaBean {
    
    public User PermissionEntry.getUser() {
        return this.user;
    }
    
    public void PermissionEntry.setUser(User user) {
        this.user = user;
    }
    
    public String PermissionEntry.getPath() {
        return this.path;
    }
    
    public void PermissionEntry.setPath(String path) {
        this.path = path;
    }
    
    public AccessLevel PermissionEntry.getAccessLevel() {
        return this.accessLevel;
    }
    
    public void PermissionEntry.setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }
    
}