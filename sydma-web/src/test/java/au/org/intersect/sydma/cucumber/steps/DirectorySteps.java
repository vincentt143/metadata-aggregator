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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.persistence.TypedQuery;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.base.Function;

import cuke4duke.annotation.After;
import cuke4duke.annotation.Before;
import cuke4duke.annotation.I18n.EN.Given;
import cuke4duke.annotation.I18n.EN.Then;
import cuke4duke.annotation.I18n.EN.When;
import cuke4duke.spring.StepDefinitions;

import au.org.intersect.sydma.webapp.domain.AccessLevel;
import au.org.intersect.sydma.webapp.domain.PermissionEntry;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.permission.path.PathBuilder;

/**
 * Steps to test directory browsing related functionality
 * 
 * @version $Rev: 29 $
 */
// TODO CHECKSTYLE-OFF: MultipleStringLiterals
@StepDefinitions
public class DirectorySteps
{

    private static final Logger LOG = LoggerFactory.getLogger(DirectorySteps.class);

    private String pathSeparator = "/";

    @Autowired
    private WebDriver browser;

    @Value("#{workerProperties['dms.wn.localRootPath']}")
    private String localRootPath;

    @Value("#{sydmaFileProperties['sydma.localFileServer']}")
    private String localServer;

    @Given("^the research group \"([^\"]*)\" has directory \"([^\"]*)\"$")
    public void researchGroupHasDirectory(String name, String directoryPath) throws IOException
    {
        TypedQuery<ResearchGroup> query = ResearchGroup.findResearchGroupsByNameEquals(name);
        ResearchGroup group = query.getSingleResult();
        group.setDirectoryPath(directoryPath);
        group.merge();

        String uploadPath = getUploadPath();

        File directory = new File(appendPath(uploadPath, directoryPath));

        boolean success = directory.mkdir();
        if (!success)
        {
            throw new IOException("Failed to create group directory " + directory);
        }
    }

    @Given("^there is a directory called \"([^\"]*)\"$")
    public void thereIsADirectoryCalled(String directoryPath) throws IOException
    {
        String uploadPath = getUploadPath();
        File directory = new File(appendPath(uploadPath, directoryPath));
        boolean success = directory.mkdir();

        if (!success)
        {
            throw new IOException("Failed to create group directory " + directory);
        }
    }

    @Given("^the research dataset \"([^\"]*)\" has a directory$")
    public void researchDatasetHasDirectory(String name) throws IOException
    {
        String datasetAbsolutePath = getDirectoryPathForDatasetWithName(name);
        File datasetDirectory = new File(datasetAbsolutePath);

        boolean success = datasetDirectory.mkdir();
        if (!success)
        {
            throw new IOException("Failed to create directory " + datasetDirectory);
        }
    }

    @Given("^the dataset \"([^\"]*)\" has file \"([^\"]*)\"$")
    public void datasetHasFile(String datasetName, String fileName) throws IOException
    {
        String datasetAbsolutePath = getDirectoryPathForDatasetWithName(datasetName);

        String fileAbsolutePath = appendPath(datasetAbsolutePath, fileName);

        File file = new File(fileAbsolutePath);
        boolean success = file.createNewFile();
        if (!success)
        {
            throw new IOException("Failed to create file " + fileAbsolutePath);
        }
    }

    @Given("^the dataset \"([^\"]*)\" has directory \"([^\"]*)\"$")
    public void datasetHasDirectory(String datasetName, String directoryName) throws IOException
    {
        String datasetAbsolutePath = getDirectoryPathForDatasetWithName(datasetName);

        String directoryAbsolutePath = appendPath(datasetAbsolutePath, directoryName);

        File directory = new File(directoryAbsolutePath);
        boolean success = directory.mkdir();
        if (!success)
        {
            throw new IOException("Failed to create directory " + directoryAbsolutePath);
        }
    }

    @Given("^the dataset \"([^\"]*)\" has file \"([^\"]*)\" under directory \"([^\"]*)\"$")
    public void datasetDirectoryHasFile(String datasetName, String fileName, String directoryPath) throws IOException
    {
        String datasetAbsolutePath = getDirectoryPathForDatasetWithName(datasetName);

        String directoryAbsolutePath = appendPath(datasetAbsolutePath, directoryPath);

        String fileAbsolutePath = appendPath(directoryAbsolutePath, fileName);

        File file = new File(fileAbsolutePath);
        boolean success = file.createNewFile();
        if (!success)
        {
            throw new IOException("Failed to create file " + fileAbsolutePath);
        }
    }

