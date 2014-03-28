function Canvas(name){
	this.name=name;
	this.history = [];
	this.history2 = [];
	this.historyStep = -1;
	
	this.rectSelect = null;
	this.arrow = null;
	
	this.countObj = 0;
	this.positionX = 0;
	this.positionY = 0;
	
	// control of the rectangle to select objects on the screen
	this.moving = false;

	// checks if something was selected on method:rectSelectAllObj
	this.select = false;

	// checks if an arrow was clicked on metohd:arrow.'click'
	this.clickArrow = false;

	// checks if a drag and drop of a group
	this.dragDropGroup = false;

	this.down = false;
	
	this.canvasContainer = null;
	this.legendCanvasContainer = null;
	
	this.stage = null;
	this.layer = null;
	this.polygonLayer = null;
	
	
	this.running = false;
	this.saved = false;
	this.pathFile = null;
	
	this.oldIdSelected = null;
	
	this.legendStage = null;
	this.legendLayer = null;
	this.legend = null;
	this.legendWidth = 170;
	this.legendHidden = false;
	this.outputTypeColours = [];
}

var selectedCanvas = "canvas-1";

var canvasArray;
var allPositionIcons;
var refreshProcManagerCount = 30;

var imgHeight;
var imgWidth;

function findHHandWW() {
	imgHeight = this.height;
	imgWidth = this.width;
	return true;
}

window.onload = function() {
	var canvasName = "canvas-1";
	
	canvasArray = {};
	
	mountObj(canvasName);
	configureCanvas(canvasName);
};

function configureCanvas(canvasName){
	
	canvasArray[canvasName] = new Canvas(canvasName);
	
//	canvasArray[canvasName].outputTypeColours['hive'] = 'green';
//	canvasArray[canvasName].outputTypeColours['hdfs'] = 'red';
	
	var canvasContainer = "container-"+canvasName;
	var legendCanvasContainer = "container-legend-"+canvasName;
	canvasArray[canvasName].canvasContainer = canvasContainer;
	canvasArray[canvasName].legendCanvasContainer = legendCanvasContainer;

	// main stage
	var stage = new Kinetic.Stage({
		container : canvasContainer,
		width : 800,
		height : 600
	});
	canvasArray[canvasName].stage = stage;
	
	// stage for the legend
	var legendStage = new Kinetic.Stage({
		container : legendCanvasContainer,
		width : 170,
		height : 400
	});
	canvasArray[canvasName].legendStage = legendStage;

	// layer to the arrows
	var layer = new Kinetic.Layer();
	canvasArray[canvasName].layer = layer;

	// layer to polygons
	var polygonLayer = new Kinetic.Layer();
	canvasArray[canvasName].polygonLayer = polygonLayer;
	
	// layer to legend
	var legendLayer = new Kinetic.Layer();
	canvasArray[canvasName].legendLayer = legendLayer;

	// set width of the canvas
	jQuery("#"+canvasContainer).css("width", jQuery(canvasName).width() + 'px');
	
	// white background
	var background = new Kinetic.Rect({
		x : 0,
		y : 0,
		//fill : "white",
		width : stage.getWidth(),
		height : stage.getHeight()
	});
	
	// puts a different colour on the canvas before it is opened
	jQuery("#"+canvasContainer).css("background-color", "#FFFAFA");
	jQuery(".kineticjs-content").css("background-color", "white");
	jQuery(".kineticjs-content").css("background-image", "url('../image/canvas_squirl.png')");
	jQuery(".kineticjs-content").css("background-size", "920px");
	//jQuery(".kineticjs-content").css("background-repeat", "no-repeat");
	
	//remove image from footer
	jQuery("#tabsFooter ul:first li").each(function(index) {
		var nameDiv = jQuery(this).attr("aria-controls");
		if (nameDiv != undefined) {
			jQuery("#" +  nameDiv).find(".kineticjs-content").css("background-image", "none");
		}
	});
	
	canvasArray[canvasName].background = background;

	// add the background on layer
	layer.add(background);
	

	// dotted rectangle to select objects
	canvasArray[canvasName].rectSelect = new Kinetic.Rect({
		x : 0,
		y : 0,
		width : 0,
		height : 0,
		// fill: 'red',
		stroke : 'black',
		strokeWidth : 1,
		opacity : 0.5,
		dashArray : [ 33, 10 ]
	});

	canvasArray[canvasName].arrow = new Kinetic.Line({
		// name: 'arrow',
		// dashArray: [33, 10],
		strokeWidth : 4,
		stroke : "black",
		draggable : false
	});

	configureStage(canvasName);

	makeHistory(canvasName);

	jQuery("#body").keydown(function(event) {
		if (event.keyCode == 46) { // Delete
			deleteSelected();
		}
	});

	canvasArray[canvasName].arrow.on('click', function(e) {

		if (!e.ctrlKey) {
			jQuery.each(layer.getChildren(), function(index, value) {
				if (value.isArrow == true) {
					value.setStroke(value.originalColor);
					value.selected = false;
				}
			});
		}

		this.setStroke("red");
		this.selected = true;
		canvasArray[canvasName].clickArrow = true;
		stage.draw();
	});

	jQuery("#"+canvasContainer).click(
		function(e) {

			// remove the arrows that are outside the standard
			deleteArrowOutsideStandard(canvasName);

			if (!e.ctrlKey) {

				// checks if something was selected on
				// method:rectSelectAllObj
				if (!canvasArray[canvasName].select) {

					jQuery.each(polygonLayer.get('.polygon1'), function(index, value) {
						value.setStroke('black');
						value.selected = false;
					});

					// checks if an arrow was clicked on
					// metohd:arrow.'click'
					if (!canvasArray[canvasName].clickArrow) {
						jQuery.each(layer.getChildren(), function(index, value) {
							if (value.isArrow == true) {
								value.setStroke(value.originalColor);
								value.selected = false;
								layer.draw();
							}
						});
					}
					canvasArray[canvasName].clickArrow = false;
				}
			}

			canvasArray[canvasName].down = false;
			canvasArray[canvasName].moving = false;
			canvasArray[canvasName].select = rectSelectAllObj(canvasName, e);
			canvasArray[canvasName].rectSelect.remove();

			layer.draw();
			polygonLayer.draw();
		}
	);
	
	createLegend(canvasName);
	
	stage.add(layer);
	stage.add(polygonLayer);
	
	legendStage.add(legendLayer);
}

