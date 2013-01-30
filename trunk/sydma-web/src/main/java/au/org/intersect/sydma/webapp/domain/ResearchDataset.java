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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.scheduling.annotation.Async;

import au.org.intersect.sydma.webapp.permission.dataset.DatasetPermissionQuery;
import au.org.intersect.sydma.webapp.service.DatasetReadyToPublishMailService;
import au.org.intersect.sydma.webapp.service.IndexingService;
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
// TODO CHECKSTYLE-OFF: ClassFanOutComplexityCheck
public class ResearchDataset
{
    private static final String EMPTY_STRING = " ";

    private static final Logger LOGGER = LoggerFactory.getLogger(ResearchDataset.class);

    @Autowired
    private transient SolrServer solrServer;

    @Autowired
    private transient IndexingService indexingService;
    
    @Value("#{applicationTypeProperties['application.type']}")
    private transient String applicationType;

    @NotNull
    @Size(min = 1, max = 100, message = "Must be between 1 and 100 characters in length")
    @Pattern(regexp = "\\p{Alnum}[\\w\\d\\s]*", message = "Must be alphanumeric")
    private String name;

    @NotNull
    @ManyToOne
    private ResearchSubjectCode subjectCode;

    @ManyToOne
    private ResearchSubjectCode subjectCode2;

