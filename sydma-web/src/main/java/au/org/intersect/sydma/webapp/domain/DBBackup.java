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

import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;

/**
 * Entity storing a database backups
 * 
 * @version $Rev: 1 $
 */
@RooJavaBean
@RooToString(excludeFields = {"researchDataset"})
@RooJson
@RooEntity(persistenceUnit = "sydmaPU", finders = {"findDBBackupsByResearchDataset"})
// TODO CHECKSTYLE-OFF: MagicNumber
public class DBBackup
{
    @NotNull
    @Size(max = 100)
    private String date;

    @NotNull
    @Pattern(regexp = "\\s*\\S+[\\s\\S]*", message = "This is a required field")
    @Size(max = 1000, message = "This field has to be between 1 to 1000 characters")
    private String description;

    @NotNull
    private String user;

    @NotNull
    @Size(max = 1000)
    private String file;

    @ManyToOne
    private ResearchDataset researchDataset;

    public void create(Principal principal, ResearchDataset dataset, String filename, Date date)
    {
        User user = User.findUsersByUsernameEquals(principal.getName()).getSingleResult();
        String name = user.getGivenname() + " " + user.getSurname();
        this.setUser(name);
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        this.setDate(dateFormat.format(date));
        this.setFile(filename);
        this.setResearchDataset(dataset);
        this.persist();
    }

}
