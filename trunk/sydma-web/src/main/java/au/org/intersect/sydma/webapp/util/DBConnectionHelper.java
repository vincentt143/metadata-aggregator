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

package au.org.intersect.sydma.webapp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.datasource.DataSourceUtils;

import au.org.intersect.sydma.webapp.domain.DBUser;
import au.org.intersect.sydma.webapp.domain.ResearchDatasetDB;

/**
 * A wrapper object for handling how/where a connection is obtained for DB Instance creation
 * 
 * @version $Rev: 29 $
 */
public class DBConnectionHelper
{
    private static final Logger LOG = LoggerFactory.getLogger(DBConnectionHelper.class);

    private String driverClass;

    @Autowired
    @Qualifier("dbInstanceDataSource")
    private DataSource dataSource;

    @Required
    public void setDriverClass(String driverClass)
    {
        this.driverClass = driverClass;
    }

    public Connection obtainConnection()
    {
        return DataSourceUtils.getConnection(dataSource);
    }

    /**
     * Obtain a connection for query
     * 
     * @param dbUser
     *            Must pass a user with only viewing access
     * @return
     */
    public Connection obtainConnectionFor(ResearchDatasetDB datasetDB, DBUser dbUser)
    {
        try
        {
            Class.forName(driverClass);
        }
        catch (ClassNotFoundException e)
        {
            LOG.error(e.toString());
        }
        String databaseUrl = "jdbc:mysql://" + datasetDB.getDbHostname() + ":3306/" + datasetDB.getDbName();
        Connection connection = null;
        try
        {
            connection = DriverManager.getConnection(databaseUrl, dbUser.getDbUsername(), dbUser.getDbPassword());
        }
        catch (SQLException e)
        {
            LOG.error("SQL connection error for url:" + databaseUrl, e);
        }
        return connection;
    }
}
