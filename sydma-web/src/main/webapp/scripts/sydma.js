//If Sydma then Sydma else new Obj

var Sydma = Sydma ? Sydma : {};

Sydma.enableLogging = true;

Sydma.logLevel = "DEBUG";

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

(function()
{

    var getLogger = function(prefix)
    {
        var logger = function()
        {
            arguments[0] = prefix + arguments[0];
            Sydma.log.apply(this, arguments);
        };
        return logger;
    };

    Sydma.getInfo = function(className)
    {
        return getLogger("INFO::" + className + "::");
    };

    Sydma.getDebug = function(className)
    {
        if (Sydma.logLevel != "DEBUG")
        {
            return jQuery.noop;
        }
        return getLogger("DEBUG::" + className + "::");
    };
})();

/** JQUERY CONFIGURATION* */
(function()
{
    // set js css claass
    jQuery(function()
    {
        jQuery("html").addClass("js");
    });

    var ajaxDefaultData =
    {
        /* for ajax view */
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
    
    jQuery.fn.preventDoubleSubmit = function() {
    	  jQuery(this).submit(function() {
    	    if (this.beenSubmitted)
    	      return false;
    	    else
    	      this.beenSubmitted = true;
    	  });
    };
    	
    jQuery.fn.resetBeenSubmitted = function() {
    	this.beenSubmitted = false;
    };

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
            // response is in ajax html, treat it in a lightbox
            Sydma.log("Fancybox Errorhandler");
            var hiddenContent = "<div id='hiddenLightboxContent' style='display:none;'>" + request.responseText
                    + "</div>";
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
 * Parse out /scripts/ folder path Sydma is in and save it in Sydma.scriptDir
 */
(function()
{
    var scripts = document.getElementsByTagName("script");

    for ( var i in scripts)
    {
        var scriptUrl = scripts[i].src;
        var pathRegex = /(.*)sydma.js/;
        var match = pathRegex.exec(scriptUrl);
        if (match)
        {
            var path = match[1];
            Sydma.log("INFO::Sydma.js under path:" + path);
            Sydma.scriptDir = path;
            break;
        }
    }

    Sydma.getResourceUnderScriptDir = function(relativePath)
    {
        return Sydma.scriptDir + relativePath;
    };
})();

/**
 * Similar to the Java equivilent. Once created, the cyclicBarrier obj will
 * execute the passed in callback once it has been called the desired number of
 * times
 * 
 * ie. Given a limit of 2, it will execute onComplete once the function it
 * returns has been called twice
 */
(function()
{
    Sydma.cyclicBarrier = function(limit, onComplete)
    {
        var cyclicCount = 0;
        var cyclicLimit = limit;
        var cyclicBarrier = function()
        {
            cyclicCount++;
            if (cyclicCount >= cyclicLimit)
            {
                Sydma.log("CyclicBarrier::complete");
                onComplete();
            }
        };
        return cyclicBarrier;
    };

})();

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
            "minLength" : 3, // atleast 3 chars
            "delay" : 500
        // half a second
        };
        jQuery(selector).autocomplete(autoCompleteOpt);
    };
})();

/**
 * extended version of the wrapper to use jQuery's autocomplete function
 */
(function()
{
    Sydma.autocompleteUser = function(selector, url)
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

        var selectOpt = function(event, ui)
        {
            $(selector).val(ui.item.userID);
        };

        var autoCompleteOpt =
        {
            "source" : autoCompleteCallback,
            "minLength" : 3, // atleast 3 chars
            "select" : selectOpt, //on click will place the selected option
            "delay" : 500 // half a second
        };

        var renderData = function(ul, item)
        {
            return $("<li></li>").data("item.autocomplete", item).append(
                    "<a>" + item.fullName + "<br/><b>" + item.userID + "</b></a>").appendTo(ul);
        };

        jQuery(selector).autocomplete(autoCompleteOpt).data("autocomplete")._renderItem = renderData;
    };
})();

/**
 * wrapper to use jQuery's datepicker
 */
(function()
{
    var dateFormat = "dd/mm/yy";

    var defaultSelector = ".date_plugin";

    var defaultImage = Sydma.getResourceUnderScriptDir("images/calendar.gif");

    Sydma.datepicker = function(opt, inputDpOpt)
    {
        var defaultOpt =
        {
            "changeMonth" : true, // allow select by month
            "changeYear" : true, // allow select by year
            "dateFormat" : dateFormat,
            "defaultDate" : "+0", // today
            "buttonImage" : defaultImage,
            "buttonImageOnly" : true,
            "showOn" : "button",
            "yearRange" : "c-20:c+10" // show years from 20 years ago to 10
                                        // years in future in select box
        };
        var dpOpt = jQuery.extend(defaultOpt, inputDpOpt);

        var setSelectedToCurrent = function(input, inst)
        {
            // defaults input to current time if it is empty
            var $input = jQuery(input);
            if ($input.val() == "")
            {
                var dval = jQuery.datepicker.formatDate(dpOpt.dateFormat, new Date());
                $input.val(dval);
            }
        };

        if (opt.setOnShow != false)
        {
            defaultOpt["beforeShow"] = setSelectedToCurrent;
        }

        var selector = defaultSelector;
        if (opt.targetSelector != null)
        {
            selector = opt.targetSelector;
        }

        jQuery(selector).datepicker(dpOpt);
    };
})();

