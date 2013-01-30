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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import au.org.intersect.sydma.webapp.domain.DBBackup;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchDatasetDB;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.permission.PermissionType;
import au.org.intersect.sydma.webapp.service.ApplicationType;
import au.org.intersect.sydma.webapp.service.ApplicationTypeService;
import au.org.intersect.sydma.webapp.service.PermissionService;
import au.org.intersect.sydma.webapp.util.Breadcrumb;
import au.org.intersect.sydma.webapp.util.DBBackupHelper;
import au.org.intersect.sydma.webapp.util.ModelAndViewRedirectHelper;

/**
 * Controller to handle database backups
 */
@RequestMapping("/dbinstance/**")
@Controller
@DC2D
public class DBBackupController
{
    private static final Logger LOG = LoggerFactory.getLogger(DBBackupController.class);

    private static final String ACCESS_DENIED = "accessDenied";
    private static final String SUBMIT_PARAM = "_submit";

    private static final String DATASET_ID_PARAM = "datasetId";
    private static final String BACKUP_ID_PARAM = "backupId";

    private static final String DB_BACKUP_MODEL = "dBBackup";
    private static final String DATASET_MODEL = "dataset";
    private static final String RESEARCH_DATASET_DB_MODEL = "researchDatasetDB";

    private static final String CAN_BACKUP = "canBackup";
    private static final String CHECKED_FOR_BACKUP = "checkedForBackup";

    private static final String MANAGE_DATABASE_BACKUP_VIEW = "dbinstance/manageBackup";
    private static final String CREATE_BACKUP_VIEW = "dbinstance/createBackup";
    private static final String REDIRECT_CREATE_BACKUP_VIEW = "/dbinstance/createBackup";
    private static final String CONFIRM_RESTORE_VIEW = "dbinstance/confirmRestore";
    private static final String REDIRECT_TO_MANAGE_VIEW = "/dbinstance/manageBackup";

    private static List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> createBackupBreadcrumbs = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> confirmRestoreBreadcrumbs = new ArrayList<Breadcrumb>();

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private ApplicationTypeService applicationTypeService;

    @Autowired
    private ModelAndViewRedirectHelper modelAndViewRedirectHelper;

    @Autowired
    private DBBackupHelper dbBackupHelper;

    static
    {
        breadcrumbs.add(Breadcrumb.getHome());
        breadcrumbs.add(new Breadcrumb("sections.dbinstance.backup.title"));

        createBackupBreadcrumbs.add(Breadcrumb.getHome());
        createBackupBreadcrumbs.add(new Breadcrumb("sections.dbinstance.create.backup"));

        confirmRestoreBreadcrumbs.add(Breadcrumb.getHome());
        confirmRestoreBreadcrumbs.add(new Breadcrumb("sections.dbinstance.confirm.restore"));
    }

