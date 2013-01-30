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

package au.org.intersect.sydma.webapp.util;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * 
 * Helper object to create a redirect model and view with params and auto detects ajaxView
 * 
 * @version $Rev: 29 $
 */
public class ModelAndViewRedirectHelper
{
    private static final String AJAX_SOURCE_PARAM = "ajaxSource";
    private static final String FRAGMENTS_PARAM = "fragments";

    public ModelAndView generateRedirectView(String view, HttpServletRequest request)
    {
        ModelAndView mav = new ModelAndView();
        mav.setView(new RedirectView(view, true, true, true));

        if (request.getParameter(AJAX_SOURCE_PARAM) != null)
        {
            mav.addObject(AJAX_SOURCE_PARAM, request.getParameter(AJAX_SOURCE_PARAM));
        }
        if (request.getParameter(FRAGMENTS_PARAM) != null)
        {
            mav.addObject(FRAGMENTS_PARAM, request.getParameter(FRAGMENTS_PARAM));
        }
        return mav;
    }
    
    public ModelAndView generateRedirectView(String view, HttpServletRequest request, Map<String, Object> map)
    {
        ModelAndView mav = generateRedirectView(view, request);
        mav.addAllObjects(map);
        return mav;
    }
    
}
