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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.org.intersect.sydma.webapp.domain.AccessLevel;
import au.org.intersect.sydma.webapp.domain.RdsRequest;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.exception.NoneUniqueNameException;
import au.org.intersect.sydma.webapp.permission.path.Path;
import au.org.intersect.sydma.webapp.permission.path.PathBuilder;
import au.org.intersect.sydma.webapp.util.RifCsWriter;
import au.org.intersect.sydma.webapp.util.TokenInputHelper;

/**
 * 
 * 
 * @version $Rev: 29 $
 */
@Service
public class ResearchGroupServiceImpl implements ResearchGroupService
{
    @Autowired
    private RifCsWriter rifCsWriter;

    @Autowired
    private TokenInputHelper tokenInputHelper;

    @Autowired
    private PermissionService permissionService;
    
    @Override
    public ResearchGroup editGroup(ResearchGroup researchGroup, String keywords) throws NoneUniqueNameException
    {

        ResearchGroup group = ResearchGroup.findResearchGroup(researchGroup.getId());
        User previousPI = group.getPrincipalInvestigator();
        researchGroup.setPrincipalInvestigator(previousPI);

        if (researchGroup.isDuplicate())
        {
            throw new NoneUniqueNameException("Group name [" + researchGroup.getName() + "] is not unique");
        }

        tokenInputHelper.setKeywordsForGroup(keywords, researchGroup);
        researchGroup.updateRifCsIfNeeded(rifCsWriter, previousPI);
        researchGroup.setDirectoryPath(group.getDirectoryPath());
        researchGroup.setIsPhysical(group.getIsPhysical());
        researchGroup.merge();

        RdsRequest rdsReq = RdsRequest.findRdsRequestByResearchGroupEquals(researchGroup).getSingleResult();
        rdsReq.setName(researchGroup.getName());
        rdsReq.merge();
        
        ResearchGroup.indexResearchGroup(researchGroup);
        return researchGroup;
    }

    @Override
    public ResearchGroup create(String directoryPath, RdsRequest rdsRequest)
    {
        ResearchGroup researchGroup = new ResearchGroup();
        researchGroup.setName(rdsRequest.getName());
        researchGroup.setSubjectCode(rdsRequest.getSubjectCode());
        researchGroup.setSubjectCode2(rdsRequest.getSubjectCode2());
        researchGroup.setSubjectCode3(rdsRequest.getSubjectCode3());
        researchGroup.setPrincipalInvestigator(rdsRequest.getPrincipalInvestigator());
        researchGroup.setDataManagementContact(rdsRequest.getDataManagementContact());
        researchGroup.setDescription(rdsRequest.getDescription());
        researchGroup.setDirectoryPath(directoryPath);
        researchGroup.setIsPhysical(false);
        researchGroup.persist();
        
        ResearchGroup.indexResearchGroup(researchGroup);
        
        Path pathToGroup = PathBuilder.groupPath(researchGroup);
        permissionService.addPermission(researchGroup.getPrincipalInvestigator(), pathToGroup, AccessLevel.FULL_ACCESS);
        return researchGroup;
    }
}
