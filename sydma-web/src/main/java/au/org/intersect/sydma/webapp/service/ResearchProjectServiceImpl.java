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

import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;
import au.org.intersect.sydma.webapp.exception.NoneUniqueNameException;
import au.org.intersect.sydma.webapp.util.RifCsWriter;
import au.org.intersect.sydma.webapp.util.TokenInputHelper;

/**
 * 
 * 
 * @version $Rev: 29 $
 */
@Service
public class ResearchProjectServiceImpl implements ResearchProjectService
{
    @Autowired
    private RifCsWriter rifCsWriter;

    @Autowired
    private TokenInputHelper tokenInputHelper;

    @Override
    public ResearchProject editProject(ResearchProject researchProject, String keywords) throws NoneUniqueNameException
    {
        ResearchProject persistedProject = ResearchProject.findResearchProject(researchProject.getId());
        ResearchGroup group = persistedProject.getResearchGroup();
        researchProject.setResearchGroup(group);

        if (researchProject.isDuplicateWithin(group))
        {
            throw new NoneUniqueNameException("Project name [" + researchProject.getName() + "] is not unique");
        }
        researchProject.updateRifCsIfNeeded(rifCsWriter);
        tokenInputHelper.setKeywordsForProject(keywords, researchProject);
        researchProject.merge();
        ResearchProject.indexResearchProject(researchProject);
        return researchProject;
    }

    @Override
    public ResearchProject create(ResearchProject researchProject, ResearchGroup researchGroup, String keywords)
        throws NoneUniqueNameException
    {
        if (researchProject.isDuplicateWithin(researchGroup))
        {
            throw new NoneUniqueNameException("Project name [" + researchProject.getName() + "] is not unique");
        }
        researchGroup.associateWithResearchProject(researchProject);
        tokenInputHelper.setKeywordsForProject(keywords, researchProject);
        researchProject.persist();
        ResearchProject.indexResearchProject(researchProject);
        return researchProject;
    }

}
