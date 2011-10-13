/** Copyright (c) 2011, Intersect, Australia
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Intersect, Intersect's partners, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package au.org.intersect.sydma.webapp.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.TypedQuery;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

import au.org.intersect.sydma.webapp.util.RifCsWriter;
import au.org.intersect.sydma.webapp.validator.DiacriticString;
import au.org.intersect.sydma.webapp.validator.Email;

/**
 * User Model.
 */
@RooJavaBean
@RooToString
@RooJson
@RooEntity(table = "users", finders = {"findUsersByEmailEqualsAndUserType", "findUsersByEmailEquals",
        "findUsersByUsernameEquals", "findUsersByUserTypeAndUsernameEquals", "findUsersByUserType",
        "findUsersByGivennameEquals", "findUsersByUserTypeAndIdEquals"}, persistenceUnit = "sydmaPU")
// TODO CHECKSTYLE-OFF: MagicNumber
// TODO CHECKSTYLE-OFF: MultipleStringLiteralsCheck
public class User
{
    @Size(max = 100)
    private String username;

    @Size(min = 6, max = 100, message = "Password must be greater than 6 characters")
    private String password;

    @DiacriticString(regexp = "[\\p{Alpha}][\\p{Alpha}\\s\\-]*")
    @Size(min = 1, max = 100, message = "Surname is a required field")
    private String surname;

    @Size(min = 1, max = 100, message = "Given name is required field")
    @DiacriticString(regexp = "[\\p{Alpha}][\\p{Alpha}\\s\\-]*")
    private String givenname;

    @Email(message = "Email is not valid")
    @Size(min = 1, max = 150, message = "Email is a required field")
    private String email;

    @Size(max = 100)
    @Pattern(regexp = "([\\p{Alnum}\\s\\-]*)*", message = "Must be alphabetic")
    private String institution;

    private Boolean enabled;
    
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<Role>();

    @Enumerated(EnumType.STRING)
    private UserType userType;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", fetch = FetchType.EAGER)
    private Set<PermissionEntry> permissionEntries = new HashSet<PermissionEntry>();
    
    @OneToMany(mappedBy = "principalInvestigator")
    private Set<ResearchGroup> researchGroups  = new HashSet<ResearchGroup>();
    
    @OneToOne(cascade = CascadeType.ALL)
    private DBUser dbUser;


    /**
     * Return the authorities for a given user
     * 
     * @return
     */
    public Collection<GrantedAuthority> getGrantedAuthorities()
    {
        final Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        for (final Role role : roles)
        {
            grantedAuthorities.add(new GrantedAuthorityImpl(role.getName()));
        }
        return grantedAuthorities;
    }

    public String getKeyForRifCs()
    {
        return this.email;
    }

    /**
     * Creates a UniKey user
     * 
     * @param principal
     * @param givenname
     * @param surname
     * @param email
     */
    public static void createUniKeyUser(String principal, String givenname, String surname, String email)
    {
        User user = new User();
        user.setUserType(UserType.UNIKEY);
        user.setUsername(principal);
        user.setGivenname(givenname);
        user.setSurname(surname);
        user.setEmail(email);
        user.setEnabled(true);
        user.setPassword("unikey_user");
        Role researcher = Role.findRolesByNameEquals("ROLE_RESEARCHER").getSingleResult();
        Role active = Role.findRolesByNameEquals("ACTIVE").getSingleResult();
        user.getRoles().add(researcher);
        user.getRoles().add(active);
        user.merge();
    }

    public void modify(User user)
    {
        this.setUsername(user.getEmail());
        this.setEmail(user.getEmail());
        this.setGivenname(user.getGivenname());
        this.setSurname(user.getSurname());
        this.setInstitution(user.getInstitution());
        this.merge();
    }

    public void activateUser(String password)
    {
        Md5PasswordEncoder encoder = new Md5PasswordEncoder();
        this.setPassword(encoder.encodePassword(password, null));
        Role active = Role.findRolesByNameEquals("ACTIVE").getSingleResult();
        this.getRoles().add(active);
        this.merge();
    }

    public void assignRole(Collection<Role> roles)
    {
        removeAllAssignableRoles();
        this.getRoles().addAll(roles);  
        this.merge();
    }

    public void assignNoRole()
    {
        removeAllAssignableRoles();
        this.merge();
    }

    public void removeAllAssignableRoles()
    {
        List<Role> assignableRoles = Role.findRolesByNameLike("ROLE_").getResultList();
        this.getRoles().removeAll(assignableRoles); 
    }
    
    public void acceptTermsAndConditions()
    {
        Role accept = Role.findRolesByNameEquals("ACCEPTED_TC").getSingleResult();
        this.getRoles().add(accept);
        this.merge();
    }
  
    public boolean hasAcceptedTermsAndConditions()
    {
        Role acceptTC = Role.findRolesByNameEquals("ACCEPTED_TC").getSingleResult();
        for (Role role : this.getRoles())
        {
            if (role.equals(acceptTC))
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasAssignRolePermission()
    {
        Role support = Role.findRolesByNameEquals("ROLE_ICT_SUPPORT").getSingleResult();
        for (Role role : this.getRoles())
        {
            if (role.equals(support))
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean isActive()
    {
        Role active = Role.findRolesByNameEquals("ACTIVE").getSingleResult();
        for (Role role : this.getRoles())
        {
            if (role.equals(active))
            {
                return true;
            }
        }
        return false;
    }

    public String toString()
    {
        return username;
    }

    public boolean isDuplicate()
    {
        if (getId() == null)
        {
            return !findUsersByEmailEquals(email).getResultList().isEmpty();
        }
        else
        {
            return !findUsersWithEmailEqualsAndIdNotEquals(email, getId()).getResultList().isEmpty();
        }
    }

    public static TypedQuery<User> findUsersWithEmailEqualsAndIdNotEquals(String email, Long id)
    {
        if (email == null || email.length() == 0)
        {
            throw new IllegalArgumentException("The email argument is required");
        }
        EntityManager em = User.entityManager();
        String query = "SELECT o FROM User AS o WHERE o.email = :email AND o.id != :id";
        TypedQuery<User> q = em.createQuery(query, User.class);

        q.setParameter("id", id);
        q.setParameter("email", email);
        return q;
    }
    
    public boolean isPrincipalInvestigatorForAnAdvertisedGroup()
    {
        List<ResearchGroup> allResearchGroups = ResearchGroup.findAllResearchGroups();
        boolean found = false;

        for (int i = 0; i < allResearchGroups.size(); i++)
        {
            if (!found && this.getId().equals(allResearchGroups.get(i).getPrincipalInvestigator()
                    .getId()))
            {
                found = !allResearchGroups.get(i).getAdvertisedResearchProjects().isEmpty();
            }
        }

        return found;
    }
    
    public void updatePiRifCs(RifCsWriter rifCsWriter, ResearchGroup currentGroup)
    {
        if (isPrincipalInvestigatorForAnAdvertisedGroup())
        {
            rifCsWriter.writePrincipalInvestigatorRifCs(this, currentGroup);
        }        
    }
    
}
