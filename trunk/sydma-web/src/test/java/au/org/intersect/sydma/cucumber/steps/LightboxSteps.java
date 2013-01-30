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

import static org.junit.Assert.fail;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;

import cuke4duke.annotation.I18n.EN.Then;
import cuke4duke.spring.StepDefinitions;

@StepDefinitions
public class LightboxSteps
{

    @Autowired
    private WebDriver browser;

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

    @Then("^I wait for lightbox to fully open$")
    public void iWaitForLightboxFullyOpen() throws InterruptedException
    {
        Thread.sleep(1000);
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

}