function createLegend(canvasName) {
	
	var posX = 0;
	var posY = 10;
	var width = canvasArray[canvasName].legendWidth;

	var legendLayer = canvasArray[canvasName].legendLayer;
	var legendStage = canvasArray[canvasName].legendStage;

	var linkTypeColours = canvasArray[canvasName].outputTypeColours;

	var outputTypeColours = [['RECORDED',getColorOutputType('RECORDED')],
	    	                 ['BUFFERED',getColorOutputType('BUFFERED')],
	    	                 ['TEMPORARY',getColorOutputType('TEMPORARY')]];

	var outputExistenceColours = [
	    	                 ['At least one dataset exists',getColorOutputExistence('true')],
	    	                 ['No Dataset exist',getColorOutputExistence('false')]];

	var runningStatusColours = [
	    	                 ['OK',getColorRunning('OK')],
	    	                 ['ERROR',getColorRunning('ERROR')]];
		
	var arcColoursArray = {};
		arcColoursArray['Running Status'] = runningStatusColours;
		arcColoursArray['Output Dataset'] = outputExistenceColours;
		arcColoursArray['Output Type'] = outputTypeColours;

	var arcColoursArrayLength = outputTypeColours.length + outputExistenceColours.length + runningStatusColours.length + linkTypeColours.length + 4;
	
	var groupLegend = new Kinetic.Group({
		draggable : false,
		id : "legend",
		dragBoundFunc : function(pos) {
			return rulesDragAndDropObj(canvasName, pos, 80, 80);
		}
	});
		
	groupLegend.on('dragstart dragmove', function(e) {
		canvasArray[canvasName].rectSelect.remove();
	});
		
	
	var contPosition = 0;
	
	var labelTitle = new Kinetic.Text({
		text : 'Arc',
		fontSize : 14,
		fill : 'black',
        fontStyle : 'bold',
        x : posX + 25,
		y : posY + 20*contPosition
	});
	
	groupLegend.add(labelTitle);
	++contPosition;
	
	var rec = new Kinetic.Rect({
		x : posX + 10,
		y : posY + 20*contPosition,
		width : 10,
		height : 10,
		stroke : getColorOutputType('UNDEFINED'),
		fill: getColorOutputType('UNDEFINED'),
		strokeWidth : 1,
		draggable : false
	});
		
	var labelRec = new Kinetic.Text({
		text : 'Undefined',
		fontSize : 10,
		fill : 'black',
		x : posX + 25,
		y : posY + 20*contPosition
	});
	groupLegend.add(rec);
	groupLegend.add(labelRec);
	++contPosition;
	
	for (var v in arcColoursArray){
			
	    var array = arcColoursArray[v];
	    
	    if (array.length > 0){
	    	labelTitle = new Kinetic.Text({
				text : v,
				fontSize : 10,
				fill : 'black',
		        fontStyle : 'bold',
		        x : posX + 25,
				y : posY + 20*contPosition
			});
	    	
			groupLegend.add(labelTitle);
			++contPosition;
	    
			for (var i = 0; i < array.length; i++) {
				rec = new Kinetic.Rect({
					x : posX + 10,
					y : posY + 20*contPosition,
					width : 10,
					height : 10,
					stroke : array[i][1],
					fill: array[i][1],
					strokeWidth : 1,
					draggable : false
				});
					
				labelRec = new Kinetic.Text({
					text : capitaliseFirstLetter(array[i][0]),
					fontSize : 10,
					fill : 'black',
					x : posX + 25,
					y : posY + 20*contPosition
				});
					
				groupLegend.add(rec);
				groupLegend.add(labelRec);
		        ++contPosition;
			}
	    }
	}
	

	if (linkTypeColours.length > 0){
		var labelTitle = new Kinetic.Text({
			text : 'Link',
			fontSize : 14,
			fill : 'black',
	        fontStyle : 'bold',
	        x : posX + 25,
			y : posY + 20*contPosition
		});
		
		groupLegend.add(labelTitle);
		++contPosition;
		
		    
		for (var i = 0; i < linkTypeColours.length; i++) {
			var rec = new Kinetic.Rect({
				x : posX + 10,
				y : posY + 20*contPosition,
				width : 10,
				height : 10,
				stroke : linkTypeColours[i][1],
				fill: linkTypeColours[i][1],
				strokeWidth : 1,
				draggable : false
			});
						
			var labelRec = new Kinetic.Text({
				text : capitaliseFirstLetter(linkTypeColours[i][0]),
				fontSize : 10,
				fill : 'black',
				x : posX + 25,
				y : posY + 20*contPosition
			});
						
			groupLegend.add(rec);
			groupLegend.add(labelRec);
		       ++contPosition;
		}
	}
	
	legendStage.setWidth(width+10);
	legendStage.setHeight((arcColoursArrayLength * 20) + 40);
	
	// set width of the canvas
	var legendCanvasContainer = canvasArray[canvasName].legendCanvasContainer;
	jQuery("#"+legendCanvasContainer).css("width", legendStage.getWidth()+ 20 + 'px');
	jQuery("#"+legendCanvasContainer).css("height", legendStage.getHeight()+ 30 + 'px');
	
	jQuery("#header-legend-"+canvasName).css("width", legendStage.getWidth()+ 20 + 'px');
	
	canvasArray[canvasName].legend = groupLegend;
	legendLayer.add(groupLegend);
	legendLayer.draw();
}

// a = retangle, b = object, bx = object.getX() and by = object.getY()
function collidesObj(a, b, bx, by) {

	if (a.getX() < bx + b.getWidth() && a.getX() + a.getWidth() > bx && a.getY() < by + b.getHeight() && a.getY() + a.getHeight() > by) {
		return true;
	} else if (a.getX() > bx + b.getWidth() && a.getX() + a.getWidth() < bx && a.getY() < by + b.getHeight() && a.getY() + a.getHeight() > by) {
		return true;
	} else if (a.getX() > bx + b.getWidth() && a.getX() + a.getWidth() < bx	&& a.getY() > by + b.getHeight() && a.getY() + a.getHeight() < by) {
		return true;
	}
	if (a.getX() < bx + b.getWidth() && a.getX() + a.getWidth() > bx && a.getY() > by + b.getHeight() && a.getY() + a.getHeight() < by) {
		return true;
	} else {
		return false;
	}

}

function rectSelectAllObj(canvasName, e) {
	
	var polygonLayer = canvasArray[canvasName].polygonLayer;

	canvasArray[canvasName].select = false;

	jQuery.each(polygonLayer.get('.group1'),
			function(index, value) {
				if (collidesObj(canvasArray[canvasName].rectSelect, value, value.getX() + 40, value
						.getY() + 50)) {
					value.getChildren()[2].setStroke("red");
					value.getChildren()[2].selected = true;
					polygonLayer.draw();
					canvasArray[canvasName].select = true;
				} else {
					if (!e.ctrlKey && !canvasArray[canvasName].dragDropGroup) {
						value.getChildren()[2].setStroke(value.getChildren()[1].originaColor);
						value.getChildren()[2].selected = false;
						polygonLayer.draw();
					}
				}
			});

	return canvasArray[canvasName].select;
}

function deselectOnClick(canvasName, obj, e) {
	// <![CDATA[
	
	var layer = canvasArray[canvasName].layer;
	var polygonLayer = canvasArray[canvasName].polygonLayer;
	var stage = canvasArray[canvasName].stage;

	if (!e.ctrlKey && !canvasArray[canvasName].dragDropGroup) {
		jQuery.each(polygonLayer.get('.polygon1'), function(index, value) {
			value.setStroke('black');
			value.selected = false;
		});

		jQuery.each(layer.getChildren(), function(index, value) {
			if (value.isArrow == true) {
				value.setStroke(value.originalColor);
				value.selected = false;
				layer.draw();
			}
		});
	}

	canvasArray[canvasName].dragDropGroup = false;

	obj.setStroke("red");
	obj.selected = true;

	stage.draw();

	// ]]>
}

