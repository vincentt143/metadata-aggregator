Sydma.manageDataset = Sydma.manageDataset ? Sydma.manageDataset : {};

(function()
{
   

    jQuery.ajaxSetup(
    {
        "data" : 
        {
            "ajaxSource" : false //don't use ajax view here yet until everything is migrated    
        }
          
    });
    Sydma.useAjaxView = false;
    
    /**
     * @private
     * @static
     */
    var createPathParam = function(nodeData)
    {
        var filePath = nodeData.absolutePath;
        var param =
        {
            "filePath" : filePath
        };
        var paramStr = jQuery.param(param);
        return paramStr;
    };
    
    /**
     * @public
     * @static
     */
    Sydma.manageDataset.getUrlCreator = function(baseUrl)
    {
        var createFunc = function(nodeData)
        {
            var paramStr = createPathParam(nodeData);
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
                Sydma.log("DEBUG::onActionComplete", $node);

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
            Sydma.log("DEBUG::ManageDataset::createActionForNode ", nodeData);
            var links = opt.links;

            var nodeType = nodeData.fileType;
            var linksToAdd = [];
            for ( var i in links)
            {
                var linkOpt = links[i];

                var linkForTypes = linkOpt.forTypes;

                var indexOf = jQuery.inArray(nodeType, linkForTypes);
                if (indexOf >= 0)
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
                    var actionVar =
                    {
                        "linkVar" : linkOpt,
                        "linkData" : nodeData,
                        "onLinkCreate" : linkControl.linkCreateCallback
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
            
            Sydma.log("DEBUG::managedataset::moveNode::Moving from " + movingPath + " " + destinationPath);
            
            
            var onComplete = function()
            {
                actionMenu.clean();
                
                //careful that the nodes MUST be fetched one at a time, otherwise the first refresh may detach the later node
                var $nodeOldParent = jQuery("#" + movingNodeOriginParentId);                
                fileTree.refresh($nodeOldParent);
                
                var $nodeNewParent = jQuery("#" + movingNodeNewParentId);
                fileTree.refresh($nodeNewParent);                                
            };
            
            var cancelAction = function()
            {
                Sydma.log("DEBUG::managedataset::moveNode::Cancelling move");
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
            
            Sydma.log("**** ACITO NCONTROL ID", connectionId);
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
        var createBrowseTree = function(opt, linkOpt, moveNodeOptInput)
        {
            moveNodeOpt = moveNodeOptInput;
            var actionMenuOpt =
            {
                "containerSelector" : linkOpt.actionMenuContainerSelector
            };

            actionMenu = Sydma.actionMenu.createActionMenu(actionMenuOpt);
            
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

                opt.onNodeSelect = nodeControl.createActionForNode;
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