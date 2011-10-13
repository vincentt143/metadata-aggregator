/**
 * Js to handle action for opening the lightbox either directly or binding to a link, it also handles any actions within the lightbox
 */

Sydma.ActionControl = Sydma.ActionControl ? Sydma.ActionControl : {};

(function()
{
    /**
     * opt 
        onActionComplete : function()
        onActionCancel : function()
        ajaxData : {}
     */
    Sydma.ActionControl.createActionControl = function(inputOpt)
    {
        var opt = 
        {
            "showSpinner" : true,
            "beforeClose" : jQuery.noop,
            "onActionComplete" : jQuery.noop,
            "onActionCancel" : jQuery.noop,
            "ajaxData" : {}
        };
        opt = jQuery.extend(opt, inputOpt);
        
        var ajaxData = opt.ajaxData;
        
        var onActionComplete = opt.onActionComplete;
        var onActionCancel = opt.onActionCancel;
        var connectionId = opt.connectionId;
        var showSpinner = opt.showSpinner;
        var beforeClose = opt.beforeClose;
        
        var isComplete = false;
        
        var waitingAjax = false;
        
        var onCleanup = function()
        {
            return beforeClose(waitingAjax);
        };
        
        //checks if isComplete is true, if not fire cancel function
        var onClose = function()
        {
            if (!isComplete)
            {
                onActionCancel();
            }            
        };
        
        var getAjaxContainer = function()
        {
            return jQuery("#fancybox-content").find(".sydma_content");
        };
        
        var onSubmitResponse = function(resp, textStatus, jqXHR)
        {
            waitingAjax = false;
            var $resp = jQuery(resp);
            //Sydma.log("DEBUG::ActionControl::Ajax response", resp);
            //Sydma.log("DEBUG::ActionControl::Ajax status", textStatus);
            //Sydma.log("DEBUG::ActionControl::Ajax xhr", jqXHR);
            if ($resp.find("#ajaxSuccess").length > 0 || resp == "")
            {
                isComplete = true;
                // success
                if (jQuery.isFunction(onActionComplete))
                {
                    onActionComplete();
                }
                jQuery.fancybox.close();
            }
            else
            {
                var $ajaxContent = getAjaxContainer();
                $ajaxContent.replaceWith(resp);
                
                prepareForm();
            }
        };

        var onFormSubmit = function(event)
        {
            var linkData = event.data;
            event.preventDefault();
            var $form = jQuery(this);
            doAjaxForm($form);
            return false;
        };
        
        var prepareForm = function()
        {
            //getAjaxContainer().children().addClass("sydma_content");
            //getAjaxContainer().parent().attr("id", "content");
            var $ajaxContainer = getAjaxContainer();
            var $form = $ajaxContainer.find("form");
            if ($form.length == 0)
            {
                //no form in popup, whatever
                return;
            }
            // since we replaced the form we need to rewire the submit
            wireForm($form);
            focusForm($form);
        };
        
        //focus on the first visible input element in the form
        var focusForm = function($form)
        {   
            $form.find("input:visible").filter(":first").focus();            
        };

        var useAjaxForm = function($form)
        {

            var postData = {};
            if (Sydma.useAjaxView)
            {
                postData =
                {                    
                    "ajaxSource" : "true",
                    "fragments" : "lightboxable"                          
                };
            }
                                                
            postData = jQuery.extend(postData, ajaxData);
            var ajaxInAction = function()
            {
                if (showSpinner)
                {
                    $form.children().css("visibility", "hidden");
                    Sydma.ajaxSpinner($form);
                }            
                waitingAjax = true;
            };
            var ajaxFormOpt =
            {
                "success" : onSubmitResponse,
                "data" : postData,
                "beforeSubmit" : ajaxInAction
            };            
            $form.ajaxForm(ajaxFormOpt);
        };
        
        //intercept submit for the form
        var wireForm = function($form)
        {
            //$form.bind("submit", onFormSubmit);
            
            useAjaxForm($form);
        };
        
        var onLightboxShow = function()
        {
            jQuery("#fancybox-content").children().addClass("sydma_content");
            prepareForm();           
        };

        var showActionLightbox = function(url)
        {
            var boxOpt =
            {
                'overlayColor' : '#000',
                'overlayOpacity' : 0.7,
                'titleShow' : false,
                'hideOnOverlayClick' : false,
                'autoScale' : false,
                'scrolling' : 'no',
                'transitionIn' : 'elastic',
                'transitionOut' : 'elastic',
                'type' : 'ajax',
                "href" : url,
                "onComplete" : onLightboxShow,
                "onClosed" : onClose,
                "onCleanup" : onCleanup
            };
            jQuery.fancybox(boxOpt);
        };

        var createLightboxControl = function(event)
        {
            event.preventDefault();
            var $link = jQuery(this);
            var url = $link.attr("href");
            showActionLightbox(url);
            return false;
        };

        /**
         * @public
         */
        var linkCreated = function($link)
        {
            // bind trigger
            $link.click(createLightboxControl);
        };
        
        /**
         * @public
         */
        var doUrl = function(url)
        {
            showActionLightbox(url);            
        };

        //optionally use either linkCreateCallback or use doUrl directly
        var api =
        {
            "linkCreateCallback" : linkCreated,
            "doUrl" : doUrl
        };
        return api;
    };
})();