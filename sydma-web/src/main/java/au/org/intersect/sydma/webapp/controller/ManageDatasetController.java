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
//TODO CHECKSTYLE-OFF: ImportOrder
package au.org.intersect.sydma.webapp.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import flexjson.JSONSerializer;

import au.org.intersect.dms.core.errors.ConnectionClosedError;
import au.org.intersect.dms.core.errors.PathNotFoundException;
import au.org.intersect.sydma.webapp.controller.propertyeditor.TrimString;
import au.org.intersect.sydma.webapp.domain.PermissionEntry;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.dto.DirectoryName;
import au.org.intersect.sydma.webapp.dto.FilePathInfo;
import au.org.intersect.sydma.webapp.permission.PermissionType;
import au.org.intersect.sydma.webapp.permission.path.Path;
import au.org.intersect.sydma.webapp.permission.path.PathBuilder;
import au.org.intersect.sydma.webapp.security.SecurityContextFacade;
import au.org.intersect.sydma.webapp.service.FileManagementService;
import au.org.intersect.sydma.webapp.service.FilePathService;
import au.org.intersect.sydma.webapp.service.PermissionService;
import au.org.intersect.sydma.webapp.util.Breadcrumb;

/**
 * Controller for browsing and managing dataset and its directories and files
 * 
 * @version $Rev: 29 $
 */
// TODO CHECKSTYLE-OFF: ClassFanOutComplexity
@Controller
@RequestMapping("/managedataset/**")
public class ManageDatasetController extends AbstractControllerWithDmsConnection
{
    private static final Logger LOG = LoggerFactory.getLogger(ManageDatasetController.class);

    private static final String BROWSE_VIEW = "managedataset/browse";

    private static final String CREATE_DIR_VIEW = "managedataset/createDirectory";

    private static final String DELETE_DIR_VIEW = "managedataset/deleteDirectory";

    private static final String DELETE_FILE_VIEW = "managedataset/deleteFile";

    private static final String MOVE_FILE_VIEW = "managedataset/move";

    private static final String PATH_NOT_FOUND_VIEW = "managedataset/pathNotFound";

    private static final String SUCCESS_AJAX_VIEW = "managedataset/success";

    private static final String ROOT_PATH_ATTR = "nodeRootPath";
    private static final String ROOT_NAME_ATTR = "nodeRootName";
    private static final String ROOT_PERMISSION_ATTR = "nodeRootPermission";
    private static final String GROUP_ATTR = "group";
    private static final String PROJECT_ATTR = "project";
    private static final String DATASET_ATTR = "dataset";

    private static final String NAMED_PATH_ATTR = "namedPath";
    private static final String FILE_PATH_ATTR = "filePath";
    private static final String FILE_TYPE_ATTR = "fileType";
    private static final String CONNECTION_ID_ATTR = "connectionId";
    private static final String DIRECTORY_NAME_ATTR = "directoryName";

    private static final String MOVING_PATH_ATTR = "movingPath";
    private static final String DESTINATION_PATH_ATTR = "destinationPath";

    private static final String MOVING_NAMED_PATH_ATTR = "movingNamedPath";
    private static final String DESTINATION_NAMED_PATH_ATTR = "destinationNamedPath";

    private static final String ERROR_ATTR = "executionError";

    private static final String PATH_SEPARATOR = "/";

    private static final String ACCESS_DENIED_VIEW = "accessDenied";
    private static final String MOVE_DENIED_VIEW = "managedataset/moveDenied";

    @Autowired
    private FilePathService filePathService;

    @Autowired
    private SecurityContextFacade securityContextFacade;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private FileManagementService fileManagementService;

    @InitBinder
    public void setBinder(WebDataBinder dataBinder)
    {
        dataBinder.registerCustomEditor(String.class, new TrimString());
    }

    private List<Breadcrumb> addBreadCrumb(String datasetName)
    {
        List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
        breadcrumbs.add(Breadcrumb.getHome());
        breadcrumbs.add(new Breadcrumb(datasetName, false));
        return breadcrumbs;
    }

