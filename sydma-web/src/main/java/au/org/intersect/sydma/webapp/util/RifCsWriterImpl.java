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
package au.org.intersect.sydma.webapp.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import au.org.intersect.sydma.webapp.domain.Building;
import au.org.intersect.sydma.webapp.domain.PubliciseStatus;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;
import au.org.intersect.sydma.webapp.domain.User;

/**
 * Class to generate RIFCS using templates
 * 
 * @version $Rev: 29 $
 */
public class RifCsWriterImpl implements RifCsWriter
{
    private static final String SYDNEY_UNI_KEY_ATTRIBUTE_NAME = "sydneyUniKey";
    private static final Logger LOG = LoggerFactory.getLogger(RifCsWriterImpl.class);
    private static final String DOT_XML = ".xml";
    private static final String SYDNEY_UNI_KEY = "sydney.edu.au/stc/PTY/0006";

    private static final String HAS_SUBJECT_CODE_2 = "hasSubjectCode2";
    private static final String HAS_SUBJECT_CODE_3 = "hasSubjectCode3";
    private String destinationDirectory;

    @Required
    public void setDestinationDirectory(String destinationDirectory)
    {
        this.destinationDirectory = destinationDirectory;
    }

    public void writeDatasetRifCs(ResearchDataset dataset)
    {
        Map<String, Object> datasetAttributes = new HashMap<String, Object>();
        datasetAttributes.put("researchDataset", dataset);

        Building physicalLocation = dataset.getPhysicalLocation();
        if (physicalLocation != null)
        {
            datasetAttributes.put("hasLocation", Boolean.TRUE);
            datasetAttributes.put("buildingName", physicalLocation.getBuildingName());
            addAttributeIfNotEmpty(datasetAttributes, "additionalLocationInformation",
                    dataset.getAdditionalLocationInformation());
            addAttributeIfNotEmpty(datasetAttributes, "address1", physicalLocation.getAddressLine1());
            addAttributeIfNotEmpty(datasetAttributes, "address2", physicalLocation.getAddressLine2());
            addAttributeIfNotEmpty(datasetAttributes, "city", physicalLocation.getCity());
            addAttributeIfNotEmpty(datasetAttributes, "state", physicalLocation.getStateName());
            addAttributeIfNotEmpty(datasetAttributes, "postalCode", physicalLocation.getPostCode());
            addAttributeIfNotEmpty(datasetAttributes, "country", physicalLocation.getCountry());
        }

        datasetAttributes.put(HAS_SUBJECT_CODE_2, dataset.getSubjectCode2() == null ? Boolean.FALSE : Boolean.TRUE);
        datasetAttributes.put(HAS_SUBJECT_CODE_3, dataset.getSubjectCode3() == null ? Boolean.FALSE : Boolean.TRUE);
        writeFileFromTemplate("dataset", datasetAttributes, dataset.getKeyForRifCs() + DOT_XML);
    }

    private void addAttributeIfNotEmpty(Map<String, Object> datasetAttributes, String attributeName,
            String attributeValue)
    {
        if (attributeValue != null && attributeValue.length() > 0)
        {
            datasetAttributes.put(attributeName, attributeValue);
        }
    }

    public void writeProjectRifCs(ResearchProject project)
    {
        Map<String, Object> projectAttributes = new HashMap<String, Object>();
        projectAttributes.put("researchProject", project);
        // only add url if they not blank, this way the template knows whether or not to render
        if (project.getUrl() != null && !project.getUrl().isEmpty())
        {
            projectAttributes.put("url", project.getUrl());
        }

        projectAttributes.put(HAS_SUBJECT_CODE_2, project.getSubjectCode2() == null ? Boolean.FALSE : Boolean.TRUE);
        projectAttributes.put(HAS_SUBJECT_CODE_3, project.getSubjectCode3() == null ? Boolean.FALSE : Boolean.TRUE);
        writeFileFromTemplate("activity", projectAttributes, project.getKeyForRifCs() + DOT_XML);
    }

