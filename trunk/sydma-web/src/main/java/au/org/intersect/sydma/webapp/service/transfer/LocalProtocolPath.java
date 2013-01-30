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

package au.org.intersect.sydma.webapp.service.transfer;

import org.springframework.beans.factory.annotation.Autowired;

import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.exception.PermissionDeniedException;
import au.org.intersect.sydma.webapp.permission.PermissionType;
import au.org.intersect.sydma.webapp.permission.path.Path;
import au.org.intersect.sydma.webapp.permission.path.PathBuilder;
import au.org.intersect.sydma.webapp.service.FilePathService;
import au.org.intersect.sydma.webapp.service.PermissionService;

/**
 * 
 *
 * @version $Rev: 29 $
 */
public class LocalProtocolPath implements ProtocolPath
{
    @Autowired
    private PermissionService permissionService;

    @Autowired
    private FilePathService filePathService;

    @Override
    public String getDisplayPath(String path, User user)
    {
        return PathBuilder.buildFromString(path).getDisplayName();
    }

    @Override
    public String getTransportPath(String path, User user)
    {
        Path virtualPath = securePath(path, user, PermissionType.DOWNLOAD);
        return determineVirtualItem(virtualPath);
    }

    private Path securePath(String virtualPath, User user, PermissionType permissionType)
    {
        Path path = PathBuilder.buildFromString(virtualPath);
        boolean allowed = permissionService.canDirectoryAction(permissionType, path, user);
        if (!allowed)
        {
            throw new PermissionDeniedException("No access to " + path.getPath());
        }
        return path;
    }

    private String determineVirtualItem(Path path)
    {
        String localItem = filePathService.resolveToRelativePath(path);
        return localItem;
    }

}
