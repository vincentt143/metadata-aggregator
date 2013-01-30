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

import javax.validation.Valid;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.org.intersect.sydma.webapp.domain.Publication;

/**
 * Controller to assist in rendering forms for adding publications
 */
@RequestMapping("/publication/**")
@Controller
public class PublicationController
{
    private static final String ADD_VIEW = "publication/add";
    private static final String EDIT_VIEW = "publication/edit";
    private static final String TABLE_ENTRY_VIEW = "publication/tableEntry";

    private static final String PUBLICATION_ATTR = "publication";

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String renderAdd(@ModelAttribute(PUBLICATION_ATTR) Publication publication)
    {
        return ADD_VIEW;
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String validateAdd(@Valid @ModelAttribute(PUBLICATION_ATTR) Publication publication, BindingResult results)
    {
        if (results.hasErrors())
        {
            return ADD_VIEW;
        }
        else
        {
            String citations = StringEscapeUtils.escapeXml(publication.getCitations());
            publication.setCitations(citations);
            return TABLE_ENTRY_VIEW;
        }
    }

    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public String renderEdit(@ModelAttribute(PUBLICATION_ATTR) Publication publication)
    {
        return EDIT_VIEW;
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public String validateEdit(@Valid @ModelAttribute(PUBLICATION_ATTR) Publication publication, BindingResult results)
    {
        if (results.hasErrors())
        {
            return EDIT_VIEW;
        }
        else
        {
            String citations = StringEscapeUtils.escapeXml(publication.getCitations());
            publication.setCitations(citations);
            return TABLE_ENTRY_VIEW;
        }
    }
}
