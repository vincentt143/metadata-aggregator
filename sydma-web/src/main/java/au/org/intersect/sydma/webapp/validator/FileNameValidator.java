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
package au.org.intersect.sydma.webapp.validator;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filename validator
 * 
 * @version $Rev: 29 $
 */
public class FileNameValidator implements ConstraintValidator<FileName, String>
{
    private static final Logger LOG = LoggerFactory.getLogger(FileNameValidator.class);

    private static Pattern specialChars = Pattern.compile("[/\\\\?%*:|\"<>]");
    private static Pattern windowsNames = Pattern
            .compile("^(CON|PRN|AUX|CLOCK(\\$)?|NUL|COM[0123456789]|LPT[0123456789])[:]?$", Pattern.CASE_INSENSITIVE);
    private static Pattern cantStartWith = Pattern.compile("^[.$]");

    public void initialize(FileName annotation)
    {
    }

    public boolean isValid(String value, ConstraintValidatorContext context)
    {
        if (value == null || value.length() == 0)
        {
            return true;
        }

        boolean specialCharsFound = specialChars.matcher(value).find();
        boolean windowsNamesFound = windowsNames.matcher(value).find();
        boolean cantStartWithFound = cantStartWith.matcher(value).find();

        LOG.info("specialCharsMatcher = " + specialCharsFound);
        LOG.info("windowsNamesMatcher = " + windowsNamesFound);
        LOG.info("cantStartWithMatcher = " + cantStartWithFound);

        boolean returnValue = !specialCharsFound && !windowsNamesFound && !cantStartWithFound;
        return returnValue;
    }

}
