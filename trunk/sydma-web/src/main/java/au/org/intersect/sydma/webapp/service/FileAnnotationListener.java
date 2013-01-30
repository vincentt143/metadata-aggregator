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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import au.org.intersect.dms.core.service.BasicConnectionDetails;
import au.org.intersect.dms.core.service.WorkerEventListener;
import au.org.intersect.sydma.webapp.domain.FileAnnotation;

/**
 * @author carlos
 * 
 */
public class FileAnnotationListener implements WorkerEventListener
{

    private static final Logger LOG = LoggerFactory.getLogger(FileAnnotationListener.class);

    private static final String LOCAL_PROTOCOL = "local";

    @Value("#{sydmaFileProperties['sydma.localFileServer']}")
    private String localServer;

    @Autowired
    private FilePathService filePathService;

    public FileAnnotationListener()
    {

    }

    public FileAnnotationListener(FilePathService filePathService, String localServer)
    {
        this.filePathService = filePathService;
        this.localServer = localServer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.org.intersect.dms.core.service.WorkerEventListener#createDirectory
     * (au.org.intersect.dms.core.service.BasicConnectionDetails, java.lang.String, java.lang.String)
     */
    @Override
    public void createDirectory(BasicConnectionDetails connParams, String parentPath, String name)
    {
        // don't need to do anything
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.org.intersect.dms.core.service.WorkerEventListener#rename(au.org.intersect
     * .dms.core.service.BasicConnectionDetails, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void rename(BasicConnectionDetails connParams, String parentPath, String oldName, String newName)
    {
        throw new UnsupportedOperationException("Rename is not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.org.intersect.dms.core.service.WorkerEventListener#delete(au.org.intersect
     * .dms.core.service.BasicConnectionDetails, java.lang.String)
     */
    @Override
    public void delete(BasicConnectionDetails connParams, String path)
    {
        LOG.info("delete {} ", path);
        boolean deleteLocal = LOCAL_PROTOCOL.equals(connParams.getProtocol());

        if (deleteLocal)
        {
            FileAnnotation annotationToDelete = resolvePath(path);
            if (annotationToDelete != null)
            {
                annotationToDelete.remove();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.org.intersect.dms.core.service.WorkerEventListener#copyFromTo(au.org
     * .intersect.dms.core.service.BasicConnectionDetails, java.lang.String,
     * au.org.intersect.dms.core.service.BasicConnectionDetails, java.lang.String)
     */
    @Override
    public void copyFromTo(BasicConnectionDetails connParamsFrom, String pathFrom, BasicConnectionDetails connParamsTo,
            String pathTo)
    {
        LOG.info("copyFrom {} to {}", pathFrom, pathTo);
        boolean fromLocal = LOCAL_PROTOCOL.equals(connParamsFrom.getProtocol());
        boolean toLocal = LOCAL_PROTOCOL.equals(connParamsTo.getProtocol());
        if (fromLocal && toLocal)
        {
            // merge
            mergeAnnotationsForPaths(pathFrom, pathTo);
        }
        else if (!fromLocal && toLocal)
        {
            // don't copy and mark
            updateAnnotationForPath(pathTo);
        }
        // other transfer require no actions

    }

    private void updateAnnotationForPath(String pathTo)
    {
        FileAnnotation toAnnotation = resolvePath(pathTo);
        if (toAnnotation != null)
        {
            updateAnnotation(toAnnotation);
        }
    }

    private void mergeAnnotationsForPaths(String pathFrom, String pathTo)
    {
        final FileAnnotation fromAnnotation = resolvePath(pathFrom);
        final FileAnnotation toAnnotation = resolvePath(pathTo);

        if (fromAnnotation != null && toAnnotation != null)
        {
            mergeAnnotations(fromAnnotation, toAnnotation);
        }
        else if (fromAnnotation == null && toAnnotation != null)
        {
            updateAnnotation(toAnnotation);
        }
        else if (fromAnnotation != null && toAnnotation == null)
        {
            String virtualPath = filePathService.relativeToVirtualPath(pathTo);
            if (virtualPath != null)
            {
                assignAnnotation(fromAnnotation, virtualPath);
            }
        }
    }

    private void assignAnnotation(FileAnnotation fromAnnotation, String virtualPathTo)
    {
        // create a toAnnotation
        FileAnnotation toAnnotation = new FileAnnotation(virtualPathTo, fromAnnotation.getAnnotation());
        toAnnotation.persist();

    }

    private void updateAnnotation(FileAnnotation annotation)
    {
        // mark the the annotation as out of date
        annotation.setOutOfDate(true);
        annotation.merge();
    }

    private void mergeAnnotations(FileAnnotation fromAnnotation, FileAnnotation toAnnotation)
    {
        String fromAnnotationContent = fromAnnotation.getAnnotation();
        String toAnnotationContent = toAnnotation.getAnnotation();

        String newAnnotation = toAnnotationContent + " \n" + fromAnnotationContent;

        toAnnotation.setAnnotation(newAnnotation);

        // and persist updated to annotation
        updateAnnotation(toAnnotation);
    }

    private FileAnnotation resolvePath(String path)
    {
        String virtualPath = filePathService.relativeToVirtualPath(path);
        return virtualPath != null ? FileAnnotation.findFileAnnotationByPath(virtualPath) : null;
    }

}
