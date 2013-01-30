Sydma.keywords = Sydma.keywords ? Sydma.keywords : {};

(function()
{
    function populateVocabulary(event)
    {
        var keywords = $.map($("#keywords").tokenInput("get"), function(word,i){return word.name;});
        Sydma.log("List of keywords= " + keywords);
        $("#vocabulary").attr('value', keywords.toString());        
    }

    //  opt : options, a hashmap with
    //   prePopulate : true/false
    //   prePopulateKeywords : optional keywords (json)
    //   searchUrl : the URL for searching
    //   formAction : URL to init the action in form
    //   formId : id of the form
    Sydma.keywords.init = function(opt)
    {
        var $form = $(opt.formId);
        $form.attr("action", opt.formAction);
        
        $('#submit-form').click(function()
        {
            populateVocabulary(null);
            $form.submit();
        });
        
        //This is to resolve a duplication issue with tokeninput and fancybox view
        if (!($(".token-input-list").length > 1))
        {  
            if (opt.prePopulate)
            {
                $("#keywords").tokenInput(opt.searchUrl, {crossDomain: false, preventDuplicates: true, hintText: "Enter keyword",
                    tokenFormatter: function(item){ return "<li><p>" + item.name + "</p></li>"}, prePopulate: opt.prePopulateKeywords
                });          
            }
            else
            {
                $("#keywords").tokenInput(opt.searchUrl, {crossDomain: false, preventDuplicates: true, hintText: "Enter keyword",
                    tokenFormatter: function(item){ return "<li><p>" + item.name + "</p></li>"}
                });      
            }
        }

    };
})();