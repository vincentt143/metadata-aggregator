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
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import au.org.intersect.sydma.webapp.domain.MasterVocabulary;
import au.org.intersect.sydma.webapp.domain.MasterVocabularyTerm;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.domain.Vocabulary;
import au.org.intersect.sydma.webapp.permission.PermissionType;
import au.org.intersect.sydma.webapp.service.IndexingService;
import au.org.intersect.sydma.webapp.service.PermissionService;
import au.org.intersect.sydma.webapp.service.VocabularyService;
import au.org.intersect.sydma.webapp.util.Breadcrumb;
import au.org.intersect.sydma.webapp.util.ModelAndViewRedirectHelper;
import au.org.intersect.sydma.webapp.util.TokenInputHelper;

/**
 * Vocabulary Controller
 */
@Controller
public class VocabularyController
{
    private static final Logger LOGGER = LoggerFactory.getLogger(VocabularyController.class);

    private static final String MANAGE_VOCABULARY_PAGE = "researchgroup/managevocabulary";
    private static final String REDIRECT_MANAGE_VOCABULARY_PAGE = "/researchgroup/managevocabulary";
    private static final String REDIRECT_CREATE_VOCABULARY_PAGE = "/researchgroup/createvocabulary";

    private static final String VOCABULARY_ATTR = "vocabulary";
    private static final String KEYWORD_ATTR = "keyword";
    private static final String SUBMIT_PARAM = "_submit";
    private static final String RESEARCH_GROUP_REQUEST_MODEL = "researchGroup";

    private static List<Breadcrumb> breadcrumbsForVocabulary = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> breadcrumbsForEditingWord = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> breadcrumbsForDeleteVocabulary = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> breadcrumbsForCreateVocabulary = new ArrayList<Breadcrumb>();

    @Autowired
    private ModelAndViewRedirectHelper modelAndViewRedirectHelper;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private TokenInputHelper tokenInputHelper;

    @Autowired
    private IndexingService indexingService;

    @Autowired
    private VocabularyService vocabularyService;

    static
    {
        breadcrumbsForVocabulary.add(Breadcrumb.getHome());
        breadcrumbsForVocabulary.add(new Breadcrumb("sections.manage.vocabulary"));
        breadcrumbsForEditingWord.add(Breadcrumb.getHome());
        breadcrumbsForEditingWord.add(new Breadcrumb("sections.edit.vocabulary"));
        breadcrumbsForDeleteVocabulary.add(Breadcrumb.getHome());
        breadcrumbsForDeleteVocabulary.add(new Breadcrumb("sections.delete.vocabulary"));
        breadcrumbsForCreateVocabulary.add(Breadcrumb.getHome());
        breadcrumbsForCreateVocabulary.add(new Breadcrumb("sections.create.vocabulary"));
    }

    @RequestMapping(value = "researchgroup/managevocabulary/{id}", method = RequestMethod.GET)
    public ModelAndView manageVocabulary(@PathVariable("id") final Long id, final Model model, Principal principal,
            HttpServletRequest request, @RequestParam("browse") final Boolean browse, 
            final HttpServletResponse response)
    {
        String view = permissionService.canGroup(PermissionType.MANAGE_VOCABULARY, id, principal,
                new PermissionService.ResearchGroupAction()
                {
                    @Override
                    public String act(ResearchGroup researchGroup, User user)
                    {
                        List<Vocabulary> vocabularyList = Vocabulary.findVocabularysByResearchGroup(researchGroup)
                                .getResultList();
                        Boolean isEmptyList = vocabularyList.isEmpty();
                        if (isEmptyList && Boolean.FALSE.equals(browse))
                        {
                            String url = REDIRECT_CREATE_VOCABULARY_PAGE;
                            return url;
                        }

                        model.addAttribute(Breadcrumb.BREADCRUMBS, breadcrumbsForVocabulary);
                        model.addAttribute("isEmptyList", isEmptyList);
                        ResearchGroup group = ResearchGroup.findResearchGroup(id);
                        model.addAttribute(RESEARCH_GROUP_REQUEST_MODEL, group);
                        model.addAttribute("vocabularyList", vocabularyList);
                        Vocabulary vocabulary = new Vocabulary();
                        model.addAttribute(VOCABULARY_ATTR, vocabulary);

                        // response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                        // response.setHeader("Pragma", "no-cache");
                        // response.setDateHeader("Expires", 0);

                        return MANAGE_VOCABULARY_PAGE;
                    }
                });
        if (view.equals(REDIRECT_CREATE_VOCABULARY_PAGE))
        {
            return redirectToViewInstance(view, request, id);
        }
        else
        {
            return new ModelAndView(view);
        }
    }

