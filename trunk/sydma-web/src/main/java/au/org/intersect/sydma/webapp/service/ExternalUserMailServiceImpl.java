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

import org.antlr.stringtemplate.StringTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.util.MailHelper;
import au.org.intersect.sydma.webapp.util.MailTemplateLoader;

/**
 * Service implementation to send email
 *
 * @version $Rev: 29 $
 */
public class ExternalUserMailServiceImpl implements ExternalUserMailService
{
    private static final String EXTERNAL_USER_MODEL = "externalUser";

    @Autowired
    private MailTemplateLoader mailTemplateLoader;
    
    @Autowired
    private transient MailHelper mailHelper;
    
    private String mailFrom;    
    
    private String createExternalUserSubject;
    
    private String resetPasswordSubject;
    
    @Required
    public void setMailFrom(String mailFrom)
    {
        this.mailFrom = mailFrom;
    }
    

    @Required
    public void setCreateExternalUserSubject(String createExternalUserSubject)
    {
        this.createExternalUserSubject = createExternalUserSubject;
    }

    @Required
    public void setResetPasswordSubject(String resetPasswordSubject)
    {
        this.resetPasswordSubject = resetPasswordSubject;
    }

    @Override
    public void sendNewExternalUserEmail(User user, String password, String baseUrl)
    {
        String loginUrl = baseUrl + "/login";
        StringTemplate template = mailTemplateLoader.loadTemplate("externalUserTemplate");
        template.setAttribute(EXTERNAL_USER_MODEL, user);
        template.setAttribute("password", password);
        template.setAttribute("loginUrl", loginUrl);
        String[] mailTo = new String[]{user.getEmail()};    
        mailHelper.sendMessage(mailFrom, createExternalUserSubject, mailTo, template.toString());
    }
    
    @Override
    public void sendResetPasswordEmail(User user, String password, String baseUrl)
    {
        String loginUrl = baseUrl + "/login";
        StringTemplate template = mailTemplateLoader.loadTemplate("resetPasswordTemplate");
        template.setAttribute(EXTERNAL_USER_MODEL, user);
        template.setAttribute("password", password);
        template.setAttribute("loginUrl", loginUrl);
        String[] mailTo = new String[]{user.getEmail()};
        mailHelper.sendMessage(mailFrom, resetPasswordSubject, mailTo, template.toString());
    }
}
