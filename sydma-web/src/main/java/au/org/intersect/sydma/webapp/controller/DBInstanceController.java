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

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.EntityNotFoundException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import au.org.intersect.sydma.webapp.controller.propertyeditor.DBSchemaPropertyEditor;
import au.org.intersect.sydma.webapp.controller.propertyeditor.TrimString;
import au.org.intersect.sydma.webapp.domain.DBAccess;
import au.org.intersect.sydma.webapp.domain.DBBackup;
import au.org.intersect.sydma.webapp.domain.DBSchema;
import au.org.intersect.sydma.webapp.domain.DBUser;
import au.org.intersect.sydma.webapp.domain.ResearchDatabaseQuery;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchDatasetDB;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.dto.DBInstanceDto;
import au.org.intersect.sydma.webapp.exception.NoneUniqueNameException;
import au.org.intersect.sydma.webapp.exception.ResearchDatasetDBAlreadyExistsException;
import au.org.intersect.sydma.webapp.exception.ResearchDatasetDBDoesNotExistException;
import au.org.intersect.sydma.webapp.exception.ResearchDatasetDBException;
import au.org.intersect.sydma.webapp.permission.PermissionType;
import au.org.intersect.sydma.webapp.service.ApplicationType;
import au.org.intersect.sydma.webapp.service.ApplicationTypeService;
import au.org.intersect.sydma.webapp.service.NewMailService;
import au.org.intersect.sydma.webapp.service.PermissionService;
import au.org.intersect.sydma.webapp.service.ResearchDatasetDBService;
import au.org.intersect.sydma.webapp.util.Breadcrumb;
import au.org.intersect.sydma.webapp.util.DBBackupHelper;
import au.org.intersect.sydma.webapp.util.DBPasswordHelper;
import au.org.intersect.sydma.webapp.util.ModelAndViewRedirectHelper;
import au.org.intersect.sydma.webapp.util.UrlHelper;

/**
 * Controller to handle dataset dbSchemas
 */
// TODO CHECKSTYLE-OFF: ClassFanOutComplexity
// TODO CHECKSTYLE-OFF: MultipleStringLiteralsCheck
@RequestMapping("/dbinstance/**")
@Controller
@DC2D
public class DBInstanceController
{
    private static final String ACCESS_DENIED = "accessDenied";

    private static final int NUMBER_OF_INDENTATION_SPACES = 8;

    private static final String SCHEMA_DTO = "schema";

    private static final Logger LOG = LoggerFactory.getLogger(DBInstanceController.class);

    private static final String CREATE_INSTANCE_VIEW = "dbinstance/create";
    private static final String VIEW_INSTANCE_VIEW = "dbinstance/view";
    private static final String EDIT_INSTANCE_VIEW = "dbinstance/edit";
    private static final String REVERSE_INSTANCE_VIEW = "dbinstance/reverse";
    private static final String DELETE_INSTANCE_VIEW = "dbinstance/delete";
    private static final String CHANGE_PASSWORD_VIEW = "dbinstance/change";

    private static final String SHOW_DELETE_ATTR = "showDelete";
    private static final String SHOW_EDIT_ATTR = "showEdit";
    private static final String SHOW_REVERSE_ATTR = "showReverse";

    private static final String SCHEMAS_ATTR = "schemas";
    private static final String DATASET_ATTR = "dataset";
    private static final String DATASET_DB_ATTR = "datasetDB";
    private static final String DB_USER_ATTR = "dbUser";
    private static final String DB_INSTANCE_DTO = "dbInstanceDto";
    private static final String CAN_EDIT_ATTR = "canEdit";
    private static final String CAN_DELETE_ATTR = "canDelete";
    private static final String CAN_CHANGE_PASSWORD = "canChangePassword";

    private static final String DATASET_ID_PARAM = "datasetId";
    private static final String SUBMIT_PARAM = "_submit";

    private static final String REDIRECT_TO_VIEW_VIEW = "/dbinstance/view";
    private static final String REDIRECT_TO_HOME_VIEW = "redirect:/";

    private static List<Breadcrumb> createBreadcrumbs = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> viewBreadcrumbs = new ArrayList<Breadcrumb>();
    private static Breadcrumb editBreadcrumb;
    private static Breadcrumb reverseBreadcrumb;

    private static Breadcrumb deleteBreadcrumb;