    @RequestMapping(value = "researchgroup/addWord", method = RequestMethod.POST)
    public ModelAndView manageVocabulary(@RequestParam("groupId") final Long id, @Valid final Vocabulary word,
            final BindingResult result, final Model model, HttpServletRequest request, Principal principal)
    {
        String view = permissionService.canGroup(PermissionType.MANAGE_VOCABULARY, id, principal,
                new PermissionService.ResearchGroupAction()
                {
                    @Override
                    public String act(ResearchGroup researchGroup, User user)
                    {
                        word.clean();
                        if (word.isDuplicate())
                        {
                            String[] errorCode = {""};
                            String[] errorArg = {""};
                            FieldError error = new FieldError(VOCABULARY_ATTR, KEYWORD_ATTR, word.getKeyword(), true,
                                    errorCode, errorArg, "Keyword already exists in the vocabulary.");
                            result.addError(error);
                        }

                        if (result.hasErrors())
                        {
                            List<Vocabulary> vocabularyList = Vocabulary.findVocabularysByResearchGroup(researchGroup)
                                    .getResultList();
                            model.addAttribute("isEmptyList", vocabularyList.isEmpty());
                            model.addAttribute(Breadcrumb.BREADCRUMBS, breadcrumbsForVocabulary);
                            model.addAttribute(RESEARCH_GROUP_REQUEST_MODEL, researchGroup);
                            model.addAttribute(VOCABULARY_ATTR, word);
                            model.addAttribute("vocabularyList", vocabularyList);
                            return MANAGE_VOCABULARY_PAGE;
                        }
                        word.persist();

                        return REDIRECT_MANAGE_VOCABULARY_PAGE;
                    }
                });
        if (view.equals(REDIRECT_MANAGE_VOCABULARY_PAGE))
        {
            return redirectToViewInstance(view, request, id);
        }
        else
        {
            return new ModelAndView(view);
        }
    }

    @RequestMapping(value = "researchgroup/deleteword/{groupid}")
    public ModelAndView deleteWord(@PathVariable("groupid") final Long groupid, @RequestParam("id") final Long id,
            final Model model, Principal principal, HttpServletRequest request)
    {
        String view = permissionService.canGroup(PermissionType.MANAGE_VOCABULARY, groupid, principal,
                new PermissionService.ResearchGroupAction()
                {
                    @Override
                    public String act(ResearchGroup researchGroup, User user)
                    {
                        vocabularyService.removeTerm(researchGroup, id);

                        return REDIRECT_MANAGE_VOCABULARY_PAGE;
                    }
                });
        if (view.equals(REDIRECT_MANAGE_VOCABULARY_PAGE))
        {
            return redirectToViewInstance(view, request, groupid);
        }
        else
        {
            return new ModelAndView(view);
        }
    }

    @RequestMapping(value = "researchgroup/deletevocabulary", method = RequestMethod.GET)
    public String deleteVocabulary(@RequestParam("id") final Long id, final Model model, Principal principal,
            HttpServletRequest request)
    {
        return permissionService.canGroup(PermissionType.MANAGE_VOCABULARY, id, principal,
                new PermissionService.ResearchGroupAction()
                {
                    @Override
                    public String act(ResearchGroup researchGroup, User user)
                    {
                        model.addAttribute("id", id);
                        model.addAttribute(Breadcrumb.BREADCRUMBS, breadcrumbsForDeleteVocabulary);
                        return "researchgroup/deletevocabulary";
                    }
                });
    }

