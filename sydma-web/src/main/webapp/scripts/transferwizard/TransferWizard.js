
Sydma.transfer = Sydma.transfer ? Sydma.transfer : {};

Sydma.transfer.TransferWizard = Sydma.transfer.TransferWizard ? Sydma.transfer.TransferWizard : {};

(function()
{   
    
    var debug = Sydma.getDebug("TransferWizard");
    var info = Sydma.getInfo("TransferWizard");
    
    var txtPaneInstruction = "Please select a connection type from the dropdown";
    
    var showToolbar = function($toolbar)
    {
        $toolbar.css("visibility", "visible");
    };
    
    var hideToolbar = function($toolbar)
    {
        $toolbar.css("visibility", "hidden");
    };
        
    
    var loadTreeOptions = function($canvas, $info, $toolbar, $select, paneOpt)
    {
        var currentPane, refreshAvailable;
        
        var paneOptions = Sydma.transfer.TransferPane.getPaneOptions(); 
        
        var $typeSelectContainer = jQuery("<div/>", 
                {
                    "class" : "transfer-type-select-container pane-slide transfer-slide-in-view"
                });
        var $typeSelectDescription = jQuery("<p/>",
                {
                    "text" : txtPaneInstruction,
                    "class" : "transfer-type-select-description"
                });
        $typeSelectContainer.append($typeSelectDescription);
        
        var $emptyOption = jQuery("<option/>",
                {
            "text" : "",
            "value" : ""
        });
        $select.append($emptyOption);
        jQuery.each(paneOptions, function(paneType, paneCreator)
                {
                    debug("Pane", paneType);
                    
                    var $option = jQuery("<option/>",
                            {
                                "text" : paneType,
                                "value" : paneType
                            });
                    $select.append($option);
                });
        
        $canvas.empty().append($typeSelectContainer);
        
        var createPane = function(paneName, paneCreator)
        {
            var copiedPaneOpt = jQuery.extend(true, {"refreshAvailable" : refreshAvailable}, paneOpt);
                                    
            var newPane = paneCreator($canvas, copiedPaneOpt);
            if (newPane == false)
            {
                //if paneCreator does not allow creation then short circuit
                return;
            }
            
            if (newPane.loadPane() != false)
            {
                showToolbar($toolbar);               
                currentPane = newPane;     
                $info.text("Connection: " + paneName);
            }                          
        };
        

        var resetPane = function(onReset)
        {
            debug("Reset pane");
            $info.empty();
            if (currentPane != null)
            {                
                var onDestroy = function()
                {
                    debug("On destroy");
                    currentPane = null;
                    hideToolbar($toolbar);
                    $refresh.css("visibility", "hidden");
                    onReset();
                };
                currentPane.destroy(onDestroy);                
            }            
            else
            {
                onReset();
            }            
        };
        
        var onSelectChange = function()
        {
            var val = $select.find("option:selected").val();
            debug("pane select ", val);
            
            var onReset = function()
            {

                var paneCreator = paneOptions[val];
                if (jQuery.isFunction(paneCreator))
                {
                    createPane(val, paneCreator);
                }
            };                        
            resetPane(onReset);                          
        };
        
        $select.bind("change", onSelectChange);
        
        var onRefresh = function()
        {
            if (currentPane != null)
            {
                currentPane.refresh();
            }
        };
        
        refreshAvailable = function()
        {
            $refresh.css("visibility", "visible");
        };
        
        var $refresh = $toolbar.find(".transfer-pane-refresh");
        $refresh.css("visibility", "hidden");
        
        //toolbar is hidden initially
        hideToolbar($toolbar);
        
        //wire reset & refresh
        
        $refresh.bind("click", onRefresh);        
        
        var getCurrentPane = function()
        {
            return currentPane;
        };
        
        var api = 
        {
            "getCurrentPane" : getCurrentPane
        };
        
        return api;
    };
    
    var createButtonTransfer = function(transferButtonSelector, doTransferCallback)
    {
        var fromPaneApi, toPaneApi;
        var $button = jQuery(transferButtonSelector);
        debug("BUTTON TRANSFER", $button);
        
        var fromHasSelected = false;
        var toHasSelected = false;        
        var canUpload = false;
        var isMyComputerConnection = false;
        $button.click(doTransferCallback);
        
        /**
         * verification is needed as jstree does not fire deselect event when switching selected nodes
         * ie. we cannot use a count of selected and deselected event to determine the number of selected nodes 
         */
        var verifyDeselection = function(paneApi, onDeselection)
        {
            var $selected = paneApi.getCurrentPane().getSelectedNodes();
            
            if ($selected.length == 0)
            {
                onDeselection();
            }                        
        };
        
        var considerShowButton = function()
        {
            if (isMyComputerConnection)
            {
                showButton();
            }
            else if (fromHasSelected && toHasSelected && canUpload)
            {
                showButton();
            }
            else
            {
                hideButton();
            }
        };
        
        var fromSelected = function()
        {
            fromHasSelected = true;
            considerShowButton();
        };
        
        var toSelected = function()
        {
            toHasSelected = true;
            considerShowButton();
        };        

        var fromDeselected = function()
        {
            fromHasSelected = false;
            hideButton();
        };
        
        var toDeselected = function()
        {
            toHasSelected = false;
            hideButton();
        };
        
        var showButton = function()
        {
            $button.attr("disabled", false);
        };
        
        var hideButton = function()
        {
            $button.attr("disabled", true);
        };
        

        var onFromSelect = function (nodeData)
        {
            debug("From Select", nodeData);
            fromSelected();
        };
        
        var onFromDeselect = function (nodeData)
        {
            verifyDeselection(fromPaneApi, fromDeselected);
        };
        
        var onFromDestroy = function()
        {
            fromDeselected();
        };
        
        var onToSelect = function (nodeData)
        {
            // Determine if the destination connection is PC. This should allow upload regardless of permission
            nodeData.connectionId == -1 ? isMyComputerConnection = true : isMyComputerConnection = false;
            canUpload = nodeData.canUpload;
            toSelected();    
        };
        
        var onToDeselect = function (nodeData)
        {
            nodeData.connectionId == -1 ? isMyComputerConnection = true : isMyComputerConnection = false;
            canUpload = nodeData.canUpload;
            verifyDeselection(toPaneApi, toDeselected);
        };
        
        var onToDestroy = function()
        {
            toDeselected();
        };
        
        var setFromPane = function(paneApi)
        {
            fromPaneApi = paneApi;
        };
        
        var setToPane = function(paneApi)
        {
            toPaneApi = paneApi;
        };
        
        hideButton(); //start off hidden
                
        var api =
        {
            "onFromSelect" : onFromSelect,
            "onFromDeselect" : onFromDeselect,
            "onFromDestroy" : onFromDestroy,
            "onToSelect" : onToSelect,
            "onToDeselect" : onToDeselect,
            "onToDestroy" : onToDestroy,
            "setFromPane" : setFromPane,
            "setToPane" : setToPane
        };
        return api;
    };
        
    Sydma.transfer.TransferWizard.init = function(inputOpt)
    {
        var fromPaneApi, toPaneApi;
        var opt = {};
        jQuery.extend(true, opt, inputOpt);
        
        var transferActionControl = Sydma.transfer.TransferAction.createTransferControl(opt.transferOpt);
        
        var $fromPane = jQuery(opt.fromSelector);
        var $toPane = jQuery(opt.toSelector);
        var $fromToolbar = jQuery(opt.fromToolbarSelector);
        var $toToolbar = jQuery(opt.toToolbarSelector);
        var $fromSelect = jQuery(opt.fromSelectSelector);
        var $toSelect = jQuery(opt.toSelectSelector);
        var $fromInfo = jQuery(opt.fromInfoSelector);
        var $toInfo = jQuery(opt.toInfoSelector);

        var doTransfer = function()
        {
            var fromPane = fromPaneApi.getCurrentPane();
            var toPane = toPaneApi.getCurrentPane();                       
            
            if (fromPane == null || toPane == null)
            {
                //shouldn't end up here, just to be safe
                return;
            }
            var srcConnectionId = fromPane.getConnectionId();
            var destConnectionId = toPane.getConnectionId();
            
            var fromNodesData = fromPane.getSelectedNodesData();
            var toNodesData = toPane.getSelectedNodesData();
            
            var srcPaths = [];
            for (var i in fromNodesData)
            {
                var srcPath = fromNodesData[i].absolutePath;
                srcPaths.push(srcPath);
            }
            
            var destPath = toNodesData[0].absolutePath;
            
            transferActionControl.doTransfer(srcPaths, srcConnectionId, destPath, destConnectionId);
        };
        
        var transferButtonControl = createButtonTransfer(opt.transferButtonSelector, doTransfer);

        
        
        var fromTreeSetting = 
        {
            "tabIndex" : 10,
            "limitSelection" : -1, // turn on multi select restriction
            "allowEntitySelect" : false,
            "allowFileSelect" : true,            
            "allowDatasetSelect" : false,
            "allowDirectorySelect" : true,
            "onSelect" : transferButtonControl.onFromSelect,
            "onDeselect" : transferButtonControl.onFromDeselect
        };
        //TODO: Allow directory select - somehow get the permissions as well...
        var toTreeSetting = 
        {
            "tabIndex" : 20,
            "limitSelection" : 1, // turn on multi select restriction
            "allowEntitySelect" : false,
            "allowFileSelect" : false,            
            "allowDatasetSelect" : true,
            "allowDirectorySelect" : true,
            "onSelect" : transferButtonControl.onToSelect,
            "onDeselect" : transferButtonControl.onToDeselect
        };        
        
        var fromOpt =
        {
            "paneFlag" : Sydma.transfer.TransferPane.PANE_FROM,
            "treeOpt" : fromTreeSetting,
            "onNodeMove" : transferActionControl.doTransfer,
            "onDestroy" : transferButtonControl.onFromDestroy
        };
        
        
        fromPaneApi = loadTreeOptions($fromPane, $fromInfo, $fromToolbar, $fromSelect, fromOpt);
        
        var toOpt =
        {
            "paneFlag" : Sydma.transfer.TransferPane.PANE_TO,
            "treeOpt" : toTreeSetting,
            "onNodeMove" : transferActionControl.doTransfer,
            "onDestroy" : transferButtonControl.onToDestroy
        };
        toPaneApi = loadTreeOptions($toPane, $toInfo, $toToolbar, $toSelect, toOpt);    
        
        transferButtonControl.setFromPane(fromPaneApi);
        transferButtonControl.setToPane(toPaneApi);
    };    
    
    
})();

