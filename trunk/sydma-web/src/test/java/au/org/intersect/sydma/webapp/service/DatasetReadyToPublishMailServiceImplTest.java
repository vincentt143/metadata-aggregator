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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.antlr.stringtemplate.StringTemplate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.util.MailHelper;
import au.org.intersect.sydma.webapp.util.MailTemplateLoader;

/**
 * 
 * 
 * @version $Rev: 29 $
 */
// TODO CHECKSTYLE-OFF: ExecutableStatementCount
public class DatasetReadyToPublishMailServiceImplTest
{

    private MailTemplateLoader templateLoader;
    private DatasetReadyToPublishMailServiceImpl mailService;
    private MailHelper mailHelper;

    private String templateString = "Dear $firstName$ $surname$,"
            + "One of your colleagues has indicated that a subset "
            + "of your research data is ready for advertising in Research Data Australia "
            + "(http://services.ands.org.au/home/orca/rda/)." + "Advertising your data raises your research profile, "
            + "increases your Google ranking and promotes your research." + "Research Dataset: $datasetName$"
            + "Research Project: $projectName$" + "Research Group: $groupName$"

            + "Advertising Requested By: $requestedBy$"

            + "You can approve or reject this request through $advertiseUrl$ in My Research Data Manager.";

    @Before
    public void setUp()
    {
        templateLoader = Mockito.mock(MailTemplateLoader.class);
        mailHelper = Mockito.mock(MailHelper.class);
        mailService = new DatasetReadyToPublishMailServiceImpl();
        mailService.setMailTemplateLoader(templateLoader);
        mailService.setMailFrom("from@intersect.org.au");
        mailService.setMailHelper(mailHelper);
    }

    @Test
    public void testSendReadyToPublishEmail()
    {
        User pi = new User();
        pi.setEmail("pi@intersect.org.au");
        pi.setGivenname("Fred");
        pi.setSurname("Smith");

        ResearchGroup group = new ResearchGroup();
        group.setName("My group name");
        group.setPrincipalInvestigator(pi);

        ResearchProject project = new ResearchProject();
        project.setName("My project name");
        project.setResearchGroup(group);

        ResearchDataset dataset = new ResearchDataset();
        dataset.setName("My dataset name");
        dataset.setResearchProject(project);
        dataset.setId(20L);

        // expectations
        StringTemplate template = new StringTemplate(templateString);

        when(templateLoader.loadTemplate("datasetReadyToPublishTemplate")).thenReturn(template);

        mailService.sendReadyToPublishEmail(dataset, "requestedby", "http://baseurl");

        ArgumentCaptor<String> from = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subject = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String[]> to = ArgumentCaptor.forClass(String[].class);
        ArgumentCaptor<String> text = ArgumentCaptor.forClass(String.class);

        verify(mailHelper).sendMessage(from.capture(), subject.capture(), to.capture(), text.capture());
        assertEquals("from@intersect.org.au", from.getValue());
        assertEquals("Research dataset 'My dataset name' is ready for advertising", subject.getValue());
        assertEquals("pi@intersect.org.au", to.getValue()[0]);
        String emailText = text.getValue();

        assertTrue(emailText.contains("Research Dataset: My dataset name"));
        assertTrue(emailText.contains("Research Group: My group name"));
        assertTrue(emailText.contains("Research Project: My project name"));
        assertTrue(emailText.contains("Advertising Requested By: requestedby"));
        assertTrue(emailText.contains("Dear Fred Smith,"));
        assertTrue(emailText
                .contains("One of your colleagues has indicated that a subset of your research data is ready for "
                        + "advertising in Research Data Australia (http://services.ands.org.au/home/orca/rda/)."));
        assertTrue(emailText.contains("Advertising your data raises your research profile, increases your Google "
                + "ranking and promotes your research."));
        assertTrue(emailText.contains("You can approve or reject this request through "
                + "http://baseurl/researchdataset/publish/20 in My Research Data Manager."));
    }

}