    @RequestMapping(value = "/manageBackup", method = RequestMethod.GET)
    public String viewBackup(final Model model, final Principal principal,
            @RequestParam(DATASET_ID_PARAM) final Long datasetId, final HttpServletRequest request)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return ACCESS_DENIED;
        }

        return permissionService.canDataset(PermissionType.BACKUP_DATABASE_INSTANCE, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {
                        List<DBBackup> dbBackupList = DBBackup.findDBBackupsByResearchDataset(dataset).getResultList();

                        model.addAttribute(CAN_BACKUP, permissionService.canAction(
                                PermissionType.BACKUP_DATABASE_INSTANCE, datasetId, principal));
                        model.addAttribute(DATASET_ID_PARAM, datasetId);
                        model.addAttribute(DATASET_MODEL, dataset);
                        model.addAttribute(Breadcrumb.BREADCRUMBS, breadcrumbs);
                        model.addAttribute(DB_BACKUP_MODEL, dbBackupList);
                        return MANAGE_DATABASE_BACKUP_VIEW;
                    }
                });
    }

    @RequestMapping(value = "/manageBackup", method = RequestMethod.POST)
    public ModelAndView viewBackup(final Principal principal, @RequestParam(DATASET_ID_PARAM) final Long datasetId,
            final HttpServletRequest request)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return new ModelAndView(ACCESS_DENIED);
        }

        String view = permissionService.canDataset(PermissionType.BACKUP_DATABASE_INSTANCE, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {

                        return REDIRECT_CREATE_BACKUP_VIEW;
                    }
                });
        if (view.equals(REDIRECT_CREATE_BACKUP_VIEW))
        {
            return redirectToViewInstance(view, request, datasetId, "datasetId");
        }
        else
        {
            return new ModelAndView(view);
        }
    }

    @RequestMapping(value = "/createBackup", method = RequestMethod.GET)
    public String createBackup(final Model model, final Principal principal,
            @RequestParam(DATASET_ID_PARAM) final Long datasetId, final HttpServletRequest request)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return ACCESS_DENIED;
        }

        return permissionService.canDataset(PermissionType.BACKUP_DATABASE_INSTANCE, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {
                        DBBackup dbBackup = new DBBackup();
                        model.addAttribute(DB_BACKUP_MODEL, dbBackup);
                        model.addAttribute(DATASET_ID_PARAM, datasetId);
                        model.addAttribute(Breadcrumb.BREADCRUMBS, createBackupBreadcrumbs);
                        return CREATE_BACKUP_VIEW;
                    }
                });
    }

    @RequestMapping(value = "/createBackup", method = RequestMethod.POST)
    public ModelAndView createBackup(@Valid @ModelAttribute("dBBackup") final DBBackup databaseBackup,
            final BindingResult result, @RequestParam(value = DATASET_ID_PARAM) final Long datasetId,
            final Model model, final Principal principal, final HttpServletRequest request,
            @RequestParam(value = SUBMIT_PARAM, required = false) final String submit)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return new ModelAndView(ACCESS_DENIED);
        }

        if (!"Backup".equals(submit))
        {
            return redirectToViewInstance(REDIRECT_TO_MANAGE_VIEW, request, datasetId, DATASET_ID_PARAM);
        }

        String view = permissionService.canDataset(PermissionType.BACKUP_DATABASE_INSTANCE, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {
                        if (result.hasFieldErrors("description"))
                        {
                            model.addAttribute(Breadcrumb.BREADCRUMBS, createBackupBreadcrumbs);
                            model.addAttribute(DATASET_ID_PARAM, datasetId);
                            model.addAttribute(DB_BACKUP_MODEL, databaseBackup);
                            return CREATE_BACKUP_VIEW;
                        }
                        String description = databaseBackup.getDescription();
                        dbBackupHelper.createBackup(dataset, description, principal);
                        return REDIRECT_TO_MANAGE_VIEW;
                    }
                });
        if (view.equals(REDIRECT_TO_MANAGE_VIEW))
        {
            return redirectToViewInstance(view, request, datasetId, DATASET_ID_PARAM);
        }
        else
        {
            return new ModelAndView(view);
        }
    }

    @RequestMapping(value = "/confirmRestore", method = RequestMethod.GET)
    public String confirmRestore(final Model model, final Principal principal,
            @RequestParam(BACKUP_ID_PARAM) final Long backupId, final HttpServletRequest request)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return ACCESS_DENIED;
        }

        Long datasetId = DBBackup.findDBBackup(backupId).getResearchDataset().getId();

        return permissionService.canDataset(PermissionType.BACKUP_DATABASE_INSTANCE, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {

                        ResearchDatasetDB datasetDB = ResearchDatasetDB
                                .findResearchDatasetDBsByResearchDataset(dataset).getSingleResult();
                        datasetDB.setState(null);
                        model.addAttribute(BACKUP_ID_PARAM, backupId);
                        model.addAttribute(RESEARCH_DATASET_DB_MODEL, datasetDB);
                        model.addAttribute(CHECKED_FOR_BACKUP, false);
                        model.addAttribute(Breadcrumb.BREADCRUMBS, confirmRestoreBreadcrumbs);

                        return CONFIRM_RESTORE_VIEW;
                    }
                });
    }

    // TODO CHECKSTYLE-OFF: ParameterNumberCheck
    @RequestMapping(value = "/confirmRestore", method = RequestMethod.POST)
    public ModelAndView confirmRestore(@Valid final ResearchDatasetDB researchDatasetDB, final BindingResult result,
            final Model model, final Principal principal, final HttpServletRequest request,
            @RequestParam(value = SUBMIT_PARAM, required = false) final String submit,
            @RequestParam(value = BACKUP_ID_PARAM) final Long backupId,
            @RequestParam(value = "confirmBackup", required = false) final String confirmBackup)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return new ModelAndView(ACCESS_DENIED);
        }

        final ResearchDatasetDB datasetDB = ResearchDatasetDB.findResearchDatasetDB(researchDatasetDB.getId());
        Long datasetId = datasetDB.getResearchDataset().getId();

        if (!"Restore".equals(submit))
        {
            return redirectToViewInstance(REDIRECT_TO_MANAGE_VIEW, request, datasetId, DATASET_ID_PARAM);
        }

        String view = permissionService.canDataset(PermissionType.BACKUP_DATABASE_INSTANCE, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {
                        if (result.hasFieldErrors("state"))
                        {
                            model.addAttribute(RESEARCH_DATASET_DB_MODEL, researchDatasetDB);
                            model.addAttribute(BACKUP_ID_PARAM, backupId);
                            model.addAttribute(Breadcrumb.BREADCRUMBS, confirmRestoreBreadcrumbs);
                            model.addAttribute(CHECKED_FOR_BACKUP, confirmBackup != null ? true : false);
                            return CONFIRM_RESTORE_VIEW;
                        }

                        if (confirmBackup != null)
                        {
                            String description = "This backup was performed before restoring this database. Reason: "
                                    + researchDatasetDB.getState();
                            dbBackupHelper.createBackup(dataset, description, principal);
                        }

                        String description = researchDatasetDB.getState();
                        dbBackupHelper.restoreFromBackup(dataset, backupId, description, principal);
                        return REDIRECT_TO_MANAGE_VIEW;
                    }
                });
        if (view.equals(REDIRECT_TO_MANAGE_VIEW))
        {
            return redirectToViewInstance(view, request, datasetId, DATASET_ID_PARAM);
        }
        else
        {
            return new ModelAndView(view);
        }
    }

    private ModelAndView redirectToViewInstance(String view, HttpServletRequest request, Long id, String param)
    {
        ModelAndView mav = modelAndViewRedirectHelper.generateRedirectView(view, request);
        mav.addObject(param, id);
        return mav;
    }
}
