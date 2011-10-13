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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.staticmock.AnnotationDrivenStaticEntityMockingControl;
import org.springframework.mock.staticmock.MockStaticEntityMethods;

import au.org.intersect.sydma.webapp.domain.DBSchema;
import au.org.intersect.sydma.webapp.domain.DBUser;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchDatasetDB;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.util.DBConnectionHelper;

@MockStaticEntityMethods
public class ResearchDatasetDBServiceImplTest
{
    
    private ResearchDatasetDBServiceImpl researchDatasetDBService;

    private DBSchemaService schemaService;
    
    private DBConnectionHelper dbConnectionHelper;
    
    private Connection connection;
    
    private EntityManager entityManager;
    
    private String schemaSql = "SOME SQL";        
    private Long datasetId = new Long(1);
    private String schemaName = "schema";
    private String schemaFile = "schema.sql";
    private String dbName = "dataset_" + datasetId;
    private Long userId = new Long(1);
    private String dbUsername = "user_" + userId;
    private String password = "password";

    private DBSchema dbSchema; 
    private DBSchema spyDbSchema; 
    
    @Before
    public void setUp()
    {        
        schemaService = Mockito.mock(DBSchemaService.class);
        dbConnectionHelper = Mockito.mock(DBConnectionHelper.class);        
        connection = Mockito.mock(Connection.class);        
        entityManager = Mockito.mock(EntityManager.class);
        
        researchDatasetDBService = new ResearchDatasetDBServiceImpl();
        researchDatasetDBService.setDbConnectionHelper(dbConnectionHelper);
        researchDatasetDBService.setDBSchemaService(schemaService);      
        
        dbSchema = new DBSchema(schemaName, schemaFile);
        spyDbSchema = spy(dbSchema);
    }
    
    public void mockGetConnection() throws SQLException
    {
        when(dbConnectionHelper.obtainConnection()).thenReturn(connection);
    }
    
    @Test
    public void testCreateDBForDatasetAndGrantFreshUser() throws IOException, SQLException
    {
      
        
        
        User user = new User();        
        user.setUsername("user");        
        user.setId(userId);
        
        ResearchDataset dataset = new ResearchDataset();
        dataset.setId(datasetId);
        ResearchDataset spyDataset = spy(dataset);
        doReturn(spyDataset).when(spyDataset).merge();
        
        DBSchema spyDbSchema = spy(dbSchema);
        doReturn(schemaSql).when(spyDbSchema).loadSchemaSql();        
        
        //Give Roo an entitymanager when it asks for one through the static method
        User.entityManager();
        AnnotationDrivenStaticEntityMockingControl.expectReturn(entityManager);
        AnnotationDrivenStaticEntityMockingControl.playback();
                
        mockGetConnection();
                        
        researchDatasetDBService.createDBForDatasetAndGrantUser(spyDataset, spyDbSchema, user);
        
        
        verify(schemaService).createDatabase(eq(dbName), eq(connection));
        verify(schemaService).createSchemaInDB(eq(dbName), eq(schemaSql), eq(connection));
        verify(schemaService).createUser(eq(dbUsername), eq(password), eq(connection));
        verify(schemaService).grantUser(eq(dbUsername), eq(dbName), eq(connection));
        
        //verify the entityManager is called to merge the user
        verify(entityManager).merge(user);
        
        DBUser createdDBUser = user.getDbUser();
        assertNotNull("User does not have a DBUser entity created for it", createdDBUser);
        assertEquals("Persisted DBUsername is incorrect", dbUsername, createdDBUser.getDbUsername());
        assertEquals("Persisted user password is incorrect", password, createdDBUser.getDbPassword());
        
        
        //verify the dataset is updated
        verify(spyDataset).merge();       
        
        ResearchDatasetDB dbInstance = spyDataset.getDatabaseInstance();
        assertNotNull("Dataset does not have a DBInstance entity created for it", dbInstance);
        assertEquals("Persisted DBName is incorrect", dbName, dbInstance.getDbName());
        assertEquals("Persisted DBSchemaName is incorrect", spyDbSchema, dbInstance.getDbSchema());
        
    }
    

    @Test
    public void testCreateDBForDatasetAndGrantExistingUser() throws IOException, SQLException
    {
      
        User user = new User();        
        user.setUsername("user");
        user.setId(userId);
        
        DBUser dbUser = new DBUser("user_" + userId, "password");
        user.setDbUser(dbUser);
        
        ResearchDataset dataset = new ResearchDataset();
        dataset.setId(datasetId);
        ResearchDataset spyDataset = spy(dataset);
        doReturn(spyDataset).when(spyDataset).merge();
        

        doReturn(schemaSql).when(spyDbSchema).loadSchemaSql();        
    
        mockGetConnection();
                        
        researchDatasetDBService.createDBForDatasetAndGrantUser(spyDataset, spyDbSchema, user);
        
        verify(schemaService).createDatabase(eq(dbName), eq(connection));
        verify(schemaService).createSchemaInDB(eq(dbName), eq(schemaSql), eq(connection));
        verify(schemaService, never()).createUser(any(String.class), any(String.class), any(Connection.class));
        verify(schemaService).grantUser(eq(dbUsername), eq(dbName), eq(connection));
        
        //verify the user is not updated
        verify(entityManager, never()).merge(user);

        //verify the dataset is updated
        verify(spyDataset).merge();       
        
        ResearchDatasetDB dbInstance = spyDataset.getDatabaseInstance();
        assertNotNull("Dataset does not have a DBInstance entity created for it", dbInstance);
        assertEquals("Persisted DBName is incorrect", dbName, dbInstance.getDbName());
        assertEquals("Persisted DBSchemaName is incorrect", spyDbSchema, dbInstance.getDbSchema());
        

    }
    
    
    
}
