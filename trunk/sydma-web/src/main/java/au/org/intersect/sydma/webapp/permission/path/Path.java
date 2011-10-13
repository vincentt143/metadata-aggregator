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
 * Common interface for all Path types
 *
 * @version $Rev: 29 $
 */
public interface Path
{
    String SEPARATOR = "/";

    /**
     * Get the path to the object as a string
     * @return - the path to the object
     */
    public String getPath();
    
    /**
     * Get the group id associated with this path - if any
     * @return - the path to the object
     */
    public Long getGroupId();
    
    /**
     * Get the project id associated with this path - if any
     * @return - the path to the object
     */
    public Long getProjectId();

    /**
     * Get the dataset id associated with this path - if any
     * @return - the path to the object
     */
    public Long getDatasetId();
    
    /**
     * Get the file path associated with this path - if any
     * @return - the path to the object
     */
    public String getFilePath();
    
    /**
     * Returns true if it is a path to a Group
     * @return
     */
    boolean isGroupPath();
    
    /**
     * Returns true if it is a path to a Project
     * @return
     */
    boolean isProjectPath();

    /**
     * Returns true if it is a path to a Dataset
     * @return
     */
    boolean isDatasetPath();

    /**
     * Returns true if it is a path to a File
     * @return
     */
    boolean isFilePath();
}
