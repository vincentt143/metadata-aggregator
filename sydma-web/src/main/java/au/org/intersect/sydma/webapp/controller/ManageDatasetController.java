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

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.domain.JobItem;
import au.org.intersect.dms.core.errors.ConnectionClosedError;
import au.org.intersect.dms.core.errors.PathNotFoundException;
import au.org.intersect.dms.core.service.dto.JobStatus;
import au.org.intersect.sydma.webapp.controller.propertyeditor.TrimString;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;
import au.org.intersect.sydma.webapp.dto.DirectoryName;
import au.org.intersect.sydma.webapp.dto.FilePathInfo;
import au.org.intersect.sydma.webapp.permission.path.Path;
import au.org.intersect.sydma.webapp.permission.path.PathBuilder;
import au.org.intersect.sydma.webapp.security.SecurityContextFacade;
import au.org.intersect.sydma.webapp.service.FilePathService;
import au.org.intersect.sydma.webapp.util.Breadcrumb;

/**
 * Controller for browsing and managing dataset and its directories and files
 * 
 * @version $Rev: 29 $
 */
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

    @Autowired
    private FilePathService filePathService;

    @Autowired
    private SecurityContextFacade securityContextFacade;
    
    @InitBinder
    public void setBinder(WebDataBinder dataBinder) 
    {
        dataBinder.registerCustomEditor(String.class, new TrimString());      
    }

    private void addBreadCrumb(String groupName, String projectName, String datasetName, Model model)
    {
        List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
        breadcrumbs.add(Breadcrumb.getHome());
        breadcrumbs.add(new Breadcrumb(groupName));
        breadcrumbs.add(new Breadcrumb(projectName));
        breadcrumbs.add(new Breadcrumb(datasetName));
        model.addAttribute(Breadcrumb.BREADCRUMBS, breadcrumbs);
    }

    @RequestMapping("/browse/dataset/{datasetId}")
    public String browseDataset(@PathVariable("datasetId") Long datasetId, Model model)
    {
        ResearchDataset dataset = ResearchDataset.findResearchDataset(datasetId);
        ResearchProject project = dataset.getResearchProject();
        ResearchGroup group = project.getResearchGroup();

        // add breadcrumbs
        String datasetName = dataset.getName();
        String projectName = project.getName();
        String groupName = group.getName();
        addBreadCrumb(groupName, projectName, datasetName, model);

        String virtualRootPath = filePathService.createVirtualPath(group.getId(), project.getId(), datasetId);

        model.addAttribute(ROOT_NAME_ATTR, datasetName);
        model.addAttribute(ROOT_PATH_ATTR, virtualRootPath);

        model.addAttribute(GROUP_ATTR, group);
        model.addAttribute(PROJECT_ATTR, project);
        model.addAttribute(DATASET_ATTR, dataset);

        return BROWSE_VIEW;
    }

    @RequestMapping(value = "/directory/create", method = RequestMethod.GET)
    public String createDirectoryRender(@RequestParam(FILE_PATH_ATTR) String virtualPath, Model model)
    {
        model.addAttribute(FILE_PATH_ATTR, virtualPath);
        model.addAttribute(DIRECTORY_NAME_ATTR, new DirectoryName());
        return CREATE_DIR_VIEW;
    }

    @RequestMapping(value = "/createDirectory", method = RequestMethod.POST)
    public String createDirectoryPost(@RequestParam(FILE_PATH_ATTR) String virtualPath,
            @RequestParam(CONNECTION_ID_ATTR) Integer connectionId, Model model,
            @Valid @ModelAttribute(DIRECTORY_NAME_ATTR) DirectoryName directoryName, BindingResult result)
    {
        if (!result.hasErrors())
        {
            Path pathInfo = PathBuilder.buildFromString(virtualPath);
            String absolutePath = filePathService.resolveRelativePath(pathInfo);
            String directory = pathInfo.getFilePath();
            try
            {
                boolean createSuccess = getDmsService().createDir(connectionId, absolutePath,
                        directoryName.getDirectoryName());

                if (!createSuccess)
                {
                    String[] codes = {"manageDataset.directory.create.fail"};
                    Object[] args = {};
                    FieldError fieldError = new FieldError(FILE_PATH_ATTR, DIRECTORY_NAME_ATTR,
                            directoryName.getDirectoryName(), false, codes, args, "Failed to create directory");
                    result.addError(fieldError);
                    model.addAttribute(FILE_PATH_ATTR, virtualPath);
                    return CREATE_DIR_VIEW;
                }

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
            catch (Exception e)
            {
                String[] codes = {"manageDataset.directory.create.exception"};
                String errorMessage = "An error occured";
                if (e.getMessage() != null)
                {
                    errorMessage = e.getMessage();
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

    @RequestMapping(value = "/directory/delete", method = RequestMethod.GET)
    public String deleteDirectoryRender(@RequestParam(FILE_PATH_ATTR) String virtualPath, Model model)
    {
        FilePathInfo pathInfo = filePathService.parseVirtualPath(virtualPath);
        String directory = pathInfo.getDirectory();
        model.addAttribute(NAMED_PATH_ATTR, directory);
        model.addAttribute(FILE_PATH_ATTR, virtualPath);
        return DELETE_DIR_VIEW;
    }

    @RequestMapping(value = "/file/delete", method = RequestMethod.GET)
    public String deleteFileRender(@RequestParam(FILE_PATH_ATTR) String virtualPath, Model model)
    {
        FilePathInfo pathInfo = filePathService.parseVirtualPath(virtualPath);
        String directory = pathInfo.getDirectory();
        model.addAttribute(NAMED_PATH_ATTR, directory);
        model.addAttribute(FILE_PATH_ATTR, virtualPath);
        return DELETE_FILE_VIEW;
    }

    /**
     * Deletes files and directories
     */
    @RequestMapping(method = RequestMethod.POST, value = "/delete")
    public String delete(@RequestParam Integer connectionId, @RequestParam(FILE_PATH_ATTR) String virtualPath,
            @RequestParam(FILE_TYPE_ATTR) String fileType, Model model)
    {
        Path pathInfo = PathBuilder.buildFromString(virtualPath);
        String absolutePath = filePathService.resolveRelativePath(pathInfo);
        String directory = pathInfo.getFilePath();

        FileInfo fileToDelete = new FileInfo();
        fileToDelete.setAbsolutePath(absolutePath);
        List<FileInfo> fileInfos = new ArrayList<FileInfo>();
        fileInfos.add(fileToDelete);

        String errorMessage = "An error occured";
        boolean deleteSuccess = false;
        try
        {
            deleteSuccess = getDmsService().delete(connectionId, fileInfos);
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
        catch (Exception e)
        {
            if (e.getMessage() != null)
            {
                errorMessage = e.getMessage();
            }
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
            view = SUCCESS_AJAX_VIEW;
        }
        return view;
    }

    @RequestMapping(value = "/file/move", method = RequestMethod.GET)
    public String moveFileRender(@RequestParam(MOVING_PATH_ATTR) String movingPath,
            @RequestParam(DESTINATION_PATH_ATTR) String destinationPath, Model model)
    {
        Path movingPathInfo = PathBuilder.buildFromString(movingPath);
        Path destinationPathInfo = PathBuilder.buildFromString(destinationPath);

        addPathAttrToModel(movingPath, movingPathInfo, destinationPath, destinationPathInfo, model);
        return MOVE_FILE_VIEW;
    }

    @RequestMapping(value = "/move", method = RequestMethod.POST)
    public String moveFile(@RequestParam(CONNECTION_ID_ATTR) Integer connectionId,
            @RequestParam(MOVING_PATH_ATTR) String movingPath,
            @RequestParam(DESTINATION_PATH_ATTR) String destinationPath, Model model)
    {
        Path movingPathInfo = PathBuilder.buildFromString(movingPath);
        String movingAbsolutePath = filePathService.resolveRelativePath(movingPathInfo);
        

        Path destinationPathInfo = PathBuilder.buildFromString(destinationPath);
        String destinationAbsolutePath = filePathService.resolveRelativePath(destinationPathInfo);

        String executionError = performDelete(connectionId, movingAbsolutePath, destinationAbsolutePath);

        if (executionError != null)
        {
            addPathAttrToModel(movingPath, movingPathInfo, destinationPath, destinationPathInfo, model);
            model.addAttribute(ERROR_ATTR, executionError);
            return MOVE_FILE_VIEW;
        }

        return SUCCESS_AJAX_VIEW;
    }

    private String performDelete(Integer connectionId, String movingAbsolutePath, String destinationAbsolutePath)
    {
        String username = securityContextFacade.getAuthorizedUsername();
        Integer destinationConnection = connectLocal();

        List<String> copySource = new ArrayList<String>(1);
        copySource.add(movingAbsolutePath);

        Long jobId = getDmsService().copy(username, connectionId, copySource, destinationConnection,
                destinationAbsolutePath);
        String executionError = null;
        LOG.info("movingAbsolutePath " + movingAbsolutePath);
        LOG.info("destinationAbsolutePath " + destinationAbsolutePath);
        
        try
        {
            while (true)
            {

                JobItem jobItem = getDmsService().getJobStatus(username, jobId);
                String status = jobItem.getStatus();
                LOG.info("Deletion job status " + status);
                if (JobStatus.FINISHED.toString().equals(status))
                {
                    break;
                }
                if (JobStatus.ABORTED.toString().equals(status))
                {
                    executionError = "Move aborted";
                    break;
                }
                if (JobStatus.CANCELLED.toString().equals(status))
                {
                    executionError = "Move cancelled";
                    break;
                }
                // TODO CHECKSTYLE-OFF: Magic Number
                Thread.sleep(100);
            }

            List<FileInfo> filesToDelete = new ArrayList<FileInfo>(1);
            FileInfo srcToDelete = new FileInfo();
            srcToDelete.setAbsolutePath(movingAbsolutePath);
            filesToDelete.add(srcToDelete);
            boolean deleteSuccess = getDmsService().delete(connectionId, filesToDelete);
            if (!deleteSuccess)
            {
                executionError = "Move failed during delete phase";
            }
        }
        catch (Exception e)
        {            
            executionError = "" + e.getMessage();
        }
        return executionError;

    }

    private void addPathAttrToModel(String movingPath, Path movingPathInfo, String destinationPath,
            Path destinationPathInfo, Model model)
    {
        String movingNamedpath = movingPathInfo.getFilePath();
        String destinationNamedpath = destinationPathInfo.getFilePath();
        
        if (StringUtils.isEmpty(destinationNamedpath))
        {
            destinationNamedpath = "Dataset top level";
        }

        model.addAttribute(MOVING_NAMED_PATH_ATTR, movingNamedpath);
        model.addAttribute(DESTINATION_NAMED_PATH_ATTR, destinationNamedpath);
        model.addAttribute(MOVING_PATH_ATTR, movingPath);
        model.addAttribute(DESTINATION_PATH_ATTR, destinationPath);

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
