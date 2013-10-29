function Canvas(name){
	this.name=name;
	this.history = [];
	this.history2 = [];
	this.historyStep = -1;
	
	this.countObj = 0;
	
	this.rectSelect = null;
	
	this.positionX = null;
	this.positionY = null;
	
	this.dragDropGroup = null;
	this.moving = null;
	this.down = null;
	this.select = null;
	this.clickArrow = null;
	this.arrow = null;
}

var canvasArray;

window.onload = function() {
	var canvasName = "canvas";
	
	canvasArray = {};
	canvasArray[canvasName] = new Canvas(canvasName);
	
	configureCanvas(canvasName);
}; // FIM window.onload

function configureCanvas(canvasName){
//	var countObj;
//	if (jQuery("[id$='countObj']").val() != 0) {
//		countObj = jQuery("[id$='countObj']").val();
//	} else {
//		countObj = 0;
//	}

	// main stage
	var stage = new Kinetic.Stage({
		container : "canvas",
		width : 600,
		height : 400
	});
	canvasArray[canvasName].stage = stage;

	// layer to the arrows
	var layer = new Kinetic.Layer();
	canvasArray[canvasName].layer = layer;

	// layer to polygons
	var polygonLayer = new Kinetic.Layer();
	canvasArray[canvasName].polygonLayer = polygonLayer;

	// set width of the canvas
	jQuery("#canvas").css("width", jQuery("#canvas1").width() + 'px');

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
	jQuery("#canvas").css("background-color", "#FFFAFA");
	jQuery(".kineticjs-content").css("background-color", "white");

	// control of the rectangle to select objects on the screen
	canvasArray[canvasName].moving = false;

	// checks if something was selected on method:rectSelectAllObj
	canvasArray[canvasName].select = false;

	// checks if an arrow was clicked on metohd:arrow.'click'
	canvasArray[canvasName].clickArrow = false;

	// checks if a drag and drop of a group
	canvasArray[canvasName].dragDropGroup = false;

	canvasArray[canvasName].down = false;

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

				makeHistory(canvasName);

				jQuery.each(polygonLayer.get('.group1'), function(index, value) {
					var group = this;
					var groupNumber = group.getId().substring(5,this.getId().length);
					jQuery.each(value.getChildren(), function(index, value2) {
						if (value2.selected) {
							removeElement(group.getId());
							deleteLayerChildren(canvasName, groupNumber);
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
		});

	canvasArray[canvasName].arrow.on('click', function(e) {

		if (!e.ctrlKey) {
			jQuery.each(layer.getChildren(), function(index, value) {
				if (value.isArrow == true) {
					value.setStroke("black");
				}
			});
		}

		this.setStroke("red");
		this.selected = true;
		canvasArray[canvasName].clickArrow = true;
		stage.draw();
	});

	jQuery("#canvas").click(
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
								value.setStroke('black');
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

	canvasArray[canvasName].positionX = 0;
	canvasArray[canvasName].positionY = 0;

	mountObj(canvasName);

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
					value.getChildren()[1].setStroke("red");
					value.getChildren()[1].selected = true;
					polygonLayer.draw();
					canvasArray[canvasName].select = true;
				} else {
					if (!e.ctrlKey && !canvasArray[canvasName].dragDropGroup) {
						value.getChildren()[1].setStroke(value.getChildren()[1].originaColor);
						value.getChildren()[1].selected = false;
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
				value.setStroke('black');
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
			if (value.getChildren()[1] !== undefined && value.getChildren()[1].selected == true) {
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
			value.setStroke('black');
			value.selected = false;
			layer.draw();
		}
	});

}

function deleteSelected(canvasName) {
	
	var polygonLayer = canvasArray[canvasName].polygonLayer;
	var layer = canvasArray[canvasName].layer;

	makeHistory(canvasName);

	jQuery.each(polygonLayer.get('.group1'), function(index, value) {
		var group = this;
		var groupNumber = group.getId().substring(5, this.getId().length);
		jQuery.each(value.getChildren(), function(index, value2) {
			if (value2.selected == true) {
				deleteLayerChildren(canvasName, groupNumber);
				group.remove();
			}
		});
	});

	var listSize = layer.getChildren().size();
	for ( var i = 0; i < listSize; i++) {
		jQuery.each(layer.getChildren(), function(index, value) {
			if (value !== undefined) {
				if (value.isArrow == true) {
					if (value.selected == true) {
						value.remove();
						return false;
					}
				}
			}

		});
	}

	polygonLayer.draw();
	layer.draw();

}

// remove the arrows that are outside the standard
function deleteArrowOutsideStandard(canvasName) {
	var layer = canvasArray[canvasName].layer;
	
	var listSize = layer.getChildren().size();
	for ( var i = 0; i < listSize; i++) {
		jQuery.each(layer.getChildren(), function(index, value) {
			if (value !== undefined) {
				if (value.getName() !== undefined) {
					var regex = new RegExp("^arrow([0-9]+)-([0-9]+)");
					if (!regex.test(value.getName())) {
						value.remove();
						return false;
					}
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

function deleteLayerChildren(canvasName, groupNumber) {
	
	var layer = canvasArray[canvasName].layer;

	var listSize = layer.getChildren().size();
	for ( var i = 0; i < listSize; i++) {
		jQuery.each(layer.getChildren(),
			function(index, value) {
				if (value !== undefined && value.isArrow == true) {
					if (value.idOutput == groupNumber || value.idInput == groupNumber){
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
	var groupNumber = obj.getId().substring(5, obj.getId().length);

	jQuery.each(layer.getChildren(), function(index, value) {
		if (value.isArrow == true) {
			if (value.idOutput == groupNumber) {
				value.getPoints()[0].x = group.getX() + 40;
				value.getPoints()[0].y = group.getY() + 50;

				var idGroup = value.getName().split("-");
				var g = polygonLayer.get('#group' + idGroup[1]);

				if (g[0] !== undefined) {
					
					var newPoint = null;
					var newPoint2 = null;
					var angle = null;

					var idCircle = g[0].getChildren()[0].getId().split("-");
					if (idCircle[1] == "2") {
						newPoint = getArrowPositions2(g, group, 47, 100);
						newPoint2 = getArrowPositions2(g, group, 60), 100;
						angle = getArrowAngle(newPoint, group);
					} else if (idCircle[1] == "1") {
						newPoint = getArrowPositions2(g, group, 47, 40);
						newPoint2 = getArrowPositions2(g, group, 60, 40);
						angle = getArrowAngle(newPoint, group);
					}
					updatePositionArrow(value, newPoint, newPoint2, 20, 10, angle);
				}

			}
		}
	});

	jQuery.each(layer.getChildren(), function(index, value) {
		if (value.isArrow == true) {
			if (value.idInput == groupNumber) {

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

function addLink(canvasName, inId, outId) {
	// out
	var layer = canvasArray[canvasName].layer;
	var polygonLayer = canvasArray[canvasName].polygonLayer;
	var arrow = canvasArray[canvasName].arrow;
	
	var polygonGroupOut = getElement(polygonLayer, inId);
	var circleOut = polygonGroupOut.getChildren()[0];
	circleOut.setFill('white');
	arrow.setPoints([ polygonGroupOut.getX() + 40, polygonGroupOut.getY() + 50,
			polygonGroupOut.getX() + 40 + 1, polygonGroupOut.getY() + 50 + 1 ]);

	// in
	var polygonGroupIn = getElement(polygonLayer, outId);
	var circleIn = polygonGroupIn.getChildren()[0];
	circleIn.setFill('white');

	var idOutput = circleOut.getName().substring(6, circleOut.getName().length);
	var idInput = circleIn.getName().substring(6, circleIn.getName().length);
	arrow.setName("arrow" + idOutput + "-" + idInput);

	var newPoint = getArrowPositions(arrow, polygonGroupIn, 47);
	var newPoint2 = getArrowPositions(arrow, polygonGroupIn, 60);

	var angle = getArrowAngle2(newPoint, arrow);
	
	updatePositionArrow(arrow, newPoint, newPoint2, 20, 10, angle);

	var arrowClone = arrow.clone();
	arrowClone.idInput = idInput;
	arrowClone.idOutput = idOutput;
	arrowClone.isArrow = true;
	layer.add(arrowClone);
	layer.draw();
	polygonLayer.draw();

	makeHistory(canvasName);
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
			group.getX() + 40, group.getY() + 50, g[0]
			.getX() + offsetX, g[0].getY() + 50, g[0]
			.getX() + offsetX, g[0].getY() + 50, position);
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

function updatePositionArrow(value, newPoint, newPoint2){
	value.getPoints()[1].x = newPoint2[0];
	value.getPoints()[1].y = newPoint2[1];

	value.getPoints()[2].x = newPoint2[0];
	value.getPoints()[2].y = newPoint2[1];

	value.getPoints()[3].x = newPoint[0] - headlen * Math.cos(angle - Math.PI / 6);
	value.getPoints()[3].y = newPoint[1] - headlen * Math.sin(angle - Math.PI / 6);

	value.getPoints()[4].x = newPoint[0];
	value.getPoints()[4].y = newPoint[1];

	value.getPoints()[5].x = newPoint[0] - headlen * Math.cos(angle + Math.PI / 6);
	value.getPoints()[5].y = newPoint[1] - headlen * Math.sin(angle + Math.PI / 6);

	value.getPoints()[6].x = newPoint2[0];
	value.getPoints()[6].y = newPoint2[1];

	value.getPoints()[7].x = newPoint2[0] - headlen2 * Math.cos(angle - Math.PI / 3);
	value.getPoints()[7].y = newPoint2[1] - headlen2 * Math.sin(angle - Math.PI / 3);

	value.getPoints()[8].x = newPoint2[0];
	value.getPoints()[8].y = newPoint2[1];

	value.getPoints()[9].x = newPoint2[0] - headlen2 * Math.cos(angle + Math.PI / 3);
	value.getPoints()[9].y = newPoint2[1] - headlen2 * Math.sin(angle + Math.PI / 3);
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

function undoHistory(canvasName) {
	
	if (canvasArray[canvasName].historyStep > 0) {
		canvasArray[canvasName].historyStep--;

		var json = canvasArray[canvasName].history[canvasArray[canvasName].historyStep];
		var json2 = canvasArray[canvasName].history2[canvasArray[canvasName].historyStep];

		canvasArray[canvasName].polygonLayer = Kinetic.Node.create(json, canvasName);
		canvasArray[canvasName].layer = Kinetic.Node.create(json2, canvasName);

		ready(canvasName);
	}
}

function addElements(canvasName, positions) {
	canvasArray[canvasName].countObj = 0;
	var positionsArrays = JSON.parse(positions);

	for ( var i = 0; i < positionsArrays.length; i++) {
		addElement(canvasName, positionsArrays[i][0], positionsArrays[i][1],
				positionsArrays[i][2], positionsArrays[i][3],
				positionsArrays[i][4]);
	}
}

function addElement(canvasName, elementId, elementType, elementImg, posx, posy) {

	// alert(elementId + " " + elementType + " " + elementImg + " " + posx + " "
	// + posy);
	
	var polygonLayer = canvasArray[canvasName].polygonLayer;
	var stage = canvasArray[canvasName].stage;

	var img = new Image({
		width : 16,
		height : 16
	});
	// img.src = "./"+elementImg;
	img.src = elementImg;

	var posInitX = 40;
	var poxInitY = 50;

	var result = createPolygon(canvasName, img, posInitX, poxInitY);
	var polygon = result[0];

	var circle1 = new Kinetic.Circle({
		x : 40,
		y : 50,
		radius : 42,
		// fill: 'white',
		// stroke: 'black',
		// strokeWidth: 1,
		draggable : false
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
	var group = createGroup(canvasName, circle1, polygon, srcImageText, typeText);

	polygonLayer.add(group);

	configureGroup(canvasName, group, posx, posy, polygon);
	group.hasChangedId = true;

	updateIdObj("group" + canvasArray[canvasName].countObj, elementId);
	updateTypeObj(canvasName, "group" + canvasArray[canvasName].countObj, elementId);
	updateLabelObj(canvasName, "group" + canvasArray[canvasName].countObj, elementId);

	stage.draw();

}

function redoHistory(canvasName) {
	
	if (canvasArray[canvasName].historyStep < history.length - 1) {
		canvasArray[canvasName].historyStep++;

		var json = canvasArray[canvasName].history[canvasArray[canvasName].historyStep];
		var json2 = canvasArray[canvasName].history2[canvasArray[canvasName].historyStep];

		canvasArray[canvasName].polygonLayer = Kinetic.Node.create(json, canvasName);
		canvasArray[canvasName].layer = Kinetic.Node.create(json2, canvasName);

		ready(canvasName);
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

	stage.setWidth(layer.getChildren()[0].getWidth());
	stage.setHeight(layer.getChildren()[0].getHeight());
	background.setWidth(stage.getWidth());
	background.setHeight(stage.getHeight());

	// add the background on layer
	layer.add(background);
	background.moveToBottom();

	// set width of the canvas
	jQuery("#canvas").css("width", jQuery("#canvas1").width() + 'px');

	stage.add(layer);
	stage.add(polygonLayer);

	configureStage(canvasName);

	jQuery.each(polygonLayer.get('.group1'), function(index, value) {
		var group = this;

		var circle1 = value.getChildren()[0];
		var polygon1 = value.getChildren()[1];
		var text = value.getChildren()[2];

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
	
	var polygonLayer = canvasArray[canvasName].polygonLayer;
	var stage = canvasArray[canvasName].stage;

	var circle1 = new Kinetic.Circle({
		x : 40,
		y : 50,
		radius : 42,
		// fill: 'white',
		// stroke: 'black',
		// strokeWidth: 1,
		draggable : false
	});

	configureCircle(canvasName, circle1);

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

			// var countObj = 0;

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
					canvasName, 
					imgTab, posInitX,
					poxInitY);
				var polygon = result[0];
				var polygonTab = result[1];
				var polygonTabImage = result[2];

				var polygonTabFake = new Kinetic.RegularPolygon({
					x : 40,
					y : 50,
					opacity : 0,
					radius : 27,
					sides : 4,
					stroke : 'black',
					strokeWidth : 4,
					fillPatternImage : imgTab,
					fillPatternOffset : [7, 7 ],
					fillPatternX : -18,
					fillPatternY : 0,
					fillPatternRepeat : 'no-repeat',
					fillPatternScale : 0.9,
					fillPatternRotationDeg : 315,
					draggable : true
				});
				polygonTabFake.rotateDeg(45);
				polygonTabFake.setAbsolutePosition(posInitX,poxInitY);

				posInitX = posInitX + 60;

				polygonTabFake.on('dragstart',function() {
					jQuery('#body').css('cursor','url('+ polygonTabImage+ ') 30 30,default');

					this.remove();
					layerTab.add(polygonTabFake.clone());
					layerTab.draw();

				});

				// var countObj = 0;

				canvasArray[canvasName].positionX = 0;
				canvasArray[canvasName].positionY = 0;

				polygonTabFake.on('dragend',function() {

					try {
						document.body.style.cursor = 'default';

						var mousePos = stageTab.getMousePosition();
						if (mousePos.x > 0 || mousePos.y > 0) {
							this.remove();
							layerTab.add(polygonTabFake.clone());
							layerTab.draw();
						}

					} catch (e) {
						try {

							canvasArray[canvasName].countObj++;

							var group1 = createGroup(
								canvasName,
								circle1,
								polygon,
								srcImageText,
								typeText);

							polygonLayer.add(group1);
							this.group = group1;

							// -----
							var mousePosStage = stage.getMousePosition();

							configureGroup(
								canvasName,
								this.group,
								mousePosStage.x - 30,
								mousePosStage.y - 30,
								polygon);

							layerTab.add(polygonTabFake.clone());
							layerTab.draw();

							addElementBt(typeText.getText(),"group"+ canvasArray[canvasName].countObj);
							updateTypeObj(canvasName, "group"+ canvasArray[canvasName].countObj, "group"+ canvasArray[canvasName].countObj);

						} catch (e) {

							document.body.style.cursor = 'default';
							// removeElement(this.group.getId());
							this.remove();
							layerTab.add(polygonTabFake.clone());
							layerTab.draw();

							makeHistory(canvasName);
						}
					}
				});

				polygonTabFake.on('mouseup',function() {
					this.remove();
					layerTab.add(polygonTabFake.clone());
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

function clearCanvas(canvasName) {
	canvasArray[canvasName].polygonLayer.removeChildren();
	
	jQuery.each(canvasArray[canvasName].layer.getChildren(), function(index, value) {
		if (value.isArrow == true) {
			value.remove();
		}
	});
	
	canvasArray[canvasName].stage.draw();
}

function getElement(polygonLayer, id) {
	for ( var i = 0; i < polygonLayer.getChildren().length; i++) {
		if (polygonLayer.getChildren()[i].getChildren()[3].getText() == id) {
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

	var text = polygonLayer.get('#' + groupID)[0].getChildren()[3];
	text.setText(elementId);

}

function updateLink(canvasName, linkName, nameOutput, nameInput) {
	
	var layer = canvasArray[canvasName].layer;

	jQuery.each(layer.getChildren(), function(index, value) {
		if (value.getName() == linkName) {
			value.nameOutput = nameOutput;
			value.nameInput = nameInput;
		}
	});

}

function save(canvasName, path) {
	var polygonLayer = canvasArray[canvasName].polygonLayer;
    var positions = {};

	// update element positions
	for ( var i = 0; i < polygonLayer.getChildren().length; i++) {
		var element = polygonLayer.getChildren()[i];
		positions[element.getId()] = [ element.getX(), element.getY() ];
	}
	saveWorkflow(path, JSON.stringify(positions));
}

function configureCircle(canvasName, circle1) {
	
	var layer = canvasArray[canvasName].layer;
	var polygonLayer = canvasArray[canvasName].polygonLayer;

	circle1.on('mouseover', function() {
		document.body.style.cursor = 'pointer';
	});

	circle1.on('mouseout', function() {
		document.body.style.cursor = 'default';
	});

	canvasArray[canvasName].down = false;

	circle1.on("click",
			function(e) {

				var circle = this;
				var arrow = canvasArray[canvasName].arrow;

				if (canvasArray[canvasName].down) {
					canvasArray[canvasName].down = false;
					
					deleteArrowOutsideStandard(canvasName);
					
					var output = arrow.output.getChildren()[3].getText();
					var input = this.getParent().getChildren()[3].getText();
					addLink(canvasName, output, input);
					
					addLinkModalBt(arrow.output.getId(), this.getParent().getId());


				} else {
					canvasArray[canvasName].down = true;

					var polygonGroup = polygonLayer.get('#'	+ this.getParent().getId());
					arrow.setPoints([ polygonGroup[0].getX() + 40,
							polygonGroup[0].getY() + 50,
							polygonGroup[0].getX() + 40 + 1,
							polygonGroup[0].getY() + 50 + 1 ]);

					var idOutput = this.getName().substring(6, this.getName().length);
					arrow.setName("arrow" + idOutput);

					arrow.output = this.getParent();
					arrow.idOutput = idOutput;

					circle.setFill('white');

					var cloneArrow = arrow.clone();
					layer.add(cloneArrow);

					layer.draw();
					polygonLayer.draw();
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

function createGroup(canvasName, circle1, polygon, srcImageText, typeText) {
	
	var countObj = canvasArray[canvasName].countObj;

	var group1 = new Kinetic.Group({
		draggable : true,
		id : "group" + countObj,
		name : 'group1',
		dragBoundFunc : function(pos) {
			return rulesDragAndDropObj(canvasName, pos, 80, 80);
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

		var objImg = this.getChildren()[1].clone();
		objImg.setStroke('black');
		objImg.selected = false;
		var imagePath = objImg.toDataURL({
			width : 80,
			height : 80
		});

		if (!this.hasChangedId) {
			openChangeIdModal(this.getId(), imagePath);
			this.hasChangedId = true;

		} else {
			openModal(this.getId(), imagePath);
		}

	});

	makeHistory(canvasName);
}

function createPolygon(canvasName, imgTab, posInitX, poxInitY) {
	
	var polygonTab = new Kinetic.RegularPolygon({
		x : 40,
		y : 50,
		radius : 27,
		sides : 4,
		stroke : 'black',
		strokeWidth : 4,
		fillPatternImage : imgTab,
		fillPatternOffset : [ 7, 7 ],
		fillPatternX : -18,
		fillPatternY : 0,
		fillPatternRepeat : 'no-repeat',
		fillPatternScale : 0.9,
		fillPatternRotationDeg : 315,
		draggable : false
	});

	polygonTab.rotateDeg(45);

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

		deselectOnClick(canvasName, this, e);

	});

	polygon.on('mousedown', function(e) {

		deselectOnClick(canvasName, this, e);

	});

	return [ polygon, polygonTab, polygonTabImage ];
}

function removeLink(canvasName, name) {
	
	var layer = canvasArray[canvasName].layer;
	
	for ( var i = 0; i < layer.getChildren().length; i++) {
		var arrow = layer.getChildren()[i];
		if (arrow.getName() == name) {
			arrow.remove();
			layer.draw();
			return;
		}
	}
}

function updateLabelObj(canvasName, groupId, newGroupId) {
	
	var polygonLayer = canvasArray[canvasName].polygonLayer;
	
	var group = polygonLayer.get("#" + groupId);

	var px = group[0].getChildren()[1].getX() - 40;
	var py = group[0].getChildren()[1].getY() + 30;
	if (newGroupId.length > 10 && newGroupId.length < 15) {
		px = px + 10;
	}
	if (newGroupId.length > 5 && newGroupId.length <= 10) {
		px = px + 20;
	}
	if (newGroupId.length >= 1 && newGroupId.length <= 5) {
		px = px + 30;
	}

	var textLabelObj = new Kinetic.Text({
		text : newGroupId,
		fontSize : 10,
		fill : 'black',
		x : px,
		y : py
	});

	group[0].add(textLabelObj);
	polygonLayer.draw();

}
