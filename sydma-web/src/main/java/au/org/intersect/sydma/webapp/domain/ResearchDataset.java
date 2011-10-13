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
package au.org.intersect.sydma.webapp.domain;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;

import au.org.intersect.sydma.webapp.permission.dataset.DatasetPermissionQuery;
import au.org.intersect.sydma.webapp.service.DatasetReadyToPublishMailService;
import au.org.intersect.sydma.webapp.util.RifCsWriter;

/**
 * Research Dataset Model.
 */
@RooJavaBean
@RooToString
@RooJson
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name", "research_project"}))
@RooEntity(finders = {"findResearchDatasetsByNameEqualsAndResearchProject", "findResearchDatasetsByNameEquals",
        "findResearchDatasetsByResearchProject"}, persistenceUnit = "sydmaPU")
// TODO CHECKSTYLE-OFF: MagicNumber
public class ResearchDataset
{

    @NotNull
    @Size(min = 1, max = 100)
    @Pattern(regexp = "\\p{Alnum}[\\w\\d\\s]*", message = "Must be alphanumeric")
    private String name;

    @NotNull
    @ManyToOne
    private ResearchSubjectCode subjectCode;

    @ManyToOne
    private ResearchSubjectCode subjectCode2;
    
    @ManyToOne
    private ResearchSubjectCode subjectCode3;
    
    @Size(max = 1000)
    @NotNull
    @NotEmpty(message = "Description is a required field")
    @Pattern(regexp = "\\s*\\S+[\\s\\S]*", message = "Description is a required field")
    private String description;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @NotNull
    private ResearchProject researchProject;

    @Enumerated(EnumType.STRING)
    private PubliciseStatus publiciseStatus;

    @ManyToOne
    private PublicAccessRight publicAccessRight;
    
    private Boolean isPhysical;
    
    @ManyToOne
    private Building physicalLocation;
    
    @OneToOne(cascade = CascadeType.ALL)
    private ResearchDatasetDB databaseInstance;

    private String additionalLocationInformation;


    public void setResearchProject(ResearchProject researchProject)
    {
        this.researchProject = researchProject;
    }

    public boolean isAdvertised()
    {
        return this.publiciseStatus == PubliciseStatus.ADVERTISED;
    }

    public boolean isReadyForAdvertising()
    {
        return this.publiciseStatus == PubliciseStatus.MARKED_AS_READY_FOR_ADVERTISING;
    }

    public boolean isNotAdvertised()
    {
        return this.publiciseStatus == PubliciseStatus.NOT_ADVERTISED;
    }

    public void markAsAdvertised()
    {
        this.publiciseStatus = PubliciseStatus.ADVERTISED;
    }

    public void markAsReadyForAdvertising()
    {
        this.publiciseStatus = PubliciseStatus.MARKED_AS_READY_FOR_ADVERTISING;
    }

    public void markAsNotAdvertised()
    {
        this.publiciseStatus = PubliciseStatus.NOT_ADVERTISED;
    }

    public void publishRifCS(RifCsWriter rifCsWriter)
    {
        ResearchProject project = this.researchProject;
        ResearchGroup group = project.getResearchGroup();
        User principalInvestigator = group.getPrincipalInvestigator();

        rifCsWriter.writeGroupRifCs(group);
        rifCsWriter.writePrincipalInvestigatorRifCs(principalInvestigator, group);
        rifCsWriter.writeProjectRifCs(project);
        rifCsWriter.writeDatasetRifCs(this);
    }
    
    public void eraseRifCs(RifCsWriter rifCsWriter)
    {
        ResearchProject project = this.researchProject;
        ResearchGroup group = project.getResearchGroup();
        User principalInvestigator = group.getPrincipalInvestigator();
        
        rifCsWriter.eraseDatasetRifCs(this);
        rifCsWriter.eraseProjectRifCs(project);
        project.updateRifCsIfNeeded(rifCsWriter);
        rifCsWriter.eraseGroupRifCs(group);
        group.updateRifCsIfNeeded(rifCsWriter, principalInvestigator);
        rifCsWriter.erasePrincipalInvestigatorRifCs(principalInvestigator, group);
        principalInvestigator.updatePiRifCs(rifCsWriter, group);
        
    }

    public String getKeyForRifCs()
    {
        return "www.sydney.edu.au-metadata-aggregator-research-dataset-" + this.getId();
    }

    public static TypedQuery<ResearchDataset> findResearchDatasetsByNameEqualsAndResearchProjectWithIdNotEqual(Long id,
            String name, ResearchProject researchProject)
    {
        if (name == null || name.length() == 0)
        {
            throw new IllegalArgumentException("The name argument is required");
        }
        if (researchProject == null)
        {
            throw new IllegalArgumentException("The researchProject argument is required");
        }
        EntityManager em = ResearchDataset.entityManager();
        String query;
        query = "SELECT o FROM ResearchDataset AS o WHERE o.name = :name  AND o.researchProject = :researchProject "
                + "AND o.id != :id";
        TypedQuery<ResearchDataset> q = em.createQuery(query, ResearchDataset.class);
        q.setParameter("id", id);
        q.setParameter("name", name);
        q.setParameter("researchProject", researchProject);
        return q;
    }

