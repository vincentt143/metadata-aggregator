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

import java.sql.SQLException;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mchange.v2.c3p0.PooledDataSource;

import cuke4duke.annotation.After;
import cuke4duke.annotation.Before;
import cuke4duke.spring.StepDefinitions;

import au.org.intersect.sydma.webapp.domain.PublicAccessRight;
import au.org.intersect.sydma.webapp.domain.RdsRequest;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.Role;
import au.org.intersect.sydma.webapp.domain.User;

/**
 * Start and End steps
 * 
 * @version $Rev: 29 $
 */
@StepDefinitions
public class CleanupSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupSteps.class);

    @Autowired
    private WebDriver browser;

    @Autowired
    private PooledDataSource dataSource;
    
    @Before
    public void before()
    {
        cleanup();
    }

    @After
    public void after() throws SQLException
    {
        cleanup();
        browser.close();
        dataSource.close();
    }
     
    private void cleanup()
    {
        LOGGER.debug(" *** Starting Database Cleanup ***");
        for (RdsRequest rdsRequest : RdsRequest.findAllRdsRequests())
        {
            rdsRequest.remove();
        }

        for (ResearchGroup researchGroup : ResearchGroup.findAllResearchGroups())
        {
            researchGroup.remove();
        }

        for (PublicAccessRight right : PublicAccessRight.findAllPublicAccessRights())
        {
            right.remove();
        }

        for (User user : User.findAllUsers())
        {
            user.remove();
        }

        for (Role role : Role.findAllRoles())
        {
            role.remove();
        }
        LOGGER.debug(" *** Done with Database Cleanup ***");
    }
}
