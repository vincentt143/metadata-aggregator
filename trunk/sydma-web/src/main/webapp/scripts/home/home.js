Sydma = Sydma ? Sydma : {};

Sydma.home = {};

// linkTemplate in format <dd class="tempLink"><a
// href="/sydma/project/create?id={ID}">TEXT</a></dd> where {ID} to be replaced

/*
 * TODO: REFACTOR onLoad logic in SelectControl
 */
(function()
{

    var debug = Sydma.getDebug("home");
    var info = Sydma.getInfo("home");
    
    var linkId = 0;
    

    var getNewLinkId = function()
    {
        linkId++;
        return "home_action_link_" + linkId;
    };

    Sydma.home.getRestUrlGenerator = function(baseUrl)
    {
        var urlCreator = function(id)
        {
            return baseUrl.replace(/\{ID\}/, id);
        };
        return urlCreator;
    };

    Sydma.home.getParamUrlGenerator = function(baseUrl, idKey)
    {        
        var key = idKey ? idKey : "id";

        var urlCreator = function(id)
        {
            debug("ParamUrlCreator::adding param " + key + "=" + id);
            var paramData = {};
            
            paramData[key] = id;
            
            var param = jQuery.param(paramData);

            var url = baseUrl;
            var sep = '&';
            if (url.indexOf('?') < 0)
            {
                sep = '?';
            }
            else
            {
                var lastChar = url.slice(-1);
                if (lastChar == "?" || lastChar == "&")
                {
                    sep = '';
                }
            }
            url += sep + param;
            return url;
        };
        return urlCreator;
    };

    

    var onLoadParams = jQuery.deparam.fragment();
    debug("onLoadParams", onLoadParams);
    
    /*
     * contentData sourceSelect sideBar getChildUrl linkTemplates childControl
     * 
     */
    Sydma.home.SelectControl = function(opt)
    {

        var contentMap = {};
        var linkTemplates = {};
        var childControl = null;
        var $sourceSelect = jQuery(opt.sourceSelect);
        
        var getContentsOnLoad = opt.getContentsOnLoad;
        var getContentsUrl = opt.getContentsUrl;
        var onNew = opt.onNew;
        var onReset = opt.onReset;
        var onSelect = opt.onSelect;
        var paramVar = opt.paramVar; //ajaxStateRecord
        var parentParamVar = opt.parentParamVar; //ajaxStateRecord
        
        var selectedParentOnLoad = onLoadParams[parentParamVar];        
        var selectedOnLoad = onLoadParams[paramVar];

        var menuOpt =
        {
            "containerSelector" : opt.sideBar
        };
        var actionMenu = Sydma.actionMenu.createActionMenu(menuOpt);

        linkTemplates = opt.linkTemplates;
        childControl = opt.childControl;
        
        var getCurrentSelectedParent = function()
        {
            var ajaxState = jQuery.deparam.fragment();
            return ajaxState[parentParamVar];
        };
        
        var getCurrentSelected = function()
        {
            var ajaxState = jQuery.deparam.fragment();
            return ajaxState[paramVar];
        };
        
        var notifyChildren = function(selectedId)
        {
        	if (childControl != null)
    		{
  

        		childControl.parentSelected(selectedId);
    		}
        };
        
        var createActionVar = function(linkTemplate, sourceId)
        {           

            var url;
            if (jQuery.isFunction(linkTemplate.url))
            {
                url = linkTemplate.url(sourceId);
            }
            else
            {
                url = linkTemplate.url;
            }
            
            var onActionComplete = function()
            {
                //trigger select again, slightly hackish
                //TODO: Improve this logic
                
                var currentSelectedParent = getCurrentSelectedParent();
                selectedOnLoad = getCurrentSelected();
                getContentsFor(currentSelectedParent, populateOnLoad);
                
                var selected = $sourceSelect.find(":selected");
                debug("SelectControl::actionComplete, triggering select again", selected);
                selected.click();
                
            };
            
            var onLinkCreate = jQuery.noop;
            if (Sydma.useAjaxView && linkTemplate.ajaxView === true)
            {
                var actionControlOpt = 
                {
                    "onActionComplete": onActionComplete
                };
                var linkControl = Sydma.ActionControl.createActionControl(actionControlOpt);
                onLinkCreate = linkControl.linkCreateCallback;
            }
            
            var linkVar =
            {
                "url" : url,
                "title" : linkTemplate.linkText,
                "linkClass" : ""
            };

            var actionVar =
            {
                "onLinkCreate" : onLinkCreate,
                "linkVar" : linkVar,
                "linkData" : {}
            };

            return actionVar;
        };

        var checkPermission = function(linkKey, linkTemplate, permissions)
        {
            var requirePermission = linkTemplate.requirePermission;        
            if (requirePermission === false)
            {
                
                return true;
            }
            else
            {
                var linkPermissions = permissions[linkKey];

                if (linkPermissions === true)
                {
                    return true;
                }
            }
            return false;
        };
        
        var checkPreInsert = function(linkTemplate, sourceId)
        {
            var beforeInsert = linkTemplate.beforeInsert;
            if (jQuery.isFunction(beforeInsert))
            {
                var entryData = contentMap[sourceId];
                return beforeInsert(linkTemplate, entryData);
            }
            return true;
        };

        var addLinksToSidebar = function(sourceId)
        {

            var current = contentMap[sourceId];
            var permissions = current.permissionMap;
            //debug("Adding links to sidebar with permissions", permissions);


            var linksToAdd = [];
            for ( var linkKey in linkTemplates)
            {
                var linkTemplate = linkTemplates[linkKey];
               
                if (!checkPermission(linkKey, linkTemplate, permissions))
                {
                    continue;
                }                
                
                if (!checkPreInsert(linkTemplate, sourceId))
                {
                    continue;
                }
            
                // insertLink(linkKey, sourceId, linkPermissions);
                var linkTemplate = linkTemplates[linkKey];
                var linkVar = createActionVar(linkTemplate, sourceId);
                linksToAdd.push(linkVar);
            }
            actionMenu.addLinks(linksToAdd);
        };
        
        var performSelect = function(selectedId)
        {
        	addLinksToSidebar(selectedId);
            
            var entryData = contentMap[selectedId];
            if (jQuery.isFunction(onSelect))
            {
                onSelect(entryData);
            }
        };

        var entrySelect = function()
        {

            var selected = $sourceSelect.find(":selected");
            if (selected.length == 0)
            {
                debug("entrySelect::Nothing selected");
                return;
            }
            var sourceId = selected.val();
            
            performSelect(sourceId);
        	notifyChildren(sourceId);
    	           
            pushSelectedState(sourceId);
        };
        
        var getContentsFor = function(parentId, successOverride)
        {
        	var url;
        	if (parentId != null && parentId != "")
    		{
        		url = getContentsUrl.replace(/\{ID\}/, parentId);
    		}       
        	else
    		{
        		url = getContentsUrl;
    		}
        	
        	var onSuccess;
        	if (successOverride != null)
    		{
        		onSuccess = successOverride;
    		}
        	else
    		{
        		onSuccess = receiveNewEntries;
    		}
        	
        	
            debug("Getting child with url " + url);
            jQuery.ajax(
            {
                "url" : url,
                "data" : null,
                "dataType" : 'json',
                "success" : onSuccess,
                "error" : errorJson
            });
        };
        
        var keyToWatch = [37,38,39,40];
        var onKeyEvent = function(event)
        {
        	//we only want tab and arrows
        	if (jQuery.inArray(event.which, keyToWatch) != -1)
    		{
        		entrySelect();
    		}        	
        };

        $sourceSelect.bind("click ", entrySelect);
        $sourceSelect.bind("keyup", onKeyEvent);

        var errorJson = function(error)
        {
            info("Error::",error);
            // Error in Json Response. Refresh the page to take user to log in screen
            window.location.reload();
        };
        
        var insertEntries = function(data, toSelect)
        {
        	$sourceSelect.empty();
            contentMap = {};
            var optionHtml = "";
           
            for ( var i in data)
            {
                var currentContent = data[i];
                var selectAttr = "";
                if (toSelect != null && toSelect != "")
            	{
                	if (toSelect == currentContent.id)
            		{
                		selectAttr = "selected='true'";
            		}
            	}
                optionHtml += "<" + "option value=\"" + currentContent.id + "\" " + selectAttr + " title=\"" + currentContent.name + "\">"
                        + currentContent.name + "</" + "option>";
                contentMap[currentContent.id] = currentContent;
            }
            $sourceSelect.append(optionHtml);
        };

        var receiveNewEntries = function(data)
        {
            debug("SelectControl[" + opt.sourceSelect + "]::Received new entries ", data);
            
            insertEntries(data);
            
            actionMenu.clean();

            if (jQuery.isFunction(onNew))
            {
                onNew();
            }
        };
        
        var pushSelectedState = function(id)
        {
        	var state = {};
        	state[paramVar] = id;
        	debug("PushState", state); 
        	jQuery.bbq.pushState(state);
        };
        
        var removeSelectedState = function()
        {
        	jQuery.bbq.removeState(paramVar);
        };
        	
        var populateOnLoad = function(data)
    	{
        	insertEntries(data, selectedOnLoad);
        	if (selectedOnLoad != null && selectedOnLoad != "")
    		{
        		performSelect(selectedOnLoad);
    		}        	
    	};
   
        
        if (selectedParentOnLoad != null && selectedParentOnLoad != "")
    	{
        	debug("SelectControl::" + opt.sourceSelect + "::onLoadHasParentSelected::" + selectedParentOnLoad);
        	getContentsFor(selectedParentOnLoad, populateOnLoad);        	
    	}
        else
    	{
        	if (getContentsOnLoad)
        	{
            	getContentsFor(null, populateOnLoad);
        	}
    	}

        //insertEntries(opt.contentData); // intiate children on instantiation
        
        
        

        var hideSelect = function()
        {
        	removeSelectedState(); //reset param
            $sourceSelect.empty();
            contentMap = {};
            actionMenu.clean();
            if (onReset != null)
            {
                onReset();
            }
        };

        var focus = function()
        {
            $sourceSelect.focus();
        };
        
        var parentSelected = function(parentId)
        {
        	
            hideSelect(); // clear out ourself and children
            if (childControl != null)
        	{
            	childControl.hideSelect();
        	}
            getContentsFor(parentId);
        };

        var api =        
        {
            "receiveNewEntries" : receiveNewEntries, // public function
            "hideSelect" : hideSelect,
            "focus" : focus,
            "parentSelected" : parentSelected
        };
        return api;
    };

    Sydma.home.publishDatasetCheck = function(linkTemplate, datasetData)
    {
        //debug("Check need for Publish Dataset link ", datasetData);
        return datasetData.showAdvertiseLink;
    };

    Sydma.home.rejectAdvertisingDatasetCheck = function(linkTemplate, datasetData)
    {
        //debug("Check need for Advertise Dataset link ", datasetData);
        return datasetData.showRejectAdvertisingLink;
    };
    
    Sydma.home.notPhysicalCheck = function(linkTemplate, datasetData)
    {
        //debug("Check isPhysical ", datasetData);
        return !datasetData.physicalCollection;
    };
    

    Sydma.home.noSchemaCheck = function(linkTemplate, datasetData)
    {
        return datasetData.researchDatasetDBSchemaId === null;
    };
    
    Sydma.home.hasSchemaCheck = function(linkTemplate, datasetData)
    {
        return !(datasetData.researchDatasetDBSchemaId === null);
    };

})();