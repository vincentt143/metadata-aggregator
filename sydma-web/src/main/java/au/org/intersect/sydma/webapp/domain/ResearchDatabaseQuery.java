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

import javax.persistence.CascadeType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;

/**
 * Entity storing queries for dataset database query
 * 
 * @version $Rev: 29 $
 */
@RooJavaBean
@RooToString(excludeFields = {"researchDatasetDB"})
@RooJson
@RooEntity(persistenceUnit = "sydmaPU", finders = {"findResearchDatabaseQuerysByResearchDatasetDB",
        "findResearchDatabaseQuerysByNameEquals"})
// TODO CHECKSTYLE-OFF: MagicNumberCheck
public class ResearchDatabaseQuery
{
    //Validation
    @NotNull
    @Size(max = 100)
    @NotEmpty(message = "Name is a required field")
    @Pattern(regexp = "\\s*\\S+[\\s\\S]*", message = "Name is a required field")
    private String name;

    @NotEmpty(message = "SQL Query is a required field")
    @Pattern(regexp = "\\s*\\S+[\\s\\S]*", message = "SQL Query is a required field")
    @Size(max = 1000)
    private String query;

    @NotEmpty(message = "Description is a required field")
    @Pattern(regexp = "\\s*\\S+[\\s\\S]*", message = "Description is a required field")
    @Size(max = 1000)
    private String description;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private ResearchDatasetDB researchDatasetDB;

    public void save(ResearchDatasetDB dataset)
    {
        this.researchDatasetDB = dataset;
        trim();
        this.persist();
    }
    
    public void edit(ResearchDatasetDB datasetDB)
    {
        this.researchDatasetDB = datasetDB;
        trim();
        this.merge();
    }
    
    private void trim()
    {
        this.name = name.trim();
        this.query = query.trim();
        this.description = description.trim();       
    }
}
