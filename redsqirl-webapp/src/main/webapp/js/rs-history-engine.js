/********************************************************************/
/********************************************************************/
/********************************************************************/
/********************************************************************/
//Framework

function CommandHistory() {
	this.cur_index = -1;
	this.hist_stack = new Array();
}

CommandHistory.prototype.undoName = function() {
	var name = "";
	if(this.cur_index >= 0 ){
		name = this.hist_stack[this.cur_index].getName();
	}
	return name;
};

CommandHistory.prototype.undo = function() {
	if(this.cur_index >= 0){
		this.hist_stack[this.cur_index].undo();
		--this.cur_index;
	}
};

CommandHistory.prototype.redoName = function() {
	var name = "";
	if(this.cur_index < this.hist_stack.length -1 ){
		name = this.hist_stack[this.cur_index+1].getName();
	}
	return name;
};

CommandHistory.prototype.redo = function() {
	if(this.cur_index < this.hist_stack.length -1 ){
		++this.cur_index;
		this.hist_stack[this.cur_index].redo();
	}
};

CommandHistory.prototype.push_command = function(command) {
	while(this.cur_index + 1 < this.hist_stack.length){
		this.hist_stack.pop();
	}
	++this.cur_index;
	this.hist_stack[this.cur_index] = command;
};

CommandHistory.prototype.execute = function(command) {
	this.push_command(command);
	command.redo();
};


function Command(){
}

Command.prototype.undo = function(){
};

Command.prototype.redo = function(){
};

Command.prototype.getName = function(){
};

Command.prototype.clean = function(){
};

/********************************************************************/
/********************************************************************/
/********************************************************************/
/********************************************************************/
//Example
function CommandExample(myExStr) {
	Command.call(this);
	this.myExStr = myExStr;
};

CommandExample.prototype = Object.create(Command.prototype); // See note below
CommandExample.prototype.constructor = CommandExample;

CommandExample.prototype.undo = function(){
};

CommandExample.prototype.redo = function(){
};

CommandExample.prototype.getName = function(){
};
/********************************************************************/
/********************************************************************/
/********************************************************************/
/********************************************************************/


function CommandDelete(selecteds, selectedArrows) {
	Command.call(this);
	this.selecteds = selecteds;
	this.selectedArrows = selectedArrows;
	this.cloneId = "";
};

CommandDelete.prototype = Object.create(Command.prototype);
CommandDelete.prototype.constructor = CommandDelete;

CommandDelete.prototype.undo = function(){
	//alert(" undo ");
	undoDeleteSelected(this.selecteds,this.cloneId);
};

CommandDelete.prototype.redo = function(){
	//alert(" redo ");
	tmpCommandObj = this;
	deleteElements(getAllIconPositions());
};

CommandDelete.prototype.getName = function(){
	return "delete";
};

function deleteSelected(canvasName){
	canvasArray[canvasName].commandHistory.execute(new CommandDelete(getSelectedIconsCommaDelimited(), getSelectedArrowsCommaDelimited()));
}




function CommandAddObj(canvasName, elementType, elementImg, posx, posy, numSides, idElement, selecteds) {
	Command.call(this);
	this.canvasName = canvasName;
	this.elementType = elementType;
	this.elementImg = elementImg;
	this.posx = posx;
	this.posy = posy;
	this.numSides = numSides;
	this.idElement = idElement;
	this.selecteds = selecteds;
};

CommandAddObj.prototype = Object.create(Command.prototype);
CommandAddObj.prototype.constructor = CommandAddObj;

CommandAddObj.prototype.undo = function(){
	deleteElementsJS(this.idElement, "");
};

CommandAddObj.prototype.redo = function(){
	
	addElement(this.canvasName,
			this.elementType,
			this.elementImg,
			this.posx,
			this.posy,
			this.numSides,
			this.idElement,
			this.selecteds
		);
	canvasArray[this.canvasName].polygonLayer.draw();
	
};

CommandAddObj.prototype.getName = function(){
	return "add Element";
};


function CommandAddArrow(canvasName, outId, inId, name) {
	Command.call(this);
	this.canvasName = canvasName;
	this.outId = outId;
	this.inId = inId;
	this.name = name;
};

CommandAddArrow.prototype = Object.create(Command.prototype);
CommandAddArrow.prototype.constructor = CommandAddArrow;

CommandAddArrow.prototype.undo = function(){
	deleteElementsJS("", this.name);
};

CommandAddArrow.prototype.redo = function(){
	addLink(this.canvasName, this.outId, this.inId);
	updateAllArrowColor();
};

CommandAddArrow.prototype.getName = function(){
	return "add Arrow";
};


function CommandPaste() {
	Command.call(this);
	this.idsToPaste = "";
};

CommandPaste.prototype = Object.create(Command.prototype);
CommandPaste.prototype.constructor = CommandPaste;

CommandPaste.prototype.undo = function(){
	deleteElementsJS(this.idsToPaste, "");
};

CommandPaste.prototype.redo = function(){
	tmpCommandObj = this;
	pasteJS();
};

CommandPaste.prototype.getName = function(){
	return "paste";
};

function paste(canvasName){
	canvasArray[canvasName].commandHistory.execute(new CommandPaste());
}


function CommandReplaceAll(selecteds) {
	Command.call(this);
	this.selecteds = selecteds;
	this.cloneId = "";
};

CommandReplaceAll.prototype = Object.create(Command.prototype);
CommandReplaceAll.prototype.constructor = CommandReplaceAll;

CommandReplaceAll.prototype.undo = function(){
	undoDeleteSelected(this.cloneId);
};

CommandReplaceAll.prototype.redo = function(){
	tmpCommandObj = this;
	replaceAll(this.selecteds);
};

CommandReplaceAll.prototype.getName = function(){
	return "replaceAll";
};

function replaceAll(canvasName){
	canvasArray[canvasName].commandHistory.execute(new CommandReplaceAll(getSelectedIconsCommaDelimited()));
}
