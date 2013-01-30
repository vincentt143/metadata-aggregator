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
import javax.persistence.EntityManager;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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

import au.org.intersect.sydma.webapp.permission.project.ProjectPermissionQuery;
import au.org.intersect.sydma.webapp.service.IndexingService;
import au.org.intersect.sydma.webapp.util.RifCsWriter;

/**
 * Research Project Model.
 */
@RooJavaBean
@RooToString
@RooJson
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name", "research_group"}))
@RooEntity(finders = {"findResearchProjectsByNameEquals", "findResearchProjectsByNameEqualsAndResearchGroup",
        "findResearchProjectsByResearchGroup"}, persistenceUnit = "sydmaPU")
// TODO CHECKSTYLE-OFF: MagicNumber
public class ResearchProject
{
    private static final String EMPTY_STRING = " ";

    private static final Logger LOGGER = LoggerFactory.getLogger(ResearchProject.class);

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

    @Size(max = 1000, message = "Must not exceed 1000 characters in length")
    @NotNull
    @NotEmpty(message = "Description is a required field")
    @Pattern(regexp = "\\s*\\S+[\\s\\S]*", message = "Description is a required field")
    private String description;

    @Size(max = 400, message = "Must not exceed 400 characters in length")
    @URL(protocol = "", message = "Must be a valid URL")
    private String url;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @NotNull
    private ResearchGroup researchGroup;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "researchProject")
    private Set<ResearchDataset> researchDatasets = new HashSet<ResearchDataset>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @Valid
    private List<Publication> publications = new ArrayList<Publication>();

    @ManyToMany
    private List<Vocabulary> keywords = new ArrayList<Vocabulary>();

    public void setResearchGroup(ResearchGroup researchGroup)
    {
        this.researchGroup = researchGroup;
    }

    /**
     * Guard method to ensure both side of the relationship is updated
     */
    public void associateWithResearchDataset(ResearchDataset researchDataset)
    {
        if (researchDatasets == null)
        {
            researchDatasets = new HashSet<ResearchDataset>();
        }
        researchDatasets.add(researchDataset);
        researchDataset.setResearchProject(this);
    }

    public String getKeyForRifCs()
    {
        if ("AGR_ENV".equals(applicationType))
        {
            return "www.sydney.edu.au-agriculture-environment-research-project-" + this.getId();
        }
        else
        {
            return "www.sydney.edu.au-metadata-aggregator-research-project-" + this.getId();
        }
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

    public List<ResearchDataset> getAdvertisedResearchDatasets()
    {
        List<ResearchDataset> advertisedDatasets = new ArrayList<ResearchDataset>();
        
        for (ResearchDataset dataset : this.getResearchDatasets())
        {
            if (dataset.isAdvertised())
            {
                advertisedDatasets.add(dataset);
            }
        }

        // sort them so we get predictable ordering in the rifcs
        Collections.sort(advertisedDatasets, new Comparator<ResearchDataset>()
        {
            @Override
            public int compare(ResearchDataset d1, ResearchDataset d2)
            {
                return d1.getId().compareTo(d2.getId());
            }
        });
        return advertisedDatasets;
    }

    public boolean hasAdvertisedDatasets()
    {
        return !getAdvertisedResearchDatasets().isEmpty();
    }

    public static TypedQuery<ResearchProject> findResearchProjectsByNameEqualsAndResearchGroupAndIdNotEquals(Long id,
            String name, ResearchGroup researchGroup)
    {
        if (name == null || name.length() == 0)
        {
            throw new IllegalArgumentException("The name argument is required");
        }
        if (researchGroup == null)
        {
            throw new IllegalArgumentException("The researchGroup argument is required");
        }
        EntityManager em = ResearchDataset.entityManager();
        String query;
        query = "SELECT o FROM ResearchProject AS o WHERE o.name = :name  AND o.researchGroup = :researchGroup "
                + "AND o.id != :id";
        TypedQuery<ResearchProject> q = em.createQuery(query, ResearchProject.class);
        q.setParameter("id", id);
        q.setParameter("name", name);
        q.setParameter("researchGroup", researchGroup);
        return q;
    }

    public static List<ResearchProject> findAllResearchProjects()
    {
        return entityManager().createQuery("SELECT o FROM ResearchProject o ORDER BY o.name", ResearchProject.class)
                .getResultList();
    }

    public static TypedQuery<ResearchProject> findResearchProjectsByResearchGroup(ResearchGroup researchGroup)
    {
        if (researchGroup == null)
        {
            throw new IllegalArgumentException("The researchGroup argument is required");
        }
        EntityManager em = ResearchProject.entityManager();
        TypedQuery<ResearchProject> q = em.createQuery(
                "SELECT o FROM ResearchProject AS o WHERE o.researchGroup = :researchGroup ORDER BY o.name",
                ResearchProject.class);
        q.setParameter("researchGroup", researchGroup);
        return q;
    }

    public boolean isDuplicateWithin(ResearchGroup researchGroup)
    {
        if (getId() == null)
        {
            return !findResearchProjectsByNameEqualsAndResearchGroup(name, researchGroup).getResultList().isEmpty();
        }
        else
        {
            return !findResearchProjectsByNameEqualsAndResearchGroupAndIdNotEquals(getId(), name, researchGroup)
                    .getResultList().isEmpty();
        }  
    }

    public void updateRifCsIfNeeded(RifCsWriter rifCsWriter)
    {
        if (hasAdvertisedDatasets())
        {
            rifCsWriter.writeProjectRifCs(this);
        }
    }

    public static List<ResearchProject> findProjectWithPermission(ProjectPermissionQuery permissionQuery)
    {
        TypedQuery<ResearchProject> query = permissionQuery.createQuery(ResearchProject.entityManager());
        return query.getResultList();
    }
    

    public static void reindexResearchProjectForKeyword(ResearchProject project, Vocabulary term)
    {
        new ResearchProject().indexingService.reindexResearchProjectKeyword(project, term);
    }
    
    @Async
    public static void reindexResearchProjectForKeywordAsync(ResearchProject project, Vocabulary term)
    {
        if (project.getKeywords().contains(term))
        {
            Collection<ResearchProject> rps = new ArrayList<ResearchProject>();
            rps.add(project);
            indexResearchProjectsAsync(rps);
        }
        else
        {
            for (ResearchDataset dataset : project.getResearchDatasets())
            {
                ResearchDataset.reindexResearchDatasetForKeyword(dataset, term);
            }            
        }
    }
    
    @Async
    public static void indexResearchProjects(Collection<ResearchProject> researchProjects) 
    {
        sleep(1L);
        new ResearchProject().indexingService.indexResearchProjects(researchProjects);
    }
    
    public static void indexResearchProjectsAsync(Collection<ResearchProject> researchProjects)
    {
        float boostValue = 3;
        List<SolrInputDocument> documents = new ArrayList<SolrInputDocument>();
        for (ResearchProject researchProject : researchProjects)
        {
            LOGGER.info("Indexing RP: " + researchProject.getId());
            SolrInputDocument sid = new SolrInputDocument();
            sid.addField("id", "researchproject_" + researchProject.getId());
            sid.addField("researchproject.id_l", researchProject.getId());
            sid.addField("researchproject.name_s", researchProject.getName());
            sid.addField("researchproject.subjectcode_t", researchProject.getSubjectCode());
            sid.addField("researchproject.subjectcode2_t", researchProject.getSubjectCode2());
            sid.addField("researchproject.subjectcode3_t", researchProject.getSubjectCode3());
            sid.addField("researchproject.description_s", researchProject.getDescription());
            sid.addField("researchproject.url_s", researchProject.getUrl());
            sid.addField("researchproject.researchgroup_t", researchProject.getResearchGroup());
            sid.addField("researchproject.keywords_t", Vocabulary.toSolrText(researchProject.getKeywords()));
            // Add summary field to allow searching documents for objects of this type
            sid.addField(
                    "researchproject_solrsummary_t",
                    new StringBuilder().append(researchProject.getId()).append(EMPTY_STRING)
                            .append(researchProject.getName()).append(EMPTY_STRING)
                            .append(researchProject.getSubjectCode()).append(EMPTY_STRING)
                            .append(researchProject.getSubjectCode2()).append(EMPTY_STRING)
                            .append(researchProject.getSubjectCode3()).append(EMPTY_STRING)
                            .append(researchProject.getDescription()).append(EMPTY_STRING)
                            .append(researchProject.getUrl()).append(EMPTY_STRING)
                            .append(researchProject.getResearchGroup()).append(EMPTY_STRING)
                            .append(researchProject.getKeywords()));
            sid.setDocumentBoost(boostValue);
            documents.add(sid);
        }

        SolrServer solrServer = solrServer();

        try
        {
            solrServer.add(documents);
            solrServer.commit();
            IndexingService idxSrv = new ResearchProject().indexingService;
            for (ResearchProject researchProject : researchProjects)
            {
                idxSrv.indexResearchDatasets(researchProject.getResearchDatasets());
            }
            LOGGER.info("Indexing RPs - DONE");
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

    public static void deleteIndex(ResearchProject researchproject)
    {
        SolrServer solrServer = solrServer();

        try
        {
            solrServer.deleteById("researchproject_" + researchproject.getId());
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
        String searchString = "ResearchProject_solrsummary_t:" + queryString;
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

    public static void indexResearchProject(ResearchProject researchproject)
    {
        List<ResearchProject> researchprojects = new ArrayList<ResearchProject>();
        researchprojects.add(researchproject);
        indexResearchProjects(researchprojects);
    }

    public static final SolrServer solrServer()
    {
        SolrServer solrServer = new ResearchProject().solrServer;
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
