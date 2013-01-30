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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for password validator
 *
 * @version $Rev: 29 $
 */
public class RstudioPasswordValidatorTest
{
    private RstudioPasswordValidator passwordValidator;
    
    @Before
    public void setUp()
    {
        passwordValidator = new RstudioPasswordValidator();
    }
    
    @Test
    public void testWithFiveCharactersInvalid()
    {
        assertFalse(passwordValidator.isValidPassword("asdas"));
    }

    @Test
    public void testWithSixCharactersValid()
    {
        assertTrue(passwordValidator.isValidPassword("asdass"));
    }
    
    @Test
    public void testAcceptsNumbers()
    {
        assertTrue(passwordValidator.isValidPassword("123123"));        
    }

    @Test
    public void testAcceptsUppercase()
    {
        assertTrue(passwordValidator.isValidPassword("ASDASD"));        
    }

    @Test
    public void testAcceptsSomeSymbols()
    {
        assertTrue(passwordValidator.isValidPassword("@#$%^&+="));        
    }
    
    @Test
    public void testDoesNotAcceptExclamationMark()
    {
        assertFalse(passwordValidator.isValidPassword("asdasd!"));        
    }
    
    @Test
    public void testDoesNotAcceptPipe()
    {
        assertFalse(passwordValidator.isValidPassword("asdasd|"));        
    }
    
    @Test
    public void testDoesNotAcceptSpaces()
    {
        assertFalse(passwordValidator.isValidPassword("asd as"));
    }

}