function dragAndDropGroup(canvasName, obj, e) {
	
	var polygonLayer = canvasArray[canvasName].polygonLayer;
	var stage = canvasArray[canvasName].stage;
	var background = canvasArray[canvasName].background;
	
	canvasArray[canvasName].dragDropGroup = true;

	var group = obj;
	var newX = 0;
	var newY = 0;
	var differenceX = 0;
	var differenceY = 0;
	var positionX = canvasArray[canvasName].positionX;
	var positionY = canvasArray[canvasName].positionY;
	var mousePos = stage.getMousePosition();

	// if(e.ctrlKey){
	jQuery.each(polygonLayer.get('.group1'), function(index, value) {
		if (value.getId() != group.getId()) {
			if (value.getChildren()[2] !== undefined && value.getChildren()[2].selected == true) {
				if (mousePos !== undefined) {

					if (group.getX() > positionX) {
						differenceX = mousePos.x - positionX;
						newX = value.getX() + differenceX;
					} else if (group.getX() < positionX) {
						differenceX = positionX - mousePos.x;
						newX = value.getX() - differenceX;
					} else if (group.getX() == positionX) {
						newX = group.getX();
					}

					if (group.getY() > positionY) {
						differenceY = mousePos.y - positionY;
						newY = value.getY() + differenceY;
					} else if (group.getY() < positionY) {
						differenceY = positionY - mousePos.y;
						newY = value.getY() - differenceY;
					} else if (group.getY() == positionY) {
						newY = group.getY();
					}

					if (mousePos.x < 60) {
						newX = value.getX();
					}
					if (mousePos.y < 60) {
						newY = value.getY();
					}

					var xCanvas = stage.getWidth();
					var yCanvas = stage.getHeight();

					if (newX < 5) {
						newX = 5;
					} else if (newX + 80 > xCanvas) {
						stage.setWidth(xCanvas + 200);
						background.setWidth(stage.getWidth());
					}

					if (newY < 5) {
						newY = 5;
					} else if (newY + 80 > yCanvas) {
						stage.setHeight(yCanvas + 200);
						background.setHeight(stage.getHeight());
					}

					value.setPosition(newX, newY);
					changePositionArrow(canvasName, value);
				}
			}
		}
	});

	if (group.getX() > positionX) {
		canvasArray[canvasName].positionX = positionX + differenceX;
	} else if (group.getX() < positionX) {
		canvasArray[canvasName].positionX = positionX - differenceX;
	}

	if (group.getY() > positionY) {
		canvasArray[canvasName].positionY = positionY + differenceY;
	} else if (group.getY() < positionY) {
		canvasArray[canvasName].positionY = positionY - differenceY;
	}

	// }
}

function selectAll(canvasName) {
	
	var polygonLayer = canvasArray[canvasName].polygonLayer;
	var layer = canvasArray[canvasName].layer;
	
	jQuery.each(polygonLayer.get('.polygon1'), function(index, value) {
		value.setStroke("red");
		value.selected = true;
		polygonLayer.draw();
	});

	jQuery.each(layer.getChildren(), function(index, value) {
		if (value.isArrow == true) {
			value.setStroke("red");
			value.selected = true;
			layer.draw();
		}
	});

}

function deselectAll(canvasName) {
	
	var polygonLayer = canvasArray[canvasName].polygonLayer;
	var layer = canvasArray[canvasName].layer;

	jQuery.each(polygonLayer.get('.polygon1'), function(index, value) {
		value.setStroke('black');
		value.selected = false;
	});
	try{
		polygonLayer.draw();
	}catch(exception){
		//alert(exception);
	}
	jQuery.each(layer.getChildren(), function(index, value) {
		if (value.isArrow == true) {
			value.setStroke(value.originalColor);
			value.selected = false;
		}
	});
	layer.draw();

}

function deleteSelected() {
	
	var polygonLayer = canvasArray[selectedCanvas].polygonLayer;
	var layer = canvasArray[selectedCanvas].layer;

	makeHistory(selectedCanvas);

	jQuery.each(polygonLayer.get('.group1'), function(index, value) {
		var group = this;
		jQuery.each(value.getChildren(), function(index, value2) {
			if (value2.selected) {
				removeElement(group.getId());
				deleteLayerChildren(selectedCanvas, group.getId());
				group.remove();
			}
		});
	});

	var listSize = layer.getChildren().size();
	for ( var i = 0; i < listSize; i++) {
		jQuery.each(layer.getChildren(), function(index, value) {
			if (value !== undefined && value.isArrow == true) {
				if (value.selected) {
					removeLinkBt(
						value.output,
						value.nameOutput,
						value.input,
						value.nameInput);
					if (value.label != null){
						value.label.remove();
					}
					value.remove();
					return false;
				}
			}
		});
	}
	layer.draw();
	polygonLayer.draw();

}

// remove the arrows that are outside the standard
function deleteArrowOutsideStandard(canvasName) {
	var layer = canvasArray[canvasName].layer;
	var listSize = layer.getChildren().size();
	for ( var i = 0; i < listSize; i++) {
		jQuery.each(layer.getChildren(), function(index, value) {
			if (value !== undefined) {
				if (value.isArrow && (value.idOutput == null || value.idInput == null)) {
					if (value.label != null){
						value.label.remove();
					}
					value.remove();
					return false;
				}
			}
		});
	}
}

function getCircleLineIntersectionPoint(pointAx, pointAy, pointBx, pointBy,
		circleX, circleY, radius) {

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

	return [ px, py ];
}

function deleteLayerChildren(canvasName, idGroup) {
	
	var layer = canvasArray[canvasName].layer;

	var listSize = layer.getChildren().size();
	for ( var i = 0; i < listSize; i++) {
		jQuery.each(layer.getChildren(),
			function(index, value) {
				if (value !== undefined && value.isArrow == true) {
					if (value.idOutput == idGroup || value.idInput == idGroup){
						if (value.label != null){
							value.label.remove();
						}
						value.remove();
						return false;
					}
				}
			});
	}
}

function changePositionArrow(canvasName, obj) {
	
	var polygonLayer = canvasArray[canvasName].polygonLayer;
	var layer = canvasArray[canvasName].layer;

	var group = obj;
	var idGroup = obj.getId();
	
	jQuery.each(layer.getChildren(), function(index, value) {
		if (value.isArrow == true) {
			if (value.idOutput == idGroup) {
				value.getPoints()[0].x = group.getX() + 40;
				value.getPoints()[0].y = group.getY() + 50;

				var g = getElement(polygonLayer, value.idInput);

				if (g !== undefined) {
					
					var newPoint = null;
					var newPoint2 = null;
					var angle = null;

					newPoint = getArrowPositions2(g, group, 47, 40);
					newPoint2 = getArrowPositions2(g, group, 60, 40);
					angle = getArrowAngle(newPoint, group);

					updatePositionArrow(value, newPoint, newPoint2, 20, 10, angle);
				}

			}
		}
	});

	jQuery.each(layer.getChildren(), function(index, value) {
		if (value.isArrow == true) {
			if (value.idInput == idGroup) {

				var newPoint = getArrowPositions(value, group, 47);
				var newPoint2 = getArrowPositions(value, group, 60);
				
				var angle = getArrowAngle2(newPoint, value);
				
				updatePositionArrow(value, newPoint, newPoint2, 20, 10, angle);
			}
		}
	});

	layer.draw();
	polygonLayer.draw();
}

function addLinks(canvasName, positions) {
	var linkArrays = JSON.parse(positions);

	for ( var i = 0; i < linkArrays.length; i++) {
		addLink(canvasName, linkArrays[i][0], linkArrays[i][1]);
	}
}

function addLink(canvasName, outId, inId) {
	// out
	var layer = canvasArray[canvasName].layer;
	var polygonLayer = canvasArray[canvasName].polygonLayer;
	var arrow = canvasArray[canvasName].arrow;
	
	var polygonGroupOut = getElement(polygonLayer, outId);
	arrow.setPoints([ polygonGroupOut.getX() + 40, polygonGroupOut.getY() + 50,
			polygonGroupOut.getX() + 40 + 1, polygonGroupOut.getY() + 50 + 1 ]);

	// in
	var polygonGroupIn = getElement(polygonLayer, inId);

	arrow.setName("arrow" + outId + "-" + inId);

	var newPoint = getArrowPositions(arrow, polygonGroupIn, 47);
	var newPoint2 = getArrowPositions(arrow, polygonGroupIn, 60);

	var angle = getArrowAngle2(newPoint, arrow);
	
	updatePositionArrow(arrow, newPoint, newPoint2, 20, 10, angle);

	var arrowClone = arrow.clone();
	arrowClone.idInput = inId;
	arrowClone.idOutput = outId;
	arrowClone.isArrow = true;
	layer.add(arrowClone);
	layer.draw();
	polygonLayer.draw();

	makeHistory(canvasName);
	
	return arrowClone;
}

function getArrowAngle(newPoint, group){
	return Math.atan2(newPoint[1] - group.getY() - 50,
			newPoint[0] - group.getX() - 40);
}

