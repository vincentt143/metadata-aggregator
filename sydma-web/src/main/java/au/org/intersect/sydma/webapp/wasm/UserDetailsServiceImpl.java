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
package au.org.intersect.sydma.webapp.wasm;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import au.org.intersect.sydma.webapp.domain.User;

/**
 * Get user User Details from the database by user name
 */
public class UserDetailsServiceImpl implements UserDetailsService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    
    @Autowired
    private WASMService wasmService;

    @Override
    public UserDetails loadUserByUsername(final String sKey) throws UsernameNotFoundException// , DataAccessException
    {
        LOGGER.debug("Authenticating sKey: " + sKey);
        WASMAuth auth = wasmService.getAuth(null, sKey, WASMService.SKEY_MODE);
        // WASMAuth auth = dummyAuth();
        if (!auth.isSuccessful() || auth.getPrincipal() == null)
        {
            return new CredentialsExpiredUserDetails();
        }
        
        String principal = auth.getPrincipal();
        String givenname = auth.getGivenname();
        String surname = auth.getSurname();
        String email = auth.getEmail();
        createUnikeyUser(principal, givenname, surname, email);
        User user = User.findUsersByUsernameEquals(principal).getSingleResult();           
        return new AuthenticatedUserDetails(principal, user.getGrantedAuthorities());
    }
    
    private WASMAuth dummyAuth()
    {
    	String[] lines = new String[]{"loginName:ictintersect2",
    			"givenNames:researchers",
    			"surname:intersect",
    			"email:carlos@localhost",
    			"msgID:1",
    			"status:OK",
    			"sKey:skey"};
    	return new WASMAuth(java.util.Arrays.asList(lines),1);
    }

    /**
     * Looks for User in the table, and create one if none there
     * @param principal
     */
    private void createUnikeyUser(String principal, String givenname, String surname, String email)
    {
        if (isUserRegistered(principal))
        {
            LOGGER.info("Principal already registered in the application");
        }
        else
        {
            LOGGER.info("Principal not registered in the application. Creating one: ");
            User.createUniKeyUser(principal, givenname, surname, email);
        }
    }
    
    /**
     * Check if the user is registered
     * @param principal
     * @return
     */
    private boolean isUserRegistered(String principal)
    {
        return !User.findUsersByUsernameEquals(principal).getResultList().isEmpty();
    }
    
    /**
     * Encapsulates user details for UniKey users
     *
     * @version $Rev: 29 $
     */
    class AuthenticatedUserDetails implements UserDetails
    {
        private static final long serialVersionUID = 1L;

        private String finalPrincipal;
        private Collection<GrantedAuthority> grantedAuthorities;
        
        public AuthenticatedUserDetails(final String finalPrincipal, 
                final Collection<GrantedAuthority> grantedAuthorities)
        {
            this.finalPrincipal = finalPrincipal;
            this.grantedAuthorities = grantedAuthorities;
        }
        
        @Override
        public boolean isEnabled()
        {
            return true;
        }
        
        @Override
        public boolean isCredentialsNonExpired()
        {
            return true;
        }
        
        @Override
        public boolean isAccountNonLocked()
        {
            return true;
        }
        
        @Override
        public boolean isAccountNonExpired()
        {
            return true;
        }
        
        @Override
        public String getUsername()
        {
            return finalPrincipal;
        }
        
        @Override
        public String getPassword()
        {
            return "";
        }
        
        @Override
        public Collection<GrantedAuthority> getAuthorities()
        {
            return grantedAuthorities;
        }

    }


    /**
     * Encapsulates user details for failed UniKey users
     *
     * @version $Rev: 29 $
     */
    class CredentialsExpiredUserDetails implements UserDetails
    {
        private static final long serialVersionUID = 1L;

        public CredentialsExpiredUserDetails()
        {
        }
        
        @Override
        public boolean isEnabled()
        {
            return true;
        }
        
        @Override
        public boolean isCredentialsNonExpired()
        {
            return false;
        }
        
        @Override
        public boolean isAccountNonLocked()
        {
            return true;
        }
        
        @Override
        public boolean isAccountNonExpired()
        {
            return true;
        }
        
        @Override
        public String getUsername()
        {
            return "";
        }
        
        @Override
        public String getPassword()
        {
            return "";
        }
        
        @Override
        public Collection<GrantedAuthority> getAuthorities()
        {
            return null;
        }
    }

}
