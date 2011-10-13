/** 
 * OO class that abstracts the backbone logic for select tree browsing
 * 
 * TODO: Prepend all methods that should not be publicly called with underscore
 */

Sydma.fileTree = Sydma.fileTree ? Sydma.fileTree : {};

(function()
{
    /** 
     * FileTree abstract class, you must create a subclass that implements the abstract functions
     * 
     */
    Sydma.fileTree.FileTree = function()
    {
             
    };
    
    /**
     * Most basic of basic jstree configurations
     */
    Sydma.fileTree.FileTree.prototype.generateDefaultTreeOpt = function(treeNodeId)
    {
        var saveOpenedCookieName = "jstree_open_" + treeNodeId;
        // delete tree state cookie
        $.cookie(saveOpenedCookieName, null);
                       
        var defaultTreeOpt =
        {          
            "json_data" :
            {
                data : null, // REMEMBER TO SET THIS
                ajax : false // REMEMBER TO SET THIS
            },
            "ui" :
            {
                "select_limit" : 1, // PROBABLY SHOULD SET THIS
                "disable_selecting_children" : true
            },
            "themes" :
            {
                "theme" : "classic"
            },
            "cookies" :
            {
                "save_selected" : false,
                "save_opened" : saveOpenedCookieName
            },
            "plugins" : [ "json_data", "ui", "themes", "cookies", "hotkeys", "types" ]
            
        };

        return defaultTreeOpt;
    };
    
    /**
     * Abstract function
     */
    Sydma.fileTree.FileTree.prototype.extractDataName = function(dataNode)
    {
        Sydma.log("Sydma.fileTree.FileTree.extractDataName is an abstract function, please implement it");
    };
    
    /**
     * Abstract function
     */
    Sydma.fileTree.FileTree.prototype.extractDataType = function(dataNode)
    {
        Sydma.log("Sydma.fileTree.FileTree.extractDataType is an abstract function, please implement it");
    };
    
    Sydma.fileTree.FileTree.prototype.nodeSort = function(obj1, obj2)
    {
        var name1 = this.extractDataName(obj1).toUpperCase();
        var name2 = this.extractDataName(obj2).toUpperCase();
        
        if (this.extractDataType(obj1) == "FILE" && this.extractDataType(obj2) == "DIRECTORY") // .metadata.fileType
            // =
            // DIRECTORY
            // |
            // FILE
            return 1;

        if (this.extractDataType(obj1) == "DIRECTORY" && this.extractDataType(obj2) == "FILE")
            return -1;

        if (name1.substring(0, 1) == ".")
        {
            if (name2.substring(0, 1) == ".")
            {
                if (name1 > name2)
                    return 1;
                if (name1 < name2)
                    return -1;
                return 0;
            }
            return 1;
        }
        if (name2.substring(0, 1) == ".")
            return -1;
        if (name1 > name2)
            return 1;
        if (name1 < name2)
            return -1;
        return 0;
    };
    
    Sydma.fileTree.FileTree.prototype.getAjaxData = function()
    {
        
        var fileTree = this;
        //closure
        var getAjaxDataFunc = function(node)        
        {
            Sydma.log("DEBUG::FileTree::getAjaxData::node", node);
            var nodeData = jQuery(node).data();
            var v =
            {
                "connectionId" : fileTree.connectionId,
                "path" : node == -1 ? "/" : nodeData.jstree.absolutePath
            };
            return v;
        };
        return getAjaxDataFunc;
    };
    
    
    Sydma.fileTree.FileTree.prototype.getAjaxError = function() 
    {
        var fileTree = this;
        return function(request, textStatus, errorThrown)    
        {
            //Sydma.log("DEBUG::Tree Ajax Errors ", textStatus, errorThrown);
            Sydma.defaultAjaxErrorHandler(request, textStatus, errorThrown);
        };
    };
    
    
    /**
     * Abstract Function, need to be implemented by child classes
     */
    Sydma.fileTree.FileTree.prototype.jsTreeNodeBinder = function()
    {
        Sydma.log("Sydma.fileTree.FileTree.jsTreeNodeBinder is an abstract function, please implement it");
    };
    
    /**
     * Iterate through the data while calling the data binder
     */
    Sydma.fileTree.FileTree.prototype.createJsTreeNodes = function(data, jsTreeNodeBinder)
    {
        var treeData = [];
        for(var i in data)
        {            
            var treeNode = this.jsTreeNodeBinder(i, data[i]);
            treeData.push(treeNode);
        }        
        return treeData; 
    };
    /**
     * On success ajax call to fetch the node's children
     * Sort them and then pass them to the createJsTree processor 
     * to convert the data into format understood by jstree
     * 
     */
    Sydma.fileTree.FileTree.prototype.getAjaxSuccess = function()
    {
        var fileTree = this;
        return function(response)
        {
            if (response.data != null)
            { 
                //closure function to maintain context of "this"
                var sortFunc = function(node1, node2)
                {
                    return fileTree.nodeSort(node1, node2);
                };
                response.data.sort(sortFunc);
                            
                var treeData = fileTree.createJsTreeNodes(response.data);            
                            
                return treeData;
            }
            else
            {
                Sydma.log("ERROR::Connection failed", response);
                jQuery("#messageDialog").showMessage(response.error,
                {
                    title : 'Connection Failed'
                });
            }
        };
    };
    
    /**
     * Abstract function
     */
    Sydma.fileTree.FileTree.prototype.getXhrFactory = function()
    {
        Sydma.log("Sydma.fileTree.FileTree.getXhrFactory is an abstract function, please implement it");
    };
    
    /**
     * For extending tree options in implementing class
     */
    Sydma.fileTree.FileTree.prototype.getExtendTreeOpt = function(opt)
    {
        return {};
    };

    Sydma.fileTree.FileTree.prototype.getTreeOpt = function()
    {
        
        var treeOpt = this.generateDefaultTreeOpt(this.opt.treeId);
        var limitSelection = (this.opt.limitSelection != null) ? this.opt.limitSelection : -1; //the default is not to limit them
        

        var disallowSelect = function()
        {
            return false;
        };
        var allowSelect = function()
        {
            return true;
        };
        
        var fileSelect;
        if (this.opt.allowFileSelect)
        {
            fileSelect = allowSelect;
        }
        else
        {
            fileSelect = disallowSelect;
        }
        

        var datasetSelect;
        if (this.opt.allowDatasetSelect)
        {
            datasetSelect = allowSelect;
        }
        else
        {
            datasetSelect = disallowSelect;
        }       
        
        var entitySelect;
        if (this.opt.allowEntitySelect)
        {
            entitySelect = allowSelect;
        }
        else
        {
            entitySelect = disallowSelect;
        }       
        
        var treeOptExtra = 
        {
            //initial load data
            "json_data" :
            {
                ajax : 
                {
                    url : this.opt.url,
                    xhr : this.getXhrFactory(),
                    data : this.getAjaxData(),
                    success : this.getAjaxSuccess(),
                    error : this.getAjaxError()
                }     
            },
            "ui" :
            {
                "select_limit" : limitSelection
            },
            "types" :
            {    
                "types" :
                {
                    "ENTITY" : //ENTITY represents VIRTUAL directories, usually means they are not selectable as they don't actually exist
                    {                     
                        "select_node" : entitySelect,
                        "icon" : 
                        {
                            "image" : "../scripts/filetree/resource/entity_dir_icon.png"
                        }
                    },

                    "DATASET" :
                    {
                        "select_node" : datasetSelect
                    },
                    "FILE" :
                    {
                        "select_node" : fileSelect
                    },
                    "DIRECTORY" :
                    {
                        "select_node" : true
                    },
                    "default" :
                    {
                        "select_node" : true
                    }
                }
            }
        };        
        jQuery.extend(true, treeOpt, treeOptExtra); // deep extend
        
        return treeOpt;
    };
    

    Sydma.fileTree.FileTree.prototype.refresh = function($node)
    {
        this.jstreeApi.refresh($node);
    };
    
    
    /**
     * Util function called after tree initialization is complete 
     * incase there are any other things you'd like to do to the tree
     */
    Sydma.fileTree.FileTree.prototype.treeInitialized = function()
    {
        
    };
    
    /**
     * The only method that should be called externally
     * 
     * 
     * expects the following in opt
        "treeSelector" :
        "tabIndex" :    //number to assign for the tabIndex navigation. ie, <li tabindex="X">....</li>
        "connectionId" : //defaults to -1. ie, local file browsing
        "limitSelection" : 1|-1 for limiting multi select or not
        "allowFileSelect" : true|false //defaults to allow
        "allowDatasetSelect" : true|false  //defaults to allow
        "allowEntitySelect" : true|false  //defaults to allow
        "enableDoubleClick" : true|false //defaults to true
     */
    Sydma.fileTree.FileTree.prototype.loadTree = function(inputOpt)
    {
        //saves opt, making it publicly accessible        
        this.opt = inputOpt;
        //Sydma.log("DEBUG::FileTree.loadTree::input", inputOpt);
        this.connectionId = inputOpt.connectionId ? inputOpt.connectionId : -1;
        //Sydma.log("DEBUG::FileTree.loadTree::connectionId " + this.connectionId + " " + inputOpt.connectionId);
        var treeNodeSelector = inputOpt.treeSelector;
        var tabIndex = inputOpt.tabIndex;
        
        var $treeNode = jQuery(treeNodeSelector);
        
        var treeOpt = this.getTreeOpt();
        
        //Sydma.log("DEBUG::FileTree.loadTree::initial", treeOpt);
        
        var extraTreeOpt = this.getExtendTreeOpt(inputOpt);

        //Sydma.log("DEBUG::FileTree.loadTree::extra", extraTreeOpt);
        
        jQuery.extend(true, treeOpt, extraTreeOpt); // deep extend
        
        Sydma.log("DEBUG::FileTree.loadTree::final", treeOpt);
        
        this.jstree = $treeNode.jstree(treeOpt);
        this.jstreeApi = jQuery.jstree._reference($treeNode);       
        
        //wire double clicking to open the clicked node
        if (inputOpt.enableDoubleClick != false)
        {
            var openNode = function()
            {
                $treeNode.jstree("open_node", this);
            };
            $treeNode.find("ul").delegate("li", "dblclick", openNode); 
        }
        
        this.treeInitialized();
    };
    
})();        