function getArrowAngle2(newPoint, value){
	return Math.atan2(newPoint[1] - value.getPoints()[0].y,
			newPoint[0] - value.getPoints()[0].x);
}

function getArrowPositions(value, group, position){
	return getCircleLineIntersectionPoint(value.getPoints()[0].x, 
			value.getPoints()[0].y, group.getX() + 40, group.
			getY() + 50, group.getX() + 40,
			group.getY() + 50, position);
}

function getArrowPositions2(g, group, position, offsetX){
	return getCircleLineIntersectionPoint(
			group.getX() + 40, group.getY() + 50, g
			.getX() + offsetX, g.getY() + 50, g
			.getX() + offsetX, g.getY() + 50, position);
}

function updatePositionArrow(arrow, newPoint, newPoint2, headlen, headlen2, angle){
	arrow.setPoints([ arrow.getPoints()[0].x, arrow.getPoints()[0].y,
		newPoint2[0], newPoint2[1], newPoint2[0], newPoint2[1],
		newPoint[0] - headlen * Math.cos(angle - Math.PI / 6),
		newPoint[1] - headlen * Math.sin(angle - Math.PI / 6), newPoint[0],
		newPoint[1], newPoint[0] - headlen * Math.cos(angle + Math.PI / 6),
		newPoint[1] - headlen * Math.sin(angle + Math.PI / 6),
		newPoint2[0], newPoint2[1],
		newPoint2[0] - headlen2 * Math.cos(angle - Math.PI / 3),
		newPoint2[1] - headlen2 * Math.sin(angle - Math.PI / 3),
		newPoint2[0], newPoint2[1],
		newPoint2[0] - headlen2 * Math.cos(angle + Math.PI / 3),
		newPoint2[1] - headlen2 * Math.sin(angle + Math.PI / 3) ]);
	
	if (arrow.label != null){
		var x1 = arrow.getPoints()[0].x
		var y1 = arrow.getPoints()[0].y
		
		var x2 = arrow.getPoints()[1].x
		var y2 = arrow.getPoints()[1].y
		
		arrow.label.setX((x1 + x2 + (Math.cos(angle) * 42))/2  - Math.abs((Math.cos(angle) * arrow.label.getText().length*2.7)));
		arrow.label.setY((y1 + y2 + (Math.sin(angle) * 42))/2);
	}
}

function rulesDragAndDropObj(canvasName, pos, valueX, valueY) {
	
	var stage = canvasArray[canvasName].stage;
	var background = canvasArray[canvasName].background;

	var xCanvas = stage.getWidth();
	var yCanvas = stage.getHeight();
	
	var newX;
	if (pos.x < 5) {
		newX = 5;
	} else if (pos.x + valueX > xCanvas) {
		newX = pos.x;
		stage.setWidth(xCanvas + 200);
		background.setWidth(stage.getWidth());
	} else {
		newX = pos.x;
	}

	var newY;
	if (pos.y < 5) {
		newY = 5;
	} else if (pos.y + valueY > yCanvas) {
		newY = pos.y;
		stage.setHeight(yCanvas + 200);
		background.setHeight(stage.getHeight());
	} else {
		newY = pos.y;
	}

	return {
		x : newX,
		y : newY
	};

}

function rulesDragAndDropGroupObj(canvasName, pos, valueX, valueY) {
	// <![CDATA[
	
	var stage = canvasArray[canvasName].stage;
	var background = canvasArray[canvasName].background;

	var xCanvas = stage.getWidth();
	var yCanvas = stage.getHeight();
	var newX = pos.x;
	var newY = pos.y;

	if (pos.x < 5) {
		newX = 5;
	} else if (valueX != null && pos.x < valueX) {
		newX = valueX;
	} else if (pos.x + 100 > xCanvas) {
		newX = pos.x;
		stage.setWidth(xCanvas + 200);
		background.setWidth(stage.getWidth());
	}

	if (pos.y < 5) {
		newY = 5;
	} else if (valueY != null && pos.y < valueY) {
		newY = valueY;
	} else if (pos.y + 100 > yCanvas) {
		newY = pos.y;
		stage.setHeight(yCanvas + 200);
		background.setHeight(stage.getHeight());
	}

	return {
		x : newX,
		y : newY
	};

	// ]]>
}

function makeHistory(canvasName) {
	
	var polygonLayer = canvasArray[canvasName].polygonLayer;
	var layer = canvasArray[canvasName].layer;
	
	canvasArray[canvasName].historyStep++;
	if (canvasArray[canvasName].historyStep < history.length) {
		canvasArray[canvasName].history.length = canvasArray[canvasName].historyStep;
		canvasArray[canvasName].history2.length = canvasArray[canvasName].historyStep;
	}

	jQuery.each(polygonLayer.getChildren(), function(index, value) {
		if (value === undefined) {
			value.remove();
		}
	});

	jQuery.each(layer.getChildren(), function(index, value) {
		if (value === undefined) {
			value.remove();
		}
	});

	deleteArrowOutsideStandard(canvasName);
	canvasArray[canvasName].rectSelect.remove();

	var json2 = layer.toJSON();
	json2 = json2.replace('"children":"', '"children":');
	json2 = json2.substring(0, json2.length - 2) + json2.substring(json2.length - 1, json2.length);
	json2 = json2.replace(/\\/g, "");

	canvasArray[canvasName].history2.push(json2);
	// console.log(json2);

	var json = polygonLayer.toJSON();
	json = json.replace('"children":"', '"children":');
	json = json.substring(0, json.length - 2) + json.substring(json.length - 1, json.length);
	json = json.replace(/\\/g, "");
	canvasArray[canvasName].history.push(json);
	// console.log(json);

}

function addElements(canvasName, positions) {
	canvasArray[canvasName].countObj = 0;
	var positionsArrays = JSON.parse(positions);
	var numSides = 4;

	//try{
	
	for ( var i = 0; i < positionsArrays.length; i++) {
		
		//alert(positionsArrays[i][2]);
		
		if(checkImg(positionsArrays[i][2])){
			var group = addElement(canvasName, positionsArrays[i][1],
					positionsArrays[i][2], positionsArrays[i][3],
					positionsArrays[i][4],
					numSides,
					positionsArrays[i][0]);
		}else if(checkImg("./"+positionsArrays[i][2])){
			
			var group = addElement(canvasName, positionsArrays[i][1],
					"./"+positionsArrays[i][2], positionsArrays[i][3],
					positionsArrays[i][4],
					numSides,
					positionsArrays[i][0]);
			
		}else if (positionsArrays[i][2].substring(0, 3) === '../'){
			var group = addElement(canvasName, positionsArrays[i][1],
					"./"+positionsArrays[i][2], positionsArrays[i][3],
					positionsArrays[i][4],
					numSides,
					positionsArrays[i][0]);
		}else{
			var group = addElement(canvasName, positionsArrays[i][1],
					location.protocol + '//' + location.host+positionsArrays[i][2], positionsArrays[i][3],
					positionsArrays[i][4],
					numSides,
					positionsArrays[i][0]);
		}
		
		updateIdObj(positionsArrays[i][0], positionsArrays[i][0]);
		updateTypeObj(canvasName, positionsArrays[i][0], positionsArrays[i][0]);
		updateLabelObj(positionsArrays[i][0], positionsArrays[i][0]);
		
		group.hasChangedId = true;
	}
	
	canvasArray[canvasName].stage.draw();
	
	/*}catch(exception){
		alert(exception);
	}*/
	
}

function checkImg(src){
   var jqxhr = jQuery.get(src, function() {
     return true;
   }).fail(function() { 
    return false;
   });
}

