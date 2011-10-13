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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;

import au.org.intersect.sydma.webapp.service.DatasetReadyToPublishMailService;
import au.org.intersect.sydma.webapp.util.RifCsWriter;

/**
 * 
 * 
 * @version $Rev: 29 $
 */
public class ResearchDatasetTest
{

    private static final String NON_PI_EMAIL = "not-pi@intersect.org.au";
    private static final String PI_EMAIL = "email@intersect.org.au";
    private PublicAccessRight right1;
    private PublicAccessRight right2;
    private ResearchDataset dataset;
    private User principalInvestigator;
    private User otherUser;
    private DatasetReadyToPublishMailService mailService;
    private RifCsWriter rifCsWriter;

    @Before
    public void setUp()
    {
        right1 = new PublicAccessRight();
        right2 = new PublicAccessRight();
        dataset = new ResearchDataset();
        ResearchProject researchProject = new ResearchProject();
        ResearchGroup researchGroup = new ResearchGroup();
        principalInvestigator = new User();
        principalInvestigator.setEmail(PI_EMAIL);
        principalInvestigator.setId(1L);
        principalInvestigator.setUsername("pi-username");
        researchGroup.setPrincipalInvestigator(principalInvestigator);
        researchProject.setResearchGroup(researchGroup);
        dataset.setResearchProject(researchProject);
        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail(NON_PI_EMAIL);
        mailService = mock(DatasetReadyToPublishMailService.class);
        rifCsWriter = mock(RifCsWriter.class);

    }

    @Test
    public void testGetKeyForRifCs()
    {
        dataset.setId(123L);
        assertEquals("www.sydney.edu.au-metadata-aggregator-research-dataset-123", dataset.getKeyForRifCs());
    }

    @Test
    public void testAdvertiseANonAdvertisedDatasetMarksIsAsReadyAndSendsEmailIfUserIsNotPI()
    {
        dataset.setPubliciseStatus(PubliciseStatus.NOT_ADVERTISED);

        dataset.advertise("anotheruser", right1, null, mailService, "some url");
        assertEquals(PubliciseStatus.MARKED_AS_READY_FOR_ADVERTISING, dataset.getPubliciseStatus());
        assertEquals(right1, dataset.getPublicAccessRight());
        verify(mailService).sendReadyToPublishEmail(dataset, "anotheruser", "some url");
        verifyZeroInteractions(rifCsWriter);
    }

    @Test
    public void testAdvertiseANonAdvertisedDatasetMarksItAsAdvertisedIfUserIsPI()
    {
        dataset.setPubliciseStatus(PubliciseStatus.NOT_ADVERTISED);

        dataset.advertise("pi-username", right1, rifCsWriter, mailService, "some url");
        assertEquals(PubliciseStatus.ADVERTISED, dataset.getPubliciseStatus());
        assertEquals(right1, dataset.getPublicAccessRight());
        verifyZeroInteractions(mailService);
    }

    @Test
    public void testAdvertiseMarkedAsReadyDatasetMarksItAsAdvertisedAndUpdatesRightsInfoIfUserIsPI()
    {
        dataset.setPubliciseStatus(PubliciseStatus.MARKED_AS_READY_FOR_ADVERTISING);
        dataset.setPublicAccessRight(right2);
        RifCsWriter rifCsWriter = mock(RifCsWriter.class);

        dataset.advertise("pi-username", right1, rifCsWriter, mailService, "some url");
        assertEquals(PubliciseStatus.ADVERTISED, dataset.getPubliciseStatus());
        assertEquals(right1, dataset.getPublicAccessRight());
        verifyZeroInteractions(mailService);
    }

    @Test
    public void testAdvertiseMarkedAsReadyDatasetDoesNothingIfUserIsNotPI()
    {
        dataset.setPubliciseStatus(PubliciseStatus.MARKED_AS_READY_FOR_ADVERTISING);
        dataset.setPublicAccessRight(right2);

        dataset.advertise("anotheruser", right1, rifCsWriter, mailService, "some url");
        assertEquals(PubliciseStatus.MARKED_AS_READY_FOR_ADVERTISING, dataset.getPubliciseStatus());
        assertEquals(right2, dataset.getPublicAccessRight());
        verifyZeroInteractions(mailService);
        verifyZeroInteractions(rifCsWriter);
    }

