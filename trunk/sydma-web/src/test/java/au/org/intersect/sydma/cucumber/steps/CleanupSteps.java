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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mchange.v2.c3p0.PooledDataSource;

import cuke4duke.annotation.After;
import cuke4duke.annotation.Before;
import cuke4duke.spring.StepDefinitions;

import au.org.intersect.sydma.webapp.domain.ActivityLog;
import au.org.intersect.sydma.webapp.domain.MasterVocabularyTerm;
import au.org.intersect.sydma.webapp.domain.PermissionEntry;
import au.org.intersect.sydma.webapp.domain.PublicAccessRight;
import au.org.intersect.sydma.webapp.domain.RdsRequest;
import au.org.intersect.sydma.webapp.domain.ResearchDatabaseQuery;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.Role;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.domain.Vocabulary;

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
    
    @Autowired
    private CommonsHttpSolrServer solrServer;


    @Before
    public void before() throws SQLException
    {
        cleanup();
    }

    @After
    public void after()
    {
//        try
//        {
//            solrServer.deleteByQuery("*:*");
//        }
//        catch (Exception e)
//        {
//            LOGGER.error("Error deleting solr entries");
//        }
        
        browser.close();
        try
        {
            cleanup();
        }
        catch (SQLException e)
        {
            LOGGER.error("Error deleting database entries");
        }
        try
        {
            dataSource.close();
        }
        catch (SQLException e)
        {
            LOGGER.error("Error closing database connection");
        }
    }
    
    //TODO CHECKSTYLE-OFF: CyclomaticComplexityCheck
    //TODO CHECKSTYLE-OFF: NPathComplexityCheck
    private void cleanup() throws SQLException
    {
        LOGGER.debug(" *** Starting Database Cleanup ***");
        Connection connection = dataSource.getConnection();
        for (MasterVocabularyTerm terms : MasterVocabularyTerm.findAllMasterVocabularyTerms())
        {
            terms.remove();
        }
        
        for (Vocabulary vocabulary : Vocabulary.findAllVocabularys())
        {
            vocabulary.remove();
        }
        
        for (PermissionEntry entry : PermissionEntry.findAllPermissionEntrys())
        {
            entry.remove();
        }
        
        for (ResearchDatabaseQuery query : ResearchDatabaseQuery.findAllResearchDatabaseQuerys())
        {
            query.remove();
        }
        
        for (ActivityLog logEntry : ActivityLog.findAllActivityLogs())
        {
            logEntry.remove();
        }

        dropSydmaDbInstance(connection);
        dropSydmaUser(connection); 
        connection.close();

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

    private void dropSydmaUser(Connection connection) throws SQLException
    {
        Statement statement = connection.createStatement();
        ResultSet results = statement.executeQuery("SELECT User from mysql.user");
        while (results.next())
        {
            String username = results.getString("User");
            if (username.startsWith("sydma_user_"))
            {
                Statement dropStatement = connection.createStatement();
                dropStatement.executeUpdate("DROP USER " + username);
                dropStatement.close();
            }
        }
    }

    private void dropSydmaDbInstance(Connection connection) throws SQLException
    {
        Statement statement = connection.createStatement();
        ResultSet results = statement.executeQuery("SHOW databases");
        while (results.next())
        {
            String database = results.getString("Database");
            if (database.startsWith("dataset_"))
            {
                Statement dropStatement = connection.createStatement();
                dropStatement.executeUpdate("DROP DATABASE " + database);
                dropStatement.close();

            }
        }
    }
}
