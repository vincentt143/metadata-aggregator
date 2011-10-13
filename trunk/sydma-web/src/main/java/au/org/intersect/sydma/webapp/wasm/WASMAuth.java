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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WASM Authentication
 * 
 * @version $Rev: 29 $
 */
public class WASMAuth
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WASMAuth.class);
    
    protected static WASMAuth unsuccessfulAuthentication = new WASMAuth();

    private Map<String, String> attrs = new HashMap<String, String>();
    
    private boolean successful = false;

    /**
     * @param lines
     *            A LinkedList of String objects containing the attributes returned from the session manager. These
     *            should be of the format: name:value
     **/
    protected WASMAuth(final List<String> lines, Integer msgID)
    {
        if (!parse(lines))
        {
        	LOGGER.error("WASM transport error");
        }
        else if (get("msgID") == null)
        {
            LOGGER.error("No msgID returned by wasm");
        }
        else if (get("status") == null)
        {
            LOGGER.error("No status returned by wasm");
        }
        else if (msgID != Integer.parseInt(get("msgID")))
        {
            LOGGER.error("Message IDs differ: " + msgID + " != " + get("msgID"));
        }
        else
        {
        	successful = "OK".equalsIgnoreCase(get("status"));
        }

        if (!successful)
        {
            LOGGER.error("Error Logging In: " + get("status"));
            if (get("statusDesc") != null)
            {
                LOGGER.error("Status Description: " + get("statusDesc"));
            }
        }

    }
    
    /*
     * Used for unauthenticated requests
     */
    private WASMAuth()
    {
       successful = false;
    }
    
    public boolean isSuccessful()
    {
        return successful;
    }

    private String get(final String attributeName)
    {
        return attrs.get(attributeName);
    }

    private boolean parse(final List<String> lines)
    {
        for (String line : lines)
        {
            if (line == null)
                continue;
            int sepIdx = line.indexOf(':');
            if (sepIdx == -1)
            {
                attrs.put("statusDesc", "Invalid WASM response!");
                return false;
            }
            attrs.put(line.substring(0, sepIdx).trim(), line.substring(sepIdx + 1).trim());
        }
        return true;
    }

    public String getPrincipal()
    {
        return get("loginName");
    }
    
    public String getGivenname()
    {
    	return get("givenNames");
    }
    
    public String getSurname()
    {
    	return get("surname");
    }
    
    public String getEmail()
    {
    	return get("email");
    }

    public String getSKey()
    {
        return get("sKey");
    }

}
