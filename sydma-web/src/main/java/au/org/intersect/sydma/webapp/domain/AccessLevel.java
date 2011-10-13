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
package au.org.intersect.sydma.webapp.domain;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Enumeration of the different access levels that can be given to an object in the research group tree
 *
 * @version $Rev: 29 $
 */
public enum AccessLevel
{

    /**
     * Full access - the user can do CRUD objects in the tree
     */
    FULL_ACCESS("Full Access", ""),
    /**
     * Editing access - the user can read and update objects in the tree
     */
    EDITING_ACCESS("Editing Access", ""),
    /** 
     * Viewing access - the user can read abjects in the tree 
     */
    VIEWING_ACCESS("Viewing Access", ""), 
    /** 
     * No access - the user cannot do any operations over the object 
     */    
    NO_ACCESS("No Access", "");
    
    /**
     * Short name of the access level
     */
    private String name;
    
    /**
     * Full description of the access level
     */
    private String message;

    private AccessLevel(String name, String message)
    {
        this.name = name;
        this.message = message;
    }
    public String getName()
    {
        return name;
    }

    public String getMessage()
    {
        return message;
    }
    
    public static Collection<AccessLevel> coveredBy(AccessLevel level)
    {
        Collection<AccessLevel> levels = new ArrayList<AccessLevel>();
        levels.add(level);
        if (level == EDITING_ACCESS)
        {
            levels.add(FULL_ACCESS);
        }
        else if (level == VIEWING_ACCESS)
        {
            levels.add(FULL_ACCESS);
            levels.add(EDITING_ACCESS);            
        }
        return levels;
    }
    
    public boolean isAtLeast(AccessLevel level)
    {
        if (this == NO_ACCESS)
        {
            return false;
        }
        if (this == VIEWING_ACCESS)
        {
            return level == VIEWING_ACCESS;
        }
        else if (this == FULL_ACCESS)
        {
            return true;
        }
        // else if (this == EDITING_ACCESS)
        return level == VIEWING_ACCESS || level == EDITING_ACCESS; 
    }
}
