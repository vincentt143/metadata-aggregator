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
package au.org.intersect.sydma.webapp.wasm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * Authenticates user with remote WASM service and returns proper WASM authentication token
 * @author carlos
 *
 */
public class WASMService
{

    public static final int IKEY_MODE = 0;
    public static final int SKEY_MODE = 1;
    private static final int DEFAULT_PORT = 1317;
    private static final int DEFAULT_TIMEOUT = 10000;

    private static final Logger LOGGER = LoggerFactory.getLogger(WASMAuth.class);
    
    @Value("#{wasm[remote_host]}")
    private String remoteHost;

    @Value("#{wasm[remote_port]}")
    private int remotePort = DEFAULT_PORT;
    
    @Value("#{wasm[app_id]}")
    private String applicationID;
    
    @Value("#{wasm[app_realm]}")
    private String applicationRealm;
    
    @Value("#{wasm[app_password]}")
    private String applicationPassword;
    
    @Value("#{wasm[socket_timeout]}")
    private int socketTimeout = DEFAULT_TIMEOUT;
    
    private String sKeyCookieName;
    
    public WASMAuth getAuth(String iKey, String sKey, int mode)
    {

        if (mode != IKEY_MODE && mode != SKEY_MODE)
        {
            throw new IllegalArgumentException("Invalid mode");
        }
        else if (mode == IKEY_MODE && (iKey == null || iKey.length() == 0))
        {
            throw new IllegalArgumentException("iKey cannot be null or empty in IKEY_MODE");
        }
        else if (mode == SKEY_MODE && (sKey == null || sKey.length() == 0))
        {
            throw new IllegalArgumentException("sKey cannot be null or empty in SKEY_MODE");
        }
        return generateWasmAuthentication(iKey, sKey, mode);
    }
    
    private WASMAuth generateWasmAuthentication(String iKey, String sKey, int mode)
    {
        Socket socket = null;
        try
        {
            socket = new Socket(InetAddress.getByName(remoteHost), remotePort);
            socket.setSoTimeout(socketTimeout);
            int msgID = sendRequest(iKey, sKey, mode, socket);
            return new WASMAuth(readResponse(socket), msgID);
        }
        catch (UnknownHostException e)
        {
            LOGGER.error("Failed authentication against WASM. Unknown host", e);
            return WASMAuth.unsuccessfulAuthentication;
        }
        catch (IOException e)
        {
            LOGGER.error("Failed authentication against WASM. IO Exception", e);
            return WASMAuth.unsuccessfulAuthentication;
        }
        finally
        {
            if (socket != null)
            {
                try
                {
                    socket.close();
                }
                catch (IOException e)
                {
                    LOGGER.error("Error closing socket", e);
                }
            }
        }
    }
    
    private static List<String> readResponse(Socket socket) throws IOException
    {
        List<String> lines = new LinkedList<String>();
        BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String line = socketIn.readLine();
        while (line != null && !"".equals(line.trim()) && !socket.isClosed())
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("WASM >" + line);
            }
            lines.add(line);
            line = socketIn.readLine();
        }
        return lines;
    }

    private int sendRequest(String iKey, String sKey, int mode, Socket socket) throws IOException
    {
        BufferedWriter socketOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        int msgID = new Random().nextInt();

        writeWasmLn(socketOut, "msgID", Integer.toString(msgID));
        writeWasmLn(socketOut, "appID", applicationID);
        writeWasmLn(socketOut, "appRealm", applicationRealm);
        writeWasmLn(socketOut, "appPassword", applicationPassword);
        if (mode == IKEY_MODE)
        {
            writeWasmLn(socketOut, "action", "whoisIkey");
            writeWasmLn(socketOut, "iKey", iKey);
            if (sKey != null && sKey.length() > 0)
            {
                writeWasmLn(socketOut, "sKey", sKey);
            }
        }
        else
        {
            writeWasmLn(socketOut, "action", "whoisSkey");
            writeWasmLn(socketOut, "sKey", sKey);
        }
        socketOut.write("\n");
        socketOut.flush();
        return msgID;
    }

    public String getSKeyCookieName()
    {
        ensureCookie();
        return sKeyCookieName;
    }

    private void ensureCookie()
    {
        if (sKeyCookieName != null)
        {
            return;
        }
        sKeyCookieName = "wasm_" + applicationRealm + "_" + applicationID + "_skey";
    }
    
    private static void writeWasmLn(BufferedWriter bw, String prop, String value) throws IOException
    {
        bw.write(prop + ":" + value + "\n");
    }

}
