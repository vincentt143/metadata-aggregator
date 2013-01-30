Sydma.transfer = Sydma.transfer ? Sydma.transfer : {};

(function()
{
    
    var debug = Sydma.getDebug("TransferPane");
    var info = Sydma.getInfo("TransferPane");
    
    /**
     * @static
     * @private
     */
    var paneOptions = {};
    var paneId = 0;
    
    //configure transitionEnd event based on browser
    var vP = "";
    var transitionEnd = "transitionEnd";
    if (jQuery.browser.webkit) {
        vP = "-webkit-";
        transitionEnd = "webkitTransitionEnd";
    } else if (jQuery.browser.msie) {
        vP = "-ms-";
        transitionEnd = "msTransitionEnd";  
    } else if (jQuery.browser.mozilla) {
        vP = "-moz-";
        transitionEnd = "transitionend";
    } else if (jQuery.browser.opera) {
        vP = "-o-";
        transitionEnd = "oTransitionEnd";
    }   

    /**
     * @public
     * @constructor
     * 
     * @param $canvas
     * @param inputOpt
     *  {
     *      "paneFlag" : indicate whether this is a FROM or TO pane,
     *      "treeOpt" : opts to pass to FileTree class
     *  }
     */
    Sydma.transfer.TransferPane = function($canvas, inputOpt)
    {

        this.opt = 
        {
            "onNodeMove" : jQuery.noop,
            "onDestroy" : jQuery.noop
        };

        jQuery.extend(this.opt, inputOpt);
        this._panes = [];
        this.$canvas = $canvas;
        this._inTransition = false;
        this._transitionQueue = [];
        this._connectionId = null;
        this._fileTree = null; //to be assigned when a fileTree is initialized
        
        //null is checked as during child prototype defining, the super class constructor is called without params
        if ($canvas != null)
        {
            this._$currentPaneSlide = $canvas.children(":first");
        }
    };
    
    /**
     * @internal
     * 
     * variables
     */
    Sydma.transfer.TransferPane.prototype._$currentPaneSlide;
    Sydma.transfer.TransferPane.prototype._panes;

    Sydma.transfer.TransferPane.prototype._inTransition;

    Sydma.transfer.TransferPane.prototype._transitionQueue;
    
    /**
     * @private
     * 
     * util to obtain a unique id for a SLIDING pane
     */
    var getPaneId = function()
    {
        paneId++;
        return paneId;
    };

    /**
     * @public
     * 
     * @param infoText
     * 
     * @returns $pane
     * 
     * Panes MUST BE CREATED this way
     * 
     * 
     * Creates a pane that contains some header text and registers it with the Pane object
     * 
     * <div class="pane-slide server-slide">
     *     <p "transfer-notice">
     */
    Sydma.transfer.TransferPane.prototype.createPane = function(infoText)
    {
        var $div = jQuery("<div/>",
        {
            "id" : "transfer-pane-" + getPaneId(),
            "class" : "pane-slide server-slide"
        });
        if (infoText != null)
        {
            var titleContent = "";
            if (jQuery.isArray(infoText))
            {
                for ( var i in infoText)
                {
                    var text = infoText[i];
                    if (i > 0)
                    {
                        titleContent += "<br/>";
                    }
                    titleContent += text;
                }
            }
            else
            {
                titleContent = infoText;
            }
            var $p = jQuery("<p/>",
            {
                "class" : "transfer-notice"
            });
            $p.html(titleContent);
            $div.append($p);
        }
                
        this._panes.push($div);
        return $div;
    };

    /**
     * @public
     * @abstract
     * 
     * entry method
     * 
     */
    Sydma.transfer.TransferPane.prototype.loadPane = function()
    {
        debug("loadPane::Abstract method, please implement");
    };
    
    /**
     * @public
     * @abstract
     * 
     * refreshes the currently loaded tree (if there is one)
     * 
     */
    Sydma.transfer.TransferPane.prototype.refresh = function()
    {
        debug("refresh::Abstract method, please implement");
    };

    /**
     * @public
     * 
     * calling destroy will make the pane obj perform a transition back to the home pane, afterward it'll remove all of its panes from the dom
     */
    Sydma.transfer.TransferPane.prototype.destroy = function(onDestroy)
    {
        var $homePane = this.$canvas.children(":first");

        debug("Destroy", this._panes);
        this.opt.onDestroy();
        var onHomeShown = function()
        {
            for ( var i in this._panes)
            {
                var $pane = this._panes[i];
                debug("destroy::Destroying pane", $pane);
                $pane.remove();
            }
            if (jQuery.isFunction(onDestroy))
            {
                onDestroy();
            }
        };

        var opt =
        {
            "$pane" : $homePane,
            "direction" : Sydma.transfer.TransferPane.TRANSITION_R,
            "before" : jQuery.noop,
            "after" : onHomeShown
        };
        this._transitionPane(opt);
    };

    /**
     * @public
     */
    Sydma.transfer.TransferPane.prototype.getSelectedNodesData = function()
    {
        var nodeDatas = [];
        if (this._fileTree != null)
        {
            var $nodes = this._fileTree.getSelectedNodes();
            $nodes.each(function()
            {   
                var nodeData = jQuery(this).data("jstree");
                nodeDatas.push(nodeData);               
            });
        }
        return nodeDatas;
    };
    
    /**
     * @public
     */
    Sydma.transfer.TransferPane.prototype.getSelectedNodes = function()
    {
        if (this._fileTree != null)
        {
            return this._fileTree.getSelectedNodes();
        }
        return [];
    };
    

    /**
     * @public
     */
    Sydma.transfer.TransferPane.prototype.getConnectionId = function()
    {
        return this._connectionId;
    };
    
    /**
     * @internal
     * used to check if a transition is being performed
     * if there isn't one being performed then it'll perform a transition if one if available in the queue
     */
    Sydma.transfer.TransferPane.prototype._checkTransitionQueue = function()
    {        
        
        if (this._inTransition)
        {
            return;
        }

        if (this._transitionQueue.length > 0)
        {
            (this._transitionQueue.shift())();
        }
    };

    /**
     * @private
     * 
     * creates a transition closure which can be queued up and called later
     */
    var createTransition = function(context, inputOpt)
    {
        var transition = function()
        {
            context._inTransition = true;
            var opt =
            {
                "$pane" : null,
                "direction" : Sydma.transfer.TransferPane.TRANSITION_L, ///defaults to transition left
                "before" : jQuery.noop,
                "after" : jQuery.noop
            };
            jQuery.extend(opt, inputOpt);

            var $newPane = opt.$pane;
            var direction = opt.direction;
            var before = opt.before;
            var after = opt.after;

            var $currentPaneSlide = context._$currentPaneSlide;
            
            debug("transition::newPane", $newPane, "oldPane", $currentPaneSlide);

            var onComplete = function()
            {
                context._$currentPaneSlide = $newPane;
                context._inTransition = false;
                jQuery.proxy(after, context)();
                context._checkTransitionQueue();
            };

            //defaults to transition left
            var leftToSet = "";
            if (direction == Sydma.transfer.TransferPane.TRANSITION_R)
            {
                leftToSet = "100%";    
            }
            else if (direction == Sydma.transfer.TransferPane.TRANSITION_L)
            {
                leftToSet = "-100%";
            }
            else
            {
                log("WARNG::TransitionDirection is not set correctly");
            }
            
                
            //add the new pane to the canvas
            context.$canvas.append($newPane);

            jQuery.proxy(before, context)();

            //not using animation
            $currentPaneSlide.css("left", leftToSet);
            //IE7-hide the elements that are transitioned away from the pane
            $currentPaneSlide.css("visibility", "hidden");
            $newPane.css("left", "0");    
            onComplete();
            
            //use modernizer to detect whether the browser supports csstransition
            
            /*
            if (Modernizr.csstransitions)            
            {
                // animate sliding
                //csstransition need to be animated in sequence as current browser engine causes conflict
                //when applied simultaneously
                debug("Using CSS3 transition, adding animation style");
                
                var newPaneIn = function()
                {
                    $newPane.unbind(transitionEnd);
                    onComplete();
                };
                
                var currentPaneOut = function()
                {
                    $currentPaneSlide.unbind(transitionEnd);
                    $newPane.bind(transitionEnd, newPaneIn);
                    $newPane.css("left", "0");    
                };
                
                $currentPaneSlide.bind(transitionEnd, currentPaneOut);                    
                $currentPaneSlide.css("left", leftToSet);                                        
            }
            else
            {
                debug("Using old style jQuery animation");
                // use old style jquery animate
                var cyclicBarrier = Sydma.cyclicBarrier(2, onComplete);                
                
                $currentPaneSlide.animate(
                {
                    "left" : leftToSet
                }, cyclicBarrier);
                $newPane.animate(
                {
                    "left" : "0"
                }, cyclicBarrier);
            }
            */

        
        };
        return transition;
    };

    /**
     * @internal
     * 
     * create and registers a transition
     */
    Sydma.transfer.TransferPane.prototype._transitionPane = function(inputOpt)
    {
        var transition = createTransition(this, inputOpt);
        this._transitionQueue.push(transition);

        this._checkTransitionQueue();

    };

    /**
     * @internal 
     * 
     * 
     * @param selection
     *      array of the selection data obj, expects at least name in the data obj
     * @infoText 
     *      text to display at the top
     *      
     * 
     * creates a pane of the format
     *      <div class="pane-side">
     *          <p class="transfer-notice">{infoText}
     *          <ul class="transfer-list">
     *              <li>
     *                  <a class="transfer-list-item-fake">{limited data.text}
     *                  <a class="transfer-list-item" title="{data.text}">{limited data.text}
     *                  
     * Fake item is placed to allocate space within the li,                             
     * because transfer-list-item is placed absolutely within the li so it is taken out of the page flow,
     * and won't affect dimensions when it expands upon hover,
     * however that also means it will NOT take up space, which means li collapses.
     * Putting the fake item and setting it to visibility:invisible ensures li expands to correct dimension
     * 
     */
    Sydma.transfer.TransferPane.prototype._createSelection = function(selection, infoText, onClick)
    {
        
        var $div = this.createPane(infoText);

        var $ul = jQuery("<ul/>",
        {
            "class" : "transfer-list"
        });
        
        var selectionClicked = false;

        for ( var i in selection)
        {
            var selectData = selection[i];
            var $li = jQuery("<li/>");
            var displayText = Sydma.limitText(selectData.name);
            var $a = jQuery("<a/>",
            {
                "text" : displayText,
                "class" : "transfer-list-item",
                "title" : selectData.name,
                "tabindex" : 1,
                "href" : ""
            });
            var $fake = jQuery("<a/>",
            {
                "text" : displayText,
                "class" : "transfer-list-item-fake"
            });
            $li.append($fake);
            $li.append($a);
            $ul.append($li);

            $a.data("selectData", selectData);
            if (jQuery.isFunction(onClick))
            {
                //disable click event once a selection has already been clicked
                var checkClick = function(event)
                {
                    event.preventDefault();
                    if (!selectionClicked)
                    {
                        selectionClicked = true;
                        jQuery.proxy(onClick, this)(event);
                    }                    
                    else
                    {
                        debug("A selection has already been clicked");
                    }
                    return false;
                };
                $a.bind("click", $a, jQuery.proxy(checkClick, this));
            }
        }

        $div.append($ul);

        var afterTransit = function()
        {

        };

        var transitionOpt =
        {
            "$pane" : $div,
            "direction" : Sydma.transfer.TransferPane.TRANSITION_L,
            "after" : afterTransit
        };
        this._transitionPane(transitionOpt);
    };
    
    /**
     * @internal 
     * 
     * returns true if the pane is a FROM pane
     * 
     */
    Sydma.transfer.TransferPane.prototype._getEnableDnD = function()
    {
        return this.opt.paneFlag == Sydma.transfer.TransferPane.PANE_FROM;  
    };
    
    /**
     * @internal
     * 
     * handles a jstree move event by parsing out the path information from the src and dest node
     * 
     */
    Sydma.transfer.TransferPane.prototype._moveNode = function(e)
    {
        debug("Move Node Event ", e);
        
        var $src = e.o;
        
        // drop targets are the <a>, so we need to traverse back up to the <li> which is the proper jstree node
        var $dest = e.r.closest("li");
//        
//        debug("Src Node ", $src);
//        debug("Dest Node ", $dest);
        
        var srcPaths = [];
        
        $src.each(function()
                {
                    var srcData = jQuery(this).data("jstree");
                    var srcPath = srcData.absolutePath;
                    srcPaths.push(srcPath);
                });
        
        
        var destData = $dest.data("jstree");
        
        
        var srcConnectionId = this._connectionId;
        
        var destPath = destData.absolutePath;
        var destConnectionId = destData.connectionId;
//        
//
//        debug("Move path from ", srcData);
//        debug("Move pathe to ", destData);
        
        //pass the information through the callback
        this.opt.onNodeMove(srcPaths, srcConnectionId, destPath, destConnectionId);
        
        return false;
    };
    
    /**
     * @internal
     * 
     * supplied to jstree to check whether a move is legal.
     * the move's source cannot be entity or dataset
     * 
     * destination end dropping control is handled by the jstree-drop assignment in the destination tree
     * 
     */
    Sydma.transfer.TransferPane.prototype._checkMove = function(event)
    {
                
        var srcData = event.o.data("jstree");
        var tgtData = event.r.parent().data("jstree");
//        debug("_checkMove::srcData", srcData);
//        debug("_checkMove::tgtData", tgtData);
        
        if (srcData.fileType == "ENTITY" || srcData.fileType == "DATASET")
        {
            return false;
        }
        if (tgtData == null || tgtData.fileType == "ENTITY")
        {
            return false;
        }
        
        return true;
    };

    /**
     * @public
     * @static
     * 
     * Used to get what pane options has been registered
     */
    Sydma.transfer.TransferPane.getPaneOptions = function()
    {
        return paneOptions;
    };

    /**
     * @public
     * @static
     * 
     * @param key 
     *      name of the Pane Type
     * @param type
     *      a creator function for the pane type      
     * 
     * Used to register a pane option as available
     */
    Sydma.transfer.TransferPane.registerPaneType = function(key, type)
    {
        paneOptions[key] = type;
    };

    /**
     * @public
     * @static
     */
    Sydma.transfer.TransferPane.TRANSITION_R = "RIGHT";

    Sydma.transfer.TransferPane.TRANSITION_L = "LEFT";
    
    Sydma.transfer.TransferPane.PANE_FROM = "PANE_FROM";
    Sydma.transfer.TransferPane.PANE_TO = "PANE_TO";

})();

