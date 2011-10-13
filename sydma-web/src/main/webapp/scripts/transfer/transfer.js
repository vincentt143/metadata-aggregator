/**
 * Javascript for upload page
 * 
 */

// If Sydma then Sydma else new Obj
Sydma = Sydma ? Sydma : {};

Sydma.transfer = Sydma.transfer ? Sydma.transfer : {};

(function()
{
    // shared jsTree config

    Sydma.transfer.jstreeSelectedSelector = ".jstree-clicked";

    var localConnectionId = -1;
    var srcConfirmSelector = "#confirm_source";

    var destConfirmSelector = "#confirm_destination";

    
    var reload = function()
    {
        location.reload();
    };
    
    /**
     * Set the tree as focused
     */
    var focusTree = function(treeSelector)
    {
        var destination = jQuery.jstree._reference(treeSelector);
        destination.set_focus();
        jQuery(treeSelector).find("a").focus();
    };

    /**
     * Create a tree for browsing the local file system
     */
    var initLocalTree = function(treeId, opt)
    {
        var $treeNode = jQuery(treeId);
        $treeNode.data("connectionId", localConnectionId);

        $treeNode.addClass('_TREE_NODE_'); // this allow us to quickly find all
        // connection trees
        var label = 'My PC';

        opt.url = "list";
        opt.connectionId = localConnectionId;

        var localTree = new Sydma.fileTree.LocalTree();
        localTree.loadTree(opt);
        focusTree(treeId);
    };

    /**
     * Create a connection to the server for the server browsing tree
     */
    var createConnection = function(makeConnectionUrl, onConnectionCallback)
    {
        var onSuccess = function(resp)
        {
            var connectionId = resp.data;
            onConnectionCallback(connectionId);
        };
        jQuery.post(makeConnectionUrl, onSuccess, "json");
    };

    /**
     * Create a tree for browsing the server file system
     * 
     * Expects connectUrl in the opt
     */
    var initServerTree = function(treeId, opt)
    {
        var makeConnectionUrl = opt.connectUrl;
        // we don't create the tree until a connection has been established
        var onConnection = function(connectionId)
        {
            var $treeNode = jQuery(treeId);
            $treeNode.data("connectionId", connectionId);
            var serverTree = new Sydma.fileTree.ServerTree();
            opt.connectionId = connectionId;
            serverTree.loadTree(opt);

            focusTree(treeId);
        };
        createConnection(makeConnectionUrl, onConnection);
    };

    /**
     * Determine the type of tree to create for the tab and create it
     * accordingly
     */
    var createTreeForTab = function(index, opt)
    {
        var treeOpt;
        var treeSelector;
        if (index == 0)
        {
            treeOpt = opt.treeOpts.srcOpt;
            treeOpt.limitSelection = -1; // turn off multi select restriction
            treeOpt.allowEntitySelect = false;
            treeOpt.allowFileSelect = true;
            treeOpt.allowDatasetSelect = true;
        }
        else
            if (index == 1)
            {
                treeOpt = opt.treeOpts.destOpt;
                treeOpt.limitSelection = 1; // turn on multi select restriction
                treeOpt.allowEntitySelect = false;
                treeOpt.allowFileSelect = false;
                treeOpt.allowDatasetSelect = true;
            }
        treeSelector = treeOpt.treeSelector;

        if (treeOpt.treeType == "local")
        {
            initLocalTree(treeSelector, treeOpt);
        }
        else
            if (treeOpt.treeType == "server")
            {
                initServerTree(treeSelector, treeOpt);
            }
    };

    /*
     * Constructs a copy of the file tree which contains only the selected files
     * and attaches it to the target element
     */
    var buildSelectedTree = function(treeContainer, attachTo)
    {
        var $tree = jQuery(treeContainer);
        var treeClass = $tree[0].classList;

        var $copyOfTree = $tree.clone(false); // don't clone event/data

        var firstSelector = Sydma.transfer.jstreeSelectedSelector + ":first";
        var removeNonSelected = function(index, current)
        {
            var $selected = jQuery(current).find(firstSelector);
            if ($selected.length > 0)
            {
                // keep
            }
            else
            {
                jQuery(current).remove();
            }
        };
        $copyOfTree.find("li").each(removeNonSelected);

        $copyOfTree.find("*").removeAttr("id");// we dont' need id on copied
        // contents

        var $attachTo = jQuery(attachTo);
        $attachTo.empty();
        $attachTo.append($copyOfTree);
    };

    /*
     * Executes call to copy both selected local file tree and destination file
     * tree to the confirmation page
     */
    var doConfirm = function(opt)
    {
        var srcTreeSelector = opt.treeOpts.srcOpt.treeSelector;
        buildSelectedTree(srcTreeSelector, srcConfirmSelector);

        var destTreeSelector = opt.treeOpts.destOpt.treeSelector;
        buildSelectedTree(destTreeSelector, destConfirmSelector);
    };

    var getSelectedPath = function(tree)
    {
        var selectedNodes = tree.find(Sydma.transfer.jstreeSelectedSelector);

        var pathArr = [];
        var extractPath = function(i, ele)
        {
            var selected = jQuery(ele).parent();
            var selectedData = jQuery(selected).data();
            Sydma.log("DEBUG::extractPath", selected, selectedData);
            var path = selectedData.jstree.absolutePath;
            pathArr[pathArr.length] = path;
        };
        selectedNodes.each(extractPath);
        return pathArr;
    };

    var doTransfer = function(opt)
    {
        var srcTreeSelector = opt.treeOpts.srcOpt.treeSelector;
        var destTreeSelector = opt.treeOpts.destOpt.treeSelector;

        var $destinationTree = jQuery(destTreeSelector);
        var $sourceTree = jQuery(srcTreeSelector);

        var sourceItems = getSelectedPath($sourceTree);
        var destDir = getSelectedPath($destinationTree);

        var sourceConnection = $sourceTree.data().connectionId;
        var destConnection = $destinationTree.data().connectionId;

        Sydma.log("DEBUG::doTransfer::sourceItems", sourceItems);
        Sydma.log("DEBUG::doTransfer::destinationDir", destDir);

        var transferData =
        {
            "source_connectionId" : sourceConnection,
            "source_item" : sourceItems,
            "destination_connectionId" : destConnection,
            "destination_item" : destDir[0]
        };

        var appletWindow = null; // initialised before use to validate
        // !appletWindow check.

        var data = jQuery.param(transferData, true);
        appletWindow = window.open('../transfer/index?' + data, '_blank',
                'resizable=1,toolbar=1,location=1,menubar=1,status=1,width=950,height=650');

        /*
         * IF THE WINDOW DIDN'T OPEN (APPLETWINDOW == null) THEN ALERT USER THAT
         * THEIR POPUP BLOCKER HAS STOPPED THE SCRIPT RUNNING
         */
        if (appletWindow == null)
        { // && appletWindow.open == null
            $messageDialog
                    .showMessage(
                            "Unable to copy to your PC, This may be due to your browser's popup blocker. Please allow popups and try again.",
                            {
                                title : 'Failure To submit Job'
                            });
        }
    };

    
    /**
     * @public
     * 
     * entry point
     * 
     * expects the following in opt 
            "tabSelector" : 
            "invalidMsgs" : [], //array of strings presenting the error message to display for failure to enter the tab with index
                                //ie, failed attempt to enter the 2nd tab will display msg in invalidMsgs[1]
            "treeOpts" : 
                {
                    "srcOpt" :
                        {
                            "treeSelector" :
                            "treeType" : "local"|"server",
                            ...rest of the treeOpts for FileTree
                        },
                    "destOpt" :
                        {
                            "treeSelector" :
                            "treeType" : "local"|"server",
                            ...rest of the treeOpts for FileTree
                        }                                       
                }    
            
            
     */
    Sydma.transfer.createTab = function(opt)
    {

        var confirmHasSelected = function($tree)
        {
            var selected = jQuery($tree).find(Sydma.transfer.jstreeSelectedSelector);
            if (selected.length == 0)
            {
                return false;
            }
            return true;
        };

        var onSelect = function($tabs, index)
        {
            // don't need to do anything, we use onShow for things
        };

        var initializeTabContent = function(index)
        {
            if (index == 0 || index == 1)
            {
                createTreeForTab(index, opt);
            }
        };

        var initialized = [];
        var onShow = function($tabs, index)
        {
            if (initialized[index] != true)
            {
                initializeTabContent(index);
            }
            initialized[index] = true;

            if (index == 2)
            {
                doConfirm(opt);
            }
        };

        var invalidMsgs = opt.invalidMsgs;
        var showInvalidMsg = function(index)
        {
            alert(invalidMsgs[index]);
        };

        var tabOpt =
        {
            "tabSelector" : opt.tabSelector,
            "validateTabContent" : confirmHasSelected,
            "showInvalidMsg" : showInvalidMsg,
            "onSelect" : onSelect,
            "onShow" : onShow
        };
        Sydma.tabWizard.wizard(tabOpt);

        var onConfirm = function()
        {
            doTransfer(opt);
            reload();
        };
        jQuery("#confirm_finish").click(onConfirm);
    };

})();// instant execute