    @RequestMapping(value = "researchgroup/deletevocabulary", method = RequestMethod.POST)
    public ModelAndView deleteVocabulary(@RequestParam("id") final Long id, Principal principal, final Model model,
            HttpServletRequest request, @RequestParam(value = SUBMIT_PARAM, required = false) final String submit)
    {
        if (!"Delete All".equals(submit))
        {
            return redirectToViewInstance(REDIRECT_MANAGE_VOCABULARY_PAGE, request, id);
        }

        String view = permissionService.canGroup(PermissionType.MANAGE_VOCABULARY, id, principal,
                new PermissionService.ResearchGroupAction()
                {
                    @Override
                    public String act(ResearchGroup researchGroup, User user)
                    {
                        vocabularyService.removeVocabulary(researchGroup);
                        return REDIRECT_MANAGE_VOCABULARY_PAGE;
                    }
                });
        if (view.equals(REDIRECT_MANAGE_VOCABULARY_PAGE))
        {
            return redirectToViewInstance(view, request, id);
        }
        else
        {
            return new ModelAndView(view);
        }
    }

    @RequestMapping(value = "researchgroup/editword/{id}", method = RequestMethod.GET)
    public String editWord(@PathVariable("id") final Long id, final Model model, Principal principal,
            HttpServletRequest request)
    {
        final Long groupId = Vocabulary.findVocabulary(id).getResearchGroup().getId();
        return permissionService.canGroup(PermissionType.MANAGE_VOCABULARY, groupId, principal,
                new PermissionService.ResearchGroupAction()
                {
                    @Override
                    public String act(ResearchGroup researchGroup, User user)
                    {
                        Vocabulary word = Vocabulary.findVocabulary(id);
                        model.addAttribute("groupId", word.getResearchGroup().getId());
                        model.addAttribute(VOCABULARY_ATTR, word);
                        model.addAttribute(Breadcrumb.BREADCRUMBS, breadcrumbsForEditingWord);
                        return "researchgroup/editword";
                    }
                });
    }

    @RequestMapping(value = "researchgroup/editword", method = RequestMethod.POST)
    public ModelAndView editWord(@Valid final Vocabulary word, final BindingResult result, final Model model,
            Principal principal, HttpServletRequest request,
            @RequestParam(value = SUBMIT_PARAM, required = false) final String submit,
            @RequestParam("groupId") final Long groupId)
    {
        if (!"Submit".equals(submit))
        {
            return redirectToViewInstance(REDIRECT_MANAGE_VOCABULARY_PAGE, request, groupId);
        }
        String view = permissionService.canGroup(PermissionType.MANAGE_VOCABULARY, groupId, principal,
                new PermissionService.ResearchGroupAction()
                {
                    @Override
                    public String act(ResearchGroup researchGroup, User user)
                    {
                        ResearchGroup group = ResearchGroup.findResearchGroup(groupId);
                        word.setResearchGroup(group);
                        if (word.isDuplicate())
                        {
                            String[] errorCode = {""};
                            String[] errorArg = {""};
                            FieldError error = new FieldError(VOCABULARY_ATTR, KEYWORD_ATTR, word.getKeyword(), true,
                                    errorCode, errorArg, "Keyword already exists in the vocabulary.");
                            result.addError(error);
                        }

                        if (result.hasFieldErrors(KEYWORD_ATTR))
                        {
                            model.addAttribute("groupId", groupId);
                            model.addAttribute(VOCABULARY_ATTR, word);
                            model.addAttribute(Breadcrumb.BREADCRUMBS, breadcrumbsForEditingWord);
                            return "researchgroup/editword";
                        }
                        vocabularyService.editTerm(groupId, word);
                        ResearchGroup.indexResearchGroup(researchGroup);
                        return REDIRECT_MANAGE_VOCABULARY_PAGE;
                    }
                });
        if (view.equals(REDIRECT_MANAGE_VOCABULARY_PAGE))
        {
            return redirectToViewInstance(view, request, groupId);
        }
        else
        {
            return new ModelAndView(view);
        }
    }

