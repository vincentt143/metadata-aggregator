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
//TODO CHECKSTYLE-OFF: MultipleStringLiteralsCheck
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cuke4duke.annotation.I18n.EN.Given;
import cuke4duke.annotation.I18n.EN.Then;
import cuke4duke.annotation.I18n.EN.When;
import cuke4duke.spring.StepDefinitions;

import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;
import au.org.intersect.sydma.webapp.domain.User;

@StepDefinitions
public class RouteSteps
{
    private static final Logger LOG = LoggerFactory.getLogger(RouteSteps.class);

    private static String baseUrl = "http://localhost:7675/sydma-web";
    private static Map<String, String> urlMap = new HashMap<String, String>();

    private static final String VIEW_DATASET = "view dataset";
    private static final String ADVERTISE_DATASET = "publish dataset";
    private static final String VIEW_PERMISSION = "view permission";
    
    static
    {
        urlMap.put("base", baseUrl);
        urlMap.put("browse rds", baseUrl + "/home/index");
        urlMap.put(ADVERTISE_DATASET, baseUrl + "/researchdataset/publish/");
        urlMap.put("administrator home", baseUrl + "/admin/index");
        urlMap.put("request rds", baseUrl + "/rds/new");
        urlMap.put("permissions selection", baseUrl + "/permission/index");
        urlMap.put("group permissions", baseUrl + "/permission/index/add?type=group&id=1");
        urlMap.put("edit research group", baseUrl + "/researchgroup/edit/");
        urlMap.put("edit research project", baseUrl + "/researchproject/edit/");
        urlMap.put("create research project", baseUrl + "/researchproject/new/");
        urlMap.put("edit research dataset", baseUrl + "/researchdataset/edit/");
        urlMap.put("create research dataset", baseUrl + "/researchdataset/new/");
        urlMap.put("edit research group including id", baseUrl + "/researchgroup/edit/1");
        urlMap.put("edit research project including id", baseUrl + "/researchproject/edit/1");
        urlMap.put("edit research dataset including id", baseUrl + "/researchdataset/edit/1");
        urlMap.put(VIEW_DATASET, baseUrl + "/managedataset/browse/dataset/");
        urlMap.put("create external user", baseUrl + "/usermanagement/new");
        urlMap.put("view external users", baseUrl + "/usermanagement/list");
        urlMap.put("edit external user", baseUrl + "/usermanagement/edit/");
        urlMap.put("user management", baseUrl + "/usermanagement/index");
        urlMap.put("login", baseUrl + "/login");
        urlMap.put("assign role", baseUrl + "/usermanagement/assignroles");
        urlMap.put("edit roles", baseUrl + "/usermanagement/editroles/");
        urlMap.put("create sydney research data", baseUrl + "/phycol/new");
        urlMap.put("Unapproved RDS requests", baseUrl + "/admin/rds/list?created");
        urlMap.put("view permission", baseUrl + "/permission/view/");
    }

    @Autowired
    // @Qualifier("htmlUnitDriver")
    private WebDriver browser;

    @When("^I am on the ([^\"]*) page$")
    public void iAmOnThePage(String requetedUrl)
    {
        browser.get(urlMap.get(requetedUrl));
    }

