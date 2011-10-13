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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.org.intersect.sydma.webapp.controller.propertyeditor.ResearchSubjectCodePropertyEditor;
import au.org.intersect.sydma.webapp.domain.AccessLevel;
import au.org.intersect.sydma.webapp.domain.RdsRequest;
import au.org.intersect.sydma.webapp.domain.ResearchGroup;
import au.org.intersect.sydma.webapp.domain.ResearchSubjectCode;
import au.org.intersect.sydma.webapp.permission.path.Path;
import au.org.intersect.sydma.webapp.permission.path.PathBuilder;
import au.org.intersect.sydma.webapp.service.PermissionService;
import au.org.intersect.sydma.webapp.util.Breadcrumb;

/**
 * Controller to handle physical collections
 * 
 * @version $Rev: 29 $
 */
@RequestMapping("/phycol/**")
@Controller
public class PhysicalCollectionController
{
    private static final String BREADCRUMB_ATTR = "breadcrumbs";

    private static final String PHY_COL_ATTR = "researchGroup";
    private static final String PHY_COL_CREATE_VIEW = "phycol/new";
    private static final String HOME_VIEW = "redirect:/";

    private static List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
    
    static
    {
        breadcrumbs.add(Breadcrumb.getHome());
        breadcrumbs.add(new Breadcrumb("Describe Sydney Research Data"));
    }
    

    @Autowired
    private PermissionService permissionService;
    

    @InitBinder
    public void setBinder(WebDataBinder dataBinder) 
    {
        dataBinder.registerCustomEditor(ResearchSubjectCode.class, new ResearchSubjectCodePropertyEditor());      
    }

    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String createPhyColRender(@ModelAttribute(PHY_COL_ATTR) ResearchGroup phyCol, Model model)
    {
        model.addAttribute(BREADCRUMB_ATTR, breadcrumbs);
        return PHY_COL_CREATE_VIEW;
    }

    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public String createPhyColPost(Model model, @Valid @ModelAttribute(PHY_COL_ATTR) ResearchGroup phyCol,
            BindingResult result)
    {
        if (!result.hasErrors())
        {
         // check for duplicate in research group
            boolean groupIsUnique;
            boolean rdsIsUnique;
            groupIsUnique = ResearchGroup.findResearchGroupsByNameEquals(phyCol.getName()).getResultList().isEmpty();
            rdsIsUnique = RdsRequest.findRdsRequestsByNameEquals(phyCol.getName()).getResultList().isEmpty();
            if (!groupIsUnique || !rdsIsUnique)
            {
                String[] nameErrorCode = {""};
                String[] nameErrorArg = {""};
                FieldError nameError = new FieldError(PHY_COL_ATTR, "name", phyCol.getName(), true, nameErrorCode,
                        nameErrorArg, "Group already exists.");
                result.addError(nameError);
            }
            else
            {
                phyCol.setIsPhysical(true);
                phyCol.persist();
                Path pathToGroup = PathBuilder.groupPath(phyCol);
                permissionService.addPermission(phyCol.getPrincipalInvestigator(), 
                        pathToGroup, AccessLevel.FULL_ACCESS);
                
            }
        }
        if (result.hasErrors())
        {
            
            model.addAttribute(BREADCRUMB_ATTR, breadcrumbs);
            return PHY_COL_CREATE_VIEW;    
        }
        
        
        
        return HOME_VIEW;
    }

}
