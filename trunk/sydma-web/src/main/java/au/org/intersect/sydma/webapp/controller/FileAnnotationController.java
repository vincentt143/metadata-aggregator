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

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.errors.PathNotFoundException;
import au.org.intersect.dms.core.service.DmsService;
import au.org.intersect.sydma.webapp.controller.propertyeditor.TrimString;
import au.org.intersect.sydma.webapp.domain.FileAnnotation;
import au.org.intersect.sydma.webapp.domain.ResearchDataset;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.permission.PermissionType;
import au.org.intersect.sydma.webapp.permission.path.Path;
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
@RequestMapping("/fileannotation/**")
@Controller
public class FileAnnotationController extends AbstractControllerWithDmsConnection
{
    private static final String ACCESS_DENIED = "accessDenied";
    private static final Logger LOG = LoggerFactory.getLogger(FileAnnotationController.class);
    private static final String CREATE_ANNOTATION_VIEW = "fileannotation/create";
    private static final String EDIT_ANNOTATION_VIEW = "fileannotation/edit";
    private static final String DELETE_ANNOTATION_VIEW = "fileannotation/delete";
    private static final String INVALID_PATH_VIEW = "fileannotation/invalidPath";
    private static final String ANNOTATION_EXIST_VIEW = "fileannotation/alreadyExist";
    private static final String ANNOTATION_DOES_NOT_EXIST_VIEW = "fileannotation/doesNotExist";

    private static final String ANNOTATION_PATH_ATTR = "filePath";
    private static final String FILE_ANNOTATION_ATTR = "fileAnnotation";
    private static final String NAMED_PATH_ATTR = "namedPath";

    private static final String SUBMIT_PARAM = "_submit";

    private static final String SUCCESS_AJAX_VIEW = "managedataset/success";

    @Autowired
    private FilePathService filePathService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private ApplicationTypeService applicationTypeService;
    

