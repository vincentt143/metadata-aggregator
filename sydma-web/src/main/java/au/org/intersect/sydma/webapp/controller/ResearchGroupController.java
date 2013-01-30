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
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import au.org.intersect.sydma.webapp.controller.propertyeditor.ResearchSubjectCodePropertyEditor;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchSubjectCode;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.exception.NoneUniqueNameException;
import au.org.intersect.sydma.webapp.permission.PermissionType;
import au.org.intersect.sydma.webapp.service.PermissionService;
import au.org.intersect.sydma.webapp.service.PermissionServiceImpl;
import au.org.intersect.sydma.webapp.service.ResearchGroupService;
import au.org.intersect.sydma.webapp.util.Breadcrumb;
import au.org.intersect.sydma.webapp.util.RifCsWriter;
import au.org.intersect.sydma.webapp.util.TokenInputHelper;

/**
 * Research Group Controller.
 */
@RequestMapping("/researchgroup/**")
@Controller
public class ResearchGroupController
{
    private static final String VOCABULARY = "vocabulary";
    private static final String VIEW_PAGE = "researchgroup/view";
    private static final String EDIT_PAGE = "researchgroup/edit";
    private static final String RESEARCH_GROUP_REQUEST_MODEL = "researchGroup";

    private static List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> breadcrumbsForEditing = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> breadcrumbsForViewing = new ArrayList<Breadcrumb>();

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionServiceImpl.class);

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private TokenInputHelper tokenInputHelper;
    
    @Autowired
    private ResearchGroupService researchGroupService;

    static
    {
        breadcrumbs.add(Breadcrumb.getHome());
        breadcrumbs.add(new Breadcrumb("sections.group.create.title"));
        breadcrumbsForEditing.add(Breadcrumb.getHome());
        breadcrumbsForEditing.add(new Breadcrumb("sections.group.edit.title"));
        breadcrumbsForViewing.add(Breadcrumb.getHome());
        breadcrumbsForViewing.add(new Breadcrumb("sections.group.view.title"));
    }

    @Autowired
    private RifCsWriter rifCsWriter;

    @InitBinder
    public void initDataBinder(WebDataBinder binder)
    {
        binder.registerCustomEditor(ResearchSubjectCode.class, new ResearchSubjectCodePropertyEditor());
    }

    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
    public String viewResearchGroup(@PathVariable("id") Long id, final Model model, Principal principal)
    {
        return permissionService.canViewGroup(id, principal, new PermissionService.ResearchGroupAction()
        {
            @Override
            public String act(ResearchGroup researchGroup, User user)
            {
                model.addAttribute(Breadcrumb.BREADCRUMBS, breadcrumbsForViewing);
                model.addAttribute(RESEARCH_GROUP_REQUEST_MODEL, researchGroup);
                return VIEW_PAGE;
            }
        });

    }

    @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
    public String editResearchGroup(@PathVariable("id") Long id, final Model model, java.security.Principal principal)
    {
        return permissionService.canGroup(PermissionType.EDIT_GROUP, id, principal,
                new PermissionService.ResearchGroupAction()
                {
                    @Override
                    public String act(ResearchGroup group, User user)
                    {
                        model.addAttribute(VOCABULARY, tokenInputHelper.appendJson(group.getKeywords()));
                        model.addAttribute("breadcrumbs", breadcrumbsForEditing);
                        model.addAttribute(RESEARCH_GROUP_REQUEST_MODEL, group);
                        return EDIT_PAGE;
                    }

                });
    }

    @RequestMapping(value = "/editResearchGroup", method = RequestMethod.PUT)
    public String editResearchGroup(@RequestParam(value = "id") final Long id,
            @RequestParam(VOCABULARY) final String keywords, @Valid final ResearchGroup researchGroup,
            final BindingResult result, final Model model, final HttpServletRequest request, Principal principal)
    {
        LOGGER.info("Binding result: " + result);
        return permissionService.canGroup(PermissionType.EDIT_GROUP, id, principal,
                new PermissionService.ResearchGroupAction()
                {
                    @Override
                    public String act(ResearchGroup group, User user)
                    {
                        if (!group.getVersion().equals(researchGroup.getVersion()))
                        {
                            model.addAttribute(VOCABULARY,
                                    tokenInputHelper.buildJsonOnValidationError(keywords, researchGroup));
                            model.addAttribute(RESEARCH_GROUP_REQUEST_MODEL, researchGroup);
                            model.addAttribute("version_error", true);
                            return EDIT_PAGE;
                        }

                        if (!result.hasErrors())
                        {
                            try
                            {
                                researchGroupService.editGroup(researchGroup, keywords);
                                return "redirect:/";
                            }
                            catch (NoneUniqueNameException e)
                            {
                                String[] nameErrorCode = {""};
                                String[] nameErrorArg = {""};
                                FieldError nameError = new FieldError("researchGroup", "name", researchGroup.getName(),
                                        true, nameErrorCode, nameErrorArg, "Group already exists.");
                                result.addError(nameError);
                            }
                        }
                        // Proceed with error handling

                        model.addAttribute(VOCABULARY,
                                tokenInputHelper.buildJsonOnValidationError(keywords, researchGroup));
                        model.addAttribute(RESEARCH_GROUP_REQUEST_MODEL, researchGroup);
                        return EDIT_PAGE;
                    }
                });
    }

    @RequestMapping
    public String index()
    {
        return "researchgroup/index";
    }
}
