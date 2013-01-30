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

import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.org.intersect.sydma.webapp.domain.PubliciseStatus;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
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
public class ResearchDatasetServiceImpl implements ResearchDatasetService
{

    @Autowired
    private FileAccessService fileAccessService;

    @Autowired
    private RifCsWriter rifCsWriter;

    @Autowired
    private TokenInputHelper tokenInputHelper;

    @Override
    public ResearchDataset createDataset(Long projectId, ResearchDataset researchDataset, String keywords)
        throws NoneUniqueNameException
    {
        ResearchProject researchProject = ResearchProject.findResearchProject(projectId);
        if (researchProject == null)
        {
            throw new EntityNotFoundException("ResearchProject with id [" + projectId + "] could not be found");
        }

        String datasetName = researchDataset.getName();

        if (ResearchDataset.isDuplicate(null, researchDataset.getName(), researchProject))
        {
            throw new NoneUniqueNameException("Dataset name [" + datasetName + "] is not unique");
        }

        Boolean isPhysical = researchProject.getResearchGroup().getIsPhysical();

        researchDataset.setIsPhysical(isPhysical);

        researchProject.associateWithResearchDataset(researchDataset);
        researchDataset.setPubliciseStatus(PubliciseStatus.NOT_ADVERTISED);
        tokenInputHelper.setKeywordsForDataset(keywords, researchDataset);
        researchDataset.persist();
        if (BooleanUtils.isNotTrue(isPhysical))
        {
            if (!fileAccessService.prepareDatasetFileSpace(researchDataset))
            {
                // TODO: Decide what to do to recover
            }
        }
        ResearchDataset.indexResearchDataset(researchDataset);
        return researchDataset;
    }

    @Override
    public ResearchDataset editDataset(ResearchDataset researchDataset, String keywords) throws NoneUniqueNameException
    {
        ResearchDataset persistedDataset = ResearchDataset.findResearchDataset(researchDataset.getId());
        ResearchProject project = persistedDataset.getResearchProject();
        researchDataset.setResearchProject(project);
        researchDataset.setPubliciseStatus(persistedDataset.getPubliciseStatus());
        researchDataset.setIsPhysical(persistedDataset.getIsPhysical());
        researchDataset.setDatabaseInstance(persistedDataset.getDatabaseInstance());
        String datasetName = researchDataset.getName();

        if (ResearchDataset.isDuplicate(researchDataset.getId(), researchDataset.getName(), project))
        {
            throw new NoneUniqueNameException("Dataset name [" + datasetName + "] is not unique");
        }
        tokenInputHelper.setKeywordsForDataset(keywords, researchDataset);
        researchDataset.merge();
        researchDataset.updateRifCsIfNeeded(rifCsWriter);
        ResearchDataset.indexResearchDataset(researchDataset);
        return researchDataset;
    }
}
