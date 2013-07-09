 //<![CDATA[
	    
	    		 (function (jquery) {

					var settings = {
						tabBarWidth: 500
				    }

				    jquery.fn.scrollabletab = function (options) {
				
				        var ops = jquery.extend(settings, options);
				        var ul = this.children('ul').first();

				        var leftArrow = this.find(".buttonsArrowsLeft").first();
				        var rightArrow = this.find(".buttonsArrowsRight").first();
				
				        var moveable = ul;
				        leftArrow.click(function () {
				            var offset = ops.tabBarWidth / 6;
				            var currentPosition = moveable.css('left').replace('px', '') / 1;
				
				            if (currentPosition + offset >= 0) {
				                moveable.stop().animate({ left: '0' }, 'slow');
				            }
				            else {
				                moveable.stop().animate({ left: currentPosition + offset + 'px' }, 'slow');
				            }
				        });
				
				        rightArrow.click(function () {
				            var offset = ops.tabBarWidth / 6;
				            var currentPosition = moveable.css('left').replace('px', '') / 1;
				            var tabsRealWidth = 0;
				            ul.find('li').each(function (index, element) {
				                tabsRealWidth += jquery(element).width();
				                tabsRealWidth += (jquery(element).css('margin-right').replace('px', '') / 1);
				            });
				            
				            if (currentPosition + tabsRealWidth + 35 > ul.offsetParent().width() ) {
				                moveable.stop().animate({ left: currentPosition - offset + 'px' }, 'slow');
				            }
				        });
				
				        return this;
				    };
				
				})(jQuery);
	    	
	    //]]>