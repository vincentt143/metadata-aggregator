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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import au.org.intersect.sydma.webapp.domain.DBAccess;
import au.org.intersect.sydma.webapp.domain.DBSchema;
import au.org.intersect.sydma.webapp.domain.DBUser;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchDatasetDB;
import au.org.intersect.sydma.webapp.dto.DBInstanceDto;
import au.org.intersect.sydma.webapp.exception.NoneUniqueNameException;
import au.org.intersect.sydma.webapp.exception.ResearchDatasetDBAlreadyExistsException;
import au.org.intersect.sydma.webapp.exception.ResearchDatasetDBDoesNotExistException;
import au.org.intersect.sydma.webapp.exception.ResearchDatasetDBSqlException;
import au.org.intersect.sydma.webapp.util.DBConnectionHelper;
import au.org.intersect.sydma.webapp.util.DBHostnameHelper;
import au.org.intersect.sydma.webapp.util.DBPasswordHelper;

/**
 * Manages creation of db schema for dataset
 * 
 * @version $Rev: 29 $
 */
@Transactional("sydmaPU")
// TODO CHECKSTYLE-OFF: IllegalCatch
public class ResearchDatasetDBServiceImpl implements ResearchDatasetDBService
{
    private static final Logger LOG = LoggerFactory.getLogger(ResearchDatasetDBServiceImpl.class);
    private static final int DB_STATEMENT_RETRY = 3;

    @Autowired
    private DBConnectionHelper dbConnectionHelper;

    @Autowired
    private DBHostnameHelper dbHostnameHelper;

    @Autowired
    private DBPasswordHelper dbPasswordHelper;

    @Autowired
    private FileAccessService fileAccessService;
    
    public ResearchDatasetDBServiceImpl()
    {
    }

    @Override
    public void commitReverseEngineeredDatabase(List<String> ddlCommands, ResearchDatasetDB datasetDB, DBSchema schema)
        throws NoneUniqueNameException
    {
        Connection connection = null;
        try
        {
            DBSchema existingSchema = DBSchema.findDBSchema(schema.getName());
            if (existingSchema != null)
            {
                throw new NoneUniqueNameException("Schema name already in use");
            }
            connection = startConnection();
            schema.setFilename(schema.getName() + ".sql");
            schema.createNewSchemaFile(ddlCommands);
            datasetDB.setDbSchema(schema);
            schema.persist();
        }
        catch (IOException e)
        {
            LOG.error("Cannot write schema file to folder: ", e);
        }
        finally
        {
            closeConnection(connection);
        }
    }

    @Override
    public ResearchDatasetDB createDBForDataset(ResearchDataset dataset, DBInstanceDto dbInstanceDto)
    {

        if (dataset.getDatabaseInstance() != null)
        {
            throw new ResearchDatasetDBAlreadyExistsException("A Database Instance already exists for Dataset id:["
                    + dataset.getId() + "]");
        }

        Connection connection = startConnection();
        DBUser faDbUser = null;
        DBUser uaDbUser = null;
        DBUser vaDbUser = null;

        // Create DB Instance and load schema
        ResearchDatasetDB datasetDB = null;
        try
        {
            datasetDB = createDBInstanceWithSchema(dbInstanceDto, dataset, connection);
            initializeSchema(datasetDB, connection);
            // Create Users of full access, update access and view access

            faDbUser = createDBUserWithGrant(datasetDB, DBAccess.FULL_ACCESS, connection);
            uaDbUser = createDBUserWithGrant(datasetDB, DBAccess.UPDATE_ACCESS, connection);
            vaDbUser = createDBUserWithGrant(datasetDB, DBAccess.VIEW_ACCESS, connection);

            finishConnection(connection);
        }
        catch (RuntimeException e)
        {
            LOG.error("RunTimeException encountered while creating db instance, will rollback", e);
            if (faDbUser != null)
            {
                faDbUser.dropUser(connection);
            }
            if (uaDbUser != null)
            {
                uaDbUser.dropUser(connection);
            }
            if (vaDbUser != null)
            {
                vaDbUser.dropUser(connection);
            }

            if (datasetDB != null)
            {
                datasetDB.dropInstance(connection);
            }

            throw e;
        }
        finally
        {
            closeConnection(connection);
        }

        return datasetDB;

    }

