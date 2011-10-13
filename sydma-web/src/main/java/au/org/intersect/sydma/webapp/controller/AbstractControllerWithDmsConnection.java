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

package au.org.intersect.sydma.webapp.controller;

import javax.servlet.ServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;

import au.org.intersect.dms.core.errors.ConnectionClosedError;
import au.org.intersect.dms.core.service.DmsService;


/**
 * Handles ConnectionClosed Exception
 */
@Controller
public abstract class AbstractControllerWithDmsConnection
{
    private static final Logger LOG = LoggerFactory.getLogger(AbstractControllerWithDmsConnection.class);
    
    private static final String AJAX_FLAG = "ajax";

    @Autowired
    private DmsService dmsService;        
    
    @Value("#{sydmaFileProperties['sydma.localFileServer']}")
    private String localServer;
    
    protected Integer connectLocal()
    {
        String protocol = "local";
        String server = localServer;
        String username = null;
        String password = null;
        Integer connectionId = dmsService.openConnection(protocol, server, username, password);
        return connectionId;
    }
    
    protected DmsService getDmsService()
    {
        return dmsService;
    }
    
    @ExceptionHandler(ConnectionClosedError.class)
    public String connectionClosed(ConnectionClosedError e, ServletRequest request)
    {        
        if (request.getParameter(AJAX_FLAG) != null)
        {
            return "exceptions/connectionClosedExceptionAjax";    
        }
        return "exceptions/connectionClosedException";
    }
}