/**
 * Upload Progress
 * 
 * TODO: Refactor dms code
 */
(function()
{

    function createAjaxTimer(job, $cell)
    {
        var getUpdate = function()
        {
            var numErrors = 0;
            var onError = function()
            {
                numErrors = numErrors + 1;
                if (numErrors > 5)
                {
                    $cell.html('Error!');
                }
                else
                {
                    $cell.html('Error, retrying...');
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
                    Sydma.transfer.renderProgress(newJobData, $cell);
                }
                else
                {
                    $cell.html('Server error');
                }
            };
            var ajaxOpt =
            {
                url : '../jobs/jobStatus',
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
    }

    function calculateTimeRemainingUnits(estimatedTimeRemaining)
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
    }

    $.formatBytes = function(bytes)
    {
        var result;

        // we use 1000 instead of 1024 to be consistent with disk utilites
        var blockSize = 1000;

        if (bytes >= (blockSize * blockSize * blockSize))
        {
            result = bytes / (blockSize * blockSize * blockSize);
            result = Math.round(result * 100) / 100;
            result += ' GB';
        }
        else
            if (bytes >= (blockSize * blockSize))
            {
                result = bytes / (blockSize * blockSize);
                result = Math.round(result * 100) / 100;
                result += ' MB';
            }
            else
            {
                result = bytes / blockSize;
                result = Math.round(result * 100) / 100;
                result += ' KB';
            }

        return result;
    };

    function calculateByteUnits(displayedAverageSpeed)
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
    }

    function getPercentageString(job)
    {
        return job.currentNumberOfFiles + " of " + job.totalNumberOfFiles + " [" + job.percentage + "%]";
    }

    Sydma.transfer.renderProgress = function(job, $cell)
    {
        Sydma.log("DEBUG::Rendering progress", job);
        if (job.status in
        {
            'FINISHED' : 1,
            'CANCELLED' : 1,
            'ABORTED' : 1
        })
        {
            $cell.html(getPercentageString(job) + " "
                    + (job.status == 'FINISHED' ? 'Finished' : (job.status == 'ABORTED' ? 'Error' : 'Cancelled')));
        }
        else
        {
            $cell.html(
                    '<div class="pb"><span style="position:absolute; margin-left:10px; margin-top:2px"></span></div>')
                    .append('<div class="status"  style="float:left"></div>').append(
                            '<button id="jobCancel" class="clickable" style="float:right">Cancel</button>');
            $cell.find('div.pb span').html(getPercentageString(job));
            $cell.find('div.pb').progressbar(
            {
                value : job.percentage
            });

            var message = '';
            switch (job.status)
            {
                case 'CREATED':
                    message = 'Scoping job';
                    break;
                case 'MONITORING':
                    message = 'Waiting for data';
                    break;
                case 'SCOPING':
                    message = 'Scoping job';
                    break;
                case 'COPYING':
                    var totalTime = calculateTimeRemainingUnits(job.estimatedTimeRemaining);
                    var displayedSpeed = calculateByteUnits(job.displayedAverageSpeed);
                    message = 'Copying Speed: ' + displayedSpeed + '<br>' + totalTime + ' Remaining.' + '<br>';
                    break;
            }
            $cell.find('div.status').html(message);
            $cell.find('#jobCancel').click(function()
            {
                Sydma.log("Cancelling Job");
                jQuery.ajax(
                {
                    "type": 'POST',
                    "url" : '../transferJob/jobCancel',
                    "dataType" : 'json',
                    "data" :
                    {
                        "jobId" : job.jobId
                    },
                    "success" : function(newData)
                    {
                        if (newData == null)
                        {
                            Sydma.defaultAjaxErrorHandler();
                        }
                        if (newData.data == null || newData.data != true)
                        {
                            $cell.find('div.status').html('Couldn\'t cancel job');
                        }
                    }
                });
            });
            // creates a timer to refresh this
            createAjaxTimer(job, $cell);
        }
    };

})();// instant execute
