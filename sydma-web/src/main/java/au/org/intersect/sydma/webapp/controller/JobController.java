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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import au.org.intersect.dms.core.domain.JobItem;
import au.org.intersect.dms.core.domain.JobSearchResult;
import au.org.intersect.dms.core.service.DmsService;
import au.org.intersect.dms.core.service.dto.JobType;
import au.org.intersect.sydma.webapp.json.JsonResponse;
import au.org.intersect.sydma.webapp.security.SecurityContextFacade;

/**
 * Home controller, nothing for now
 */
@RequestMapping("/jobs/**")
@Controller
@Transactional("sydmaPU")
public class JobController
{
    @Autowired
    private DmsService dmsService;

    @Autowired
    private SecurityContextFacade securityContextFacade;

    @RequestMapping
    public String index()
    {
        return "jobs/index";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/jobStatus")
    @ResponseBody
    public String jobStatus(@RequestParam(value = "jobId") long jobId)
    {
        String username = securityContextFacade.getAuthorizedUsername();
        JobItem job = dmsService.getJobStatus(username, jobId);
        JobItemJson jobJson = new JobItemJson(job);
        JsonResponse jobResponse = new JsonResponse(jobJson, null);

        return jobResponse.toJson();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/listRecentJobs")
    @ResponseBody
    public String listRecentJobs(@RequestParam(value = "iDisplayStart") int startIndex,
            @RequestParam(value = "iDisplayLength") int pageSize, @RequestParam(value = "sEcho") int token)
    {
        String username = securityContextFacade.getAuthorizedUsername();
        JobSearchResult resp = dmsService.getJobs(username, startIndex, pageSize);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("totalRecords", resp.getTotalSize());
        result.put("sEcho", token);
        result.put("jobs", resp.getJobs());

        JsonResponse jobResponse = new JsonResponse(result, null);

        return jobResponse.toJson();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/jobCancel")
    @ResponseBody
    public String jobCancel(@RequestParam(value = "jobId") long jobId)
    {
        String username = securityContextFacade.getAuthorizedUsername();
        Boolean resp = dmsService.stopJob(username, jobId);

        JsonResponse jobResponse = new JsonResponse(resp, null);
        return jobResponse.toJson();
    }

    /**
     * Copy of JobItem for purpose of json serialization to get around the flexjson deep serialization issue
     * 
     * @version $Rev: 29 $
     */
    private final class JobItemJson
    {
        private static final int PERCENTAGE_MULTIPLIER = 100;

        private Long jobId;
        private String source;
        private List<String> sourceDirs;
        private String destination;
        private String destinationDir;
        private String createdTime;
        private String copyStartedTime;
        private String finishedTime;
        private Integer currentNumberOfFiles;
        private Integer totalNumberOfFiles;
        private Double percentage;
        private String status;
        private JobType type;
        private Double averageSpeed;
        private Double estimatedTimeRemaining;
        private Double displayedAverageSpeed;

        private JobItemJson(JobItem jobItem)
        {
            jobId = jobItem.getJobId();
            source = jobItem.getSource();
            sourceDirs = jobItem.getSourceDirs();
            destination = jobItem.getDestination();
            destinationDir = jobItem.getDestinationDir();
            createdTime = jobItem.getCreatedTime();
            copyStartedTime = jobItem.getCopyStartedTime();
            finishedTime = jobItem.getFinishedTime();
            currentNumberOfFiles = jobItem.getCurrentNumberOfFiles();
            totalNumberOfFiles = jobItem.getTotalNumberOfFiles();
            percentage = jobItem.getPercentage();
            status = jobItem.getStatus();
            type = jobItem.getType();
            averageSpeed = jobItem.getAverageSpeed();
            estimatedTimeRemaining = jobItem.getEstimatedTimeRemaining();
            displayedAverageSpeed = jobItem.getDisplayedAverageSpeed();
        }

        public Long getJobId()
        {
            return jobId;
        }

        public String getSource()
        {
            return source;
        }

        public List<String> getSourceDirs()
        {
            return sourceDirs;
        }

        public String getDestination()
        {
            return destination;
        }

        public String getDestinationDir()
        {
            return destinationDir;
        }

        public String getCreatedTime()
        {
            return createdTime;
        }

        public String getCopyStartedTime()
        {
            return copyStartedTime;
        }

        public String getFinishedTime()
        {
            return finishedTime;
        }

        public Integer getCurrentNumberOfFiles()
        {
            return currentNumberOfFiles;
        }

        public Integer getTotalNumberOfFiles()
        {
            return totalNumberOfFiles;
        }

        public Double getPercentage()
        {
            return percentage;
        }

        public String getStatus()
        {
            return status;
        }

        public JobType getType()
        {
            return type;
        }

        public Double getAverageSpeed()
        {
            return averageSpeed;
        }

        public Double getEstimatedTimeRemaining()
        {
            return estimatedTimeRemaining;
        }

        public Double getDisplayedAverageSpeed()
        {
            return displayedAverageSpeed;
        }

    }

}
