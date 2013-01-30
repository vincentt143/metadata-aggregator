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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import au.org.intersect.sydma.webapp.domain.ActivityLog;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.permission.PermissionType;
import au.org.intersect.sydma.webapp.service.PermissionService;
import au.org.intersect.sydma.webapp.util.Breadcrumb;

/**
 * Controller for activity logging
 */
@Controller
@RequestMapping("/permission/**")
public class ActivityLogController
{
    private static List<Breadcrumb> activityLogBreadcrumbs = new ArrayList<Breadcrumb>();

    @Autowired
    private PermissionService permissionService;

    static
    {
        activityLogBreadcrumbs.add(Breadcrumb.getHome());
        activityLogBreadcrumbs.add(new Breadcrumb("sections.permissions.activity.log"));
    }

    @RequestMapping(value = "/activityLog", method = RequestMethod.GET)
    public String activityLog(@RequestParam("id") final Long id, final Model model, Principal principal)
    {
        return permissionService.canViewActivityLog(PermissionType.ACTIVITY_LOG, id, principal,
                new PermissionService.ResearchGroupAction()
                {
                    @Override
                    public String act(ResearchGroup group, User user)
                    {
                        List<ActivityLog> activityLog = ActivityLog.findActivityLogsByResearchGroup(group)
                                .getResultList();
                        model.addAttribute(Breadcrumb.BREADCRUMBS, activityLogBreadcrumbs);
                        model.addAttribute("activityLog", activityLog);
                        model.addAttribute("id", id);
                        return "permission/activityLog";
                    }
                });
    }
}
