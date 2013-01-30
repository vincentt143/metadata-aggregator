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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.scheduling.annotation.Async;

import au.org.intersect.sydma.webapp.permission.group.GroupPermissionQuery;
import au.org.intersect.sydma.webapp.service.IndexingService;
import au.org.intersect.sydma.webapp.util.RifCsWriter;

/**
 * Research Group Model.
 */
@RooJavaBean
@RooToString(excludeFields = {"duplicate", "advertisedResearchProjects"})
@RooJson
@RooEntity(finders = {"findResearchGroupsByIdEquals", "findResearchGroupsByNameEquals",
        "findResearchGroupsByDirectoryPathEquals", "findResearchGroupsByIsPhysicalNot", 
        "findResearchGroupsByIsPhysical"}, persistenceUnit = "sydmaPU")     
// TODO CHECKSTYLE-OFF: MagicNumber
public class ResearchGroup
{
    private static final String EMPTY_STRING = " ";

    private static final Logger LOGGER = LoggerFactory.getLogger(ResearchGroup.class);

    @Autowired
    private transient SolrServer solrServer;
    
    @Autowired
    private transient IndexingService indexingService;

    @Value("#{applicationTypeProperties['application.type']}")
    private transient String applicationType;

    @NotNull
    @Size(min = 1, max = 100, message = "Must be between 1 and 100 characters in length")
    @Column(unique = true)
    @Pattern(regexp = "\\p{Alnum}[\\w\\d\\s]*", message = "Must be alphanumeric")
    private String name;

    @NotNull
    @ManyToOne
    private ResearchSubjectCode subjectCode;

    @ManyToOne
    private ResearchSubjectCode subjectCode2;

    @ManyToOne
    private ResearchSubjectCode subjectCode3;

    @NotNull(message = "Principal Investigator is a required field")
    @OneToOne
    private User principalInvestigator;

    @OneToOne
    private User dataManagementContact;

    @URL(protocol = "", message = "Must be a valid URL")
    @Size(max = 400, message = "Must not exceed 400 characters in length")
    private String url;

    @NotNull
    @Size(max = 1000, message = "Must not exceed 1000 characters in length")
    @NotEmpty(message = "Description is a required field")
    private String description;

    private String directoryPath;