/**
 * Local File Pane
 */
(function()
{
    var debug = Sydma.getDebug("LocalPane");
    var info = Sydma.getInfo("LocalPane");    

    /**
     * @private
     * @static 
     */
    
    var uniqueInUse = false;
    
    var txtPaneName = "My Computer";
    
    var txtUniqueInUse = "Only one connection to My Computer is allowed at a time";
    
    var txtConnecting = "Connecting to My Computer";
    
    var appletUrl = "";

    var appletProps =
    {
        "width" : "16",
        "height" : "12",
    };

    var appletParams = {
        "tunnelUrl" : ""
    };

    var connectionId = -1;

    var url = "list";
    
    var instanceCount = 0;

    /**
     * initialize
     */
    Sydma.transfer.LocalPane = function()
    {
        Sydma.transfer.TransferPane.apply(this, arguments);
        instanceCount++;
        this.id = "local-pane-" + instanceCount;
        this._connectionId = connectionId;
    };

    /**
     * Extend LocalPane for browsing locally
     */
    Sydma.transfer.LocalPane.prototype = new Sydma.transfer.TransferPane();

    /**
     * @public 
     * @override
     */
    Sydma.transfer.LocalPane.prototype.destroy = function()
    {   
        uniqueInUse = false;
        Sydma.transfer.TransferPane.prototype.destroy.apply(this, arguments);        
    };
    
    /**
     * @internal
     * 
     * creates an applet in the given container
     */
    Sydma.transfer.LocalPane.prototype._createApplet = function($container)
    {
        // create applet
        debug("Create applet");
        Sydma.applet.insertApplet($container, appletUrl, appletProps, appletParams);
    };
    
    /**
     * @internal
     * creates a local tree
     * 
     */
    Sydma.transfer.LocalPane.prototype._createTree = function($treePane, onTreeInit)
    {
        var $treeContainer = jQuery("<div/>",
            {
                "class" : "transfer-tree-container"
            });
        $treePane.append($treeContainer);
        var treeOpt = this.opt.treeOpt;

        if (treeOpt == null)
        {
            info("_loadPane::WARNING::TreeOpt is empty", this.opt);
            treeOpt = {};
        }

        var enableDnD = this._getEnableDnD();
        
        var initialTreeOpt =
        {
            "connectionId" : connectionId,
            "url" : url,
            "treeSelector" : $treeContainer,
            "onTreeInit" : jQuery.proxy(onTreeInit, this),
            "enableDnD" : enableDnD,
            "DnDTarget" : !enableDnD,
            "treeOpt" :
            {
                "dnd" :
                {
                    "drop_finish" : jQuery.proxy(this._moveNode, this),
                    "drop_check" : jQuery.proxy(this._checkMove, this)
                },
                "crrm" :
                {
                    "move" : 
                    {
                        "check_move" : function () {return false;}
                    }
                }
                
            }
        };

        jQuery.extend(true, treeOpt, initialTreeOpt);

        $treePane.data("connectionId", connectionId);

        this._fileTree = new Sydma.fileTree.LocalTree();
        this._fileTree.loadTree(treeOpt);
        debug("Local Treee Loaded");
        $treePane.bind("move_node.jstree", this._moveNode);
    };
    
    /**
     * @public
     * @implementation
     * 
     * refreshes the currently loaded tree (if there is one)
     * 
     */
    Sydma.transfer.LocalPane.prototype.refresh = function()
    {
        if (this._fileTree != null)
        {
            this._fileTree.setFocus();
            this._fileTree.refresh();
        }        
    };

    /**
     * @public
     * @implementation
     * 
     */
    Sydma.transfer.LocalPane.prototype.loadPane = function()
    {        
        
        var $div = this.createPane(txtConnecting);

        var beforeTransit = function()
        {
            Sydma.ajaxSpinner($div.find("p"));
        };
      
        var transitionOpt =
        {
            "$pane" : $div,
            "direction" : Sydma.transfer.TransferPane.TRANSITION_L,
            "before" : beforeTransit
        };
        this._transitionPane(transitionOpt);
        
        
        this._createApplet($('#applet-container'));
        
        // create tree

        var $treePane = this.createPane();

        var showRefresh = function()
        {
            this.opt.refreshAvailable();  
        };
        var onTreeInit = function()
        {
            debug("LocalTree Init");
            var transitionOpt =
            {
                "$pane" : $treePane,
                "direction" : Sydma.transfer.TransferPane.TRANSITION_L,
                "after" : showRefresh
            };
            this._transitionPane(transitionOpt);
        };
        
        var self = this;
        Sydma.applet.appletReady(function() {
        	self._createTree($treePane, onTreeInit);
        });

    };

    /**
     * Used for configuring the url to use to access the local file via the applet
     * 
     * @public
     * @static
     * 
     * @param urlOpt
     */
    Sydma.transfer.LocalPane.setUrls = function(urlOpt)
    {
        appletUrl = urlOpt.appletUrl;
        appletParams["tunnelUrl"] = urlOpt.tunnelUrl;
    };

    /**
     * @public
     * @static
     * 
     * factory method registered for the given type. 
     * 
     * LocalPane has the added restriction that there can only ever be one instance of it in operation
     */
    var creator = function($pane, inputOpts)
    {
        if (uniqueInUse)
        {
            //Having multiple LocalPane is disallowed
            alert(txtUniqueInUse);
            return false;
        }
        uniqueInUse = true;
        return new Sydma.transfer.LocalPane($pane, inputOpts);
    };
    
    Sydma.transfer.LocalPane.registerPaneType = function()
    {
        Sydma.transfer.TransferPane.registerPaneType(txtPaneName, creator);  
    };

})();