    @Override
    public void deleteDBForDataset(ResearchDataset dataset)
    {
        ResearchDatasetDB dbInstance = dataset.getDatabaseInstance();
        if (dbInstance == null)
        {

            throw new ResearchDatasetDBDoesNotExistException("Database Instance does not exist for dataset with id:"
                    + dataset.getId());
        }
        List<DBUser> dbUsers = dbInstance.getDbUsers();
        Connection connection = startConnection();
        try
        {
            // the record in sydma database will always be deleted regardless of
            // success of sql commands
            for (DBUser dbUser : dbUsers)
            {
                dropUser(dbUser, connection);
            }
            dropDbInstance(dbInstance, connection);
        }
        catch (Exception e)
        {
            // suppress
        }
        finally
        {
            closeConnection(connection);
        }

        dbInstance.remove();
        dataset.setDatabaseInstance(null);
        dataset.merge();
    }

    @Override
    public boolean changeDBPassword(DBUser dbUser)
    {
        String password = dbPasswordHelper.assignPassword();

        Connection connection = startConnection();
        try
        {
            dbUser.changeUserDBPassword(connection, password);
            dbUser.setDbPassword(password);
            dbUser.merge();
        }
        catch (Exception e)
        {
            LOG.error("Change password to database failed for user " + dbUser.getDbUsername());
            return false;
        }

        return true;
    }

    private void dropUser(DBUser dbUser, Connection connection)
    {
        int retries = 0;
        while (retries < DB_STATEMENT_RETRY)
        {
            retries++;
            try
            {
                dbUser.dropUser(connection);
                break;
            }
            catch (Exception e)
            {
                LOG.error("Exception encountered while dropping user, will suppress", e);
            }
        }
    }

    private void dropDbInstance(ResearchDatasetDB dbInstance, Connection connection)
    {
        int retries = 0;
        while (retries < DB_STATEMENT_RETRY)
        {
            retries++;
            try
            {
                dbInstance.dropInstance(connection);
                break;
            }
            catch (Exception e)
            {
                LOG.error("Exception encountered while deleting db instance, will suppress", e);
            }
        }
    }

    private DBUser createDBUserWithGrant(ResearchDatasetDB datasetDB, DBAccess dbAccess, Connection connection)
    {

        String dbPassword = dbPasswordHelper.assignPassword();
        DBUser dbUser = new DBUser(dbPassword, dbAccess, datasetDB);
        datasetDB.addDBUser(dbUser);
        datasetDB.merge();
        dbUser.createAndGrantUser(connection);
        return dbUser;
    }

    private ResearchDatasetDB createDBInstanceWithSchema(DBInstanceDto dbInstanceDto, ResearchDataset dataset,
            Connection connection)
    {

        String dbHostname = dbHostnameHelper.assignHostname(dataset);

        ResearchDatasetDB datasetDB = new ResearchDatasetDB(dbInstanceDto.getDescription(),
                dbInstanceDto.getDbSchema(), dbHostname, dataset);
        dataset.setDatabaseInstance(datasetDB);
        datasetDB.persist();

        datasetDB.createInstance(connection);

        return datasetDB;
    }

    private void initializeSchema(ResearchDatasetDB datasetDB, Connection connection)
    {
        datasetDB.createSchema(connection);
    }

    private Connection startConnection()
    {
        Connection con = dbConnectionHelper.obtainConnection();
        try
        {
            con.setAutoCommit(false);
        }
        catch (SQLException e)
        {
            throw new ResearchDatasetDBSqlException("Failed to initiate connection", e);
        }

        return con;
    }

    private void finishConnection(Connection connection)
    {
        try
        {
            connection.commit();
        }
        catch (SQLException e)
        {
            LOG.error("Failed to commit connection changes", e);
            rollBack(connection);
        }
    }

    private void closeConnection(Connection connection)
    {
        try
        {
            if (connection != null)
            {
                connection.close();
            }
        }
        catch (SQLException e)
        {
            LOG.error("Failed to close connection", e);
        }
    }

    private void rollBack(Connection connection)
    {
        try
        {
            connection.rollback();
        }
        catch (SQLException e)
        {
            LOG.error("Failed to rollback connection", e);
        }
    }

    public void setDbConnectionHelper(DBConnectionHelper dbConnectionHelper)
    {
        this.dbConnectionHelper = dbConnectionHelper;
    }

    public void setDbHostnameHelper(DBHostnameHelper dbHostnameHelper)
    {
        this.dbHostnameHelper = dbHostnameHelper;
    }

    public void setDbPasswordHelper(DBPasswordHelper dbPasswordHelper)
    {
        this.dbPasswordHelper = dbPasswordHelper;
    }

    public void setFileAccessService(FileAccessService fileAccessService)
    {
        this.fileAccessService = fileAccessService;
    }

}
