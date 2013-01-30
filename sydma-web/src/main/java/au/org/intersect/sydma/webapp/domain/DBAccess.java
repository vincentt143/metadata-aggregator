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

/**
 * Enum specifying the dbUser's access level type on the underylying database
 * 
 * @version $Rev: 29 $
 */
public enum DBAccess
{

    FULL_ACCESS(
            "Full Access",
            "ALL",
            "fa"), 
    UPDATE_ACCESS(
            "Update Access",
            "INSERT,SELECT,UPDATE,DELETE",
            "ua"), 
    VIEW_ACCESS(
            "View Access",
            "SELECT",
            "va");

    private String accessName;
    
    private String dbSuffix;
    
    private String sqlGrants;

    private DBAccess(String accessName, String sqlGrants, String dbSuffix)
    {
        this.accessName = accessName;
        this.sqlGrants = sqlGrants;
        this.dbSuffix = dbSuffix;
    }

    public String getAccessName()
    {
        return accessName;
    }
    

    public String getDbSuffix()
    {
        return dbSuffix;
    }
    
    public String getSqlGrants()
    {
        return sqlGrants;
    }

}
