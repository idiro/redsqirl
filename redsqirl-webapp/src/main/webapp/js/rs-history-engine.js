/********************************************************************/
/********************************************************************/
/********************************************************************/
/*********************** Framework **********************************/
//

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
/*********************** CommandDelete ******************************/
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


/********************************************************************/
/********************************************************************/
/********************* Command Add Object ***************************/
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


/********************************************************************/
/********************************************************************/
/********************** CommandAddArrow *****************************/
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


/********************************************************************/
/********************************************************************/
/*********************** CommandPaste *******************************/
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


/********************************************************************/
/********************************************************************/
/********************* CommandReplaceAll ****************************/
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

/********************************************************************/
/********************************************************************/
/************************ CommandMove *******************************/
function CommandMove(canvasName, oldValues,newValues) {
    Command.call(this);
    this.canvasName = canvasName;
    this.oldValues = oldValues;
    this.newValues = newValues;
};

CommandMove.prototype = Object.create(Command.prototype);
CommandMove.prototype.constructor = CommandMove;

CommandMove.prototype.undo = function(){
    //alert("Undo");
    var canvasNameCur = this.canvasName;
    jQuery.each(this.oldValues, function(index, value) {
        if(value.elementId !== undefined ){
            var group = canvasArray[canvasNameCur].polygonLayer.get('#' + value.elementId)[0];
            //alert(group.getId());
            //alert(group.getId()+" ("+group.X+","+group.Y+") ("+value.X+","+value.Y+")");
            group.setPosition(value.X,value.Y);
            changePositionArrow(canvasNameCur, group);
        }
    });
    
    canvasArray[this.canvasName].polygonLayer.draw();
    canvasArray[this.canvasName].layer.draw();
};

CommandMove.prototype.redo = function(){
    //alert("Redo");
    var canvasNameCur = this.canvasName;
    jQuery.each(this.newValues, function(index, value) {
        if(value.elementId !== undefined ){
            var group = canvasArray[canvasNameCur].polygonLayer.get('#' + value.elementId)[0];
            group.setPosition(value.X,value.Y);
            changePositionArrow(canvasNameCur, group);
        }
    });
    canvasArray[this.canvasName].polygonLayer.draw();
    canvasArray[this.canvasName].layer.draw();
};

CommandMove.prototype.getName = function(){
    return "move elements";
};

/********************************************************************/
/********************************************************************/
/********************** CommandChangeId *****************************/
var currentChangeIdGroup = null;

function CommandChangeId(groupId, oldId,newId, oldComment, newComment) {
    Command.call(this);
    this.groupId = groupId;
    this.oldId = oldId;
    this.newId = newId;
    this.oldComment = oldComment; 
    this.newComment = newComment;
};

CommandChangeId.prototype = Object.create(Command.prototype);
CommandChangeId.prototype.constructor = CommandChangeId;

CommandChangeId.prototype.undo = function(){
    //alert("Undo");
    jQuery('#canvas-tabs').block({ message: jQuery('#domMessageDivCanvas1') });
    currentChangeIdGroup = this.groupId;
    changeIdElement(this.groupId,this.oldId,this.oldComment);
    updateLabelObj(this.groupId,this.oldId);
};

CommandChangeId.prototype.redo = function(){
    //alert("Redo");
    jQuery('#canvas-tabs').block({ message: jQuery('#domMessageDivCanvas1') });
    currentChangeIdGroup = this.groupId;
    changeIdElement(this.groupId,this.newId,this.newComment);
    updateLabelObj(this.groupId,this.newId);
};

CommandChangeId.prototype.getName = function(){
    return "change element id";
};

function execChangeIdElementCommand(groupId, oldId,newId, oldComment, newComment){
    if(oldId != newId || oldComment != newComment){
        canvasArray[selectedCanvas].commandHistory.execute(
        new CommandChangeId(groupId, oldId,newId, oldComment, newComment));
    }else{
        jQuery('#canvas-tabs').block({ message: jQuery('#domMessageDivCanvas1') });
        currentChangeIdGroup = groupId;
        changeIdElement(groupId,newId,newComment);
        updateLabelObj(groupId,newId);
    }
}

