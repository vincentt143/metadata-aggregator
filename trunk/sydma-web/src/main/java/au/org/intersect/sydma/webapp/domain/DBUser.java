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

package au.org.intersect.sydma.webapp.domain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

import au.org.intersect.sydma.webapp.exception.ResearchDatasetDBSqlException;

/**
 * Entity storing a user's access information to dataset databases
 * 
 * @version $Rev: 29 $
 */
@RooJavaBean
@RooToString
@RooEntity(persistenceUnit = "sydmaPU", finders = {"findDBUsersByDatabaseInstanceAndAccessLevel"})
public class DBUser
{
    private static final Logger LOG = LoggerFactory.getLogger(DBUser.class);

    @NotNull
    @ManyToOne
    private ResearchDatasetDB databaseInstance;

    @NotNull
    @Column(unique = true)
    private String dbUsername;

    @NotNull
    private String dbPassword;

    @Enumerated(EnumType.STRING)
    @NotNull
    private DBAccess accessLevel;

    protected DBUser()
    {

    }

    public DBUser(String dbPassword, DBAccess accessLevel, ResearchDatasetDB databaseInstance)
    {
        this.dbPassword = dbPassword;
        this.databaseInstance = databaseInstance;
        this.accessLevel = accessLevel;
        this.dbUsername = generateDBUsername(databaseInstance.getResearchDataset(), accessLevel);
    }

    private String generateDBUsername(ResearchDataset researchDataset, DBAccess accessLevel)
    {
        return "sydma_user_" + researchDataset.getId() + accessLevel.getDbSuffix();
    }
    
    public void changeUserDBPassword(Connection connection, String password)
    {
        String query = "update user set password=PASSWORD('" + password + "') where User='" + this.dbUsername + "';";
        String useDatabase = "use mysql;";
        String flushPrivileges = "flush privileges;";
        try
        {
            Statement st = null;
            st = connection.createStatement();
            
            st.execute(useDatabase);
            st.execute(query);
            st.execute(flushPrivileges);
        }
        catch (SQLException sqle)
        {
            throw new ResearchDatasetDBSqlException("Failed to change DB password", sqle);
        }
    }

    public void createAndGrantUser(Connection connection)
    {
        String sql = "GRANT " + accessLevel.getSqlGrants() + " ON " + databaseInstance.getDbName()
                + ".* TO ?@'%' IDENTIFIED BY ?";
        try
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, dbUsername);
            statement.setString(2, dbPassword);
            statement.execute();
        }
        catch (SQLException e)
        {
            throw new ResearchDatasetDBSqlException("Failed to create and grant user", e);
        }
    }

    public void dropUser(Connection connection)
    {
        String sql = "DROP USER " + dbUsername;
        try
        {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        }
        catch (SQLException e)
        {
            LOG.error("Failed to drop db user", e);
            throw new ResearchDatasetDBSqlException("Failed to drop db user with name:" + dbUsername, e);
        }
    }
}