    @RequestMapping("/browse/dataset/{datasetId}")
    public String browseDataset(@PathVariable("datasetId") final Long datasetId, final Model model, Principal principal)
    {
        return permissionService.canDataset(PermissionType.VIEW_DATASET, datasetId, principal,
                new PermissionService.ResearchDatasetAction()
                {
                    @Override
                    public String act(ResearchDataset dataset, User user)
                    {
                        if (dataset.getIsPhysical())
                        {
                            return "accessDenied";
                        }

                        ResearchProject project = dataset.getResearchProject();
                        ResearchGroup group = project.getResearchGroup();

                        String datasetName = dataset.getName();
                        model.addAttribute(Breadcrumb.BREADCRUMBS, addBreadCrumb(datasetName));

                        String virtualRootPath = filePathService.createVirtualPath(group.getId(), project.getId(),
                                datasetId);

                        // at dataset level we only allow create directory
                        List<PermissionType> applicableActions = new ArrayList<PermissionType>(1);
                        applicableActions.add(PermissionType.CREATE_DIRECTORY);
                        List<PermissionType> allowedActions = permissionService.restrictDirectoryActions(
                                applicableActions, virtualRootPath, user);

                        List<String> allowedActionList = new ArrayList<String>(allowedActions.size());
                        for (PermissionType permission : allowedActions)
                        {
                            allowedActionList.add(permission.getPermissionTypeName());
                        }

                        JSONSerializer serializer = new JSONSerializer();
                        model.addAttribute(ROOT_PERMISSION_ATTR, serializer.serialize(allowedActionList));
                        model.addAttribute(ROOT_NAME_ATTR, datasetName);
                        model.addAttribute(ROOT_PATH_ATTR, virtualRootPath);

                        model.addAttribute(GROUP_ATTR, group);
                        model.addAttribute(PROJECT_ATTR, project);
                        model.addAttribute(DATASET_ATTR, dataset);

                        return BROWSE_VIEW;
                    }
                });

    }

    @RequestMapping(value = "/directory/create", method = RequestMethod.GET)
    public String createDirectoryRender(@RequestParam(FILE_PATH_ATTR) final String virtualPath, final Model model,
            final Principal principal)
    {
        return permissionService.canDirectory(PermissionType.CREATE_DIRECTORY, virtualPath, principal,
                new PermissionService.DirectoryAction()
                {
                    @Override
                    public String act(Path path, User user)
                    {
                        model.addAttribute(FILE_PATH_ATTR, virtualPath);
                        model.addAttribute(DIRECTORY_NAME_ATTR, new DirectoryName());
                        return CREATE_DIR_VIEW;
                    }
                });
    }

    @RequestMapping(value = "/createDirectory", method = RequestMethod.POST)
    public String createDirectoryPost(@RequestParam(FILE_PATH_ATTR) final String virtualPath,
            @RequestParam(CONNECTION_ID_ATTR) final Integer connectionId, final Model model,
            @Valid @ModelAttribute(DIRECTORY_NAME_ATTR) final DirectoryName directoryName, final BindingResult result,
            final Principal principal)
    {
        return permissionService.canDirectory(PermissionType.CREATE_DIRECTORY, virtualPath, principal,
                new PermissionService.DirectoryAction()
                {
                    @Override
                    public String act(Path path, User user)
                    {
                        if (!result.hasErrors())
                        {
                            String absolutePath = filePathService.resolveToRelativePath(path);
                            String filePath = path.getPath();
                            try
                            {
                                boolean createSuccess = getDmsService().createDir(connectionId, absolutePath,
                                        directoryName.getDirectoryName());

                                if (!createSuccess)
                                {
                                    String[] codes = {"manageDataset.directory.create.fail"};
                                    Object[] args = {};
                                    FieldError fieldError = new FieldError(FILE_PATH_ATTR, DIRECTORY_NAME_ATTR,
                                            directoryName.getDirectoryName(), false, codes, args,
                                            "Failed to create directory");
                                    result.addError(fieldError);
                                    model.addAttribute(FILE_PATH_ATTR, virtualPath);
                                    return CREATE_DIR_VIEW;
                                }
                            }
                            // TODO CHECKSTYLE-OFF: IllegalCatch
                            catch (PathNotFoundException e)
                            {
                                return handlePathNotFoundException(filePath, e, model);
                            }
                            catch (ConnectionClosedError e)
                            {
                                // connection closed is handled by other handlers
                                throw e;
                            }
                            catch (Exception e)
                            {
                                String[] codes = {"manageDataset.directory.create.exception"};
                                String errorMessage = "An error occured";
                                if (e.getMessage() != null)
                                {
                                    //Overwrite the original exception message
                                    //errorMessage = e.getMessage();
                                    errorMessage = "Cannot create as there is a directory with the same name";
                                }
                                Object[] args = {errorMessage};
                                FieldError fieldError = new FieldError(FILE_PATH_ATTR, DIRECTORY_NAME_ATTR,
                                        directoryName.getDirectoryName(), false, codes, args, errorMessage);
                                result.addError(fieldError);
                            }
                        }
                        // Check for error again incase new errors have been added during execution
                        if (result.hasErrors())
                        {
                            model.addAttribute(FILE_PATH_ATTR, virtualPath);
                            return CREATE_DIR_VIEW;
                        }
                        return SUCCESS_AJAX_VIEW;
                    }
                });
    }

