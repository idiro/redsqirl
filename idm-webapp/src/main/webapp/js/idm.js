
function canvasResizeSplitter(){
	//<![CDATA[
	
	jQuery("#canvas").css("height", jQuery("#canvas-tabs").height()-160+'px');
	jQuery("#canvas").css("width", jQuery("#canvas-tabs").width()+'px');
	jQuery("#tabsFooter").css("width", jQuery("#canvas-tabs").width()-10+'px');

	jQuery(".splitter-bar-horizontal").css("width", jQuery(window).width()-jQuery(".splitter-pane").width()-28 +'px');
	jQuery("#tabs1").css("width", jQuery("#splitHCanvas").width()-20 +'px');
	jQuery("#tabs2").css("width", jQuery("#splitHCanvas").width()-20 +'px');
	
	resizeCanvas();
	
	resizeTabs();
	
	configureLeft();

	validateArrowsAll();
	
	//]]>
  }

function resizeCanvas(){
	
	//jQuery("#tabsFooter canvas").parent().remove();
	//mountObj();
	
	for (var i in nameTabs){
		//alert(nameTabs[i]);
		jQuery("#"+nameTabs[i]).css("height", jQuery("#canvas-tabs").height()-jQuery("#tabsFooter").height()-30+'px');
		jQuery("#"+nameTabs[i]).css("width", jQuery("#canvas-tabs").width()+'px');
		jQuery("#container-"+nameTabs[i]).css("height", jQuery("#canvas-tabs").height()-jQuery("#tabsFooter").height()-30+'px');
	}
	
}

function resizeCanvasOnPageReady(){
	
	for (var i in nameTabs){
		//alert(nameTabs[i]);
		jQuery("#"+nameTabs[i]).css("height", jQuery("#canvas-tabs").height()-jQuery("#tabsFooter").height()-30+'px');
		jQuery("#"+nameTabs[i]).css("width", jQuery("#canvas-tabs").width()+'px');
		jQuery("#container-"+nameTabs[i]).css("height", jQuery("#canvas-tabs").height()-jQuery("#tabsFooter").height()-130+'px');
	}
	
}

function resizeTables(){
	
	jQuery("#processManager .extdt-content").style("height", jQuery("#tabs-2").height()-130+"px", "important");
	
	jQuery("#errorTable .extdt-content").style("height", jQuery("#tabs-3").height()-60+"px", "important");
	
	jQuery("#hiveFileSystem .extdt-content").style("height", jQuery("#tabs-4").height()-160+"px", "important");
	
	jQuery("#hdfsFileSystem .extdt-content").style("height", jQuery("#tabs-7").height()-160+"px", "important");
	
	jQuery("#tabRemote .extdt-content").style("height", jQuery("#tabs-8").height()-185+"px", "important");
	
	jQuery("#hdfsfsSaveFile .extdt-content").style("height", "20px", "important");
	
}

function onPageReady(){

	  jQuery("#body").css('width', jQuery(window).width()-20+'px');
	  jQuery("#body").css('height', jQuery(window).height()-20+'px');
	  jQuery("#splitVCanvas").css("height", jQuery(window).height()-100+'px').trigger("resize");
	  jQuery("#menu").css('width', jQuery(window).width()-20+'px');
	  jQuery("#splitVCanvas").css('width', jQuery(window).width()-40+'px');
	  jQuery("#splitVCanvas").css('height', jQuery(window).height()-160+'px');
	  jQuery(".splitter-pane").css("height", jQuery(window).height()-160+'px');
	  jQuery("#splitHCanvas").css("height", jQuery(window).height()-180+'px');
	  jQuery("#tabFlowchart").css("height", jQuery(window).height()-180+'px');
	  jQuery(".splitter-bar-horizontal").css("width", jQuery(window).width()-jQuery(".splitter-pane").width()-38 +'px').trigger("resize");
	  jQuery("#tabs1").css("width", jQuery("#splitHCanvas").width()-20 +'px');
	  jQuery("#tabs2").css("width", jQuery("#splitHCanvas").width()-20 +'px');
	  jQuery("#tabs1").children('ul').first().css("left", 0+'px');
	  jQuery("#tabs2").children('ul').first().css("left", 0+'px');
	  jQuery("#buttonsTabs1").css("left", jQuery("#tabs1").width()-30+'px');
	  jQuery("#buttonsTabs2").css("left", jQuery("#tabs2").width()-30+'px');
	  jQuery("#tabFlowchart-1").css("height", jQuery(window).height()-205+'px');
	  jQuery("#canvas-tabs").css("height", jQuery(window).height()-215+'px');
	  jQuery("#canvas").css("height", jQuery("#canvas-tabs").height()-160+'px');
	  jQuery("#canvas").css("width", jQuery("#canvas-tabs").width()+'px');
	  
	  resizeTabs();
	  resizeTables();
	  
	  jQuery("#buttonsCanvas1").hide();
	  jQuery("#buttonsTabs1").hide();
	  jQuery("#buttonsTabs2").hide();
	  jQuery("#buttonstabRemote").hide();
	  jQuery("#buttonsTabsFooter").hide();

	  jQuery("#splitHCanvas").css("overflow", "hidden");
	  jQuery("#canvas-tabs").css("overflow", "hidden");
	  jQuery("#tabFlowchart").css("overflow", "hidden");
	  jQuery("#tabs1").css("overflow", "hidden");
	  jQuery("#tabs2").css("overflow", "hidden");
	  jQuery(".splitter-pane").css("overflow", "hidden");
	  jQuery("#tabRemote").css("overflow", "hidden");
	  
	  resizeCanvasOnPageReady();
	  
	  configureFooterCss();

	  validateArrowsAll();
	  
}

