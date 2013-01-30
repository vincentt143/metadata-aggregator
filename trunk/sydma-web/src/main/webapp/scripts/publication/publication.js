Sydma.publication = Sydma.publication ? Sydma.publication : {};

(function()
{

    var debug = Sydma.getDebug("Publication");

    var txtRemovePublication = "Delete Publication";
    var txtRemoveDetail = "Are you sure you want to delete this publication?";
    var txtRemove = "Delete";
    var txtCancel = "Cancel";
    
    var $currentDialog;
    
    var dialogSelector = "#publication-dialog";
    
    var oddClass = "odd";
    var evenClass = "even";

    var removeCurrentDialog = function()
    {
        if ($currentDialog != null)
        {
            $currentDialog.remove();
        }
    };
    

    var determineDialogPositionToPlace = function($againstEle)
    {
        var pos = $againstEle.offset();
        
        var x = pos.left + $againstEle.outerWidth();
        debug("Pos ", pos);
        var y = pos.top - jQuery(document).scrollTop();
        debug("Against element [" + x + "," + y + "]", $againstEle);            
        return [x, y];
    };
    
    Sydma.publication.init = function(opt)
    {
        var $publicationTBody = jQuery("#publication_list tbody");
        var $addPublication = jQuery("#add_publication");
        
        
        var renderAddUrl = opt.renderAddUrl;
        var renderEditUrl = opt.renderEditUrl;
        
        //if a dialog is left over from previous ajax, kill it
        removeCurrentDialog();
        
        //bind cleanup procedures for when lightbox changes or closes
        $publicationTBody.bind("remove", function(){removeCurrentDialog();});
        if (Sydma.ActionControl != null)
        {
            //unfortunately fancybox close doesn't trigger remove event, so we're adopting a listener registering system
            //hopefully this won't cause hanging child issues
            Sydma.ActionControl.registerOnCloseListener("publication", removeCurrentDialog);
        }
        
        var createDialog = function($dialog, inputOpt, $againstElement)
        {
            var dialogPosition = determineDialogPositionToPlace($againstElement);
            var dialogOpt =
            {
                "autoOpen" : true,
                "show" : "blind",
                "hide" : "explode",
                "zIndex" : 2000,
                "position" : dialogPosition,
                "close" : function()
                {
                    debug("CLOSE");
                    $(this).remove();
                    $currentDialog = null;
                } // remove dialog on close
            };
            jQuery.extend(dialogOpt, inputOpt);
            
            debug("Create Dialog against", $againstElement);
            removeCurrentDialog();
            $currentDialog = $dialog;
            $currentDialog.dialog(dialogOpt);            
        };

        /**
         * very brute force atm, possibly change to something more clever
         */
        var reIndex = function()
        {
            var reNumberInputs = function(rowIndex, row)
            {
                debug("Renumber Inputs", row);
                var $row = jQuery(row);
                
                if (rowIndex % 2 === 0)
                {
                    $row.removeClass(evenClass);
                    $row.addClass(oddClass);
                }
                else
                {
                    $row.removeClass(oddClass);
                    $row.addClass(evenClass);                    
                }                

                var reNumberInput = function(inputNum, input)
                {
                    var $input = jQuery(input);
                    var name = $input.attr("name");
                    
                    //replace the index. ie. whatever[1].else => whatever[2].else
                    name = name.replace(/\[\d+\]/, "[" + rowIndex + "]");
                    $input.attr("name", name);  
                };
                $row.find("input").each(reNumberInput);
            };

            $publicationTBody.find("tr").each(reNumberInputs);
        };

        var removePublication = function(event)
        {
            event.preventDefault();                       
            
            removeCurrentDialog();
            
            var $a = jQuery(this);
            var $row = $a.closest("tr");
            
            var $confirmation = jQuery("<div/>",
                    {
                        "title" : txtRemovePublication                      
                    });
            var $detail = jQuery("<p/>",
                    {
                        "text" : txtRemoveDetail
                    });
            var $removeButton = jQuery("<input/>",
                    {
                        "type" : "button",
                        "value" : txtRemove
                    });
            var $cancelButton = jQuery("<input/>",
                    {
                        "type" : "button",
                        "value" : txtCancel
                    });
            $confirmation.append($detail).append($cancelButton).append($removeButton);
            

            $cancelButton.bind("click", removeCurrentDialog);
            
            var doRemove = function()
            {
                $row.remove();
                removeCurrentDialog();
                reIndex();
                
            };
            $removeButton.bind("click", doRemove);            
            createDialog($confirmation, {"width" : "200px"}, $row);            
            return false;
        };
        
        var getOnValidEdit = function($row)
        {
            var onValidEdit = function($resp)
            {
                var $updatedRow = $resp.find("tr");
                $row.replaceWith($updatedRow);        
                reIndex();
            };
            return onValidEdit;
        };
        
        var getCreateEditDialog = function($row)
        {
            var createEditDialog = function(resp)
            {
                var $resp = jQuery(resp);     
                createDialog($resp.find(dialogSelector), null, $row);
                bindForm(getOnValidEdit($row));
            };
            return createEditDialog;
        };

        // gets the values from the table and pushing them into form in dialog box
        var editPublication = function(event)
        {
            event.preventDefault();

            var $a = jQuery(this);
            var $entryRow = $a.closest("tr");

            var $inputs = $entryRow.find("input");

            var entryData = {};
            
            $inputs.each(function(index, val)
            {
                var $input = jQuery(val);
                var name = $input.attr("name");
                // strip  modelAttr prefix
                name = name.replace(/publications\[\d+\]./, ""); 

                var value = $input.attr("value");
                // sanitize the input field
                $input.text(value).html();

                entryData[name] = value;
            });

            debug("Edit entry ", entryData);
            var opt =
            {
                "url" : renderEditUrl, 
                "data" : entryData,
                "success" : getCreateEditDialog($entryRow), 
                "dataType" : "html"
            };
            jQuery.ajax(opt);

            return false;
        };

        var addEntry = function($newRow)
        {
            $publicationTBody.find("tr.empty").remove();
            $publicationTBody.append($newRow);
        };

        var validateForm = function(resp, onValidation)
        {
            var $resp = jQuery(resp);
            if ($resp.find("form").length > 0)
            {
                var $dialog = jQuery(dialogSelector);
                $dialog.html($resp.find(dialogSelector).html());
                bindForm(onValidation);
            }
            else
            {
                onValidation($resp);
                removeCurrentDialog();
            }
        };

        var bindForm = function(onValidation)
        {
            var onSuccess = function(resp)
            {
                validateForm(resp, onValidation);
            };
            var opt =
            {
                "data" :
                {
                    "ajaxSource" : true,
                    "fragments" : "lightboxable"
                },
                "success" : onSuccess
            };
            jQuery(dialogSelector).find("form").ajaxForm(opt);
        };
        
        var onValidAdd = function($resp)
        {
            var $newRow = $resp.find("tr");
            debug("Append row ", $newRow);
            debug("Append table ", $publicationTBody);
            addEntry($newRow);
            reIndex();
        };

        var createAddDialog = function(resp)
        {
            var $resp = jQuery(resp);     
            createDialog($resp.find(dialogSelector), null, $addPublication);
            bindForm(onValidAdd);
        };

        var addPublicationDialog = function()
        {
            var opt =
            {
                "url" : renderAddUrl,
                "dataType" : "html",
                "success" : createAddDialog
            };
            jQuery.ajax(opt);
        };

        //delegation is used so events are automatically removed upon lightbox content change
        $publicationTBody.delegate(".remove_publication", "click", removePublication);
        $publicationTBody.delegate(".edit_publication", "click", editPublication);
        jQuery("#publication_section").delegate("#add_publication", "click", addPublicationDialog);
        
    };
})();