function addElement(canvasName, elementType, elementImg, posx, posy, numSides, idElement) {
	
	//alert(elementImg);
	
	var polygonLayer = canvasArray[canvasName].polygonLayer;

	var img = new Image();
	img.src = elementImg;
	img.onload = findHHandWW;

	var result = createPolygon(img, 40, 50, numSides, canvasName);
	var polygon = result[0];
	
	var circle0 = new Kinetic.Circle({
		x : 40,
		y : 50,
		radius : 32,
		draggable : false,
		fill :'white',
		stroke : 'white',
		strokeWidth : 5
	});
	configureCircle(canvasName, circle0);

	var circle1 = new Kinetic.Circle({
		x : 40,
		y : 50,
		radius : 42,
		draggable : false,
		fill :'white',
		stroke : 'white',
		strokeWidth : 5
	});
	configureCircle(canvasName, circle1);
	
	
	var arc1 = new Kinetic.Shape({
	    drawFunc: function(canvas) {
	        var context = canvas.getContext();
	        var x = 40;
	        var y = 50;
	        var radius = 42;
	        var startAngle = 0.1 * Math.PI;
	        var endAngle = 0.6 * Math.PI;
	        var context = canvas.getContext('2d');
	        context.beginPath();
	        context.arc(x, y, radius, startAngle, endAngle, false);
	        canvas.stroke(this);
	    },
	    fill: '#00D2FF',
	    stroke: 'silver',
	    strokeWidth: 4,
	    draggable:false
	});
	
	var arc2 = new Kinetic.Shape({
	    drawFunc: function(canvas) {
	        var context = canvas.getContext();
	        var x = 40;
	        var y = 50;
	        var radius = 42;
	        var startAngle = 0.8 * Math.PI;
	        var endAngle = 1.3 * Math.PI;
	        var context = canvas.getContext('2d');
	        context.beginPath();
	        context.arc(x, y, radius, startAngle, endAngle, false);
	        canvas.stroke(this);
	    },
	    fill: '#00D2FF',
	    stroke: 'silver',
	    strokeWidth: 4,
	    draggable:false
	});
	
	var arc3 = new Kinetic.Shape({
	    drawFunc: function(canvas) {
	        var context = canvas.getContext();
	        var x = 40;
	        var y = 50;
	        var radius = 42;
	        var startAngle = 1.5 * Math.PI;
	        var endAngle = 1.9 * Math.PI;
	        var context = canvas.getContext('2d');
	        context.beginPath();
	        context.arc(x, y, radius, startAngle, endAngle, false);
	        canvas.stroke(this);
	    },
	    fill: '#00D2FF',
	    stroke: 'silver',
	    strokeWidth: 4,
	    draggable:false
	});
	

	var srcImageText = new Kinetic.Text({
		text : "srcImageText"
	});
	srcImageText.setStroke(null);

	var typeText = new Kinetic.Text({
		text : elementType
	});

	canvasArray[canvasName].countObj++;
	var group = createGroup(canvasName, circle0, circle1, polygon, srcImageText, typeText, idElement, arc1,arc2,arc3);
	
	polygonLayer.add(group);

	configureGroup(canvasName, group, posx, posy, polygon);
	
	return group;
}

function undoHistory() {
	
	if (canvasArray[selectedCanvas].historyStep > 0) {
		canvasArray[selectedCanvas].historyStep--;

		var json = canvasArray[selectedCanvas].history[canvasArray[selectedCanvas].historyStep];
		var json2 = canvasArray[selectedCanvas].history2[canvasArray[selectedCanvas].historyStep];

		canvasArray[selectedCanvas].polygonLayer = Kinetic.Node.create(json, selectedCanvas);
		canvasArray[selectedCanvas].layer = Kinetic.Node.create(json2, selectedCanvas);

		ready(selectedCanvas);
	}
	
}

function redoHistory() {
	
	if (canvasArray[selectedCanvas].historyStep < history.length - 1) {
		canvasArray[selectedCanvas].historyStep++;

		var json = canvasArray[selectedCanvas].history[canvasArray[selectedCanvas].historyStep];
		var json2 = canvasArray[selectedCanvas].history2[canvasArray[selectedCanvas].historyStep];

		canvasArray[selectedCanvas].polygonLayer = Kinetic.Node.create(json, selectedCanvas);
		canvasArray[selectedCanvas].layer = Kinetic.Node.create(json2, selectedCanvas);

		ready(selectedCanvas);
	}
}

function ready(canvasName) {
	
	var layer = canvasArray[canvasName].layer;
	var polygonLayer = canvasArray[canvasName].polygonLayer;
	var stage = canvasArray[canvasName].stage;
	var background = canvasArray[canvasName].background;

	// variable to control image of the object
	var imgTab1 = new Image();

	// control of the rectangle to select objects on the screen
	canvasArray[canvasName].moving = false;

	stage.remove();

	// main stage
	stage = new Kinetic.Stage({
		container : "canvas",
		width : 800,
		height : 600
	});
	canvasArray[canvasName].stage = stage;

	stage.setWidth(layer.getChildren()[1].getWidth());
	stage.setHeight(layer.getChildren()[1].getHeight());
	background.setWidth(stage.getWidth());
	background.setHeight(stage.getHeight());

	// add the background on layer
	layer.add(background);
	background.moveToBottom();

	// set width of the canvas
	jQuery("#"+canvasArray[canvasName].canvasContainer).css("width", jQuery(canvasName).width() + 'px');

	stage.add(layer);
	stage.add(polygonLayer);

	configureStage(canvasName);

	jQuery.each(polygonLayer.get('.group1'), function(index, value) {
		var group = this;

		var circle1 = value.getChildren()[1];
		var polygon1 = value.getChildren()[2];
		var text = value.getChildren()[3];

		// alert(text.getText().substring(2));
		imgTab1.src = './' + text.getText().substring(2);
		polygon1.setFillPatternImage(imgTab1);

		group.setDragBoundFunc(function(pos) {
			return rulesDragAndDropObj(canvasName, pos, 80, 80);
		});

		configureGroupListeners(canvasName, group);

		configureCircle(canvasName, circle1);

		polygon1.on('click', function(e) {
			deselectOnClick(canvasName, this, e);
		});

		polygon1.on('mousedown', function(e) {
			deselectOnClick(canvasName, this, e);
		});

		polygonLayer.draw();
	});

}

/**
 * 
 * Method to mount the footer of canvas and your tabs. Makes one for the 'divs'
 * and 'images' to create the objects and put their functionalities. generates
 * javascript for HTML5 canvas
 * 
 */
