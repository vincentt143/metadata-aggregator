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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

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

import au.org.intersect.sydma.webapp.controller.propertyeditor.ResearchSubjectCodePropertyEditor;
import au.org.intersect.sydma.webapp.domain.RdsRequest;
import au.org.intersect.sydma.webapp.domain.RdsRequestStatus;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchSubjectCode;
import au.org.intersect.sydma.webapp.service.RdsService;
import au.org.intersect.sydma.webapp.util.Breadcrumb;

/**
 * RDS Request Controller.
 */
@RequestMapping("/rds/**")
@Controller
public class RdsController
{
    private static final String RDS_REQUEST_MODEL = "rdsRequest";
    private static final String REDIRECT_TO_NEW = "rds/new";
    private static final String REDIRECT_TO_SHOW = "redirect:rds/show/";

    private static List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();

    @Autowired
    private RdsService rdsService;

    static
    {
        breadcrumbs.add(Breadcrumb.getHome());
        breadcrumbs.add(new Breadcrumb("RDS space"));
    }
    

    @InitBinder
    public void setBinder(WebDataBinder dataBinder) 
    {
        dataBinder.registerCustomEditor(ResearchSubjectCode.class, new ResearchSubjectCodePropertyEditor());      
    }


    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String newRds(Model model)
    {
        model.addAttribute(Breadcrumb.BREADCRUMBS, breadcrumbs);
        model.addAttribute(RDS_REQUEST_MODEL, new RdsRequest());
        return REDIRECT_TO_NEW;
    }

    @RequestMapping(value = "/createRdsRequest", method = RequestMethod.POST)
    public String createRdsRequest(@Valid RdsRequest rdsRequest, BindingResult result, Model model,
            HttpServletRequest request)
    {
        if (result.hasErrors())
        {
            model.addAttribute(RDS_REQUEST_MODEL, rdsRequest);
            return REDIRECT_TO_NEW;
        }
        // check for duplicate in research group
        boolean groupIsUnique;
        boolean rdsIsUnique;
        groupIsUnique = ResearchGroup.findResearchGroupsByNameEquals(rdsRequest.getName()).getResultList().isEmpty();
        rdsIsUnique = RdsRequest.findRdsRequestsByNameEquals(rdsRequest.getName()).getResultList().isEmpty();
        if (!groupIsUnique || !rdsIsUnique)
        {
            model.addAttribute(RDS_REQUEST_MODEL, rdsRequest);
            String groupName = rdsRequest.getName();
            String[] nameErrorCode = {""};
            String[] nameErrorArg = {""};
            FieldError nameError = new FieldError(RDS_REQUEST_MODEL, "name", groupName, true, nameErrorCode,
                    nameErrorArg, "Group already exists.");
            result.addError(nameError);
            return REDIRECT_TO_NEW;
        }
        
        rdsService.createRdsRequest(rdsRequest);
        return REDIRECT_TO_SHOW + rdsRequest.getId();

    }


    @RequestMapping(value = "/show/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model model)
    {
        model.addAttribute(Breadcrumb.BREADCRUMBS, breadcrumbs);
        RdsRequest rdsRequest = RdsRequest.findRdsRequest(id);
        model.addAttribute("submitUrl", "/rds/" + id.toString());
        model.addAttribute("updatable", rdsRequest.getRequestStatus() == RdsRequestStatus.CREATED);
        model.addAttribute("rdsRequest", rdsRequest);
        return "rds/show";
    }

}