    @Test
    public void testRejectMarkedAsReadyDatasetMarksAsNotAdvertisedIfPI()
    {
        dataset.setPubliciseStatus(PubliciseStatus.MARKED_AS_READY_FOR_ADVERTISING);
        dataset.setPublicAccessRight(right2);

        assertTrue(dataset.rejectAdvertise("pi-username"));
        assertEquals(PubliciseStatus.NOT_ADVERTISED, dataset.getPubliciseStatus());
        assertEquals(right2, dataset.getPublicAccessRight());
        verifyZeroInteractions(mailService);
        verifyZeroInteractions(rifCsWriter);
    }

    @Test
    public void testRejectMarkedAsReadyDatasetDoesNothingIfNotPI()
    {
        dataset.setPubliciseStatus(PubliciseStatus.MARKED_AS_READY_FOR_ADVERTISING);
        dataset.setPublicAccessRight(right2);

        assertFalse(dataset.rejectAdvertise("anotheruser"));
        assertEquals(PubliciseStatus.MARKED_AS_READY_FOR_ADVERTISING, dataset.getPubliciseStatus());
        assertEquals(right2, dataset.getPublicAccessRight());
        verifyZeroInteractions(mailService);
        verifyZeroInteractions(rifCsWriter);
    }

    @Test
    public void testAdvertiseAlreadyAdvertisedDatasetDoesNothing()
    {
        dataset.setPubliciseStatus(PubliciseStatus.ADVERTISED);
        dataset.setPublicAccessRight(right2);

        dataset.advertise("pi-username", right1, rifCsWriter, mailService, "some url");
        assertEquals(PubliciseStatus.ADVERTISED, dataset.getPubliciseStatus());
        assertEquals(right2, dataset.getPublicAccessRight());
        verifyZeroInteractions(mailService);
        verifyZeroInteractions(rifCsWriter);
    }

    @Test
    public void testCanBeAdvertisedOrRejectedMethods()
    {
        dataset.setPubliciseStatus(PubliciseStatus.NOT_ADVERTISED);
        assertTrue(dataset.canBeAdvertisedBy(principalInvestigator));
        assertFalse(dataset.canBeRejectedBy(principalInvestigator));
        assertTrue(dataset.canBeAdvertisedBy(otherUser));
        assertFalse(dataset.canBeRejectedBy(otherUser));

        dataset.setPubliciseStatus(PubliciseStatus.MARKED_AS_READY_FOR_ADVERTISING);
        assertTrue(dataset.canBeAdvertisedBy(principalInvestigator));
        assertTrue(dataset.canBeRejectedBy(principalInvestigator));
        assertFalse(dataset.canBeAdvertisedBy(otherUser));
        assertFalse(dataset.canBeRejectedBy(otherUser));

        dataset.setPubliciseStatus(PubliciseStatus.ADVERTISED);
        assertFalse(dataset.canBeAdvertisedBy(principalInvestigator));
        assertFalse(dataset.canBeRejectedBy(principalInvestigator));
        assertFalse(dataset.canBeAdvertisedBy(otherUser));
        assertFalse(dataset.canBeRejectedBy(otherUser));
    }
    
    @Test
    public void updateRifCsOnlyUpdatesIfAdvertised()
    {
        ResearchDataset dataset = new ResearchDataset();
        dataset.setPubliciseStatus(PubliciseStatus.ADVERTISED);
        
        dataset.updateRifCsIfNeeded(rifCsWriter);
        verify(rifCsWriter).writeDatasetRifCs(dataset);
        verifyNoMoreInteractions(rifCsWriter);
    }

    @Test
    public void updateRifCsDoesNothingIfNotAdvertised()
    {
        ResearchDataset dataset = new ResearchDataset();
        dataset.setPubliciseStatus(PubliciseStatus.MARKED_AS_READY_FOR_ADVERTISING);
        
        dataset.updateRifCsIfNeeded(rifCsWriter);
        verifyZeroInteractions(rifCsWriter);
    }
    

}
