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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.staticmock.AnnotationDrivenStaticEntityMockingControl;
import org.springframework.mock.staticmock.MockStaticEntityMethods;

import au.org.intersect.sydma.webapp.domain.DBAccess;
import au.org.intersect.sydma.webapp.domain.DBSchema;
import au.org.intersect.sydma.webapp.domain.DBUser;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchDatasetDB;
import au.org.intersect.sydma.webapp.dto.DBInstanceDto;
import au.org.intersect.sydma.webapp.exception.ResearchDatasetDBSqlException;
import au.org.intersect.sydma.webapp.util.DBConnectionHelper;
import au.org.intersect.sydma.webapp.util.DBHostnameHelper;
import au.org.intersect.sydma.webapp.util.DBPasswordHelper;

@MockStaticEntityMethods
// TODO CHECKSTYLE-OFF: ExecutableStatementCount
public class ResearchDatasetDBServiceImplTest
{

    private ResearchDatasetDBServiceImpl researchDatasetDBService;

    private DBConnectionHelper dbConnectionHelper;

    private DBHostnameHelper dbHostnameHelper;

    private DBPasswordHelper dbPasswordHelper;
    
    private FileAccessService fileAccessService;

    private Connection connection;
    private Statement statement;

    private EntityManager entityManager;

    private String schemaSql = "SOME SQL;";
    private Long datasetId = new Long(1);
    private String schemaName = "schema";
    private String schemaFile = "schema.sql";
    private String dbName = "dataset_" + datasetId;
    private Long userId = new Long(1);
    private String dbUserPrefix = "sydma_user_";
    private String password = "password";
    private String dbHostname = "host1";

    private DBSchema dbSchema;
    
    private DBInstanceDto dbInstanceDto;
    
    private String dbInstanceDescription = "description";

    private String expectedCreateFaUserSql = "GRANT " + DBAccess.FULL_ACCESS.getSqlGrants() + " ON " + dbName
            + ".* TO ?@'%' IDENTIFIED BY ?";

    private String expectedCreateUaUserSql = "GRANT " + DBAccess.UPDATE_ACCESS.getSqlGrants() + " ON " + dbName
            + ".* TO ?@'%' IDENTIFIED BY ?";

    private String expectedCreateVaUserSql = "GRANT " + DBAccess.VIEW_ACCESS.getSqlGrants() + " ON " + dbName
            + ".* TO ?@'%' IDENTIFIED BY ?";
    
    private String faUsername = dbUserPrefix + datasetId + DBAccess.FULL_ACCESS.getDbSuffix();
    private String uaUsername = dbUserPrefix + datasetId + DBAccess.UPDATE_ACCESS.getDbSuffix();
    private String vaUsername = dbUserPrefix + datasetId + DBAccess.VIEW_ACCESS.getDbSuffix();

    private String expectedCreateDBSql = "CREATE DATABASE IF NOT EXISTS " + dbName;
    
    private String expectedDropDbSql = "DROP DATABASE " + dbName;      

    private String expectedCreateSchemaSql = schemaSql;
    
    

