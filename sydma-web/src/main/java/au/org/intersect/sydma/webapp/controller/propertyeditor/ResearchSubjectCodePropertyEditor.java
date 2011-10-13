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

package au.org.intersect.sydma.webapp.controller.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.intersect.sydma.webapp.domain.ResearchSubjectCode;

/**
 * Property editor to convert input string to Research Subject Code entity
 * 
 * @version $Rev: 29 $
 */
public class ResearchSubjectCodePropertyEditor extends PropertyEditorSupport
{
    private static final Logger LOG = LoggerFactory.getLogger(ResearchSubjectCodePropertyEditor.class);

    @Override
    public String getAsText()
    {
        if (getValue() == null)
        {
            return "";
        }
        else
        {
            ResearchSubjectCode code = (ResearchSubjectCode) getValue();
            return code.getDisplayName();
        }
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException
    {
        if (StringUtils.isNotEmpty(text))
        {
            ResearchSubjectCode code = null;
            Pattern pattern = Pattern.compile("^(\\d+).*");
            Matcher m = pattern.matcher(text);
            if (m.matches())
            {
                String match = m.group(1);
                code = ResearchSubjectCode.findResearchSubjectCode(match);

            }
            if (code == null)
            {
                throw new IllegalArgumentException("Invalid Research Subject Code Input");
            }

            setValue(code);

        }
        else
        {
            setValue(null);
        }

    }
}
