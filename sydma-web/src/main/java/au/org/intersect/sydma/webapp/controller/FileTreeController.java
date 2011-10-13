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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.json.JsonFileInfo;
import au.org.intersect.sydma.webapp.json.JsonResponse;
import au.org.intersect.sydma.webapp.permission.dataset.DatasetPermissionQuery;
import au.org.intersect.sydma.webapp.permission.group.GroupPermissionQuery;
import au.org.intersect.sydma.webapp.permission.path.Path;
import au.org.intersect.sydma.webapp.permission.path.PathBuilder;
import au.org.intersect.sydma.webapp.permission.project.ProjectPermissionQuery;
import au.org.intersect.sydma.webapp.service.FilePathService;
import au.org.intersect.sydma.webapp.service.PermissionService;

/**
 * Controller that handles file browsing ajax calls from FileTree js class
 */
@RequestMapping("/filetree/**")
@Controller
public class FileTreeController extends AbstractControllerWithDmsConnection
{
    private static final Logger LOG = LoggerFactory.getLogger(FileTreeController.class);

    @Autowired
    private FilePathService filePathService;
    
    @Autowired
    private PermissionService permissionService;

    @RequestMapping(method = RequestMethod.POST, value = "/connect")
    @ResponseBody
    public String createConnection()
    {
        Integer connectionLocal = connectLocal();
        JsonResponse response = new JsonResponse(connectionLocal, null);
        return response.toJson();
    }

    @RequestMapping(value = "/listDownload")
    @ResponseBody
    public String listDestination(@RequestParam(value = "path") String pathString,
            @RequestParam("connectionId") Integer connectionId, Model model, Principal principal)
    {

        Path path = PathBuilder.buildFromString(pathString);
        List<JsonFileInfo> fileList = null;

        if (path.isDatasetPath() || path.isFilePath())
        {
            fileList = listUploadDirectory(path, connectionId, principal);
        }
        else if (path.isProjectPath())
        {
            fileList = listDownloadDatasets(path, principal);
        }
        else if (path.isGroupPath())
        {
            fileList = listDownloadProjects(path, principal);
        }
        else
        {
            fileList = listDownloadGroups(principal);
        }

        JsonResponse response = new JsonResponse(fileList, null);
        return response.toJson();
    }

    @RequestMapping(value = "/listUpload")
    @ResponseBody
    public String listUploadDestination(@RequestParam(value = "path") String pathString,
            @RequestParam("connectionId") Integer connectionId, Model model, Principal principal)
    {

        Path path = PathBuilder.buildFromString(pathString);
        List<JsonFileInfo> fileList = null;

        if (path.isDatasetPath() || path.isFilePath())
        {
            fileList = listUploadDirectory(path, connectionId, principal);
        }
        else if (path.isProjectPath())
        {
            fileList = listUploadDatasets(path, principal);
        }
        else if (path.isGroupPath())
        {
            fileList = listUploadProjects(path, principal);
        }
        else
        {
            fileList = listUploadGroups(principal);
        }

        JsonResponse response = new JsonResponse(fileList, null);
        return response.toJson();
    }
    
    private List<JsonFileInfo> listUploadGroups(Principal principal)
    {
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();

        GroupPermissionQuery groupQuery = permissionService.getUploadGroupPermission(user);

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

    private List<JsonFileInfo> listUploadProjects(Path path, Principal principal)
    {
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();

        ProjectPermissionQuery projectQuery = permissionService.getUploadProjectPermission(user, path);

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

    
    private List<JsonFileInfo> listUploadDatasets(Path path, Principal principal)
    {
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();

        DatasetPermissionQuery datasetQuery = permissionService.getUploadDatasetPermissions(user, path);

        List<JsonFileInfo> entityList = new ArrayList<JsonFileInfo>();
        for (ResearchDataset researchDataset : ResearchDataset.findDatasetsWithPermission(datasetQuery))
        {
            Path virtualPath = PathBuilder.datasetPath(researchDataset);
            JsonFileInfo entityFile = new JsonFileInfo(researchDataset.getName(), virtualPath.getPath(), 
                    JsonFileInfo.DATASET_TYPE, "", null);
            entityList.add(entityFile);
        }
        return entityList;
    }

    private List<JsonFileInfo> listUploadDirectory(Path filePath, Integer connectionId, Principal principal)
    {
        String uploadRelativePath = filePathService.resolveRelativePath(filePath);
        LOG.info("Listing destination with connection {} relativeUploadPath {}", connectionId, uploadRelativePath);
        List<FileInfo> fileInfoList = getDmsService().getList(connectionId, uploadRelativePath);

        List<JsonFileInfo> jsonFileInfoList = new ArrayList<JsonFileInfo>();
        for (FileInfo fileInfo : fileInfoList)
        {
            String uploadPath = fileInfo.getAbsolutePath();
            String virtualPath = filePathService.relativeToVirtualPath(filePath, uploadPath);

            JsonFileInfo jsonFileInfo = new JsonFileInfo(fileInfo.getName(), virtualPath, fileInfo.getFileType()
                    .toString(), fileInfo.getModificationDate(), fileInfo.getSize());
            jsonFileInfoList.add(jsonFileInfo);
        }
        return jsonFileInfoList;
    }

    private List<JsonFileInfo> listDownloadDatasets(Path path, Principal principal)
    {
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();

        DatasetPermissionQuery datasetQuery = permissionService.getDownloadDatasetPermissions(user, path);

        List<JsonFileInfo> entityList = new ArrayList<JsonFileInfo>();
        for (ResearchDataset researchDataset : ResearchDataset.findDatasetsWithPermission(datasetQuery))
        {
            Path virtualPath = PathBuilder.datasetPath(researchDataset);
            JsonFileInfo entityFile = new JsonFileInfo(researchDataset.getName(), virtualPath.getPath(), 
                    JsonFileInfo.DATASET_TYPE, "", null);
            entityList.add(entityFile);
        }
        return entityList;
    }

    private List<JsonFileInfo> listDownloadProjects(Path path, Principal principal)
    {
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();

        ProjectPermissionQuery projectQuery = permissionService.getDownloadProjectPermission(user, path);

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

    private List<JsonFileInfo> listDownloadGroups(Principal principal)
    {
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();

        GroupPermissionQuery groupQuery = permissionService.getDownloadGroupPermission(user);

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


}