    @Autowired
    private ResearchDatasetDBService dbService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private ModelAndViewRedirectHelper modelAndViewRedirectHelper;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ApplicationTypeService applicationTypeService;

    @Autowired
    private DBBackupHelper dbBackupHelper;

    @Autowired
    private DBPasswordHelper dbPasswordHelper;

    @Autowired
    private NewMailService mailService;

    static
    {
        createBreadcrumbs.add(Breadcrumb.getHome());
        createBreadcrumbs.add(new Breadcrumb("sections.dbinstance.create.title"));

        viewBreadcrumbs.add(Breadcrumb.getHome());
        viewBreadcrumbs.add(new Breadcrumb("sections.dbinstance.view.title"));

        editBreadcrumb = new Breadcrumb("sections.dbinstance.edit.title");
        reverseBreadcrumb = new Breadcrumb("sections.dbinstance.reverse.title");

        deleteBreadcrumb = new Breadcrumb("sections.dbinstance.delete.title");
        new Breadcrumb("sections.dbinstance.delete.confirm.title");
    }

    @InitBinder
    public void setBinder(WebDataBinder dataBinder)
    {
        dataBinder.registerCustomEditor(String.class, new TrimString());
        dataBinder.registerCustomEditor(DBSchema.class, new DBSchemaPropertyEditor());
    }

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public String createDBInstanceForDatasetRender(final Model model,
            @RequestParam(DATASET_ID_PARAM) final Long datasetId, final Principal principal)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return ACCESS_DENIED;
        }

        return permissionService.canDataset(PermissionType.CREATE_DATABASE_INSTANCE, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {
                        if (dataset.getDatabaseInstance() != null)
                        {
                            throw new ResearchDatasetDBAlreadyExistsException(
                                    "A Database Instance already exists for Dataset id:[" + dataset.getId() + "]");
                        }
                        model.addAttribute(Breadcrumb.BREADCRUMBS, createBreadcrumbs);
                        final DBInstanceDto dbInstanceDto = new DBInstanceDto();

                        dbInstanceDto.setDatasetId(datasetId);

                        return setupCreateDBInstanceView(dataset, dbInstanceDto, model);
                    }
                });
    }

    private String setupCreateDBInstanceView(ResearchDataset dataset, final DBInstanceDto dbInstanceDto,
            final Model model)
    {
        model.addAttribute(DATASET_ATTR, dataset);
        model.addAttribute(DB_INSTANCE_DTO, dbInstanceDto);
        model.addAttribute(SCHEMAS_ATTR, DBSchema.findAllDBSchemas());

        return CREATE_INSTANCE_VIEW;
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ModelAndView createDBInstanceForDataset(final HttpServletRequest request, final Model model,
            final Principal principal, @RequestParam(value = SUBMIT_PARAM, required = false) final String submit,
            @Valid @ModelAttribute(DB_INSTANCE_DTO) final DBInstanceDto dbInstanceDto, final BindingResult result)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return new ModelAndView(ACCESS_DENIED);
        }

        final Long datasetId = dbInstanceDto.getDatasetId();

        String view = permissionService.canDataset(PermissionType.CREATE_DATABASE_INSTANCE, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {
                        if ("Create".equals(submit))
                        {
                            if (result.hasErrors())
                            {
                                return setupCreateDBInstanceView(dataset, dbInstanceDto, model);
                            }
                            dbService.createDBForDataset(dataset, dbInstanceDto);

                            return REDIRECT_TO_VIEW_VIEW;
                        }

                        return REDIRECT_TO_HOME_VIEW;
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

    @RequestMapping(value = "/view", method = RequestMethod.GET)
    public String viewDBInstanceForDatasetRender(final Model model,
            @RequestParam(DATASET_ID_PARAM) final Long datasetId, final Principal principal,
            @RequestParam(value = "successfullyChanged", required = false) final boolean successfullyChanged,
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
                        model.addAttribute(Breadcrumb.BREADCRUMBS, viewBreadcrumbs);
                        final ResearchDatasetDB datasetDB = dataset.getDatabaseInstance();
                        if (datasetDB == null)
                        {
                            throw new ResearchDatasetDBDoesNotExistException(
                                    "Database Instance does not exist for dataset with id:" + datasetId);
                        }

                        final DBUser dbUser = findAppropriateDBUser(datasetDB, datasetId, principal);
                        model.addAttribute("dbQueryPermission",
                                dbUser.getAccessLevel().equals(DBAccess.FULL_ACCESS) ? true : false);

                        boolean canDelete = permissionService.canAction(PermissionType.DELETE_DATABASE_INSTANCE,
                                datasetId, principal);

                        boolean canEdit = permissionService.canAction(PermissionType.EDIT_DATABASE_INSTANCE, datasetId,
                                principal);

                        boolean canReverse = permissionService.canAction(PermissionType.REVERSE_DATABASE_SCHEMA,
                                datasetId, principal);

                        boolean canChangePassword = permissionService.canAction(
                                PermissionType.CHANGE_DATABASE_PASSWORD, datasetId, principal);

                        DBBackup dBBackup = datasetDB.getDBBackupUsed();
                        if (dBBackup != null)
                        {
                            model.addAttribute("dBBackup", dBBackup);
                            model.addAttribute("hasRestored", true);
                        }
                        model.addAttribute(SHOW_DELETE_ATTR, canDelete);
                        model.addAttribute(SHOW_EDIT_ATTR, canEdit);
                        model.addAttribute(SHOW_REVERSE_ATTR, canReverse);
                        model.addAttribute(CAN_CHANGE_PASSWORD, canChangePassword);

                        model.addAttribute(DB_USER_ATTR, dbUser);
                        model.addAttribute(DATASET_ATTR, dataset);
                        model.addAttribute(DATASET_DB_ATTR, datasetDB);

                        if (successfullyChanged)
                        {
                            model.addAttribute("successfullyChanged", successfullyChanged);
                        }

                        List<ResearchDatabaseQuery> queries = ResearchDatabaseQuery
                                .findResearchDatabaseQuerysByResearchDatasetDB(datasetDB).getResultList();
                        model.addAttribute("dbQuery", queries);
                        model.addAttribute(CAN_EDIT_ATTR,
                                permissionService.canAction(PermissionType.EDIT_DATABASE_SQL, datasetId, principal));
                        model.addAttribute(CAN_DELETE_ATTR,
                                permissionService.canAction(PermissionType.CREATE_DATABASE_SQL, datasetId, principal));
                        return VIEW_INSTANCE_VIEW;
                    }
                });
    }

    @RequestMapping(value = "/view", method = RequestMethod.POST)
    public ModelAndView viewInstance(final Principal principal, @RequestParam(DATASET_ID_PARAM) final Long datasetId,
            final HttpServletRequest request, @RequestParam(value = SUBMIT_PARAM, required = false) final String submit)
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
                        if ("Change Database Password".equals(submit))
                        {
                            return "/dbinstance/change";
                        }
                        if ("New Query".equals(submit))
                        {
                            return "/dbinstance/query";
                        }
                        return "redirect:/";
                    }
                });
        if ("/dbinstance/change".equals(view))
        {
            return redirectToViewInstance(view, request, datasetId);
        }
        else if ("/dbinstance/query".equals(view))
        {
            return redirectToViewInstance(view, request, datasetId);
        }
        else
        {
            return new ModelAndView(view);
        }
    }

    @RequestMapping(value = "/reverse", method = RequestMethod.GET)
    public String reverseDBInstance(final Model model, @RequestParam(DATASET_ID_PARAM) final Long datasetId,
            final Principal principal)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return ACCESS_DENIED;
        }
        final List<Breadcrumb> reversePageBreadcrumbs = this.createBreadcrumbUpToViewInstance(datasetId);
        reversePageBreadcrumbs.add(reverseBreadcrumb);
        model.addAttribute(Breadcrumb.BREADCRUMBS, reversePageBreadcrumbs);

        return permissionService.canDataset(PermissionType.REVERSE_DATABASE_SCHEMA, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {
                        DBSchema schema = new DBSchema("", "");
                        populateReverseEngineerModel(model, dataset, schema);
                        return REVERSE_INSTANCE_VIEW;
                    }
                });

    }

    private void populateReverseEngineerModel(final Model model, ResearchDataset dataset, DBSchema schema)
    {
        final ResearchDatasetDB datasetDB = dataset.getDatabaseInstance();
        if (datasetDB == null)
        {
            throw new ResearchDatasetDBDoesNotExistException("Database Instance does not exist for dataset with id:"
                    + dataset.getId());
        }

        List<String> sql;
        try
        {
            sql = dbBackupHelper.dabataseDdl(datasetDB);
        }
        catch (IOException e)
        {
            LOG.error("Could not reverse engineer database DDL", e);
            throw new RuntimeException(e);
        }

        model.addAttribute("reversedSql", htmlFormat(sql));
        model.addAttribute(DATASET_ATTR, dataset);
        model.addAttribute(DATASET_DB_ATTR, datasetDB);
        model.addAttribute(SCHEMA_DTO, schema);
    }

    @RequestMapping(value = "/reverse", method = RequestMethod.POST)
    public ModelAndView reverseDBInstancePost(

    @Valid @ModelAttribute(SCHEMA_DTO) final DBSchema schema, final BindingResult results,
            @RequestParam(value = SUBMIT_PARAM, required = false) final String submit, final Model model,
            final HttpServletRequest request, @RequestParam(DATASET_ID_PARAM) final Long datasetId,
            final Principal principal)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return new ModelAndView(ACCESS_DENIED);
        }

        if ("Reverse".equals(submit) && results.hasErrors())
        {
            ResearchDataset dataset = ResearchDataset.findResearchDataset(datasetId);
            populateReverseEngineerModel(model, dataset, schema);
            return new ModelAndView(REVERSE_INSTANCE_VIEW);
        }

        String view = permissionService.canDataset(PermissionType.REVERSE_DATABASE_SCHEMA, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {

                        if ("Reverse".equals(submit))
                        {
                            return commitReverseEngineeredDatabase(schema, dataset, results, model);
                        }
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

    private String commitReverseEngineeredDatabase(final DBSchema schema, ResearchDataset dataset,
            final BindingResult results, final Model model)
    {
        try
        {
            final ResearchDatasetDB datasetDB = dataset.getDatabaseInstance();
            List<String> sql = dbBackupHelper.dabataseDdl(datasetDB);
            dbService.commitReverseEngineeredDatabase(sql, datasetDB, schema);
            return REDIRECT_TO_VIEW_VIEW;
        }
        catch (NoneUniqueNameException e)
        {
            String[] nameErrorCode = {""};
            String[] nameErrorArg = {""};
            FieldError nameError = new FieldError(SCHEMA_DTO, "name", schema.getName(), true, nameErrorCode,
                    nameErrorArg, "A schema with the same name already exists");
            results.addError(nameError);
            populateReverseEngineerModel(model, dataset, schema);
            return REVERSE_INSTANCE_VIEW;
        }
        catch (IOException e)
        {
            return REVERSE_INSTANCE_VIEW;
        }
    }

    private String htmlFormat(List<String> sql)
    {
        StringBuffer dllSql = new StringBuffer();
        for (String line : sql)
        {
            if (line.startsWith("/*") || line.startsWith("DROP "))
            {
                continue;
            }

            if (!line.startsWith("CREATE TABLE") && !line.startsWith(") ENGINE"))
            {
                dllSql.append(StringUtils.repeat("&nbsp;", NUMBER_OF_INDENTATION_SPACES));
            }

            dllSql.append(line + "<br />");

            if (line.endsWith(";"))
            {
                dllSql.append("<br />");
            }

        }
        return dllSql.toString();
    }

    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public String editDBInstanceForDatasetRender(final Model model,
            @RequestParam(DATASET_ID_PARAM) final Long datasetId, final Principal principal)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return ACCESS_DENIED;
        }
        final List<Breadcrumb> editPageBreadcrumbs = this.createBreadcrumbUpToViewInstance(datasetId);
        editPageBreadcrumbs.add(editBreadcrumb);
        model.addAttribute(Breadcrumb.BREADCRUMBS, editPageBreadcrumbs);

        return permissionService.canDataset(PermissionType.EDIT_DATABASE_INSTANCE, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {
                        final ResearchDatasetDB datasetDB = dataset.getDatabaseInstance();
                        if (datasetDB == null)
                        {
                            throw new ResearchDatasetDBDoesNotExistException(
                                    "Database Instance does not exist for dataset with id:" + datasetId);
                        }

                        model.addAttribute(DATASET_ATTR, dataset);
                        model.addAttribute(DATASET_DB_ATTR, datasetDB);

                        return EDIT_INSTANCE_VIEW;
                    }
                });
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public ModelAndView editDBInstanceForDatasetSubmit(final HttpServletRequest request, final Model model,
            @RequestParam(DATASET_ID_PARAM) final Long datasetId,
            @RequestParam(value = SUBMIT_PARAM, required = false) final String submit, final Principal principal,
            @ModelAttribute(DATASET_DB_ATTR) final ResearchDatasetDB inputDBInstance, final BindingResult results)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return new ModelAndView(ACCESS_DENIED);
        }
        // unless submit is Delete, redirect to view page
        if (!"Save".equals(submit))
        {
            return redirectToViewInstance(REDIRECT_TO_VIEW_VIEW, request, datasetId);
        }

        // custom validation as we only expect some of the values to be filled in
        validateEditDBInstance(inputDBInstance, DATASET_DB_ATTR, results);

        String view = permissionService.canDataset(PermissionType.EDIT_DATABASE_INSTANCE, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {
                        final ResearchDatasetDB datasetDB = dataset.getDatabaseInstance();
                        if (datasetDB == null)
                        {
                            throw new ResearchDatasetDBDoesNotExistException(
                                    "Database Instance does not exist for dataset with id:" + datasetId);
                        }
                        if (results.hasErrors())
                        {
                            final List<Breadcrumb> editPageBreadcrumbs = createBreadcrumbUpToViewInstance(datasetId);
                            editPageBreadcrumbs.add(editBreadcrumb);
                            model.addAttribute(Breadcrumb.BREADCRUMBS, editPageBreadcrumbs);

                            model.addAttribute(DATASET_ATTR, dataset);
                            return EDIT_INSTANCE_VIEW;
                        }
                        String newDescription = inputDBInstance.getDescription();
                        datasetDB.setDescription(newDescription);
                        datasetDB.merge();

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

    private void validateEditDBInstance(ResearchDatasetDB inputDBInstance, String modelName, BindingResult results)
    {
        String description = inputDBInstance.getDescription();
        if (StringUtils.isEmpty(description))
        {
            String[] codes = new String[] {"NotEmpty.datasetDB.description", "NotEmpty"};
            Object[] args = new Object[] {"description"};
            FieldError error = new FieldError(modelName, "description", description, false, codes, args,
                    "Cannot be Empty");
            results.addError(error);
        }
        // TODO CHECKSTYLE-OFF: MagicNumber
        int maxLength = 1000;
        if (description != null && description.length() > maxLength)
        {
            String[] codes = new String[] {"Length.datasetDB.description", "Length"};
            Object[] args = new Object[] {"description", maxLength};
            FieldError error = new FieldError(modelName, "description", description, false, codes, args,
                    "Input length exceeded limit");
            results.addError(error);
        }
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public String deleteDBInstanceForDatasetRender(final Model model,
            @RequestParam(DATASET_ID_PARAM) final Long datasetId, final Principal principal)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return ACCESS_DENIED;
        }

        final ResearchDataset researchDataset = findDataset(datasetId);
        final List<Breadcrumb> crumbsUpToView = this.createBreadcrumbUpToViewInstance(datasetId);
        return permissionService.canDataset(PermissionType.DELETE_DATABASE_INSTANCE, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {
                        final ResearchDatasetDB datasetDB = researchDataset.getDatabaseInstance();
                        if (datasetDB == null)
                        {
                            throw new ResearchDatasetDBDoesNotExistException(
                                    "Database Instance does not exist for dataset with id:" + datasetId);
                        }

                        crumbsUpToView.add(deleteBreadcrumb);
                        model.addAttribute(Breadcrumb.BREADCRUMBS, crumbsUpToView);

                        model.addAttribute(DATASET_ATTR, dataset);
                        model.addAttribute(DATASET_DB_ATTR, datasetDB);
                        return DELETE_INSTANCE_VIEW;
                    }
                });
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ModelAndView deleteDBInstanceForDatasetPost(final HttpServletRequest request,
            @RequestParam(DATASET_ID_PARAM) final Long datasetId,
            @RequestParam("confirmDbName") final String confirmDbName,
            @RequestParam(value = SUBMIT_PARAM, required = false) final String submit, final Principal principal)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return new ModelAndView(ACCESS_DENIED);
        }

        // unless submit is Delete, redirect to view page
        if (!"Delete".equals(submit))
        {
            return redirectToViewInstance(REDIRECT_TO_VIEW_VIEW, request, datasetId);
        }
        final ResearchDataset researchDataset = findDataset(datasetId);
        final List<Breadcrumb> crumbsUpToView = this.createBreadcrumbUpToViewInstance(datasetId);
        final Map<String, Object> model = new HashMap<String, Object>();
        String view = permissionService.canDataset(PermissionType.DELETE_DATABASE_INSTANCE, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {

                        final ResearchDatasetDB datasetDB = researchDataset.getDatabaseInstance();
                        if (datasetDB == null)
                        {
                            throw new ResearchDatasetDBDoesNotExistException(
                                    "Database Instance does not exist for dataset with id:" + datasetId);
                        }

                        if (datasetDB.getDbName().equals(confirmDbName))
                        {

                            dbService.deleteDBForDataset(dataset);

                            return REDIRECT_TO_HOME_VIEW;
                        }
                        model.put("confirmFailure", true);
                        crumbsUpToView.add(deleteBreadcrumb);
                        model.put(Breadcrumb.BREADCRUMBS, crumbsUpToView);

                        model.put(DATASET_ATTR, dataset);
                        model.put(DATASET_DB_ATTR, datasetDB);
                        return DELETE_INSTANCE_VIEW;
                    }
                });
        ModelAndView mav = new ModelAndView(view);
        mav.addAllObjects(model);
        return mav;
    }

    private DBUser findAppropriateDBUser(ResearchDatasetDB datasetDB, Long datasetId, Principal principal)
    {

        if (permissionService.canAction(PermissionType.CREATE_DATABASE_INSTANCE, datasetId, principal))
        {
            // show full access
            return datasetDB.findFullAccessDBUser();
        }
        if (permissionService.canAction(PermissionType.EDIT_DATABASE_INSTANCE, datasetId, principal))
        {
            // show update access
            return datasetDB.findUpdateAccessDBUser();
        }
        if (permissionService.canAction(PermissionType.VIEW_DATABASE_INSTANCE, datasetId, principal))
        {
            // show view access
            return datasetDB.findViewAccessDBUser();
        }

        // no access
        return null;
    }

    @RequestMapping(value = "/downloadconfig", method = RequestMethod.GET)
    public String downloadConfigForDataset(@RequestParam(DATASET_ID_PARAM) final Long datasetId,
            final HttpServletRequest request, final HttpServletResponse response, final Principal principal,
            final Locale locale) throws IOException
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return ACCESS_DENIED;
        }

        final ResearchDataset researchDataset = findDataset(datasetId);

        return permissionService.canDataset(PermissionType.VIEW_DATABASE_INSTANCE, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {
                        final ResearchDatasetDB datasetDB = researchDataset.getDatabaseInstance();
                        if (datasetDB == null)
                        {
                            throw new ResearchDatasetDBDoesNotExistException(
                                    "Database Instance does not exist for dataset with id:" + datasetId);
                        }
                        final DBUser dbUser = findAppropriateDBUser(datasetDB, datasetId, principal);

                        response.setContentType("application/force-download");
                        response.setHeader("Content-Disposition", "attachment; filename=\"dbInstanceAccess-"
                                + datasetId + ".txt\"");

                        ServletOutputStream out;
                        try
                        {
                            out = response.getOutputStream();
                            writeAccessText(out, locale, dataset, datasetDB, dbUser);
                            out.flush();
                            out.close();
                        }
                        catch (IOException e)
                        {
                            throw new ResearchDatasetDBException("Failed to create config txt file");
                        }

                        return null;
                    }
                });
    }

    @RequestMapping(value = "/change", method = RequestMethod.GET)
    public String changeDBInstancePasswordRender(final Model model,
            @RequestParam(DATASET_ID_PARAM) final Long datasetId, final Principal principal)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return ACCESS_DENIED;
        }

        return permissionService.canDataset(PermissionType.CHANGE_DATABASE_PASSWORD, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {
                        final ResearchDatasetDB datasetDB = dataset.getDatabaseInstance();
                        if (datasetDB == null)
                        {
                            throw new ResearchDatasetDBDoesNotExistException(
                                    "Database Instance does not exist for dataset with id:" + datasetId);
                        }
                        final DBUser dbUser = findAppropriateDBUser(datasetDB, datasetId, principal);
                        model.addAttribute(DB_USER_ATTR, dbUser);
                        model.addAttribute(Breadcrumb.BREADCRUMBS, viewBreadcrumbs);
                        model.addAttribute(DATASET_ATTR, dataset);
                        return CHANGE_PASSWORD_VIEW;
                    }
                });
    }

    // TODO CHECKSTYLE-OFF: ParameterNumber
    @RequestMapping(value = "/changePassword", method = RequestMethod.PUT)
    public ModelAndView changeDBInstancePasswordPost(
            @RequestParam(value = "fullAccessLevel", required = false) final String fullAccess,
            @RequestParam(value = "editingAccessLevel", required = false) final String editingAccess,
            @RequestParam(value = "viewingAccessLevel", required = false) final String viewingAccess,
            @RequestParam(value = "_cancel", required = false) final String cancel,
            @RequestParam(value = "datasetId") final Long datasetId, final Model model, final Principal principal,
            final HttpServletRequest request)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return new ModelAndView(ACCESS_DENIED);
        }

        if ("Cancel".equals(cancel))
        {
            return redirectToViewInstance("view", request, datasetId);
        }

        String view = permissionService.canDataset(PermissionType.CHANGE_DATABASE_PASSWORD, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {
                        boolean successfulChange = false;
                        final ResearchDatasetDB datasetDB = dataset.getDatabaseInstance();
                        //FIXME : successfulChange does not validate properly if multiple checkboxes are ticked
                        if (fullAccess != null)
                        {
                            DBUser dBUser = DBUser.findDBUsersByDatabaseInstanceAndAccessLevel(datasetDB,
                                    DBAccess.FULL_ACCESS).getSingleResult();
                            successfulChange = dbService.changeDBPassword(dBUser);
                        }
                        if (editingAccess != null)
                        {
                            DBUser dBUser = DBUser.findDBUsersByDatabaseInstanceAndAccessLevel(datasetDB,
                                    DBAccess.UPDATE_ACCESS).getSingleResult();
                            successfulChange = dbService.changeDBPassword(dBUser);
                        }
                        if (viewingAccess != null)
                        {
                            DBUser dBUser = DBUser.findDBUsersByDatabaseInstanceAndAccessLevel(datasetDB,
                                    DBAccess.VIEW_ACCESS).getSingleResult();
                            successfulChange = dbService.changeDBPassword(dBUser);
                        }
                        
                        model.addAttribute("successfullyChanged", successfulChange);
                        
                        if (successfulChange)
                        {
                            mailService.sendDatabasePasswordChangedEmail(dataset, UrlHelper.getCurrentBaseUrl(request));
                        }

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

    private void writeAccessText(ServletOutputStream out, Locale locale, ResearchDataset dataset,
            ResearchDatasetDB datasetDB, DBUser dbUser) throws IOException
    {
        String datasetLabel = messageSource.getMessage("dbinstance.dataset", null, "Dataset", locale);
        String hostnameLabel = messageSource.getMessage("dbinstance.hostname", null, "Hostname", locale);
        String usernameLabel = messageSource.getMessage("dbinstance.username", null, "Username", locale);
        String passwordlabel = messageSource.getMessage("dbinstance.password", null, "Password", locale);
        String dbNameLabel = messageSource.getMessage("dbinstance.dbName", null, "Instance Name", locale);
        String schemaLabel = messageSource.getMessage("dbinstance.dbSchema", null, "Schema Name", locale);

        out.println(buildPropertyPair(datasetLabel, dataset.getName()));
        out.println(buildPropertyPair(dbNameLabel, datasetDB.getDbName()));
        out.println(buildPropertyPair(schemaLabel, datasetDB.getDbSchema().getName()));
        String instructionDescription = messageSource.getMessage("dbinstance.accessInstructionDescription", null,
                "Use these values to access database instance:", locale);
        out.println(instructionDescription);

        out.println(buildPropertyPair(hostnameLabel, datasetDB.getDbHostname()));
        out.println(buildPropertyPair(usernameLabel, dbUser.getDbUsername()));
        out.println(buildPropertyPair(passwordlabel, dbUser.getDbPassword()));

    }

    private String buildPropertyPair(String label, String value)
    {
        return label + ": " + value;
    }

    private ResearchDataset findDataset(Long datasetId)
    {
        ResearchDataset researchDataset = ResearchDataset.findResearchDataset(datasetId);
        if (researchDataset == null)
        {
            throw new EntityNotFoundException("Failed to find Research Dataset with id [" + datasetId + "]");
        }
        return researchDataset;
    }

    private List<Breadcrumb> createBreadcrumbUpToViewInstance(Long datasetId)
    {
        List<Breadcrumb> crumbs = new ArrayList<Breadcrumb>();
        crumbs.add(Breadcrumb.getHome());
        crumbs.add(new Breadcrumb("sections.dbinstance.view.title", "/dbinstance/view?datasetId=" + datasetId));
        return crumbs;
    }

}
