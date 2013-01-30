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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.sydma.webapp.domain.FileAnnotation;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.json.JsonFileInfo;
import au.org.intersect.sydma.webapp.json.JsonResponse;
import au.org.intersect.sydma.webapp.permission.PermissionType;
import au.org.intersect.sydma.webapp.permission.dataset.DatasetPermissionQuery;
import au.org.intersect.sydma.webapp.permission.group.GroupPermissionQuery;
import au.org.intersect.sydma.webapp.permission.path.Path;
import au.org.intersect.sydma.webapp.permission.path.PathBuilder;
import au.org.intersect.sydma.webapp.permission.project.ProjectPermissionQuery;
import au.org.intersect.sydma.webapp.service.ApplicationType;
import au.org.intersect.sydma.webapp.service.ApplicationTypeService;
import au.org.intersect.sydma.webapp.service.FilePathService;
import au.org.intersect.sydma.webapp.service.PermissionService;

/**
 * Controller that handles file browsing ajax calls from FileTree js class
 * 
 * Some terminologies dmsPath do not end with /, ie, abc/def instead of abc/def/ virtualPath refer to
 * /{grpid}/{prjid}/{datasetid}/{a}/{b}/{c} absolutePath refer to /uploadPath/grpDir/dataDir/{a}/{b}/{c} named path
 * refer to datasetName/a/b/c
 */
@RequestMapping("/filetree/**")
@Controller
public class FileTreeController extends AbstractControllerWithDmsConnection
{
    private static final Logger LOG = LoggerFactory.getLogger(FileTreeController.class);

    private final List<PermissionType> directoryActions = new ArrayList<PermissionType>();

    @Autowired
    private FilePathService filePathService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private ReloadableResourceBundleMessageSource messageSource;

