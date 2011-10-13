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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import au.org.intersect.dms.encrypt.EncryptionAgent;
import au.org.intersect.dms.encrypt.EncryptionAgentException;
import au.org.intersect.dms.tunnel.HddUtil;
import au.org.intersect.sydma.webapp.json.JsonResponse;
import au.org.intersect.sydma.webapp.permission.path.Path;
import au.org.intersect.sydma.webapp.permission.path.PathBuilder;
import au.org.intersect.sydma.webapp.security.SecurityContextFacade;
import au.org.intersect.sydma.webapp.service.FilePathService;

/**
 * Controller for managing upload process
 */
@Controller
@RequestMapping("/transferJob/**")
public class TransferJobController extends AbstractControllerWithDmsConnection
{
    private static final Logger LOG = LoggerFactory.getLogger(TransferJobController.class);

    private static final int HDD_CONNECTION = -1;

    @Autowired
    private FilePathService filePathService;

    @Autowired
    private SecurityContextFacade securityContextFacade;

    @Autowired
    private EncryptionAgent agent;
    
    @RequestMapping(method = RequestMethod.POST, value = "/jobCancel")
    @ResponseBody
    public String jobCancel(@RequestParam(value = "jobId") long jobId)
    {
        String username = securityContextFacade.getAuthorizedUsername();
        Boolean resp = getDmsService().stopJob(username, jobId);        
        JsonResponse jobResponse = new JsonResponse(resp, null);
        return jobResponse.toJson();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/copyJob")
    @ResponseBody
    public String copy(@RequestParam(value = "source_connectionId") Integer sourceConnectionId,
            @RequestParam(value = "source_item") List<String> sourceItem,
            @RequestParam(value = "destination_connectionId") Integer destinationConnectionId,
            @RequestParam(value = "destination_item") String destinationItem,
            @RequestParam(value = "encode", required = false) Boolean encode)
    {
        Long jobId = createJob(sourceConnectionId, sourceItem, destinationConnectionId, destinationItem);
        if (encode != null && encode)
        {
            return encodeResponse(encode, jobId);
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("jobId", jobId.toString());
        JsonResponse jobResponse = new JsonResponse(map, null);
        return jobResponse.toJson();

    }

    private Long createJob(Integer sourceConnectionId, List<String> sourceItem, Integer destinationConnectionId,
            String destinationItem)
    {
        String username = securityContextFacade.getAuthorizedUsername();

        Integer connectionLocal = connectLocal();

        String transferDestination;
        List<String> transferSource;
        Integer sourceConnection;
        Integer destinationConnection;
        if (sourceConnectionId != HDD_CONNECTION)
        {
            transferSource = new ArrayList<String>();
            for (String source : sourceItem)
            {
                transferSource.add(determineVirtualItem(source));
            }
            sourceConnection = connectionLocal;
        }
        else
        {
            transferSource = sourceItem;
            sourceConnection = sourceConnectionId;
        }

        if (destinationConnectionId != HDD_CONNECTION)
        {
            transferDestination = determineVirtualItem(destinationItem);
            destinationConnection = connectionLocal;
        }
        else
        {
            transferDestination = destinationItem;
            destinationConnection = destinationConnectionId;
        }

        Long jobId = getDmsService().copy(username, sourceConnection, transferSource, destinationConnection,
                transferDestination);
        return jobId;
    }

    private String encodeResponse(Boolean encode, Long jobId)
    {
        try
        {
            byte[] encjobId = agent.process((jobId.toString()).getBytes());
            String jobIdAsHexString = HddUtil.convertByteToHexString(encjobId);

            Map<String, String> map = new HashMap<String, String>();
            map.put("jobId", jobId.toString());
            map.put("encJobId", jobIdAsHexString);

            JsonResponse jobResponse = new JsonResponse(map, null);
            return jobResponse.toJson();
        }
        catch (EncryptionAgentException e)
        {
            throw new RuntimeException("The web-app could not encrypt your Job ID correctly; "
                    + "Therefore this job will not be executed.");
        }

    }

    private String determineVirtualItem(String virtualPath)
    {
        Path path = PathBuilder.buildFromString(virtualPath);
        String localItem = filePathService.resolveRelativePath(path);
        return localItem;
    }

}
