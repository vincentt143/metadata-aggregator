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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import au.org.intersect.sydma.webapp.exception.ResearchDatasetDBSqlException;


/**
 * Manages db schema by talking directly with the database through jdbc
 *
 * @version $Rev: 29 $
 */
@Service
@Transactional("sydmaPU")
public class DBSchemaServiceImpl implements DBSchemaService
{
    private static final Logger LOG = LoggerFactory.getLogger(DBSchemaServiceImpl.class);

    
    @Override
    public boolean createDatabase(String dbName, Connection connection)
    {        
        String sql = "CREATE DATABASE " + dbName;
  
        int rows = executeSql(sql, connection);
        
        if (rows > 0)
        {
            return true;
        }
        return false;
    }

    @Override
    public boolean createSchemaInDB(String dbName, String schemaSql, Connection connection)
    {
        try
        {
            executeSql(schemaSql, dbName, connection);
            
            LOG.info("Create schema complete");
            
            return true;
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            LOG.info("Error creating schema ", e);
        }        
       
        return false;
    }
    
    @Override
    public boolean createUser(String dbUsername, String password, Connection connection)
    {
        String sql = "CREATE USER ?@'%' IDENTIFIED BY ?";
        LOG.info("CREATE USER ");
        try
        {
            PreparedStatement statement = createPreparedStatementForSql(sql, connection);
            
            statement.setString(1, dbUsername);
            statement.setString(2, password);
            return statement.execute();
        }
        catch (SQLException e)
        {
            throw new ResearchDatasetDBSqlException("Failed to create user with sql " + sql, e);
        }
    }

    @Override
    public boolean grantUser(String dbUsername, String dbName, Connection connection)
    {
        String sql = "GRANT ALL ON " + dbName + ".* TO ?@'%'";
        LOG.info("GRANT USER " + sql);
        try
        {
            PreparedStatement statement = createPreparedStatementForSql(sql, connection);
            
            statement.setString(1, dbUsername);
            boolean result = statement.execute();
            
            String grants = showUserGrants(dbUsername, connection);
            LOG.info("USER PERMS::" + grants);

            return result;
        }
        catch (SQLException e)
        {
            throw new ResearchDatasetDBSqlException("Failed to grant user permssions with sql " + sql, e);
        }
    }
    
    

    @Override
    public boolean createAndGrantUser(String dbName, String dbUsername, String password, Connection connection)
    {
        String sql = "GRANT ALL ON " + dbName + ".* TO ?@'%' IDENTIFIED BY ?";
        LOG.info("CREATE AND GRANT USER ");
        try
        {
            PreparedStatement statement = createPreparedStatementForSql(sql, connection);            
            statement.setString(1, dbUsername);
            statement.setString(2, password);
            return statement.execute();
        }
        catch (SQLException e)
        {
            throw new ResearchDatasetDBSqlException("Failed to create and grant user with sql " + sql, e);
        }
    }
    
    
    private PreparedStatement createPreparedStatementForSql(String sql, Connection connection) throws SQLException
    {
        return connection.prepareStatement(sql);
    }
    
    private int executeSql(String sql, String dbName, Connection connection) throws SQLException
    {
        LOG.info("Executing sql on db " + dbName);
                
        connection.setCatalog(dbName);
        showCurrentDB(connection);
        return executeSql(sql, connection);        
    }
    
    
    private int executeSql(String sql, Connection connection)
    {
        LOG.info("Excecute SQL::" + sql);
        try
        {
            Statement statement = connection.createStatement();
            int rows = statement.executeUpdate(sql);
            return rows;
        }
        catch (SQLException e)
        {
            throw new ResearchDatasetDBSqlException("Failed to execute sql " + sql, e);
        }    
    }

    /*
     * debug use
     */
    private String showCurrentDB(Connection connection) throws SQLException
    {
        String catalog = connection.getCatalog();
        LOG.info("Current catalog " + catalog);
        return catalog;        
    }
    private String showUserGrants(String dbUsername, Connection connection) throws SQLException
    {
        String debug = "";
        PreparedStatement statement = connection.prepareStatement("SHOW GRANTS FOR " + dbUsername + "@'%'");
        ResultSet userPerms = statement.executeQuery();
        while (userPerms.next())
        {
            debug += "\n " + userPerms.getString(1);
        }
        return debug;
    }

}
