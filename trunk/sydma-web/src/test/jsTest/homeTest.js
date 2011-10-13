var ATest = AsyncTestCase('ATest');

ATest.prototype.setUp = function()
{
    //turn off default logging, for our purpose we will rely on jstestdriver loggings
    Sydma.enableLogging = false;
    
    jstestdriver.console.log("Setup");
    /*:DOC += <div id="page"> 
      <select id="select1"> 
      <option value="1" id="opt1">Val 1</option>
      <option value="2" id="opt2">Val 2</option>
      </select> 
      <select id="target1"> 
      </select> 
      <select id="target2"> 
      </select>
      <dl id="sidebar1"></dl> 
      </div>
     */
};

ATest.prototype.testSimple = function(queue)
{
    
    jstestdriver.console.log("JsTestDriver", "Init test");
    assertEquals(1, jQuery('#page').length);
    assertEquals(1, jQuery('#select1').length);
    assertEquals(1, jQuery('#opt1').length);
    assertEquals(1, jQuery('#opt2').length);
    assertEquals(1, jQuery('#target1').length);
    assertEquals(1, jQuery('#target2').length);
    assertEquals(1, jQuery('#sidebar1').length);
    
    jstestdriver.console.log("JsTestDriver", "Verified page elements setup, proceed to test page script");
    // setup mock ajax
    //http://sydma.intersect.org.au/getchild?parentId=1
    jQuery.mockjax(
    {
        url : '*',
        responseTime : 0,
        contentType: 'text/json',
        responseText: 
            [{
                id : "test id",
                name : "test name"
            }]                    
    });    
    
    queue.call('Setup mock ajax and click to trigger the system', function(callbacks) 
            {
                var noop = callbacks.noop();
                var doneCallback = function()
                {   
                    noop();
                };
                var testOpt =
                {
                    sidebar : "#sidebar1",
                    getChildUrl : "http://sydma.intersect.org.au/getchild",
                    getChildParamName : "parentId",
                    source : "#select1",
                    target : "#target1",
                    createChildUrl : "http://sydma.intersect.org.au/createchild",
                    createChildLinkText : "Create Child",
                    createChildParamName : "parentId",
                    ajaxDoneCallback : doneCallback
                };
                Sydma.home.setupFetchChildren(testOpt);
                jstestdriver.console.log("Current body before click", jQuery("body").html());                
                jQuery("#opt1").click();                
            });    
    
    queue.call('Verify state after triggering click', function(callbacks)
            {
                jstestdriver.console.log("Current body after click", jQuery("body").html());
                //verify state changes
                assertEquals("Unexpected number of option element found in select #target1", 1, jQuery("#target1 option").length);
                assertEquals("Unexpected number of class .tempLink found in sidebar", 2, jQuery(".tempLink").length);
            });
   
    window.close();
};