    @Given("^I am on the page for dataset \"([^\"]*)\"$")
    public void datasetPage(String datasetName)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();
        browser.get(urlMap.get("publish dataset") + dataset.getId());
    }

    @Given("^I am on the permission page for (group|project|dataset) \"([^\"]*)\"$")
    public void permissionPageForGroupProjectOrDataset(String type, String name)
    {
        Long id = null;
        if ("group".equals(type))
        {
            ResearchGroup group = ResearchGroup.findResearchGroupsByNameEquals(name).getSingleResult();
            id = group.getId();
        }
        else if ("project".equals(type))
        {
            ResearchProject project = ResearchProject.findResearchProjectsByNameEquals(name).getSingleResult();
            id = project.getId();
        }
        else if ("dataset".equals(type))
        {
            ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(name).getSingleResult();
            id = dataset.getId();
        }

        if (id != null)
        {
            browser.get(urlMap.get("permissions selection") + "/add?type=" + type + "&id=" + id);
        }

    }

    @Given("^I am on the page for research group \"([^\"]*)\"$")
    public void researchGroupPage(String researchGroupName)
    {
        ResearchGroup researchGroup = ResearchGroup.findResearchGroupsByNameEquals(researchGroupName).getSingleResult();
        browser.get(urlMap.get("edit research group") + researchGroup.getId());
    }

    @Given("^I am on the create project page for research group \"([^\"]*)\"$")
    public void createProjectPage(String researchGroupName)
    {
        ResearchGroup researchGroup = ResearchGroup.findResearchGroupsByNameEquals(researchGroupName).getSingleResult();
        browser.get(urlMap.get("create research project") + researchGroup.getId());
    }
    
    @When("^I am on the create dataset page for research project \"([^\"]*)\"$")
    public void createDatasetPage(String researchProjectName)
    {
        ResearchProject researchProject = ResearchProject.findResearchProjectsByNameEquals(researchProjectName)
                .getSingleResult();
        browser.get(urlMap.get("create research dataset") + researchProject.getId());
    }
    
    @When("^I am on the edit page for research dataset \"([^\"]*)\"$")
    public void researchDatasetPage(String researchDatasetName)
    {
        ResearchDataset researchDataset = ResearchDataset.findResearchDatasetsByNameEquals(researchDatasetName)
                .getSingleResult();
        browser.get(urlMap.get("edit research dataset") + researchDataset.getId());
    }

    @Given("^I am on the edit page for research project \"([^\"]*)\"$")
    public void researchProjectPage(String researchProjectName)
    {
        ResearchProject researchProject = ResearchProject.findResearchProjectsByNameEquals(researchProjectName)
                .getSingleResult();
        browser.get(urlMap.get("edit research project") + researchProject.getId());
    }

    @Given("^I am on the view dataset page for research dataset \"([^\"]*)\"$")
    public void viewResearchDatasetPage(String researchDatasetName) throws InterruptedException
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(researchDatasetName)
                .getSingleResult();
        browser.get(urlMap.get(VIEW_DATASET) + dataset.getId());
        Thread.sleep(500);
    }

    @Then("^I should be on the view dataset page for research dataset \"([^\"]*)\"$")
    public void iShouldBeOnViewResearchDatasetPage(String researchDatasetName)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(researchDatasetName)
                .getSingleResult();
        String currentUrl = browser.getCurrentUrl();
        String expectedUrl = urlMap.get(VIEW_DATASET) + dataset.getId();
        assertEquals("URLs did not match. Expected " + expectedUrl + ", got " + currentUrl, expectedUrl, currentUrl);
    }

    @Then("^I should be on the advertise dataset page for research dataset \"([^\"]*)\"$")
    public void iShouldBeOnTheAdvertiseDatasetPage(String researchDatasetName)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(researchDatasetName)
                .getSingleResult();
        String currentUrl = browser.getCurrentUrl();
        String expectedUrl = urlMap.get(ADVERTISE_DATASET) + dataset.getId();
        assertEquals("URLs did not match. Expected " + expectedUrl + ", got " + currentUrl, expectedUrl, currentUrl);
    }

    @Then("^I should be on the ([^\"]*) page$")
    public void iShouldBeOnThePage(String expectedPage)
    {
        String expectedUrl = urlMap.get(expectedPage);
        assertNotNull("Url not found in URL map: " + expectedPage, expectedUrl);
        String currentUrl = browser.getCurrentUrl();
        
        String[] urlSection = currentUrl.split("#");
        String urlWithoutHash = urlSection[0];
        
        assertEquals("URLs did not match. Expected " + expectedUrl + ", got " + urlWithoutHash, 
                expectedUrl, urlWithoutHash);
    }

    @Given("^I am on the edit external user page for user \"([^\"]*)\"$")
    public void editExternalUserPage(String externalUser)
    {
        User user = User.findUsersByGivennameEquals(externalUser).getSingleResult();
        browser.get(urlMap.get("edit external user") + user.getId());
    }

    @Given("^I am on the edit roles page for \"([^\"]*)\"$")
    public void editUnikeyUserPage(String unikeyUser)
    {
        User user = User.findUsersByUsernameEquals(unikeyUser).getSingleResult();
        browser.get(urlMap.get("edit roles") + user.getId());
    }
    
    @When("^I am on the view permission page for research group \"([^\"]*)\"$")
    public void researchGroupViewPermission(String researchGroupName)
    {
        ResearchGroup researchGroup = ResearchGroup.findResearchGroupsByNameEquals(researchGroupName)
                .getSingleResult();
        browser.get(urlMap.get("view permission") + researchGroup.getId());
    }
    
    @Then("^I should be on the view permission page for research group \"([^\"]*)\"$")
    public void iShouldBeOnTheViewPermissionPage(String researchGroupName)
    {
        ResearchGroup group = ResearchGroup.findResearchGroupsByNameEquals(researchGroupName)
                .getSingleResult();
        String currentUrl = browser.getCurrentUrl();
        String expectedUrl = urlMap.get(VIEW_PERMISSION) + group.getId();
        assertEquals("URLs did not match. Expected " + expectedUrl + ", got " + currentUrl, expectedUrl, currentUrl);
    }

}