    @RequestMapping(value = "researchgroup/createvocabulary/{id}", method = RequestMethod.GET)
    public String createVocabulary(@PathVariable("id") final Long id, final Model model, Principal principal)
    {
        return permissionService.canGroup(PermissionType.MANAGE_VOCABULARY, id, principal,
                new PermissionService.ResearchGroupAction()
                {
                    @Override
                    public String act(ResearchGroup researchGroup, User user)
                    {
                        MasterVocabulary vocabulary = new MasterVocabulary();
                        List<MasterVocabulary> masterVocabulary = MasterVocabulary.findAllMasterVocabularys();
                        model.addAttribute(Breadcrumb.BREADCRUMBS, breadcrumbsForCreateVocabulary);
                        model.addAttribute(VOCABULARY_ATTR, vocabulary);
                        model.addAttribute("masterVocabulary", masterVocabulary);
                        model.addAttribute("id", id);
                        return "researchgroup/createvocabulary";
                    }
                });
    }

    @RequestMapping(value = "researchgroup/createVocabulary", method = RequestMethod.POST)
    public ModelAndView createVocabulary(@RequestParam("id") final Long id,
            @RequestParam("vocabularySelect") final String vocabularySelect, final Model model, Principal principal,
            final HttpServletRequest request)
    {
        String view = permissionService.canGroup(PermissionType.MANAGE_VOCABULARY, id, principal,
                new PermissionService.ResearchGroupAction()
                {
                    @Override
                    public String act(ResearchGroup researchGroup, User user)
                    {
                        model.addAttribute(Breadcrumb.BREADCRUMBS, breadcrumbsForCreateVocabulary);
                        MasterVocabulary masterVocabulary = MasterVocabulary.findMasterVocabularysByNameEquals(
                                vocabularySelect).getSingleResult();

                        List<MasterVocabularyTerm> vocabularyTerms = MasterVocabularyTerm
                                .findMasterVocabularyTermsByMasterVocabulary(masterVocabulary).getResultList();

                        for (MasterVocabularyTerm term : vocabularyTerms)
                        {
                            Vocabulary vocabulary = new Vocabulary();
                            vocabulary.setKeyword(term.getKeyword());
                            vocabulary.setResearchGroup(researchGroup);
                            vocabulary.persist();
                        }
                        return REDIRECT_MANAGE_VOCABULARY_PAGE;
                    }
                });
        if (view.equals(REDIRECT_MANAGE_VOCABULARY_PAGE))
        {
            return redirectToViewInstance(view, request, id);
        }
        else
        {
            return new ModelAndView(view);
        }
    }

    private ModelAndView redirectToViewInstance(String view, HttpServletRequest request, Long id)
    {
        String pathVariableView = view + "/" + id;
        ModelAndView mav = modelAndViewRedirectHelper.generateRedirectView(pathVariableView, request);
        // object to check window is open
        mav.addObject("browse", true);
        return mav;
    }

    @RequestMapping("vocabulary/searchKeywords")
    @ResponseBody
    public String searchKeywords(@RequestParam(value = "q") String search, @RequestParam(value = "id") Long id,
            Model model)
    {
        ResearchGroup group = ResearchGroup.findResearchGroup(id);
        List<Vocabulary> matchResult = Vocabulary.findVocabularysByResearchGroupAndKeywordLike(group, search)
                .getResultList();
        return tokenInputHelper.appendJson(matchResult);
    }
}