function configureFooterCss(){
	
	  jQuery("#tabsFooter").css("width", jQuery("#canvas-tabs").width()+5+'px');
	  jQuery("#tabsFooter").css("overflow", "hidden");
	  jQuery("#tabsFooter").css("position", "absolute");
	  jQuery("#tabsFooter").css("bottom", "0");
	  jQuery("#tabsFooter").css("z-index", jQuery("#canvas-tabs").zIndex()+1);
	  
	  validateArrows(jQuery("#tabsFooter"),jQuery("#buttonsTabsFooter"));
}

function resizing(){
	
	  isResizing = true;

	  jQuery("#body").css('width', jQuery(window).width()-20+'px');
	  jQuery("#body").css('height', jQuery(window).height()-20+'px');
	  jQuery("#splitVCanvas").css("height", jQuery(window).height()-150+'px').trigger("resize");
	  jQuery("#menu").css('width', jQuery(window).width()-20+'px');
	  jQuery("#splitVCanvas").css('width', jQuery(window).width()-40+'px');
	  jQuery("#splitVCanvas").css('height', jQuery(window).height()-160+'px');
	  jQuery(".splitter-pane").css("height", jQuery(window).height()-160+'px');
	  jQuery("#splitHCanvas").css("height", jQuery(window).height()-180+'px');
	  jQuery("#tabFlowchart").css("height", jQuery(window).height()-180+'px');
	  jQuery(".splitter-bar-horizontal").css("width", jQuery(window).width()-jQuery(".splitter-pane").width()-28 +'px').trigger("resize");
	  jQuery("#tabs1").css("width", jQuery("#splitHCanvas").width()-20 +'px');
	  jQuery("#tabs2").css("width", jQuery("#splitHCanvas").width()-20 +'px');
	  jQuery("#tabFlowchart-1").css("height", jQuery(window).height()-205+'px');
	  jQuery("#canvas-tabs").css("height", jQuery(window).height()-215+'px');
	  jQuery("#canvas").css("height", jQuery("#canvas-tabs").height()-160+'px');
	  jQuery("#canvas").css("width", jQuery("#canvas-tabs").width()+'px');
	  jQuery("#tabsFooter").css("width", jQuery("#canvas-tabs").width()-10+'px');
	  
	  isResizing = false;
	  
	  resizeCanvas();

	  resizeTabs();
	  
	  configureLeft();
	  
	  validateArrowsAll();

}

function configureLeft(){
	
	jQuery("#buttonsCanvas1").css("left", jQuery("#canvas-tabs").width()-30+'px');
	jQuery("#buttonsTabs1").css("left", jQuery("#tabs1").width()-30+'px');
	jQuery("#buttonsTabs2").css("left", jQuery("#tabs2").width()-30+'px');
	jQuery("#buttonstabRemote").css("left", jQuery("#tabRemote").width()-30+'px');
	jQuery("#buttonsTabsFooter").css("left", jQuery("#tabsFooter").width()-30+'px');
	
}

function validateArrowsAll(){
	
	validateArrows(jQuery("#canvas-tabs"),jQuery("#buttonsCanvas1"));
	validateArrows(jQuery("#tabs1"),jQuery("#buttonsTabs1"));
	validateArrows(jQuery("#tabs2"),jQuery("#buttonsTabs2"));
	validateArrows(jQuery("#tabRemote"),jQuery("#buttonstabRemote"));
	validateArrows(jQuery("#tabsFooter"),jQuery("#buttonsTabsFooter"));
	
}