/**
 * Server Pane
 */
(function()
{
    var debug = Sydma.getDebug("ServerPane");
    var info = Sydma.getInfo("ServerPane");

    /**private static**/

    var txtSelectGroup = "Select a Research Group";
    var txtSelectProject = "Select a Research Project";
    var txtSelectDataset = "Select a Research Dataset";
    var txtConnectServer = "Connecting to server";
    var txtPaneName = "Datasets";
    
    var txtJsonError = "Unexpected server response, your session may have timed out. Please try logging in again.";
    
    var instanceCount = 0;

    /**
     * initialize
     */
    Sydma.transfer.ServerPane = function()
    {
        Sydma.transfer.TransferPane.apply(this, arguments);
        instanceCount++;
        this.id = "server-pane-" + instanceCount;
    };

    /**
     * Extend TransferPane for browsing server
     */
    Sydma.transfer.ServerPane.prototype = new Sydma.transfer.TransferPane();

    /**
     * @internal
     * 
     */
    Sydma.transfer.ServerPane.prototype._currentPath = "ServerConnection: ";

    /**
     * @internal
     * 
     * used to determine which url this pane should use for server file browsing
     */
    Sydma.transfer.ServerPane.prototype._getListUrl = function()
    {
        if (this.opt.paneFlag == Sydma.transfer.TransferPane.PANE_TO)
        {
            return Sydma.transfer.ServerPane.listToUrl;
        }
        else if (this.opt.paneFlag == Sydma.transfer.TransferPane.PANE_FROM)
        {
            return Sydma.transfer.ServerPane.listFromUrl;
        }        
        else
        {
            info("WARNING::_getListUrl::paneFlag is not defined correctly in opt", this.opt);
        }
    };
    
    /**
     * @public
     * @implementation
     * 
     * refreshes the currently loaded tree (if there is one)
     * 
     */
    Sydma.transfer.ServerPane.prototype.refresh = function()
    {
        if (this._fileTree != null)
        {
            this._fileTree.setFocus();
            this._fileTree.refresh();    
        }                
    };
    
    Sydma.transfer.ServerPane.prototype._loadServerTree = function()
    {
     // create pane
        var $div = this.createPane();

        var $treeContainer = jQuery("<div/>",
                {
                    "class" : "transfer-tree-container"
                });

        $div.append($treeContainer);

        // create tree in pane
        this._fileTree = new Sydma.fileTree.ServerTree();

        var treeOpt = this.opt.treeOpt;

        if (treeOpt == null)
        {
            info("_loadDirectoryTree::WARNING::TreeOpt is empty", this.opt);
            treeOpt = {};
        }
        var enableDnD = this._getEnableDnD();
        
        //if this is a TO pane, then the dataset is droppable
        var datasetClass = "";
        if (this.opt.paneFlag == Sydma.transfer.TransferPane.PANE_TO)
        {
            datasetClass = "jstree-drop";
        }
        
        //not idea, too much of the initial node is hard coded  here, however it is somewhat difficult to easily
        //configure it in the server tree as there is a fair amount of customization
        var initialTreeOpt =
        {
            "url" : this._getListUrl(),
            "treeSelector" : $treeContainer,
            "connectionId" : this._connectionId,
            "enableDnD" : enableDnD,
            "DnDTarget" : !enableDnD,
            "treeOpt" :
            {
                "dnd" :
                {
                    "drop_finish" : jQuery.proxy(this._moveNode, this),
                    "drop_check" : jQuery.proxy(this._checkMove, this)
                },
                //use crrm to disable moving within own tree
                "crrm" :
                {
                    "move" : 
                    {
                        "check_move" : function () {return false;}
                    }
                },
                "json_data" :
                {
                    
                }
            }
        };

        jQuery.extend(true, treeOpt, initialTreeOpt);

        this._fileTree.loadTree(treeOpt);
        
        this._fileTree.jstree.bind("move_node.jstree", this._moveNode);        

        // transite pane
      
        var afterTransit = function()
        {
            this.opt.refreshAvailable();
        };
        var transitionOpt =
        {
            "$pane" : $div,
            "direction" : Sydma.transfer.TransferPane.TRANSITION_L,
            "after" : afterTransit
        };
        this._transitionPane(transitionOpt);
    };
    
    /**
     * @internal
     * 
     * creates a serverTree
     */
    Sydma.transfer.ServerPane.prototype._loadDirectoryTree = function(event)
    {
        var $a = event.data;
        var datasetData = $a.data("selectData");

        this._currentPath += "/" + datasetData.name;

        debug("_loadDataset::datasetData", datasetData);

        // create pane
        var $div = this.createPane(this._currentPath);

        var $treeContainer = jQuery("<div/>",
                {
                    "class" : "transfer-tree-container"
                });

        $div.append($treeContainer);

        // create tree in pane
        this._fileTree = new Sydma.fileTree.ServerTree();

        var treeOpt = this.opt.treeOpt;

        if (treeOpt == null)
        {
            info("_loadDirectoryTree::WARNING::TreeOpt is empty", this.opt);
            treeOpt = {};
        }
        var enableDnD = this._getEnableDnD();
        
        //if this is a TO pane, then the dataset is droppable
        var datasetClass = "";
        if (this.opt.paneFlag == Sydma.transfer.TransferPane.PANE_TO)
        {
            datasetClass = "jstree-drop";
        }
        
        //not idea, too much of the initial node is hard coded  here, however it is somewhat difficult to easily
        //configure it in the server tree as there is a fair amount of customization
        var initialTreeOpt =
        {
            "url" : this._getListUrl(),
            "treeSelector" : $treeContainer,
            "connectionId" : this._connectionId,
            "enableDnD" : enableDnD,
            "DnDTarget" : !enableDnD,
            "treeOpt" :
            {
                "dnd" :
                {
                    "drop_finish" : jQuery.proxy(this._moveNode, this),
                    "drop_check" : jQuery.proxy(this._checkMove, this)
                },
                //use crrm to disable moving within own tree
                "crrm" :
                {
                    "move" : 
                    {
                        "check_move" : function () {return false;}
                    }
                },
                "json_data" :
                {
                    data :
                    {
                        "state" : "closed",
                        "data" :
                        {
                            "icon" : "directory",
                            "title" : datasetData.name,
                            "attr" :
                            {
                                "title" : datasetData.name,  
                                "class" : datasetClass
                            }
                        },
                        "metadata" :
                        {
                            "name" : datasetData.name,
                            "absolutePath" : datasetData.absolutePath,
                            "fileType" : "DATASET",
                            "size" : null,
                            "creationDate" : "",
                            "connectionId" : this._connectionId
                        },
                        "attr" :
                        {
                            "id" : this.id + "-tree",
                            "tabindex" : treeOpt.tabIndex,
                            "rel" : "DATASET"
                        }
                    }
                }
            }
        };

        jQuery.extend(true, treeOpt, initialTreeOpt);

        this._fileTree.loadTree(treeOpt);
        
        this._fileTree.jstree.bind("move_node.jstree", this._moveNode);        

        // transite pane
      
        var afterTransit = function()
        {
            this.opt.refreshAvailable();
        };
        var transitionOpt =
        {
            "$pane" : $div,
            "direction" : Sydma.transfer.TransferPane.TRANSITION_L,
            "after" : afterTransit
        };
        this._transitionPane(transitionOpt);
    };

    /**
     * @internal
     * 
     * load up the available dataset selection
     */
    Sydma.transfer.ServerPane.prototype._loadDataset = function(event)
    {
        var $a = event.data;
        var projectData = $a.data("selectData");

        var data =
        {
            "path" : projectData.absolutePath,
            "connectionId" : this._connectionId
        };

        this._currentPath += "/" + projectData.name;
        var titleText = [ this._currentPath, txtSelectDataset ];
        var onDatasetRetrieval = function(resp)
        {
            var selection = resp.data;
            this._createSelection(selection, titleText, this._loadDirectoryTree);
        };
        var ajaxOpt = 
        {
            "url" : this._getListUrl(),
            "data" : data,
            "dataType" : "json",
            "success" : jQuery.proxy(onDatasetRetrieval, this),
            "error" : jQuery.proxy(this._jsonError, this)
        };
        jQuery.ajax(ajaxOpt);

    };

    /**
     * @internal
     * 
     * load up the available project selection
     */
    Sydma.transfer.ServerPane.prototype._loadProject = function(event)
    {
        var $a = event.data;
        var groupData = $a.data("selectData");


        var data =
        {
            "path" : groupData.absolutePath,
            "connectionId" : this._connectionId
        };

        this._currentPath += "/" + groupData.name;
        var onProjectRetrieval = function(resp)
        {
            var selection = resp.data;
            this._createSelection(selection, [ this._currentPath, txtSelectProject ], this._loadDataset);
        };
        var ajaxOpt = 
        {
            "url" : this._getListUrl(),
            "data" : data,
            "dataType" : "json",
            "success" : jQuery.proxy(onProjectRetrieval, this),
            "error" : jQuery.proxy(this._jsonError, this)
        };
        jQuery.ajax(ajaxOpt);

    };
    
    /**
     * @internal
     * 
     * load up the available group selection
     */
    Sydma.transfer.ServerPane.prototype._loadGroup = function()
    {
        var data =
        {
            "path" : "/",
            "connectionId" : this._connectionId
        };
        var onRootRetrieval = function(resp)
        {
            var selection = resp.data;
            this._createSelection(selection, [ this._currentPath, txtSelectGroup ], this._loadProject);
        };
        var ajaxOpt = 
        {
            "url" : this._getListUrl(),
            "data" : data,
            "dataType" : "json",
            "success" : jQuery.proxy(onRootRetrieval, this),
            "error" : jQuery.proxy(this._jsonError, this)
        };
        jQuery.ajax(ajaxOpt);
    };
    
    Sydma.transfer.ServerPane.prototype._jsonError = function(error)
    {
        var $div = this.createPane(txtJsonError);        

        var transitionOpt =
        {
            "$pane" : $div,
            "direction" : Sydma.transfer.TransferPane.TRANSITION_L
        };
        this._transitionPane(transitionOpt);        
    };

    /**
     * @internal
     * 
     * on establishing a connection to the server, associate the connectionId to the pane and proceed to load up groups
     */
    Sydma.transfer.ServerPane.prototype._onConnect = function(response)
    {
        if (response == null)
        {
            info("Failed to connect to server");
            return;
        }
        this._connectionId = response.data;
        debug("_onConnect::ConnectionId", this._connectionId);

        this._loadServerTree();
    };

    /**
     * @public
     * 
     */
    Sydma.transfer.ServerPane.prototype.loadPane = function()
    {
        var $div = this.createPane(txtConnectServer);

        var beforeTransit = function()
        {
            Sydma.ajaxSpinner($div.find("p"));
        };
        var afterTransit = function()
        {
            
        };
        var transitionOpt =
        {
            "$pane" : $div,
            "direction" : Sydma.transfer.TransferPane.TRANSITION_L,
            "before" : beforeTransit,
            "after" : afterTransit
        };
        this._transitionPane(transitionOpt);

        var onConnectWrap = function(response)
        {
            this._onConnect(response);
        };

        // start connection
        jQuery.post(this.getConnectUrl(), jQuery.proxy(onConnectWrap, this), "json");
    };

    Sydma.transfer.ServerPane.prototype.getConnectUrl = function()
    {
    	return Sydma.transfer.ServerPane.connectUrl;
    };
    
    /**
     * Used for configuring the urls to use to access the server, MUST BE CALLED before any ServerPanes are used
     * 
     * @public
     * @static
     * 
     * @param urlOpt
     */
    Sydma.transfer.ServerPane.setUrls = function(urlOpt)
    {
    	Sydma.transfer.ServerPane.listFromUrl = urlOpt.listFromUrl;
    	Sydma.transfer.ServerPane.listToUrl = urlOpt.listToUrl;
    	Sydma.transfer.ServerPane.connectUrl = urlOpt.connectUrl;
    };

    Sydma.transfer.ServerPane.listFromUrl = "";
    Sydma.transfer.ServerPane.listToUrl = "";
    Sydma.transfer.ServerPane.connectUrl = "";

    /**
     * @public
     * @static
     */
    var creator = function($pane, inputOpts)
    {
        return new Sydma.transfer.ServerPane($pane, inputOpts);
    };

    Sydma.transfer.ServerPane.registerPaneType = function()
    {
        Sydma.transfer.TransferPane.registerPaneType(txtPaneName, creator);  
    };

})();

