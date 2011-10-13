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

package au.org.intersect.sydma.webapp.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import au.org.intersect.sydma.webapp.domain.DBSchema;
import au.org.intersect.sydma.webapp.domain.DBUser;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchDatasetDB;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.exception.ResearchDatasetDBAlreadyExistsException;
import au.org.intersect.sydma.webapp.exception.ResearchDatasetDBDoesNotExistException;
import au.org.intersect.sydma.webapp.exception.ResearchDatasetDBSchemaException;
import au.org.intersect.sydma.webapp.exception.ResearchDatasetDBSqlException;
import au.org.intersect.sydma.webapp.util.DBConnectionHelper;

/**
 * Manages creation of db schema for dataset
 * 
 * @version $Rev: 29 $
 */
@Service
@Transactional("sydmaPU")
public class ResearchDatasetDBServiceImpl implements ResearchDatasetDBService
{
    private static final Logger LOG = LoggerFactory.getLogger(ResearchDatasetDBServiceImpl.class);

    private static final String DEFAULT_PASSWORD = "password";

    @Autowired
    private DBConnectionHelper dbConnectionHelper;
    
    @Autowired
    private DBSchemaService dbSchemaService;
    
    public ResearchDatasetDBServiceImpl()
    {
        
    }
    

    @Override
    public DBUser grantUserAccessToDataset(User user, ResearchDataset dataset)
    {

        ResearchDatasetDB datasetDB = dataset.getDatabaseInstance();
        if (datasetDB == null)
        {
            throw new ResearchDatasetDBDoesNotExistException();
        }
        Connection connection = startConnection();
        Savepoint savepoint;
        try
        {
            savepoint = connection.setSavepoint();
        }
        catch (SQLException e1)
        {
            throw new ResearchDatasetDBSqlException("Failed to create connection savepoint");
        }
        try
        {
            DBUser dbUser = grantUserAccessToDatasetDB(user, datasetDB, connection);
            return dbUser;
        }
        // TODO CHECKSTYLE-OFF: Illegal Catch
        catch (RuntimeException e)
        {
            rollBack(connection, savepoint);
            throw e;
        }
        finally
        {
            closeConnection(connection);
        }
    }


    private DBUser grantUserAccessToDatasetDB(User user, ResearchDatasetDB datasetDB, Connection connection)
    {
        DBUser dbUser = user.getDbUser();
        if (dbUser == null)
        {
            //if user doesn't exist we default to a standard password
            LOG.info("DB User does not exist, creating new one");
            dbUser = this.createDBUser(user, DEFAULT_PASSWORD, connection);
        }
        boolean success = dbSchemaService.grantUser(dbUser.getDbUsername(), datasetDB.getDbName(), connection);
        LOG.info("Granting user access: " + success);
        return dbUser;
    }

    private DBUser createDBUser(User user, String password, Connection connection)
    {
        String dbUsername = "user_" + user.getId();
        DBUser dbUser = new DBUser(dbUsername, password);
        user.setDbUser(dbUser);   
        dbUser.setUser(user);
        user.merge();
        dbSchemaService.createUser(dbUsername, password, connection);
        
        return dbUser;
    }

    @Override
    public ResearchDatasetDB createDBForDatasetAndGrantUser(ResearchDataset dataset, DBSchema dbSchema, User user)
    {
        
        if (dataset.getDatabaseInstance() != null)
        {
            throw new ResearchDatasetDBAlreadyExistsException(
                    "A Database Instance already exists for Dataset id:[" + dataset.getId() + "]");
        }


        Connection connection = startConnection();
        Savepoint savepoint;
        try
        {
            savepoint = connection.setSavepoint();
        }
        catch (SQLException e1)
        {
            throw new ResearchDatasetDBSqlException("Failed to create connection savepoint");
        }
        try
        {

            ResearchDatasetDB datasetDB = this.createDBForDataset(dbSchema, dataset, connection);

            // create user

            this.grantUserAccessToDatasetDB(user, datasetDB, connection);

            finishConnection(connection, savepoint);
            return datasetDB;
        }
        // TODO CHECKSTYLE-OFF: Illegal Catch
        catch (RuntimeException e)
        {
            LOG.error("Exception occurred, will perform rollback ", e);
            rollBack(connection, savepoint);
            throw e;
        }
        finally
        {
            closeConnection(connection);
        }
    }

    private ResearchDatasetDB createDBForDataset(DBSchema dbSchema, ResearchDataset dataset, Connection connection)
    {
        String dbName = "dataset_" + dataset.getId();
        String schemaSql;
        try
        {
            schemaSql = dbSchema.loadSchemaSql();
        }
        catch (IOException e)
        {
            LOG.info("ERROR LOADING FILE", e);
            throw new ResearchDatasetDBSchemaException("Failed to load dataset schema", e);
        }
        ResearchDatasetDB datasetDB = new ResearchDatasetDB(dbName, dbSchema, dataset);
        dbSchemaService.createDatabase(dbName, connection);
        dbSchemaService.createSchemaInDB(dbName, schemaSql, connection);
        dataset.setDatabaseInstance(datasetDB);
        dataset.merge();
        return datasetDB;
    }

    private Connection startConnection()
    {
        Connection con = dbConnectionHelper.obtainConnection();
        try
        {
            con.setAutoCommit(false);
            LOG.info("AUTO COMMIT " + con.getAutoCommit());
        }
        catch (SQLException e)
        {
            throw new ResearchDatasetDBSqlException("Failed to initiate connection", e);
        }

        return con;
    }

    private void finishConnection(Connection connection, Savepoint savepoint)
    {
        try
        {
            connection.commit();
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            LOG.error("Failed to commit connection changes", e);
            rollBack(connection, savepoint);
        }
    }

    private void closeConnection(Connection connection)
    {
        try
        {
            connection.close();
        }
        catch (SQLException e)
        {
            LOG.error("Failed to close connection", e);
        }
    }

    private void rollBack(Connection connection, Savepoint savepoint)
    {
        try
        {
            connection.rollback(savepoint);
        }
        catch (SQLException e)
        {
            LOG.error("Failed to rollback connection", e);
        }
    }
    
    
    
    public void setDBSchemaService(DBSchemaService dbSchemaService)
    {
        this.dbSchemaService = dbSchemaService;
    }
    

    public void setDbConnectionHelper(DBConnectionHelper dbConnectionHelper)
    {
        this.dbConnectionHelper = dbConnectionHelper;
    }
}
