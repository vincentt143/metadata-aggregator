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

package au.org.intersect.sydma.webapp.util;

import java.io.Writer;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * An class for generating a result set to a csv file
 * 
 * @version $Rev: 29 $
 */

public class CSVHelper
{
    private static final Logger LOG = LoggerFactory.getLogger(CSVHelper.class);

    private static final boolean INCLUDE_HEADERS = true;
    private static final String CSV_FILE_EXTENSION = ".csv";
    private static final String DATE_FORMAT = "yyyyMMdd";
    private static final String ENCODE_FORMAT = "UTF-8";
    private static final String SPACE_FORMAT = "-";
    
    public static void writeCSVFile(Writer osWriter, ResultSet resultSet)
    {
        CSVWriter writer;
        try
        {
            writer = new CSVWriter(osWriter);
            writer.writeAll(resultSet, INCLUDE_HEADERS);
            writer.close();
        }
        catch (IOException e)
        {
            LOG.error(e.toString());
        }
        catch (SQLException e)
        {
            LOG.error(e.toString());
        }
    }

    public static String formFilename(String queryName)
    {
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Date date = new Date();
        String parseName = queryName.replaceAll("\\s", SPACE_FORMAT);
        try
        {
            parseName = URLEncoder.encode(parseName, ENCODE_FORMAT);
        }
        catch (UnsupportedEncodingException e)
        {
            LOG.error(e.toString());
        }
        String filename = parseName + SPACE_FORMAT + dateFormat.format(date) + CSV_FILE_EXTENSION;
        return filename;
    }
}
