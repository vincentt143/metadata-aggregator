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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import au.org.intersect.sydma.webapp.domain.DBBackup;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchDatasetDB;
import au.org.intersect.sydma.webapp.domain.User;

/**
 * Helper to handle execution of sql dumps
 * 
 * @version $Rev: 1 $
 */
// TODO CHECKSTYLE-OFF: MultipleStringLiteralsCheck
public class DBBackupHelperImpl implements DBBackupHelper
{
    private static final Logger LOG = LoggerFactory.getLogger(DBBackupHelper.class);
    private static final String PATH_SEPERATOR = "/";
    private static final String ENCODE_FORMAT = "UTF-8";

    private static final String FILE_DATE_FORMAT = "yyyyMMdd-HHmmss";
    private static final String DISPLAY_DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";

    private static final String SQL_EXTENSION = ".sql";

    private String username;

    private String password;

    private String uploadRoot;

    private String mysqlDumpPath;

    @Required
    public void setUsername(String username)
    {
        this.username = username;
    }

    @Required
    public void setPassword(String password)
    {
        this.password = password;
    }

    @Required
    public void setUploadRoot(String uploadRoot)
    {
        this.uploadRoot = uploadRoot;
    }

    @Required
    public void setMysqlDumpPath(String mysqlDumpPath)
    {
        this.mysqlDumpPath = mysqlDumpPath;
        if (mysqlDumpPath.endsWith("/"))
        {
            this.mysqlDumpPath = this.mysqlDumpPath.substring(0, this.mysqlDumpPath.length()-1);
        }
    }

    public void createBackup(ResearchDataset dataset, String description, Principal principal)
    {
        DateFormat dateFormat = new SimpleDateFormat(FILE_DATE_FORMAT);
        Date date = new Date();
        DBBackup databaseBackup = new DBBackup();
        ResearchDatasetDB datasetDB = ResearchDatasetDB.findResearchDatasetDBsByResearchDataset(dataset)
                .getSingleResult();

        String datasetName = dataset.getName();
        datasetName = datasetName.replaceAll(" ", "_");
        String filename = datasetName + "_" + dateFormat.format(date) + SQL_EXTENSION;

        if (!dumpDatabase(datasetDB, filename, description))
        {
            LOG.info("Failed to backup database.");
            // TODO: throw exception
        }
        databaseBackup.setDescription(description);
        databaseBackup.create(principal, dataset, filename, date);
    }

    public void restoreFromBackup(ResearchDataset dataset, Long backupId, String description, Principal principal)
    {
        DateFormat dateFormat = new SimpleDateFormat(DISPLAY_DATE_FORMAT);
        Date date = new Date();
        DBBackup dBBackup = DBBackup.findDBBackup(backupId);
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        ResearchDatasetDB datasetDB = ResearchDatasetDB.findResearchDatasetDBsByResearchDataset(dataset)
                .getSingleResult();
        String filename = dBBackup.getFile();

        if (!restoreDatabase(datasetDB, filename))
        {
            LOG.info("Failed to restore database.");
            // TODO: throw exception
        }
        
        datasetDB.setLastRestoredBy(user);
        datasetDB.setState(description);
        datasetDB.setDBBackupUsed(dBBackup);
        datasetDB.setDateOfRestoration(dateFormat.format(date));
        datasetDB.merge();
    }

    public boolean dumpDatabase(ResearchDatasetDB datasetDB, String filename, String description)
    {
        if (!checkIfDirectoryExists(uploadRoot))
        {
            LOG.info("Failed to create backup directory");
        }
        try
        {
            executeUnixCommand(buildCommand(datasetDB, filename, description));
            return true;
        }
        catch (IOException e)
        {
            LOG.error("SQL dump error: " + e);
            return false;
        }
    }

    public boolean restoreDatabase(ResearchDatasetDB datasetDB, String filename)
    {
        try
        {
            executeUnixCommand(restoreCommand(datasetDB, filename));
            return true;
        }
        catch (IOException e)
        {
            LOG.error("SQL restore error: " + e);
            return false;
        }
    }

    private Process executeUnixCommand(String command) throws IOException
    {
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/sh", "-c", command);
        processBuilder.redirectErrorStream(true);
        LOG.info("Starting child process /bin/sh -c " + command.replaceFirst("--password='[^']*'","--password='******'"));
        return processBuilder.start();
    }

    private String restoreCommand(ResearchDatasetDB datasetDB, String filename)
    {
        String fileDirectory = uploadRoot + PATH_SEPERATOR + filename;
        String command = commandBasic("mysql", datasetDB) +" " + datasetDB.getDbName() + " < " + fileDirectory;
        return command;
    }

    private String buildCommand(ResearchDatasetDB datasetDB, String filename, String description)
    {
        String databaseName = datasetDB.getDbName();
        String uploadDirectory = uploadRoot + PATH_SEPERATOR + filename;

        String command = commandBasic(mysqlDumpPath + "/mysqldump", datasetDB) + " " + databaseName
                + " | sed '6i --\\ Backing up before: " + encodeDescription(description)
                + "\\nDROP DATABASE IF EXISTS " + databaseName + "; " + "\\nCREATE DATABASE IF NOT EXISTS "
                + databaseName + "; " + "\\nUSE " + databaseName + ";' > " + uploadDirectory;
        return command;
    }

    private String buildDdlCommand(ResearchDatasetDB datasetDB)
    {
        String command = commandBasic(mysqlDumpPath + "/mysqldump", datasetDB) + " --skip-comments --no-data --no-create-db "
                + datasetDB.getDbName();
        return command;
    }

    private String commandBasic(String tool, ResearchDatasetDB datasetDB)
    {
        return String.format("%1$s --host='%2$s' --user='%3$s' --password='%4$s' ",tool, datasetDB.getDbHostname(),
                username, password);
    }

    private String encodeDescription(String description)
    {
        String encodedDescription = null;
        try
        {
            encodedDescription = URLEncoder.encode(description, ENCODE_FORMAT);
        }
        catch (UnsupportedEncodingException e)
        {
            LOG.error("Encoding error of SQL description: " + e);
        }
        return encodedDescription;
    }
    
    private boolean checkIfDirectoryExists(String directoryPath)
    {              
        File directory = new File(directoryPath);
        if (!directory.isDirectory())
        {
            return directory.mkdir();
        }
        return true;
    }

    @Override
    public List<String> dabataseDdl(ResearchDatasetDB datasetDB) throws IOException
    {
        Process process = executeUnixCommand(buildDdlCommand(datasetDB));
        return IOUtils.readLines(process.getInputStream());
    }
}
