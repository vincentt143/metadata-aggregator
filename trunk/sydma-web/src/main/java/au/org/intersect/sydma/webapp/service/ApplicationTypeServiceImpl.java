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
package au.org.intersect.sydma.webapp.service;

/**
 * @author carlos
 * 
 */
public class ApplicationTypeServiceImpl implements ApplicationTypeService
{

    private ApplicationType theType;
    private Boolean allowExternal;

    public void setApplicationType(String name)
    {
        theType = ApplicationType.valueOf(name.toUpperCase());
    }

    public void setAllowExternal(Boolean value)
    {
        allowExternal = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * au.org.intersect.sydma.webapp.service.ApplicationTypeService#applicationIs(au.org.intersect.sydma.webapp.service
     * .ApplicationType)
     */
    @Override
    public boolean applicationIs(ApplicationType aType)
    {
        return theType.equals(aType);
    }

    @Override
    public String getThemeName()
    {
        return theType.name().toLowerCase();
    }

    @Override
    public Boolean getAllowExternal()
    {
        return allowExternal;
    }

}
