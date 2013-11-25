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
	
	this.stage = null;
	this.layer = null;
	this.polygonLayer = null;
	
	this.running = false;
	this.isSaved = false;
	this.pathFile = null;
}

var selectedCanvas = "canvas-1";

var canvasArray;
var allPositionIcons;
var refreshProcManagerCount = 30;

window.onload = function() {
	var canvasName = "canvas-1";
	
	canvasArray = {};
	
	mountObj();
	configureCanvas(canvasName);
}; 

function configureCanvas(canvasName){
	
	canvasArray[canvasName] = new Canvas(canvasName);
	
	var canvasContainer = "container-"+canvasName;
	canvasArray[canvasName].canvasContainer = canvasContainer;

	// main stage
	var stage = new Kinetic.Stage({
		container : canvasContainer,
		width : 800,
		height : 600
	});
	canvasArray[canvasName].stage = stage;

	// layer to the arrows
	var layer = new Kinetic.Layer();
	canvasArray[canvasName].layer = layer;

	// layer to polygons
	var polygonLayer = new Kinetic.Layer();
	canvasArray[canvasName].polygonLayer = polygonLayer;

	// set width of the canvas
	jQuery("#"+canvasContainer).css("width", jQuery(canvasName).width() + 'px');

	// white background
	var background = new Kinetic.Rect({
		x : 0,
		y : 0,
		width : stage.getWidth(),
		height : stage.getHeight(),
		fill : "white"
	});
	canvasArray[canvasName].background = background;

	// add the background on layer
	layer.add(background);

	// puts a different color on the canvas before it is opened
	jQuery("#"+canvasContainer).css("background-color", "#FFFAFA");
	jQuery(".kineticjs-content").css("background-color", "white");

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

	// ------------ START STAGE CONTROL

	configureStage(canvasName);

	// ------------ END STAGE CONTROL

	makeHistory(canvasName);

	jQuery("#body").keydown(
		function(event) {
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
	
	stage.add(layer);
	stage.add(polygonLayer);

	// ------------- FIM CANVAS
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
		polygonLayer.draw();
	});

	jQuery.each(layer.getChildren(), function(index, value) {
		if (value.isArrow == true) {
			value.setStroke(value.originalColor);
			value.selected = false;
			layer.draw();
		}
	});

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

function addElements(canvasName, positions) {
	canvasArray[canvasName].countObj = 0;
	var positionsArrays = JSON.parse(positions);
	var numSides = 4;

	for ( var i = 0; i < positionsArrays.length; i++) {
		var group = addElement(canvasName, positionsArrays[i][1],
				positionsArrays[i][2], positionsArrays[i][3],
				positionsArrays[i][4],
				numSides,
				positionsArrays[i][0]);
		
		updateIdObj(positionsArrays[i][0], positionsArrays[i][0]);
		updateTypeObj(canvasName, positionsArrays[i][0], positionsArrays[i][0]);
		updateLabelObj(positionsArrays[i][0], positionsArrays[i][0]);
		
		group.hasChangedId = true;
	}
	
	canvasArray[canvasName].stage.draw();
}

function addElement(canvasName, elementType, elementImg, posx, posy, numSides, idElement) {

	var polygonLayer = canvasArray[canvasName].polygonLayer;

	var img = new Image({
		width : 16,
		height : 16
	});
	// img.src = "./"+elementImg;
	img.src = elementImg;

	var result = createPolygon(img, 40, 50, numSides);
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

	var srcImageText = new Kinetic.Text({
		text : "srcImageText"
	});
	srcImageText.setStroke(null);

	var typeText = new Kinetic.Text({
		text : elementType
	});

	canvasArray[canvasName].countObj++;
	var group = createGroup(canvasName, circle0, circle1, polygon, srcImageText, typeText, idElement);
	
	polygonLayer.add(group);

	configureGroup(canvasName, group, posx, posy, polygon);
	
	return group;
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
	// imgTab1.src = '../image/icons/button-cut.png';

	// control of the rectangle to select objects on the screen
	canvasArray[canvasName].moving = false;

	stage.remove();

	// main stage
	stage = new Kinetic.Stage({
		container : "canvas",
		width : 600,
		height : 400
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
function mountObj() {

	// for list divs
	jQuery("#tabsFooter ul:first li").each(function(index) {

		var posInitX = 40;
		var poxInitY = 50;

		var nameDiv = jQuery(this).attr("aria-controls");

		if (nameDiv != undefined) { // groupNumber

			// ------------ START TAB

			// stage to footer
			stageTab = new Kinetic.Stage({
				container : nameDiv,
				width : 500,
				height : 100
			});

			// layer to footer tab1
			var layerTab = new Kinetic.Layer();
			
			var numSides = 4;

			// for list of obj imagens
			jQuery("#" + nameDiv).find("img").each(function(index) {

				// variable to control image of
				// the object
				var imgTab = new Image({
					width : 16,
					height : 16
				});
				imgTab.src = jQuery(this).attr("src");

				var srcImageText = new Kinetic.Text({
					text : jQuery(this).attr("src")
				});
				srcImageText.setStroke(null);

				var typeText = new Kinetic.Text({
					text : jQuery(this).next().text()
				});
				typeText.setStroke(null);

				// ------------------ START
				// GROUP

				var result = createPolygon(
					imgTab, posInitX,
					poxInitY,
					numSides);
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

				posInitX = posInitX + 60;

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

				// jQuery( "#"+nameDiv
				// ).find("img").remove();

			});
			// END for obj imagens

			stageTab.add(layerTab);

		}// END IF

	});
	// END for divs

}

function clearCanvas() {
	canvasArray[selectedCanvas].polygonLayer.removeChildren();
	
	jQuery.each(canvasArray[selectedCanvas].layer.getChildren(), function(index, value) {
		if (value.isArrow == true) {
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
	saveWorkflow(selectedCanvas, path, getIconPositions());
	setSaved(selectedCanvas, true);
	setPathFile(selectedCanvas, path);
}

function configureCircle(canvasName, circle1) {
	
	circle1.on('mouseover', function() {
		document.body.style.cursor = 'pointer';
	});

	circle1.on('mouseout', function() {
		document.body.style.cursor = 'default';
	});

	canvasArray[canvasName].down = false;

	circle1.on("click",
			function(e) {

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

//					var polygonGroup = polygonLayer.get('#'	+ this.getParent().getId());
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
	});

	group.on('dragend', function(e) {
		group.setDragBoundFunc(function(pos) {
			return rulesDragAndDropObj(canvasName, pos, 80, 80);
		});
		makeHistory(canvasName);
	});
}

function createGroup(canvasName, circle0, circle1, polygon, srcImageText, typeText, groupId) {
	
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

function createPolygon(imgTab, posInitX, poxInitY, numSides) {
	
	var rotateDeg = 0;
	if (numSides%2 == 0 ){
		rotateDeg = 360/(2*numSides);
	}
	
	var polygonTab = new Kinetic.RegularPolygon({
		x : 40,
		y : 50,
		radius : 30,
		sides : numSides,
		stroke : 'black',
		strokeWidth : 4,
		fillPatternImage : imgTab,
		fillPatternOffset : [ 7, 7 ],
		fillPatternX : -18,
		fillPatternY : 0,
		fillPatternRepeat : 'no-repeat',
		fillPatternRotationDeg : -rotateDeg,
		draggable : false
	});
	polygonTab.rotateDeg(rotateDeg);
	

	var polygonTabImage = polygonTab.toDataURL({
		width : 75,
		height : 75
	});

	polygonTab.setAbsolutePosition(posInitX, poxInitY);
	polygonTab.selected = false;

	var polygon = polygonTab.clone({
		name : 'polygon1',
		draggable : false
	});

	polygon.on('click', function(e) {

		deselectOnClick(selectedCanvas, this, e);

	});

	polygon.on('mousedown', function(e) {

		deselectOnClick(selectedCanvas, this, e);

	});

	return [ polygon, polygonTab, polygonTabImage ];
}

function removeLink(name) {
	
	var layer = canvasArray[selectedCanvas].layer;
	
	for ( var i = 0; i < layer.getChildren().length; i++) {
		var arrow = layer.getChildren()[i];
		if (arrow.getName() == name) {
			arrow.remove();
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

function getColorOutput(status, fileExists){
	if (status == "TEMPORARY"){
		return "purple";
	}
	else if (status == "RECORDED"){
		return "orange";
	}
	else if (status == "BUFFERED"){
		return "blue";
	}
	else {
		return "white";
	}
}

function getColorRunning(status){
	if (status == "OK"){
		return "green";
	}
	else if (status == "ERROR"){
		return "red";
	}
	else {
		return "white";
	}
}

function updateActionOutputStatus(groupId, status, fileExists) {
	
	var polygonLayer = canvasArray[selectedCanvas].polygonLayer;
	
	var group = getElement(polygonLayer, groupId);
	
	group.getChildren()[1].setStroke(getColorOutput(status, fileExists));
	
	polygonLayer.draw();

}

function updateActionRunningStatus(groupId, status) {
	
	var polygonLayer = canvasArray[selectedCanvas].polygonLayer;
	
	var group = getElement(polygonLayer, groupId);
	
	group.getChildren()[0].setStroke(getColorRunning(status));
	
	polygonLayer.draw();

}

//function getColorArrowType(fileType){
//	if (fileType == "DATA FILE"){
//		return "grey";
//	}
//	else if (fileType == "Hive Table"){
//		return "green";
//	}
//	else if (fileType == "Hive Partition"){
//		return "yellow";
//	}
//	else if (fileType == "TEXT MAP-REDUCE DIRECTORY"){
//		return "blue";
//	}
//	else if (fileType == "BINARY MAP-REDUCE DIRECTORY"){
//		return "orange";
//	}
//	else if (fileType == "TEXT MAP-REDUCE DIRECTORY"){
//		return "brown";
//	}
//	else {
//		return "black";
//	}
//}

function updateArrowType(idOutput, idInput, color) {
	
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
	
	layer.draw();

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