/**
 * Until function to capture enter keys in a form
 */
(function()
{
    var enterKeyCode = 13;

    var notTextArea = function(event)
    {
        return event.target.tagName.toLowerCase() != "textarea";
    };

    var isSubmit = function(event)
    {
        if (event.which == 13 && notTextArea(event))
        {
            return true;
        }
        return false;
    };

    Sydma.lockFormEnter = function($form)
    {
        var onKeyPress = function(event)
        {
            if (isSubmit(event))
            {
                var $submitToUse = $form.find("input.submit-button[type='submit']").first();
                if ($submitToUse.length > 0)
                {
                    event.preventDefault();

                    // mark the submit button as clicked in the form for
                    // ajaxForm to pickup
                    $form[0].clk = $submitToUse[0];
                    setTimeout(function()
                    {
                        $form[0].clk.clk = null;
                    }, 100);

                    // trigger submit
                    $submitToUse.trigger("submit");

                    return false;
                }
                // no submit button.. eh, let the browser deal with it
            }
        };
        $form.keypress(onKeyPress);
    };
})();

/**
 * Util function to add ajax spinner to a container
 */
(function()
{
    // bind jquery spinner plugin
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
        lines : 12, // The number of lines to draw
        length : 7, // The length of each line
        width : 4, // The line thickness
        radius : 10, // The radius of the inner circle
        color : '#000', // #rgb or #rrggbb
        speed : 1, // Rounds per second
        trail : 60, // Afterglow percentage
        shadow : false
    // Whether to render a shadow
    };

    Sydma.ajaxSpinner = function($container)
    {
        $container.spin(spinnerOpt);
    };
})();

/**
 * Util to limit text length
 */
(function()
{
    var defaultMax = 15;

    Sydma.limitText = function(text, maxLength)
    {
        var textLimit = maxLength ? maxLength : defaultMax;

        var newText;
        if (text.length > textLimit)
        {
            newText = text.substring(0, textLimit) + "...";
        }
        else
        {
            newText = text;
        }
        return newText;
    };
})();

/**
 * Util function to reload the page upon clicking button of the
 * reloadButtonSelector class
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
    
    Sydma.applet.appletRef = null;

    Sydma.applet.findApplet = function()
    {
        return Sydma.applet.appletRef;
    };

    Sydma.applet.appletReady = function(callback)
    {
    	function setGlobalApplet(ref) {
    		var $obj = $('#'+ref);
                if ($obj.size() != 1) return false;
                var obj = $obj.get(0);
    		if (obj == undefined || obj == null) return false;
    		if (!('isActive' in obj) || !obj.isActive()) return false;
    		Sydma.applet.appletRef = obj;
    		return obj.isActive();
    	}
        var tries = 0;
        var waitLoop = function()
        {
            if (!setGlobalApplet('applet_unique_obj') && !setGlobalApplet('applet_unique_emb'))
            {
                if (tries < 30)
                {
                    Sydma.log("DEBUG::Waiting on applet " + tries);
                    setTimeout(waitLoop, 1000); // Wait 1 second to ensure it's loaded
                    tries++;
                }
                else
                {
                    alert("Timeout while enabling plugin (applet) to connect to your computer, please reload page");
                }
                return;
            }
            callback();
        };
        waitLoop();
    };

    Sydma.applet.insertApplet = function($container, url, props, params)
    {

    	if (Sydma.applet.appletRef != null) {
    		Sydma.log("Re-using applet");
    		return;
    	}
    	
    	params['archive'] = url;
    	params['code'] = 'au.org.intersect.dms.applet.BrowseApplet.class';
    	params['cache_option'] = 'No';
    	params['mayscript'] = 'true';

        Sydma.log("Inserting applet", $container);
        
        var paramsStr = "";
        var paramsForEmbed = "";
        if (params != null)
        {
            for (key in params)
            {
                paramsStr += '<param name="' + key + '" value="' + params[key] + '">';
                paramsForEmbed += key + '="' + params[key] + '" ';
            }
        }

        $('<object classid="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93" id="applet_unique_obj" '
            + ' width="'+props['width']+'" height="'+props['height']+'"'
            + ' codebase="http://java.sun.com/products/plugin/autodl/jinstall-1_6-windows-i586.cab#Version=1,6,0,0">'
            + paramsStr
            + '<comment>'
            + '<embed type="application/x-java-applet;version=1.6" '
            + ' pluginspage="http://www.oracle.com/technetwork/java/javase/downloads/index.html" '
            + ' id="applet_unique_emb" '
            + ' width="'+props['width']+'" height="'+props['height']+'" '
            + paramsForEmbed
            + ' >'
            + '<noembed>Your Browser Does Not Have Java 1.6 Support, Which Is Needed To Run This Applet!</noembed></embed>'
            + '</comment></object>').appendTo($container);
        
        var txtLockPage = "There is a transfer in progress involving your local computer, if you leave the page the transfer will be interrupted!";
        var preventPageUnload = function()
        {
        	var applet = Sydma.applet.appletRef;
        	if (applet != null && ('jobsRunning' in applet) && applet.jobsRunning() != 0) {
        		alert(txtLockPage);
        		return 'Are you sure you wish to leave the page?';
        	}
        };        
        window.onbeforeunload = preventPageUnload;
    };

})();
