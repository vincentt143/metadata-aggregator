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

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

import au.org.intersect.sydma.webapp.exception.ResearchDatasetDBSchemaException;
import au.org.intersect.sydma.webapp.exception.ResearchDatasetDBSqlException;

/**
 * Entity storing specific information relating to a db created for the dataset
 * 
 * @version $Rev: 29 $
 */
@RooJavaBean
@RooToString(excludeFields = {"researchDataset", "dBBackupUsed", "lastRestoredBy"})
@RooEntity(persistenceUnit = "sydmaPU", finders = {"findResearchDatasetDBsByResearchDataset"})
public class ResearchDatasetDB
{
    private static final Logger LOG = LoggerFactory.getLogger(ResearchDatasetDB.class);

    @OneToOne
    @NotNull
    private ResearchDataset researchDataset;

    @ManyToOne
    @NotNull
    private DBSchema dbSchema;

    @NotNull
    @NotEmpty
    @Column(unique = true)
    private String dbName;

    @NotNull
    @NotEmpty
    private String dbHostname;

    // TODO CHECKSTYLE-OFF: MagicNumber
    @Size(max = 1000, message = "Must not exceed 1000 characters in length")
    @NotNull
    @NotEmpty(message = "Description is a required field")
    private String description;

    @Size(max = 1000, message = "Must not exceed 1000 characters in length")
    @javax.validation.constraints.Pattern(regexp = "\\s*\\S+[\\s\\S]*", message = "This is a required field")
    private String state;

    @OneToOne
    private DBBackup dBBackupUsed;

    @Size(max = 100, message = "Must not exceed 100 characters in length")
    private String dateOfRestoration;

    @OneToOne
    private User lastRestoredBy;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "researchDatasetDB")
    private List<ResearchDatabaseQuery> dbQueries = new ArrayList<ResearchDatabaseQuery>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "databaseInstance")
    private List<DBUser> dbUsers = new ArrayList<DBUser>();

    public ResearchDatasetDB()
    {
        super();
    }

    public ResearchDatasetDB(String description, DBSchema dbSchema, String dbHostname, ResearchDataset researchDataset)
    {
        this.description = description;
        this.dbSchema = dbSchema;
        this.dbHostname = dbHostname;
        this.researchDataset = researchDataset;
        this.dbName = generateDbName(researchDataset);
    }

    private String generateDbName(ResearchDataset researchDataset)
    {
        return "dataset_" + researchDataset.getId();
    }

    public void createInstance(final Connection connection)
    {
        String sql = "CREATE DATABASE IF NOT EXISTS " + dbName;
        try
        {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        }
        catch (SQLException e)
        {
            throw new ResearchDatasetDBSqlException("Failed to create database instance for dataset with id:"
                    + researchDataset.getId(), e);
        }
    }

    public void createSchema(final Connection connection)
    {
        List<String> sqlCommands = loadSchema();
        try
        {
            String currentCatalog = connection.getCatalog();
            connection.setCatalog(dbName);
            Statement statement = connection.createStatement();
            for (String sqlCommand : sqlCommands)
            {
                statement.executeUpdate(sqlCommand);
            }
            connection.setCatalog(currentCatalog);
        }
        catch (SQLException e)
        {
            throw new ResearchDatasetDBSqlException("Failed to create database schema for dataset with id:"
                    + researchDataset.getId(), e);
        }
    }

    private List<String> loadSchema()
    {
        try
        {
            return splitBySqlCommand(dbSchema.loadSchemaSql());
        }
        catch (IOException e)
        {
            throw new ResearchDatasetDBSchemaException("Failed to load dataset schema with name " + dbSchema.getName(),
                    e);
        }
    }

    private List<String> splitBySqlCommand(String loadSchemaSql)
    {
        List<String> commandList = new ArrayList<String>();
        Pattern p = Pattern.compile("([^;]*);");
        Matcher m = p.matcher(loadSchemaSql);
        while (m.find())
        {
            commandList.add(m.group(1) + ";");
        }
        return commandList;
    }

    public void dropInstance(Connection connection)
    {
        String sql = "DROP DATABASE " + dbName;
        try
        {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        }
        catch (SQLException e)
        {
            throw new ResearchDatasetDBSqlException("Failed to drop database instance for dataset with id:"
                    + researchDataset.getId(), e);
        }
    }

    public void addDBUser(DBUser dbUser)
    {
        if (this.dbUsers == null)
        {
            this.dbUsers = new ArrayList<DBUser>();
        }
        dbUsers.add(dbUser);
    }

    public DBUser findFullAccessDBUser()
    {
        for (DBUser dbUser : this.getDbUsers())
        {
            if (DBAccess.FULL_ACCESS.equals(dbUser.getAccessLevel()))
            {
                return dbUser;
            }
        }
        return null;
    }

    public DBUser findViewAccessDBUser()
    {
        for (DBUser dbUser : this.getDbUsers())
        {
            if (DBAccess.VIEW_ACCESS.equals(dbUser.getAccessLevel()))
            {
                return dbUser;
            }
        }
        return null;
    }

    public DBUser findUpdateAccessDBUser()
    {
        for (DBUser dbUser : this.getDbUsers())
        {
            if (DBAccess.UPDATE_ACCESS.equals(dbUser.getAccessLevel()))
            {
                return dbUser;
            }
        }
        return null;
    }
}
