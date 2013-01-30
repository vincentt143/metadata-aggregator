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
package au.org.intersect.sydma.webapp.permission.path;

/**
 * Encapsulates basic functionality common to all paths
 *
 * @version $Rev: 29 $
 */
abstract class AbstractPath implements Path
{
    private static final String OPERATION_NOT_SUPPORTED = "Operation not supported";

    public Long getGroupId()
    {
        throw new IllegalArgumentException(OPERATION_NOT_SUPPORTED);
    }
    
    public Long getProjectId()
    {
        throw new IllegalArgumentException(OPERATION_NOT_SUPPORTED);
    }
    
    public Long getDatasetId()
    {
        throw new IllegalArgumentException(OPERATION_NOT_SUPPORTED);
    }
    
    public String getFilePath()
    {
        throw new IllegalArgumentException(OPERATION_NOT_SUPPORTED);
    }
    
    @Override
    public int hashCode()
    {
        return getPath().hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        return (o != null) && (o instanceof Path) && getPath().equals(((Path) o).getPath()); 
    }
    
    @Override
    public boolean isGroupPath()
    {
        return false;
    }
    
    @Override
    public boolean isProjectPath()
    {
        return false;
    }

    @Override
    public boolean isDatasetPath()
    {
        return false;
    }

    @Override
    public boolean isFilePath()
    {
        return false;
    }
    
    public boolean isAffectedBy(Path path)
    {
        return getPath().startsWith(path.getPath()) || path.getPath().startsWith(getPath());
    }

}