function mountObj(canvasName) {

	// for list divs
	jQuery("#tabsFooter ul:first li").each(function(index) {

		var posInitX = 40;
		var poxInitY = 50;
		
		var posInitTextX = 16;
		var posInitTextY = 80;

		var nameDiv = jQuery(this).attr("aria-controls");

		if (nameDiv != undefined) { // groupNumber

			// ------------ START TAB

			// stage to footer
			stageTab = new Kinetic.Stage({
				container : nameDiv,
				width : jQuery("#canvas-tabs").width()-10,
				height : 100
			});
			
			jQuery("#" +  nameDiv).find(".kineticjs-content").css("background-image", "none");

			// layer to footer tab1
			var layerTab = new Kinetic.Layer();
			
			var numSides = 4;

			var numObjs = 0;
			
			// for list of obj imagens
			jQuery("#" + nameDiv).find("img").each(function(index) {

				numObjs++;
				
				// variable to control image of
				// the object
				var imgTab = new Image();
				
				//alert(jQuery(this).attr("src"));
				
				imgTab.src = jQuery(this).attr("src");
				imgTab.onload = findHHandWW;
				
				var srcImageText = new Kinetic.Text({
					text : jQuery(this).attr("src")
				});
				srcImageText.setStroke(null);
				
				//label on footer
				var labelText = jQuery(this).next().text();
				var labelTextSize8 = labelText;
				if(labelText.length > 8){
					labelTextSize8 = labelText.substring(0,7).concat(".");
				}
				labelTextSize8 = labelTextSize8.replace("_"," ");
				labelTextSize8 = ucFirstAllWords(labelTextSize8);

				var typeText = new Kinetic.Text({
					text : jQuery(this).next().text()
				});
				typeText.setStroke(null);
				
				var typeLabel = new Kinetic.Text({
                    x:posInitTextX,
                    y:posInitTextY,
                    fontSize: 12,
                    fill: 'black',
                    text : labelTextSize8
                });
				typeLabel.setPosition(posInitTextX,posInitTextY);

				// GROUP
				var result = createPolygon(
					imgTab, posInitX,
					poxInitY,
					numSides,
					canvasName);
				var polygonTab = result[1];
				var polygonTabImage = result[2];

				var rotateDeg = 0;
				if (numSides%2 == 0 ){
					rotateDeg = 360/(2*numSides);
				}
				var polygonTabFake = new Kinetic.RegularPolygon({
					x : 40,
					y : 50,
					opacity : 0,
					radius : 30,
					sides : numSides,
					draggable : true
				});
				polygonTabFake.rotateDeg(rotateDeg);
				polygonTabFake.setAbsolutePosition(posInitX,poxInitY);
				polygonTabFake.posInitX = posInitX;
				polygonTabFake.posInitY = poxInitY;

				posInitX = posInitX + 70;
				posInitTextX = posInitTextX + 70;

				polygonTabFake.on('dragstart',function() {
					jQuery('#body').css('cursor','url('+ polygonTabImage+ ') 30 30,default');
				});

				polygonTabFake.on('dragend',function() {
					
					var stage = canvasArray[selectedCanvas].stage;

					var mousePosStage = stage.getMousePosition();
					if (mousePosStage !== undefined){

						addElement(selectedCanvas,
								typeText.getText(),
								srcImageText.getText(),
								mousePosStage.x - 30,
								mousePosStage.y - 30,
								numSides,
								"group" + (+canvasArray[selectedCanvas].countObj+1));
						
						addElementBt(typeText.getText(),"group"+ canvasArray[selectedCanvas].countObj);
						updateTypeObj(selectedCanvas, "group"+ canvasArray[selectedCanvas].countObj, "group"+ canvasArray[selectedCanvas].countObj);
						
					}
					document.body.style.cursor = 'default';
					
					var polygonTabFakeClone = polygonTabFake.clone();
					polygonTabFakeClone.setAbsolutePosition(polygonTabFake.posInitX,polygonTabFake.posInitY);
					layerTab.add(polygonTabFakeClone);
					this.remove();
					
					layerTab.draw();
					canvasArray[selectedCanvas].polygonLayer.draw();
				});

				polygonTabFake.on('mouseup',function() {
					document.body.style.cursor = 'default';
					
					var polygonTabFakeClone = polygonTabFake.clone();
					polygonTabFakeClone.setAbsolutePosition(polygonTabFake.posInitX,polygonTabFake.posInitY);
					layerTab.add(polygonTabFakeClone);
					this.remove();
					
					layerTab.draw();
				});

				layerTab.add(polygonTab);
				layerTab.add(polygonTabFake.clone());
				layerTab.add(typeLabel);

				// jQuery( "#"+nameDiv ).find("img").remove();

			});
			// END for obj imagens

			if(stageTab.getWidth() < numObjs * 70){
				stageTab.setWidth(numObjs * 70);
			}
			
			stageTab.add(layerTab);

		}// END IF

	});
	// END for divs

}

function clearCanvas() {
	canvasArray[selectedCanvas].polygonLayer.removeChildren();
	
	jQuery.each(canvasArray[selectedCanvas].layer.getChildren(), function(index, value) {
		if (value.isArrow == true) {
			if (value.label != null){
				value.label.remove();
			}
			value.remove();
		}
	});
	
	canvasArray[selectedCanvas].stage.draw();
}

function getElement(polygonLayer, id) {
	for ( var i = 0; i < polygonLayer.getChildren().length; i++) {
		if (polygonLayer.getChildren()[i].getChildren()[4].getText() == id) {
			return polygonLayer.getChildren()[i];
		}
	}
}

/**
 * 
 * Method to update the new id of element. call by a4j:jsFunction oncomplete add
 * the new Element and retrive the new id.
 * 
 */
function updateTypeObj(canvasName, groupID, elementId) {
	
	var polygonLayer = canvasArray[canvasName].polygonLayer;

	var text = polygonLayer.get('#' + groupID)[0].getChildren()[4];
	text.setText(elementId);

}

function updateLink(linkName, nameOutput, nameInput) {
	
	var layer = canvasArray[selectedCanvas].layer;

	jQuery.each(layer.getChildren(), function(index, value) {
		if (value.getName() == linkName) {
			value.nameOutput = nameOutput;
			value.nameInput = nameInput;
		}
	});

}

function getSelectedIconsCommaDelimited(){
	var polygonLayer = canvasArray[selectedCanvas].polygonLayer;
    var ans = "";

	// update element positions
    jQuery.each(polygonLayer.get('.polygon1'), function(index, value) {
    	if(value.selected){
    		ans = ans.concat(",",value.getParent().getChildren()[4].getText());
    	}
	});
    if(ans.length > 0){
    	return ans.substring(1);
    }
    return ans;
}

function getIconPositions(){
	var polygonLayer = canvasArray[selectedCanvas].polygonLayer;
    var positions = {};

	// update element positions
	for ( var i = 0; i < polygonLayer.getChildren().length; i++) {
		var element = polygonLayer.getChildren()[i];
		positions[element.getId()] = [ element.getX(), element.getY() ];
	}
	return JSON.stringify(positions);
}

function getAllIconPositions(){
    var canvasPos = {};
    
    jQuery.each(canvasArray, function(index, value) {
    	var polygonLayer = value.polygonLayer;
    	var positions = {};
    	// update element positions
    	for ( var i = 0; i < polygonLayer.getChildren().length; i++) {
    		var element = polygonLayer.getChildren()[i];
    		positions[element.getId()] = [ element.getX(), element.getY() ];
    	}
    	canvasPos[index] = positions;
	});
	return JSON.stringify(canvasPos);
}

function save(path) {
	setSaved(selectedCanvas, true);
	setPathFile(selectedCanvas, path);
	saveWorkflow(selectedCanvas, path, getIconPositions());
}

function configureCircle(canvasName, circle1) {
	
	circle1.on('mouseover', function() {
		document.body.style.cursor = 'pointer';
	});

	circle1.on('mouseout', function() {
		document.body.style.cursor = 'default';
	});

	canvasArray[canvasName].down = false;

	circle1.on("click",	function(e) {

		var arrow = canvasArray[canvasName].arrow;

		if (canvasArray[canvasName].down) {
			canvasArray[canvasName].down = false;
			
			deleteArrowOutsideStandard(canvasName);
			
			var output = arrow.output.getChildren()[4].getText();
			var input = this.getParent().getChildren()[4].getText();
			var arrowClone = addLink(canvasName, output, input);
			
			addLinkModalBt(arrow.output.getId(), this.getParent().getId(), arrowClone.getName());

		} else {
			var polygonLayer = canvasArray[canvasName].polygonLayer;
			var layer = canvasArray[canvasName].layer;
			
			canvasArray[canvasName].down = true;

			var polygonGroup = getElement(polygonLayer, this.getParent().getId());
			arrow.setPoints([ polygonGroup.getX() + 40,
					polygonGroup.getY() + 50,
					polygonGroup.getX() + 40 + 1,
					polygonGroup.getY() + 50 + 1 ]);

			var idOutput = this.getName();
			arrow.setName("arrow" + idOutput);

			arrow.output = this.getParent();

			var cloneArrow = arrow.clone();
			cloneArrow.isArrow = true;
			layer.add(cloneArrow);

			layer.draw();
		}

	});

	return circle1;
}

