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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;

/**
 * Entity to manage the permission activity logs
 * 
 * @version $Rev: 29 $
 */
@RooJavaBean
@RooToString(excludeFields = {"researchGroup", "user", "path"})
@RooJson
@RooEntity(persistenceUnit = "sydmaPU", finders = "findActivityLogsByResearchGroup")
// TODO CHECKSTYLE-OFF: MagicNumber
public class ActivityLog
{
    @NotNull
    private Date date;

    @Enumerated(EnumType.STRING)
    private Activity activity;

    @NotNull
    @Size(max = 20000)
    private String changes;

    @NotNull
    @ManyToOne
    private User principal;

    @NotNull
    @ManyToOne
    private ResearchGroup researchGroup;

    public String getDisplayDate()
    {
        DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss a");
        return dateFormat.format(this.date);
    }

    public static TypedQuery<ActivityLog> findActivityLogsByResearchGroup(ResearchGroup researchGroup)
    {
        if (researchGroup == null)
        {
            throw new IllegalArgumentException("The researchGroup argument is required");
        }
        EntityManager em = ActivityLog.entityManager();
        TypedQuery<ActivityLog> q = em.createQuery(
                "SELECT o FROM ActivityLog AS o WHERE o.researchGroup = :researchGroup ORDER by o.id DESC",
                ActivityLog.class);
        q.setParameter("researchGroup", researchGroup);
        return q;
    }
}
