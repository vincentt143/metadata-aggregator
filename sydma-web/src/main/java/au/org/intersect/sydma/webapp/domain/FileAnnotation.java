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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;

/**
 * Entity storing a user's access information to dataset databases
 * 
 * @version $Rev: 29 $
 */
@RooJavaBean
@RooToString
@RooJson
@RooEntity(persistenceUnit = "sydmaPU", finders = {"findFileAnnotationsByPath"})
// TODO CHECKSTYLE-OFF: MagicNumber
public class FileAnnotation
{
    private static final Logger LOG = LoggerFactory.getLogger(FileAnnotation.class);

    // please note that path's size is too long for mysql to apply unique constraint to
    @NotNull
    @Size(max = 20000)
    @NotEmpty
    private String path;

    @NotNull
    @Size(max = 1000)
    @NotEmpty(message = "required field")
    private String annotation;

    private boolean outOfDate;

    public FileAnnotation()
    {

    }

    public FileAnnotation(String path, String annotation)
    {
        super();
        this.path = path;
        this.annotation = annotation;
    }

    /**
     * Find paths that are the immediate children of a given path eg. these paths exist <br/>
     * /a/b/c/ <br/>
     * /a/b/c/d/ <br/>
     * /a/b/c/h/ <br/>
     * /a/b/c/d/g/ <br/>
     * /a/b/c/e/h/ <br/>
     * 
     * Paths that has parent path /a/b/c/ <br/>
     * /a/b/c/d/ <br/>
     * /a/b/c/h/ <br/>
     */
    public static TypedQuery<FileAnnotation> findFileAnnotationsByParentPath(String parentPath)
    {
        if (parentPath == null || parentPath.length() == 0)
        {
            throw new IllegalArgumentException("The parentPath argument is required");
        }
        EntityManager em = ResearchDataset.entityManager();
        String query;
        query = "SELECT o FROM FileAnnotation AS o WHERE o.path LIKE :childPath  AND o.path NOT LIKE :childChildPath ";
        TypedQuery<FileAnnotation> q = em.createQuery(query, FileAnnotation.class);
        q.setParameter("childPath", parentPath + "_%");
        q.setParameter("childChildPath", parentPath + "_%/_%");
        return q;
    }

    public static FileAnnotation findFileAnnotationByPath(String path)
    {
        List<FileAnnotation> annotations = FileAnnotation.findFileAnnotationsByPath(path).getResultList();
        if (annotations.size() > 0)
        {
            return annotations.get(0);
        }
        else
        {
            return null;
        }
    }

    /**
     * eg. these paths exist <br/>
     * /a/b/c/d/ <br/>
     * /a/b/c/d/f/ <br/>
     * /a/b/c/d/g/ <br/>
     * /a/b/c/e/h/ <br/>
     * 
     * Paths affected by /a/b/c/d/ <br/>
     * /a/b/c/d/ <br/>
     * /a/b/c/d/f/ <br/>
     * /a/b/c/d/g/ <br/>
     * 
     */
    public static TypedQuery<FileAnnotation> findFileAnnotationsByAffectedPath(String path)
    {
        if (path == null || path.length() == 0)
        {
            throw new IllegalArgumentException("The parentPath argument is required");
        }
        EntityManager em = ResearchDataset.entityManager();
        String query;
        query = "SELECT o FROM FileAnnotation AS o WHERE o.path LIKE :path ";
        TypedQuery<FileAnnotation> q = em.createQuery(query, FileAnnotation.class);
        q.setParameter("path", path + "%");
        return q;
    }
}
