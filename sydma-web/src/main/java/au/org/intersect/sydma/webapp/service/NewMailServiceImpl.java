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

import java.util.ArrayList;
import java.util.List;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import au.org.intersect.sydma.webapp.domain.AccessLevel;
import au.org.intersect.sydma.webapp.domain.RdsRequest;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.permission.path.PathBuilder;
import au.org.intersect.sydma.webapp.util.MailHelper;
import au.org.intersect.sydma.webapp.util.MailTemplateLoader;

/**
 * Service implementation to send email
 * 
 * @version $Rev: 29 $
 */
public class NewMailServiceImpl implements NewMailService
{
    private static final String RDS_REQUEST_MODEL = "rdsRequest";
    private static final String DATASET_INSTANCE_MODEL = "dataset";
    private static final String LOGIN_URL = "loginUrl";

    /**
     * Template engine
     */
    private static final StringTemplateGroup ST_GROUP = new StringTemplateGroup("rdsRequestEmail");

    @Autowired
    private transient MailHelper mailHelper;

    @Autowired
    private ApplicationTypeService applicationTypeService;

    @Autowired
    private MailTemplateLoader mailTemplateLoader;

    @Autowired
    private PermissionService permissionService;

    private String mailFrom;

    private String[] mailTo;

    private String subject;

    @Required
    public void setMailFrom(String mailFrom)
    {
        this.mailFrom = mailFrom;
    }

    @Required
    public void setMailTo(String[] mailTo)
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
        StringTemplate template = mailTemplateLoader.loadTemplate("rdsRequestTemplate");
        template.setAttribute(RDS_REQUEST_MODEL, rdsRequest);
        mailHelper.sendMessage(mailFrom, subject, mailTo, template.toString());
    }

    @Override
    public void sendDatabasePasswordChangedEmail(ResearchDataset dataset, String baseUrl)
    {
        StringTemplate template = mailTemplateLoader.loadTemplate("databasePasswordChangeTemplate");
        template.setAttribute(DATASET_INSTANCE_MODEL, dataset);
        List<User> allUsers = User.findAllUsers();
        List<String> mailToUsers = new ArrayList<String>();

        for (User user : allUsers)
        {
            if (permissionService.getLevelForPath(PathBuilder.datasetPath(dataset), user).isAtLeast(
                    AccessLevel.VIEWING_ACCESS))
            {
                mailToUsers.add(user.getEmail());
            }
        }

        setMailTo(mailToUsers.toArray(new String[0]));
        String loginUrl = baseUrl + "/signin";
        template.setAttribute(LOGIN_URL, loginUrl);
        template.setAttribute("project", dataset.getResearchProject().getName());
        template.setAttribute("group", dataset.getResearchProject().getResearchGroup().getName());
        setSubject("Agriculture and Environment Data Manager - Database Password Change");
        mailHelper.sendMessage(mailFrom, subject, mailTo, template.toString());
    }

}
