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

package au.org.intersect.sydma.webapp.validator;

//TODO CHECKSTYLE-OFF: ImportControl
import org.joda.time.DateTime;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import au.org.intersect.sydma.webapp.domain.ResearchDataset;

/**
 * Validate the date range specified in Research Dataset dateTo and dateFrom is valid
 */
public class TemporalDataValidator
{
    private static final String[] INVALID_DATE_FROM_CODES = {"DateTime.dateFrom.invalidRange"};
    private static final String[] INVALID_DATE_TO_CODES = {"DateTime.dateTo.invalidRange"};
    private static final String[] INVALID_DATE_START_CODES = {"DateTime.date.invalidAfterToday"};
    
    public void validateResearchDatasetTemporalData(ResearchDataset dataset, String objectName, BindingResult results)
    {
        DateTime dateFrom = dataset.getDateFrom();
        DateTime dateTo = dataset.getDateTo();
        validateTemporalRange(dateFrom, dateTo, objectName, results);
        validateDateBeforeCurrent(dateFrom, objectName, "dateFrom", results);
        validateDateBeforeCurrent(dateTo, objectName, "dateTo", results);
    }
    
    public void validateDateBeforeCurrent(DateTime date, String objectName, String fieldName, BindingResult results)
    {
        if (date == null)
        {
            return;
        }
        if (date.isAfterNow())
        {
            Object[] args = new Object[] {date};
            FieldError error = new FieldError(objectName, fieldName, date, false, INVALID_DATE_START_CODES,
                    args, "Date must not be after today");
            results.addError(error);
        }
            
    }

    public void validateTemporalRange(DateTime dateFrom, DateTime dateTo, String objectName, BindingResult results)
    {
 
        // if any of them is null then no need to validate
        if (dateFrom == null || dateTo == null)
        {
            return;
        }

        if (dateTo.isBefore(dateFrom))
        {
            Object[] fromArgs = new Object[] {dateFrom};
            FieldError fromError = new FieldError(objectName, "dateFrom", dateFrom, false, INVALID_DATE_FROM_CODES,
                    fromArgs, "From Date must be before To Date");
            results.addError(fromError);

            Object[] toArgs = new Object[] {dateFrom};
            FieldError toError = new FieldError(objectName, "dateTo", dateTo, false, INVALID_DATE_TO_CODES, toArgs,
                    "From Date must be before To Date");
            results.addError(toError);
        }

    }
}
