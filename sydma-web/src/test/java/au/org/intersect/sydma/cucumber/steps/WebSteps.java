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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Function;

import cuke4duke.annotation.I18n.EN.And;
import cuke4duke.annotation.I18n.EN.Given;
import cuke4duke.annotation.I18n.EN.Then;
import cuke4duke.annotation.I18n.EN.When;
import cuke4duke.spring.StepDefinitions;

//TODO CHECKSTYLE-OFF: MultipleStringLiteralsCheck
@StepDefinitions
public class WebSteps
{
    private static final Logger LOG = LoggerFactory.getLogger(WebSteps.class);

    @Autowired
    private WebDriver browser;

    @Then("show me the page")
    public void showMeThePage() throws IOException
    {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy-kk-mm-ss");
        Date now = new Date();
        String fileName = "page_" + formatter.format(now) + ".html";
        BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
        out.write(browser.getPageSource());
        out.close();
    }

    @Given("^I select option \"([^\"]*)\" from select \"([^\"]*)\"$")
    public void selectOptionWithText(String visibleText, String selectId) throws InterruptedException
    {
        String xPath = "//option[text()='" + visibleText + "']";        
        
      //option is loaded last so we wait until we have it before proceeding
        findElement(By.xpath(xPath));
        int retry = 0;
        int limit = 5;
        while (retry < limit)
        {                            
            try
            {                
                Select select = new Select(findElement(By.id(selectId)));
                select.selectByVisibleText(visibleText);    
                break;
            }
            catch (StaleElementReferenceException element)
            {
                retry++;
            }
        }
        retry = 0;
        while (retry < limit)
        {                            
            try
            {                
                //try finding it again to get the newest reference after the option is selected
                findElement(By.xpath(xPath)).click();         
                break;
            }
            catch (StaleElementReferenceException element)
            {
                retry++;
            }
        }
   
    }

    @Given("^I select option \"([^\"]*)\" from radio group$")
    public void selectOptionFromRadio(String optionId) throws InterruptedException
    {
        findElement(By.id(optionId)).click();
    }
    
    @Then("^option \"([^\"]*)\" should be disabled$")
    public void disabledElement(String optionId)
    {
        assertFalse(findElement(By.id(optionId)).isEnabled());
    }
    
    @Then("^I click on the dropdown box$")
    public void clickOnTokenInputBox()
    {
        findElement(By.className("token-input-dropdown-item2")).click();
    }
    
    @Then("^I remove the selected user from the dropdown box")
    public void removeUserFromTokenBox()
    {
        findElement(By.className("token-input-delete-token")).click();
    }

    @Given("^I select role \"([^\"]*)\" from select \"([^\"]*)\"$")
    public void selectRoleWithText(String visibleText, String selectId) throws InterruptedException
    {
        String xPath = "//option[contains(.," + visibleText + ")]";
        Select select = new Select(findElement(By.id(selectId)));
        select.deselectAll();
        findElement(By.xpath(xPath)).click();
    }

    @And("^I add another role \"([^\"]*)\" from select \"([^\"]*)\"$")
    public void addAnotherRole(String role, String selectId) throws InterruptedException
    {
        String xPath = "//option[contains(.," + role + ")]";
        findElement(By.xpath(xPath)).click();
    }

    @And("^I select no roles from select \"([^\"]*)\"$")
    public void iSelectNoRoles(String selectId)
    {
        Select select = new Select(browser.findElement(By.id(selectId)));
        select.deselectAll();
    }

    @When("^I follow \"([^\"]*)\"$")
    public void iFollow(String linkText)
    {
        findElement(By.linkText(linkText)).click();
    }

    @When("^I press \"([^\"]*)\"$")
    public void iPressButtonWithId(String buttonId) throws InterruptedException
    {
        findElement(By.xpath("//input[@id='" + buttonId + "']")).click();
    }

    @When("^I press button with value \"([^\"]*)\"$")
    public void iPressButtonWithValue(String buttonValue) throws InterruptedException
    {
        findElement(By.xpath("//input[@value='" + buttonValue + "']")).click();
    }

    @When("I click on query \"([^\"]*)\"$")
    public void iClickLinkWithValue(String linkName) throws InterruptedException
    {
        findElement(By.xpath("//a[@id='" + linkName + "']")).click();
    }
    
    @When("I click on link \"([^\"]*)\"$")
    public void iClickOnLinkWithValue(String linkName) throws InterruptedException
    {
        findElement(By.xpath("//a[text()='" + linkName + "']/@href")).click();
    }
    
    @When("^I fill in \"([^\"]*)\" with \"([^\"]*)\"$")
    public void iFillIn(String field, String value)
    {
        WebElement fieldElement = findElement(By.xpath("//input[@id='" + field + "']"));
        fieldElement.clear();
        fieldElement.click();
        fieldElement.sendKeys(value);
    }

