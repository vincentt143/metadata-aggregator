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
package au.org.intersect.sydma.cucumber.steps;

//TODO CHECKSTYLE-OFF: ImportOrderCheck

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import cuke4duke.annotation.I18n.EN.Given;
import cuke4duke.annotation.I18n.EN.Then;
import cuke4duke.spring.StepDefinitions;

import au.org.intersect.sydma.webapp.domain.PublicAccessRight;
import au.org.intersect.sydma.webapp.domain.PubliciseStatus;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;

@StepDefinitions
public class ResearchDatasetSteps
{
    @Then("^the dataset \"([^\"]*)\" should be advertised$")
    public void datasetShouldBeAdvertised(String researchDatasetName) throws IOException
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(researchDatasetName)
                .getSingleResult();
        assertEquals(PubliciseStatus.ADVERTISED, dataset.getPubliciseStatus());
    }

    @Then("^the dataset \"([^\"]*)\" should be ready for advertising$")
    public void datasetShouldBeReadyForAdvertising(String researchDatasetName) throws IOException
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(researchDatasetName)
                .getSingleResult();
        assertEquals(PubliciseStatus.MARKED_AS_READY_FOR_ADVERTISING, dataset.getPubliciseStatus());
    }

    @Then("^the dataset \"([^\"]*)\" should be not advertised$")
    public void datasetShouldBeNotAdvertised(String researchDatasetName) throws IOException
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(researchDatasetName)
                .getSingleResult();
        assertEquals(PubliciseStatus.NOT_ADVERTISED, dataset.getPubliciseStatus());
    }

    @Given("^I have public access right option \"([^\"]*)\" with description \"([^\"]*)\"$")
    public void iHavePublicAccessRightOption(String name, String description)
    {
        PublicAccessRight right = new PublicAccessRight();
        right.setShortName(name);
        right.setDescription(description);
        right.merge();
    }

    @Then("^the dataset \"([^\"]*)\" should have public access right \"([^\"]*)\"$")
    public void theDatasetDataset1ShouldHavePublicAccessRight(String researchDatasetName, String rightShortName)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(researchDatasetName)
                .getSingleResult();
        assertEquals(rightShortName, dataset.getPublicAccessRight().getShortName());
    }

    @Then("^dataset \"([^\"]*)\" should have subject code \"([^\"]*)\"$")
    public void datasetShouldHaveSubjectCode(String dsName, String code)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(dsName).getSingleResult();
        assertNotNull(dataset.getSubjectCode());
        assertEquals(code, dataset.getSubjectCode().getSubjectCode());
    }

    @Then("^dataset \"([^\"]*)\" should have physical location \"([^\"]*)\"$")
    public void datasetShouldHavePhysicalLocation(String dsName, String buildingName)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(dsName).getSingleResult();
        assertEquals(buildingName, dataset.getPhysicalLocation().getBuildingName());
    }

    @Then("^dataset \"([^\"]*)\" should have no physical location$")
    public void datasetShouldHaveNoPhysicalLocation(String dsName)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(dsName).getSingleResult();
        assertNull(dataset.getPhysicalLocation());
    }
    
    @Then("^dataset \"([^\"]*)\" should have additional location information \"([^\"]*)\"$")
    public void datasetShouldHaveAdditionalLocationInformation(String dsName, String additionalInfo)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(dsName).getSingleResult();
        assertEquals(additionalInfo, dataset.getAdditionalLocationInformation());
    }

    @Then("^dataset \"([^\"]*)\" should have description \"([^\"]*)\"$")
    public void datasetShouldHaveDescription(String dsName, String desc)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(dsName).getSingleResult();
        assertEquals(desc, dataset.getDescription());
    }
}
