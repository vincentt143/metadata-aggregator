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

//TODO CHECKSTYLE-OFF: ImportOrder
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;

import cuke4duke.annotation.I18n.EN.Given;
import cuke4duke.spring.StepDefinitions;

import au.org.intersect.sydma.webapp.domain.Role;
import au.org.intersect.sydma.webapp.domain.Role.RoleNames;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.domain.UserType;

@StepDefinitions
public class UserSteps
{
    private static final String MOCK_EMAIL = "test@email.com";
    private static final String MOCK_SURNAME = "surname";
    private static final String MOCK_GIVEN = "given";
    private static final String MOCK_INSTITUTION = "intersect";

    private static final String ADMINISTRATOR = "administrator";
    private static final String RESEARCHER = "researcher";
    private static final String ICT_SUPPORT = "ict_support";
    private static final String RESEARCH_MANAGER = "research_manager";

    @Autowired
    private WebDriver browser;

    @Given("^I have the usual users and roles$")
    public void iHaveTheUsualUsersAndRoles()
    {
        createRole(RoleNames.ROLE_ADMINISTRATOR, "Administrator", ADMINISTRATOR);
        createRole(RoleNames.ROLE_ICT_SUPPORT, "ICT Support", ICT_SUPPORT);
        createRole(RoleNames.ROLE_RESEARCHER, "Researcher", RESEARCHER);
        createRole(RoleNames.ROLE_RESEARCH_DATA_MANAGER, "Research Manager", RESEARCH_MANAGER);
        createRole(RoleNames.ACCEPTED_TC, "Accepted Terms and Conditions", null);
        createRole(RoleNames.ACTIVE, "Active", null);
        addRole(RoleNames.ROLE_RESEARCHER, RESEARCH_MANAGER);
        addRole(RoleNames.ACCEPTED_TC, ADMINISTRATOR);
        addRole(RoleNames.ACCEPTED_TC, ICT_SUPPORT);
        addRole(RoleNames.ACCEPTED_TC, RESEARCHER);
        addRole(RoleNames.ACCEPTED_TC, RESEARCH_MANAGER);
        addRole(RoleNames.ACTIVE, ADMINISTRATOR);
        addRole(RoleNames.ACTIVE, ICT_SUPPORT);
        addRole(RoleNames.ACTIVE, RESEARCHER);
        addRole(RoleNames.ACTIVE, RESEARCH_MANAGER);
    }

    private void addRole(RoleNames roleName, String username)
    {
        User user = User.findUsersByUsernameEquals(username).getSingleResult();
        Role role = Role.findRolesByNameEquals(roleName.toString()).getSingleResult();
        user.getRoles().add(role);
        user.merge();
    }

    private void createRole(RoleNames roleName, String displayName, String username)
    {
        String roleNameStr = roleName.toString();

        Role role = new Role();
        role.setName(roleNameStr);
        role.setDisplayName(displayName);
        role.persist();

        if (username != null)
        {
            User user = new User();
            user.setEnabled(true);
            user.setUserType(UserType.INTERNAL);
            user.setUsername(username);
            Md5PasswordEncoder encoder = new Md5PasswordEncoder();
            user.setPassword(encoder.encodePassword(username, null));
            user.setGivenname(MOCK_GIVEN);
            user.setSurname(MOCK_SURNAME);
            user.setEmail(MOCK_EMAIL);
            user.setInstitution(MOCK_INSTITUTION);
            user.getRoles().add(role);
            user.merge();
        }
    }

    @Given("^I have a UniKey user \"([^\"]*)\"$")
    public void unikeyUser(String username)
    {
        User user = new User();
        user.setEnabled(true);
        user.setUsername(username);
        user.setUserType(UserType.UNIKEY);
        user.setGivenname(MOCK_GIVEN);
        user.setSurname(MOCK_SURNAME);
        user.setEmail(MOCK_EMAIL);
        user.setInstitution(MOCK_INSTITUTION);
        Md5PasswordEncoder encoder = new Md5PasswordEncoder();
        user.setPassword(encoder.encodePassword(username, null));
        user.persist();
        Role role = Role.findRolesByNameEquals(RoleNames.ROLE_RESEARCHER.toString()).getSingleResult();
        user.getRoles().add(role);
        user.merge();
    }

    @Given("^I have an internal user \"([^\"]*)\"$")
    public void externalUser(String username)
    {
        User user = new User();
        user.setEnabled(true);
        user.setUsername(username);
        user.setUserType(UserType.INTERNAL);
        user.setGivenname(MOCK_GIVEN);
        user.setSurname(MOCK_SURNAME);
        user.setEmail(MOCK_EMAIL);
        user.setInstitution(MOCK_INSTITUTION);
        Md5PasswordEncoder encoder = new Md5PasswordEncoder();
        user.setPassword(encoder.encodePassword(username, null));
        user.persist();
        Role role = Role.findRolesByNameEquals(RoleNames.ROLE_RESEARCHER.toString()).getSingleResult();
        user.getRoles().add(role);
        user.merge();
    }

    @Given("^I log in as \"([^\"]*)\"$")
    public void iLogInAs(String username) throws InterruptedException
    {
        browser.get("http://localhost:7675/sydma-web/login");
        WebElement userNameField = browser.findElement(By.id("j_username"));
        WebElement passwordField = browser.findElement(By.id("j_password"));
        userNameField.sendKeys(username);
        passwordField.sendKeys(username);
        browser.findElement(By.xpath("//input[@id='proceed']")).click();
    }
}