    @RequestMapping(value = "/directory/delete", method = RequestMethod.GET)
    public String deleteDirectoryRender(@RequestParam(FILE_PATH_ATTR) final String virtualPath, final Model model,
            Principal principal)
    {
        return permissionService.canDirectory(PermissionType.DELETE_DIRECTORY, virtualPath, principal,
                new PermissionService.DirectoryAction()
                {
                    @Override
                    public String act(Path path, User user)
                    {
                        FilePathInfo pathInfo = filePathService.parseVirtualPath(virtualPath);
                        String directory = pathInfo.getDirectory();
                        model.addAttribute(NAMED_PATH_ATTR, directory);
                        model.addAttribute(FILE_PATH_ATTR, virtualPath);
                        return DELETE_DIR_VIEW;
                    }
                });
    }

    @RequestMapping(value = "/file/delete", method = RequestMethod.GET)
    public String deleteFileRender(@RequestParam(FILE_PATH_ATTR) final String virtualPath, final Model model,
            final Principal principal)
    {
        return permissionService.canDirectory(PermissionType.DELETE_FILE, virtualPath, principal,
                new PermissionService.DirectoryAction()
                {
                    @Override
                    public String act(Path path, User user)
                    {
                        FilePathInfo pathInfo = filePathService.parseVirtualPath(virtualPath);
                        String directory = pathInfo.getDirectory();
                        model.addAttribute(NAMED_PATH_ATTR, directory);
                        model.addAttribute(FILE_PATH_ATTR, virtualPath);
                        return DELETE_FILE_VIEW;
                    }
                });
    }

    /**
     * Deletes files and directories
     */
    @RequestMapping(method = RequestMethod.POST, value = "/delete")
    public String delete(@RequestParam final Integer connectionId,
            @RequestParam(FILE_PATH_ATTR) final String virtualPath,
            @RequestParam(FILE_TYPE_ATTR) final String fileType, final Model model, final Principal principal)
    {
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        Path path = PathBuilder.buildFromString(virtualPath);
        final String view;
        if (("directory".equals(fileType) && permissionService.canDirectoryAction(PermissionType.DELETE_DIRECTORY,
                path, user))
                || ("file".equals(fileType) && permissionService.canDirectoryAction(PermissionType.DELETE_FILE, path,
                        user)))
        {
            view = performDelete(fileType, virtualPath, path, connectionId, model);
        }
        else
        {
            view = ACCESS_DENIED_VIEW;
        }
        return view;
    }

    private String performDelete(String fileType, String virtualPath, Path path, Integer connectionId, Model model)
    {
        String directory = "";
        if (path.isFilePath())
        {
            directory = path.getFilePath();
        }

        String errorMessage = "An error occured";
        boolean deleteSuccess = false;
        try
        {
            deleteSuccess = fileManagementService.deleteFileOrDirectory(path, connectionId);
        }
        // TODO CHECKSTYLE-OFF: IllegalCatch
        catch (PathNotFoundException e)
        {
            return handlePathNotFoundException(directory, e, model);
        }
        catch (ConnectionClosedError e)
        {
            // connection closed is handled by other handlers
            throw e;
        }
        final String view;
        if (!deleteSuccess)
        {

            model.addAttribute(NAMED_PATH_ATTR, directory);
            model.addAttribute(FILE_PATH_ATTR, virtualPath);
            model.addAttribute(ERROR_ATTR, errorMessage);

            if ("file".equals(fileType))
            {
                view = DELETE_FILE_VIEW;
            }
            else
            {
                view = DELETE_DIR_VIEW;
            }
        }
        else
        {
            cleanUpPermissions(path, fileType);
            view = SUCCESS_AJAX_VIEW;
        }
        return view;
    }

    @RequestMapping(value = "/file/move", method = RequestMethod.GET)
    public String moveFileRender(@RequestParam(MOVING_PATH_ATTR) final String movingPath,
            @RequestParam(DESTINATION_PATH_ATTR) final String destinationPath, final Model model,
            final Principal principal)
    {
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        Path virtualMovingPath = PathBuilder.buildFromString(movingPath);
        Path virtualDestinationPath = PathBuilder.buildFromString(destinationPath);

        if (permissionService.canDirectoryAction(PermissionType.MOVE_DIRECTORY_FILE, virtualMovingPath, user)
                && permissionService.canDirectoryAction(PermissionType.MOVE_DIRECTORY_FILE, virtualDestinationPath,
                        user))
        {

            Path movingPathInfo = PathBuilder.buildFromString(movingPath);
            Path destinationPathInfo = PathBuilder.buildFromString(destinationPath);

            addPathAttrToModel(movingPath, movingPathInfo, destinationPath, destinationPathInfo, model);
            return MOVE_FILE_VIEW;
        }

        model.addAttribute(MOVING_PATH_ATTR, getNamedPath(virtualMovingPath));
        model.addAttribute(DESTINATION_PATH_ATTR, getNamedPath(virtualDestinationPath));

        return MOVE_DENIED_VIEW;
    }