(function()
{

    /**
     * initialize
     */
    Sydma.fileTree.LocalTree = function()
    {
        Sydma.fileTree.FileTree.apply(this, arguments);
    };
    
    /**
     * Extend FileTree for local browsing 
     */
    Sydma.fileTree.LocalTree.prototype = new Sydma.fileTree.FileTree();
    Sydma.fileTree.LocalTree.prototype.constructor = Sydma.fileTree.FileTree;        
    
    /**
     * Implementation of jsTreeNodeBinder 
     */
    Sydma.fileTree.LocalTree.prototype.jsTreeNodeBinder = function(index, nodeData)
    {
        //Sydma.log("DEBUG::jsTreeNodeBinder::Data", nodeData);
        var metadata = nodeData.metadata;        
        
        var data = 
        {
            "title" : nodeData.data.title,
            "icon" : nodeData.data.icon,
            "attr" :
                {
                    "title" : nodeData.data.title //shows up as <a title="X">...</a>
                    
                }                
        };
        var attr = 
        {
            "id" : this.opt.treeId + "-" + nodeData.attr.id,
            "rel" : metadata.fileType,
            "tabindex" : this.opt.tabIndex //shows up as <li tabindex="X">...</li>
        };
        var treeNode = 
        {
            "attr" : attr,
            "data" : data,                        
            "metadata" : metadata,
            "state" : nodeData.state            
        };        
        //Sydma.log("DEBUG::LocalTree::jsTreeNodeBinder::Node", treeNode);
        return treeNode;
    };
    
    /**
     * function makeXhrForApplet(appletId) returns a XHR object with the
     * following implementation
     * 
     * xhr.open(type, s.url, s.async) : stores the URL as the 'method' to call
     * on the applet (see send below) xhr.abort() : just call the
     * onreadystatechange as it should, send is 'almost' synchronous
     * xhr.onreadystatechange : initially jQuery.noop xhr.readyState : just the
     * readyState property as per specs. xhr.getResponseHeader(header) : just
     * returns content-type json; no other headers are reported xhr.responseText :
     * the json text from the applet xhr.responseXML : nothing xhr.status : 200
     * after the applet returns xhr.send : calls the applet send(url, data),
     * where url is set in the open; url can be used to determine which
     * functionality is being called.
     * 
     * Specs for readyState (from wikipedia): The onreadystatechange event
     * listener If the open method of the XMLHttpRequest object was invoked with
     * the third parameter set to true for an asynchronous request, the
     * onreadystatechange event listener will be automatically invoked for each
     * of the following actions that change the readyState property of the
     * XMLHttpRequest object. - After the open method has been invoked
     * successfully, the readyState property of the XMLHttpRequest object should
     * be assigned a value of 1. - After the send method has been invoked and
     * the HTTP response headers have been received, the readyState property of
     * the XMLHttpRequest object should be assigned a value of 2. - Once the
     * HTTP response content begins to load, the readyState property of the
     * XMLHttpRequest object should be assigned a value of 3. - Once the HTTP
     * response content has finished loading, the readyState property of the
     * XMLHttpRequest object should be assigned a value of 4.
     * 
     * The major user agents are inconsistent with the handling of the
     * onreadystatechange event listener.
     */
    function makeXhrForApplet(applet)
    {
        // we may allow the applet to return a different content-type in the
        // future, for the time being, assumes this
        var headers =
        {
            'content-type' : 'application/json;charset=UTF-8'
        };
        var xhr =
        {
            readyState : 0
        };
        var appletParams = {};
        var open = function(type, url, async)
        {
            appletParams.method = url;
            xhr.readyState = 1;
        };
        var abort = function()
        {
            xhr.readyState = 4;
            xhr.onreadystatechange("abort");
        };
        var getResponseHeader = function(header)
        {
            return headers[header];
        };
        var send = function(data)
        {
            var times = 0;
            var tryOnce = function()
            {
                if (times > 5)
                {
                    xhr.readyState = 4;
                    xhr.status = 500;
                    xhr.responseText = '{"error":"applet not loaded or not working, try again."}';
                    xhr.onreadystatechange("timeout");
                    return;
                }
                if (applet != null && applet.isActive())
                {
                    xhr.responseText = applet.send(appletParams.method, data);
                    xhr.status = 200;
                    xhr.readyState = 4;
                    xhr.onreadystatechange(4);
                }
                else
                {
                    times++;
                    setTimeout(tryOnce, 1000);
                    return;
                }
            };
            setTimeout(tryOnce, 10);
        };
        xhr.open = open;
        xhr.setRequestHeader = function()
        {
        };
        xhr.abort = abort;
        xhr.getResponseHeader = getResponseHeader;
        xhr.onreadystatechange = function()
        {
        };
        xhr.send = send;
        return xhr;
    }
    
    /**
     * Implementation of abstract method
     */
    Sydma.fileTree.LocalTree.prototype.getXhrFactory = function()
    {
        var xhr = makeXhrForApplet(this.applet);
        var factory = function()
        {
            return xhr;
        };
        return factory;
    };
    
    /**
     * Implementing method to extend the jsTree configuration
     */
    Sydma.fileTree.LocalTree.prototype.getExtendTreeOpt = function(inputOpt)
    {
        var rootNodeId = inputOpt.treeId + "-n_";
        var treeOpt = 
        {
            "core" :
            {
                "initially_open" : [ rootNodeId ]
            },
            "json_data" :
            {
                data :
                {
                    "state" : "closed",
                    "data" :
                    {
                        "icon" : "directory",
                        "title" : "/",
                        "attr" : 
                        {
                            "title" : "/"
                            
                        }
                    },
                    "metadata" :
                    {
                        "name" : "/",
                        "absolutePath" : "/",
                        "fileType" : "ENTITY", 
                        "size" : "4096",
                        "creationDate" : ""
                    },
                    "attr" :
                    {
                        "id" : rootNodeId,
                        "tabindex" : inputOpt.tabIndex,
                        "rel" : "ENTITY" //root is entity as we do not want it to be selectable
                    }
                }
            }
        };
        return treeOpt;        
    };
    
    /**
     * Extended method to allow time for applet to load
     * 
     * 
     * Expects the following additional property in opt
            appletId : //id for the applet
     */
    Sydma.fileTree.LocalTree.prototype.loadTree = function(inputOpt)
    {
        var applet = Sydma.applet.findApplet(inputOpt.appletId);
        if (applet == null)
        {
            Sydma.log("DEBUG::Applet appears to be null???", jQuery("object#sydma-applet, embed#sydma-applet").length);
        }        
        this.applet = applet;
        
        var treeFile = this;
        
        var initialize = function()
        {            
            Sydma.fileTree.FileTree.prototype.loadTree.call(treeFile, inputOpt);           
        };
        Sydma.applet.appletReady(applet, initialize);
    };
    
    /**
     * Implementation to extract name from tne nodeDatafor sorting
     */
    Sydma.fileTree.LocalTree.prototype.extractDataName = function(dataNode)
    {
        return dataNode.metadata.name;
    };
    
    /**
     * Implementation to extract fileType from tne nodeData for sorting
     */
    Sydma.fileTree.LocalTree.prototype.extractDataType = function(dataNode)
    {
        return dataNode.metadata.fileType;
    };
    
})();       