function configureStage(canvasName) {
	
	var layer = canvasArray[canvasName].layer;
	var stage = canvasArray[canvasName].stage;
	var rectSelect = canvasArray[canvasName].rectSelect;
	
	stage.on("mousedown", function(e) {
		if (canvasArray[canvasName].moving) {
			canvasArray[canvasName].moving = false;
			layer.draw();
		} else {
			var mousePos = stage.getMousePosition();
			layer.add(rectSelect);
			rectSelect.setX(mousePos.x);
			rectSelect.setY(mousePos.y);
			rectSelect.setWidth(0);
			rectSelect.setHeight(0);
			canvasArray[canvasName].moving = true;
			layer.drawScene();
		}
	});

	stage.on("mousemove", function() {
		var mousePos = stage.getMousePosition();
		//click for arrow
		if (canvasArray[canvasName].down) {
			canvasArray[canvasName].arrow.getPoints()[1].x = mousePos.x;
			canvasArray[canvasName].arrow.getPoints()[1].y = mousePos.y;
			canvasArray[canvasName].down = true;
			layer.draw();
		}
		//click for rectangle
		if (canvasArray[canvasName].moving) {
			rectSelect.setWidth(mousePos.x - rectSelect.getX());
			rectSelect.setHeight(mousePos.y - rectSelect.getY());
			canvasArray[canvasName].moving = true;
			layer.draw();
		}
	});

	stage.on("mouseup", function(e) {
		canvasArray[canvasName].moving = false;
		canvasArray[canvasName].select = rectSelectAllObj(canvasName, e);
		rectSelect.remove();
	});
}

function configureGroupListeners(canvasName, group) {
	group.on('mouseenter', function(e) {
		canvasArray[canvasName].positionX = this.getX() + 40;
		canvasArray[canvasName].positionY = this.getY() + 50;
	});

	group.on('dragstart dragmove', function(e) {
		canvasArray[canvasName].rectSelect.remove();
		dragAndDropGroup(canvasName, this, e);
		changePositionArrow(canvasName, this);
		group.getChildren()[2].off('click');
	});

	group.on('dragend', function(e) {
		group.setDragBoundFunc(function(pos) {
			return rulesDragAndDropObj(canvasName, pos, 80, 80);
		});
		makeHistory(canvasName);
	});
	
	group.on('click', function(e) {
		
		deselectOnClick(canvasName, group.getChildren()[2], e);
		
		group.getChildren()[2].on('click', function(e) {
			polygonOnClick(this, e, canvasName);
		});
	});
	
}

function createGroup(canvasName, circle0, circle1, polygon, srcImageText, typeText, groupId, arc1,arc2,arc3) {
	
	var countObj = canvasArray[canvasName].countObj;

	var group1 = new Kinetic.Group({
		draggable : true,
		id : groupId,
		name : 'group1',
		dragBoundFunc : function(pos) {
			return rulesDragAndDropObj(canvasName, pos, 80, 80);
		}
	});

	var circ0 = circle0.clone();
	var circ = circle1.clone();
	var poly = polygon.clone();
	circ.setId("circle" + countObj + "-1");
	circ.setName("circle" + countObj);
	circ.setPosition(40, 50);
	circ0.setPosition(40, 50);
	poly.setPosition(40, 50);
	group1.add(circ);
	group1.add(circ0);
	group1.add(poly);
	group1.add(srcImageText.clone());
	group1.add(typeText.clone());
	group1.add(arc1);
	group1.add(arc2);
	group1.add(arc3);

	return group1;
}

function configureGroup(canvasName, group, mousePosX, mousePosY, polygon) {

	document.body.style.cursor = 'default';

	deselectAll(canvasName);
	polygon.setStroke("red");
	polygon.selected = true;

	group.setPosition(mousePosX, mousePosY);
	group.hasChangedId = false;

	jQuery("#countObj").val(canvasArray[canvasName].countObj);

	configureGroupListeners(canvasName, group);

	group.on('dblclick', function(e) {

		this.getChildren()[2].setStroke('black');
		this.getChildren()[2].selected = false;
		canvasArray[selectedCanvas].down = false;

		canvasArray[selectedCanvas].polygonLayer.draw();
		
		var objImg = this.getChildren()[2].clone();
		var imagePath = objImg.toDataURL({
			width : 80,
			height : 80
		});

		if (!this.hasChangedId) {
			openChangeIdModal(this.getId(), imagePath);
			this.hasChangedId = true;

		} else {
			openModal(this.getId(), imagePath, canvasName);
		}

	});

	makeHistory(canvasName);
}

function createPolygon(imgTab, posInitX, poxInitY, numSides, canvasName) {
	
	var rotateDeg = 0;
	if (numSides%2 == 0 ){
		rotateDeg = 360/(2*numSides);
	}

	var height = 44.5/imgHeight;
	var width = 44.5/imgWidth;
	
	var offsetY = imgHeight/2;
	var offsetX = imgWidth/2;
	
	//FIXME - error on footer in the first time open
	if(isNaN(offsetX) && isNaN(offsetY)){
		offsetX = 25;
		offsetY = 25;
	}
	
	var polygonTab = new Kinetic.RegularPolygon({
		x : 40,
		y : 50,
		radius : 30,
		sides : numSides,
		stroke : 'black',
		strokeWidth : 4,
		fillPatternImage : imgTab,
		fillPatternOffset : [ offsetX, offsetY ],
//		fillPatternX : -18,
//		fillPatternY : 0,
		fillPatternRepeat : 'no-repeat',
		fillPatternRotationDeg : -rotateDeg,
		fillPatternScale : [height, width],
		draggable : false
	});
	polygonTab.rotateDeg(rotateDeg);
	
	var polygonTabImage;
	try{
		polygonTabImage = polygonTab.toDataURL({
			width : 75,
			height : 75
		});
	}catch(exception){
		//alert(exception);
	}

	polygonTab.setAbsolutePosition(posInitX, poxInitY);
	polygonTab.selected = false;

	var polygon = polygonTab.clone({
		name : 'polygon1',
		draggable : false
	});

	polygon.on('click', function(e) {

		polygonOnClick(this, e, canvasName);

	});

	polygon.on('mousedown', function(e) {

		deselectOnClick(selectedCanvas, this, e);

	});

	return [ polygon, polygonTab, polygonTabImage ];
}

function polygonOnClick(obj,e, canvasName){
	
	deselectOnClick(selectedCanvas, obj, e);
	
	var arrow = canvasArray[canvasName].arrow;

	if (!e.ctrlKey) {
		if (canvasArray[canvasName].down) {
			
			canvasArray[canvasName].down = false;
			
			if(canvasArray[canvasName].oldIdSelected != obj.getParent().getId()){
				
				deleteArrowOutsideStandard(canvasName);
				
				var output = arrow.output.getChildren()[4].getText();
				var input = obj.getParent().getChildren()[4].getText();
				var arrowClone = addLink(canvasName, output, input);
				
				//alert(arrow.output.getId() + "  " + obj.getParent().getId());
				addLinkModalBt(arrow.output.getId(), obj.getParent().getId(), arrowClone.getName());
				
			}

		}
		/*else {
			var polygonLayer = canvasArray[canvasName].polygonLayer;
			var layer = canvasArray[canvasName].layer;
			
			canvasArray[canvasName].down = true;
			
			var polygonGroup = getElement(polygonLayer, obj.getParent().getId());
			arrow.setPoints([ polygonGroup.getX() + 40,
					polygonGroup.getY() + 50,
					polygonGroup.getX() + 40 + 1,
					polygonGroup.getY() + 50 + 1 ]);

			var idOutput = obj.getName();
			arrow.setName("arrow" + idOutput);

			arrow.output = obj.getParent();

			var cloneArrow = arrow.clone();
			cloneArrow.isArrow = true;
			layer.add(cloneArrow);
			
			canvasArray[canvasName].oldIdSelected = obj.getParent().getId();
			
			layer.draw();
		}*/
	}
	
}

