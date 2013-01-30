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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import au.org.intersect.sydma.webapp.domain.ResearchDataset;

public class TemporalDataValidatorTest
{
    private TemporalDataValidator temporalDataValidator;

    private DateTime dateMinus2;

    private DateTime dateMinus1;

    private DateTime dateCurrent;

    private DateTime datePlus1;


    private String dateFormat = "dd/MM/yyyy";

    private DateTimeFormatter formatter;

    private String tMinus2 = "09/11/2011";

    private String tMinus1 = "10/11/2011";

    private String tCurrent = "11/11/2011";

    private String tPlus1 = "12/11/2011";

    private String objectName = "dataset";

    private BindingResult results;

    @Before
    public void setUp()
    {
        results = Mockito.mock(BindingResult.class);

        temporalDataValidator = new TemporalDataValidator();
        formatter = DateTimeFormat.forPattern(dateFormat);

        dateMinus2 = formatter.parseDateTime(tMinus2);
        dateMinus1 = formatter.parseDateTime(tMinus1);
        dateCurrent = formatter.parseDateTime(tCurrent);
        datePlus1 = formatter.parseDateTime(tPlus1);

        calibrateCurrentTime(dateCurrent);
    }

    @Test
    public void testValidationCorrectBothNull()
    {
        DateTime fromDate = null;
        DateTime toDate = null;

        ResearchDataset dataset = new ResearchDataset();
        dataset.setDateFrom(fromDate);
        dataset.setDateTo(toDate);

        temporalDataValidator.validateResearchDatasetTemporalData(dataset, objectName, results);

        verify(results, never()).addError(any(ObjectError.class));
    }
    

    @Test
    public void testValidationCorrectFromTodayToToday()
    {
        DateTime fromDate = dateCurrent;
        DateTime toDate = dateCurrent;

        ResearchDataset dataset = new ResearchDataset();
        dataset.setDateFrom(fromDate);
        dataset.setDateTo(toDate);

        temporalDataValidator.validateResearchDatasetTemporalData(dataset, objectName, results);

        verify(results, never()).addError(any(ObjectError.class));
    }

    @Test
    public void testValidationCorrectM1P1()
    {
        DateTime fromDate = dateMinus1;
        DateTime toDate = datePlus1;

        ResearchDataset dataset = new ResearchDataset();
        dataset.setDateFrom(fromDate);
        dataset.setDateTo(toDate);

        temporalDataValidator.validateResearchDatasetTemporalData(dataset, objectName, results);
        
        ArgumentCaptor<FieldError> argument = ArgumentCaptor.forClass(FieldError.class);
        verify(results, times(1)).addError(argument.capture());

        List<FieldError> errors = argument.getAllValues();

        FieldError error = findErrorWithCode("DateTime.date.invalidAfterToday", errors);

        assertNotNull("Expected from date after today error not raised", error);
    }

    @Test
    public void testValidationCorrectM2M1()
    {
        DateTime fromDate = dateMinus2;
        DateTime toDate = dateMinus1;

        ResearchDataset dataset = new ResearchDataset();
        dataset.setDateFrom(fromDate);
        dataset.setDateTo(toDate);

        temporalDataValidator.validateResearchDatasetTemporalData(dataset, objectName, results);

        verify(results, never()).addError(any(ObjectError.class));
    }

    @Test
    public void testValidationFailFromDateAfterCurrent()
    {
        DateTime fromDate = datePlus1;
        DateTime toDate = null;

        ResearchDataset dataset = new ResearchDataset();
        dataset.setDateFrom(fromDate);
        dataset.setDateTo(toDate);

        temporalDataValidator.validateResearchDatasetTemporalData(dataset, objectName, results);

        ArgumentCaptor<FieldError> argument = ArgumentCaptor.forClass(FieldError.class);
        verify(results, times(1)).addError(argument.capture());

        List<FieldError> errors = argument.getAllValues();

        FieldError error = findErrorWithCode("DateTime.date.invalidAfterToday", errors);

        assertNotNull("Expected from date after today error not raised", error);
    }

    @Test
    public void testValidationFailFromDateAfterToDate()
    {
        DateTime fromDate = dateMinus1;
        DateTime toDate = dateMinus2;

        ResearchDataset dataset = new ResearchDataset();
        dataset.setDateFrom(fromDate);
        dataset.setDateTo(toDate);

        temporalDataValidator.validateResearchDatasetTemporalData(dataset, objectName, results);

        ArgumentCaptor<FieldError> argument = ArgumentCaptor.forClass(FieldError.class);
        verify(results, times(2)).addError(argument.capture());

        List<FieldError> errors = argument.getAllValues();
        
        FieldError dateFromError = findErrorWithCode("DateTime.dateFrom.invalidRange", errors);
        assertNotNull("Expected Validation Error not raised", dateFromError);
        
        FieldError dateToError = findErrorWithCode("DateTime.dateTo.invalidRange", errors);
        assertNotNull("Expected Validation Error not raised", dateToError);
    }

    private FieldError findErrorWithCode(String code, List<FieldError> errors)
    {
        for (FieldError error : errors)
        {
            if (code.equals(error.getCodes()[0]))
            {
                return error;
            }
        }
        return null;
    }

    private void calibrateCurrentTime(DateTime currentTime)
    {
        DateTimeUtils.setCurrentMillisFixed(currentTime.getMillis());
    }

}
