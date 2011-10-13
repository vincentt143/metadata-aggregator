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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;

import au.org.intersect.sydma.webapp.permission.project.ProjectPermissionQuery;
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

    @Size(max = 400)
    @URL(protocol = "")
    private String url;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @NotNull
    private ResearchGroup researchGroup;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "researchProject")
    private Set<ResearchDataset> researchDatasets = new HashSet<ResearchDataset>();

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
        return "www.sydney.edu.au-metadata-aggregator-research-project-" + this.getId();
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
        for (ResearchDataset dataset : this.researchDatasets)
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

    public boolean isDuplicate()
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

}
