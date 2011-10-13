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

package au.org.intersect.sydma.webapp.service;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.stereotype.Service;

import au.org.intersect.sydma.webapp.domain.Role;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.domain.UserType;

/**
 * 
 * 
 * @version $Rev: 29 $
 */
@Service
// TODO CHECKSTYLE-OFF: MagicNumber
public class ExternalUserServiceImpl implements ExternalUserService
{
    @Autowired
    private ExternalUserMailService mailService;

    /**
     * Creating an external user. Generates a one time password given to the user.
     * 
     * @param user
     */
    public void createExternalUser(User user,  String baseUrl)
    {
        String password = generatePassword();
        user.setUsername(user.getEmail());
        user.setEnabled(true);
        user.setUserType(UserType.INTERNAL);
        Role researcher = Role.findRolesByNameEquals("ROLE_RESEARCHER").getSingleResult();
        user.getRoles().add(researcher);
        Md5PasswordEncoder encoder = new Md5PasswordEncoder();
        user.setPassword(encoder.encodePassword(password, null));

        mailService.sendNewExternalUserEmail(user, password, baseUrl);
    }

    /**
     * This method checks if only INTERNAL user email exists. UNIKEY email is ignored.
     * 
     * @param email
     */
    public boolean checkIfEmailExists(String email)
    {
        if (email.isEmpty())
        {
            return false;
        }

        if (User.findUsersByEmailEqualsAndUserType(email, UserType.INTERNAL).getResultList().isEmpty())
        {
            return false;
        }
        return true;
    }

    public void resetExternalUser(User user, String baseUrl)
    {
        String password = generatePassword();
        Md5PasswordEncoder encoder = new Md5PasswordEncoder();
        user.setPassword(encoder.encodePassword(password, null));
        Role active = Role.findRolesByNameEquals("ACTIVE").getSingleResult();
        user.getRoles().remove(active);
        user.merge();

        mailService.sendResetPasswordEmail(user, password, baseUrl);
    }

    private String generatePassword()
    {
        return RandomStringUtils.randomAlphanumeric(10);
    }
}