    @Before
    public void setUp() throws IOException, SQLException
    {
        dbConnectionHelper = Mockito.mock(DBConnectionHelper.class);
        dbHostnameHelper = Mockito.mock(DBHostnameHelper.class);
        dbPasswordHelper = Mockito.mock(DBPasswordHelper.class);
        fileAccessService = Mockito.mock(FileAccessService.class);
        connection = Mockito.mock(Connection.class);
        statement = Mockito.mock(Statement.class);
        entityManager = Mockito.mock(EntityManager.class);

        researchDatasetDBService = new ResearchDatasetDBServiceImpl();
        researchDatasetDBService.setDbConnectionHelper(dbConnectionHelper);
        researchDatasetDBService.setDbHostnameHelper(dbHostnameHelper);
        researchDatasetDBService.setDbPasswordHelper(dbPasswordHelper);
        researchDatasetDBService.setFileAccessService(fileAccessService);

        when(dbHostnameHelper.assignHostname(any(ResearchDataset.class))).thenReturn(dbHostname);
        when(dbPasswordHelper.assignPassword()).thenReturn(password);

        DBSchema schemaEntity = new DBSchema(schemaName, schemaFile);
        dbSchema = spy(schemaEntity);
        doReturn(schemaSql).when(dbSchema).loadSchemaSql();

        when(dbConnectionHelper.obtainConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        
        dbInstanceDto = new DBInstanceDto();
        dbInstanceDto.setDbSchema(dbSchema);
        dbInstanceDto.setDescription(dbInstanceDescription);
    }

    @Test
    public void testCreateDbInstance() throws SQLException
    {
        PreparedStatement preparedStatementFa = Mockito.mock(PreparedStatement.class);
        PreparedStatement preparedStatementUa = Mockito.mock(PreparedStatement.class);
        PreparedStatement preparedStatementVa = Mockito.mock(PreparedStatement.class);

        when(connection.prepareStatement(expectedCreateFaUserSql)).thenReturn(preparedStatementFa);
        when(connection.prepareStatement(expectedCreateUaUserSql)).thenReturn(preparedStatementUa);
        when(connection.prepareStatement(expectedCreateVaUserSql)).thenReturn(preparedStatementVa);

        ResearchDataset dataset = new ResearchDataset();
        dataset.setId(datasetId);
        ResearchDataset spyDataset = spy(dataset);
        doReturn(spyDataset).when(spyDataset).merge();
        
        ResearchDatasetDB.entityManager();
        AnnotationDrivenStaticEntityMockingControl.expectReturn(entityManager);

        AnnotationDrivenStaticEntityMockingControl.playback();


        // execute call
        researchDatasetDBService.createDBForDataset(spyDataset, dbInstanceDto);

        ResearchDatasetDB createdDbInstance = spyDataset.getDatabaseInstance();

        assertNotNull("DB Instance was not created", createdDbInstance);

        // verify db and schema execution
        verify(statement).executeUpdate(eq(expectedCreateDBSql));

        verify(statement).executeUpdate(eq(expectedCreateSchemaSql));

        // verify created users and statement
        DBUser faUser = createdDbInstance.findFullAccessDBUser();
        DBUser uaUser = createdDbInstance.findUpdateAccessDBUser();
        DBUser vaUser = createdDbInstance.findViewAccessDBUser();

        assertNotNull("Full Access DBUser was not created", faUser);
        assertEquals("faUser has wrong access level", DBAccess.FULL_ACCESS, faUser.getAccessLevel());

        assertNotNull("Update Access DBUser was not created", uaUser);
        assertEquals("uaUser has wrong access level", DBAccess.UPDATE_ACCESS, uaUser.getAccessLevel());

        assertNotNull("View Access DBUser was not created", vaUser);
        assertEquals("vaUser has wrong access level", DBAccess.VIEW_ACCESS, vaUser.getAccessLevel());

        verify(dbPasswordHelper, times(3)).assignPassword();

        verify(connection).prepareStatement(eq(expectedCreateFaUserSql));
        verify(connection).prepareStatement(eq(expectedCreateUaUserSql));
        verify(connection).prepareStatement(eq(expectedCreateVaUserSql));

        verify(preparedStatementFa).setString(eq(1), eq(faUser.getDbUsername()));
        verify(preparedStatementFa).setString(eq(2), eq(password));

        verify(preparedStatementUa).setString(eq(1), eq(uaUser.getDbUsername()));
        verify(preparedStatementUa).setString(eq(2), eq(password));

        verify(preparedStatementVa).setString(eq(1), eq(vaUser.getDbUsername()));
        verify(preparedStatementVa).setString(eq(2), eq(password));

    }
    

    @Test
    public void testCreateDbInstanceRollbackWithFailureAtLastUser() throws SQLException
    {
        //not concerned about particulars
        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.prepareStatement(expectedCreateVaUserSql)).thenThrow(new SQLException("Test Rollback"));
        

        ResearchDatasetDB.entityManager();
        AnnotationDrivenStaticEntityMockingControl.expectReturn(entityManager);

        AnnotationDrivenStaticEntityMockingControl.playback();

        ResearchDataset dataset = new ResearchDataset();
        dataset.setId(datasetId);
        ResearchDataset spyDataset = spy(dataset);
        doReturn(spyDataset).when(spyDataset).merge();

        // execute call
        boolean exceptionRaised = false;
        try
        {
            researchDatasetDBService.createDBForDataset(spyDataset, dbInstanceDto);    
        }
        catch (ResearchDatasetDBSqlException e)
        {
            exceptionRaised = true;
        }
        
        assertTrue(exceptionRaised);
        
        
        //verify database instance is created then dropped
        verify(statement).executeUpdate(expectedCreateDBSql);
        verify(statement).executeUpdate(expectedDropDbSql);
        
        String dropFaUser = "DROP USER " + faUsername;
        String dropUaUser = "DROP USER " + uaUsername;
        String dropVaUser = "DROP USER " + vaUsername;
        verify(statement).executeUpdate(dropFaUser);
        verify(statement).executeUpdate(dropUaUser);
        verify(statement, never()).executeUpdate(dropVaUser);
    }
    

    @Test
    public void testCreateDbInstanceRollbackWithFailureAtSchema() throws SQLException
    {
        //not concerned about particulars
        
        
        
        ResearchDatasetDB.entityManager();
        AnnotationDrivenStaticEntityMockingControl.expectReturn(entityManager);

        AnnotationDrivenStaticEntityMockingControl.playback();

        ResearchDataset dataset = new ResearchDataset();
        dataset.setId(datasetId);
        ResearchDataset spyDataset = spy(dataset);
        doReturn(spyDataset).when(spyDataset).merge();
        

        when(statement.executeUpdate(expectedCreateSchemaSql)).thenThrow(new SQLException("Test Rollback"));


        // execute call
        boolean exceptionRaised = false;
        try
        {
            researchDatasetDBService.createDBForDataset(spyDataset, dbInstanceDto);    
        }
        catch (ResearchDatasetDBSqlException e)
        {
            exceptionRaised = true;
        }
        
        assertTrue(exceptionRaised);
        
        
        //verify database instance is created then dropped
        verify(statement).executeUpdate(expectedCreateDBSql);
        verify(statement).executeUpdate(expectedDropDbSql);
        
        String dropFaUser = "DROP USER " + faUsername;
        String dropUaUser = "DROP USER " + uaUsername;
        String dropVaUser = "DROP USER " + vaUsername;
        verify(statement, never()).executeUpdate(dropFaUser);
        verify(statement, never()).executeUpdate(dropUaUser);
        verify(statement, never()).executeUpdate(dropVaUser);
    }

}