    public void writeGroupRifCs(ResearchGroup group)
    {
        Map<String, Object> groupAttributes = new HashMap<String, Object>();
        groupAttributes.put("researchGroup", group);
        // only add url and description if they are not blank, this way the template knows whether or not to render
        if (group.getUrl() != null && !group.getUrl().isEmpty())
        {
            groupAttributes.put("url", group.getUrl());
        }
        if (group.getDescription() != null && !group.getDescription().isEmpty())
        {
            groupAttributes.put("description", group.getDescription());
        }
        groupAttributes.put(HAS_SUBJECT_CODE_2,
                 group.getSubjectCode2() == null ? Boolean.FALSE : Boolean.TRUE);
        groupAttributes.put(HAS_SUBJECT_CODE_3,
                 group.getSubjectCode3() == null ? Boolean.FALSE : Boolean.TRUE);

        writeFileFromTemplate("party", groupAttributes, group.getKeyForRifCs() + DOT_XML);
    }

    public void writePrincipalInvestigatorRifCs(User principalInvestigator, ResearchGroup group)
    {
        Map<String, Object> piAttributes = new HashMap<String, Object>();
        piAttributes.put("researchGroup", group);
        piAttributes.put("user", principalInvestigator);
        piAttributes.put(HAS_SUBJECT_CODE_2,
                 group.getSubjectCode2() == null ? Boolean.FALSE : Boolean.TRUE);
        piAttributes.put(HAS_SUBJECT_CODE_3,
                 group.getSubjectCode3() == null ? Boolean.FALSE : Boolean.TRUE);
        writeFileFromTemplate("principalInvestigator", piAttributes, principalInvestigator.getKeyForRifCs() + DOT_XML);
    }

    @Override
    public void deletePrincipalInvestigatorRifCs(User principalInvestigator)
    {
        File file = new File(destinationDirectory + File.separator + principalInvestigator.getKeyForRifCs() + DOT_XML);
        boolean deleteResult = file.delete();
        if (!deleteResult)
        {
            throw new RuntimeException("Failed to delete principal investigator RIFCS file: " + file.getAbsolutePath());
        }
    }

    private void writeFileFromTemplate(String templateName, Map<String, Object> attributes, String fileName)
    {
        attributes.put(SYDNEY_UNI_KEY_ATTRIBUTE_NAME, SYDNEY_UNI_KEY);
        StringTemplateGroup group = new StringTemplateGroup("rif-cs");
        StringTemplate template = group.getInstanceOf("META-INF/rif-cs/" + templateName);
        template.registerRenderer(String.class, new XmlEscapingRenderer());
        template.setAttributes(attributes);
        try
        {
            writeToFile(template.toString(), destinationDirectory + File.separator + fileName);
        }
        catch (IOException e)
        {
            LOG.error("Failed to write rif-cs", e);
            throw new RuntimeException("Failed to write rif-cs", e);
        }
    }

    private void writeToFile(String content, String fileName) throws IOException
    {
        FileUtils.writeStringToFile(new File(fileName), content);
    }

    public void eraseDatasetRifCs(ResearchDataset dataset)
    {
        String filename = dataset.getKeyForRifCs();
        eraseFileWithCatch(filename);
        dataset.setPubliciseStatus(PubliciseStatus.NOT_ADVERTISED);
    }

    public void eraseProjectRifCs(ResearchProject project)
    {
        if (!project.hasAdvertisedDatasets())
        {
            String filename = project.getKeyForRifCs();
            eraseFileWithCatch(filename);
        }
    }

    public void eraseGroupRifCs(ResearchGroup group)
    {
        if (!group.hasAdvertisedProjects())
        {
            String filename = group.getKeyForRifCs();
            eraseFileWithCatch(filename);
        }

    }

    public void erasePrincipalInvestigatorRifCs(User principalInvestigator, ResearchGroup group)
    {
        if (!principalInvestigator.isPrincipalInvestigatorForAnAdvertisedGroup())
        {
            String filename = principalInvestigator.getKeyForRifCs();
            eraseFileWithCatch(filename);
        }
    }

    private void eraseFileWithCatch(String filename)
    {
        try
        {
            eraseFile(filename);
        }
        catch (IOException e)
        {
            LOG.error("Failed to delete the following rif-cs file", filename, e);
            throw new RuntimeException("Failed to delete the rif-cs file", e);
        }
    }

    private void eraseFile(String filename) throws IOException
    {
        File file = new File(destinationDirectory + File.separator + filename + DOT_XML);
        file.delete();
    }

}
