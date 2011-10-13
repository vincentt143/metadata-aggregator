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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.org.intersect.sydma.webapp.domain.RdsRequest;
import au.org.intersect.sydma.webapp.domain.RdsRequestStatus;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.dto.GroupDirectoryPath;
import au.org.intersect.sydma.webapp.service.FileAccessService;
import au.org.intersect.sydma.webapp.service.RdsService;
import au.org.intersect.sydma.webapp.util.Breadcrumb;

/**
 * Administration Controller.
 */
@Controller
@RequestMapping("/admin/**")
public class AdminController
{
    private static final String GROUP_DIRECTORY_MODEL = "groupDirectory";

    private static final String SUBMIT_URL_PAGE = "/admin/rds/";

    private static final String RDS_REQUEST = "rdsRequest";

    private static final String SUBMIT_URL = "submitUrl";

    private static final String UPDATABLE = "updatable";

    private static final String ADMIN_RDS_SHOW_PAGE = "admin/rds/show";

    private static final String ADMIN_RDS_APPROVED_LIST_PAGE = "admin/rds/listApproved";
    private static final String ADMIN_RDS_UNAPPROVED_LIST_PAGE = "admin/rds/listUnapproved";

    private static final Logger LOG = LoggerFactory.getLogger(AdminController.class);

    private static List<Breadcrumb> unapprovedRequestsBreadcrumbs = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> approvedRequestsBreadcrumbs = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> rdsBreadcrumbs = new ArrayList<Breadcrumb>();
    private static List<Breadcrumb> indexBreadcrumbs = new ArrayList<Breadcrumb>();

    private static final String REDIRECT_TO_RDS_SHOW = "redirect:admin/rds/show/";

    @Autowired
    private RdsService rdsService;

    @Autowired
    private FileAccessService fileAccessService;

    static
    {
        indexBreadcrumbs.add(Breadcrumb.getHome());
        indexBreadcrumbs.add(new Breadcrumb("Administrator"));

        unapprovedRequestsBreadcrumbs.add(Breadcrumb.getHome());
        unapprovedRequestsBreadcrumbs.add(Breadcrumb.getAdminHome());
        unapprovedRequestsBreadcrumbs.add(new Breadcrumb("Unapproved RDS Requests"));

        approvedRequestsBreadcrumbs.add(Breadcrumb.getHome());
        approvedRequestsBreadcrumbs.add(Breadcrumb.getAdminHome());
        approvedRequestsBreadcrumbs.add(new Breadcrumb("Approved RDS Requests"));

        rdsBreadcrumbs.add(Breadcrumb.getHome());
        rdsBreadcrumbs.add(Breadcrumb.getAdminHome());
        rdsBreadcrumbs.add(new Breadcrumb("RDS Requests Status"));
    }

    @RequestMapping(value = "/rds/list", params = "created", method = RequestMethod.GET)
    public String listCreatedRequests(Model model)
    {
        model.addAttribute(Breadcrumb.BREADCRUMBS, unapprovedRequestsBreadcrumbs);
        model.addAttribute("rdsRequests", RdsRequest.findRdsRequestsByRequestStatus(RdsRequestStatus.CREATED)
                .getResultList());
        return ADMIN_RDS_UNAPPROVED_LIST_PAGE;
    }

    @RequestMapping(value = "/rds/list", params = "approved", method = RequestMethod.GET)
    public String listCompletedRequests(Model model)
    {
        model.addAttribute(Breadcrumb.BREADCRUMBS, approvedRequestsBreadcrumbs);
        model.addAttribute("rdsRequests", RdsRequest.findRdsRequestsByRequestStatus(RdsRequestStatus.APPROVED)
                .getResultList());
        return ADMIN_RDS_APPROVED_LIST_PAGE;
    }

    @RequestMapping(value = "/rds/show/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model model)
    {
        model.addAttribute(Breadcrumb.BREADCRUMBS, rdsBreadcrumbs);
        model.addAttribute(GROUP_DIRECTORY_MODEL, new GroupDirectoryPath());
        RdsRequest rdsRequest = RdsRequest.findRdsRequest(id);
        model.addAttribute(SUBMIT_URL, SUBMIT_URL_PAGE + id.toString());
        model.addAttribute(UPDATABLE, rdsRequest.getRequestStatus() == RdsRequestStatus.CREATED);
        model.addAttribute(RDS_REQUEST, rdsRequest);
        return ADMIN_RDS_SHOW_PAGE;
    }

    @RequestMapping(value = "/rds/{id}", params = "Approve", method = RequestMethod.POST)
    public String approve(@PathVariable("id") Long id, Model model,
            @Valid @ModelAttribute(GROUP_DIRECTORY_MODEL) GroupDirectoryPath directory, BindingResult result)
    {
        RdsRequest rdsRequest = RdsRequest.findRdsRequest(id);

        String directoryPath = directory.getDirPath();

        boolean verified = fileAccessService.verifyGroupPath(directoryPath);
        if (!verified)
        {
            String[] args = {};
            String[] codes = {"researchGroup.dirPath.inaccessible"};
            result.addError(new FieldError(GROUP_DIRECTORY_MODEL, "dirPath", directory.getDirPath(), false, codes,
                    args, "Directory path invalid or not empty"));
        }
        else
        {
            if (!ResearchGroup.findResearchGroupsByDirectoryPathEquals(directoryPath).getResultList().isEmpty())
            {
                String[] args = {};
                String[] codes = {"researchGroup.dirPath.inuse"};
                result.addError(new FieldError(GROUP_DIRECTORY_MODEL, "dirPath", directory.getDirPath(), false, codes,
                        args, "Directory path is already used by an existing Research Group"));
            }
        }
        if (result.hasErrors() || rdsRequest.getRequestStatus() != RdsRequestStatus.CREATED)
        {
            model.addAttribute(SUBMIT_URL, SUBMIT_URL_PAGE + id.toString());
            model.addAttribute(UPDATABLE, rdsRequest.getRequestStatus() == RdsRequestStatus.CREATED);
            model.addAttribute(RDS_REQUEST, rdsRequest);
            return ADMIN_RDS_SHOW_PAGE;
        }

        rdsService.approveRdsRequest(directory.getDirPath(), rdsRequest);
        return REDIRECT_TO_RDS_SHOW + rdsRequest.getId();
    }

    @RequestMapping(value = "/rds/{id}", params = "Reject", method = RequestMethod.POST)
    public String reject(@PathVariable("id") Long id, Model model)
    {
        RdsRequest rdsRequest = RdsRequest.findRdsRequest(id);
        if (rdsRequest.getRequestStatus() != RdsRequestStatus.CREATED)
        {
            model.addAttribute(UPDATABLE, rdsRequest.getRequestStatus() == RdsRequestStatus.CREATED);
            model.addAttribute(RDS_REQUEST, rdsRequest);
            return ADMIN_RDS_SHOW_PAGE;
        }

        rdsService.rejectRdsRequest(rdsRequest);
        return REDIRECT_TO_RDS_SHOW + rdsRequest.getId();
    }

    @RequestMapping(value = "/index")
    public void index(Model model)
    {
        model.addAttribute(Breadcrumb.BREADCRUMBS, indexBreadcrumbs);
    }
}