    @RequestMapping(value = "/move", method = RequestMethod.POST)
    public String moveFile(@RequestParam(CONNECTION_ID_ATTR) Integer srcConnectionId,
            @RequestParam(MOVING_PATH_ATTR) String movingPath,
            @RequestParam(DESTINATION_PATH_ATTR) String destinationPath, Model model, Principal principal)
    {
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        Path srcPath = PathBuilder.buildFromString(movingPath);
        Path destPath = PathBuilder.buildFromString(destinationPath);

        if (permissionService.canDirectoryAction(PermissionType.MOVE_DIRECTORY_FILE, srcPath, user)
                && permissionService.canDirectoryAction(PermissionType.MOVE_DIRECTORY_FILE, destPath, user))
        {
            String username = securityContextFacade.getAuthorizedUsername();

            Integer destConnectionId = connectLocal(principal.getName());

            // TODO: CLEAN UP
            boolean moveSuccess = fileManagementService.moveFileOrDirectory(username, srcPath, destPath,
                    srcConnectionId, destConnectionId);
            if (moveSuccess)
            {
                permissionService.updatePermissionEntries(srcPath, destPath);
            }

            return SUCCESS_AJAX_VIEW;
        }
        return ACCESS_DENIED_VIEW;
    }

    private void cleanUpPermissions(Path path, String fileType)
    {
        // Remove permissions for the deleted file or directory
        List<PermissionEntry> affectedEntries = PermissionEntry.findPermissionEntrysByPathEquals(path.getPath())
                .getResultList();
        if (!affectedEntries.isEmpty())
        {
            for (PermissionEntry entry : affectedEntries)
            {
                // TODO: Do we need to log this in the permission activity log?
                entry.remove();
            }
        }
        // Clean up children permissions if directory is deleted
        if ("directory".equals(fileType))
        {
            List<PermissionEntry> childEntries = PermissionEntry.findChildPath(path.getPath()).getResultList();
            for (PermissionEntry childEntry : childEntries)
            {
                childEntry.remove();
            }
        }
    }

    private void addPathAttrToModel(String movingPath, Path movingPathInfo, String destinationPath,
            Path destinationPathInfo, Model model)
    {
        String movingNamedpath = getNamedPath(movingPathInfo);
        String destinationNamedpath = getNamedPath(destinationPathInfo);

        if (StringUtils.isEmpty(destinationNamedpath))
        {
            destinationNamedpath = "Dataset top level";
        }

        model.addAttribute(MOVING_NAMED_PATH_ATTR, movingNamedpath);
        model.addAttribute(DESTINATION_NAMED_PATH_ATTR, destinationNamedpath);
        model.addAttribute(MOVING_PATH_ATTR, movingPath);
        model.addAttribute(DESTINATION_PATH_ATTR, destinationPath);

    }

    private String getNamedPath(Path pathInfo)
    {

        if (pathInfo.isDatasetPath() || pathInfo.isFilePath())
        {
            Long datasetId = pathInfo.getDatasetId();
            ResearchDataset dataset = ResearchDataset.findResearchDataset(datasetId);

            String entityPath = dataset.getName();
            String directoryPath = "";
            if (pathInfo.isFilePath())
            {
                directoryPath = pathInfo.getFilePath();
            }
            // truncate path separator at end
            if (directoryPath.length() > 1
                    && PATH_SEPARATOR.equals(String.valueOf(directoryPath.charAt(directoryPath.length() - 1))))
            {
                directoryPath = directoryPath.substring(0, directoryPath.length() - 1);
            }
            return entityPath + directoryPath;
        }
        else
        {
            // We don't handle none-dataset path
            throw new IllegalArgumentException("Unexpected path type");
        }
    }

    private String handlePathNotFoundException(String path, PathNotFoundException e, Model model)
    {
        String errorMessage;
        if (e.getMessage() != null)
        {
            errorMessage = e.getMessage();
        }
        else
        {
            errorMessage = "The path does not exist";
        }

        model.addAttribute(FILE_PATH_ATTR, path);
        model.addAttribute(ERROR_ATTR, errorMessage);

        return PATH_NOT_FOUND_VIEW;
    }

}
