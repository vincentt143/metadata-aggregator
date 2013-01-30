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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import au.org.intersect.sydma.webapp.domain.DBAccess;
import au.org.intersect.sydma.webapp.domain.DBUser;
import au.org.intersect.sydma.webapp.domain.ResearchDatabaseQuery;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchDatasetDB;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.exception.ResearchDatasetDBDoesNotExistException;
import au.org.intersect.sydma.webapp.exception.ResearchDatasetDBException;
import au.org.intersect.sydma.webapp.permission.PermissionType;
import au.org.intersect.sydma.webapp.service.ApplicationType;
import au.org.intersect.sydma.webapp.service.ApplicationTypeService;
import au.org.intersect.sydma.webapp.service.PermissionService;
import au.org.intersect.sydma.webapp.util.Breadcrumb;
import au.org.intersect.sydma.webapp.util.CSVHelper;
import au.org.intersect.sydma.webapp.util.DBConnectionHelper;
import au.org.intersect.sydma.webapp.util.ModelAndViewRedirectHelper;

/**
 * Controller to handle database queries
 */
// TODO CHECKSTYLE-OFF: ClassFanOutComplexity
@RequestMapping("/dbinstance/**")
@Controller
@DC2D
public class DBQueryController
{
    private static final Logger LOG = LoggerFactory.getLogger(DBInstanceController.class);

    private static final String QUERY_SCHEMA_VIEW = "dbinstance/query";
    private static final String SHOW_QUERY_VIEW = "dbinstance/show";
    private static final String EDIT_QUERY_VIEW = "dbinstance/editQuery";
    private static final String DELETE_QUERY_VIEW = "dbinstance/deleteQuery";

    private static final String QUERY_MODEL = "researchDatabaseQuery";
    private static final String ACCESS_DENIED = "accessDenied";

    private static final String DATASET_ID_PARAM = "datasetId";
    private static final String QUERY_ID_PARAM = "queryId";
    private static final String SUBMIT_PARAM = "_submit";

    private static final String REDIRECT_TO_VIEW_VIEW = "/dbinstance/view";

    private static Breadcrumb showQueryBreadcrumb;
    private static Breadcrumb createQueryBreadcrumb;
    private static Breadcrumb editQueryBreadcrumb;
    private static Breadcrumb deleteQueryBreadcrumb;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private DBConnectionHelper dbConnectionHelper;

    @Autowired
    private ApplicationTypeService applicationTypeService;

    @Autowired
    private ModelAndViewRedirectHelper modelAndViewRedirectHelper;

    static
    {
        showQueryBreadcrumb = new Breadcrumb("sections.database.queries.show.title");
        createQueryBreadcrumb = new Breadcrumb("sections.database.queries.title");
        editQueryBreadcrumb = new Breadcrumb("sections.database.queries.edit.title");
        deleteQueryBreadcrumb = new Breadcrumb("sections.database.queries.delete.title");
    }

