Sydma.manageDataset = Sydma.manageDataset ? Sydma.manageDataset : {};

/**
 * A ManageTree that extends the ServerTree class for processing and
 * communicating with the server
 */
(function()
{

    /* static private */
    var moveableClass = "moveable";
    var moveableSelector = "." + moveableClass;

    var moveTargetClass = "moveTarget";
    var moveTargetSelector = "." + moveTargetClass;

    /**
     * initialize
     */
    Sydma.manageDataset.ManageTree = function()
    {
        Sydma.fileTree.ServerTree.apply(this, arguments);
    };

    /**
     * Extend ServerTree for browsing server but with different starting params
     * and hook in triggers when the nodes are clicked on
     */
    Sydma.manageDataset.ManageTree.prototype = new Sydma.fileTree.ServerTree();
    Sydma.manageDataset.ManageTree.prototype.constructor = Sydma.fileTree.ServerTree;

    Sydma.manageDataset.ManageTree.prototype.treeInitialized = function()
    {
        var treeNodeSelector = this.opt.treeSelector;

        var $treeNode = jQuery(treeNodeSelector);

        var manageTree = this;
        var onNodeSelect = function(event, data)
        {
            manageTree.onNodeSelect(event, data);
        };

        var onNodeMove = function(event, data)
        {
            manageTree.onNodeMove(event, data);
        };

        $treeNode.bind("select_node.jstree", onNodeSelect);
        $treeNode.bind("move_node.jstree", onNodeMove);
    };

    /**
     * Implementing method to extend the jsTree configuration
     * 
     * expects the extra property in inputOpt dropFinish : function(dragged,
     * target)
     */
    Sydma.manageDataset.ManageTree.prototype.getExtendTreeOpt = function(inputOpt)
    {
        var manageTree = this;
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
                        "title" : inputOpt.rootName,
                        "attr" :
                        {
                            "tabindex" : inputOpt.tabIndex
                        }
                    },
                    "metadata" :
                    {
                        "name" : inputOpt.rootName,
                        "absolutePath" : inputOpt.rootPath,
                        "fileType" : "DATASET",
                        "size" : "",
                        "creationDate" : ""                        
                    },
                    "attr" :
                    {
                        "id" : rootNodeId,
                        "class" : moveTargetClass
                    }
                }
            },
            "dnd" : // configure drag and drop
            {

            },
            "crrm" :
            {
                "move" :
                {
                    "check_move" : manageTree.checkMove
                }
            },
            "progressive_render" : true,
            "plugins" : [ "json_data", "ui", "themes", "cookies", "hotkeys", "types", "dnd", "crrm" ]
        // add dnd and crrm
        };
        return treeOpt;
    };

    Sydma.manageDataset.ManageTree.prototype.checkMove = function(data)
    {
              
        var position = data.p;
        //jstree fire 4 positions: before, inside, after and last
        if (position != "inside" && position != "last")     
        {
            //do not allow reordering
            return false;
        }
        var src = data.o;
        var target = data.r;       

        if (target.hasClass(moveTargetClass))
        {            
            //check if the src's parent is the target destination, ie. prevent moving within the same folder
            var srcParent = data.op; //li -> ul (parent1) -> li (true node parent)
            //Sydma.log("DEBUG::ManagetTree::checkMove::parent target", srcParent[0], target[0]);
            if (srcParent[0] == target[0])
            {
                return false;
            }
            return true;
        }
        return false;
    };

    Sydma.manageDataset.ManageTree.prototype.dropCheck = function(data)
    {
        var src = data.o;
        var target = data.r;

        var dropAllow = false;
        if (data.r.hasClass(moveTargetSelector))
        {
            dropAllow = true;
        }
        Sydma.log("DEBUG::ManagetTree::dropCheck ", dropAllow);
        return dropAllow;
    };

    Sydma.manageDataset.ManageTree.prototype.dragCheck = function(data)
    {
        var src = data.o;
        var target = data.r;

        var insideAllowed = this.dropCheck(data);

        var allowed =
        {
            "before" : false,
            "after" : false,
            "inside" : insideAllowed
        };
        Sydma.log("DEBUG::ManagetTree::dragCheck ", allowed);
        return allowed;
    };

    Sydma.manageDataset.ManageTree.prototype.dropFinish = function(data)
    {
        var src = data.o;
        var target = data.r;
        Sydma.log("DEBUG::ManagetTree::dropFinish ", src, target);
    };

    /**
     * extended jsTreeNodeBinder to add DnD class
     */
    Sydma.manageDataset.ManageTree.prototype.jsTreeNodeBinder = function(index, nodeData)
    {

        var treeNode = Sydma.fileTree.ServerTree.prototype.jsTreeNodeBinder.apply(this, [ index, nodeData ]);

        var fileType = nodeData.fileType;

        var addClass = "";
        // moveable
        if (fileType == "DIRECTORY" || fileType == "FILE")
        {
            addClass += " " + moveableClass;
            // addClass += " jstree-drag";
        }
        // targetable
        if (fileType == "DIRECTORY" || fileType == "DATASET")
        {
            addClass += " " + moveTargetClass;
            // addClass += " jstree-drop";
        }

        treeNode.attr["class"] = addClass;

        return treeNode;
    };

    Sydma.manageDataset.ManageTree.prototype.onNodeSelect = function(event, data)
    {
        var node = data.rslt.obj;
        var metadata = node.data("jstree");
        this.opt.onNodeSelect(node, metadata);
    };

    Sydma.manageDataset.ManageTree.prototype.onNodeMove = function(event, data)
    {
        Sydma.log("onNodeMove", data);
        var $movingNode = jQuery(data.rslt.o);
        var movingNodeOriginParentId = data.rslt.op.attr("id");
        var movingMetadata = $movingNode.data("jstree");
        var $destinationNode = jQuery(data.rslt.r);
        var movingNodeNewParentId = data.rslt.np.attr("id");
        var destinationMetadata = $destinationNode.data("jstree");

        Sydma.log("$movingNodeOriginParent", movingNodeOriginParentId);
        Sydma.log("$movingNodeNewParent", movingNodeNewParentId);
        
        var cancelMove = function()
        {
            // rollback
            jQuery.jstree.rollback(data.rlbk);
        };
        
        //we pass by ID as at this point jstree has DETACHED the nodes, passing by the jquery objects will potentially cause 
        //reference problems
        this.opt.onNodeMove(movingNodeOriginParentId, movingMetadata, movingNodeNewParentId, destinationMetadata, cancelMove);

    };

})();