    @InitBinder
    public void setBinder(WebDataBinder dataBinder)
    {
        dataBinder.registerCustomEditor(String.class, new TrimString());
    }

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public String createAnnotationRender(@RequestParam(value = "connectionId") final Integer connectionId,
            @RequestParam(value = ANNOTATION_PATH_ATTR) final String dmsPath,
            @ModelAttribute(FILE_ANNOTATION_ATTR) final FileAnnotation fileAnnotation, final BindingResult results,
            final Model model, final Principal principal)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return ACCESS_DENIED;
        }

        return permissionService.canDirectory(PermissionType.CREATE_ANNOTATION, dmsPath, principal,
                new PermissionService.DirectoryAction()
                {
                    @Override
                    public String act(Path path, User user)
                    {

                        addNamedPath(path, model);
                        String virtualPath = path.getPath();

                        fileAnnotation.setPath(virtualPath);

                        if (!pathHasAnnotation(fileAnnotation))
                        {
                            return ANNOTATION_EXIST_VIEW;
                        }

                        FileInfo fileInfo = getFileInfo(connectionId, path);
                        if (fileInfo == null)
                        {
                            return INVALID_PATH_VIEW;
                        }

                        return CREATE_ANNOTATION_VIEW;
                    }
                });
    }

    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public String editAnnotationRender(@RequestParam(value = "connectionId") final Integer connectionId,
            @RequestParam(value = ANNOTATION_PATH_ATTR) final String dmsPath,
            @ModelAttribute(FILE_ANNOTATION_ATTR) final FileAnnotation fileAnnotation, final BindingResult results,
            final Model model, final Principal principal)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return ACCESS_DENIED;
        }

        return permissionService.canDirectory(PermissionType.CREATE_ANNOTATION, dmsPath, principal,
                new PermissionService.DirectoryAction()
                {
                    @Override
                    public String act(Path path, User user)
                    {
                        model.addAttribute(NAMED_PATH_ATTR, dmsPath);

                        FileAnnotation fileAnnotation = FileAnnotation.findFileAnnotationsByPath(path.getPath())
                                .getSingleResult();
                        if (fileAnnotation == null)
                        {
                            return ANNOTATION_DOES_NOT_EXIST_VIEW;
                        }

                        FileInfo fileInfo = getFileInfo(connectionId, path);
                        if (fileInfo == null)
                        {
                            return INVALID_PATH_VIEW;
                        }

                        model.addAttribute("fileAnnotation", fileAnnotation);
                        return EDIT_ANNOTATION_VIEW;
                    }
                });
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public String editAnnotationProcess(@RequestParam(value = "connectionId") final Integer connectionId,
            @RequestParam(value = SUBMIT_PARAM, required = false) final String submit, final Model model,
            final Principal principal,
            @Valid @ModelAttribute(FILE_ANNOTATION_ATTR) final FileAnnotation fileAnnotationRequest,
            final BindingResult results)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return ACCESS_DENIED;
        }

        if ("Edit".equals(submit))
        {
            String virtualPath = fileAnnotationRequest.getPath();
            return permissionService.canDirectory(PermissionType.CREATE_ANNOTATION, virtualPath, principal,
                    new PermissionService.DirectoryAction()
                    {
                        @Override
                        public String act(Path path, User user)
                        {
                            FileAnnotation fileAnnotation = FileAnnotation.findFileAnnotationsByPath(path.getPath())
                                    .getSingleResult();
                            
                            if (fileAnnotation == null)
                            {
                                return ANNOTATION_DOES_NOT_EXIST_VIEW;
                            }
                            if (results.hasErrors())
                            {
                                addNamedPath(path, model);
                                return EDIT_ANNOTATION_VIEW;
                            }

                            FileInfo fileInfo = getFileInfo(connectionId, path);
                            if (fileInfo == null)
                            {
                                addNamedPath(path, model);
                                return INVALID_PATH_VIEW;
                            }

                            // do update, mark it as no longer out of date
                            fileAnnotation.setAnnotation(fileAnnotationRequest.getAnnotation());
                            fileAnnotation.setOutOfDate(false);
                            fileAnnotation.persist();

                            return SUCCESS_AJAX_VIEW;
                        }
                    });
        }
        return SUCCESS_AJAX_VIEW;
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String createAnnotationProcess(@RequestParam(value = "connectionId") final Integer connectionId,
            @RequestParam(value = SUBMIT_PARAM, required = false) final String submit, final Model model,
            final Principal principal,
            @Valid @ModelAttribute(FILE_ANNOTATION_ATTR) final FileAnnotation fileAnnotation,
            final BindingResult results)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return ACCESS_DENIED;
        }

        if ("Create".equals(submit))
        {
            String virtualPath = fileAnnotation.getPath();
            return permissionService.canDirectory(PermissionType.CREATE_ANNOTATION, virtualPath, principal,
                    new PermissionService.DirectoryAction()
                    {
                        @Override
                        public String act(Path path, User user)
                        {
                            if (!pathHasAnnotation(fileAnnotation))
                            {
                                return ANNOTATION_EXIST_VIEW;
                            }

                            if (results.hasErrors())
                            {
                                addNamedPath(path, model);
                                return CREATE_ANNOTATION_VIEW;
                            }

                            FileInfo fileInfo = getFileInfo(connectionId, path);
                            if (fileInfo == null)
                            {
                                addNamedPath(path, model);
                                return INVALID_PATH_VIEW;
                            }

                            // do creation
                            fileAnnotation.persist();

                            return SUCCESS_AJAX_VIEW;
                        }
                    });
        }
        return SUCCESS_AJAX_VIEW;
    }
    

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public String deleteAnnotationRender(@RequestParam(value = "connectionId") final Integer connectionId,
            @RequestParam(value = ANNOTATION_PATH_ATTR) final String dmsPath,
            final Model model, final Principal principal)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return ACCESS_DENIED;
        }

        return permissionService.canDirectory(PermissionType.DELETE_ANNOTATION, dmsPath, principal,
                new PermissionService.DirectoryAction()
                {
                    @Override
                    public String act(Path path, User user)
                    {
                        addNamedPath(path, model);

                        FileAnnotation fileAnnotation = FileAnnotation.findFileAnnotationsByPath(path.getPath())
                                .getSingleResult();
                        if (fileAnnotation == null)
                        {
                            return ANNOTATION_DOES_NOT_EXIST_VIEW;
                        }

                        FileInfo fileInfo = getFileInfo(connectionId, path);
                        if (fileInfo == null)
                        {
                            return INVALID_PATH_VIEW;
                        }

                        model.addAttribute(FILE_ANNOTATION_ATTR, fileAnnotation);
                        return DELETE_ANNOTATION_VIEW;
                    }
                });
    }
    

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public String deleteAnnotationProcess(@RequestParam(value = "connectionId") final Integer connectionId,
            @RequestParam(value = SUBMIT_PARAM, required = false) final String submit, final Model model,
            final Principal principal,
            @Valid @ModelAttribute(FILE_ANNOTATION_ATTR) final FileAnnotation fileAnnotation,
            final BindingResult results)
    {
        if (applicationTypeService.applicationIs(ApplicationType.AGGREGATOR))
        {
            return ACCESS_DENIED;
        }

        if ("Delete".equals(submit))
        {
            String virtualPath = fileAnnotation.getPath();
            return permissionService.canDirectory(PermissionType.DELETE_ANNOTATION, virtualPath, principal,
                    new PermissionService.DirectoryAction()
                    {
                        @Override
                        public String act(Path path, User user)
                        {

                            FileAnnotation fileAnnotation = FileAnnotation.findFileAnnotationsByPath(path.getPath())
                                    .getSingleResult();
                            if (fileAnnotation == null)
                            {
                                return ANNOTATION_DOES_NOT_EXIST_VIEW;
                            }

                            FileInfo fileInfo = getFileInfo(connectionId, path);
                            if (fileInfo == null)
                            {
                                return INVALID_PATH_VIEW;
                            }
                            // do creation
                            fileAnnotation.remove();

                            return SUCCESS_AJAX_VIEW;
                        }
                    });
        }
        return SUCCESS_AJAX_VIEW;
    }
    


    private void addNamedPath(Path path, Model model)
    {
        String namedPath = getNamedPath(path);
        model.addAttribute(NAMED_PATH_ATTR, namedPath);
    }

    private boolean pathHasAnnotation(FileAnnotation fileAnnotation)
    {
        String path = fileAnnotation.getPath();
        if (FileAnnotation.findFileAnnotationsByPath(path).getResultList().size() > 0)
        {
            return false;
        }
        return true;
    }

    private FileInfo getFileInfo(Integer connectionId, Path path)
    {
        String fileRelativePath = filePathService.resolveToRelativePath(path);

        DmsService dmsService = this.getDmsService();
        try
        {
            return dmsService.getFileInfo(connectionId, fileRelativePath);
        }
        catch (PathNotFoundException e)
        {
            // file does not exist
            return null;
        }
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
                    && "/".equals(String.valueOf(directoryPath.charAt(directoryPath.length() - 1))))
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

}
