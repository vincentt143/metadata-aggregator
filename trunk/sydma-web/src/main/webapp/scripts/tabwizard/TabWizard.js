/**
 * JS object that handles the commonly used N tab workflow
 * 
 * It provides the following functionality
 * 
 * 1) Wires onClick navigation to .tabNext .tabBack elements
 * 2) upon leaving a tab, validation is done on it through the validateTabContent callback
 * 3) upon attempt to enter a tab, a check is made to make sure all tabs up to the tab in question has been completed
 *      if not it will raise an error message through the showInvalidMsg callback
 * 
 */

Sydma.tabWizard = Sydma.tabWizard ? Sydma.tabWizard : {};

(function()
{
    var tabNextSelector = ".tabNext";
    var tabBackSelector = ".tabBack";
    var tabContentSelector = ".tabContent";

    /**
     * @private
     * @static
     * 
     * Wires up Next/Back buttons in the tab so when clicked, they'll select the
     * next/previous tab
     * 
     * current is the 0-index number of the tab the button is on direction is 1
     * or -1, 1 means next , -1 means back
     */
    var wireTabButton = function($tabs, $button, current, direction)
    {
        var targetTab = current + direction; // switch to tab pending on
        // direction
        var switchFunc = function()
        {
            Sydma.log("DEBUG::Tab switch from " + current + " toward " + targetTab);
            $tabs.tabs('select', targetTab);
            return false;
        };
        $button.click(switchFunc);
    };

    /**
     * @private
     * @static
     * 
     * search for tab navigation buttons and wire triggers to them
     */
    var configureButtons = function($tabs, $tabContents)
    {
        for ( var i = 0; i < $tabContents.length; i++)
        {
            var $tabContent = $tabContents.eq(i);

            var $next = $tabContent.find(tabNextSelector);
            var $back = $tabContent.find(tabBackSelector);

            if ($next.length > 0)
            {
                wireTabButton($tabs, $next, i, 1);
            }
            if ($back.length > 0)
            {
                wireTabButton($tabs, $back, i, -1);
            }
        }
    };

    /**
     * @public
     * @static
     * 
     * entry point
     * expects the following in the opts
     * 
     * 
            "tabSelector" : //selector for the tabs container
            "validateTabContent" : //function($tabContent) to validate the content of the tab is complete
            "showInvalidMsg" : //function(index) to display a msg for failed attempt to entry a tab with given index
            "onSelect" : //function($tabs, index) callback for selection of a tab with given index
            "onShow" : //function($tabs, index) callback for showing of a tab with given index
     */
    Sydma.tabWizard.wizard = function(opts)
    {
        var tabSelector = opts.tabSelector;

        var validateTabContent = opts.validateTabContent; // function
        var showInvalidMsg = opts.showInvalidMsg; // function
        var onShow = opts.onShow;
        var onSelect = opts.onSelect;

        var $tabs = jQuery(tabSelector);

        var $tabContents = $tabs.find(tabContentSelector);

        var currentTabIndex = -1;

        var doneTabs = [];

        var markTabAsDone = function(index)
        {
            var $li = $tabs.find("ul:first li").eq(index);
            $li.addClass("done");
            doneTabs[index] = true;
        };
        var unmarkTabAsDone = function(index)
        {
            var $li = $tabs.find("ul:first li").eq(index);
            $li.removeClass("done");
            doneTabs[index] = false;
        };

        /*
         * verify all tabs before tab of index are complete
         */
        var verifyPreviousTabsDone = function(index)
        {
            for ( var i = 0; i < index; i++)
            {
                if (doneTabs[i] != true)
                {
                    return false;
                }
            }
            return true;
        };

        var tabShow = function(event, ui)
        {   
            var index = ui.index;
            
            onShow($tabs, index);
            currentTabIndex = index;
        };

        var tabSelect = function(event, ui)
        {
            var index = ui.index;
            var $tabSelected = $tabContents.eq(index);
            if (currentTabIndex != -1)
            {

                var $previousTabContent = $tabContents.eq(currentTabIndex);

                //if validateTabContent is not a function then we presume validation is not needed and simply mark tab as done
                if (!jQuery.isFunction(validateTabContent) || validateTabContent($previousTabContent))
                {
                    markTabAsDone(currentTabIndex);
                }
                else
                {
                    unmarkTabAsDone(currentTabIndex);
                }

                if (!verifyPreviousTabsDone(index))
                {
                    if (!jQuery.isFunction(showInvalidMsg))
                    {
                        Sydma.log("WARN::TabWizard::tabSelect::showInvalidMsg is not a function, no error message will be raised");
                    }
                    else
                    {
                        showInvalidMsg(index);
                    }                    
                    return false;
                }

                onSelect($tabs, index);
            }
        };

        var tabOpts =
        {
            "select" : tabSelect,
            "show" : tabShow,
            "selected" : 0 //always show the first tab
        };

        jQuery(tabSelector).tabs(tabOpts);

        configureButtons($tabs, $tabContents);

    };

})();
