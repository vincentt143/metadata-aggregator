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

import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.util.MailHelper;

/**
 * 
 *
 * @version $Rev: 29 $
 */
public class DatasetReadyToPublishMailServiceImpl implements DatasetReadyToPublishMailService
{
    private static final StringTemplateGroup ST_GROUP = new StringTemplateGroup("datasetReadyToPublish");

    @Autowired
    private transient MailHelper mailHelper;

    private String mailFrom;

    @Required
    public void setMailFrom(String mailFrom)
    {
        this.mailFrom = mailFrom;
    }

    public void setMailHelper(MailHelper mailHelper)
    {
        this.mailHelper = mailHelper;
    }

    @Override
    public void sendReadyToPublishEmail(ResearchDataset researchDataset, String requestedByUsername, String baseUrl)
    {
        String advertiseUrl = baseUrl + "/researchdataset/publish/" + researchDataset.getId();
        String subject = "Research dataset '" + researchDataset.getName() + "' is ready for advertising";
        User pi = researchDataset.getResearchProject().getResearchGroup().getPrincipalInvestigator();
        String mailTo = pi.getEmail();

        StringTemplate template = ST_GROUP.getInstanceOf("META-INF/email/datasetReadyToPublishTemplate");
        template.setAttribute("datasetName", researchDataset.getName());
        template.setAttribute("projectName", researchDataset.getResearchProject().getName());
        template.setAttribute("groupName", researchDataset.getResearchProject().getResearchGroup().getName());
        template.setAttribute("requestedBy", requestedByUsername);
        template.setAttribute("firstName", pi.getGivenname());
        template.setAttribute("surname", pi.getSurname());
        template.setAttribute("advertiseUrl", advertiseUrl);
        mailHelper.sendMessage(mailFrom, subject, mailTo, template.toString());
    }
}