    private Boolean isPhysical;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "researchGroup")
    private Set<ResearchProject> researchProjects = new HashSet<ResearchProject>();

    @ManyToMany
    private List<Vocabulary> keywords;

    public void associateWithResearchProject(ResearchProject researchProject)
    {
        if (researchProjects == null)
        {
            researchProjects = new HashSet<ResearchProject>();
        }
        researchProjects.add(researchProject);
        researchProject.setResearchGroup(this);
    }

    public String getOriginatingSource()
    {
        if (this.url == null || this.url.isEmpty())
        {
            return "www.sydney.edu.au";
        }
        else
        {
            return this.url;
        }
    }

    public String getKeyForRifCs()
    {
        if ("AGR_ENV".equals(applicationType))
        {
            return "www.sydney.edu.au-agriculture-environment-research-group-" + this.getId();
        }
        else
        {
            return "www.sydney.edu.au-metadata-aggregator-research-group-" + this.getId();
        }
    }

    public List<ResearchProject> getAdvertisedResearchProjects()
    {
        List<ResearchProject> advertisedProjects = new ArrayList<ResearchProject>();
        for (ResearchProject project : this.getResearchProjects())
        {
            if (project.hasAdvertisedDatasets())
            {
                advertisedProjects.add(project);
            }
        }
        Collections.sort(advertisedProjects, new Comparator<ResearchProject>()
        {

            @Override
            public int compare(ResearchProject p1, ResearchProject p2)
            {
                return p1.getId().compareTo(p2.getId());
            }
        });
        return advertisedProjects;
    }

    public boolean hasAdvertisedProjects()
    {
        return !getAdvertisedResearchProjects().isEmpty();
    }

    public boolean getHasAdvertisedProjects()
    {
        return !getAdvertisedResearchProjects().isEmpty();
    }

    public static TypedQuery<ResearchGroup> findResearchGroupsWithNameEqualsAndIdNotEquals(String name, Long id)
    {
        if (name == null || name.length() == 0)
        {
            throw new IllegalArgumentException("The name argument is required");
        }
        EntityManager em = ResearchDataset.entityManager();
        String query = "SELECT o FROM ResearchGroup AS o WHERE o.name = :name AND o.id != :id";
        TypedQuery<ResearchGroup> q = em.createQuery(query, ResearchGroup.class);
        q.setParameter("id", id);
        q.setParameter("name", name);
        return q;
    }

    public static List<ResearchGroup> findAllResearchGroups()
    {
        return entityManager().createQuery("SELECT o FROM ResearchGroup o ORDER BY o.name", ResearchGroup.class)
                .getResultList();
    }

    public boolean isDuplicate()
    {
        if (getId() == null)
        {
            return !findResearchGroupsByNameEquals(name).getResultList().isEmpty();
        }
        else
        {
            return !findResearchGroupsWithNameEqualsAndIdNotEquals(name, getId()).getResultList().isEmpty();
        }
    }

    public String getDirectoryPath()
    {
        LOGGER.debug("Research Group id" + getId());
        LOGGER.debug("Directory path" + this.directoryPath);
        return this.directoryPath;
    }

    public void updateRifCsIfNeeded(RifCsWriter rifCsWriter, User previousPI)
    {
        if (hasAdvertisedProjects())
        {
            rifCsWriter.writeGroupRifCs(this);
            if (getPrincipalInvestigator().getId() != previousPI.getId())
            {
                rifCsWriter.deletePrincipalInvestigatorRifCs(previousPI);
                rifCsWriter.writePrincipalInvestigatorRifCs(getPrincipalInvestigator(), this);
                // we also need to update all the project and dataset records under this
                for (ResearchProject project : this.getAdvertisedResearchProjects())
                {
                    rifCsWriter.writeProjectRifCs(project);
                    for (ResearchDataset dataset : project.getAdvertisedResearchDatasets())
                    {
                        rifCsWriter.writeDatasetRifCs(dataset);
                    }
                }
            }
            else
            {
                // we still update the PI record, as it contains some details from the group
                rifCsWriter.writePrincipalInvestigatorRifCs(getPrincipalInvestigator(), this);
            }
        }
    }

    public static List<ResearchGroup> findResearchGroupWithPermission(GroupPermissionQuery groupPermissionQuery)
    {
        TypedQuery<ResearchGroup> query = groupPermissionQuery.createQuery(ResearchDataset.entityManager());
        return query.getResultList();
    }

    @Async
    public static void reindexResearchGroupForKeywordAsync(ResearchGroup group, Vocabulary term)
    {
        LOGGER.debug("Keywords: " + group.getKeywords());
        
        if (group.getKeywords().contains(term))
        {
            Collection<ResearchGroup> rgs = new ArrayList<ResearchGroup>();
            rgs.add(group);
            indexResearchGroupsAsync(rgs);
        }
        else
        {
            for (ResearchProject project : group.getResearchProjects())
            {
                ResearchProject.reindexResearchProjectForKeyword(project, term);
            }
        }
    }
    
    @Async
    public static void indexResearchGroups(Collection<ResearchGroup> researchGroups)
    {
        sleep(1L);
        new ResearchGroup().indexingService.indexResearchGroups(researchGroups);
    }

    public static void indexResearchGroupsAsync(Collection<ResearchGroup> researchGroups)
    {
        List<SolrInputDocument> documents = new ArrayList<SolrInputDocument>();
        for (ResearchGroup researchGroup : researchGroups)
        {
            LOGGER.info("Indexing RG: " + researchGroup.getId());
            SolrInputDocument sid = new SolrInputDocument();
            sid.addField("id", "researchgroup_" + researchGroup.getId());
            sid.addField("researchgroup.directorypath_s", researchGroup.getDirectoryPath());
            sid.addField("researchgroup.id_l", researchGroup.getId());
            sid.addField("researchgroup.name_s", researchGroup.getName());
            sid.addField("researchgroup.subjectcode_t", researchGroup.getSubjectCode());
            sid.addField("researchgroup.subjectcode2_t", researchGroup.getSubjectCode2());
            sid.addField("researchgroup.subjectcode3_t", researchGroup.getSubjectCode3());
            sid.addField("researchgroup.principalinvestigator_t", researchGroup.getPrincipalInvestigator());
            sid.addField("researchgroup.datamanagementcontact_t", researchGroup.getDataManagementContact());
            sid.addField("researchgroup.url_s", researchGroup.getUrl());
            sid.addField("researchgroup.description_s", researchGroup.getDescription());
            sid.addField("researchgroup.isphysical_b", researchGroup.getIsPhysical());
            sid.addField("researchgroup.keywords_t", Vocabulary.toSolrText(researchGroup.getKeywords()));
            // Add summary field to allow searching documents for objects of this type
            sid.addField(
                    "researchgroup_solrsummary_t",
                    new StringBuilder().append(researchGroup.getDirectoryPath()).append(EMPTY_STRING)
                            .append(researchGroup.getId()).append(EMPTY_STRING).append(researchGroup.getName())
                            .append(EMPTY_STRING).append(researchGroup.getSubjectCode()).append(EMPTY_STRING)
                            .append(researchGroup.getSubjectCode2()).append(EMPTY_STRING)
                            .append(researchGroup.getSubjectCode3()).append(EMPTY_STRING)
                            .append(researchGroup.getPrincipalInvestigator()).append(EMPTY_STRING)
                            .append(researchGroup.getDataManagementContact()).append(EMPTY_STRING)
                            .append(researchGroup.getUrl()).append(EMPTY_STRING).append(researchGroup.getDescription())
                            .append(EMPTY_STRING).append(researchGroup.getIsPhysical()).append(EMPTY_STRING)
                            .append(researchGroup.getKeywords()));
            documents.add(sid);
        }

        try
        {
            SolrServer solrServer = solrServer();
            solrServer.add(documents);
            solrServer.commit();
            IndexingService idxSrv = new ResearchGroup().indexingService;
            for (ResearchGroup researchGroup : researchGroups)
            {
                idxSrv.indexResearchProjects(researchGroup.getResearchProjects());
            }
            LOGGER.info("Indexing RGs - DONE");
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

    public static void deleteIndex(ResearchGroup researchgroup)
    {
        SolrServer solrServer = solrServer();

        try
        {
            solrServer.deleteById("researchgroup_" + researchgroup.getId());
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
        String searchString = "ResearchGroup_solrsummary_t:" + queryString;
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

    public static void indexResearchGroup(ResearchGroup researchgroup)
    {
        List<ResearchGroup> researchgroups = new ArrayList<ResearchGroup>();
        researchgroups.add(researchgroup);
        indexResearchGroups(researchgroups);
    }

    public static final SolrServer solrServer()
    {
        SolrServer solrServer = new ResearchGroup().solrServer;
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
