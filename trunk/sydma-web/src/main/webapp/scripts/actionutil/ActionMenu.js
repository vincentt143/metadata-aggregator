/**
 * 
 */

Sydma.actionMenu = Sydma.actionMenu ? Sydma.actionMenu : {}; 

(function()
{    
    
    var actionBarTempLinkClass = "tempLink";
    var actionBarTempLinkSelector = "." + actionBarTempLinkClass;
    
    /**
     * expects the following in opt
            containerSelector: //also expects a dl in container
     */
    Sydma.actionMenu.createActionMenu = function(opt)
    {
        var containerSelector = opt.containerSelector;        
        var $actionMenuContainer = jQuery(containerSelector);
        var $actionMenu = $actionMenuContainer.find("dl");
        var hide = function()
        {
            $actionMenuContainer.hide();
        };
        
        var show = function()
        {
            $actionMenuContainer.show();
        };
        
        var addLinks = function(actionVars)
        {            
            clearLinks();
            Sydma.log("DEBUG::ActionMenu[" + containerSelector + "]::addLinks::actionVars", actionVars);
            for (var i in actionVars)
            {
                var actionVar = actionVars[i];
                var linkVar = actionVar.linkVar;
                var onLinkCreate = actionVar.onLinkCreate;
                var linkData = actionVar.linkData;
                
                
                var title = linkVar.title;
                var url = linkVar.url;
                var linkClass = linkVar.linkClass;
                
                var $link = jQuery("<a title=\"" + title + "\" >" + title + "</a>");
                var linkUrl = "";
                if (url)
                {                    
                    if (jQuery.isFunction(url))
                    {
                        linkUrl = url(linkData);
                    }
                    else
                    {
                        linkUrl = linkVar.url;
                    }                    
                }   
                $link.attr("href", linkUrl);
                if (linkClass)
                {
                    $link.addClass(linkClass);
                }
                if (jQuery.isFunction(onLinkCreate))
                {
                    onLinkCreate($link);
                }
                
                var $dd = jQuery("<dd class=\"" + actionBarTempLinkClass + "\"></dd>");                
                $dd.append($link);
                $actionMenu.append($dd);
            }       
            //if no links then we hide
            if (actionVars.length == 0)
            {
                hide();
            }
            else
            {
                show();
            }
        };
        
        var clearLinks = function()
        {
            $actionMenu.find(actionBarTempLinkSelector).remove();  
        };
        
        var clean = function()
        {
            Sydma.log("DEBUG::ActionMenu::clean");
            clearLinks();
            hide();
        };
        
        
        //initialize
        
        hide();
        
        var api = 
        {
            "clean" : clean,
            
            /**
             * accepts an opt, 
                    title:
                    url: //only if intended to be used as is
             */
            "addLinks" : addLinks
        };
        return api;
    };
    
})();
    