    public FileTreeController(ApplicationTypeService applicationTypeService)
    {
        super();
        directoryActions.add(PermissionType.CREATE_DIRECTORY);
        directoryActions.add(PermissionType.DELETE_DIRECTORY);
        directoryActions.add(PermissionType.DELETE_FILE);
        directoryActions.add(PermissionType.MOVE_DIRECTORY_FILE);
        directoryActions.add(PermissionType.ASSIGN_DIRECTORY_PERMISSION);
        if (applicationTypeService.applicationIs(ApplicationType.AGR_ENV))
        {
            directoryActions.add(PermissionType.CREATE_ANNOTATION);
            directoryActions.add(PermissionType.EDIT_ANNOTATION);
            directoryActions.add(PermissionType.DELETE_ANNOTATION);
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/connect")
    @ResponseBody
    public String createConnection(Principal principal)
    {
        Integer connectionLocal = connectLocal(principal.getName());
        JsonResponse response = new JsonResponse(connectionLocal, null);
        return response.toJson();
    }

    /**
     * Returns json tree info for the source
     * 
     * @param dmsPath
     * @param connectionId
     * @param model
     * @param principal
     * @return
     */
    @RequestMapping(value = "/listDownload")
    @ResponseBody
    public String listDownload(@RequestParam(value = "path") String dmsPath,
            @RequestParam("connectionId") Integer connectionId, Model model, Principal principal)
    {

        Path path = PathBuilder.buildFromString(dmsPath);
        List<JsonFileInfo> fileList = null;
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();

        if (path.isDatasetPath() || path.isFilePath())
        {
            fileList = listDownloadDirectory(path, connectionId, user);
        }
        else if (path.isProjectPath())
        {
            fileList = listDownloadDatasets(path, user);
        }
        else if (path.isGroupPath())
        {
            fileList = listDownloadProjects(path, user);
        }
        else
        {
            fileList = listDownloadGroups(user);
        }
        JsonResponse response = new JsonResponse(fileList, null);
        return response.toJson();
    }

    /**
     * returns json tree for destination
     * 
     * @param dmsPath
     * @param connectionId
     * @param model
     * @param principal
     * @return
     */
    @RequestMapping(value = "/listUpload")
    @ResponseBody
    public String listDestination(@RequestParam(value = "path") String dmsPath,
            @RequestParam("connectionId") Integer connectionId, Model model, Principal principal)
    {

        Path path = PathBuilder.buildFromString(dmsPath);
        List<JsonFileInfo> fileList = null;
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();

        if (path.isDatasetPath() || path.isFilePath())
        {
            fileList = listUploadDirectory(path, connectionId, user);
        }
        else if (path.isProjectPath())
        {
            fileList = listUploadDatasets(path, user);
        }
        else if (path.isGroupPath())
        {
            fileList = listUploadProjects(path, user);
        }
        else
        {
            fileList = listUploadGroups(user);
        }

        JsonResponse response = new JsonResponse(fileList, null);
        return response.toJson();
    }

    private List<JsonFileInfo> listUploadGroups(User user)
    {
        GroupPermissionQuery groupQuery = permissionService.getUploadGroupPermission(user);
        return buildGroupJson(groupQuery);
    }

    private List<JsonFileInfo> listDownloadGroups(User user)
    {
        GroupPermissionQuery groupQuery = permissionService.getDownloadGroupPermission(user);
        return buildGroupJson(groupQuery);
    }

    private List<JsonFileInfo> buildGroupJson(GroupPermissionQuery groupQuery)
    {
        List<JsonFileInfo> entityList = new ArrayList<JsonFileInfo>();
        for (ResearchGroup researchGroup : ResearchGroup.findResearchGroupWithPermission(groupQuery))
        {
            String virtualPath = filePathService.createVirtualPath(researchGroup.getId());
            JsonFileInfo entityFile = new JsonFileInfo(researchGroup.getName(), virtualPath, JsonFileInfo.ENTITY_TYPE,
                    "", null);
            entityList.add(entityFile);
        }
        return entityList;
    }

    private List<JsonFileInfo> listUploadProjects(Path path, User user)
    {
        ProjectPermissionQuery projectQuery = permissionService.getUploadProjectPermission(user, path);
        return buildProjectsJson(projectQuery);
    }

    private List<JsonFileInfo> listDownloadProjects(Path path, User user)
    {
        ProjectPermissionQuery projectQuery = permissionService.getDownloadProjectPermission(user, path);
        return buildProjectsJson(projectQuery);
    }

    private List<JsonFileInfo> buildProjectsJson(ProjectPermissionQuery projectQuery)
    {
        List<JsonFileInfo> entityList = new ArrayList<JsonFileInfo>();
        for (ResearchProject researchProject : ResearchProject.findProjectWithPermission(projectQuery))
        {
            Path virtualPath = PathBuilder.projectPath(researchProject);
            JsonFileInfo entityFile = new JsonFileInfo(researchProject.getName(), virtualPath.getPath(),
                    JsonFileInfo.ENTITY_TYPE, "", null);
            entityList.add(entityFile);
        }
        return entityList;
    }

    private List<JsonFileInfo> listUploadDatasets(Path path, User user)
    {
        DatasetPermissionQuery datasetQuery = permissionService.getUploadDatasetPermissions(user, path);
        return buildDatasetsJson(datasetQuery, user);
    }

    private List<JsonFileInfo> listDownloadDatasets(Path path, User user)
    {
        DatasetPermissionQuery datasetQuery = permissionService.getDownloadDatasetPermissions(user, path);
        return buildDatasetsJson(datasetQuery, user);
    }

    private List<JsonFileInfo> buildDatasetsJson(DatasetPermissionQuery datasetQuery, User user)
    {
        List<JsonFileInfo> entityList = new ArrayList<JsonFileInfo>();
        for (ResearchDataset researchDataset : ResearchDataset.findDatasetsWithPermission(datasetQuery))
        {
            Path virtualPath = PathBuilder.datasetPath(researchDataset);
            boolean canUpload = permissionService.hasEditingAccessPermissionForDataset(user, researchDataset);
            JsonFileInfo entityFile = new JsonFileInfo(researchDataset.getName(), virtualPath.getPath(),
                    JsonFileInfo.DATASET_TYPE, "", null, canUpload);
            entityList.add(entityFile);
        }
        return entityList;
    }

    private List<JsonFileInfo> listUploadDirectory(Path filePath, Integer connectionId, User user)
    {
        String virtualPath = filePath.getPath();

        String dmsAbsolutePath = filePathService.resolveToRelativePath(filePath);
        List<FileInfo> fileInfoList = getDmsService().getList(connectionId, dmsAbsolutePath);

        List<JsonFileInfo> jsonFileInfoList = new ArrayList<JsonFileInfo>();
        for (FileInfo fileInfo : fileInfoList)
        {
            String childAbsolutePath = fileInfo.getAbsolutePath();
            String childVirtualPath = filePathService.relativeToVirtualPath(filePath, childAbsolutePath);

            if (permissionService.canDirectoryAction(PermissionType.VIEW_DIRECTORY,
                    PathBuilder.buildFromString(childVirtualPath), user))
            {
                boolean canUpload = permissionService.hasEditingAccessPermissionForDirectory(user, childVirtualPath);
                List<String> allowedActions = getAllowedActions(virtualPath, user);
                JsonFileInfo jsonFileInfo = new JsonFileInfo(fileInfo.getName(), childVirtualPath, fileInfo
                        .getFileType().toString(), fileInfo.getModificationDate(), fileInfo.getSize(), allowedActions,
                        canUpload);

                jsonFileInfoList.add(jsonFileInfo);
            }
        }
        return jsonFileInfoList;
    }

    private List<JsonFileInfo> listDownloadDirectory(Path filePath, Integer connectionId, User user)
    {

        String virtualPath = filePath.getPath();
        List<FileAnnotation> fileAnnotations = listAnnotationsUnderDirectory(virtualPath);

        String dmsAbsolutePath = filePathService.resolveToRelativePath(filePath);
        List<FileInfo> fileInfoList = getDmsService().getList(connectionId, dmsAbsolutePath);

        List<JsonFileInfo> jsonFileInfoList = new ArrayList<JsonFileInfo>();
        for (FileInfo fileInfo : fileInfoList)
        {
            String childAbsolutePath = fileInfo.getAbsolutePath();
            String childVirtualPath = filePathService.relativeToVirtualPath(filePath, childAbsolutePath);
            if (permissionService.canDirectoryAction(PermissionType.VIEW_DIRECTORY,
                    PathBuilder.buildFromString(childVirtualPath), user))
            {
                List<String> allowedActions = getAllowedActions(childVirtualPath, user);
                JsonFileInfo jsonFileInfo = new JsonFileInfo(fileInfo.getName(), childVirtualPath, fileInfo
                        .getFileType().toString(), fileInfo.getModificationDate(), fileInfo.getSize(), allowedActions,
                        false);

                FileAnnotation fileAnnotation = getFileAnnotation(childVirtualPath, fileAnnotations);
                if (fileAnnotation != null)
                {
                    setAnnotation(jsonFileInfo, fileAnnotation);
                }

                jsonFileInfoList.add(jsonFileInfo);
            }
        }
        return jsonFileInfoList;

    }

    private void setAnnotation(JsonFileInfo jsonFileInfo, FileAnnotation fileAnnotation)
    {
        String annotation = fileAnnotation.getAnnotation();

        if (fileAnnotation.isOutOfDate())
        {
            annotation = annotation
                    + "\n"
                    + messageSource.getMessage("fileAnnotation.outOfDate", new Object[] {},
                            "Annotation may be out of date", null);
        }

        jsonFileInfo.setAnnotation(annotation);
    }

    private FileAnnotation getFileAnnotation(String virtualPath, List<FileAnnotation> fileAnnotations)
    {
        // TODO: Change this into accessing a map probably
        for (FileAnnotation annotation : fileAnnotations)
        {
            if (virtualPath.equals(annotation.getPath()))
            {
                return annotation;
            }
        }
        return null;
    }

    private List<FileAnnotation> listAnnotationsUnderDirectory(String directoryPath)
    {
        return FileAnnotation.findFileAnnotationsByParentPath(directoryPath).getResultList();
    }

    private List<String> getAllowedActions(String path, User user)
    {
        List<PermissionType> allowedActions = permissionService.restrictDirectoryActions(directoryActions, path, user);

        List<String> allowedActionList = new ArrayList<String>(allowedActions.size());
        for (PermissionType permission : allowedActions)
        {
            allowedActionList.add(permission.getPermissionTypeName());
        }
        return allowedActionList;
    }

}