/**
 * Transfer Action
 */
Sydma.transfer.TransferAction = Sydma.transfer.TransferAction ? Sydma.transfer.TransferAction : {};

(function()
{   

    var debug = Sydma.getDebug("TransferAction");
    var info = Sydma.getInfo("TransferAction");
    
    var HD_CONNECT = -1;
    
    var txtErrorCreatingJob = "Error creating job: ";        
    var txtFailedToCancelJob = "Failed to cancel job";
    
    var txtServerError = "Server error";
    var txtErrorRetry = "Error, retrying...";
    var txtLockPage = "Download in progress";
    var initialized = false;
    
    /*
     * maps returned job status to corresponding status text to display, also used to determine whether a job progress should terminate
     */
    var jobStatusTerminate = 
    {
        "FINISHED" : function(job) { return getPercentageString(job) + " Finished";}, 
        "CANCELLED" : function(job) { return getPercentageString(job) + " Cancelled";}, 
        "ABORTED" : function(job) { return getPercentageString(job) + " Error";}
    };
    
    var jobStatusInProgress = 
    {
            "CREATED" : "Scoping job", 
            "MONITORING" : "Waiting for data", 
            "SCOPING" : "Scoping job",
            "COPYING" : function(job)
            {
                var totalTime = calculateTimeRemainingUnits(job.estimatedTimeRemaining);
                var displayedSpeed = calculateByteUnits(job.displayedAverageSpeed);
                return "Copying Speed: " + displayedSpeed + "<br>" + totalTime + " Remaining.";
            }
    };
    
    /*
     * tiny applet so user don't see it
     */
    var appletProps =
    {
        "width" : "16",
        "height" : "12"
    };
    
    var urlApplet;
    var urlTunnel;
    var urlCreateJob;
    var urlCancelJob;
    var urlStatusJob;    
    
    Sydma.transfer.TransferAction.setUrls = function(opt)
    {
        urlApplet = opt.urlApplet;
        urlTunnel = opt.urlTunnel;
        urlCreateJob = opt.urlCreateJob;
        urlCancelJob = opt.urlCancelJob;
        urlStatusJob = opt.urlStatusJob;
    };
    
    /**
     * @private
     * 
     * increases the number of transfers requiring a page lock and lock the page
     */
    var preventPageUnload = function()
    {
    	if ((document.applet_unique_obj && document.applet_unique_obj.jobsRunning() > 0) ||
    		(document.applet_unique_emb && document.applet_unique_emb.jobsRunning() > 0)) {
            alert(txtLockPage);
            return 'Are you sure you wish to leave the page?';        		
    	}
    };
    window.onbeforeunload = preventPageUnload;
    
    /**
     * @private
     * 
     * pauses then sends off for a new update on the job
     * 
     */
    var createAjaxTimer = function(job, jobHandler)
    {
        var getUpdate = function()
        {
            var numErrors = 0;
            var onError = function()
            {
                numErrors = numErrors + 1;
                if (numErrors > 5)
                {
                	jobHandler.progressDisplay.showStatus('Error!');
                }
                else
                {
                	jobHandler.progressDisplay.showStatus(txtErrorRetry);
                    setTimeout(getUpdate, 5000);
                }
            };
            var onSuccess = function(newData)
            {
                if (newData == null)
                {
                    Sydma.defaultAjaxErrorHandler();
                }
                if (newData.data != null)
                {
                    var newJobData = newData.data;
                    processJobProgress(newJobData, jobHandler);
                }
                else
                {
                	jobHandler.progressDisplay.showStatus(txtServerError);
                }
            };
            var ajaxOpt =
            {
                url : urlStatusJob,
                dataType : 'json',
                data :
                {
                    jobId : job.jobId
                },
                success : onSuccess,
                error : onError
            };
            jQuery.ajax(ajaxOpt);
        };
        setTimeout(getUpdate, 1000);
    };

    var calculateTimeRemainingUnits = function(estimatedTimeRemaining)
    {

        var hourString;
        var minuteString;
        var secondString;
        var totalTime = '';

        var remainder = estimatedTimeRemaining % 3600;
        hourString = (estimatedTimeRemaining - remainder) / 3600;
        var r2 = remainder % 60;
        minuteString = (remainder - r2) / 60;
        secondString = r2;
        if (hourString < 10)
        {
            hourString = '0' + hourString;
        }
        if (minuteString < 10)
        {
            minuteString = '0' + minuteString;
        }
        if (secondString < 10)
        {
            secondString = '0' + secondString;
        }

        // converting variables into strings by adding a character.
        hourString += 'h: ';
        minuteString += 'm: ';
        secondString += 's.';

        totalTime = hourString.concat(minuteString, secondString);

        return totalTime;
    };

    var calculateByteUnits = function(displayedAverageSpeed)
    {

        var unit = 0;

        if (displayedAverageSpeed >= (1024 * 1024 * 1024))
        {
            unit = displayedAverageSpeed / (1024 * 1024 * 1024);
            unit = Math.round(unit * 100) / 100;
            unit += ' GBps';
            return unit;
        }
        else
            if (displayedAverageSpeed >= (1024 * 1024))
            {
                unit = displayedAverageSpeed / (1024 * 1024);
                unit = Math.round(unit * 100) / 100;
                unit += ' MBps';
                return unit;
            }
            else
                if (displayedAverageSpeed >= 1024)
                {
                    unit = displayedAverageSpeed / 1024;
                    unit = Math.round(unit * 100) / 100;
                    unit += ' KBps';
                    return unit;
                }
                else
                {
                    unit = displayedAverageSpeed;
                    unit = Math.round(unit * 100) / 100;
                    unit += ' Bps';
                    return unit;
                }
        return unit;
    };

    var getPercentageString = function(job)
    {
        return job.currentNumberOfFiles + " of " + job.totalNumberOfFiles + " [" + job.percentage + "%]";
    };
    
    /**
     * create a progress bar that displays information about the job transfer, returns an API
     * to update various parts of the progress bar
     */
    var createProgressDisplay = function($container, cancelCallback)
    {
        var $jobDetail = jQuery("<div/>",
                {
                    "class" : "transfer-progress-detail"
                });
        
        
        var $progressContainer = jQuery("<div/>",
                {
                    "class" : "transfer-progress"
                });
        
        var $progressBar = jQuery("<div/>",
                {
                    "class" : "transfer-progress-bar"
                });
        var $statusInfo = jQuery("<div/>",
                {
                    "class" : "transfer-progress-status-info"
                });
        var $appletContainer = jQuery("<div/>",
                {
                    "class" : "transfer-progress-applet"
                });
        var $cancel = jQuery("<a/>",
                {
                    "text" : "Cancel",
                    "class" : "transfer-progress-cancel clickable"
                });
        
        $progressContainer.append($jobDetail);
        $progressContainer.append($progressBar);
        $progressContainer.append($statusInfo);
        $progressContainer.append($cancel);
        $progressContainer.append($appletContainer);
        

        
        var doCancel = function()
        {
            $cancel.unbind("click");
            $cancel.css("visibility", "hidden");
            
            cancelCallback();            
        };

        $cancel.css("visibility", "hidden");

        $cancel.click(doCancel);
        
        $container.prepend($progressContainer);
        
        var showJobDetail = function(sources, destination)
        {
            var jobDetailMsg;
            if (sources == null || destination == null)
            {
                jobDetailMsg = "Initializing...";
            }
            else
            {
                var transferSrc = "";
                        
                for (var i in sources)
                {
                    transferSrc += sources[i] + " ";            
                }
                var transferDest = "";
                if (destination != null)
                {
                    transferDest = destination;
                }
                
                jobDetailMsg = transferSrc + " >>> " + transferDest;        
            }
            $jobDetail.text(jobDetailMsg);
        };

        
       
        var showStatus = function(status)
        {
            $statusInfo.html(status);
        };
        
        var showPercentage = function(percentage)
        {
            $progressBar.progressbar(
                    {
                        "value" : percentage
                    });  
        };
        
        var getAppletContainer = function()
        {
            return $appletContainer;
        };
        
        var hideCancel = function()
        {
            $cancel.css("visibility", "hidden");
        };
        
        var showCancel = function()
        {            
            $cancel.css("visibility", "visible");
        };
        
        
        var api = 
        {
            "showJobDetail" : showJobDetail,
            "showPercentage" : showPercentage,
            "showStatus" : showStatus,
            "getAppletContainer" : getAppletContainer,
            "showCancel" : showCancel,
            "hideCancel" : hideCancel
        };
        return api;
    };
    
    /**
     * @private
     * 
     * processes info about the job's progress and update the progress bar appropriately
     */
    var processJobProgress = function(job, jobHandler)
    {
        
        var jobStatusMsg = jobStatusTerminate[job.status];        
       
        jobHandler.progressDisplay.showPercentage(job.percentage);
        
        
        if (jobStatusMsg != null)
        {
            if (jQuery.isFunction(jobStatusMsg))
            {
                jobStatusMsg = jobStatusMsg(job);
            }
            
            jobHandler.progressDisplay.showStatus(jobStatusMsg);
            jobHandler.progressDisplay.hideCancel();
            jobHandler.terminate();
        }
        else
        {
            jobStatusMsg = jobStatusInProgress[job.status];
            if (jQuery.isFunction(jobStatusMsg))
            {
                jobStatusMsg = jobStatusMsg(job);
            }
            jobHandler.progressDisplay.showStatus(jobStatusMsg);
            
            createAjaxTimer(job, jobHandler);
        }        
    };
    
    /**
     * @private
     * 
     * 
     * creates a job. registers a job on the server and set an applet to handle the job's progress 
     * 
     */
    var createJob = function($statusCanvas, srcPath, srcConnectionId, destPath, destConnectionId)
    {
        var jobId;
        var progressDisplay, $appletContainer, jobHandler;
        var cancelFunc = function()
        {
            jQuery.ajax(
                    {
                        "type": 'POST',
                        "url" : urlCancelJob,
                        "dataType" : 'json',
                        "data" :
                        {
                            "jobId" : jobId
                        },
                        "success" : function(newData)
                        {
                            if (newData == null)
                            {
                                Sydma.defaultAjaxErrorHandler();
                            }
                            if (newData.data == null || newData.data != true)
                            {
                                progressDisplay.showStatus(txtFailedToCancelJob);
                            }
                        }
                    });  
        };
        
        progressDisplay = createProgressDisplay($statusCanvas, cancelFunc);  
        
        progressDisplay.showStatus("Initializing transfer...");
        
        var terminate = function()
        {
            debug("TERMINATE JOB");
        };
        
        jobHandler = 
        {
            "progressDisplay" : progressDisplay,
            "terminate" : terminate
        };        

        var createJobFailed = function(response)
        {            
            progressDisplay.showStatus(txtErrorCreatingJob + response.error);
        };               
        
        var createJobSuccess = function(response)
        {
            if (response.data == null)
            {
                progressDisplay.showStatus("Failed to initiate transfer on server");
                return;
            }
            
            jobHandler.progressDisplay.showJobDetail(response.data.transferSourceDisplay, response.data.transferDestinationDisplay);

            jobId = response.data.jobId;
            var executeJob = function()
            {
                applet.setJob(jobId, encJobId);                
            };
            if (srcConnectionId == HD_CONNECT || destConnectionId == HD_CONNECT)
            {
                var applet = Sydma.applet.findApplet();
                var encJobId = response.data.encJobId;
                Sydma.applet.appletReady(executeJob);   
            }
            var job = 
            {
                "status" : 'CREATED', 
                "jobId" : jobId, 
                "percentage" : 0, 
                "currentNumberOfFiles" : 0, 
                "totalNumberOfFiles" : 0
            };
            progressDisplay.showCancel();
            processJobProgress(job, jobHandler);
        };
        
        var params = 
        {
                "source_connectionId" : srcConnectionId,
                "destination_connectionId" : destConnectionId,
                "destination_item": destPath,
                "source_item": srcPath,
                "encode" : true
        };
        jQuery.ajax({
            "url": urlCreateJob,
            "data": params,
            "type": 'POST',
            "dataType": 'json',
            "traditional": true,
            "success": function(response) 
            {
                if (response == null || response.data == null) 
                {
                	if (response == null) { response = 'Failure: No Job ID'; }
                    createJobFailed(response);
                    
                } 
                else 
                {
                    
                    createJobSuccess(response);                        
                }
            }
        });
    };
    
    Sydma.transfer.TransferAction.createTransferControl = function(transferOpt)
    {
        
        var $statusCanvas = jQuery(transferOpt.canvasSelector);                
        
        
        /** 
         * @public
         */
        var initTransfer = function(srcPath, srcConnectionId, destPath, destConnectionId)
        {
            debug("Transfer srcPath " + srcPath + " srcCon " + srcConnectionId + " destPath " + destPath + " destCon " + destConnectionId);
            
            if (!initialized)
            {
                $statusCanvas.empty();
                initialized = true;
            }                        
            createJob($statusCanvas, srcPath, srcConnectionId, destPath, destConnectionId);
        };
        
        
        
        var api = 
        {
            "doTransfer" : initTransfer
        };
        return api;
    };
})();
