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
package au.org.intersect.sydma.webapp.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.service.SearchDocument;
import au.org.intersect.sydma.webapp.service.SolrIndexFacade;
import au.org.intersect.sydma.webapp.util.Breadcrumb;

/**
 * 
 * 
 * @version $Rev: 29 $
 */
@Controller
@RequestMapping("/search/**")
public class SearchController
{
    private static final int PAGINATION_SHIFT_POINT = 4;
    private static final int MAX_PAGINATION_WIDTH = 9;
    private static final int PAGE_SIZE = 10;
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchController.class);
    private static List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();

    @Autowired
    private SolrIndexFacade solrIndexFacade;

    static
    {
        breadcrumbs.add(Breadcrumb.getHome());
        breadcrumbs.add(new Breadcrumb("sections.search.breadcrumb"));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/index")
    public String search(Model model, Principal principal)
    {
        model.addAttribute("breadcrumbs", breadcrumbs);
        return "search/index";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/results")
    public ModelAndView searchResults(@RequestParam("query") String query, @RequestParam("page") int pageNumber,
            Principal principal) throws SolrServerException
    {        
        ModelAndView mav = new ModelAndView("search/results");
        mav.addObject("breadcrumbs", breadcrumbs);

        if ("".equals(query.trim()))
        {
            mav.addObject("error", "Please enter a valid search term.");
        }
        else
        {
            int startIndex = (PAGE_SIZE * pageNumber) - PAGE_SIZE; 
            QueryResponse response = solrIndexFacade.getSolrResponse(query.trim(), startIndex, PAGE_SIZE);   

            if (response.getResults().getNumFound() == 0)
            {
                mav.addObject("error", "No results found. Please try a different search term.");
            }
            
            User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
            List<SearchDocument> documents = solrIndexFacade.findDocuments(response, user);
    
            mav.addObject("userEntry", query.trim());
            mav.addObject("results", documents);
            mav.addObject("totalNumOfResults", response.getResults().getNumFound());
            mav.addObject("startResult", documents.size() == 0 ? 0 : startIndex + 1);
            mav.addObject("endResult", documents.size() == 0 ? 0 : startIndex + documents.size());
            mav.addObject("currentPage", pageNumber);
    
            int totalPages = (int) Math.ceil((double) response.getResults().getNumFound() / (double) PAGE_SIZE);
            totalPages = totalPages < 1 ? 1 : totalPages;
            mav.addObject("maxPages", totalPages);
    
            int pagerNum = PAGINATION_SHIFT_POINT;
    
            int indexStart = (pageNumber - pagerNum) <= 0 ? 1 : (pageNumber - pagerNum);
            indexStart = (indexStart + Math.min(totalPages - 1, MAX_PAGINATION_WIDTH)) <= totalPages ? indexStart
                    : (totalPages - Math.min(totalPages - 1, MAX_PAGINATION_WIDTH));
            int indexFinish = (pageNumber + pagerNum) >= totalPages ? totalPages : (indexStart + Math.min(totalPages,
                    MAX_PAGINATION_WIDTH));
    
            mav.addObject("index_start", indexStart);
            mav.addObject("index_finish", indexFinish);
    
            LOGGER.info("number of results found for search '" + query + "' : " + response.getResults().getNumFound());
        }
        return mav;
    }
}