function validateArrows(tabPrincipal, spanButtons){
	
	var ul = tabPrincipal.tabs().children('ul').first();
	var tabsRealWidth = 0;
	
	ul.find('li').each(function (index, element) {
		tabsRealWidth += jQuery(element).width();
		tabsRealWidth += jQuery(element).css('margin-right').replace('px', '') / 1;
    });

	if(tabsRealWidth - tabPrincipal.width() > -35){
		
		spanButtons.show();
		
		var disTab = tabsRealWidth - -1*ul.css("left");
		//var disTab = tabsRealWidth - -1*ul.css("left").replace('px', '');
		
		if( tabPrincipal.tabs().children('span').first().css("left") - disTab > 40 ){
			
			var dist = tabPrincipal.tabs().children('span').first().css("left") - disTab - 40;
			ul.stop().animate({ left: parseInt(ul.css("left")) + parseInt(dist) + 'px' }, 'slow');
		
		}
  	  
  	}else{
  		
  		spanButtons.hide();
  		ul.stop().animate({ left: '0' }, 'slow');
  		
  	}
    
}

function resizeTabs(){
	
	if(jQuery("#tabs-1").parent("div").attr('id') == jQuery("#tabs1").attr('id')){
		jQuery("#tabs-1").css("height", jQuery("#tabs1").height()-25+'px');
	}else if(jQuery("#tabs-1").parent("div").attr('id') == jQuery("#tabs2").attr('id')){
		jQuery("#tabs-1").css("height", jQuery("#tabs2").height()-25+'px');
	} else{
		jQuery("#tabs-1").css("height", jQuery(".splitter-pane").height()-25+'px');
	}
	
	if(jQuery("#tabs-2").parent("div").attr('id') == jQuery("#tabs1").attr('id')){
		jQuery("#tabs-2").css("height", jQuery("#tabs1").height()-25+'px');
	}else if(jQuery("#tabs-1").parent("div").attr('id') == jQuery("#tabs2").attr('id')){
		jQuery("#tabs-2").css("height", jQuery("#tabs2").height()-25+'px');
	} else{
		jQuery("#tabs-2").css("height", jQuery(".splitter-pane").height()-25+'px');
	}
	
	if(jQuery("#tabs-3").parent("div").attr('id') == jQuery("#tabs1").attr('id')){
		jQuery("#tabs-3").css("height", jQuery("#tabs1").height()-25+'px');
	}else if(jQuery("#tabs-3").parent("div").attr('id') == jQuery("#tabs2").attr('id')){
		jQuery("#tabs-3").css("height", jQuery("#tabs2").height()-25+'px');
	} else{
		jQuery("#tabs-3").css("height", jQuery(".splitter-pane").height()-25+'px');
	}
	
	if(jQuery("#tabs-4").parent("div").attr('id') == jQuery("#tabs1").attr('id')){
		jQuery("#tabs-4").css("height", jQuery("#tabs1").height()-25+'px');
	}else if(jQuery("#tabs-4").parent("div").attr('id') == jQuery("#tabs2").attr('id')){
		jQuery("#tabs-4").css("height", jQuery("#tabs2").height()-25+'px');
	} else{
		jQuery("#tabs-4").css("height", jQuery(".splitter-pane").height()-25+'px');
	}
	
	if(jQuery("#tabs-5").parent("div").attr('id') == jQuery("#tabs1").attr('id')){
		jQuery("#tabs-5").css("height", jQuery("#tabs1").height()-25+'px');
	}else if(jQuery("#tabs-5").parent("div").attr('id') == jQuery("#tabs2").attr('id')){
		jQuery("#tabs-5").css("height", jQuery("#tabs2").height()-25+'px');
	} else{
		jQuery("#tabs-5").css("height", jQuery(".splitter-pane").height()-25+'px');
	}
	
	if(jQuery("#tabs-6").parent("div").attr('id') == jQuery("#tabs1").attr('id')){
		jQuery("#tabs-6").css("height", jQuery("#tabs1").height()-25+'px');
	}else if(jQuery("#tabs-6").parent("div").attr('id') == jQuery("#tabs2").attr('id')){
		jQuery("#tabs-6").css("height", jQuery("#tabs2").height()-25+'px');
	} else{
		jQuery("#tabs-6").css("height", jQuery(".splitter-pane").height()-25+'px');
	}
	
	if(jQuery("#tabs-7").parent("div").attr('id') == jQuery("#tabs1").attr('id')){
		jQuery("#tabs-7").css("height", jQuery("#tabs1").height()-25+'px');
	}else if(jQuery("#tabs-7").parent("div").attr('id') == jQuery("#tabs2").attr('id')){
		jQuery("#tabs-7").css("height", jQuery("#tabs2").height()-25+'px');
	} else{
		jQuery("#tabs-7").css("height", jQuery(".splitter-pane").height()-25+'px');
	}
	
	if(jQuery("#tabs-8").parent("div").attr('id') == jQuery("#tabs1").attr('id')){
		jQuery("#tabs-8").css("height", jQuery("#tabs1").height()-25+'px');
	}else if(jQuery("#tabs-8").parent("div").attr('id') == jQuery("#tabs2").attr('id')){
		jQuery("#tabs-8").css("height", jQuery("#tabs2").height()-25+'px');
	} else{
		jQuery("#tabs-8").css("height", jQuery(".splitter-pane").height()-25+'px');
	}
	
	resizeTables();
	
}