    @Given("^the dataset \"([^\"]*)\" has directory \"([^\"]*)\" under directory \"([^\"]*)\"$")
    public void datasetDirectoryHasDirectory(String datasetName, String directoryName, String directoryPath)
        throws IOException
    {
        String datasetAbsolutePath = getDirectoryPathForDatasetWithName(datasetName);

        String directoryAbsolutePath = appendPath(datasetAbsolutePath, directoryPath);

        String newDirectoryAbsolutePath = appendPath(directoryAbsolutePath, directoryName);

        File directory = new File(newDirectoryAbsolutePath);
        boolean success = directory.mkdir();
        if (!success)
        {
            throw new IOException("Failed to create directory " + directoryAbsolutePath);
        }
    }

    @Then("^I select (?:directory|file) \"([^\"]*)\"$")
    public void iSelectDirectoryOrFile(String directoryName) throws InterruptedException
    {
        WebElement element = findElement(By.xpath("//a[@title = '" + directoryName + "'] "));
        element.click();
    }

    @Then("^I should be able to see (?:directory|file) \"([^\"]*)\"$")
    public void iShouldSeeDirectoryOrFile(String fileName) throws InterruptedException
    {
        WebElement directory = findElement(By.xpath("//li[a[@title = '" + fileName + "']]"));
        assertNotNull(directory);
    }

    @Then("^I should not be able to see (?:directory|file) \"([^\"]*)\"$")
    public void iShouldNotBeAbleToSeeDirectoryOrFile(String fileName) throws InterruptedException
    {
        int retry = 0;
        final int limit = 5;
        while (retry < limit)
        {
            try
            {
                browser.findElement(By.xpath("//li[a[@title = '" + fileName + "']]"));
                retry++;
                Thread.sleep(500);
            }
            catch (org.openqa.selenium.NoSuchElementException ex)
            {
                // good
                return;
            }
        }
        fail("Should not see file: " + fileName);
    }

    @When("^I open directory \"([^\"]*)\"$")
    public void iOpenDirectory(String linkText) throws InterruptedException
    {
        WebElement element = findElement(By.xpath("//li[a[text() = '" + linkText + "']]/ins"));
        element.click();
        LOG.info("Opening directory " + linkText);
        Thread.sleep(1000);
    }

    @Then("^I drag (?:file|directory) \"([^\"]*)\" into \"([^\"]*)\"$")
    public void iDragElementTitle(String from, String to)
    {
        WebElement fromElement = findElement(By.xpath("//a[@title='" + from + "']"));
        WebElement toElement = findElement(By.xpath("//a[@title='" + to + "']"));

        (new Actions(browser)).dragAndDrop(fromElement, toElement).perform();
    }

    @Then("^I should be able to see (?:file|directory) \"([^\"]*)\" in \"([^\"]*)\"$")
    public void iShouldBeAbleToSeeInDirectory(String titleToCheck, String containerDirectoryTitle)
    {
        WebElement containerElement = findElement(By.xpath("//li[a[@title='" + containerDirectoryTitle + "']]"));

        try
        {
            containerElement.findElement(By.xpath("//a[@title='" + containerDirectoryTitle + "']"));
        }
        catch (org.openqa.selenium.NoSuchElementException ex)
        {
            fail("Expected file or directory with title: " + titleToCheck + " within " + containerDirectoryTitle);
        }

    }

    @Then("^I should (have|not have) directory \"([^\"]*)\" under dataset \"([^\"]*)\" and path \"([^\"]*)\"$")
    public void iShouldHaveOrNotDirectoryUnder(String haveNotHave, String directoryName, String datasetName,
            String relativePath) throws InterruptedException
    {
        String datasetAbsolutePath = getDirectoryPathForDatasetWithName(datasetName);
        String directoryPathToCheck = datasetAbsolutePath + pathSeparator + relativePath;

        boolean hasDirectory = hasDirectoryUnder(directoryName, directoryPathToCheck);

        if ("have".equals(haveNotHave))
        {
            assertTrue("Directory " + directoryName + " should be found under " + directoryPathToCheck, hasDirectory);
        }
        else
        {
            assertFalse("Directory " + directoryName + " should not be found under " + directoryPathToCheck,
                    hasDirectory);
        }

    }

    @Then("^I should (have|not have) file \"([^\"]*)\" under dataset \"([^\"]*)\" and path \"([^\"]*)\"$")
    public void iShouldNotHaveFileUnder(String haveNotHave, String fileName, String datasetName, String relativePath)
        throws InterruptedException
    {
        String datasetAbsolutePath = getDirectoryPathForDatasetWithName(datasetName);
        String directoryPathToCheck = datasetAbsolutePath + pathSeparator + relativePath;

        boolean hasFile = hasFileUnder(fileName, directoryPathToCheck);

        if ("have".equals(haveNotHave))
        {
            assertTrue("File " + fileName + " should be found under " + directoryPathToCheck, hasFile);
        }
        else
        {
            assertFalse("File " + fileName + " should not be found under " + directoryPathToCheck, hasFile);
        }
    }

