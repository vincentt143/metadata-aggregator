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
import org.antlr.stringtemplate.StringTemplateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import au.org.intersect.sydma.webapp.domain.RdsRequest;
import au.org.intersect.sydma.webapp.util.MailHelper;

/**
 * Service implementation to send email
 * 
 * @version $Rev: 29 $
 */
public class NewRdsRequestMailServiceImpl implements NewRdsRequestMailService
{
    private static final String RDS_REQUEST_MODEL = "rdsRequest";

    /**
     * Template engine
     */
    private static final StringTemplateGroup ST_GROUP = new StringTemplateGroup("rdsRequestEmail");

    @Autowired
    private transient MailHelper mailHelper;

    private String mailFrom;

    private String mailTo;

    private String subject;

    @Required
    public void setMailFrom(String mailFrom)
    {
        this.mailFrom = mailFrom;
    }

    @Required
    public void setMailTo(String mailTo)
    {
        this.mailTo = mailTo;
    }

    @Required
    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    @Override
    public void sendNewRdsRequestEmail(RdsRequest rdsRequest)
    {
        StringTemplate template = ST_GROUP.getInstanceOf("META-INF/email/rdsRequestTemplate");
        template.setAttribute(RDS_REQUEST_MODEL, rdsRequest);
        mailHelper.sendMessage(mailFrom, subject, mailTo, template.toString());
    }

}
