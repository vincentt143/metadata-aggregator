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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.staticmock.MockStaticEntityMethods;

import au.org.intersect.sydma.webapp.domain.DBAccess;
import au.org.intersect.sydma.webapp.domain.DBSchema;
import au.org.intersect.sydma.webapp.domain.DBUser;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchDatasetDB;
import au.org.intersect.sydma.webapp.util.DBConnectionHelper;
import au.org.intersect.sydma.webapp.util.DBHostnameHelper;
import au.org.intersect.sydma.webapp.util.DBPasswordHelper;

@MockStaticEntityMethods
// TODO CHECKSTYLE-OFF: ExecutableStatementCount
public class ResearchDatasetDBServiceImplDeleteTest
{
    private ResearchDatasetDBServiceImpl dbService;

    private DBConnectionHelper dbConnectionHelper;

    private DBHostnameHelper dbHostnameHelper;

    private DBPasswordHelper dbPasswordHelper;

    private Connection connection;
    private Statement statement;

    private ResearchDataset dataset;
    private ResearchDatasetDB dbInstance;

    private Long datasetId = new Long(1);

    private String dbName = "dataset_" + datasetId;
    private String expectedDropDbSql = "DROP DATABASE " + dbName;

    private String dbUserPrefix = "sydma_user_";
    private String faUsername = dbUserPrefix + datasetId + DBAccess.FULL_ACCESS.getDbSuffix();
    private String uaUsername = dbUserPrefix + datasetId + DBAccess.UPDATE_ACCESS.getDbSuffix();
    private String vaUsername = dbUserPrefix + datasetId + DBAccess.VIEW_ACCESS.getDbSuffix();

    private String expectedDropUserFaSql = "DROP USER " + faUsername;
    private String expectedDropUserUaSql = "DROP USER " + uaUsername;
    private String expectedDropUserVaSql = "DROP USER " + vaUsername;

    @Before
    public void setUp() throws SQLException
    {

        ResearchDataset realDataset = createResearchDataset(datasetId);
        ResearchDatasetDB realInstance = createDBInstance(realDataset);

        dbConnectionHelper = Mockito.mock(DBConnectionHelper.class);
        dbHostnameHelper = Mockito.mock(DBHostnameHelper.class);
        dbPasswordHelper = Mockito.mock(DBPasswordHelper.class);
        connection = Mockito.mock(Connection.class);
        statement = Mockito.mock(Statement.class);

        dbService = new ResearchDatasetDBServiceImpl();
        dbService.setDbConnectionHelper(dbConnectionHelper);
        dbService.setDbHostnameHelper(dbHostnameHelper);
        dbService.setDbPasswordHelper(dbPasswordHelper);

        dbInstance = Mockito.spy(realInstance);
        realDataset.setDatabaseInstance(dbInstance);
        dataset = Mockito.spy(realDataset);

        doReturn(dataset).when(dataset).merge();
        doNothing().when(dbInstance).remove();

        when(dbConnectionHelper.obtainConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
    }

    @Test
    public void testDeleteDbInstance() throws SQLException
    {

        dbService.deleteDBForDataset(dataset);

        verify(statement).executeUpdate(eq(expectedDropUserFaSql));
        verify(statement).executeUpdate(eq(expectedDropUserUaSql));
        verify(statement).executeUpdate(eq(expectedDropUserVaSql));
        verify(statement).executeUpdate(eq(expectedDropDbSql));

        verify(dataset).merge();

        verify(dbInstance).remove();

        assertNull("Dataset Instance was not removed from dataset", dataset.getDatabaseInstance());
    }

    @Test
    public void testDeleteDbInstanceWithDropUserFailure() throws SQLException
    {

        when(statement.executeUpdate(expectedDropUserFaSql)).thenThrow(new SQLException("Error Suppression Test"));

        try
        {
            dbService.deleteDBForDataset(dataset);    
        }
        catch (Exception exception)
        {
            fail("All exceptions should be suppressed");
        }
        
        
        //assert dropping of database continues despite exception
        verify(statement, times(3)).executeUpdate(eq(expectedDropUserFaSql));
        verify(statement).executeUpdate(eq(expectedDropUserUaSql));
        verify(statement).executeUpdate(eq(expectedDropUserVaSql));
        verify(statement).executeUpdate(eq(expectedDropDbSql));

        verify(dataset).merge();

        verify(dbInstance).remove();

        assertNull("Dataset Instance was not removed from dataset", dataset.getDatabaseInstance());
    }
    
    @Test
    public void testDeleteDbInstanceWithDropDatabaseFailure() throws SQLException
    {

        when(statement.executeUpdate(expectedDropDbSql)).thenThrow(new SQLException("Error Suppression Test"));

        try
        {
            dbService.deleteDBForDataset(dataset);    
        }
        catch (Exception exception)
        {
            fail("All exceptions should be suppressed");
        }
        
        
        //assert dropping of database continues despite exception
        verify(statement).executeUpdate(eq(expectedDropUserFaSql));
        verify(statement).executeUpdate(eq(expectedDropUserUaSql));
        verify(statement).executeUpdate(eq(expectedDropUserVaSql));
        verify(statement, times(3)).executeUpdate(eq(expectedDropDbSql));

        verify(dataset).merge();

        verify(dbInstance).remove();

        assertNull("Dataset Instance was not removed from dataset", dataset.getDatabaseInstance());
    }

    private ResearchDataset createResearchDataset(Long id)
    {
        ResearchDataset dataset = new ResearchDataset();
        dataset.setId(id);

        return dataset;
    }

    private ResearchDatasetDB createDBInstance(ResearchDataset dataset)
    {
        DBSchema schema = new DBSchema("schema1", "schemafile");
        ResearchDatasetDB dbInstance = new ResearchDatasetDB("Description", schema, "hostname", dataset);

        DBUser userFa = new DBUser("password", DBAccess.FULL_ACCESS, dbInstance);
        DBUser userUa = new DBUser("password", DBAccess.UPDATE_ACCESS, dbInstance);
        DBUser userVa = new DBUser("password", DBAccess.VIEW_ACCESS, dbInstance);
        dbInstance.addDBUser(userFa);
        dbInstance.addDBUser(userUa);
        dbInstance.addDBUser(userVa);

        return dbInstance;
    }
}