function showImg(url){
	var img = jQuery("<img src="+url+">");
	jQuery("#helpImg").append(img);
	jQuery("#helpPage").hide();
	jQuery("#btIndex").show();
}

 function showHelp(url){
	jQuery("#helpPanel").load(url);
	jQuery("#helpIndex").hide();
	jQuery("#btIndex").show();
}

function addtooltip(text, event){
    jQuery('div.help').remove();
    jQuery('div.tooltip').remove();
    var y = event.pageY;
    var help = jQuery('<div class="tooltip">'+text+'</div>');
    help.css("top",(y+10)+"px" );
    jQuery("body").append(help);
    help.fadeIn("slow");
}

function removetoottip(){
	jQuery("div.tooltip").remove();
	jQuery('div.help').remove();
}

function resizeCanvasChangeTab(){
	for (var i in nameTabs){
		jQuery("#"+nameTabs[i]).css("height", jQuery("#canvas-tabs").height()-jQuery("#tabsFooter").height()-30+'px');
		jQuery("#container-"+nameTabs[i]).css("height", jQuery("#canvas-tabs").height()-jQuery("#tabsFooter").height()-30+'px');
	}
}

/*function to select all checkbox in a table*/
function selectAll(checkbox, checkboxId) {
    var elements = checkbox.form.elements;
    for (var i = 0; i < elements.length; i++) {
        var element = elements[i];
        if (checkboxId.test(element.id)) {
            element.checked = checkbox.checked;
        }
    }
}



(function(jQuery) {
	  if (jQuery.fn.style) {
	    return;
	  }

	  // Escape regex chars with \
	  var escape = function(text) {
	    return text.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, "\\$&");
	  };

	  // For those who need them (< IE 9), add support for CSS functions
	  var isStyleFuncSupported = !!CSSStyleDeclaration.prototype.getPropertyValue;
	  if (!isStyleFuncSupported) {
	    CSSStyleDeclaration.prototype.getPropertyValue = function(a) {
	      return this.getAttribute(a);
	    };
	    CSSStyleDeclaration.prototype.setProperty = function(styleName, value, priority) {
	      this.setAttribute(styleName, value);
	      var priority = typeof priority != 'undefined' ? priority : '';
	      if (priority != '') {
	        // Add priority manually
	        var rule = new RegExp(escape(styleName) + '\\s*:\\s*' + escape(value) +
	            '(\\s*;)?', 'gmi');
	        this.cssText =
	            this.cssText.replace(rule, styleName + ': ' + value + ' !' + priority + ';');
	      }
	    };
	    CSSStyleDeclaration.prototype.removeProperty = function(a) {
	      return this.removeAttribute(a);
	    };
	    CSSStyleDeclaration.prototype.getPropertyPriority = function(styleName) {
	      var rule = new RegExp(escape(styleName) + '\\s*:\\s*[^\\s]*\\s*!important(\\s*;)?',
	          'gmi');
	      return rule.test(this.cssText) ? 'important' : '';
	    }
	  }

	  // The style function
	  jQuery.fn.style = function(styleName, value, priority) {
	    // DOM node
	    var node = this.get(0);
	    // Ensure we have a DOM node
	    if (typeof node == 'undefined') {
	      return;
	    }
	    // CSSStyleDeclaration
	    var style = this.get(0).style;
	    // Getter/Setter
	    if (typeof styleName != 'undefined') {
	      if (typeof value != 'undefined') {
	        // Set style property
	        priority = typeof priority != 'undefined' ? priority : '';
	        style.setProperty(styleName, value, priority);
	      } else {
	        // Get style property
	        return style.getPropertyValue(styleName);
	      }
	    } else {
	      // Get CSSStyleDeclaration
	      return style;
	    }
	  };
	})(jQuery);