Sydma.manageDataset = Sydma.manageDataset ? Sydma.manageDataset : {};

(function()
{

    var debug = Sydma.getDebug("manageDataset");
    var info = Sydma.getInfo("manageDataset");
    
    /**
     * returns an api to control the info menu,
     * showFileInfo($node, nodeData) //shows information contained in nodeData
     * and
     * clean //hides the menu
     * 
     * expects an existing menu on the page in the following format
     * <dl id="{infoMenuSelector}">
     *     <dt>Info
     *     <dd>
     *          <dl>
     *              <dt id="{key1}-header">
     *              <dd id="{key1}-content>
     *              
     *              <dt id="{key2}-header">
     *              <dd id="{key2}-content>
     *          
     * 
     */
    Sydma.manageDataset.createInfoMenu = function(infoMenuSelector)
    {
      
        var keys = ["annotation", "modificationDate", "size"];
        
        var valueMap = {};
        
        for (var i in keys)
        {
            var key = keys[i];
            var $header = jQuery("#" + key + "-header"); 
            var $content = jQuery("#" + key + "-content");
            valueMap[key] = 
            {
                "header" : $header,
                "content" : $content
            };
        }
        
        var $infoMenu = jQuery(infoMenuSelector);
        //on initialization hide
        $infoMenu.hide();
        
        
        
        
        var showField = function(key, headerContent, $node, nodeData)
        {            
            if (nodeData[key] == null)
            {
                headerContent.header.hide();
                headerContent.content.empty().hide();
                return false;
            }
            else
            {
                headerContent.header.show();
                var $p = jQuery("<p/>");
                
                var content = nodeData[key];
                debug("showField::Content for key " + key, content);
                
                if (typeof content === "string" )
                {                                        
                    content = content.replace(/\n/gi, "<br/>");                                                
                }

                $p.html(content);
                headerContent.content.empty().append($p).show();
                
                return true;
            }
        };
        
        /**
         * @public
         */
        var showFileInfo = function($node, nodeData)
        {
            var hasInfo = false;
            for (var key in valueMap)
            {
                if (showField(key, valueMap[key], $node, nodeData))
                {
                    //if showField return true we mark hasInfo as true
                    hasInfo = true;
                }                
            }
            if (hasInfo)
            {
                $infoMenu.show();        
            }
            else
            {
                $infoMenu.hide();
            }            
        };
        /**
         * @public
         */
        var clean = function()
        {
            //WARN: This is not a full clean, be careful if this run into problems later 
            $infoMenu.hide();
        };
        
        var api = 
        {
            "showFileInfo" : showFileInfo,
            "clean" : clean
        };
        return api;
    };
    
    Sydma.manageDataset.checkNoAnnotation = function(nodeData)
    {
        if (nodeData.annotation != null)
        {
            return false;
        }
        return true;
    };

    Sydma.manageDataset.checkAnnotation = function(nodeData)
    {
        if (nodeData.annotation != null)
        {
            return true;
        }
        return false;
    };
    
    
    
    /**
     * @private
     * @static
     */
    var createPathParam = function(nodeData, pathKey, extraParam, includeConnection)
    {
        var filePath = nodeData.absolutePath;
        var connectionId = nodeData.connectionId;
        var param = {};
        param[pathKey] = filePath;
        
        if (connectionId != null && includeConnection === true)
        {
            param["connectionId"] = connectionId;
        }
        
        if (extraParam != null)
        {
            jQuery.extend(param, extraParam);
        }
        
        var paramStr = jQuery.param(param);
        return paramStr;
    };
    
    /**
     * @public
     * @static
     */
    Sydma.manageDataset.getUrlCreator = function(baseUrl, optPathKey, extraParam, includeConnection)
    {
        var pathKey = optPathKey ? optPathKey : "filePath";
        var createFunc = function(nodeData)
        {
            var paramStr = createPathParam(nodeData, pathKey, extraParam, includeConnection);            
            var url = baseUrl + "?" + paramStr;
            return url;
        };
        return createFunc;
    };
    
    /**
     * @private
     * @static
     */
    var createNodeControl = function(actionMenu, opt, connectionId)
    {                
        /**
         * @private
         */
        var getOnActionComplete = function($node, linkOpt)
        {
            var linkOnComplete = linkOpt.onComplete;
            var onActionComplete = function()
            {
                //debug("onActionComplete", $node);

                if (jQuery.isFunction(linkOnComplete))
                {
                    linkOnComplete($node);
                }

            };
            return onActionComplete;
        };


        /**
         * @public
         * Function to create links in the side bar
         */
        var createActionForNode = function($node, nodeData)
        {
            actionMenu.clean();
            //debug("createActionForNode ", nodeData);
            var links = opt.links;
            
            
            var nodeType = nodeData.fileType;
            var nodePermissions = nodeData.allowedActions;
            debug("createActionForNode::Node Permissions ", nodePermissions);
            var linksToAdd = [];
            for ( var linkName in links)
            {
                var linkOpt = links[linkName];
                
                
                if (jQuery.isFunction(linkOpt.beforeInsert))
                {
                    var insertCheck = linkOpt.beforeInsert(nodeData);
                    if (insertCheck === false)
                    {
                        //if false is explicitly returned then we skip link creation
                        continue;
                    }
                }
                
                
                var linkForTypes = linkOpt.forTypes;

                var isForType = jQuery.inArray(nodeType, linkForTypes) >= 0;
                var hasPermission = linkOpt.bypassPermission || jQuery.inArray(linkName, nodePermissions) >= 0;
                if (isForType && hasPermission)
                {
                	var onComplete;
                	if(linkOpt.useAjax === false)
            		{
            			onComplete = jQuery.noop;           		
            		}
                	else
                	{                		
                		var onActionComplete = getOnActionComplete($node, linkOpt);
                		
                		var actionControlOpt = 
                		{
                				"onActionComplete": onActionComplete,
                				"ajaxData" : 
                				{
                					"connectionId" : connectionId
                				}
                		};
                		var linkControl = Sydma.ActionControl.createActionControl(actionControlOpt);
                		onComplete = linkControl.linkCreateCallback;
                	}
                	                	
                    var actionVar =
                    {
                        "linkVar" : linkOpt,
                        "linkData" : nodeData,
                        "onLinkCreate" : onComplete
                    };
                    linksToAdd.push(actionVar);
                }
            }
            actionMenu.addLinks(linksToAdd);
        };
        var api =
        {
            "createActionForNode" : createActionForNode
        };
        return api;
    };
    
    /**
     * @public
     * @static
     * @api
     *  entry point, the returned API object contains several methods
     *  which can then be passed into the options used when creating the tree and links
     */
    Sydma.manageDataset.createControlManager = function()
    {
        var fileTree = null; //private variable
        var actionMenu = null;
        var connectionId = null;
        var moveNodeOpt = null;
        var infoMenu = null;
        
        
        var createMoveNodeUrl = function(baseUrl, movingPath, destinationPath)
        {
            var param =
            {
                "movingPath" : movingPath,
                "destinationPath" : destinationPath
            };
            var paramStr = jQuery.param(param);
            return baseUrl + "?" + paramStr;            
        };
        
        /**
         * @private
         */
        var moveNode = function(movingNodeOriginParentId, movingMetadata, movingNodeNewParentId, destinationMetadata, rollbackFunc)
        {
            var moveNodeUrl = moveNodeOpt.moveNodeUrl;
            
            var movingPath = movingMetadata.absolutePath;
            var destinationPath = destinationMetadata.absolutePath;                      
            
            debug("moveNode::Moving from " + movingPath + " " + destinationPath);
            
            
            var onComplete = function()
            {
                actionMenu.clean();
                infoMenu.clean();
                
                //careful that the nodes MUST be fetched one at a time, otherwise the first refresh may detach the later node
                var $nodeOldParent = jQuery("#" + movingNodeOriginParentId);                
                fileTree.refresh($nodeOldParent);
                
                var $nodeNewParent = jQuery("#" + movingNodeNewParentId);
                fileTree.refresh($nodeNewParent);                                
            };
            
            var cancelAction = function()
            {
                debug("moveNode::Cancelling move");
                rollbackFunc();
            };
            
            var beforeClose = function(waitingAjax)
            {
                if (waitingAjax)
                {
                    alert("Server is currently processing the Move action, please wait for the action to complete");   
                    return false;
                }                
            };
            
            var actionOpt =
            {
                "onActionComplete" : onComplete, 
                "onActionCancel" : cancelAction,
                "beforeClose" : beforeClose,
                "ajaxData" : 
                {
                    "connectionId" : connectionId
                }
            };
            
            var actionControl = Sydma.ActionControl.createActionControl(actionOpt);
            
            
            var url = createMoveNodeUrl(moveNodeUrl, movingPath, destinationPath); 
            
            actionControl.doUrl(url);
            
            return true;
        };
        
        
        /**
         * @private
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
         * @private
         */
        var getNodeParent = function($node)
        {
            return $node.parent();
        };

        /**
         * @public
         */
        var onDeleteActionLinkComplete = function($node)
        {
            var $parentNode = getNodeParent($node);
            fileTree.refresh($parentNode);
            actionMenu.clean();
        };

        /**
         * @public
         */
        var onCreateActionLinkComplete = function($node)
        {
            fileTree.refresh($node);
        };
             


        /**
         * @public 
         * 
         * Create the browsing tree which browses group/project/dataset/... by
         * querying the server through ajax
         * 
         * Must be called after creating the Manager
         * 
         */
        var createBrowseTree = function(opt, linkOpt, moveNodeOptInput, inputInfoMenu)
        {
            moveNodeOpt = moveNodeOptInput;
            var actionMenuOpt =
            {
                "containerSelector" : linkOpt.actionMenuContainerSelector
            };

            actionMenu = Sydma.actionMenu.createActionMenu(actionMenuOpt);
            infoMenu = inputInfoMenu;
            
            var makeConnectionUrl = opt.connectUrl;
            
            // we don't create the tree until a connection has been established
            var onConnection = function(createdConnectionId)
            {
                connectionId = createdConnectionId;
                
                var treeId = opt.treeId;
                var $treeNode = jQuery(treeId);
                fileTree = new Sydma.manageDataset.ManageTree();                
                
                opt.connectionId = connectionId;

                var nodeControl = createNodeControl(actionMenu, linkOpt, connectionId);

                var onNodeSelect =  function($node, nodeData)
                {
                    nodeControl.createActionForNode($node, nodeData);
                    infoMenu.showFileInfo($node, nodeData);
                };
                
                opt.onNodeSelect = onNodeSelect;
                opt.onNodeMove = moveNode;
                fileTree.loadTree(opt);
            };
            createConnection(makeConnectionUrl, onConnection);

        };
        
        var api = 
        {
            "createBrowseTree" : createBrowseTree,
            "onDeleteActionLinkComplete" : onDeleteActionLinkComplete,
            "onCreateActionLinkComplete" : onCreateActionLinkComplete
        };
        return api;
    };

})();