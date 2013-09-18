
var stage;
var layer;
var polygonLayer;
var background;

var history = new Array();
var history2 = new Array();

var historyStep = -1;
var rectSelect;

var positionX;
var positionY;

var dragDropGroup;
var moving;
var down;
var select;
var clickArrow;

var countObj;

		window.onload = function() {
			
			var countObj;
			if(jQuery("[id$='countObj']").val() != 0){
				countObj = jQuery("[id$='countObj']").val();
			}else{
				countObj = 0;
			}
			
			//main stage
			stage = new Kinetic.Stage({
	          container: "canvas",
	          width: 600,
	          height: 400
	        });

			//layer to the arrows
	        layer = new Kinetic.Layer();
	        
	        //layer to polygons
	        polygonLayer = new Kinetic.Layer();

	        //set width of the canvas
		    jQuery("#canvas").css("width", jQuery("#canvas1").width()+'px');

		    //white background
		    background = new Kinetic.Rect({
                x: 0, 
                y: 0,
                width: stage.getWidth(),
                height: stage.getHeight(),
                fill: "white"
            });
				
		    //add the background on layer
		    layer.add(background);
			
			//puts a different color on the canvas before it is opened
			jQuery("#canvas").css("background-color", "#FFFAFA");
			jQuery(".kineticjs-content").css("background-color", "white");

			//control of the rectangle to select objects on the screen
			moving = false;
			
			//checks if something was selected on method:rectSelectAllObj
			select = false;
			
			//checks if an arrow was clicked on metohd:arrow.'click'
			clickArrow = false;
			
			//checks if a drag and drop of a group
			dragDropGroup = false;
			
			down = false;
			
			//dotted rectangle to select objects
			rectSelect = new Kinetic.Rect({
		        x: 0,
		        y: 0,
		        width: 0,
		        height: 0,
		        //fill: 'red',
		        stroke: 'black',
		        strokeWidth: 1,
		        opacity: 0.5,
		        dashArray: [33, 10]
            });
			
			
			      
			    arrow = new Kinetic.Line({
			    	//name: 'arrow',
			    	//dashArray: [33, 10],
			    	strokeWidth: 4,
			    	stroke: "black",
			    	draggable: false
		        });

			    //------------ START STAGE CONTROL
			      
			    configureStage(layer, stage, rectSelect);
			    
			    //------------ END STAGE CONTROL
			      
			      
			    makeHistory();
			      
			      
				jQuery("#body").keydown(function(event) {
				    if (event.keyCode == 46) { // Delete
				        
				    	makeHistory();
				    	
				    	jQuery.each(polygonLayer.get('.group1'), function(index, value) {
				    		var group = this;
				    		var groupNumber = group.getId().substring(5, this.getId().length);
				    		jQuery.each(value.getChildren(), function(index, value2) {
				    			if(value2.getStroke() == "red"){
				    				removeElement(group.getId());
				    				deleteLayerChildren(groupNumber);
				    				group.remove();
								}
				    		});
						});
				    	
				    	var listSize = layer.getChildren().size();
						for (var i = 0; i < listSize; i++) {
					    	jQuery.each(layer.getChildren(), function(index, value) {
					    		if(value !== undefined){
					    			if(value.getName() !== undefined){
										var regex = new RegExp("^arrow");
							    		if(regex.test(value.getName())){
							    			if(value.getStroke() == "red"){
							    				removeLinkBt(value.output, value.nameOutput, value.input, value.nameInput);
							    				value.remove();
							    				return false;
							    			}
							    		}
					    			}
								}
					    	});
						}
				    	
				    	layer.draw();
				    	polygonLayer.draw();
				    	
				    }
				});
				
				arrow.on('click', function(e) {
			    	  
			    	  if(!e.ctrlKey){
			    		  jQuery.each(layer.getChildren(), function(index, value) {
			    			  if(value.getName() !== undefined){
			    				  var regex = new RegExp("^arrow");
					    		  if(regex.test(value.getName())){
					    			  value.setStroke("black");
					    		  }
			    			  }
				    	  });
			    	  }
			    	  
			    	  this.setStroke("red");
			    	  clickArrow = true;
			    	  stage.draw();
			    });
				
				jQuery("#canvas").click(function(e) {
					
					//remove the arrows that are outside the standard
					deleteArrowOutsideStandard();
					
					if(!e.ctrlKey){
						
						//checks if something was selected on method:rectSelectAllObj
						if(!select){
					
							jQuery.each(polygonLayer.get('.polygon1'), function(index, value) {
								value.setStroke("black");
							});
							
							//checks if an arrow was clicked on metohd:arrow.'click'
							if(!clickArrow){
								jQuery.each(layer.getChildren(), function(index, value) {
									if(value.getName() !== undefined){
										var regex = new RegExp("^arrow");
							    		if(regex.test(value.getName())){
							    			value.setStroke("black");
											layer.draw();
							    		}
									}
						    	});
							}
							clickArrow = false;
							
						}
					}
					
					down = false;
					
					moving = false;
                    
                    select = rectSelectAllObj(e);
                    
                    rectSelect.remove();
					
					layer.draw();
					polygonLayer.draw();
					
				});
				
				
				positionX = 0;
				positionY = 0;

			      
			      
		      	mountObj(polygonLayer, stage, countObj);
			      
			    stage.add(layer);
			    stage.add(polygonLayer);
			      
			    //------------- FIM CANVAS
				    
				      
		  }; // FIM window.onload
		  
		  
		  
		  //a = retangle, b = object, bx = object.getX() and by = object.getY() 
	      function collidesObj(a, b, bx, by){

	    	  if (a.getX() < bx + b.getWidth() &&
	    			  a.getX() + a.getWidth() > bx &&
	    			  a.getY() < by + b.getHeight() &&
	    			  a.getY() + a.getHeight() > by){
	    		  return true;
	          }else if (a.getX() > bx + b.getWidth() &&
	    			  a.getX() + a.getWidth() < bx &&
	    			  a.getY() < by + b.getHeight() &&
	    			  a.getY() + a.getHeight() > by){
	    		  return true;
	          }else if (a.getX() > bx + b.getWidth() &&
	    			  a.getX() + a.getWidth() < bx &&
	    			  a.getY() > by + b.getHeight() &&
	    			  a.getY() + a.getHeight() < by){
	    		  return true;
	          }if (a.getX() < bx + b.getWidth() &&
	    			  a.getX() + a.getWidth() > bx &&
	    			  a.getY() > by + b.getHeight() &&
	    			  a.getY() + a.getHeight() < by){
	    		  return true;
	          }else{
	        	  return false;
	          }
	          
	      }
	          
          function rectSelectAllObj(e){
          	
        	select = false;
        	  
          	jQuery.each(polygonLayer.get('.group1'), function(index, value) {
            	  if(collidesObj(rectSelect, value, value.getX()+40, value.getY()+50)){
            		  value.getChildren()[1].setStroke("red");
  					  polygonLayer.draw();
  					  select = true;
  				  }else{
  					if(!e.ctrlKey && !dragDropGroup){
  						value.getChildren()[1].setStroke("black");
  						polygonLayer.draw();
  					}
  				  }
  			  });
  			  
  			  return select;
          }
		  
		  function deselectOnClick(obj, e){
	  			//<![CDATA[
	  				
	  				if(!e.ctrlKey && !dragDropGroup){
	  					jQuery.each(polygonLayer.get('.polygon1'), function(index, value) {
	  						value.setStroke("black");
	  					});
	  					
	  					jQuery.each(layer.getChildren(), function(index, value) {
	  						if(value.getName() !== undefined){
	  							var regex = new RegExp("^arrow");
	  							if(regex.test(value.getName())){
	  								value.setStroke("black");
	  								layer.draw();
	  							}
	  						}
	  					});
	  					
	  				}
	  				
	  				dragDropGroup = false;
	  				
	  				obj.setStroke("red");
	  				
	  				stage.draw();
	  				
	  				//]]>
	  		  }
		  
		  function dragAndDropGroup(obj, e){
			  
	    	  dragDropGroup = true;
	    	  
			  	var group = obj;
				var newX = 0;
				var newY = 0;
				var differenceX = 0;
				var differenceY = 0;
				var mousePos = stage.getMousePosition();
				
				//if(e.ctrlKey){
					jQuery.each(polygonLayer.get('.group1'), function(index, value) {
						if(value.getId() != group.getId()){
							if(value.getChildren()[1] !== undefined && value.getChildren()[1].getStroke() == "red"){
								if(mousePos !== undefined){
									
									if(group.getX() > positionX){
										differenceX = mousePos.x - positionX;
										newX = value.getX() + differenceX;
									}else if(group.getX() < positionX){
										differenceX = positionX - mousePos.x;
										newX = value.getX() - differenceX;
									}else if(group.getX() == positionX){
										newX = group.getX();
									}
									
									if(group.getY() > positionY){
										differenceY = mousePos.y - positionY;
										newY = value.getY() + differenceY;
									}else if(group.getY() < positionY){
										differenceY = positionY - mousePos.y;
										newY = value.getY() - differenceY;
									}else if(group.getY() == positionY){
										newY = group.getY();
									}
									
									if(mousePos.x < 60){
										newX = value.getX();
									}
									if(mousePos.y < 60){
										newY = value.getY();
									}
									
									var xCanvas = stage.getWidth();
								    var yCanvas = stage.getHeight();
								    
								    if(newX < 5){
								    	newX = 5;
									}else if(newX + 80 > xCanvas){
										stage.setWidth(xCanvas + 200);
										background.setWidth(stage.getWidth());
									}
								    
								    if(newY < 5){
								    	newY = 5;
									}else if(newY + 80 > yCanvas){
										stage.setHeight(yCanvas + 200);
										background.setHeight(stage.getHeight());
									}
								    
								    value.setPosition(newX, newY);
									changePositionArrow(value);
								}
							}
						}
					});
					
					if(group.getX() > positionX){
						positionX = positionX+differenceX;
					}else if(group.getX() < positionX){
						positionX = positionX-differenceX;
					}
					
					if(group.getY() > positionY){
						positionY = positionY+differenceY;
					}else if(group.getY() < positionY){
						positionY = positionY-differenceY;
					}
					
				//}
			}
		  
		  function selectAll(){
			  jQuery.each(polygonLayer.get('.polygon1'), function(index, value) {
				  value.setStroke("red");
				  polygonLayer.draw();
			  });
			  
			  jQuery.each(layer.getChildren(), function(index, value) {
				  if(value.getName() !== undefined){
					  var regex = new RegExp("^arrow");
		    		  if(regex.test(value.getName())){
		    			  value.setStroke("red");
						  layer.draw();
		    		  }
				  }
	    	  });
			  
		  }
		  
		  function deselectAll(){
			  
			  jQuery.each(polygonLayer.get('.polygon1'), function(index, value) {
				  value.setStroke("black");
				  polygonLayer.draw();
			  });
			  
			  jQuery.each(layer.getChildren(), function(index, value) {
				  if(value.getName() !== undefined){
					  var regex = new RegExp("^arrow");
		    		  if(regex.test(value.getName())){
		    			  value.setStroke("black");
						  layer.draw();
		    		  }
				  }
	    	  });
			  
		  }
		  
		  function deleteSelected(){
			  
			  makeHistory();
			  
			  jQuery.each(polygonLayer.get('.group1'), function(index, value) {
				  var group = this;
				  var groupNumber = group.getId().substring(5, this.getId().length);
				  jQuery.each(value.getChildren(), function(index, value2) {
					  if(value2.getStroke() == "red"){
						  deleteLayerChildren(groupNumber);
						  group.remove();
					  }
				  });
			  });
			  
			  	var listSize = layer.getChildren().size();
				for (var i = 0; i < listSize; i++) {
				  jQuery.each(layer.getChildren(), function(index, value) {
					  if(value !== undefined){
						  if(value.getName() !== undefined){
							  var regex = new RegExp("^arrow");
				    		  if(regex.test(value.getName())){
				    			  if(value.getStroke() == "red"){
									  value.remove();
									  return false;
								  }
				    		  }
						  }
					  }
					  
		    	  });
				}
			  
			  polygonLayer.draw();
			  layer.draw();
			  
		  }
		  
		  //remove the arrows that are outside the standard
		  function deleteArrowOutsideStandard(){
			  var listSize = layer.getChildren().size();
			  for (var i = 0; i < listSize; i++) {
				  jQuery.each(layer.getChildren(), function(index, value) {
					  if(value !== undefined){
						  if(value.getName() !== undefined){
							  var regex = new RegExp("^arrow([0-9]+)-([0-9]+)");
							  if(!regex.test(value.getName())){
								  value.remove();
								  return false;
							  }
						  }
					  }
				  });
			  }
		  }
		  
		  
		  function getCircleLineIntersectionPoint(pointAx, pointAy, pointBx, pointBy, circleX, circleY, radius) {
			  
			  var baX = pointBx - pointAx;
		      var baY = pointBy - pointAy;
		      var caX = circleX - pointAx;
		      var caY = circleY - pointAy;
		
		      var a = baX * baX + baY * baY;
		      var bBy2 = baX * caX + baY * caY;
		      var c = caX * caX + caY * caY - radius * radius;
		
		      var pBy2 = bBy2 / a;
		      var q = c / a;
		
		      var disc = pBy2 * pBy2 - q;
		        
		      var tmpSqrt = Math.sqrt(disc);
		      var abScalingFactor1 = -pBy2 + tmpSqrt;
		        
		      var px = pointAx - baX * abScalingFactor1;
		      var py = pointAy - baY * abScalingFactor1;
		        
		      return [px, py];
		  }
		  
		  
		  function deleteLayerChildren(groupNumber){
			  
			  var listSize = layer.getChildren().size();
			  for (var i = 0; i < listSize; i++) {
				  jQuery.each(layer.getChildren(), function(index, value) {
					  if(value !== undefined){
						  if(value.getName() !== undefined){
							  
							  var regex = new RegExp("^arrow"+groupNumber+"-([0-9]+)");
							  if(regex.test(value.getName())){
								  value.remove();
								  return false;
							  }
							  
							  var regex2 = new RegExp("^arrow([0-9]+)-"+groupNumber);
							  if(regex2.test(value.getName())){
								  value.remove();
								  return false;
							  }
							  
						  }
					  }
				  });
			  }
			  
		  }
		  
		  function changePositionArrow(obj){
			  
			  var group = obj;
			  var groupNumber = obj.getId().substring(5, obj.getId().length);
				
				jQuery.each(layer.getChildren(), function(index, value) {
					if(value.getName() !== undefined){
						//alert(value.getName());
						var regex = new RegExp("^arrow"+groupNumber+"-([0-9]+)");
			    		if(regex.test(value.getName())){
			    			//alert(value.getName());
			    			value.getPoints()[0].x = group.getX()+40;
							value.getPoints()[0].y = group.getY()+50;
							
							var idGroup = value.getName().split("-");
							var g = polygonLayer.get('#group'+idGroup[1]);
							
							var newPoint;
							var newPoint2;
							var angle;
							
							if(g[0] !== undefined){
							
								var idCircle = g[0].getChildren()[0].getId().split("-");
								if(idCircle[1] == "2"){
									newPoint = getCircleLineIntersectionPoint(group.getX()+40, group.getY()+50, g[0].getX()+100, g[0].getY()+50, g[0].getX()+100, g[0].getY()+50, 47);
									newPoint2 = getCircleLineIntersectionPoint(group.getX()+40, group.getY()+50, g[0].getX()+100, g[0].getY()+50, g[0].getX()+100, g[0].getY()+50, 60);
									angle = Math.atan2(newPoint[1]-group.getY()-50, newPoint[0]-group.getX()-40);
								} else if(idCircle[1] == "1"){
									newPoint = getCircleLineIntersectionPoint(group.getX()+40, group.getY()+50, g[0].getX()+40, g[0].getY()+50, g[0].getX()+40, g[0].getY()+50, 47);
									newPoint2 = getCircleLineIntersectionPoint(group.getX()+40, group.getY()+50, g[0].getX()+40, g[0].getY()+50, g[0].getX()+40, g[0].getY()+50, 60);
									angle = Math.atan2(newPoint[1]-group.getY()-50, newPoint[0]-group.getX()-40);
								}
								
								var headlen = 20;
								var headlen2 = 10;
								
				    			value.getPoints()[1].x = newPoint2[0];
				    			value.getPoints()[1].y = newPoint2[1];
				    			
				    			value.getPoints()[2].x = newPoint2[0];
				    			value.getPoints()[2].y = newPoint2[1];
				    			
				    			value.getPoints()[3].x = newPoint[0]-headlen*Math.cos(angle-Math.PI/6);
				    			value.getPoints()[3].y = newPoint[1]-headlen*Math.sin(angle-Math.PI/6);
				    			
				    			value.getPoints()[4].x = newPoint[0];
				    			value.getPoints()[4].y = newPoint[1];
				    			
				    			value.getPoints()[5].x = newPoint[0]-headlen*Math.cos(angle+Math.PI/6);
				    			value.getPoints()[5].y = newPoint[1]-headlen*Math.sin(angle+Math.PI/6);
				    			
				    			value.getPoints()[6].x = newPoint2[0];
				    			value.getPoints()[6].y = newPoint2[1];
				    			
				    			value.getPoints()[7].x = newPoint2[0]-headlen2*Math.cos(angle-Math.PI/3);
				    			value.getPoints()[7].y = newPoint2[1]-headlen2*Math.sin(angle-Math.PI/3);
				    			
				    			value.getPoints()[8].x = newPoint2[0];
				    			value.getPoints()[8].y = newPoint2[1];
				    			
				    			value.getPoints()[9].x = newPoint2[0]-headlen2*Math.cos(angle+Math.PI/3);
				    			value.getPoints()[9].y = newPoint2[1]-headlen2*Math.sin(angle+Math.PI/3);
			    			
							}
			    			
			    		}
					}
		    	});
				
				jQuery.each(layer.getChildren(), function(index2, value2) {
					if(value2.getName() !== undefined){
						//alert(value2.getName());
						var regex = new RegExp("^arrow([0-9]+)-"+groupNumber);
						//alert(regex.test(value2.getName()));
			    		if(regex.test(value2.getName())){
			    			//alert(value2.getName());
			    			
			    			var newPoint = getCircleLineIntersectionPoint(value2.getPoints()[0].x, value2.getPoints()[0].y, group.getX()+40, group.getY()+50, group.getX()+40, group.getY()+50, 47);
			    			var newPoint2 = getCircleLineIntersectionPoint(value2.getPoints()[0].x, value2.getPoints()[0].y, group.getX()+40, group.getY()+50, group.getX()+40, group.getY()+50, 60);
			    			
			    			value2.getPoints()[1].x = newPoint2[0];
			    			value2.getPoints()[1].y = newPoint2[1];
			    			
			    			var headlen = 20;
			    			var headlen2 = 10;
			    			
			    			var angle = Math.atan2(newPoint[1]-value2.getPoints()[0].y, newPoint[0]-value2.getPoints()[0].x);
			    			
			    			value2.getPoints()[2].x = newPoint2[0];
			    			value2.getPoints()[2].y = newPoint2[1];
			    			
			    			value2.getPoints()[3].x = newPoint[0]-headlen*Math.cos(angle-Math.PI/6);
			    			value2.getPoints()[3].y = newPoint[1]-headlen*Math.sin(angle-Math.PI/6);
			    			
			    			value2.getPoints()[4].x = newPoint[0];
			    			value2.getPoints()[4].y = newPoint[1];
			    			
			    			value2.getPoints()[5].x = newPoint[0]-headlen*Math.cos(angle+Math.PI/6);
			    			value2.getPoints()[5].y = newPoint[1]-headlen*Math.sin(angle+Math.PI/6);
			    			
			    			value2.getPoints()[6].x = newPoint2[0];
			    			value2.getPoints()[6].y = newPoint2[1];
			    			
			    			value2.getPoints()[7].x = newPoint2[0]-headlen2*Math.cos(angle-Math.PI/3);
			    			value2.getPoints()[7].y = newPoint2[1]-headlen2*Math.sin(angle-Math.PI/3);
			    			
			    			value2.getPoints()[8].x = newPoint2[0];
			    			value2.getPoints()[8].y = newPoint2[1];
			    			
			    			value2.getPoints()[9].x = newPoint2[0]-headlen2*Math.cos(angle+Math.PI/3);
			    			value2.getPoints()[9].y = newPoint2[1]-headlen2*Math.sin(angle+Math.PI/3);
			    		}
					}
		    	});
				
				layer.draw();
				polygonLayer.draw();
			  
		  }
		  
		  
		  function rulesDragAndDropObj(pos, valueX, valueY){
			  
			  var xCanvas = stage.getWidth();
			  var yCanvas = stage.getHeight();
			  
			  if (pos.x < 5) {
				  var newX = 5;
			  }else if(pos.x + valueX > xCanvas){
				  newX = pos.x;
				  stage.setWidth(xCanvas + 200);
				  background.setWidth(stage.getWidth());
			  }else{
				  newX = pos.x;
			  }
			  
			  if (pos.y < 5) {
				  var newY = 5;
			  }else if(pos.y + valueY > yCanvas){
				  newY = pos.y;
				  stage.setHeight(yCanvas + 200);
				  background.setHeight(stage.getHeight());
			  }else{
				  newY = pos.y;
			  }
			  
			  return {
				  x: newX,
				  y: newY
			  };
			  
		  }
		  
		  function rulesDragAndDropGroupObj(pos, valueX, valueY){
			  //<![CDATA[
			  
			  var xCanvas = stage.getWidth();
			  var yCanvas = stage.getHeight();
			  var newX = pos.x;
			  var newY = pos.y;
			  
			  if (pos.x < 5) {
				  newX = 5;
			  }else if (valueX != null && pos.x < valueX ) {
				  newX = valueX;
			  }else if(pos.x + 100 > xCanvas){
				  newX = pos.x;
				  stage.setWidth(xCanvas + 200);
				  background.setWidth(stage.getWidth());
			  }
			  
			  if (pos.y < 5) {
				  newY = 5;
			  }else if (valueY != null &&  pos.y < valueY ) {
				  newY = valueY;
			  } else if(pos.y + 100 > yCanvas){
				  newY = pos.y;
				  stage.setHeight(yCanvas + 200);
				  background.setHeight(stage.getHeight());
			  }
			  
			  return {
				  x: newX,
				  y: newY
			  };
			 
			  //]]>
		  }
		  
		  
		  function makeHistory() {
			    historyStep++;
			    if (historyStep < history.length) {
			        history.length = historyStep;
			        history2.length = historyStep;
			    }
			    
			    jQuery.each(polygonLayer.getChildren(), function(index, value) {
			    	if(value === undefined){
			    		value.remove();
			    	}
			    });
			    
			    jQuery.each(layer.getChildren(), function(index, value) {
			    	if(value === undefined){
			    		value.remove();
			    	}
			    });
			    
			    deleteArrowOutsideStandard();
			    rectSelect.remove();
			    
			    var json2 = layer.toJSON();
			    json2 = json2.replace('"children":"', '"children":');
			    json2 = json2.substring(0, json2.length-2) + json2.substring(json2.length-1, json2.length);
			    json2 = json2.replace(/\\/g, "");
			    
			    history2.push(json2);
			    console.log(json2);
			    
			    var json = polygonLayer.toJSON();
			    json = json.replace('"children":"', '"children":');
			    json = json.substring(0, json.length-2) + json.substring(json.length-1, json.length);
			    json = json.replace(/\\/g, "");
			    history.push(json);
			    console.log(json);
			    
			}
		  
			function undoHistory() {
			    if (historyStep > 0) {
			        historyStep--;
			        
			        var json = history[historyStep];
			        var json2 = history2[historyStep];
			        
			        polygonLayer = Kinetic.Node.create(json, 'canvas');
			        layer = Kinetic.Node.create(json2, 'canvas');
			        
			        ready(layer, polygonLayer);
			    }
			}
			
			function addElements(positions){
				countObj = 0;
				var positionsArrays = JSON.parse(positions);
				
				for (var i=0; i< positionsArrays.length;i++){
		    	  	addElement(positionsArrays[i][0], positionsArrays[i][1], positionsArrays[i][2], positionsArrays[i][3], positionsArrays[i][4]);
		    	}
			}
			
			function addElement(elementId, elementType, elementImg, posx, posy){
				console.log('addElement');

				var img = new Image({
					width: 16,
					height: 16
				});
				img.src = elementImg;

				var posInitX = 40;
				var poxInitY = 50;
				
				
				var result = createPolygon(img, posInitX, poxInitY);
				var polygon = result[0];
				

				var circle1 = new Kinetic.Circle({
					x: 40,
					y: 50,
					radius: 42,
					//fill: 'white',
					//stroke: 'black',
					//strokeWidth: 1,
					draggable: false
				});
				
				configureCircle(circle1, polygonLayer);
				
				var srcImageText = new Kinetic.Text({
					text: "srcImageText"
				});
			    srcImageText.setStroke(null);
			    
			    var typeText = new Kinetic.Text({
					text: elementType
			    });
			    
				countObj++;
				var group = createGroup(circle1, polygon, countObj, 
						   srcImageText, typeText);
				
				polygonLayer.add(group);
				
				configureGroup(group, posx, posy, polygon, countObj,
						rectSelect, positionX, positionY, stage);
				
				updateIdObj("group"+countObj, elementId);
				updateTypeObj("group"+countObj, elementId);
				
				stage.draw();
				
			}
			
			function addLinks(positions){
				countObj = 0;
				var linkArrays = JSON.parse(positions);
				
				for (var i=0; i< linkArrays.length;i++){
		    	  	addLink(linkArrays[i][0], linkArrays[i][1]);
		    	}
			}
			
			function addLink(inId, outId){
				//out
				var polygonGroupOut = getElement(inId);
				var circleOut = polygonGroupOut.getChildren()[0];
				circleOut.setFill('white');
				arrow.setPoints([polygonGroupOut.getX()+40, polygonGroupOut.getY()+50, polygonGroupOut.getX()+40+1, polygonGroupOut.getY()+50+1 ]);
				//arrow.setName("arrow"+circleOut.getName().substring(6,circleOut.getName().length));
						
				//in
				var polygonGroupIn = getElement(outId);
				var circleIn = polygonGroupIn.getChildren()[0];
				circleIn.setFill('white');
					
				arrow.setName("arrow"+circleOut.getName().substring(6,circleOut.getName().length)+"-"+circleIn.getName().substring(6, circleIn.getName().length));

				var newPoint = getCircleLineIntersectionPoint(arrow.getPoints()[0].x, arrow.getPoints()[0].y, polygonGroupIn.getX()+40, polygonGroupIn.getY()+50, polygonGroupIn.getX()+40, polygonGroupIn.getY()+50, 47);
				var newPoint2 = getCircleLineIntersectionPoint(arrow.getPoints()[0].x, arrow.getPoints()[0].y, polygonGroupIn.getX()+40, polygonGroupIn.getY()+50, polygonGroupIn.getX()+40, polygonGroupIn.getY()+50, 60);

				var headlen = 20;
				var headlen2 = 10;
				var angle = Math.atan2(newPoint[1]-arrow.getPoints()[0].y,newPoint[0]-arrow.getPoints()[0].x);

				arrow.setPoints([arrow.getPoints()[0].x, arrow.getPoints()[0].y, 
				                 newPoint2[0], newPoint2[1], 
				                 newPoint2[0], newPoint2[1], 
				                 newPoint[0]-headlen*Math.cos(angle-Math.PI/6), newPoint[1]-headlen*Math.sin(angle-Math.PI/6), 
				                 newPoint[0], newPoint[1], 
				                 newPoint[0]-headlen*Math.cos(angle+Math.PI/6), newPoint[1]-headlen*Math.sin(angle+Math.PI/6), 
				                 newPoint2[0], newPoint2[1], 
				                 newPoint2[0]-headlen2*Math.cos(angle-Math.PI/3), newPoint2[1]-headlen2*Math.sin(angle-Math.PI/3), 
				                 newPoint2[0], newPoint2[1], 
				                 newPoint2[0]-headlen2*Math.cos(angle+Math.PI/3), newPoint2[1]-headlen2*Math.sin(angle+Math.PI/3)
				                 ]);

				layer.add(arrow.clone());
				layer.draw();
				polygonLayer.draw();

				makeHistory();
			}

			function redoHistory() {
				
			    if (historyStep < history.length-1) {
			        historyStep++;
			        
			        var json = history[historyStep];
			        var json2 = history2[historyStep];
			        
			        console.log(json2);
			        console.log('test');
			        
			        polygonLayer = Kinetic.Node.create(json, 'canvas');
			        layer = Kinetic.Node.create(json2, 'canvas');
			        
			        ready(layer, polygonLayer);
			    }
			}
			
			
		   function ready(layer, polygonLayer) {
			   
			   	//variable to control image of the object
			    var imgTab1 = new Image();
			    //imgTab1.src = '../image/icons/button-cut.png';
		    	  
			    //control of the rectangle to select objects on the screen
				moving = false;
				
				stage.remove();
				
				//main stage
				stage = new Kinetic.Stage({
		          container: "canvas",
		          width: 600,
		          height: 400
		        });
				
				stage.setWidth(layer.getChildren()[0].getWidth());
				stage.setHeight(layer.getChildren()[0].getHeight());
				background.setWidth(stage.getWidth());
				background.setHeight(stage.getHeight());
				
				
				//add the background on layer
			    layer.add(background);
			    background.moveToBottom();
			    
				//set width of the canvas
			    jQuery("#canvas").css("width", jQuery("#canvas1").width()+'px');
				
				stage.add(layer);
		        stage.add(polygonLayer);
				
		        configureStage(layer, stage, rectSelect);
			      
			    jQuery.each(polygonLayer.get('.group1'), function(index, value) {
			    	var group = this;
					
			    	var circle1 = value.getChildren()[0];
			    	var polygon1 = value.getChildren()[1];
			    	var text = value.getChildren()[2];
			    	
			    	//alert(text.getText().substring(2));
			    	imgTab1.src = './'+text.getText().substring(2);
			    	polygon1.setFillPatternImage(imgTab1);
			    	
			    	group.setDragBoundFunc(function(pos){ return rulesDragAndDropObj(pos, 80, 80); });
			    	
			    	configureGroupListeners(group, positionX, positionY, rectSelect);
			    	
			    	configureCircle(circle1, polygonLayer);
					  
					polygon1.on('click', function(e) {
						deselectOnClick(this, e);
					});
					  
					polygon1.on('mousedown', function(e) {
						deselectOnClick(this, e);
					});
			    	
			    	polygonLayer.draw();
			    });
			    
		   }
		   
		   
		   /**
		   * 
		   * Method to mount the footer of canvas and your tabs.
		   * Makes one for the 'divs' and 'images' to create the objects and put their functionalities. 
		   * generates javascript for HTML5 canvas 
		   * 
		   */
		   function mountObj(polygonLayer, stage, countObj) {
			   
		   
			   var circle1 = new Kinetic.Circle({
					x: 40,
			        y: 50,
			        radius: 42,
			        //fill: 'white',
			        //stroke: 'black',
			        //strokeWidth: 1,
			        draggable: false
			    });
			   
			   configureCircle(circle1, polygonLayer);
			   
			   //for list divs
			   jQuery( "#tabsFooter ul:first li" ).each(function(index) {
				   
				    var posInitX = 40;
				    var poxInitY = 50;
				   
				   	var nameDiv = jQuery(this).attr("aria-controls");
				   
				   	if(nameDiv != undefined){ //groupNumber
					   	
					   	//------------ START TAB
					   	
						//stage to footer
					    stageTab = new Kinetic.Stage({
					        container: nameDiv,
					        width: 500,
					        height: 100
					    });
					    
					    //layer to footer tab1
					    var layerTab = new Kinetic.Layer();
					    
					    
					    //var countObj = 0;
					    
					    //for list of obj imagens
				    	jQuery( "#"+nameDiv ).find("img").each(function(index) {
							
						    //variable to control image of the object
						    var imgTab = new Image({
						    	width: 16,
						        height: 16
						    });
						    imgTab.src = jQuery(this).attr("src");
						    
						    var srcImageText = new Kinetic.Text({
								text: jQuery(this).attr("src")
							});
						    srcImageText.setStroke(null);
						    
						    var typeText = new Kinetic.Text({
								text: jQuery(this).next().text()
							});
						    typeText.setStroke(null);
						   
						    
						  	//------------------ START GROUP 
						    
						    var result = createPolygon(imgTab, posInitX, poxInitY);
						    var polygon = result[0];
						    var polygonTab = result[1];
						    var polygonTabImage = result[2];
							
						      var polygonTabFake = new Kinetic.RegularPolygon({
							        x: 40,
							        y: 50,
							        opacity: 0,
							        radius: 27,
							        sides: 4,
							        stroke: 'black',
							        strokeWidth: 4,
							        fillPatternImage: imgTab,
							        fillPatternOffset: [7, 7],
							        fillPatternRepeat: 'no-repeat',
							        fillPatternScale: 1.8,
							        fillPatternRotationDeg: 315,
							        draggable: true
							  });
						      polygonTabFake.rotateDeg(45);
						      polygonTabFake.setAbsolutePosition(posInitX, poxInitY);
						      
						      posInitX = posInitX + 60;
						      
							
							polygonTabFake.on('dragstart', function() {
								
								jQuery('#body').css('cursor','url('+ polygonTabImage +') 30 30,default');
								
								this.remove();
								layerTab.add(polygonTabFake.clone());
								layerTab.draw();
								
								
							});
							
							//var countObj = 0;
							
							positionX = 0;
							positionY = 0;
							
							polygonTabFake.on('dragend', function() {

								try {
									document.body.style.cursor = 'default';

									var mousePos = stageTab.getMousePosition();
									if(mousePos.x > 0 || mousePos.y > 0){
										this.remove();
										layerTab.add(polygonTabFake.clone());
										layerTab.draw();
									}

								} catch(e) {
									try {
										
										countObj++;
										
										var group1 = createGroup(circle1, polygon, countObj, 
												   srcImageText, typeText);
										
										polygonLayer.add(group1);
										this.group = group1;
								    	
								    	
								    	//-----
										var mousePosStage = stage.getMousePosition();

										configureGroup(this.group, mousePosStage.x-30, mousePosStage.y-30, polygon, countObj,
												rectSelect, positionX, positionY, stage);

										layerTab.add(polygonTabFake.clone());
										layerTab.draw();
										
										addElementBt(typeText.getText(), "group" + countObj);

									} catch(e) {

										document.body.style.cursor = 'default';
//										removeElement(this.group.getId());
										this.remove();
										layerTab.add(polygonTabFake.clone());
										layerTab.draw();

										makeHistory();
									}
								}
							});
							
						      
						    polygonTabFake.on('mouseup', function() {
						    	this.remove();
						    	layerTab.add(polygonTabFake.clone());
						    	layerTab.draw();
						    });
						      
				    
						    layerTab.add(polygonTab);
						    layerTab.add(polygonTabFake.clone());
						    
						    //jQuery( "#"+nameDiv ).find("img").remove();
						    
					    });
					    //END for obj imagens
					    
				    	 
					     stageTab.add(layerTab);
				    
				   	}// END IF
				     
			   });
			   //END for divs
			   
		   }
		   
		   function clearCanvas(){
			   polygonLayer.removeChildren();
			   layer.removeChildren();
			   stage.draw();
		   }
		   
		   function getElement(id){
			   for(var i = 0; i < polygonLayer.getChildren().length; i++) {
					if(polygonLayer.getChildren()[i].getChildren()[3].getText() == id){
						return polygonLayer.getChildren()[i];
					}
				}
		   }
		   
		   /**
		   * 
		   * Method to update the new id of element. call by a4j:jsFunction oncomplete
		   * add the new Element and retrive the new id. 
		   * 
		   */
		   function updateTypeObj(groupID, elementId) {
			   
			   var text = polygonLayer.get('#' + groupID)[0].getChildren()[3];
			   text.setText(elementId);
			   
		   }
		   
		   function updateLink(linkName, nameOutput, nameInput) {
			   
				jQuery.each(layer.getChildren(), function(index, value) {
					if(value.getName() == linkName){
						value.nameOutput = nameOutput;
						value.nameInput = nameInput;
					}
				});
			   
		   }
		   
		   function updateElementsPositions() {
			   
				jQuery.each(polygonLayer.getChildren(), function(index, value) {
					updatePosition(value.getId(), value.getX(), value.getY());
				});
			   
		   }
		   
		   function configureCircle(circle1, polygonLayer){
			   
				circle1.on('mouseover', function() {
					document.body.style.cursor = 'pointer';
				});
				
				circle1.on('mouseout', function() {
					document.body.style.cursor = 'default';
				});
				
				  down = false;
				  
				  circle1.on("click", function(e) {
					  
					  var circle = this;
					  
					 if (down) {
						  down = false;
			    		  
			    		  arrow.setName(arrow.getName()+"-"+this.getName().substring(6, this.getName().length));
			    		  
			    		  var polygonGroup = polygonLayer.get('#'+this.getParent().getId());
			    		  
			    		  var newPoint = getCircleLineIntersectionPoint(arrow.getPoints()[0].x, arrow.getPoints()[0].y, polygonGroup[0].getX()+40, polygonGroup[0].getY()+50, polygonGroup[0].getX()+40, polygonGroup[0].getY()+50, 47);
			    		  var newPoint2 = getCircleLineIntersectionPoint(arrow.getPoints()[0].x, arrow.getPoints()[0].y, polygonGroup[0].getX()+40, polygonGroup[0].getY()+50, polygonGroup[0].getX()+40, polygonGroup[0].getY()+50, 60);
			    		  
			    		  var headlen = 20;
			    		  var headlen2 = 10;
			    		  var angle = Math.atan2(newPoint[1]-arrow.getPoints()[0].y,newPoint[0]-arrow.getPoints()[0].x);
			    		  
			    		  arrow.setPoints([arrow.getPoints()[0].x, arrow.getPoints()[0].y, 
			    		                   newPoint2[0], newPoint2[1], 
			    		                   newPoint2[0], newPoint2[1], 
			    		                   newPoint[0]-headlen*Math.cos(angle-Math.PI/6), newPoint[1]-headlen*Math.sin(angle-Math.PI/6), 
			    		                   newPoint[0], newPoint[1], 
			    		                   newPoint[0]-headlen*Math.cos(angle+Math.PI/6), newPoint[1]-headlen*Math.sin(angle+Math.PI/6), 
			    		                   newPoint2[0], newPoint2[1], 
			    		                   newPoint2[0]-headlen2*Math.cos(angle-Math.PI/3), newPoint2[1]-headlen2*Math.sin(angle-Math.PI/3), 
			    		                   newPoint2[0], newPoint2[1], 
			    		                   newPoint2[0]-headlen2*Math.cos(angle+Math.PI/3), newPoint2[1]-headlen2*Math.sin(angle+Math.PI/3)
			    		                   ]);
			    		  
			    		  var arrowClone = arrow.clone();
			    		  arrowClone.input = this.getParent().getId();
			    		  arrowClone.output = arrow.output;
			    		  layer.add(arrowClone);
			    		  
			    		  //remove the arrows that are outside the standard
			    		  deleteArrowOutsideStandard();
			    		  
			    		  circle.setFill('white');
			    		  
			    		  layer.draw();
			    		  polygonLayer.draw();
			    		  
			    		  makeHistory();
			    		  
			    		  addLinkModalBt(arrowClone.output, arrowClone.input, arrowClone.getName());
			    		  
			    	  }else{
			    		  down = true;
				          
				          //alert(this.getParent().getId());
				          
				          var polygonGroup = polygonLayer.get('#'+this.getParent().getId());
				          arrow.setPoints([polygonGroup[0].getX()+40, polygonGroup[0].getY()+50, polygonGroup[0].getX()+40+1, polygonGroup[0].getY()+50+1 ]);
				          arrow.setName("arrow"+this.getName().substring(6,this.getName().length));

				          arrow.output = this.getParent().getId();

				          circle.setFill('white');
				          
				          layer.add(arrow.clone());
				          
				          layer.draw();
				          polygonLayer.draw();
				          
			    	  }
					 
			      });
				  
				  return circle1;
		   }
		   
		   function configureStage(layer, stage, rectSelect){
			   stage.on("mousedown", function(e){
		    		  if (moving){
		    			  moving = false;
		    			  layer.draw();
		    		  } else {
		    			  var mousePos = stage.getMousePosition();
		    			  layer.add(rectSelect);
		    			  rectSelect.setX(mousePos.x);
		    			  rectSelect.setY(mousePos.y);
		    			  rectSelect.setWidth(0);
		    			  rectSelect.setHeight(0);
		    			  moving = true;
		    			  layer.drawScene();
		    		  }
	              });
			    
			    stage.on("mousemove", function() {
			    	  var mousePos = stage.getMousePosition();
			    	  //click for arrow
			    	  if (down) {
			    		  arrow.getPoints()[1].x = mousePos.x;
			              arrow.getPoints()[1].y = mousePos.y;
			              down = true;
			              layer.draw();
			          }
			    	  //click for rectangle
			          if (moving) {
	                      rectSelect.setWidth(mousePos.x - rectSelect.getX());
	                      rectSelect.setHeight(mousePos.y - rectSelect.getY());
	                      moving = true;
	                      layer.draw();
	                  }
			      });
			      
			      stage.on("mouseup", function(e){
	                    moving = false;
	                    select = rectSelectAllObj(e);
	                    rectSelect.remove();
	              });
		   }
		   
		   function configureGroupListeners(group, positionX, positionY, rectSelect){
				group.on('mouseenter', function(e) {
					positionX = this.getX()+40;
					positionY = this.getY()+50;
				});
				
				group.on('dragstart dragmove', function(e) {
					rectSelect.remove();
					dragAndDropGroup(this, e);
					changePositionArrow(this);
				});
				
				group.on('dragend', function(e) {
					group.setDragBoundFunc(function(pos){ return rulesDragAndDropObj(pos, 80, 80); });
					makeHistory();
				});
		   }
		   
		   

		   function createGroup(circle1, polygon, countObj,
				   srcImageText, typeText) {

			   var group1 = new Kinetic.Group({
				   draggable : true,
				   id : "group" + countObj,
				   name : 'group1',
				   dragBoundFunc : function(pos) {
					   return rulesDragAndDropObj(pos, 80, 80);
				   }
			   });

			   var circ = circle1.clone();
			   var poly = polygon.clone();
			   circ.setId("circle" + countObj + "-1");
			   circ.setName("circle" + countObj);
			   circ.setPosition(40, 50);
			   poly.setPosition(40, 50);
			   group1.add(circ);
			   group1.add(poly);
			   group1.add(srcImageText.clone());
			   group1.add(typeText.clone());

			   return group1;
		   }
		   

		   function configureGroup(group1, mousePosX, mousePosY, polygon, countObj,
				   rectSelect, positionX, positionY, stage) {

			   document.body.style.cursor = 'default';

			   deselectAll();
			   polygon.setStroke("red");

			   group1.setPosition(mousePosX, mousePosY);

			   jQuery("#countObj").val(countObj);

			   configureGroupListeners(group1, positionX, positionY, rectSelect);

			   group1.on('dblclick', function(e) {
				   
				   var objImg = this.getChildren()[1].clone();
				   objImg.setStroke("black");
				   var imagePath = objImg.toDataURL({
				    	  width: 80,
				    	  height: 80
				      });
				   
				   //alert(imagePath);
				   
				   openModal(this.getId(), imagePath);

			   });

			   makeHistory();
		   }
		   
		   function createPolygon(imgTab, posInitX, poxInitY){

				var polygonTab = new Kinetic.RegularPolygon({
				        x: 40,
				        y: 50,
				        radius: 27,
				        sides: 4,
				        stroke: 'black',
				        strokeWidth: 4,
				        fillPatternImage: imgTab,
				        fillPatternOffset: [7, 7],
				        fillPatternRepeat: 'no-repeat',
				        fillPatternScale: 1.8,
				        fillPatternRotationDeg: 315,
				        draggable: false
				  });
				
				polygonTab.rotateDeg(45);
				
				var polygonTabImage = polygonTab.toDataURL({
			    	  width: 75,
			    	  height: 75
			      });
				
				polygonTab.setAbsolutePosition(posInitX, poxInitY);
			    polygonTab.setStroke('red');
			    polygonTab.setStroke('black');
			    
			    var polygon = polygonTab.clone({
					name: 'polygon1',
					draggable: false
				});

				polygon.on('click', function(e) {

					deselectOnClick(this, e);

				});

				polygon.on('mousedown', function(e) {

					deselectOnClick(this, e);

				});
				
				return [polygon, polygonTab, polygonTabImage];
			}
		  