    @Then("^user \"([^\"]*)\" should have full access to directory \"([^\"]*)\" in dataset \"([^\"]*)\"$")
    public void userShouldHaveFullAccessToDirectory(String username, String directoryName, String datasetName)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();

        String datasetAbsolutePath = PathBuilder.datasetPath(dataset).getPath();
        String directoryAbsolutePath = datasetAbsolutePath.concat(directoryName).concat("/");

        PermissionEntry entry = PermissionEntry.findPermissionEntrysByPathEqualsAndUser(directoryAbsolutePath, user)
                .getSingleResult();
        assertEquals(entry.getAccessLevel(), AccessLevel.FULL_ACCESS);
    }

    @Then("^user \"([^\"]*)\" should have viewing access to directory \"([^\"]*)\" in dataset \"([^\"]*)\"$")
    public void userShouldHaveViewingAccessToDirectory(String username, String directoryName, String datasetName)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();

        String datasetAbsolutePath = PathBuilder.datasetPath(dataset).getPath();
        String directoryAbsolutePath = datasetAbsolutePath.concat(directoryName).concat("/");

        PermissionEntry entry = PermissionEntry.findPermissionEntrysByPathEqualsAndUser(directoryAbsolutePath, user)
                .getSingleResult();
        assertEquals(entry.getAccessLevel(), AccessLevel.VIEWING_ACCESS);
    }

    @Then("^user \"([^\"]*)\" should have full access to sub-directory \"([^\"]*)\" of directory \"([^\"]*)\" in"
            + " dataset \"([^\"]*)\"$")
    public void userShouldHaveFullAccessToChildDirectory(String username, String subDirectoryName,
            String directoryName, String datasetName)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();

        String datasetAbsolutePath = PathBuilder.datasetPath(dataset).getPath();
        String directoryAbsolutePath = datasetAbsolutePath.concat(directoryName).concat("/").concat(subDirectoryName)
                .concat("/");

        PermissionEntry entry = PermissionEntry.findPermissionEntrysByPathEqualsAndUser(directoryAbsolutePath, user)
                .getSingleResult();
        assertEquals(entry.getAccessLevel(), AccessLevel.FULL_ACCESS);
    }

    @Then("^user \"([^\"]*)\" should not have a permission entry in sub-directory \"([^\"]*)\" of directory "
            + "\"([^\"]*)\" in dataset \"([^\"]*)\"$")
    public void userShouldNotHavePermissionENtryToChildDirectory(String username, String subDirectoryName,
            String directoryName, String datasetName)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();

        String datasetAbsolutePath = PathBuilder.datasetPath(dataset).getPath();
        String directoryAbsolutePath = datasetAbsolutePath.concat(directoryName).concat("/").concat(subDirectoryName)
                .concat("/");

        List<PermissionEntry> entries = PermissionEntry.findPermissionEntrysByPathEqualsAndUser(directoryAbsolutePath,
                user).getResultList();
        assertEquals(entries.size(), 0);
    }

    @Given("^user \"([^\"]*)\" has full access to directory \"([^\"]*)\" in dataset \"([^\"]*)\"$")
    public void userHasFullAccessToDirectory(String username, String directoryName, String datasetName)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();

        String datasetAbsolutePath = PathBuilder.datasetPath(dataset).getPath();
        String directoryAbsolutePath = datasetAbsolutePath.concat(directoryName).concat("/");

        PermissionEntry entry = new PermissionEntry();
        entry.setAccessLevel(AccessLevel.FULL_ACCESS);
        entry.setUser(user);
        entry.setPath(directoryAbsolutePath);
        entry.merge();
    }

    @Given("^user \"([^\"]*)\" has full access to sub-directory \"([^\"]*)\" of directory \"([^\"]*)\" in"
            + " dataset \"([^\"]*)\"$")
    public void userHasFullAccessToChildDirectory(String username, String subDirectoryName, String directoryName,
            String datasetName)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();

        String datasetAbsolutePath = PathBuilder.datasetPath(dataset).getPath();
        String directoryAbsolutePath = datasetAbsolutePath.concat(directoryName).concat("/").concat(subDirectoryName)
                .concat("/");

        PermissionEntry entry = new PermissionEntry();
        entry.setAccessLevel(AccessLevel.FULL_ACCESS);
        entry.setUser(user);
        entry.setPath(directoryAbsolutePath);
        entry.merge();
    }

    @Given("^user \"([^\"]*)\" has viewing access to sub-directory \"([^\"]*)\" of directory \"([^\"]*)\" in"
            + " dataset \"([^\"]*)\"$")
    public void userHasViewingAccessToChildDirectory(String username, String subDirectoryName, String directoryName,
            String datasetName)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();

        String datasetAbsolutePath = PathBuilder.datasetPath(dataset).getPath();
        String directoryAbsolutePath = datasetAbsolutePath.concat(directoryName).concat("/").concat(subDirectoryName)
                .concat("/");

        PermissionEntry entry = new PermissionEntry();
        entry.setAccessLevel(AccessLevel.VIEWING_ACCESS);
        entry.setUser(user);
        entry.setPath(directoryAbsolutePath);
        entry.merge();
    }

    @Given("^user \"([^\"]*)\" has viewing access to directory \"([^\"]*)\" in dataset \"([^\"]*)\"$")
    public void userHasViewingAccessToDirectory(String username, String directoryName, String datasetName)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();

        String datasetAbsolutePath = PathBuilder.datasetPath(dataset).getPath();
        String directoryAbsolutePath = datasetAbsolutePath.concat(directoryName).concat("/");

        PermissionEntry entry = new PermissionEntry();
        entry.setAccessLevel(AccessLevel.VIEWING_ACCESS);
        entry.setUser(user);
        entry.setPath(directoryAbsolutePath);
        entry.merge();
    }

    @Given("^user \"([^\"]*)\" has no access to directory \"([^\"]*)\" in dataset \"([^\"]*)\"$")
    public void userHasNoAccessToDirectory(String username, String directoryName, String datasetName)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();
        ResearchDataset dataset = ResearchDataset.findResearchDatasetsByNameEquals(datasetName).getSingleResult();

        String datasetAbsolutePath = PathBuilder.datasetPath(dataset).getPath();
        String directoryAbsolutePath = datasetAbsolutePath.concat(directoryName).concat("/");

        PermissionEntry entry = new PermissionEntry();
        entry.setAccessLevel(AccessLevel.NO_ACCESS);
        entry.setUser(user);
        entry.setPath(directoryAbsolutePath);
        entry.merge();
    }

    @Before
    public void before() throws IOException
    {
        prepareUploadRoot();
        cleanupFiles();
    }

    @After
    public void after() throws IOException
    {
        cleanupFiles();
    }

    private boolean hasFileUnder(String fileName, String pathToCheck)
    {
        File directoryToCheck = new File(pathToCheck);
        File[] files = directoryToCheck.listFiles();
        for (File file : files)
        {
            if (fileName.equals(file.getName()) && file.isFile())
            {
                return true;
            }
        }
        return false;
    }

    private boolean hasDirectoryUnder(String directoryName, String pathToCheck)
    {
        File directoryToCheck = new File(pathToCheck);
        File[] files = directoryToCheck.listFiles();
        for (File file : files)
        {
            if (directoryName.equals(file.getName()) && file.isDirectory())
            {
                return true;
            }
        }
        return false;
    }

    private void prepareUploadRoot()
    {
        String uploadRoot = getUploadPath();
        File uploadDir = new File(uploadRoot);
        if (!uploadDir.isDirectory())
        {
            uploadDir.mkdir();
        }
    }

    private void cleanupFiles() throws IOException
    {
        String uploadPath = getUploadPath();
        File uploadDirectory = new File(uploadPath);
        File[] files = uploadDirectory.listFiles();
        for (File file : files)
        {
            if (file.isDirectory())
            {
                FileUtils.deleteDirectory(file);
            }
            else
            {
                file.delete();
            }
        }
    }

    private String getDirectoryPathForDatasetWithName(String datasetName)
    {
        TypedQuery<ResearchDataset> query = ResearchDataset.findResearchDatasetsByNameEquals(datasetName);
        ResearchDataset dataset = query.getSingleResult();
        ResearchGroup group = dataset.getResearchProject().getResearchGroup();

        String datasetIdStr = dataset.getId().toString();

        String uploadPath = getUploadPath();

        String groupPath = group.getDirectoryPath();

        String groupAbsolutePath = appendPath(uploadPath, groupPath);

        String datasetAbsolutePath = appendPath(groupAbsolutePath, datasetIdStr);

        return datasetAbsolutePath;
    }

    private String getUploadPath()
    {
        return appendPath(localRootPath, localServer);
    }

    private String appendPath(String basePath, String path)
    {
        return basePath + "/" + path;
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