function removeLink(name) {
	
	var layer = canvasArray[selectedCanvas].layer;
	
	for ( var i = 0; i < layer.getChildren().length; i++) {
		var arrow = layer.getChildren()[i];
		if (arrow.getName() == name) {
			arrow.remove();
			if (arrow.label != null){
				arrow.label.remove();
			}
			layer.draw();
			return;
		}
	}
}

function updateLabelObj(groupId, newGroupId) {
	
	var polygonLayer = canvasArray[selectedCanvas].polygonLayer;
	
	var group = getElement(polygonLayer, groupId);
	
	var px = group.getChildren()[2].getX() - (newGroupId.length*2);
	var py = group.getChildren()[2].getY() + 30;

	var textLabelObj = new Kinetic.Text({
		text : newGroupId,
		fontSize : 10,
		fill : 'black',
		x : px,
		y : py
	});

	group.add(textLabelObj);
	polygonLayer.draw();

}

function getColorOutputType(status){
	if (status == "TEMPORARY"){
		return "#800080"; //purple
	}
	else if (status == "RECORDED"){
		return "#f08080"; //lightcoral
	}
	else if (status == "BUFFERED"){
		return "#4682b4"; //steelblue
	}
	else {
		return "#c0c0c0"; //silver
	}
}

function getColorOutputExistence(fileExists){
	
	if (fileExists == "true"){
		return "#adff2f"; //greenyellow
	}else if (fileExists == "false"){
		return "#d2691e" //chocolate
	}else {
		return "#c0c0c0"; //silver
	}
}

function getColorRunning(status){

	if (status == "OK"){
		return "#008000"; //green
	}
	else if (status == "ERROR"){
		return "#ff0000"; //red
	}
	else {
		return "#c0c0c0"; //silver
	}
}

function updateAllOutputStatus() {
	
	var polygonLayer = canvasArray[selectedCanvas].polygonLayer;
	
	for ( var i = 0; i < polygonLayer.getChildren().length; i++) {
		updateOutputStatus(polygonLayer.getChildren()[i].getChildren()[4].getText());
	}
}

function updateActionOutputStatus(groupId, status, fileExists) {
	
	var polygonLayer = canvasArray[selectedCanvas].polygonLayer;
	
	var group = getElement(polygonLayer, groupId);
	
	group.getChildren()[5].setStroke(getColorOutputType(status));
	group.getChildren()[7].setStroke(getColorOutputExistence(fileExists));
	
	polygonLayer.draw();

}

function updateActionRunningStatus(groupId, status, fileExists) {
	
	var polygonLayer = canvasArray[selectedCanvas].polygonLayer;
	
	var group = getElement(polygonLayer, groupId);
	
	group.getChildren()[6].setStroke(getColorRunning(status));
	group.getChildren()[7].setStroke(getColorOutputExistence(fileExists));
	
	polygonLayer.draw();

}

function updateArrowType(idOutput, idInput, color, type) {
	
	var layer = canvasArray[selectedCanvas].layer;

	jQuery.each(layer.getChildren(),
		function(index, value) {
			if (value !== undefined && value.isArrow == true) {
				if (value.idOutput == idOutput && value.idInput == idInput){
					value.setStroke(color);
					value.originalColor = color;
					return false;
				}
			}
		}
	);
	
	var coloursArray = canvasArray[selectedCanvas].outputTypeColours;
	var existLegend = false;
	for (var i=0; i < coloursArray.length; ++i){
		if (coloursArray[i][0] == type){
			existLegend = true;
			break;
		}
	}
	
	if (!existLegend){
		coloursArray[coloursArray.length] = [type, color];
		
		canvasArray[selectedCanvas].legend.remove();
		var width = type.length * 8;
		if (width > canvasArray[selectedCanvas].legendWidth){
			canvasArray[selectedCanvas].legendWidth = width;
		}
		
		createLegend(selectedCanvas);
	}
	
	layer.draw();

}


function updateArrowLabel(idOutput, idInput, label) {
	
	var layer = canvasArray[selectedCanvas].layer;
	
	var posx;
	var posy;
	
	var arrow;
	

	jQuery.each(layer.getChildren(),
		function(index, value) {
			if (value !== undefined && value.isArrow == true) {
				if (value.idOutput == idOutput && value.idInput == idInput){
				
					var x1 = value.getPoints()[0].x
					var y1 = value.getPoints()[0].y
					
					var x2 = value.getPoints()[1].x
					var y2 = value.getPoints()[1].y
					
					var angle = Math.atan2(y2 - y1,
							x2 - x1);
					
					
					
					posx = (x1 + x2 + (Math.cos(angle) * 42))/2  - Math.abs((Math.cos(angle) * label.length*2.7));
					posy = (y1 + y2 + (Math.sin(angle) * 42))/2;
					
					arrow = value;
					//value.add(textLabelObj);
					return false;
				}
			}
		}
	);
	
	var textLabelObj = new Kinetic.Text({
		text : label,
		fontSize : 11,
		fontStyle : 'bold',
		fill : 'black',
		x : posx,
		y : posy
	});
	arrow.label = textLabelObj;
	
	layer.add(textLabelObj)
	
	layer.draw();

}

function updateAllArrowColours(canvasName){
	
	var layer = canvasArray[canvasName].layer;
	
	jQuery.each(layer.getChildren(), function(index, value) {
		if (value.isArrow == true) {
			updateAllArrowColor(value.idOutput, value.idInput);
		}
	});
	
}

function setSelected(selected){
	selectedCanvas = selected;
}

function getSelected(){
	return selectedCanvas;
}

function setRunning(canvasName, value){
	canvasArray[canvasName].running = value;
}

function isRunning(canvasName){
	return canvasArray[canvasName].running;
}

function setSaved(canvasName, value){
	canvasArray[canvasName].saved = value;
}

function isSaved(canvasName){
	return canvasArray[canvasName].saved;
}

function setPathFile(canvasName, value){
	canvasArray[canvasName].pathFile = value;
}

function getPathFile(canvasName){
	return canvasArray[canvasName].pathFile;
}

function capitaliseFirstLetter(string){
    return string.charAt(0).toUpperCase() + string.slice(1).toLowerCase();;
}

var isSaveAll = false;
var indexSaving;
var contSaving;
	
function saveAll(){
	isSaveAll = true;
	indexSaving = 0;
	contSaving = 0;
	selectedCanvas = nameTabs[0];
	setWorkflow(nameTabs[0]);
	if (isSaved(nameTabs[0])){
		save(getPathFile(nameTabs[0]));
		indexSaving++;
	}
	else{
		openModalSaveWorkflow();
	}
}
	
function onHideModalSaveWorkflow(saved){
   	//<![CDATA[
	if (!saved){
		indexSaving++;
	}
   	contSaving++;
   	if (isSaveAll == true && contSaving < numTabs){
   		selectedCanvas = nameTabs[indexSaving];
   		setWorkflow(nameTabs[indexSaving]);
   		if (isSaved(nameTabs[indexSaving])){
	   		save(getPathFile(nameTabs[indexSaving]));
	   		indexSaving++;
	   	}
	   	else{
	   		openModalSaveWorkflow();
	   	}
   	}
   	else{
   		isSaveAll = false;
   	}
   	//]]>
}

/**
 * 
 * Method to put all inicial letters Upper Case
 * 
 */
function ucFirstAllWords( str ){
    var pieces = str.split(" ");
    for ( var i = 0; i < pieces.length; i++ ){
        var j = pieces[i].charAt(0).toUpperCase();
        pieces[i] = j + pieces[i].substr(1);
    }
    return pieces.join(" ");
}
