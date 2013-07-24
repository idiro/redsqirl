
var stage;
var layer;
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
			
			
			/*jQuery( "#tabsFooter ul:first li" ).each(function(index) {
				   
				//alert(jQuery(this).attr("aria-controls"));
				
				var aux = jQuery(this).attr("aria-controls");
				
				jQuery( "#"+aux ).find("img").each(function(index) {
					
					alert(jQuery(this).attr("src"));
					
				});
				   
			});*/
			
			
			
			var countObj = 0;
			
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
			
			
			
			//------------ START TAB 1
			/*
				//stage to footer
			    stageTab1 = new Kinetic.Stage({
			        container: 'canvasTabFooter1',
			        width: 500,
			        height: 100
			    });
			    
			    //layer to footer tab1
			    layerTab1 = new Kinetic.Layer();
			    
			    //variable to control image of the object
			    var imgTab1 = new Image();
			    imgTab1.src = '../image/icons/button-cut.png';*/
			      
			    arrow = new Kinetic.Line({
			    	//name: 'arrow',
			    	//dashArray: [33, 10],
			    	strokeWidth: 4,
			    	stroke: "black",
			    	draggable: false
		        });

			    
			    
			  //------------ START STAGE CONTROL
			      
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
				    				deleteLayerChildren(groupNumber);
				    				group.remove();
								}
				    		});
						});
				    	
				    /*	jQuery.each(polygonLayer.get('.group2'), function(index, value) {
				    		var group = this;
				    		var groupNumber = group.getId().substring(5, this.getId().length);
				    		jQuery.each(value.getChildren(), function(index, value2) {
				    			if(value2.getStroke() == "red"){
				    				deleteLayerChildren(groupNumber);
				    				group.remove();
								}
				    		});
						});*/
				    	
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
							
							/*jQuery.each(polygonLayer.get('.polygon2'), function(index, value) {
								value.setStroke("black");
							});*/
							
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
				
				

				
			      
			  	//------------------ START GROUP 1
				
				
				
				
				
				/*
			      var polygon1Tab1 = new Kinetic.RegularPolygon({
				        x: 40,
				        y: 50,
				        radius: 27,
				        sides: 4,
				        stroke: 'black',
				        strokeWidth: 4,
				        fillPatternImage: imgTab1,
				        fillPatternOffset: [7, 7],
				        fillPatternRepeat: 'no-repeat',
				        fillPatternScale: 1.8,
				        fillPatternRotationDeg: 315,
				        draggable: false
				  });
			      polygon1Tab1.rotateDeg(45);
			      
			      var polygon1Tab1Image = polygon1Tab1.toDataURL({
			    	  width: 75,
			    	  height: 75
			      });
			      
			      polygon1Tab1.setStroke('red');
			      var polygon1Tab1ImageRed = polygon1Tab1.toDataURL({
			    	  width: 75,
			    	  height: 75
			      });
			      polygon1Tab1.setStroke('black');
			      
			      var polygon1Tab1Fake = new Kinetic.RegularPolygon({
				        x: 40,
				        y: 50,
				        opacity: 0,
				        radius: 27,
				        sides: 4,
				        stroke: 'black',
				        strokeWidth: 4,
				        fillPatternImage: imgTab1,
				        fillPatternOffset: [7, 7],
				        fillPatternRepeat: 'no-repeat',
				        fillPatternScale: 1.8,
				        fillPatternRotationDeg: 315,
				        draggable: true
				  });
			      polygon1Tab1Fake.rotateDeg(45);
			      
				var polygon1 = polygon1Tab1.clone({
					name: 'polygon1',
					draggable: false
			    });
				
				polygon1.on('click', function(e) {
					
					deselectOnClick(this, e);
					
				});
				
				polygon1.on('mousedown', function(e) {
					
					deselectOnClick(this, e);
					
				});
				
				polygon1Tab1Fake.on('dragstart', function() {
					
					jQuery('#body').css('cursor','url('+ polygon1Tab1Image +') 30 30,default');
					
					this.remove();
					layerTab1.add(polygon1Tab1Fake.clone());
					layerTab1.draw();
					
				});
				
				//var countObj = 0;
				
				positionX = 0;
				positionY = 0;
				
				polygon1Tab1Fake.on('dragend', function() {
			    	
					try {
						
						document.body.style.cursor = 'default';
						
						var mousePos = stageTab1.getMousePosition();
						if(mousePos.x > 0 || mousePos.y > 0){
							this.remove();
							layerTab1.add(polygon1Tab1Fake.clone());
							layerTab1.draw();
						}
			          
					} catch(e) {
						
						try {
						
							document.body.style.cursor = 'default';
							
							deselectAll();
							polygon1.setStroke("red");
							
							var mousePosStage = stage.getMousePosition();
							
							countObj++;
							var group1 = new Kinetic.Group({
								x: mousePosStage.x -30,
						        y: mousePosStage.y -30,
				        		  draggable: true,
				        		  id: "group" + countObj,
				        		  name: 'group1',
				        		  dragBoundFunc: function(pos) {
				        			  return rulesDragAndDropObj(pos, 80, 80);
				        		  }
				        	  });
							
							circle1.setId("circle" + countObj + "-1");
							circle1.setName("circle" + countObj);
							circle1.setPosition(40,50);
							group1.add(circle1.clone());
							group1.add(polygon1.clone());
							

							
							group1.on('mouseenter', function(e) {
								
								positionX = this.getX()+40;
								positionY = this.getY()+50;
								
							});
							
							group1.on('dragstart dragmove', function(e) {
								
								rectSelect.remove();
								
								dragAndDropGroup(this, e);
								changePositionArrow(this);
								
							});
							
							group1.on('dragend', function(e) {
								
								group1.setDragBoundFunc(function(pos){ return rulesDragAndDropObj(pos, 80, 80) });
								makeHistory();
							});
							
							group1.on('dblclick', function(e) {
								
								openModal();
								
							});
							
							
							
						  polygonLayer.add(group1);
						  
			        	  stage.draw();
						
			        	  this.remove();
				    	  layerTab1.add(polygon1Tab1Fake.clone());
				    	  layerTab1.draw();
				    	  
				    	  makeHistory();
				    	  
						} catch(e) {
							
							document.body.style.cursor = 'default';
							this.remove();
					    	layerTab1.add(polygon1Tab1Fake.clone());
					    	layerTab1.draw();
							
						}
						
					}
			          
			    });
				
			      
			    polygon1Tab1Fake.on('mouseup', function() {
			    	this.remove();
			    	layerTab1.add(polygon1Tab1Fake.clone());
			    	layerTab1.draw();
			    });
			      */
			      
					
				//------------------ END GROUP 1
			      
			    
			    
			    
			    //------------------ START GROUP 2
			
				  /*
			    var circle2 = new Kinetic.Circle({
					x: 100,
			        y: 50,
			        radius: 42,
			        //fill: 'white',
			        //stroke: 'black',
			        //strokeWidth: 1,
			        draggable: false
			    });
				
				circle2.on('mouseover', function() {
					document.body.style.cursor = 'pointer';
				});
				
				circle2.on('mouseout', function() {
					document.body.style.cursor = 'default';
				});
				
				  down = false;
				  
				  circle2.on("click", function(e) {
					  
					  var circle = this;
					  
					 if (down) {
						  down = false;
			    		  
			    		  arrow.setName(arrow.getName()+"-"+this.getName().substring(6, this.getName().length));
			    		  
			    		  var polygonGroup = polygonLayer.get('#'+this.getParent().getId());
			    		  
			    		  var newPoint = getCircleLineIntersectionPoint(arrow.getPoints()[0].x, arrow.getPoints()[0].y, polygonGroup[0].getX()+100, polygonGroup[0].getY()+50, polygonGroup[0].getX()+100, polygonGroup[0].getY()+50, 47);
			    		  var newPoint2 = getCircleLineIntersectionPoint(arrow.getPoints()[0].x, arrow.getPoints()[0].y, polygonGroup[0].getX()+100, polygonGroup[0].getY()+50, polygonGroup[0].getX()+100, polygonGroup[0].getY()+50, 60);
			    		  
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
			    		  
			    		  	//remove the arrows that are outside the standard
							deleteArrowOutsideStandard();
			    		  
			    		  circle.setFill('white');
			    		  
			    		  layer.draw();
			    		  polygonLayer.draw();
			    		  
			    		  makeHistory();
			    		  
			    	  }else{
			    		  down = true;
				          
				          var polygonGroup = polygonLayer.get('#'+this.getParent().getId());
				          arrow.setPoints([polygonGroup[0].getX()+100, polygonGroup[0].getY()+50, polygonGroup[0].getX()+100+1, polygonGroup[0].getY()+50+1 ]);
				          arrow.setName("arrow"+this.getName().substring(6,this.getName().length));
				          
				          circle.setFill('white');
				          
				          layer.add(arrow.clone());
				          
				          layer.draw();
				          polygonLayer.draw();
				          
			    	  }
					 
			      });
			      
			      var polygon2Tab1 = new Kinetic.RegularPolygon({
				        x: 100,
				        y: 50,
				        radius: 27,
				        sides: 5,
				        stroke: 'black',
				        strokeWidth: 4,
				        fillPatternImage: imgTab1,
				        fillPatternOffset: [7, 7],
				        fillPatternRepeat: 'no-repeat',
				        fillPatternScale: 1.8
				  });
			      
			      var polygon2Tab1Image = polygon2Tab1.toDataURL({
			    	  width: 127,
			    	  height: 127
			      });
			      
			      
			      polygon2Tab1.setStroke('red');
			      var polygon2Tab1ImageRed = polygon2Tab1.toDataURL({
			    	  width: 127,
			    	  height: 127
			      });
			      polygon2Tab1.setStroke('black');
			      
			      
			      var polygon2Tab1Fake = new Kinetic.RegularPolygon({
				        x: 100,
				        y: 50,
				        opacity: 0,
				        radius: 27,
				        sides: 5,
				        stroke: 'black',
				        strokeWidth: 4,
				        fillPatternImage: imgTab1,
				        fillPatternOffset: [7, 7],
				        fillPatternRepeat: 'no-repeat',
				        fillPatternScale: 1.8,
				        draggable: true
				  });
			      
			      var polygon2 = polygon2Tab1.clone({
						name: 'polygon2',
						draggable: false
				    });
			      
			      polygon2.on('click', function(e) {
			    	  
			    	  deselectOnClick(this, e);
			    	  
			      });
					
			      polygon2.on('mousedown', function(e) {
			    	  
			    	  deselectOnClick(this, e);
						
			      });
					
					polygon2Tab1Fake.on('dragstart', function() {
						
						jQuery('#body').css('cursor','url('+ polygon2Tab1Image +') 80 50,default');
						
						this.remove();
						layerTab1.add(polygon2Tab1Fake.clone());
						layerTab1.draw();
						
					});
					
					positionX = 0;
					positionY = 0;
					
					polygon2Tab1Fake.on('dragend', function() {
				    	
						try {
							
							document.body.style.cursor = 'default';
							
							var mousePos = stageTab1.getMousePosition();
							if(mousePos.x > 0 || mousePos.y > 0){
								this.remove();
								layerTab1.add(polygon2Tab1Fake.clone());
								layerTab1.draw();
							}
				          
						} catch(e) {
							
							try {
							
								document.body.style.cursor = 'default';
								
								deselectAll();
								polygon2.setStroke("red");
								
								var mousePosStage = stage.getMousePosition();
								
								countObj++;
								var group2 = new Kinetic.Group({
									x: mousePosStage.x -80,
							        y: mousePosStage.y -50,
					        		  draggable: true,
					        		  id: "group" + countObj,
					        		  name: 'group2',
					        		  dragBoundFunc: function(pos) {
					        			  return rulesDragAndDropObj(pos, 140, 100);
					        		  }
					        	});
								
								circle2.setId("circle" + countObj + "-2");
								circle2.setName("circle" + countObj);
								circle2.setPosition(100,50);
								group2.add(circle2.clone());
								group2.add(polygon2.clone());
								
								
								group2.on('mouseenter', function(e) {
									
									positionX = this.getX()+100;
									positionY = this.getY()+50;
									
								});
								
								group2.on('dragstart dragmove', function(e) {
									
									rectSelect.remove();
									
									dragAndDropGroup(this, e);
									changePositionArrow2(this);
									
								});
								
								group2.on('dragend', function(e) {
									
									group2.setDragBoundFunc(function(pos){ return rulesDragAndDropObj(pos, 140, 100) });
									makeHistory();
								});
								
							  
							  polygonLayer.add(group2);
							  
				        	  stage.draw();
							
				        	  this.remove();
					    	  layerTab1.add(polygon2Tab1Fake.clone());
					    	  layerTab1.draw();
					    	  
					    	  makeHistory();
					    	  
							} catch(e) {
								
								document.body.style.cursor = 'default';
								this.remove();
						    	layerTab1.add(polygon2Tab1Fake.clone());
						    	layerTab1.draw();
								
							}
							
						}
				          
				    });
					
				      
				    polygon2Tab1Fake.on('mouseup', function() {
				    	this.remove();
				    	layerTab1.add(polygon2Tab1Fake.clone());
				    	layerTab1.draw();
				    });
					
					
					
					*/
			      
			      
			      
					
					
					//------------------ END GROUP 2
					
					
			  /*    
			      
			      layerTab1.add(polygon1Tab1);
			      layerTab1.add(polygon2Tab1);
			      
			      layerTab1.add(polygon1Tab1Fake.clone());
			      layerTab1.add(polygon2Tab1Fake.clone());
			      
			      stageTab1.add(layerTab1);*/
			      
			      
			      
			    //------------ END TAB 1
			      
			      
			      
			      	//------------ TAB 2
					//------------ END TAB 2
			      
			      
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
  			  
  			 /* jQuery.each(polygonLayer.get('.group2'), function(index, value) {
  				if(collidesObj(rectSelect, value, value.getX()+100, value.getY()+50)){
  					value.getChildren()[1].setStroke("red");
  					polygonLayer.draw();
  					select = true;
  				}else{
  					if(!e.ctrlKey && !dragDropGroup){
  						value.getChildren()[1].setStroke("black");
  						polygonLayer.draw();
  					}
  				}
  			  });*/
  			  
  			  return select;
          }
		  
		  function deselectOnClick(obj, e){
	  			//<![CDATA[
	  				
	  				if(!e.ctrlKey && !dragDropGroup){
	  					jQuery.each(polygonLayer.get('.polygon1'), function(index, value) {
	  						value.setStroke("black");
	  					});
	  					
	  					/*jQuery.each(polygonLayer.get('.polygon2'), function(index, value) {
	  						value.setStroke("black");
	  					});*/
	  					
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
					
				/*	jQuery.each(polygonLayer.get('.group2'), function(index, value) {
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
									changePositionArrow2(value);
								}
							}
						}
					});
					*/
					
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
			  
		/*	  jQuery.each(polygonLayer.get('.polygon2'), function(index, value) {
				  value.setStroke("red");
				  polygonLayer.draw();
			  });*/
			  
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
			  
			/*  jQuery.each(polygonLayer.get('.polygon2'), function(index, value) {
				  value.setStroke("black");
				  polygonLayer.draw();
			  });*/
			  
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
			  
			/*  jQuery.each(polygonLayer.get('.group2'), function(index, value) {
				  var group = this;
				  var groupNumber = group.getId().substring(5, this.getId().length);
				  jQuery.each(value.getChildren(), function(index, value2) {
					  if(value2.getStroke() == "red"){
						  deleteLayerChildren(groupNumber);
						  group.remove();
					  }
				  });
			  });*/
			  
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
		  
		  
		/*  function changePositionArrow2(obj){
			  
			var group = obj;
			var groupNumber = obj.getId().substring(5, obj.getId().length);
				
				jQuery.each(layer.getChildren(), function(index, value) {
					if(value.getName() !== undefined){
						//alert(value.getName());
						var regex = new RegExp("^arrow"+groupNumber+"-([0-9]+)");
			    		if(regex.test(value.getName())){
			    			//alert(value.getName());
			    			value.getPoints()[0].x = group.getX()+100;
							value.getPoints()[0].y = group.getY()+50;
							
							var idGroup = value.getName().split("-");
							var g = polygonLayer.get('#group'+idGroup[1]);
							
							var newPoint;
							var newPoint2;
							var angle;
							
							if(g[0] !== undefined){
							
								var idCircle = g[0].getChildren()[0].getId().split("-");
								if(idCircle[1] == "2"){
									newPoint = getCircleLineIntersectionPoint(group.getX()+100, group.getY()+50, g[0].getX()+100, g[0].getY()+50, g[0].getX()+100, g[0].getY()+50, 47);
									newPoint2 = getCircleLineIntersectionPoint(group.getX()+100, group.getY()+50, g[0].getX()+100, g[0].getY()+50, g[0].getX()+100, g[0].getY()+50, 60);
									angle = Math.atan2(newPoint[1]-group.getY()-50, newPoint[0]-group.getX()-100);
								} else if(idCircle[1] == "1"){
									newPoint = getCircleLineIntersectionPoint(group.getX()+100, group.getY()+50, g[0].getX()+40, g[0].getY()+50, g[0].getX()+40, g[0].getY()+50, 47);
									newPoint2 = getCircleLineIntersectionPoint(group.getX()+100, group.getY()+50, g[0].getX()+40, g[0].getY()+50, g[0].getX()+40, g[0].getY()+50, 60);
									angle = Math.atan2(newPoint[1]-group.getY()-50, newPoint[0]-group.getX()-100);
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
			    			
			    			var newPoint = getCircleLineIntersectionPoint(value2.getPoints()[0].x, value2.getPoints()[0].y, group.getX()+100, group.getY()+50, group.getX()+100, group.getY()+50, 47);
			    			var newPoint2 = getCircleLineIntersectionPoint(value2.getPoints()[0].x, value2.getPoints()[0].y, group.getX()+100, group.getY()+50, group.getX()+100, group.getY()+50, 60);
			    			
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
*/
		  
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
			    //console.log(json2);
			    
			    var json = polygonLayer.toJSON();
			    json = json.replace('"children":"', '"children":');
			    json = json.substring(0, json.length-2) + json.substring(json.length-1, json.length);
			    json = json.replace(/\\/g, "");
			    history.push(json);
			    //console.log(json);
			    
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

			function redoHistory() {
				
			    if (historyStep < history.length-1) {
			        historyStep++;
			        
			        var json = history[historyStep];
			        var json2 = history2[historyStep];
			        
			        polygonLayer = Kinetic.Node.create(json, 'canvas');
			        layer = Kinetic.Node.create(json2, 'canvas');
			        
			        ready(layer, polygonLayer);
			    }
			}
			
			function updateArrow(group, countObj) {
				var groupNumber = group.getId().substring(5, group.getId().length);
				jQuery.each(layer.getChildren(), function(index, value) {
					if(value.getName() !== undefined){
						
						var regex = new RegExp("^arrow"+groupNumber+"-([0-9]+)");
			    		if(regex.test(value.getName())){
			    			var number = value.getName().split("-")[1];
			    			value.setName("arrow" + countObj + "-" + number);
			    		}
			    		
			    		var regex2 = new RegExp("^arrow([0-9]+)-"+groupNumber);
			    		if(regex2.test(value.getName())){
			    			value.setName(value.getName().split("-")[0] +"-"+ countObj);
			    		}
			    		
					}
				});
			}
			
			
		   function ready(layer, polygonLayer) {
			   
			   	//variable to control image of the object
			    var imgTab1 = new Image();
			    imgTab1.src = '../image/icons/button-cut.png';
		    	  
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
			    
			      countNumberObj = polygonLayer.get('.group1').length;
			      countNumberObj = jQuery("#countObj").val();
			      
			    jQuery.each(polygonLayer.get('.group1'), function(index, value) {
			    	var group = this;
					
			    	var circle1 = value.getChildren()[0];
			    	var polygon1 = value.getChildren()[1];
			    	
			    	
			    	countNumberObj++;
			    	
			    	updateArrow(group, countNumberObj);
			    	
			    	group.setId("group" + countNumberObj);
			    	circle1.setId("circle" + countNumberObj + "-1");
					circle1.setName("circle" + countNumberObj);
			    	
					jQuery("#countObj").val(countNumberObj);
			    	
			    	polygon1.setFillPatternImage(imgTab1);
			    	group.setDragBoundFunc(function(pos){ return rulesDragAndDropObj(pos, 80, 80); });
			    	
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
				    		  
				    		  layer.add(arrow.clone());
				    		  
				    		  	//remove the arrows that are outside the standard
								deleteArrowOutsideStandard();
				    		  
				    		  circle.setFill('white');
				    		  
				    		  layer.draw();
				    		  polygonLayer.draw();
				    		  
				    	  }else{
				    		  down = true;
					          
				    		  //alert(this.getParent().getId());
				    		  
					          var polygonGroup = polygonLayer.get('#'+this.getParent().getId());
					          arrow.setPoints([polygonGroup[0].getX()+40, polygonGroup[0].getY()+50, polygonGroup[0].getX()+40+1, polygonGroup[0].getY()+50+1 ]);
					          arrow.setName("arrow"+this.getName().substring(6,this.getName().length));
					          
					          circle.setFill('white');
					          
					          layer.add(arrow.clone());
					          
					          layer.draw();
					          polygonLayer.draw();
					          
				    	  }
						 
				      });
					  
					  polygon1.on('click', function(e) {
							deselectOnClick(this, e);
					  });
					  
					  polygon1.on('mousedown', function(e) {
						  deselectOnClick(this, e);
					  });
				      
			    	
			    	polygonLayer.draw();
			    });
			    
			    
			    
			    
		/*	    
			    jQuery.each(polygonLayer.get('.group2'), function(index, value) {
			    	var group = this;
					
			    	var circle2 = value.getChildren()[0];
			    	var polygon2 = value.getChildren()[1];
			    	
			    	countObj++;
			    	
			    	updateArrow(group, countObj);
			    	
			    	group.setId("group" + countObj);
			    	circle2.setId("circle" + countObj + "-2");
					circle2.setName("circle" + countObj);
			    	
			    	
			    	polygon2.setFillPatternImage(imgTab1);
			    	group.setDragBoundFunc(function(pos){ return rulesDragAndDropObj(pos, 140, 100) });
			    	
			    	group.on('mouseenter', function(e) {
						positionX = this.getX()+100;
						positionY = this.getY()+50;
					});
					
					group.on('dragstart dragmove', function(e) {
						rectSelect.remove();
						dragAndDropGroup(this, e);
						changePositionArrow2(this);
					});
					
					group.on('dragend', function(e) {
						group.setDragBoundFunc(function(pos){ return rulesDragAndDropObj(pos, 140, 100) });
						makeHistory();
					});
					
					circle2.on('mouseover', function() {
						document.body.style.cursor = 'pointer';
					});
					
					circle2.on('mouseout', function() {
						document.body.style.cursor = 'default';
					});
					
					down = false;
					  
					  circle2.on("click", function(e) {
						  
						  var circle = this;
						  
						 if (down) {
							  down = false;
				    		  
				    		  arrow.setName(arrow.getName()+"-"+this.getName().substring(6, this.getName().length));
				    		  
				    		  var polygonGroup = polygonLayer.get('#'+this.getParent().getId());
				    		  
				    		  var newPoint = getCircleLineIntersectionPoint(arrow.getPoints()[0].x, arrow.getPoints()[0].y, polygonGroup[0].getX()+100, polygonGroup[0].getY()+50, polygonGroup[0].getX()+100, polygonGroup[0].getY()+50, 47);
				    		  var newPoint2 = getCircleLineIntersectionPoint(arrow.getPoints()[0].x, arrow.getPoints()[0].y, polygonGroup[0].getX()+100, polygonGroup[0].getY()+50, polygonGroup[0].getX()+100, polygonGroup[0].getY()+50, 60);
				    		  
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
				    		  
				    		  	//remove the arrows that are outside the standard
								deleteArrowOutsideStandard();
				    		  
				    		  circle.setFill('white');
				    		  
				    		  layer.draw();
				    		  polygonLayer.draw();
				    		  
				    	  }else{
				    		  down = true;
					          
					          var polygonGroup = polygonLayer.get('#'+this.getParent().getId());
					          arrow.setPoints([polygonGroup[0].getX()+100, polygonGroup[0].getY()+50, polygonGroup[0].getX()+100+1, polygonGroup[0].getY()+50+1 ]);
					          arrow.setName("arrow"+this.getName().substring(6,this.getName().length));
					          
					          circle.setFill('white');
					          
					          layer.add(arrow.clone());
					          
					          layer.draw();
					          polygonLayer.draw();
					          
				    	  }
						 
				      });
					  
					  polygon2.on('click', function(e) {
							deselectOnClick(this, e);
					  });
					  
					  polygon2.on('mousedown', function(e) {
						  deselectOnClick(this, e);
					  });
				      
					  
			    	polygonLayer.draw();
			    });*/
			    
		   }
		   
		   function openModal() {
			   
			   jQuery("[id$='linkCanvasModalPanel']").click();
			   
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
			    		  
			    		  layer.add(arrow.clone());
			    		  
			    		  //alert(arrow.getName());
			    		  
			    		  	//remove the arrows that are outside the standard
							deleteArrowOutsideStandard();
			    		  
			    		  circle.setFill('white');
			    		  
			    		  layer.draw();
			    		  polygonLayer.draw();
			    		  
			    		  makeHistory();
			    		  
			    	  }else{
			    		  down = true;
				          
				          //alert(this.getParent().getId());
				          
				          var polygonGroup = polygonLayer.get('#'+this.getParent().getId());
				          arrow.setPoints([polygonGroup[0].getX()+40, polygonGroup[0].getY()+50, polygonGroup[0].getX()+40+1, polygonGroup[0].getY()+50+1 ]);
				          arrow.setName("arrow"+this.getName().substring(6,this.getName().length));
				          
				          circle.setFill('white');
				          
				          layer.add(arrow.clone());
				          
				          layer.draw();
				          polygonLayer.draw();
				          
			    	  }
					 
			      });
			   
			   
		   
			   var posInitX = 40;
			   var poxInitY = 50;
			   
			   //for list divs
			   jQuery( "#tabsFooter ul:first li" ).each(function(index) {
				   
				   
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
					    layerTab = new Kinetic.Layer();
					    
					    
					    //var countObj = 0;
					    
					    //for list of obj imagens
				    	jQuery( "#"+nameDiv ).find("img").each(function(index) {
							
				    		
						    //variable to control image of the object
						    var imgTab = new Image();
						    imgTab.src = jQuery(this).attr("src");
						      
						    
						    
						  	//------------------ START GROUP 
							
						    
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
						      var polygonTabImageRed = polygonTab.toDataURL({
						    	  width: 75,
						    	  height: 75
						      });
						      polygonTab.setStroke('black');
						      
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
									
										document.body.style.cursor = 'default';
										
										deselectAll();
										polygon.setStroke("red");
										
										var mousePosStage = stage.getMousePosition();
										
										countObj++;
										var group1 = new Kinetic.Group({
											x: mousePosStage.x -30,
									        y: mousePosStage.y -30,
							        		  draggable: true,
							        		  id: "group" + countObj,
							        		  name: 'group1',
							        		  dragBoundFunc: function(pos) {
							        			  return rulesDragAndDropObj(pos, 80, 80);
							        		  }
							        	  });
										
										var circ = circle1.clone();
										var poly = polygon.clone();
										circ.setId("circle" + countObj + "-1");
										circ.setName("circle" + countObj);
										circ.setPosition(40,50);
										poly.setPosition(40,50);
										group1.add(circ);
										group1.add(poly);
										
										group1.on('mouseenter', function(e) {
											
											positionX = this.getX()+40;
											positionY = this.getY()+50;
											
										});
										
										group1.on('dragstart dragmove', function(e) {
											
											rectSelect.remove();
											
											dragAndDropGroup(this, e);
											changePositionArrow(this);
											
										});
										
										group1.on('dragend', function(e) {
											
											group1.setDragBoundFunc(function(pos){ return rulesDragAndDropObj(pos, 80, 80); });
											makeHistory();
										});
										
										group1.on('dblclick', function(e) {
											
											openModal();
											
										});
										
									  polygonLayer.add(group1);
									  
						        	  stage.draw();
									
						        	  this.remove();
							    	  layerTab.add(polygonTabFake.clone());
							    	  layerTab.draw();
							    	  
							    	  makeHistory();
							    	  
									} catch(e) {
										
										document.body.style.cursor = 'default';
										this.remove();
								    	layerTab.add(polygonTabFake.clone());
								    	layerTab.draw();
										
									}
									
								}
						          
						    });
							
						      
						    polygonTabFake.on('mouseup', function() {
						    	this.remove();
						    	layerTab.add(polygonTabFake.clone());
						    	layerTab.draw();
						    });
						      
							//------------------ END GROUP 
				    
						    layerTab.add(polygonTab);
						    layerTab.add(polygonTabFake.clone());
						    
						    jQuery( "#"+nameDiv ).find("img").remove();
						    
					    });
					    //END for obj imagens
					    
				    	 
					     stageTab.add(layerTab);
				    
				   	}// END IF
				     
			   });
			   //END for divs
			   
			   
		   }
		  