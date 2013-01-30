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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mock.staticmock.AnnotationDrivenStaticEntityMockingControl;
import org.springframework.mock.staticmock.MockStaticEntityMethods;

import au.org.intersect.dms.core.service.BasicConnectionDetails;
import au.org.intersect.sydma.webapp.domain.FileAnnotation;

@MockStaticEntityMethods
public class FileAnnotationListenerTest
{
    private FilePathService filePathService;

    private FileAnnotationListener fileAnnotationListener;

    private String localServer = "upload";

    private String localProtocol = "local";

    private String appletProtocol = "hdd";

    private String appletHostname = "tunnel";

    private EntityManager entityManager;

    private String appletPathFrom = "/home/usr/dir1/dir2";
    private String appletPathTo = "/home/usr/dir1/dir2";

    private String serverPathFrom = "/g1/3/dir1/dir2";
    private String serverPathTo = "/g2/9/dir1/dir2";

    private String virtualPathFrom = "/1/2/3/dir1/dir2/";
    private String virtualPathTo = "/7/8/9/dir1/dir2/";

    @Before
    public void setUp()
    {
        filePathService = Mockito.mock(FilePathService.class);

        fileAnnotationListener = new FileAnnotationListener(filePathService, localServer);
        entityManager = Mockito.mock(EntityManager.class);

        Mockito.when(filePathService.relativeToVirtualPath(serverPathFrom)).thenReturn(virtualPathFrom);
        Mockito.when(filePathService.relativeToVirtualPath(serverPathTo)).thenReturn(virtualPathTo);
    }

    @Test
    public void testDeleteServerWithAnnotation()
    {
        String usernameTo = "user1";

        BasicConnectionDetails connParamsDelete = createLocalConnWithUser(usernameTo);

        String fromAnnotation = "something about this file";

        FileAnnotation fromFileAnnotation = Mockito.spy(new FileAnnotation(virtualPathFrom, fromAnnotation));

        Mockito.doNothing().when(fromFileAnnotation).remove();
        
        FileAnnotation.findFileAnnotationByPath(virtualPathFrom);
        AnnotationDrivenStaticEntityMockingControl.expectReturn(fromFileAnnotation);
        AnnotationDrivenStaticEntityMockingControl.playback();

        fileAnnotationListener.delete(connParamsDelete, serverPathFrom);
        
        Mockito.verify(fromFileAnnotation).remove();
    }
    

    @Test
    public void testDeleteServerNoAnnotation()
    {
        String usernameTo = "user1";

        BasicConnectionDetails connParamsDelete = createLocalConnWithUser(usernameTo);

        FileAnnotation fromFileAnnotation = null;
        
        FileAnnotation.findFileAnnotationByPath(virtualPathFrom);
        AnnotationDrivenStaticEntityMockingControl.expectReturn(fromFileAnnotation);
        
        AnnotationDrivenStaticEntityMockingControl.playback();

        fileAnnotationListener.delete(connParamsDelete, serverPathFrom);
        
    }
    
    @Test
    public void testCopyAppletToServer()
    {
        String usernameFrom = "user1";
        String usernameTo = "user1";

        BasicConnectionDetails connParamsFrom = createAppletConnWithUser(usernameFrom);
        BasicConnectionDetails connParamsTo = createLocalConnWithUser(usernameTo);

        String toAnnotation = "other things about that file";

        FileAnnotation toFileAnnotation = Mockito.spy(new FileAnnotation(virtualPathTo, toAnnotation));
        Mockito.doReturn(toFileAnnotation).when(toFileAnnotation).merge();

        FileAnnotation.findFileAnnotationByPath(virtualPathTo);
        AnnotationDrivenStaticEntityMockingControl.expectReturn(toFileAnnotation);

        AnnotationDrivenStaticEntityMockingControl.playback();
        
        fileAnnotationListener.copyFromTo(connParamsFrom, appletPathFrom, connParamsTo, serverPathTo);

        // to annotation should be updated
        assertTrue(toFileAnnotation.isOutOfDate());
        Mockito.verify(toFileAnnotation).merge();
    }

    @Test
    public void testCopyAppletToServerNoAnnotation()
    {
        String usernameFrom = "user1";
        String usernameTo = "user1";

        BasicConnectionDetails connParamsFrom = createAppletConnWithUser(usernameFrom);
        BasicConnectionDetails connParamsTo = createLocalConnWithUser(usernameTo);

        FileAnnotation toFileAnnotation = null;

        FileAnnotation.findFileAnnotationByPath(virtualPathTo);
        AnnotationDrivenStaticEntityMockingControl.expectReturn(toFileAnnotation);

        AnnotationDrivenStaticEntityMockingControl.playback();
        
        fileAnnotationListener.copyFromTo(connParamsFrom, appletPathFrom, connParamsTo, serverPathTo);

    }
    
    @Test
    public void testCopyServerToApplet()
    {

        String usernameFrom = "user1";
        String usernameTo = "user1";

        BasicConnectionDetails connParamsFrom = createLocalConnWithUser(usernameFrom);
        BasicConnectionDetails connParamsTo = createAppletConnWithUser(usernameTo);

        String fromAnnotation = "something about this file";

        FileAnnotation fromFileAnnotation = Mockito.spy(new FileAnnotation(virtualPathFrom, fromAnnotation));

        fileAnnotationListener.copyFromTo(connParamsFrom, appletPathFrom, connParamsTo, serverPathTo);

        // from annotation should not be touched
        Mockito.verify(fromFileAnnotation, Mockito.never()).merge();

    }