/**
 * A ServerTree that extends the basic FileTree class for processing and communicating with the server
 * 
 * Note it expects the following additional opt in loadTree        
        "url" : //for listing the childs of a node
 */
(function()
{

    /**
     * initialize
     */
    Sydma.fileTree.ServerTree = function()
    {
        Sydma.fileTree.FileTree.apply(this, arguments);
    };
    
    /**
     * Extend FileTree for browsing server 
     */
    Sydma.fileTree.ServerTree.prototype = new Sydma.fileTree.FileTree();
    Sydma.fileTree.ServerTree.prototype.constructor = Sydma.fileTree.FileTree;     
    
    /**
     * ServerTree do not need to have a xhr factory since it uses the jquery default 
     */
    Sydma.fileTree.ServerTree.prototype.getXhrFactory = function()
    {

    };
    
    /**
     * private helper function to determine whether to set the node to close or leaf
     */
    var determineState = function(nodeData)
    {
        var type = nodeData.fileType;
        
        if (type == "ENTITY" || type == "DIRECTORY" || type == "DATASET")
        {
            return "closed";
        }
        if (type == "FILE")
        {
            return "null";
        }        
    };
    
    /**
     * private helper function to determine the icon the node should use
     */
    var determineIcon = function(nodeData)
    {
        var type = nodeData.fileType;
        
        if (type == "ENTITY" || type == "DIRECTORY" || type == "DATASET")
        {
            return "directory";
        }
        if (type == "FILE")
        {
            return "file";
        }     
    };
    
    /**
     * Implementation of jsTreeNodeBinder 
     */
    Sydma.fileTree.ServerTree.prototype.jsTreeNodeBinder = function(index, nodeData)
    {
        //Sydma.log("DEBUG::jsTreeNodeBinder::Data", nodeData);
        
        
        var state = determineState(nodeData);
        var icon = determineIcon(nodeData);
        
        var metadata = nodeData; //we use flat structure for server nodes so simple association will work
        
        var data = 
        {
                "title" : nodeData.name,
                "icon" : icon,
                "attr" : 
                {
                    "title" : nodeData.name //shows up as <a title="X">..</a>
                }
        };
        var attr = 
        {
            "id" : this.opt.treeId + "-" + nodeData.nodeId,
            "rel" : metadata.fileType, //enable types plugin
            "tabindex" : this.opt.tabIndex //shows up as <li tabindex="X">..</li>            
        };                
        
        var treeNode = 
        {
            "attr" : attr,
            "data" : data,                        
            "metadata" : metadata,
            "state" : state
        };        
        //Sydma.log("DEBUG::ServerTree::jsTreeNodeBinder::Node", treeNode);
        return treeNode;
    };
    
    /**
     * Extended tree opt
     */
    Sydma.fileTree.ServerTree.prototype.getExtendTreeOpt = function(inputOpt)
    {                        
        var treeOpt = 
        {            
            "progressive_render" : true            
        };
        return treeOpt;
    };
    
    /**
     * Implementation to extract name from tne nodeDatafor sorting
     */
    Sydma.fileTree.ServerTree.prototype.extractDataName = function(dataNode)
    {
        return dataNode.name;
    };
    
    /**
     * Implementation to extract fileType from tne nodeData for sorting
     */
    Sydma.fileTree.ServerTree.prototype.extractDataType = function(dataNode)
    {
        return dataNode.fileType;
    };

})();