/**
 * RStudio Pane
 */
(function()
{
    var debug = Sydma.getDebug("TransferPane");
    var info = Sydma.getInfo("TransferPane");
    
    var txtPaneName = "R Studio";

    var instanceCount = 0;

    /**
     * initialize
     */
    Sydma.transfer.RStudioPane = function()
    {
        Sydma.transfer.ServerPane.apply(this, arguments);
    };

    /**
     * Extend LocalPane for browsing locally
     */
    Sydma.transfer.RStudioPane.prototype = new Sydma.transfer.ServerPane();

    Sydma.transfer.RStudioPane.prototype._getListUrl= function() 
    {
    	return Sydma.transfer.RStudioPane.listUrl;
    };
    
    /**
     * @public
     * 
     */
    var creator = function($pane, inputOpts)
    {
        return new Sydma.transfer.RStudioPane($pane, inputOpts);
    };
    
    var alertNoRstudioAccount = function()
    {
    	alert("You do not have an R Studio account. To create one, please go to the R Studio panel.");
    };

    Sydma.transfer.RStudioPane.registerPaneType = function(hasRstudioAccount)
    {
    	if(hasRstudioAccount)
    	{
    		Sydma.transfer.TransferPane.registerPaneType(txtPaneName, creator);
    	}
    	else
    	{
    		Sydma.transfer.TransferPane.registerPaneType(txtPaneName, alertNoRstudioAccount);
    	}
    };

    Sydma.transfer.RStudioPane.prototype.getConnectUrl = function()
    {
    	return Sydma.transfer.RStudioPane.connectUrl;
    };

    Sydma.transfer.RStudioPane.setUrls = function(urlOpt)
    {
    	Sydma.transfer.RStudioPane.listUrl = urlOpt.listUrl;
    	Sydma.transfer.RStudioPane.connectUrl = urlOpt.connectUrl;
    };

    Sydma.transfer.RStudioPane.listUrl = "";
    Sydma.transfer.RStudioPane.connectUrl = "";

})();
