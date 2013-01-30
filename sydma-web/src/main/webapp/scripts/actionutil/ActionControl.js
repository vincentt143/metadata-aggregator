/**
 * Js to handle action for opening the lightbox either directly or binding to a link, it also handles any actions within the lightbox
 */

Sydma.ActionControl = Sydma.ActionControl ? Sydma.ActionControl : {};

(function()
{

    var debug = Sydma.getDebug("ActionControl");
    var info = Sydma.getInfo("ActionControl");
    
    //used for ajaxView to notify if a controling action control should consider lightbox as complete
    Sydma.ActionControl.markComplete = false;
    
    var ajaxLinkContainerSelector = ".ajax_links";
    
    var onCloseListener = {};
    
    //Allow scripts to be aware of when the lightbox has closed so they can do some cleanup if necessary
    Sydma.ActionControl.registerOnCloseListener = function(key, listener)
    {
        onCloseListener[key] = listener;
    };
    
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
            jQuery.each(onCloseListener, function(key, listener){listener();});
            
                
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
            debug("Ajax Response ");
            waitingAjax = false;
            var $resp = jQuery(resp);
           

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
                prepareAjaxView();       
                
                //TODO: Refactor this by refactoring ActionControl into a static instance controller
                if (Sydma.ActionControl.markComplete)
                {
                    //Check if lightboxContent marked Action as complete
                    Sydma.ActionControl.markComplete = false;
                    if (jQuery.isFunction(onActionComplete))
                    {
                        onActionComplete();
                    }
                }
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
        
        var onAjaxLink = function(event)
        {
            event.preventDefault();
            var $link = jQuery(this);
            var href = $link.attr("href");            
            var opt = 
            {
                "url" : href,
                "type" : "GET",
                "dataType" : "html",
                "success" : onSubmitResponse
            };
            debug("OnAjaxLink::" + href);
            jQuery.ajax(opt);
            return false;
        };
        
        var bindCapture = function()
        {
            var $link = jQuery(this);
            var linkHref = $link.attr("href");
            //href not null, not empty, doesn't start with hash
            if (linkHref != null && linkHref != "" && linkHref.charAt(0) != '#')
            {
                debug("bindCapture::Binding ajaxLink", $link);
                $link.click(onAjaxLink);
            }
        };
        
        var wireAjaxLink = function($ajaxContainer)
        {
            var $linkContainers = $ajaxContainer.find(ajaxLinkContainerSelector);
            var $links = $linkContainers.find('a').not('.no_ajax');
            
            $links.each(bindCapture);            
        };
        
        var prepareAjaxView = function()
        {
            var $ajaxContainer = getAjaxContainer();
            
            wireAjaxLink($ajaxContainer);
            
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
            var ajaxInAction = function(arrData)
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
            Sydma.lockFormEnter($form);
            useAjaxForm($form);            
        };
        
        var onLightboxShow = function()
        {
            jQuery("#fancybox-content").children().addClass("sydma_content");
            prepareAjaxView();           
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