    @When("^I fill in textarea \"([^\"]*)\" with \"([^\"]*)\"$")
    public void iFillInTextArea(String field, String value)
    {
        WebElement fieldElement = findElement(By.xpath("//textarea[@id='" + field + "']"));
        fieldElement.clear();
        fieldElement.sendKeys(value);
    }

    @Then("^I should see \"([^\"]*)\"$")
    public void iShouldSee(String text) throws InterruptedException
    {
        String xPath = checkText(text);
        findElement(By.xpath(xPath));
    }
    
    @Then("^element with id \"([^\"]*)\" contains text \"([^\"]*)\"$")
    public void elementWithIdContainsText(String id, String text) throws InterruptedException
    {
        WebElement fieldElement = findElement(By.id(id));
        assertTrue(fieldElement.getText().contains(text));
    }
    
    @Then("^I should see an alert that says \"([^\"]*)\"$")
    public void iShouldSeeAlert(String expectedAlertMessage)
    {
        Alert alert = browser.switchTo().alert();
        String actualAlertMessage = alert.getText();
        alert.accept();

        assertTrue(actualAlertMessage.equals(expectedAlertMessage));
    }

    @Then("^I should not see \"([^\"]*)\"$")
    public void iShouldNotSee(String text)
    {
        String xPath = checkText(text);
        try
        {
            browser.findElement(By.xpath(xPath));
            fail("Expected not to find test: " + text);
        }
        catch (org.openqa.selenium.NoSuchElementException ex)
        {
        }
    }

    @Then("^I should see a \"([^\"]*)\" button$")
    public void iShouldSeeButton(String buttonId) throws InterruptedException
    {
        findElement(By.xpath("//input[@id='" + buttonId + "']")).click();
    }

    @Then("^I tick checkbox \"([^\"]*)\"$")
    public void iTickCheckbox(String checkbox) throws InterruptedException
    {
        findElement(By.xpath("//input[@id='" + checkbox + "']")).click();
    }
    
    @Then("^I should see \"([^\"]*)\" in the cell \"([^\"]*)\" in the table \"([^\"]*)\"$")
    public void iShouldSeeContentInTheTableCell(String expectedCellContent, String cellClassName, String tableName)
    {
        assertTrue(checkTableForContent(expectedCellContent, cellClassName, tableName));
    }

    @Then("^I should not see \"([^\"]*)\" in the cell \"([^\"]*)\" in the table \"([^\"]*)\"$")
    public void iShouldNotSeeContentInTheTableCell(String expectedCellContent, String cellClassName, String tableName)
    {
        assertFalse(checkTableForContent(expectedCellContent, cellClassName, tableName));
    }

    @When("^I sleep for \"([^\"]*)\" seconds$")
    public void iSleepFor(String seconds) throws InterruptedException
    {
        Thread.sleep(Integer.parseInt(seconds) * 1000);
    }

    @Then("^I should see the expected fields displayed$")
    public void expectedFields()
    {
        String[] expectedText = {"Parent Group Info", "Parent Project Info", "Dataset Info",
            "Name of Research Dataset", "Description of Research Dataset", "Location of Research Dataset",
            "Field of Research Subject Code", "Name of Research Project", "Research Project URL (if applicable)",
            "Description of Research Project", "Name of Research Group", "Principal Investigator",
            "Principal Investigator's Full Name", "Data Management Contact (if different to Principal Investigator)",
            "Research Group URL (if applicable)", "Description of Research Group"};

        for (int i = 0; i < expectedText.length; i++)
        {
            try
            {
                browser.findElement(By.xpath("//*[contains(.,expectedText[i])]"));
            }
            catch (org.openqa.selenium.NoSuchElementException ex)
            {
                fail("Expected text: " + expectedText[i]);
            }
        }

    }

    @Then("^I should see role \"([^\"]*)\"$")
    public void iShouldSeeRole(String role)
    {
        try
        {
            browser.findElement(By.xpath("//*[contains(.," + role + ")]"));
        }
        catch (org.openqa.selenium.NoSuchElementException ex)
        {
            fail("Expected text: " + role);
        }
    }

    private boolean checkTableForContent(String expectedCellContent, String cellClassName, String tableName)
    {
        WebElement table = findElement(By.id(tableName));
        assertNotNull(table);
        boolean found = false;

        for (WebElement row : table.findElements(By.tagName("tr")))
        {
            if (!found)
            {
                found = checkTableCellsForContent(row, expectedCellContent, cellClassName);
            }
        }

        return found;
    }

    private boolean checkTableCellsForContent(WebElement row, String expectedCellContent, String cellClassName)
    {
        WebElement cellContent;
        try
        {
            cellContent = findElement(By.className(cellClassName));
            return cellContent.getText().equals(expectedCellContent);
        }
        catch (TimeoutException e)
        {
            return false;
        }
    }

    private String checkText(String s)
    {
        String xPath = "//*[text()='" + s + "']";
        return xPath;
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
