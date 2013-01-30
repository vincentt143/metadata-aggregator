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

import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import au.org.intersect.dms.encrypt.EncryptionAgent;
import au.org.intersect.dms.encrypt.EncryptionAgentException;
import au.org.intersect.dms.tunnel.HddUtil;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.exception.PermissionDeniedException;
import au.org.intersect.sydma.webapp.json.JsonResponse;
import au.org.intersect.sydma.webapp.security.SecurityContextFacade;
import au.org.intersect.sydma.webapp.service.DataTranferService;
import au.org.intersect.sydma.webapp.service.JobDetail;

/**
 * Controller for managing upload process
 */
@Controller
@RequestMapping("/transferJob/**")
public class TransferJobController extends AbstractControllerWithDmsConnection
{
    private static final Logger LOG = LoggerFactory.getLogger(TransferJobController.class);

    @Autowired
    private SecurityContextFacade securityContextFacade;

    @Autowired
    private DataTranferService dataTranferService;

    @Autowired
    @Qualifier("publicAgent")
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
            @RequestParam(value = "encode", required = false) Boolean encode, Principal principal) throws Exception
    {
        try
        {
            User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();

            JobDetail jobDetail = dataTranferService.createJob(sourceConnectionId, sourceItem, destinationConnectionId,
                    destinationItem, user);
            if (encode != null && encode)
            {
                return encodeResponse(encode, jobDetail);
            }

            JsonResponse jobResponse = new JsonResponse(jobDetail, null);
            return jobResponse.toJson();
        }
        catch (PermissionDeniedException exception)
        {
            JsonResponse jobResponse = new JsonResponse(null, "Permission Denied");
            return jobResponse.toJson();
        }
        catch (Exception e)
        {
        	if (e instanceof InterruptedException)
        	{
        		Thread.currentThread().interrupt();
        		return null;
        	}
        	else
        	{
        		LOG.error("Error creating job", e);
                JsonResponse jobResponse = new JsonResponse(null, "Error ocurred: " + e.getMessage());
                return jobResponse.toJson();
        	}
        }
    }

    /**
    private JobDetail createJob(Integer sourceConnectionId, List<String> sourceItem, Integer destinationConnectionId,
            String destinationItem, Principal principal)
    {

        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        Integer connectionLocal = connectLocal(principal.getName());

        final String transferDestination;
        final List<String> transferSource;
        final List<String> transferSourceDisplay = new ArrayList<String>();
        final String transferDestinationDisplay;
        final Integer sourceConnection;
        final Integer destinationConnection;
        if (sourceConnectionId != HDD_CONNECTION)
        {
            transferSource = new ArrayList<String>();
            for (String source : sourceItem)
            {
                Path sourcePath = securePath(source, user, PermissionType.DOWNLOAD);
                transferSource.add(determineVirtualItem(sourcePath));

                String namedPath = getNamedPath(sourcePath);
                transferSourceDisplay.add(namedPath);
            }
            sourceConnection = connectionLocal;
        }
        else
        {
            transferSource = sourceItem;
            sourceConnection = sourceConnectionId;

            transferSourceDisplay.addAll(sourceItem);
        }

        if (destinationConnectionId != HDD_CONNECTION)
        {
            Path destinationPath = securePath(destinationItem, user, PermissionType.UPLOAD);
            transferDestination = determineVirtualItem(destinationPath);
            destinationConnection = connectionLocal;
            transferDestinationDisplay = getNamedPath(destinationPath);
        }
        else
        {
            transferDestination = destinationItem;
            destinationConnection = destinationConnectionId;

            transferDestinationDisplay = destinationItem;
        }

        Long jobId = getDmsService().copy(user.getUsername(), sourceConnection, transferSource, destinationConnection,
                transferDestination);
        JobDetail jobDetail = new JobDetail(jobId, transferSourceDisplay, transferDestinationDisplay);
        return jobDetail;
    }
    **/
    
    private String encodeResponse(Boolean encode, JobDetail jobDetail)
    {
        try
        {
            byte[] encjobId = agent.process((jobDetail.getJobId().toString()).getBytes());
            String jobIdAsHexString = HddUtil.convertByteToHexString(encjobId);

            jobDetail.setEncJobId(jobIdAsHexString);

            JsonResponse jobResponse = new JsonResponse(jobDetail, null);
            return jobResponse.toJson();
        }
        catch (EncryptionAgentException e)
        {
            throw new RuntimeException("The web-app could not encrypt your Job ID correctly; "
                    + "Therefore this job will not be executed.");
        }

    }

}
