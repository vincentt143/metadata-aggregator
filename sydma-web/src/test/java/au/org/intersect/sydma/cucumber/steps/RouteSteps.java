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
//TODO CHECKSTYLE-OFF: ExecutableStatementCountCheck
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import cuke4duke.annotation.I18n.EN.Given;
import cuke4duke.annotation.I18n.EN.Then;
import cuke4duke.annotation.I18n.EN.When;
import cuke4duke.spring.StepDefinitions;

import au.org.intersect.sydma.webapp.domain.PermissionEntry;
import au.org.intersect.sydma.webapp.domain.RdsRequest;
import au.org.intersect.sydma.webapp.domain.ResearchDatabaseQuery;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.permission.path.PathBuilder;

@StepDefinitions
public class RouteSteps
{
    private static final Logger LOG = LoggerFactory.getLogger(RouteSteps.class);

    private static String baseUrl = "http://localhost:<PORT>/sydma-web";
    private static Map<String, String> urlMap = new HashMap<String, String>();

    private static final String VIEW_DATASET = "view dataset";
    private static final String ADVERTISE_DATASET = "publish dataset";
    private static final String VIEW_PERMISSION = "view permission";

    // TODO CHECKSTYLE-OFF: JavaNCSSCheck
    static
    {
        urlMap.put("base", baseUrl);
        urlMap.put("browse rds", baseUrl + "/home/index");
        urlMap.put(ADVERTISE_DATASET, baseUrl + "/researchdataset/publish/");
        urlMap.put("administrator home", baseUrl + "/admin/index");
        urlMap.put("request rds", baseUrl + "/rds/new");
        urlMap.put("rds approve", baseUrl + "/admin/rds/show/");
        urlMap.put("permissions selection", baseUrl + "/permission/new");
        urlMap.put("activity log", baseUrl + "/permission/activityLog?id=1");
        urlMap.put("group permissions", baseUrl + "/permission/new?type=group&id=1");
        urlMap.put("project permissions", baseUrl + "/permission/new?type=project&id=1");
        urlMap.put("dataset permissions", baseUrl + "/permission/new?type=dataset&id=1");
        urlMap.put("directory permissions", baseUrl
                + "/permission/new?path=%2F1%2F1%2F1%2FTest_Directory%2F&type=directory");
        urlMap.put("edit research group", baseUrl + "/researchgroup/edit/");
        urlMap.put("view research group", baseUrl + "/researchgroup/view/");
        urlMap.put("edit research project", baseUrl + "/researchproject/edit/");
        urlMap.put("view research project", baseUrl + "/researchproject/view/");
        urlMap.put("create research project", baseUrl + "/researchproject/new/");
        urlMap.put("edit research dataset", baseUrl + "/researchdataset/edit/");
        urlMap.put("view research dataset", baseUrl + "/researchdataset/view/");
        urlMap.put("create research dataset", baseUrl + "/researchdataset/new/");
        urlMap.put("edit research group including id", baseUrl + "/researchgroup/edit/1");
        urlMap.put("edit research project including id", baseUrl + "/researchproject/edit/1");
        urlMap.put("edit research dataset including id", baseUrl + "/researchdataset/edit/1");
        urlMap.put(VIEW_DATASET, baseUrl + "/managedataset/browse/dataset/");
        urlMap.put("user management", baseUrl + "/usermanagement/index");
        urlMap.put("login", baseUrl + "/login");
        urlMap.put("assign role", baseUrl + "/usermanagement/list");
        urlMap.put("edit roles", baseUrl + "/usermanagement/edit/");
        urlMap.put("create sydney research data", baseUrl + "/phycol/new");
        urlMap.put("Unapproved RDS requests", baseUrl + "/admin/rds/list?created");
        urlMap.put("Approved RDS requests", baseUrl + "/admin/rds/list?approved");
        urlMap.put("view permission", baseUrl + "/permission/view/");
        urlMap.put("delete permission", baseUrl + "/permission/delete");
        urlMap.put("create db instance", baseUrl + "/dbinstance/create?datasetId=");
        urlMap.put("view db instance", baseUrl + "/dbinstance/view?datasetId=");
        urlMap.put("reverse engineer", baseUrl + "/dbinstance/reverse?datasetId=");
        urlMap.put("delete db instance", baseUrl + "/dbinstance/delete?datasetId=");
        urlMap.put("create sql", baseUrl + "/dbinstance/query?datasetId=");
        urlMap.put("show sql", baseUrl + "/dbinstance/show/");
        urlMap.put("edit sql", baseUrl + "/dbinstance/editQuery/");
        urlMap.put("delete sql", baseUrl + "/dbinstance/deleteQuery/");
        urlMap.put("create annotation", baseUrl + "/fileannotation/create");
        urlMap.put("delete annotation", baseUrl + "/fileannotation/delete");
        urlMap.put("manage backup", baseUrl + "/dbinstance/manageBackup?datasetId=");
        urlMap.put("create backup", baseUrl + "/dbinstance/createBackup?datasetId=");
        urlMap.put("confirm restore", baseUrl + "/dbinstance/confirmRestore?id=");
        urlMap.put("manage vocabulary", baseUrl + "/researchgroup/managevocabulary/");
        urlMap.put("create vocabulary", baseUrl + "/researchgroup/createvocabulary/");
        urlMap.put("delete vocabulary", baseUrl + "/researchgroup/deletevocabulary?id=");
    }

