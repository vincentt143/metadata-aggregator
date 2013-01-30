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

package au.org.intersect.sydma.webapp.json;

import java.util.List;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

/**
 * 
 * 
 * @version $Rev: 29 $
 */
@RooJavaBean
@RooToString
// TODO CHECKSTYLE-OFF: MultipleStringLiteralsCheck
public class JsonFileInfo
{
    public static final String ENTITY_TYPE = "ENTITY";
    public static final String DATASET_TYPE = "DATASET";
    public static final String DIRECTORY_TYPE = "DIRECTORY";

    private String nodeId;
    private String name;
    private String absolutePath;
    private String fileType;
    private String modificationDate;
    private Long size;
    private List<String> allowedActions;
    private String annotation;
    private boolean canUpload;

    /**
     * Used for groups and projects
     * 
     * @param name
     * @param absolutePath
     * @param fileType
     * @param modificationDate
     * @param size
     */
    public JsonFileInfo(String name, String absolutePath, String fileType, String modificationDate, Long size)
    {
        this.name = name;
        this.absolutePath = absolutePath;
        this.fileType = fileType;
        this.modificationDate = modificationDate;
        this.size = size;
        this.nodeId = "n" + absolutePath.replaceAll("\\W", "_");
        this.canUpload = false;
    }

    /**
     * Used at the dataset level
     * 
     * @param name
     * @param absolutePath
     * @param fileType
     * @param modificationDate
     * @param size
     */
    public JsonFileInfo(String name, String absolutePath, String fileType, String modificationDate, Long size,
            boolean canUpload)
    {
        this.name = name;
        this.absolutePath = absolutePath;
        this.fileType = fileType;
        this.modificationDate = modificationDate;
        this.size = size;
        this.nodeId = "n" + absolutePath.replaceAll("\\W", "_");
        this.canUpload = canUpload;
    }

    /**
     * Used at the directory level
     * 
     * @param name
     * @param absolutePath
     * @param fileType
     * @param modificationDate
     * @param size
     * @param allowedActions
     * @param canUpload
     */
    public JsonFileInfo(String name, String absolutePath, String fileType, String modificationDate, Long size,
            List<String> allowedActions, boolean canUpload)
    {
        this.name = name;
        this.absolutePath = absolutePath;
        this.fileType = fileType;
        this.modificationDate = modificationDate;
        this.size = size;
        this.nodeId = "n" + absolutePath.replaceAll("\\W", "_");
        this.allowedActions = allowedActions;
        this.canUpload = canUpload;
    }

}
