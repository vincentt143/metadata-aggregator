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

package au.org.intersect.sydma.webapp.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.service.DmsService;
import au.org.intersect.sydma.webapp.domain.User;
import au.org.intersect.sydma.webapp.json.JsonFileInfo;
import au.org.intersect.sydma.webapp.json.JsonResponse;

/**
 * Controller that handles file browsing ajax calls from FileTree js class
 */
@RequestMapping("/rstudiotree/**")
@Controller
@DC2D
public class RstudioTreeController
{
    @Autowired
    private DmsService dmsService;

    @Value("#{rstudio[rstudio_home]}")
    private String rstudioHome;

    @Value("#{rstudio[rstudio_server]}")
    private String rstudioServer;

    @RequestMapping(method = RequestMethod.POST, value = "/connect")
    @ResponseBody
    public String createConnection(Principal principal)
    {
        Integer connectionLocal = connectLocal(principal);
        JsonResponse response = new JsonResponse(connectionLocal, null);
        return response.toJson();
    }

    @RequestMapping(value = "/list")
    @ResponseBody
    public String listDestination(@RequestParam(value = "path") String dmsPath,
            @RequestParam("connectionId") Integer connectionId, Principal principal)
    {
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        String username = user.getRstudioUsername();

        String userPath = rstudioHome + username;
        String dmsTotalPath = userPath + dmsPath;

        List<FileInfo> fileInfoList = filteredList(dmsService.getList(connectionId, dmsTotalPath), dmsPath);
        List<JsonFileInfo> jsonFileInfoList = new ArrayList<JsonFileInfo>();
        for (FileInfo fileInfo : fileInfoList)
        {
            String childAbsolutePath = fileInfo.getAbsolutePath();
            String childVirtualPath = childAbsolutePath.substring(userPath.length());
            JsonFileInfo jsonFileInfo = new JsonFileInfo(fileInfo.getName(), childVirtualPath, fileInfo.getFileType()
                    .toString(), fileInfo.getModificationDate(), fileInfo.getSize(), null, true);
            jsonFileInfoList.add(jsonFileInfo);
        }

        JsonResponse response = new JsonResponse(jsonFileInfoList, null);
        return response.toJson();
    }

    private List<FileInfo> filteredList(List<FileInfo> fileInfoList, String dmsPath)
    {
        List<FileInfo> filteredList = new ArrayList<FileInfo>();
        for (FileInfo fileInfo : fileInfoList)
        {
            if (!fileInfo.getName().startsWith(".") && !("/".equals(dmsPath) && "R".equals(fileInfo.getName())))
            {
                filteredList.add(fileInfo);
            }
        }
        return filteredList;
    }

    private Integer connectLocal(Principal principal)
    {
        String protocol = "sftp";
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        String username = user.getRstudioUsername();
        String password = null;
        return dmsService.openConnection(protocol, rstudioServer, username, password);
    }

}
