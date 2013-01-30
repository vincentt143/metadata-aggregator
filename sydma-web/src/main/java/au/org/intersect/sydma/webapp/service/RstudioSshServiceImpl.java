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

//TODO CHECKSTYLE-OFF: ImportOrderCheck
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.intersect.sydma.webapp.domain.User;

/**
 * Service to connect to the R Studio server remotely
 * 
 * @version $Rev: 29 $
 */
public class RstudioSshServiceImpl implements RstudioSshService
{
    private static final int COMMAND_TIMEOUT = 5;

    private static final Logger LOG = LoggerFactory.getLogger(RstudioSshServiceImpl.class);

    private String rstudioHost;

    private String rstudioKey;

    private String rstudioRemoteUser;

    private String rstudioCreateUserScript;

    public void setRstudioCreateUserScript(String rstudioCreateUserScript)
    {
        this.rstudioCreateUserScript = rstudioCreateUserScript;
    }

    public void setRstudioRemoteUser(String rstudioRemoteUser)
    {
        this.rstudioRemoteUser = rstudioRemoteUser;
    }

    public void setRstudioHost(String rstudioHost)
    {
        this.rstudioHost = rstudioHost;
    }

    public void setRstudioKey(String rstudioKey)
    {
        this.rstudioKey = rstudioKey;
    }

    @Override
    public void addRstudioUser(final User user, final String unixPassword) throws CannotCreateRstudioUserException
    {
        try
        {
            RStudioSshExcecutor sshExecutor = new RStudioSshExcecutor(createUserSshCommand(user, unixPassword));
            sshExecutor.executeCommand();
        }
        catch (IOException e)
        {
            LOG.info("Cannot create Rstudio user.", e);
            throw new CannotCreateRstudioUserException("Unsuccessful command execution");
        }
    }

    @Override
    public void changeRstudioUserPassword(User user, String unixPassword) throws CannotChangeRstudioPasswordException
    {
        try
        {
            RStudioSshExcecutor sshExecutor = new RStudioSshExcecutor(changePassswordSshCommand(user, unixPassword));
            sshExecutor.executeCommand();
        }
        catch (IOException e)
        {
            LOG.info("Cannot change Rstudio user password.", e);
            throw new CannotChangeRstudioPasswordException("Unsuccessful command execution");
        }
    }

    private SshCommand createUserSshCommand(final User user, final String unixPassword)
    {
        SshCommand command = new SshCommand()
        {
            @Override
            public int execute(Session session) throws IOException
            {
                String rstudioUsername = user.getRstudioUsername();
                LOG.info("Creating R Studio user: " + rstudioUsername);
                String command = rstudioCreateUserScript + " -u " + rstudioUsername + " -p " + unixPassword;
                final Command cmd = session.exec(command);
                LOG.info(IOUtils.readFully(cmd.getInputStream()).toString());
                cmd.join(COMMAND_TIMEOUT, TimeUnit.SECONDS);
                return cmd.getExitStatus();
            }
        };
        return command;
    }

    private SshCommand changePassswordSshCommand(final User user, final String unixPassword)
    {
        SshCommand command = new SshCommand()
        {
            @Override
            public int execute(Session session) throws IOException
            {
                String rstudioUsername = user.getRstudioUsername();
                LOG.info("Changing R Studio password for user " + rstudioUsername);
                String command = "echo " + unixPassword + " | sudo passwd --stdin " + rstudioUsername;
                final Command cmd = session.exec(command);
                cmd.join(COMMAND_TIMEOUT, TimeUnit.SECONDS);
                return cmd.getExitStatus();
            }
        };
        return command;
    }

    /**
     * Encapsulates a ssh command to run on the server
     *
     * @version $Rev: 29 $
     */
    private interface SshCommand
    {
        int execute(final Session session) throws IOException;
    }

    /**
     * Class that runs an ssh command by delegating to SshCommand
     *
     * @version $Rev: 29 $
     */
    private final class RStudioSshExcecutor
    {
        private SshCommand command;

        private RStudioSshExcecutor(final SshCommand command)
        {
            this.command = command;
        }

        private void executeCommand() throws IOException
        {
            SSHClient ssh = null;
            try
            {
                ssh = establishSshConnection();
                establishSshSessionAndRunCommand(command, ssh);
            }
            finally
            {
                if (ssh != null && ssh.isConnected())
                {
                    try
                    {
                        ssh.disconnect();
                    }
                    catch (IOException e)
                    {
                        LOG.error("Failed to disconnect ssh session");
                    }
                }
            }

        }

        private void establishSshSessionAndRunCommand(final SshCommand command, final SSHClient ssh) throws IOException
        {
            final String base = System.getProperty("user.home") + File.separator + ".ssh" + File.separator;

            LOG.info("Logging as >" + rstudioRemoteUser + "< >" + base + rstudioKey + "<");
            ssh.authPublickey(rstudioRemoteUser, base + rstudioKey);
            final Session session = ssh.startSession();
            try
            {
                int statusCode = command.execute(session);
                if (statusCode != 0)
                {
                    throw new IOException("Command execution was not successful. Script exit status code:" 
                            + statusCode);
                }
            }
            finally
            {
                session.close();
            }
        }

        private SSHClient establishSshConnection() throws IOException
        {
            final SSHClient ssh = new SSHClient();
            ssh.loadKnownHosts();
            ssh.connect(rstudioHost);
            return ssh;
        }
    }

}