    @ManyToOne
    private ResearchSubjectCode subjectCode3;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @Valid
    private List<Publication> publications = new ArrayList<Publication>();

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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "researchDataset")
    private List<DBBackup> dbBackups = new ArrayList<DBBackup>();

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "researchDataset")
    private ResearchDatasetDB databaseInstance;

    private String additionalLocationInformation;

    @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
    private DateTime dateFrom;

    @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
    private DateTime dateTo;
    
    @ManyToMany
    private List<Vocabulary> keywords = new ArrayList<Vocabulary>();

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
        if ("AGR_ENV".equals(applicationType))
        {
            return "www.sydney.edu.au-agriculture-environment-research-dataset-" + this.getId();
        }
        else
        {
            return "www.sydney.edu.au-metadata-aggregator-research-dataset-" + this.getId();
        }
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
        boolean canAdvertisePhysical = false;
        if (isPhysical)
        {
            User user = User.findUsersByUsernameEquals(currentUser).getSingleResult();
            Role role = Role.findRolesByNameEquals(Role.RoleNames.ROLE_RESEARCH_DATA_MANAGER.toString())
                    .getSingleResult();
            if (user.getRoles().contains(role))
            {
                canAdvertisePhysical = true;
            }
        }
        if ((isPi || canAdvertisePhysical) && !isAdvertised())
        {
            setPublicAccessRight(publicAccessRight);
            markAsAdvertised();
            publishRifCS(rifCsWriter);
            return true;
        }
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
        User user = User.findUsersByUsernameEquals(currentUser).getSingleResult();
        if (canBeUnadvertisedBy(user))
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
        if (isAdvertised() && 
              (user.getId().equals(getResearchProject().getResearchGroup().getPrincipalInvestigator().getId())
               || user.hasRole(Role.RoleNames.ROLE_ICT_SUPPORT)
               || user.hasRole(Role.RoleNames.ROLE_RESEARCH_DATA_MANAGER)))
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
    
    public static void reindexResearchDatasetForKeyword(ResearchDataset dataset, Vocabulary term)
    {
        new ResearchDataset().indexingService.reindexResearchDatasetKeyword(dataset, term);
    }
    
    @Async
    public static void reindexResearchDatasetForKeywordAsync(ResearchDataset dataset, Vocabulary term)
    {
        if (dataset.getKeywords().contains(term))
        {
            Collection<ResearchDataset> dts = new ArrayList<ResearchDataset>();
            dts.add(dataset);
            indexResearchDatasetsAsync(dts);
        }
    }
    
    @Async
    public static void indexResearchDatasets(Collection<ResearchDataset> researchDatasets)
    {
        sleep(1L);
        new ResearchDataset().indexingService.indexResearchDatasets(researchDatasets);
    }
    
    // TODO CHECKSTYLE-OFF: ExecutableStatementCountCheck
    public static void indexResearchDatasetsAsync(Collection<ResearchDataset> researchDatasets)
    {
        List<SolrInputDocument> documents = new ArrayList<SolrInputDocument>();
        float boostValue = 5;
        for (ResearchDataset researchDataset : researchDatasets)
        {
            SolrInputDocument sid = new SolrInputDocument();
            sid.addField("id", "researchdataset_" + researchDataset.getId());
            sid.addField("researchdataset.id_l", researchDataset.getId());
            sid.addField("researchdataset.name_s", researchDataset.getName());
            sid.addField("researchdataset.subjectcode_t", researchDataset.getSubjectCode());
            sid.addField("researchdataset.subjectcode2_t", researchDataset.getSubjectCode2());
            sid.addField("researchdataset.subjectcode3_t", researchDataset.getSubjectCode3());
            sid.addField("researchdataset.description_s", researchDataset.getDescription());
            sid.addField("researchdataset.researchproject_t", researchDataset.getResearchProject());
            sid.addField("researchdataset.publicisestatus_t", researchDataset.getPubliciseStatus());
            sid.addField("researchdataset.publicaccessright_t", researchDataset.getPublicAccessRight());
            sid.addField("researchdataset.isphysical_b", researchDataset.getIsPhysical());
            sid.addField("researchdataset.physicallocation_t", researchDataset.getPhysicalLocation());
            sid.addField("researchdataset.databaseinstance_t", researchDataset.getDatabaseInstance());
            sid.addField("researchdataset.additionallocationinformation_s",
                    researchDataset.getAdditionalLocationInformation());
            sid.addField("researchdataset.datefrom_t", researchDataset.getDateFrom());
            sid.addField("researchdataset.dateto_t", researchDataset.getDateTo());
            sid.addField("researchdataset.keywords_t", Vocabulary.toSolrText(researchDataset.getKeywords()));
            // Add summary field to allow searching documents for objects of this type
            sid.addField(
                    "researchdataset_solrsummary_t",
                    new StringBuilder().append(researchDataset.getId()).append(EMPTY_STRING)
                            .append(researchDataset.getName()).append(EMPTY_STRING)
                            .append(researchDataset.getSubjectCode()).append(EMPTY_STRING)
                            .append(researchDataset.getSubjectCode2()).append(EMPTY_STRING)
                            .append(researchDataset.getSubjectCode3()).append(EMPTY_STRING)
                            .append(researchDataset.getDescription()).append(EMPTY_STRING)
                            .append(researchDataset.getResearchProject()).append(EMPTY_STRING)
                            .append(researchDataset.getPubliciseStatus()).append(EMPTY_STRING)
                            .append(researchDataset.getPublicAccessRight()).append(EMPTY_STRING)
                            .append(researchDataset.getIsPhysical()).append(EMPTY_STRING)
                            .append(researchDataset.getPhysicalLocation()).append(EMPTY_STRING)
                            .append(researchDataset.getDatabaseInstance()).append(EMPTY_STRING)
                            .append(researchDataset.getAdditionalLocationInformation()).append(EMPTY_STRING)
                            .append(researchDataset.getDateFrom()).append(EMPTY_STRING)
                            .append(researchDataset.getDateTo()).append(EMPTY_STRING)
                            .append(researchDataset.getKeywords()));
            sid.setDocumentBoost(boostValue);
            documents.add(sid);
        }

        try
        {
            SolrServer solrServer = solrServer();
            solrServer.add(documents);
            solrServer.commit();
        }
        catch (SolrServerException e)
        {
            LOGGER.error(e.getMessage());
        }
        catch (IOException e)
        {
            LOGGER.error(e.getMessage());
        }
    }

    public static void deleteIndex(ResearchDataset researchdataset)
    {
        SolrServer solrServer = solrServer();

        try
        {
            solrServer.deleteById("researchdataset_" + researchdataset.getId());
            solrServer.commit();
        }
        catch (SolrServerException e)
        {
            LOGGER.error(e.getMessage());
        }
        catch (IOException e)
        {
            LOGGER.error(e.getMessage());
        }
    }  

    // Pushed methods from solr aspect file
    public static QueryResponse search(String queryString)
    {
        String searchString = "ResearchDataset_solrsummary_t:" + queryString;
        return search(new SolrQuery(searchString.toLowerCase()));
    }

    public static QueryResponse search(SolrQuery query)
    {
        try
        {
            return solrServer().query(query);
        }
        catch (SolrServerException e)
        {
            LOGGER.error("Solr search error - " + e);
        }
        return new QueryResponse();
    }

    public static void indexResearchDataset(ResearchDataset researchdataset)
    {
        List<ResearchDataset> researchdatasets = new ArrayList<ResearchDataset>();
        researchdatasets.add(researchdataset);
        indexResearchDatasets(researchdatasets);
    }

    public static final SolrServer solrServer()
    {
        SolrServer solrServer = new ResearchDataset().solrServer;
        if (solrServer == null)
        {
            throw new IllegalStateException("Solr server has not been injected");
        }
        return solrServer;
    }

    private static void sleep(Long seconds)
    {
        Long millis = seconds * 1000;
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
            LOGGER.error("Interrupt exception= " + e);
        }
    }
}
