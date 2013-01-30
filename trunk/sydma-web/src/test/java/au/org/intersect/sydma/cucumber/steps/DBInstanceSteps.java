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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.mchange.v2.c3p0.PooledDataSource;

import cuke4duke.annotation.I18n.EN.Given;
import cuke4duke.annotation.I18n.EN.Then;
import cuke4duke.annotation.I18n.EN.When;
import cuke4duke.spring.StepDefinitions;

import au.org.intersect.sydma.webapp.domain.DBAccess;
import au.org.intersect.sydma.webapp.domain.DBSchema;
import au.org.intersect.sydma.webapp.domain.DBUser;
import au.org.intersect.sydma.webapp.domain.ResearchDatabaseQuery;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchDatasetDB;
import au.org.intersect.sydma.webapp.util.CSVHelper;

@StepDefinitions
@Transactional("sydmaPU")
public class DBInstanceSteps
{
    private static final Logger LOG = LoggerFactory.getLogger(DBInstanceSteps.class);

    @Autowired
    private WebDriver browser;

    @Autowired
    private PooledDataSource dataSource;

    private String password = "password";

    @Given("^dataset \"([^\"]*)\" has dbinstance with schema \"([^\"]*)\"$")
    public void datasetHasDbInstance(String datasetName, String schemaName) throws SQLException
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();
        createDbInstance(dataset, schemaName);
    }

    @Given("^I have a dbschema with name \"([^\"]*)\"$")
    public void iHaveDbSchema(String schemaName)
    {
        createDbSchema(schemaName);
    }

    @Given("^I should have a dbschema with name \"([^\"]*)\"$")
    public void iShouldHaveDbSchema(String schemaName)
    {
        DBSchema schema = DBSchema.findDBSchema(schemaName);
        if (schema == null)
        {
            fail("Expected to find schema called " + schemaName);
        }
    }

    @Given("^dataset \"([^\"]*)\" has a table \"([^\"]*)\"$")
    public void iCreateTableInDatabase(String datasetName, String tableName) throws SQLException
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();
        String databaseName = dataset.getDatabaseInstance().getDbName();
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        statement.execute("use " + databaseName);
        statement.execute("create table " + tableName + " (id integer)");
    }

    @Given("^dataset \"([^\"]*)\" table \"([^\"]*)\" has a column called \"([^\"]*)\"$")
    public void tableHasColumnCalled(String datasetName, String tableName, String columnName) throws SQLException
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();
        String databaseName = dataset.getDatabaseInstance().getDbName();
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        statement.execute("use " + databaseName);
        statement.execute("alter table " + tableName + " add column ( " + columnName + " integer)");
    }

    @When("^I fill in \"([^\"]*)\" with dataset \"([^\"]*)\" db instance name")
    public void iFillIn(String field, String datasetName)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();
        ResearchDatasetDB dbInstance = dataset.getDatabaseInstance();
        String dbName = dbInstance.getDbName();

        WebElement fieldElement = findElement(By.xpath("//input[@id='" + field + "']"));
        fieldElement.clear();
        fieldElement.sendKeys(dbName);
    }

    @Then("^I should see db user with (full|update|view) access to dataset \"([^\"]*)\"$")
    public void iShouldSeeDbUser(String accessLevel, String datasetName)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();
        ResearchDatasetDB dbInstance = dataset.getDatabaseInstance();
        DBUser dbUser = null;
        if ("full".equals(accessLevel))
        {
            dbUser = dbInstance.findFullAccessDBUser();
        }
        else if ("update".equals(accessLevel))
        {
            dbUser = dbInstance.findUpdateAccessDBUser();
        }
        else if ("view".equals(accessLevel))
        {
            dbUser = dbInstance.findViewAccessDBUser();
        }

        String dbUsernameValue = getDbUsernameValueOnViewPage();

        assertEquals("Incorrect dbUsername displayed", dbUser.getDbUsername(), dbUsernameValue);
    }

    @Then("^a db instance (should|should not) exist in the database for dataset \"([^\"]*)\"$")
    public void aDbInstanceShouldExist(String shouldOrNot, String datasetName) throws SQLException
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();

        String dbName = "dataset_" + dataset.getId();

        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SHOW DATABASES");
        boolean databaseFound = false;
        while (resultSet.next())
        {
            String databaseEntry = resultSet.getString("Database");
            if (dbName.equals(databaseEntry))
            {
                // found
                databaseFound = true;
                continue;
            }
        }
        if ("should".equals(shouldOrNot) && !databaseFound)
        {
            fail("DB Instance with name " + dbName + " does not exist in underlying database");
        }
        if ("should not".equals(shouldOrNot) && databaseFound)
        {
            fail("DB Instance with name " + dbName + " should not exist in underlying database");
        }
    }

    private String createDbUsername(Long datasetId, DBAccess dbAccess)
    {
        return "sydma_user_" + datasetId + dbAccess.getDbSuffix();
    }

    @Then("^db users (should|should not) exist in the database for dataset \"([^\"]*)\"$")
    public void dbUsersShouldExist(String shouldOrNot, String datasetName) throws SQLException
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();
        Long datasetId = dataset.getId();

        String faUsername = createDbUsername(datasetId, DBAccess.FULL_ACCESS);
        String uaUsername = createDbUsername(datasetId, DBAccess.UPDATE_ACCESS);
        String vaUsername = createDbUsername(datasetId, DBAccess.VIEW_ACCESS);

        List<String> usernames = new ArrayList<String>();
        usernames.add(faUsername);
        usernames.add(uaUsername);
        usernames.add(vaUsername);

        int originalUserCount = usernames.size();

        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT User FROM mysql.user");
        while (resultSet.next())
        {
            String userEntry = resultSet.getString("User");
            if (usernames.contains(userEntry))
            {
                usernames.remove(userEntry);
            }
        }

        boolean allFound = usernames.isEmpty();

        boolean usersNotFound = usernames.size() == originalUserCount;

        if ("should".equals(shouldOrNot) && !allFound)
        {
            fail("Not all expected DB Users exist in the underlying database, missing " + usernames);
        }
        if ("should not".equals(shouldOrNot) && !usersNotFound)
        {
            fail("Some unexpected DB Users exist in the underlying database, present " + usernames);
        }

    }

    @Then("^I have query \"([^\"]*)\" for dataset \"([^\"]*)\"$")
    public void iHaveQueryForDataset(String queryName, String datasetName)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();
        ResearchDatabaseQuery query = new ResearchDatabaseQuery();
        query.setName(queryName);
        query.setQuery("NEW QUERY");
        query.setDescription("Some query description");
        query.setResearchDatasetDB(dataset.getDatabaseInstance());
        query.merge();
    }

    @Then("^I have a proper query \"([^\"]*)\" for dataset \"([^\"]*)\"$")
    public void iHaveAProperQueryForDataset(String queryName, String datasetName)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();
        ResearchDatabaseQuery query = new ResearchDatabaseQuery();
        query.setName(queryName);
        query.setQuery("SHOW TABLES;");
        query.setDescription("Showing tables!");
        query.setResearchDatasetDB(dataset.getDatabaseInstance());
        query.merge();
    }

    @Then("^I should get a download with filename \"([^\"]*)\"$")
    public void iShouldGetDownloadWithFilename(String filename)
    {
        String csvFile = CSVHelper.formFilename(filename);
        String path = "/home/charles/Downloads/" + csvFile;
        File file = new File(path);
        String checkFilename = file.getName();
        file.delete();
        assertEquals("File doesnt exist", checkFilename, csvFile);
    }

    @Then("^the dataset \"([^\"]*)\" db state should have state \"([^\"]*)\"$")
    public void theDatasetStateShouldHaveState(String datasetName, String state)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();
        ResearchDatasetDB datasetDB = ResearchDatasetDB.findResearchDatasetDBsByResearchDataset(dataset)
                .getSingleResult();
        String dBState = datasetDB.getState();
        assertEquals("States do not match", state, dBState);
    }

    private String getDbUsernameValueOnViewPage()
    {
        WebElement userValueEle = browser.findElement(By.id("dbUsername_value"));
        String usernameValue = userValueEle.getText();
        return usernameValue;
    }

    private void createDbInstance(ResearchDataset dataset, String schemaName) throws SQLException
    {
        Connection connection = dataSource.getConnection();

        DBSchema schema = DBSchema.findDBSchema(schemaName);
        ResearchDatasetDB datasetDB = new ResearchDatasetDB("description", schema, "localhost", dataset);

        dataset.setDatabaseInstance(datasetDB);
        datasetDB.setResearchDataset(dataset);
        datasetDB.createInstance(connection);
        DBUser faUser = new DBUser(password, DBAccess.FULL_ACCESS, datasetDB);
        DBUser uaUser = new DBUser(password, DBAccess.UPDATE_ACCESS, datasetDB);
        DBUser vaUser = new DBUser(password, DBAccess.VIEW_ACCESS, datasetDB);
        faUser.createAndGrantUser(connection);
        uaUser.createAndGrantUser(connection);
        vaUser.createAndGrantUser(connection);

        datasetDB.addDBUser(faUser);
        datasetDB.addDBUser(uaUser);
        datasetDB.addDBUser(vaUser);

        dataset.merge();

    }

    private void createDbSchema(String name)
    {
        DBSchema schema = new DBSchema(name, name + ".sql");
        schema.persist();
    }

    private WebElement findElement(final By locator)
    {
        // times out after 10 seconds
        WebDriverWait wait = new WebDriverWait(browser, 10);

        // while the following loop runs, the DOM changes -
        // page is refreshed, or element is removed and re-added
        wait.until(presenceOfElementLocated(locator));
        WebElement element = browser.findElement(locator);
        if (element == null)
        {
            fail("Unable to locate element " + locator);
        }

        return element;
    }

    private Function<WebDriver, WebElement> presenceOfElementLocated(final By locator)
    {
        return new Function<WebDriver, WebElement>()
        {
            @Override
            public WebElement apply(WebDriver driver)
            {
                return driver.findElement(locator);
            }
        };
    }
}
