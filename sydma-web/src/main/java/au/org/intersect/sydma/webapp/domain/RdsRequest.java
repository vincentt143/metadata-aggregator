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

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

/**
 * RDS Model.
 */
@RooJavaBean
@RooToString
@RooEntity(finders = {"findRdsRequestsByRequestStatus", "findRdsRequestsByNameEquals"}, persistenceUnit = "sydmaPU")
// TODO CHECKSTYLE-OFF: MagicNumber
public class RdsRequest
{

    @NotNull
    @Pattern(regexp = "\\p{Alnum}[\\w\\d\\s]*", message = "Must be alphanumeric")
    private String name;

    @NotNull
    @ManyToOne
    private ResearchSubjectCode subjectCode;

    @ManyToOne
    private ResearchSubjectCode subjectCode2;
    
    @ManyToOne
    private ResearchSubjectCode subjectCode3;
    
    @NotNull(message = "Principal Investigator is a required field")
    @OneToOne
    private User principalInvestigator;

    @NotNull(message = "Amount of Storage is a required field")
    @Max(99999)
    @Min(1)
    private Integer amountOfStorage;
    
    @NotNull
    @Size(max = 1000)
    @NotEmpty(message = "Description is a required field")
    private String description;

    @OneToOne
    private User dataManagementContact;

    @Enumerated(EnumType.STRING)
    private RdsRequestStatus requestStatus;
}
