//If Sydma then Sydma else new Obj

var Sydma = Sydma ? Sydma : {};

Sydma.enableLogging = true;

Sydma.useAjaxView = true;

Sydma.log = function()
{

    if (Sydma.enableLogging && window.console && window.console.log != "undefined")
    {
        if (window.console && 'function' === typeof window.console.log)
        {
            window.console.log.apply(console, arguments);
        }
        else
        {
            for ( var i = 0; i < arguments.length; i++)
            {
                window.console.log(arguments[i]);
            }
        }

    }
};

/** JQUERY CONFIGURATION**/
(function()
{
//set js css claass
jQuery(
    function()
    {
        jQuery("html").addClass("js");
    });

var ajaxDefaultData = 
{		
    /*for ajax view*/    
    "ajax" : "enabled"
};

if (Sydma.useAjaxView)
{			
	var ajaxViewTilesData = 
	{
			"ajaxSource" : "true",
		    "fragments" : "lightboxable"
	};
	jQuery.extend(ajaxDefaultData, ajaxViewTilesData);	
}

/**
 * Never cache ajax results
 */
jQuery.ajaxSetup(
{
    "cache" : false,
    "data" : ajaxDefaultData    
});

})();


/**
 * Handling ajax errors
 */
Sydma.defaultAjaxErrorHandler = function(request, textStatus, errorThrown)
{
    if (request.status == 401)
    {
        document.location.reload();
    }
    else
    {
        var $response = jQuery(request.responseText);
        Sydma.log("Ajax Errorhandler");
        if ($response.attr("id") == "ajax_content")
        {
            //response is in ajax html, treat it in a lightbox
            Sydma.log("Fancybox Errorhandler");
            var hiddenContent = "<div id='hiddenLightboxContent' style='display:none;'>" + request.responseText + "</div>";
            jQuery("body").append(hiddenContent);
                
            var boxOpt =
            {
                'showCloseButton' : false,
                'overlayColor' : '#000',
                'overlayOpacity' : 0.7,
                'titleShow' : false,
                'hideOnOverlayClick' : false,
                'autoScale' : false,
                'scrolling' : 'no',
                'transitionIn' : 'elastic',
                'transitionOut' : 'elastic',
                'type' : 'inline',
                'href' : '#ajax_content'
            };
            jQuery.fancybox(boxOpt);     
            Sydma.log("Lightbox shown");
        }
        else
        {            
            // TODO use proper jquery messaging plugin
            if (textStatus == 'timeout')
            {
                alert('Request Time out.');
            }
            else
            {
                alert('Server error happened while processing your request.');
            }
        }
        
    }
};

/**
 * wrapper to use jQuery's autocomplete
 */
(function()
{
    Sydma.autocomplete = function(selector, url)
    {
        var autoCompleteCallback = function(request, response)
        {
            var processPotentialList = function(resp)
            {
                var list = resp.data;                               
                response(list);
            };
            var ajaxOpt = 
            {
                "url" : url,
                "data" : 
                {
                    "term" : request.term
                },     
                "dataType" : "json",
                "success" : processPotentialList
            };
            jQuery.ajax(ajaxOpt);  
        };
        
        var autoCompleteOpt = 
        {
            "source" : autoCompleteCallback,
            "minLength" : 3, //atleast 3 chars
            "delay" : 500 //half a second
        };
        jQuery(selector).autocomplete(autoCompleteOpt);
    };
})();        
        
        

/**
 * Util function to add ajax spinner to a container
 */
(function()
{
    //bind jquery spinner plugin
    jQuery.fn.spin = function(opts)
    {
        this.each(function()
        {
            var $this = $(this), data = $this.data();

            if (data.spinner)
            {
                data.spinner.stop();
                delete data.spinner;
            }
            if (opts !== false)
            {
                data.spinner = new Spinner($.extend(
                {
                    color : $this.css('color')
                }, opts)).spin(this);
            }
        });
        return this;
    };
    
    var spinnerOpt = 
    {
        lines: 12, // The number of lines to draw
        length: 7, // The length of each line
        width: 4, // The line thickness
        radius: 10, // The radius of the inner circle
        color: '#000', // #rgb or #rrggbb
        speed: 1, // Rounds per second
        trail: 60, // Afterglow percentage
        shadow: false // Whether to render a shadow
    };
    
    Sydma.ajaxSpinner = function($container)
    {        
        $container.spin(spinnerOpt);
    };
})();

/**
 * Util function to reload the page upon clicking button of the reloadButtonSelector class
 */

Sydma.reloadButtonSelector = ".sydmaReload";
Sydma.activateReloadButton = function(container)
{
    var $container;
    if (container != null)
    {
        $container = jQuery(container);
        
    }
    else
    {
        $container = jQuery("body");
    }
    var reload = function()
    {
        location.reload();
    };
    $container.delegate(Sydma.reloadButtonSelector, "click", reload);
};

/**
 * Util to find and insert applets
 */
(function()
{
    Sydma.applet = Sydma.applet ? Sydma.applet : {};
    
    Sydma.applet.findApplet = function(appletId)
    {
        var $applet = $('object#' + appletId + ', embed#' + appletId);
        return $applet.size() == 0 ? null : (("isActive" in $applet[0]) ? $applet[0] : $applet[1]);
    };
    

    Sydma.applet.appletReady = function(applet, callback) 
    {
        var tries = 0;
        var waitLoop = function()
        {
            if (applet == null || applet.isDisabled == null || !applet.isActive())
            {
                if (tries < 5)
                {
                    Sydma.log("DEBUG::Waiting on applet " + tries);
                    setTimeout(waitLoop, 500); //Wait 0.5 second to ensure applet is loaded
                    tries++;
                }
                else
                {
                    //attempts exhausted
                }
                return;
            }
            callback();
        };
        waitLoop();
    };
    
    Sydma.applet.insertApplet = function(appletId, $container, url, code, props, params) 
    {
        
        var propsStr = "";
        if (props != null) {
                for(key in props) {
                        propsStr += key + '="' + props[key] + '" ';
                }
        }
       
        var paramsStr = "";
        var paramsForEmbed = "";
        if(params != null) {
              for(key in params) {
                      paramsStr += '<param name="' + key + '" value="' + params[key] + '">';
                      paramsForEmbed += key + '="' + params[key] + '" ';
              }  
        }
       
        $('<object classid="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93" id="' + appletId + '"'
                              + propsStr + ' name="myPC" codebase="http://java.sun.com/products/plugin/autodl/jinstall-1_6-windows-i586.cab#Version=1,6,0,0">'
                              +'<param name="type" value="application/x-java-applet;version=1.6"><param name="code" value="'+code+'">'
                              +'<param name="name" value="myPC"> <param name="archive" value="' + url + '">'
                              +'<param name="cache_archive" value="dms-applet.jar">'
                              +'<param name="scriptable" value="true">'
                              + paramsStr
                              +' <comment> <embed type="application/x-java-applet;version=1.6" name="myPC" code="' + code + '" '
                              +' pluginspage="http://www.oracle.com/technetwork/java/javase/downloads/index.html" archive="' + url + '" '
                              +'" cache_archive="dms-applet.jar" id="' + appletId + '" ' + propsStr +  paramsForEmbed + ' mayscript="mayscript">'                                    
                              +'<noembed>Your Browser Does Not Have Java 1.6 Support, Which Is Needed To Run This Applet!</noembed></embed>'
                              +'</comment></object>').appendTo($container);    
        Sydma.log("Inserting applet", $container);
    };


})();