    @Test
    public void testCopyTargetAndSourceHasAnnotation()
    {

        String usernameFrom = "user1";

        String usernameTo = "user1";

        BasicConnectionDetails connParamsFrom = createLocalConnWithUser(usernameFrom);
        BasicConnectionDetails connParamsTo = createLocalConnWithUser(usernameTo);

        String fromAnnotation = "something about this file";
        String toAnnotation = "other things about that file";

        FileAnnotation fromFileAnnotation = Mockito.spy(new FileAnnotation(virtualPathFrom, fromAnnotation));

        FileAnnotation toFileAnnotation = Mockito.spy(new FileAnnotation(virtualPathTo, toAnnotation));
        Mockito.doReturn(toFileAnnotation).when(toFileAnnotation).merge();

        FileAnnotation.findFileAnnotationByPath(virtualPathFrom);
        AnnotationDrivenStaticEntityMockingControl.expectReturn(fromFileAnnotation);

        FileAnnotation.findFileAnnotationByPath(virtualPathTo);
        AnnotationDrivenStaticEntityMockingControl.expectReturn(toFileAnnotation);

        AnnotationDrivenStaticEntityMockingControl.playback();

        fileAnnotationListener.copyFromTo(connParamsFrom, serverPathFrom, connParamsTo, serverPathTo);

        // from annotation should be merged with to annotation
        assertEquals(toAnnotation + " \n" + fromAnnotation, toFileAnnotation.getAnnotation());
        
        Mockito.verify(toFileAnnotation).merge();
        assertTrue(toFileAnnotation.isOutOfDate());

    }

    @Test
    public void testCopyTargetHasAnnotation()
    {

        String usernameFrom = "user1";

        String usernameTo = "user1";

        BasicConnectionDetails connParamsFrom = createLocalConnWithUser(usernameFrom);
        BasicConnectionDetails connParamsTo = createLocalConnWithUser(usernameTo);

        String toAnnotation = "other things about that file";

        FileAnnotation fromFileAnnotation = null;

        FileAnnotation toFileAnnotation = Mockito.spy(new FileAnnotation(virtualPathTo, toAnnotation));

        Mockito.doReturn(toFileAnnotation).when(toFileAnnotation).merge();

        FileAnnotation.findFileAnnotationByPath(virtualPathFrom);
        AnnotationDrivenStaticEntityMockingControl.expectReturn(fromFileAnnotation);

        FileAnnotation.findFileAnnotationByPath(virtualPathTo);
        AnnotationDrivenStaticEntityMockingControl.expectReturn(toFileAnnotation);

        AnnotationDrivenStaticEntityMockingControl.playback();

        fileAnnotationListener.copyFromTo(connParamsFrom, serverPathFrom, connParamsTo, serverPathTo);

        // to annotation shouldn't change
        assertEquals(toAnnotation, toFileAnnotation.getAnnotation());        
        Mockito.verify(toFileAnnotation).merge();
        
        assertTrue(toFileAnnotation.isOutOfDate());

    }

    @Test
    public void testCopySourceHasAnnotation()
    {

        String usernameFrom = "user1";

        String usernameTo = "user1";

        BasicConnectionDetails connParamsFrom = createLocalConnWithUser(usernameFrom);
        BasicConnectionDetails connParamsTo = createLocalConnWithUser(usernameTo);

        String fromAnnotation = "something about this file";

        FileAnnotation fromFileAnnotation = Mockito.spy(new FileAnnotation(virtualPathFrom, fromAnnotation));

        FileAnnotation toFileAnnotation = null;



        FileAnnotation.findFileAnnotationByPath(virtualPathFrom);
        AnnotationDrivenStaticEntityMockingControl.expectReturn(fromFileAnnotation);

        FileAnnotation.findFileAnnotationByPath(virtualPathTo);
        AnnotationDrivenStaticEntityMockingControl.expectReturn(toFileAnnotation);

        FileAnnotation.entityManager();
        AnnotationDrivenStaticEntityMockingControl.expectReturn(entityManager);

        AnnotationDrivenStaticEntityMockingControl.playback();

        fileAnnotationListener.copyFromTo(connParamsFrom, serverPathFrom, connParamsTo, serverPathTo);


        // a to annotation should be created
        ArgumentCaptor<FileAnnotation> toAnnotationCapture = ArgumentCaptor.forClass(FileAnnotation.class);

        Mockito.verify(entityManager).persist(toAnnotationCapture.capture());

        FileAnnotation capturedToAnnotation = toAnnotationCapture.getValue();

        assertEquals(fromAnnotation, capturedToAnnotation.getAnnotation());

    }

    private BasicConnectionDetails createAppletConnWithUser(String username)
    {
        return createConnWithParm(appletHostname, appletProtocol, username);
    }

    private BasicConnectionDetails createLocalConnWithUser(String username)
    {
        return createConnWithParm(localServer, localProtocol, username);
    }

    private BasicConnectionDetails createConnWithParm(String hostname, String protocol, String username)
    {
        BasicConnectionDetails connectionDetail = Mockito.mock(BasicConnectionDetails.class);

        Mockito.when(connectionDetail.getProtocol()).thenReturn(protocol);
        Mockito.when(connectionDetail.getHostname()).thenReturn(hostname);
        Mockito.when(connectionDetail.getUsername()).thenReturn(username);

        return connectionDetail;
    }
}