    @Autowired
    // @Qualifier("htmlUnitDriver")
    private WebDriver browser;

    @Value("#{cucumber[cucumber_tomcat_port]}")
    private Integer tomcatPort;

    private String urlMapGet(String key)
    {
        return urlMap.get(key).replace("<PORT>", tomcatPort.toString());
    }

    @When("^I am on the ([^\"]*) page$")
    public void iAmOnThePage(String requetedUrl)
    {
        browser.get(urlMapGet(requetedUrl));
    }

    @Given("^I am on the page for dataset \"([^\"]*)\"$")
    public void datasetPage(String datasetName)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();
        browser.get(urlMapGet("publish dataset") + dataset.getId());
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
            browser.get(urlMapGet("permissions selection") + "?type=" + type + "&id=" + id);
        }

    }

    @Given("^I am on the page for research group \"([^\"]*)\"$")
    public void researchGroupPage(String researchGroupName)
    {
        ResearchGroup researchGroup = ResearchGroup.findResearchGroupsByNameEquals(researchGroupName)
            .getSingleResult();
        browser.get(urlMapGet("edit research group") + researchGroup.getId());
    }

    @Given("^I should be on the view group page for \"([^\"]*)\"$")
    public void shouldBeOnViewGroupPage(String researchGroup)
    {
        ResearchGroup group = ResearchGroup.findResearchGroupsByNameEquals(researchGroup).getSingleResult();
        String currentUrl = browser.getCurrentUrl();
        String expectedUrl = urlMapGet("view research group") + group.getId();
        assertEquals("URLs did not match. Expected " + expectedUrl + ", got " + currentUrl, expectedUrl, currentUrl);
    }
    
    @Given("^I am on the view project page for \"([^\"]*)\"$")
    public void shouldBeOnViewProjectPage(String researchProject)
    {
        ResearchProject project = ResearchProject.findResearchProjectsByNameEquals(researchProject).getSingleResult();
        browser.get(urlMapGet("view research project") + project.getId());
    }

    @Given("^I am on the view dataset page for \"([^\"]*)\"$")
    public void shouldBeOnViewDatasetPage(String researchDataset)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(researchDataset).getSingleResult();
        browser.get(urlMapGet("view research dataset") + dataset.getId());
    }

    @Given("^I am on the view group page for \"([^\"]*)\"$")
    public void iAmOnTheViewGroupPage(String groupName)
    {
        ResearchGroup group = ResearchGroup.findResearchGroupsByNameEquals(groupName).getSingleResult();
        browser.get(urlMapGet("view research group") + group.getId());
    }

    @Given("^I am on the approve request page for research group \"([^\"]*)\"$")
    public void approveRequestPage(String researchGroupName)
    {
        RdsRequest request = RdsRequest.findRdsRequestsByNameEquals(researchGroupName).getSingleResult();
        browser.get(urlMapGet("rds approve") + request.getId());
    }

    @Given("^I am on the create project page for research group \"([^\"]*)\"$")
    public void createProjectPage(String researchGroupName)
    {
        ResearchGroup researchGroup = ResearchGroup.findResearchGroupsByNameEquals(researchGroupName).getSingleResult();
        browser.get(urlMapGet("create research project") + researchGroup.getId());
    }

    @When("^I am on the create dataset page for research project \"([^\"]*)\"$")
    public void createDatasetPage(String researchProjectName)
    {
        ResearchProject researchProject = ResearchProject.findResearchProjectsByNameEquals(researchProjectName)
                .getSingleResult();
        browser.get(urlMapGet("create research dataset") + researchProject.getId());
    }

    @When("^I am on the edit page for research dataset \"([^\"]*)\"$")
    public void researchDatasetPage(String researchDatasetName)
    {
        ResearchDataset researchDataset = ResearchDataset.findResearchDatasetsByNameEquals(researchDatasetName)
                .getSingleResult();
        browser.get(urlMapGet("edit research dataset") + researchDataset.getId());
    }

    @Given("^I am on the edit page for research project \"([^\"]*)\"$")
    public void researchProjectPage(String researchProjectName)
    {
        ResearchProject researchProject = ResearchProject.findResearchProjectsByNameEquals(researchProjectName)
                .getSingleResult();
        browser.get(urlMapGet("edit research project") + researchProject.getId());
    }

    @Given("^I am on the view dataset page for research dataset \"([^\"]*)\"$")
    public void viewResearchDatasetPage(String researchDatasetName) throws InterruptedException
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(researchDatasetName)
                .getSingleResult();
        browser.get(urlMapGet(VIEW_DATASET) + dataset.getId());
        Thread.sleep(500);
    }

    @Then("^I should be on the view dataset page for research dataset \"([^\"]*)\"$")
    public void iShouldBeOnViewResearchDatasetPage(String researchDatasetName)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(researchDatasetName)
                .getSingleResult();
        String currentUrl = browser.getCurrentUrl();
        String expectedUrl = urlMapGet(VIEW_DATASET) + dataset.getId();
        assertEquals("URLs did not match. Expected " + expectedUrl + ", got " + currentUrl, expectedUrl, currentUrl);
    }

    @Then("^I should be on the advertise dataset page for research dataset \"([^\"]*)\"$")
    public void iShouldBeOnTheAdvertiseDatasetPage(String researchDatasetName)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(researchDatasetName)
                .getSingleResult();
        String currentUrl = browser.getCurrentUrl();
        String expectedUrl = urlMapGet(ADVERTISE_DATASET) + dataset.getId();
        assertEquals("URLs did not match. Expected " + expectedUrl + ", got " + currentUrl, expectedUrl, currentUrl);
    }

    @Then("^I am on the advertise dataset page for research dataset \"([^\"]*)\"$")
    public void iAmOnTheAdvertiseDatasetPage(String researchDatasetName)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(researchDatasetName)
                .getSingleResult();
        browser.get(urlMapGet(ADVERTISE_DATASET) + dataset.getId());
    }
    
    @Then("I am on the manage vocabulary page for research group \"([^\"]*)\"$")
    public void iAmOnTheManageVocabularyPage(String groupName)
    {
        ResearchGroup group = ResearchGroup.findResearchGroupsByNameEquals(groupName).getSingleResult();
        browser.get(urlMapGet("manage vocabulary") + group.getId() + "?browse=true");
    }
    
    @Then("I should be on the manage vocabulary page for \"([^\"]*)\"$")
    public void iShouldBeOnTheManageVocabularyPage(String groupName)
    {
        ResearchGroup group = ResearchGroup.findResearchGroupsByNameEquals(groupName).getSingleResult();     
        String expectedUrl = urlMapGet("manage vocabulary") + group.getId() + "?browse=true";
        String currentUrl = browser.getCurrentUrl();
        assertEquals("URLs did not match. Expected " + expectedUrl + ", got " + currentUrl, expectedUrl, currentUrl);
    }    
    
    @Then("I should be on the create vocabulary page for \"([^\"]*)\"$")
    public void iShouldBeOnTheVocabularyPage(String groupName)
    {
        ResearchGroup group = ResearchGroup.findResearchGroupsByNameEquals(groupName).getSingleResult();     
        String expectedUrl = urlMapGet("create vocabulary") + group.getId() + "/?browse=true";
        String currentUrl = browser.getCurrentUrl();
        assertEquals("URLs did not match. Expected " + expectedUrl + ", got " + currentUrl, expectedUrl, currentUrl);
    }
    
    @Then("I should be on the delete vocabulary page for \"([^\"]*)\"$")
    public void iShouldBeOnTheDeleteVocabularyPage(String groupName)
    {
        ResearchGroup group = ResearchGroup.findResearchGroupsByNameEquals(groupName).getSingleResult();     
        String expectedUrl = urlMapGet("delete vocabulary") + group.getId();
        String currentUrl = browser.getCurrentUrl();
        assertEquals("URLs did not match. Expected " + expectedUrl + ", got " + currentUrl, expectedUrl, currentUrl);
    }    
    
    
    @Then("^I should be on the ([^\"]*) page$")
    public void iShouldBeOnThePage(String expectedPage)
    {
        String expectedUrl = urlMapGet(expectedPage);
        assertNotNull("Url not found in URL map: " + expectedPage, expectedUrl);
        String currentUrl = browser.getCurrentUrl();

        String[] urlSection = currentUrl.split("#");
        String urlWithoutHash = urlSection[0];

        assertEquals("URLs did not match. Expected " + expectedUrl + ", got " + urlWithoutHash, expectedUrl,
                urlWithoutHash);
    }

    @Given("^I am on the edit external user page for user \"([^\"]*)\"$")
    public void editExternalUserPage(String externalUser)
    {
        User user = User.findUsersByGivennameEquals(externalUser).getSingleResult();
        browser.get(urlMapGet("edit external user") + user.getId());
    }

    @Given("^I am on the edit roles page for \"([^\"]*)\"$")
    public void editUnikeyUserPage(String unikeyUser)
    {
        User user = User.findUsersByUsernameEquals(unikeyUser).getSingleResult();
        browser.get(urlMapGet("edit roles") + user.getId());
    }

    @When("^I am on the view permission page for research group \"([^\"]*)\"$")
    public void researchGroupViewPermission(String researchGroupName)
    {
        ResearchGroup researchGroup = ResearchGroup.findResearchGroupsByNameEquals(researchGroupName).getSingleResult();
        browser.get(urlMapGet(VIEW_PERMISSION) + researchGroup.getId());
    }

    @When("^I am on the \"([^\"]*)\" page for research dataset \"([^\"]*)\"$")
    public void whenOnPageForDataset(String pageName, String datasetName)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();
        browser.get(urlMapGet(pageName) + dataset.getId());
    }

    @When("^I am on the \"([^\"]*)\" page for query \"([^\"]*)\" under research dataset \"([^\"]*)\"$")
    public void iAmOnThePageForResearchQueryUnderDataset(String pagename, String queryName, String datasetName)
    {
        ResearchDatabaseQuery query = ResearchDatabaseQuery.findResearchDatabaseQuerysByNameEquals(queryName)
                .getSingleResult();
        ResearchDataset researchDataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName)
                .getSingleResult();
        browser.get(urlMapGet(pagename) + query.getId() + "?datasetId=" + researchDataset.getId());
    }

    @Then("^I should be on the \"([^\"]*)\" page for research dataset \"([^\"]*)\"$")
    public void iShouldBeOnPageForDataset(String pageName, String datasetName)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();
        String expectedUrl = urlMapGet(pageName) + dataset.getId();
        String currentUrl = browser.getCurrentUrl();
        assertEquals("URLs did not match. Expected " + expectedUrl + ", got " + currentUrl, expectedUrl, currentUrl);
    }

    @Given("^I am on the view dataset page for dataset \"([^\"]*)\"$")
    public void givenIAmOnViewDatasetPageForDataset(String datasetName)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();
        browser.get(urlMapGet(VIEW_DATASET) + dataset.getId());
    }

    @Then("^I should be on the view permission page for research group \"([^\"]*)\"$")
    public void iShouldBeOnTheViewPermissionPage(String researchGroupName)
    {
        ResearchGroup group = ResearchGroup.findResearchGroupsByNameEquals(researchGroupName).getSingleResult();
        String currentUrl = browser.getCurrentUrl();
        String expectedUrl = urlMapGet(VIEW_PERMISSION) + group.getId();
        assertEquals("URLs did not match. Expected " + expectedUrl + ", got " + currentUrl, expectedUrl, currentUrl);
    }

    @Then("^I should be on the show SQL query page for query name \"([^\"]*)\" under research dataset \"([^\"]*)\"$")
    public void whenOnPageforQuery(String queryName, String datasetName)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();
        ResearchDatabaseQuery query = ResearchDatabaseQuery.findResearchDatabaseQuerysByNameEquals(queryName)
                .getSingleResult();
        String currentUrl = browser.getCurrentUrl();
        String expectedUrl = urlMapGet("show sql") + query.getId() + "?datasetId=" + dataset.getId();
        assertEquals("URLs did not match. Expected " + expectedUrl + ", got " + currentUrl, expectedUrl, currentUrl);
    }

    @Given("^I am on the create annotation page for path \"([^\"]*)\" for research dataset \"([^\"]*)\"$")
    public void iAmOnCreateAnnotationPage(String filePath, String datasetName)
    {

        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();
        String relativePath = PathBuilder.datasetPath(dataset).getPath() + filePath;
        String url = urlMapGet("create annotation") + "?filePath=" + relativePath + "&connectionId=1000";

        browser.get(url);
    }

    @Given("^I am on the delete annotation page for path \"([^\"]*)\" for research dataset \"([^\"]*)\"$")
    public void iAmOnDeleteAnnotationPage(String filePath, String datasetName)
    {

        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();
        String relativePath = PathBuilder.datasetPath(dataset).getPath() + filePath;
        String url = urlMapGet("delete annotation") + "?filePath=" + relativePath + "&connectionId=1000";

        browser.get(url);
    }

    @Given("^I should be on the delete permission page for user \"([^\"]*)\" under research group \"([^\"]*)\"$")
    public void iShouldBeOnDeletePermissionPageForUserUnderResearchGroup(String username, String researchName)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();
        PermissionEntry entry = PermissionEntry.findPermissionEntrysByUser(user).getSingleResult();
        ResearchGroup group = ResearchGroup.findResearchGroupsByNameEquals(researchName).getSingleResult();
        String expectedUrl = urlMapGet("delete permission") + "?group=" + group.getId() + "&id=" + entry.getId();
        String currentUrl = browser.getCurrentUrl();
        assertEquals("URL's did not match", expectedUrl, currentUrl);
    }

    @Given("^I am on the delete permission page for user \"([^\"]*)\" under research group \"([^\"]*)\"$")
    public void iAmOnTheDeletePermissionPageForUserUnderResearchGroup(String username, String groupName)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();
        PermissionEntry entry = PermissionEntry.findPermissionEntrysByUser(user).getSingleResult();
        ResearchGroup group = ResearchGroup.findResearchGroupsByNameEquals(groupName).getSingleResult();
        String url = urlMapGet("delete permission") + "?group=" + group.getId() + "&id=" + entry.getId();
        browser.get(url);
    }
}
