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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

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

import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;

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
        WebElement element = browser.findElement(By.xpath("//a[@title = '" + directoryName + "'] "));
        element.click();
        Thread.sleep(2000);
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
        WebElement element = browser.findElement(By.xpath("//li[a[text() = '" + linkText + "']]/ins"));
        element.click();
        LOG.info("Opening directory " + linkText);
        Thread.sleep(1000);
    }

    @Then("^I drag (?:file|directory) \"([^\"]*)\" into \"([^\"]*)\"$")
    public void iDragElementTitle(String from, String to)
    {
        WebElement fromElement = browser.findElement(By.xpath("//a[@title='" + from + "']"));
        WebElement toElement = browser.findElement(By.xpath("//a[@title='" + to + "']"));

        (new Actions(browser)).dragAndDrop(fromElement, toElement).perform();
    }

    @Then("^I should not see any ajax lightbox$")
    public void iShouldNotSeeAjaxLightbox() throws InterruptedException
    {
        int retryLimit = 5;
        int retry = 0;
        while (retry < retryLimit)
        {
            try
            {
                browser.findElement(By.id("ajax_content"));
                fail("No ajax lightbox should be loaded");
            }
            catch (org.openqa.selenium.NoSuchElementException ex)
            {
                // good
            }
            retry++;
            Thread.sleep(1000);
        }
    }

    @Then("^I click \"([^\"]*)\" in the ajax lightbox$")
    public void iClickInTheAjaxLightbox(String buttonId) throws InterruptedException
    {
        int retryLimit = 5;
        int retry = 0;
        while (retry < retryLimit)
        {
            Thread.sleep(1000);
            try
            {
                WebElement fromElement = browser.findElement(By.id(buttonId));
                fromElement.click();
                return;
            }
            catch (org.openqa.selenium.NoSuchElementException ex)
            {
                retry++;                
            }
        }
        fail("No element with id " + buttonId + " was loaded in lightbox");
    }

    @Then("^I wait for lightbox to close$")
    public void iWaitForLightboxClose() throws InterruptedException
    {
        int retryLimit = 5;
        int retry = 0;
        while (retry < retryLimit)
        {
            try
            {
                browser.findElement(By.xpath("//div[@id='fancybox-content']/div[@class='sydma_content']"));
                retry++;
                Thread.sleep(1000);
            }
            catch (org.openqa.selenium.NoSuchElementException ex)
            {
                // all good, lightbox has closed
                break;
            }
        }
    }

    @Then("^I should be able to see (?:file|directory) \"([^\"]*)\" in \"([^\"]*)\"$")
    public void iShouldBeAbleToSeeInDirectory(String titleToCheck, String containerDirectoryTitle)
    {
        WebElement containerElement;
        try
        {
            containerElement = browser.findElement(By.xpath("//li[a[@title='" + containerDirectoryTitle + "']]"));
        }
        catch (org.openqa.selenium.NoSuchElementException ex)
        {
            fail("Expected directory with title: " + containerDirectoryTitle);
            return;
        }
        try
        {
            containerElement.findElement(By.xpath("//a[@title='" + containerDirectoryTitle + "']"));
        }
        catch (org.openqa.selenium.NoSuchElementException ex)
        {
            fail("Expected file or directory with title: " + titleToCheck + " within " + containerDirectoryTitle);
        }

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
