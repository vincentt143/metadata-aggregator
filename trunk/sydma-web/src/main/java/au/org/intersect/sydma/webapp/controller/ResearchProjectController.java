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

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

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

import au.org.intersect.sydma.webapp.controller.propertyeditor.ResearchSubjectCodePropertyEditor;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchProject;
import au.org.intersect.sydma.webapp.domain.ResearchSubjectCode;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.permission.PermissionType;
import au.org.intersect.sydma.webapp.service.PermissionService;
import au.org.intersect.sydma.webapp.util.Breadcrumb;
import au.org.intersect.sydma.webapp.util.RifCsWriter;

/**
 * Research Project Controller.
 */
@RequestMapping("/researchproject/**")
@Controller
public class ResearchProjectController
{
    private static final String REDIRECT_TO_NEW = "researchproject/new";
    private static final String REDIRECT_TO_EDIT = "researchproject/edit";
    private static final String RESEARCH_PROJECT_REQUEST_MODEL = "researchProject";
    private static final String RESEARCH_GROUP_REQUEST_MODEL = "researchGroup";
    private static final String BREADCRUMBS = "breadcrumbs";

    private static List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> breadcrumbsForEditing = new ArrayList<Breadcrumb>();

    @Autowired
    private PermissionService permissionService;

    static
    {
        breadcrumbs.add(Breadcrumb.getHome());
        breadcrumbs.add(new Breadcrumb("Create Research Project"));
        breadcrumbsForEditing.add(Breadcrumb.getHome());
        breadcrumbsForEditing.add(new Breadcrumb("Edit Research Project"));
    }

    @Autowired
    private RifCsWriter rifCsWriter;
    

    @InitBinder
    public void initDataBinder(WebDataBinder binder)
    {
        binder.registerCustomEditor(ResearchSubjectCode.class, new ResearchSubjectCodePropertyEditor());     
    }

    /**
     * For ResearchProject we always initialize them with an empty ResearchGroup first, workaround for the NotNull
     * validation constraint when we only want to retrieve and set the real one later
     */
    @ModelAttribute("researchProject")
    public ResearchProject initResearchProject()
    {
        ResearchProject emptyModel = new ResearchProject();
        ResearchGroup researchGroup = new ResearchGroup();
        researchGroup.associateWithResearchProject(emptyModel);
        return emptyModel;
    }

    @RequestMapping(value = "/new/{id}", method = RequestMethod.GET)
    public String newResearchGroup(@PathVariable("id") Long groupId, final Model model, Principal principal)
    {
        return permissionService.canGroup(PermissionType.CREATE_PROJECT, groupId, principal,
                new PermissionService.ResearchGroupAction()
                {
                    @Override
                    public String act(ResearchGroup group, User user)
                    {
                        model.addAttribute(RESEARCH_GROUP_REQUEST_MODEL, group);
                        ResearchProject project = new ResearchProject();
                        project.setSubjectCode(group.getSubjectCode());
                        project.setSubjectCode2(group.getSubjectCode2());
                        project.setSubjectCode3(group.getSubjectCode3());

                        model.addAttribute(BREADCRUMBS, breadcrumbs);
                        model.addAttribute(RESEARCH_PROJECT_REQUEST_MODEL, project);
                        return REDIRECT_TO_NEW;
                    }
                });

    }

    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public String createResearchProject(@RequestParam("groupId") final Long groupId,
            @Valid final ResearchProject researchProject, final BindingResult result, final Model model,
            Principal principal)
    {
        return permissionService.canGroup(PermissionType.CREATE_PROJECT, groupId, principal,
                new PermissionService.ResearchGroupAction()
                {
                    @Override
                    public String act(ResearchGroup researchGroup, User user)
                    {
                        if (result.hasErrors())
                        {
                            model.addAttribute(BREADCRUMBS, breadcrumbs);
                            model.addAttribute(RESEARCH_GROUP_REQUEST_MODEL, researchGroup);
                            return REDIRECT_TO_NEW;
                        }

                        if (researchGroup == null)
                        {
                            throw new EntityNotFoundException("ResearchGroup with id [" + groupId
                                    + "] could not be found");
                        }
                        researchGroup.associateWithResearchProject(researchProject);

                        if (researchProject.isDuplicate())
                        {
                            String[] nameErrorCode = {""};
                            String[] nameErrorArg = {""};
                            FieldError nameError = new FieldError(RESEARCH_PROJECT_REQUEST_MODEL, "name",
                                    researchProject.getName(), true, nameErrorCode, nameErrorArg,
                                    "Project already exists under this group");
                            result.addError(nameError);
                            model.addAttribute(BREADCRUMBS, breadcrumbs);
                            model.addAttribute(RESEARCH_GROUP_REQUEST_MODEL, researchGroup);
                            return REDIRECT_TO_NEW;
                        }

                        researchProject.persist();
                        return "redirect:/";
                    }
                });

    }

    @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
    public String editResearchProject(@PathVariable("id") Long id, final Model model, java.security.Principal principal)
    {
        return permissionService.canProject(PermissionType.EDIT_PROJECT, id, principal,
                new PermissionService.ResearchProjectAction()
                {
                    @Override
                    public String act(ResearchProject project, User user)
                    {
                        model.addAttribute(BREADCRUMBS, breadcrumbsForEditing);
                        model.addAttribute(RESEARCH_PROJECT_REQUEST_MODEL, project);
                        return REDIRECT_TO_EDIT;
                    }
                });
    }

    @RequestMapping(value = "/editResearchProject", method = RequestMethod.PUT)
    public String editResearchProject(@RequestParam(value = "id") final Long id,
            @Valid final ResearchProject researchProject, final BindingResult result, final Model model,
            final java.security.Principal principal)
    {
        return permissionService.canProject(PermissionType.EDIT_PROJECT, id, principal,
                new PermissionService.ResearchProjectAction()
                {
                    @Override
                    public String act(ResearchProject persisted, User user)
                    {
                        if (result.hasErrors())
                        {
                            model.addAttribute(RESEARCH_PROJECT_REQUEST_MODEL, researchProject);
                            return REDIRECT_TO_EDIT;
                        }

                        ResearchGroup group = persisted.getResearchGroup();
                        researchProject.setResearchGroup(group);

                        if (researchProject.isDuplicate())
                        {
                            String[] nameErrorCode = {""};
                            String[] nameErrorArg = {""};
                            FieldError nameError = new FieldError("researchDataset", "name", researchProject.getName(),
                                    true, nameErrorCode, nameErrorArg, "Project already exists under this group");
                            result.addError(nameError);
                            return REDIRECT_TO_EDIT;
                        }

                        researchProject.merge();

                        persisted.updateRifCsIfNeeded(rifCsWriter);

                        return "redirect:/";
                    }
                });

    }

    @RequestMapping
    public String index()
    {
        return "researchproject/index";
    }

}