    public static List<ResearchDataset> findAllResearchDatasets()
    {
        return entityManager().createQuery("SELECT o FROM ResearchDataset o ORDER BY o.name", ResearchDataset.class)
                .getResultList();
    }

    public static TypedQuery<ResearchDataset> findResearchDatasetsByResearchProject(ResearchProject researchProject)
    {
        if (researchProject == null)
        {
            throw new IllegalArgumentException("The researchProject argument is required");
        }
        EntityManager em = ResearchDataset.entityManager();
        TypedQuery<ResearchDataset> q = em.createQuery(
                "SELECT o FROM ResearchDataset AS o WHERE o.researchProject = :researchProject ORDER BY o.name",
                ResearchDataset.class);
        q.setParameter("researchProject", researchProject);
        return q;
    }

    /**
     * returns a boolean to indicate if anything happened or not
     * 
     * @param isPi
     * @param additionalAccessInfo
     * @param publicAccessRight
     * @param rifCsWriter
     * @param mailService
     * @param baseUrl
     * @return
     */
    public boolean advertise(String currentUser, PublicAccessRight publicAccessRight, RifCsWriter rifCsWriter,
            DatasetReadyToPublishMailService mailService, String baseUrl)
    {
        boolean isPi = currentUser.equals(getResearchProject().getResearchGroup().getPrincipalInvestigator()
                .getUsername());

        // PI can advertise as long as its not already advertised
        if (isPi && !isAdvertised())
        {
            setPublicAccessRight(publicAccessRight);
            markAsAdvertised();
            publishRifCS(rifCsWriter);
            return true;
        }
        // other users can only mark as ready for advertising
        else if (!isPi && isNotAdvertised())
        {
            setPublicAccessRight(publicAccessRight);
            markAsReadyForAdvertising();
            mailService.sendReadyToPublishEmail(this, currentUser, baseUrl);
            return true;
        }
        return false;

    }

    public boolean rejectAdvertise(String currentUser)
    {
        boolean isPi = currentUser.equals(getResearchProject().getResearchGroup().getPrincipalInvestigator()
                .getUsername());
        if (isPi && isReadyForAdvertising())
        {
            markAsNotAdvertised();
            return true;
        }
        return false;
    }

    public boolean unadvertise(String currentUser, RifCsWriter rifCsWriter)
    {
        boolean isPi = currentUser.equals(getResearchProject().getResearchGroup().getPrincipalInvestigator()
                .getUsername());

        if (isPi && isAdvertised())
        {
            eraseRifCs(rifCsWriter);
            markAsNotAdvertised();
            return true;
        }
        return false;
    }

    public boolean canBeAdvertisedBy(User user)
    {
        if (isNotAdvertised())
        {
            return true;
        }
        else if (isReadyForAdvertising())
        {
            User pi = getResearchProject().getResearchGroup().getPrincipalInvestigator();

            if (pi.getId().equals(user.getId()))
            {
                return true;
            }
        }
        return false;

    }

    public boolean canBeRejectedBy(User user)
    {
        if (isReadyForAdvertising())
        {
            User pi = getResearchProject().getResearchGroup().getPrincipalInvestigator();

            if (pi.getId().equals(user.getId()))
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean canBeUnadvertisedBy(User user)
    {
        if (isAdvertised()
                && user.getId().equals(getResearchProject().getResearchGroup().getPrincipalInvestigator().getId()))
        {
            return true;        
        }
        return false;
    }

    public static boolean isDuplicate(Long id, String name, ResearchProject researchProject)
    {
        if (id == null)
        {
            TypedQuery<ResearchDataset> queryResult = findResearchDatasetsByNameEqualsAndResearchProject(name,
                    researchProject);
            return !queryResult.getResultList().isEmpty();
        }
        else
        {
            TypedQuery<ResearchDataset> queryResult = findResearchDatasetsByNameEqualsAndResearchProjectWithIdNotEqual(
                    id, name, researchProject);
            return !queryResult.getResultList().isEmpty();
        }
    }

    public void updateRifCsIfNeeded(RifCsWriter rifCsWriter)
    {
        if (isAdvertised())
        {
            rifCsWriter.writeDatasetRifCs(this);
        }
    }

    public static List<ResearchDataset> findDatasetsWithPermission(DatasetPermissionQuery permissionQuery)
    {
        TypedQuery<ResearchDataset> query = permissionQuery.createQuery(ResearchDataset.entityManager());
        return query.getResultList();
    }
}
