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

import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.queryParser.QueryParser;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.org.intersect.sydma.webapp.domain.AccessLevel;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.permission.path.Path;
import au.org.intersect.sydma.webapp.permission.path.PathBuilder;

/**
 * Facade to query Solr index for data
 * 
 * @version $Rev: 29 $
 */
@Service
public class SolrIndexFacade
{
    private static final String ID = "id";

    private static final Logger LOGGER = LoggerFactory.getLogger(SolrIndexFacade.class);

    @Autowired
    private CommonsHttpSolrServer solrServer;
    
    @Autowired
    private PermissionService permissionService;
    
    public QueryResponse getSolrResponse(String query, int startIndex, int pageSize) throws SolrServerException 
    {
        String escapedQuery = QueryParser.escape(query);
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(escapedQuery);
        solrQuery.setFields(ID, "score");
        solrQuery.setStart(startIndex);
        solrQuery.setRows(pageSize);
        solrQuery.setSortField("score", SolrQuery.ORDER.desc);
        
        QueryResponse response = new QueryResponse();
        try
        {
            response = solrServer.query(solrQuery);
        }
        catch (SolrServerException e)
        {
            LOGGER.error("Error searching solr index: ", e);
            throw new SolrServerException(e);
        }
        
        return response;
    }

    public List<SearchDocument> findDocuments(QueryResponse response, User currentUser)
    {

        List<SearchDocument> documents = new LinkedList<SearchDocument>();
        SolrDocumentList docs = response.getResults();
        
        if (docs != null)
        {
            for (SolrDocument solrDocument : docs)
            {
                String documentId = solrDocument.getFieldValue(ID).toString();
                documents.add(getSearchDocument(documentId, currentUser));
            }
        }

        return documents;
    }

    private SearchDocument getSearchDocument(String documenttId, User currentUser)
    {
        String[] stringArray = documenttId.split("_");
        Long id = Long.parseLong(stringArray[1]);
        SearchDocument document = new SearchDocument();
        Path virtualPath = null;

        if ("researchdataset".equals(stringArray[0]))
        {
            ResearchDataset dataset = ResearchDataset.findResearchDataset(id);
            virtualPath = PathBuilder.datasetPath(dataset);
            document = setDatasetDocumentParams(document, dataset);
        }

        else if ("researchproject".equals(stringArray[0]))
        {
            ResearchProject project = ResearchProject.findResearchProject(id);
            virtualPath = PathBuilder.projectPath(project);
            document = setProjectDocumentParams(document, project);
        }

        else if ("researchgroup".equals(stringArray[0]))
        {
            ResearchGroup group = ResearchGroup.findResearchGroup(id);
            virtualPath = PathBuilder.groupPath(group);
            document = setGroupDocumentParams(document, group);
        }

        if (permissionService.getLevelForPath(virtualPath, currentUser).isAtLeast(AccessLevel.VIEWING_ACCESS))
        {
            buildDocumentUrl(document, virtualPath);                            
        }

        return document;
    }

    private SearchDocument setGroupDocumentParams(SearchDocument document, ResearchGroup group)
    {
        document.setDescription(group.getDescription());
        document.setSummary(group.getName());
        document.setSubjectCode(group.getSubjectCode().toString());
        if (group.getSubjectCode2() != null)
        {
            document.setSubjectCode2(group.getSubjectCode2().toString());
        }
        if (group.getSubjectCode3() != null)
        {
            document.setSubjectCode3(group.getSubjectCode3().toString());
        }
        return document;
    }
    
    private SearchDocument setProjectDocumentParams(SearchDocument document, ResearchProject project)
    {
        document.setDescription(project.getDescription());
        document.setSummary(project.getName());
        document.setSubjectCode(project.getSubjectCode().toString());
        if (project.getSubjectCode2() != null)
        {
            document.setSubjectCode2(project.getSubjectCode2().toString());
        }
        if (project.getSubjectCode3() != null)
        {
            document.setSubjectCode3(project.getSubjectCode3().toString());
        }
        return document;
    }
    
    private SearchDocument setDatasetDocumentParams(SearchDocument document, ResearchDataset dataset)
    {
        document.setDescription(dataset.getDescription());
        document.setSummary(dataset.getName());
        document.setSubjectCode(dataset.getSubjectCode().toString());
        if (dataset.getSubjectCode2() != null)
        {
            document.setSubjectCode2(dataset.getSubjectCode2().toString());
        }
        if (dataset.getSubjectCode3() != null)
        {
            document.setSubjectCode3(dataset.getSubjectCode3().toString());
        }
        if (dataset.isAdvertised())
        {
            document.setAdvertisedStatus("Public");
        }
        else
        {
            document.setAdvertisedStatus("Private");
        }
        return document;
    }

    private void buildDocumentUrl(SearchDocument document, Path virtualPath)
    {
        String[] ids = virtualPath.getPath().split("/");
        String[] urlElements = {"g", "g", "p", "d"};
        StringBuilder sb = new StringBuilder("../home/index#");
        
        
        for (int i = 1; i < ids.length; i++)
        {
            sb.append(urlElements[i] + "=" + ids[i] + "&");
        }
        
        sb.deleteCharAt(sb.length() - 1);
        document.setUrl(sb.toString());
    }

    

}
