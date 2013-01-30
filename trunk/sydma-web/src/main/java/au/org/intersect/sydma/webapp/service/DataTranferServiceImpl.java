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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.org.intersect.dms.core.service.DmsService;
import au.org.intersect.dms.core.service.dto.OpenConnectionParameter;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.exception.PermissionDeniedException;
import au.org.intersect.sydma.webapp.permission.PermissionType;
import au.org.intersect.sydma.webapp.permission.path.Path;
import au.org.intersect.sydma.webapp.permission.path.PathBuilder;
import au.org.intersect.sydma.webapp.service.transfer.ProtocolPath;
import au.org.intersect.sydma.webapp.service.transfer.ProtocolPathResolver;

/**
 * 
 * 
 * @version $Rev: 29 $
 */
@Service
public class DataTranferServiceImpl implements DataTranferService
{
    private static final String NO_ACCESS_TO_PATH = "No access to path ";
    @Autowired
    private DmsService dmsService;

    @Autowired
    private ProtocolPathResolver protocolPathResolver;

    @Autowired
    private PermissionService permissionService;

    protected DmsService getDmsService()
    {
        return dmsService;
    }

    @Override
    public JobDetail createJob(Integer sourceConnectionId, List<String> sourceItems, Integer destinationConnectionId,
            String destinationPath, User user) throws PermissionDeniedException
    {
        final String transferDestination;
        final List<String> transferSource = new ArrayList<String>();
        final List<String> transferSourceDisplay = new ArrayList<String>();
        final String transferDestinationDisplay;

        final OpenConnectionParameter sourceParams = getDmsService().getConnectionParameters(sourceConnectionId);
        final OpenConnectionParameter destinationParams = getDmsService().getConnectionParameters(
                destinationConnectionId);

        checkSourcePermission(sourceParams, sourceItems, user);
        checkDestinationPermission(destinationParams, destinationPath, user);

        ProtocolPath srcProtocolPath = protocolPathResolver.getProtocolPathFor(sourceParams.getProtocol());

        for (String source : sourceItems)
        {
            transferSource.add(srcProtocolPath.getTransportPath(source, user));
            transferSourceDisplay.add(srcProtocolPath.getDisplayPath(source, user));

        }

        ProtocolPath destProtocolPath = protocolPathResolver.getProtocolPathFor(destinationParams.getProtocol());
        transferDestination = destProtocolPath.getTransportPath(destinationPath, user);
        transferDestinationDisplay = destProtocolPath.getDisplayPath(destinationPath, user);

        Long jobId = getDmsService().copy(user.getUsername(), sourceConnectionId, transferSource,
                destinationConnectionId, transferDestination);
        JobDetail jobDetail = new JobDetail(jobId, transferSourceDisplay, transferDestinationDisplay);
        return jobDetail;
    }

    private void checkSourcePermission(OpenConnectionParameter sourceParams, List<String> sourceItems, User user)
        throws PermissionDeniedException
    {
        if ("local".equals(sourceParams.getProtocol()))
        {
            for (String source : sourceItems)
            {
                Path sourcePath = PathBuilder.buildFromString(source);
                if (sourcePath.isFilePath())
                {
                    if (!permissionService.canDirectoryAction(PermissionType.VIEW_DIRECTORY, sourcePath, user))
                    {
                        throw new PermissionDeniedException(NO_ACCESS_TO_PATH + source);
                    }
                }
                else
                {
                    if (!permissionService.canDataset(PermissionType.VIEW_DATASET, sourcePath.getDatasetId(), user))
                    {
                        throw new PermissionDeniedException(NO_ACCESS_TO_PATH + source);
                    }
                }
            }
        }
    }

    private void checkDestinationPermission(OpenConnectionParameter destinationParams, String destinationItem, 
            User user) throws PermissionDeniedException
    {
        if ("local".equals(destinationParams.getProtocol()))
        {
            Path destinationPath = PathBuilder.buildFromString(destinationItem);
            if (destinationPath.isFilePath())
            {
                if (!permissionService.canDirectoryAction(PermissionType.UPLOAD, destinationPath, user))
                {
                    throw new PermissionDeniedException(NO_ACCESS_TO_PATH + destinationItem);
                }
            }
            else
            {
                if (!permissionService.canDirectoryAction(PermissionType.EDIT_DATASET, destinationPath, user))
                {
                    throw new PermissionDeniedException(NO_ACCESS_TO_PATH + destinationItem);
                }
            }
        }
    }
}