    @RequestMapping(value = "/show/{id}", method = RequestMethod.GET)
    public String showQueryForDataset(final Model model, @PathVariable("id") final Long queryId,
            @RequestParam(DATASET_ID_PARAM) final Long datasetId, final Principal principal,
            final HttpServletRequest request)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return ACCESS_DENIED;
        }

        return permissionService.canDataset(PermissionType.VIEW_DATABASE_SQL, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {
                        ResearchDatabaseQuery databaseQuery = ResearchDatabaseQuery.findResearchDatabaseQuery(queryId);
                        List<Breadcrumb> showBreadcrumbs = createShowQueryBreadcrumbWithId(datasetId);
                        model.addAttribute(Breadcrumb.BREADCRUMBS, showBreadcrumbs);
                        model.addAttribute(DATASET_ID_PARAM, datasetId);
                        model.addAttribute(QUERY_MODEL, databaseQuery);
                        return SHOW_QUERY_VIEW;
                    }
                });
    }

    @RequestMapping(value = "/show/{id}", method = RequestMethod.POST)
    public ModelAndView showQueryForDataset(final Principal principal,
            @RequestParam(value = SUBMIT_PARAM, required = false) final String submit,
            @PathVariable("id") final Long queryId, @RequestParam(DATASET_ID_PARAM) final Long datasetId,
            final HttpServletRequest request)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return new ModelAndView(ACCESS_DENIED);
        }

        if ("Return".equals(submit))
        {
            return redirectToViewInstance(REDIRECT_TO_VIEW_VIEW, request, datasetId);
        }

        final Map<String, Object> model = new HashMap<String, Object>();
        String view = permissionService.canDataset(PermissionType.VIEW_DATABASE_SQL, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {
                        return SHOW_QUERY_VIEW;
                    }
                });
        ModelAndView mav = new ModelAndView(view);
        mav.addAllObjects(model);
        return mav;
    }

    /**
     * Running the stored SQL query against the dataset database and returns a download prompt
     * 
     * @param model
     * @param datasetId
     * @param principal
     * @return
     */
    @RequestMapping(value = "/run")
    public String runQueryForDataset(final Model model, @RequestParam("id") final Long queryId,
            @RequestParam(DATASET_ID_PARAM) final Long datasetId, final Principal principal,
            final HttpServletResponse response, final HttpServletRequest request)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return ACCESS_DENIED;
        }

        return permissionService.canDataset(PermissionType.VIEW_DATABASE_SQL, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {
                        ResearchDatasetDB datasetDB = dataset.getDatabaseInstance();
                        DBUser dbUser = DBUser.findDBUsersByDatabaseInstanceAndAccessLevel(datasetDB,
                                DBAccess.VIEW_ACCESS).getSingleResult();
                        Connection connection = dbConnectionHelper.obtainConnectionFor(datasetDB, dbUser);

                        ResearchDatabaseQuery query = ResearchDatabaseQuery.findResearchDatabaseQuery(queryId);
                        String filename = CSVHelper.formFilename(query.getName());
                        String q = query.getQuery();

                        try
                        {
                            PreparedStatement statement = connection.prepareCall(q);
                            ResultSet resultSet = statement.executeQuery();

                            response.setContentType("text/csv");
                            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
                            response.setHeader("Pragma", "no-cache");
                            response.setHeader("Cache-Control", "no-cache");
                            CSVHelper.writeCSVFile(response.getWriter(), resultSet);
                        }
                        catch (SQLException e)
                        {
                            LOG.info("SQL error: " + e);
                            model.addAttribute("sqlerror", true);
                            model.addAttribute("q", query.getName());
                            return "redirect:/dbinstance/view?datasetId=" + datasetId;
                        }
                        catch (IOException e)
                        {
                            throw new ResearchDatasetDBException("Failed to create csv file", e);

                        }
                        finally
                        {
                            try { connection.close(); } catch(Exception e) { /* ignore */ }
                        }
                        return null;
                    }
                });
    }

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public String viewQueryForDataset(final Model model, @RequestParam(DATASET_ID_PARAM) final Long datasetId,
            final Principal principal)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return ACCESS_DENIED;
        }

        return permissionService.canDataset(PermissionType.CREATE_DATABASE_SQL, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {
                        ResearchDataset researchDataset = ResearchDataset.findResearchDataset(datasetId);
                        ResearchDatabaseQuery databaseQuery = new ResearchDatabaseQuery();
                        List<Breadcrumb> queryBreadcrumb = createQueryBreadcrumbWithId(datasetId);
                        model.addAttribute(Breadcrumb.BREADCRUMBS, queryBreadcrumb);
                        model.addAttribute(DATASET_ID_PARAM, researchDataset.getId());
                        model.addAttribute(QUERY_MODEL, databaseQuery);
                        return QUERY_SCHEMA_VIEW;
                    }
                });
    }

    @RequestMapping(value = "/createQuery", method = RequestMethod.POST)
    public ModelAndView saveQueryForDataset(@Valid final ResearchDatabaseQuery databaseQuery,
            final BindingResult result, @RequestParam(value = "id") final Long datasetId, final Model model,
            final Principal principal, final HttpServletRequest request, 
            @RequestParam(value = SUBMIT_PARAM, required = false) final String submit)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return new ModelAndView(ACCESS_DENIED);
        }
        
        if ("Cancel".equals(submit))
        {
            return redirectToViewInstance(REDIRECT_TO_VIEW_VIEW, request, datasetId);
        }

        String view = permissionService.canDataset(PermissionType.CREATE_DATABASE_SQL, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {
                        if (result.hasErrors())
                        {
                            List<Breadcrumb> queryBreadcrumb = createQueryBreadcrumbWithId(datasetId);
                            model.addAttribute(Breadcrumb.BREADCRUMBS, queryBreadcrumb);
                            model.addAttribute(QUERY_MODEL, databaseQuery);
                            return QUERY_SCHEMA_VIEW;
                        }
                        databaseQuery.save(dataset.getDatabaseInstance());
                        return "view";
                    }
                });
        if ("view".equals(view))
        {
            return redirectToViewInstance(view, request, datasetId);
        }
        else
        {
            return new ModelAndView(view);
        }
    }

    @RequestMapping(value = "/editQuery/{id}", method = RequestMethod.GET)
    public String editQueryForDataset(final Model model, @PathVariable("id") final Long queryId,
            @RequestParam(DATASET_ID_PARAM) final Long datasetId, final Principal principal)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return ACCESS_DENIED;
        }

        return permissionService.canDataset(PermissionType.EDIT_DATABASE_SQL, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {
                        ResearchDatabaseQuery databaseQuery = ResearchDatabaseQuery.findResearchDatabaseQuery(queryId);
                        List<Breadcrumb> queryBreadcrumb = editQueryBreadcrumbWithId(datasetId);
                        model.addAttribute(Breadcrumb.BREADCRUMBS, queryBreadcrumb);
                        model.addAttribute(DATASET_ID_PARAM, datasetId);
                        model.addAttribute(QUERY_MODEL, databaseQuery);
                        return EDIT_QUERY_VIEW;
                    }
                });
    }

    @RequestMapping(value = "/editQuery", method = RequestMethod.PUT)
    public ModelAndView editQueryForDataset(@Valid final ResearchDatabaseQuery databaseQuery,
            final BindingResult result, final Model model, final Principal principal, final HttpServletRequest request,
            @RequestParam(value = SUBMIT_PARAM, required = false) final String submit,
            @RequestParam(DATASET_ID_PARAM) final Long datasetId)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return new ModelAndView(ACCESS_DENIED);
        }

        if (!"Edit".equals(submit))
        {
            return redirectToViewInstance(REDIRECT_TO_VIEW_VIEW, request, datasetId);
        }

        String view = permissionService.canDataset(PermissionType.EDIT_DATABASE_SQL, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {
                        if (result.hasErrors())
                        {
                            List<Breadcrumb> queryBreadcrumb = editQueryBreadcrumbWithId(dataset.getId());
                            model.addAttribute(Breadcrumb.BREADCRUMBS, queryBreadcrumb);
                            model.addAttribute(DATASET_ID_PARAM, datasetId);
                            model.addAttribute(QUERY_MODEL, databaseQuery);
                            return EDIT_QUERY_VIEW;
                        }
                        ResearchDatabaseQuery dBQuery = ResearchDatabaseQuery.findResearchDatabaseQuery(databaseQuery
                                .getId());
                        databaseQuery.edit(dBQuery.getResearchDatasetDB());
                        return REDIRECT_TO_VIEW_VIEW;
                    }
                });
        if (REDIRECT_TO_VIEW_VIEW.equals(view))
        {
            return redirectToViewInstance(view, request, datasetId);
        }
        else
        {
            return new ModelAndView(view);
        }
    }

    @RequestMapping(value = "/deleteQuery/{id}", method = RequestMethod.GET)
    public String confirmDeleteQuery(final Model model, @PathVariable("id") final Long queryId,
            @RequestParam(DATASET_ID_PARAM) final Long datasetId, final Principal principal,
            final HttpServletRequest request)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return ACCESS_DENIED;
        }

        return permissionService.canDataset(PermissionType.CREATE_DATABASE_SQL, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {
                        ResearchDatabaseQuery query = ResearchDatabaseQuery.findResearchDatabaseQuery(queryId);
                        if (query == null)
                        {
                            throw new ResearchDatasetDBDoesNotExistException(
                                    "Database Query does not exist for dataset with id:" + queryId);
                        }
                        List<Breadcrumb> queryBreadcrumb = deleteQueryBreadcrumbWithId(dataset.getId());
                        model.addAttribute(Breadcrumb.BREADCRUMBS, queryBreadcrumb);
                        model.addAttribute(DATASET_ID_PARAM, datasetId);
                        model.addAttribute(QUERY_MODEL, query);
                        return DELETE_QUERY_VIEW;
                    }
                });
    }

    @RequestMapping(value = "/deleteQuery", method = RequestMethod.POST)
    public ModelAndView deleteQuery(final Principal principal, final ResearchDatabaseQuery databaseQuery,
            @RequestParam(value = SUBMIT_PARAM, required = false) final String submit,
            @RequestParam(QUERY_ID_PARAM) final Long queryId, @RequestParam(DATASET_ID_PARAM) final Long datasetId,
            final HttpServletRequest request)
    {

        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return new ModelAndView(ACCESS_DENIED);
        }

        if (!"Delete".equals(submit))
        {
            return redirectToViewInstance(REDIRECT_TO_VIEW_VIEW, request, datasetId);
        }

        String view = permissionService.canDataset(PermissionType.CREATE_DATABASE_SQL, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {
                        ResearchDatabaseQuery query = ResearchDatabaseQuery.findResearchDatabaseQuery(queryId);
                        if (query == null)
                        {
                            throw new ResearchDatasetDBDoesNotExistException(
                                    "Database Query does not exist for dataset with id:" + datasetId);
                        }
                        query.remove();
                        return REDIRECT_TO_VIEW_VIEW;
                    }
                });

        if (REDIRECT_TO_VIEW_VIEW.equals(view))
        {
            return redirectToViewInstance(view, request, datasetId);
        }
        else
        {
            return new ModelAndView(view);
        }
    }

    private ModelAndView redirectToViewInstance(String view, HttpServletRequest request, Long datasetId)
    {
        ModelAndView mav = modelAndViewRedirectHelper.generateRedirectView(view, request);
        mav.addObject(DATASET_ID_PARAM, datasetId);
        return mav;
    }

    private List<Breadcrumb> deleteQueryBreadcrumbWithId(Long datasetId)
    {
        List<Breadcrumb> crumbsUpToView = createBreadcrumbUpToViewInstance(datasetId);
        crumbsUpToView.add(deleteQueryBreadcrumb);
        return crumbsUpToView;
    }

    private List<Breadcrumb> editQueryBreadcrumbWithId(Long datasetId)
    {
        List<Breadcrumb> crumbsUpToView = createBreadcrumbUpToViewInstance(datasetId);
        crumbsUpToView.add(editQueryBreadcrumb);
        return crumbsUpToView;
    }

    private List<Breadcrumb> createQueryBreadcrumbWithId(Long datasetId)
    {
        List<Breadcrumb> crumbsUpToView = createBreadcrumbUpToViewInstance(datasetId);
        crumbsUpToView.add(createQueryBreadcrumb);
        return crumbsUpToView;
    }

    private List<Breadcrumb> createShowQueryBreadcrumbWithId(Long datasetId)
    {
        List<Breadcrumb> crumbsUpToView = createBreadcrumbUpToViewInstance(datasetId);
        crumbsUpToView.add(showQueryBreadcrumb);
        return crumbsUpToView;
    }

    private List<Breadcrumb> createBreadcrumbUpToViewInstance(Long datasetId)
    {
        List<Breadcrumb> crumbs = new ArrayList<Breadcrumb>();
        crumbs.add(Breadcrumb.getHome());
        crumbs.add(new Breadcrumb("sections.dbinstance.view.title", "/dbinstance/view?datasetId=" + datasetId));
        return crumbs;
    }
}
