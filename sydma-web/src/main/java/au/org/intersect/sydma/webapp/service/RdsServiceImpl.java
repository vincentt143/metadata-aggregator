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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import au.org.intersect.sydma.webapp.domain.RdsRequest;
import au.org.intersect.sydma.webapp.domain.RdsRequestStatus;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;

/**
 * 
 * 
 * @version $Rev: 29 $
 */
@Service
@Transactional("sydmaPU")
public class RdsServiceImpl implements RdsService
{
    private static final Logger LOG = LoggerFactory.getLogger(RdsServiceImpl.class);

    @Autowired
    private NewMailService mailService;

    @Override
    public void approveRdsRequest(ResearchGroup researchGroup, RdsRequest rdsRequest)
    {
        rdsRequest.setRequestStatus(RdsRequestStatus.APPROVED);
        rdsRequest.setResearchGroup(researchGroup);
        rdsRequest.merge();
    }

    @Override
    public void rejectRdsRequest(RdsRequest rdsRequest)
    {
        rdsRequest.setRequestStatus(RdsRequestStatus.REJECTED);
        rdsRequest.merge();
    }

    @Override
    public void createRdsRequest(RdsRequest rdsRequest)
    {
        rdsRequest.setRequestStatus(RdsRequestStatus.CREATED);
        rdsRequest.persist();
        try
        {
            mailService.sendNewRdsRequestEmail(rdsRequest);
        }
        catch (Exception e)
        {
            LOG.error("Cannot send email",e);
        }
    }

}
