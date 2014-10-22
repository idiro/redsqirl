/********************************************************************/
/********************************************************************/
/********************************************************************/
/*********************** Framework **********************************/

function CommandHistory() {
    this.size_max = 50;
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

CommandHistory.prototype.redoName = function() {
    var name = "";
    if(this.cur_index < this.hist_stack.length -1 ){
        name = this.hist_stack[this.cur_index+1].getName();
    }
    return name;
};

CommandHistory.prototype.update_buttonname = function() {
    var undoName = this.undoName();
    var redoName = this.redoName();
    var buttonUndo = jQuery('#buttonundo');
    if(undoName.length > 0){
        buttonUndo.attr('title',buttonUndo.attr('alt')+": "+undoName);
    }else{
        buttonUndo.attr('title',buttonUndo.attr('alt'));
    }
    var buttonRedo = jQuery('#buttonredo');
    if(redoName.length > 0){
        buttonRedo.attr('title',buttonRedo.attr('alt')+": "+redoName);
    }else{
        buttonRedo.attr('title',buttonRedo.attr('alt'));
    }
};

CommandHistory.prototype.undo = function() {
    jQuery(".tooltipCanvas").remove();
	if(this.cur_index >= 0){
		this.hist_stack[this.cur_index].undo();
		--this.cur_index;
		this.update_buttonname();
	}
};


CommandHistory.prototype.redo = function() {
    jQuery(".tooltipCanvas").remove();
	if(this.cur_index < this.hist_stack.length -1 ){
		++this.cur_index;
		this.hist_stack[this.cur_index].redo();
		this.update_buttonname();
	}
};

CommandHistory.prototype.clean = function(){
    if(this.hist_stack.length >= this.size_max ){
        var size_transform = this.size_max / 2;
        var i = 0;
        for(i=0; i < size_transform;++i){
            this.hist_stack[0].clean();
            delete this.hist_stack[0];
            this.hist_stack.shift();
        }
        this.cur_index -= i; 
    }
}

CommandHistory.prototype.push_command = function(command) {
    jQuery(".tooltipCanvas").remove();
	while(this.cur_index + 1 < this.hist_stack.length){
		var el = this.hist_stack.pop();
		el.clean();
		delete el;
	}
	++this.cur_index;
	this.hist_stack[this.cur_index] = command;
	this.clean();
	this.update_buttonname();
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
	replaceWFByClone(this.selecteds,this.cloneId, false);
};

CommandDelete.prototype.redo = function(){
	tmpCommandObj = this;
	deleteElements(getAllIconPositions());
};

CommandDelete.prototype.getName = function(){
	return msg_delete_command;
};

CommandDelete.prototype.clean = function(){
	removeCloneWorkflow(this.cloneId);
};

function deleteSelected(canvasName){
    if(getSelectedIconsCommaDelimited() || getSelectedArrowsCommaDelimited()){
	   canvasArray[canvasName].commandHistory.execute(new CommandDelete(getSelectedIconsCommaDelimited(), getSelectedArrowsCommaDelimited()));
	}
}

/********************************************************************/
/********************************************************************/
/********************* CommandAddObj ***************************/
function CommandAddObj(canvasName, elementType, elementImg, posx, posy, numSides, groupId, selecteds) {
	Command.call(this);
	this.canvasName = canvasName;
	this.elementType = elementType;
	this.elementImg = elementImg;
	this.posx = posx;
	this.posy = posy;
	this.numSides = numSides;
	this.groupId = groupId;
	this.selecteds = selecteds;
	this.elementId = '';
};

CommandAddObj.prototype = Object.create(Command.prototype);
CommandAddObj.prototype.constructor = CommandAddObj;

CommandAddObj.prototype.undo = function(){
	deleteElementsJS(this.groupId, "");
};

CommandAddObj.prototype.redo = function(){
	
	addElement(this.canvasName,
			this.elementType,
			this.elementImg,
			this.posx,
			this.posy,
			this.numSides,
			this.groupId,
			this.selecteds
		);
    tmpCommandObj = this;
    addElementBt(this.elementType,this.groupId,this.elementId);
    updateTypeObj(this.canvasName, this.groupId, this.groupId);
	canvasArray[this.canvasName].polygonLayer.draw();
	
};

CommandAddObj.prototype.getName = function(){
	return msg_addelement_command;
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
	return msg_addarrow_command;
};

/********************************************************************/
/********************************************************************/
/*********************** CommandPaste *******************************/
function CommandPaste(selecteds) {
	Command.call(this);
	this.selecteds = selecteds;
	this.idsToPaste = "";
	this.cloneId = "";
};

CommandPaste.prototype = Object.create(Command.prototype);
CommandPaste.prototype.constructor = CommandPaste;

CommandPaste.prototype.undo = function(){
	deleteElementsJS(this.idsToPaste, "");
};

CommandPaste.prototype.redo = function(){
	tmpCommandObj = this;
	if(this.cloneId.empty()){
		//generate clone inside
		pasteJS(this.selecteds);
	}else{
		//use clone
		undoPasteCloneWorkflow(this.selecteds, this.cloneId, true);
	}
};

CommandPaste.prototype.getName = function(){
	return msg_paste_command;
};

CommandPaste.prototype.clean = function(){
	removeCloneWorkflow(this.cloneId);
};

function paste(canvasName,selecteds){
	canvasArray[canvasName].commandHistory.execute(new CommandPaste(selecteds));
}

/********************************************************************/
/********************************************************************/
/********************* CommandReplaceAll ****************************/
function CommandReplaceAll(selecteds, oldStr, newStr, changeLabel) {
	Command.call(this);
	this.selecteds = selecteds;
	this.oldStr = oldStr;
	this.newStr = newStr;
	this.changeLabel = changeLabel;
	this.cloneId = "";
};

CommandReplaceAll.prototype = Object.create(Command.prototype);
CommandReplaceAll.prototype.constructor = CommandReplaceAll;

CommandReplaceAll.prototype.undo = function(){
	tmpCommandObj = this;
	rebuildElementsFromClone(this.selecteds, this.cloneId,false);
	
};

CommandReplaceAll.prototype.redo = function(){
	tmpCommandObj = this;
	replaceJS(getAllIconPositions());
};

CommandReplaceAll.prototype.getName = function(){
	return msg_replaceAll_command;
};

CommandReplaceAll.prototype.clean = function(){
	removeCloneWorkflow(this.cloneId);
};

function replaceAll(canvasName,selecteds, oldStr, newStr, changeLabel){
	canvasArray[canvasName].commandHistory.execute(new CommandReplaceAll(selecteds, oldStr, newStr, changeLabel));
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
    return msg_moveelements_command;
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
    return msg_changeelementid_command;
};

function execChangeIdElementCommand(loadMainWindow , groupId, oldId, newId, oldComment, newComment){
	if(oldId != newId || oldComment != newComment){
		if(oldId != newId ){
			if(!(loadMainWindow==='true')){
				if (!confirm(msg_confirm_changeid)) {
					return false;
				}
			}
		}
        canvasArray[selectedCanvas].commandHistory.execute(
        new CommandChangeId(groupId, oldId,newId, oldComment, newComment));
    }else{
        jQuery('#canvas-tabs').block({ message: jQuery('#domMessageDivCanvas1') });
        currentChangeIdGroup = groupId;
        changeIdElement(groupId,newId,newComment);
        updateLabelObj(groupId,newId);
    }
}


/********************************************************************/
/********************************************************************/
/******************** CommandUpdateElement **************************/
var cloneCommandUpdateElementBuffer = null;

function CommandUpdateElement(groupId,beforeCloneId,afterCloneId) {
    Command.call(this);
    //Needed with that name in canvas.xhtml
    this.selecteds = groupId;
    this.beforeCloneId = beforeCloneId;
    this.afterCloneId = afterCloneId;
};

CommandUpdateElement.prototype = Object.create(Command.prototype);
CommandUpdateElement.prototype.constructor = CommandUpdateElement;

CommandUpdateElement.prototype.undo = function(){
    tmpCommandObj = this;
    rebuildElementsFromClone(this.selecteds, this.beforeCloneId,true);
    
};

CommandUpdateElement.prototype.redo = function(){
    tmpCommandObj = this;
    rebuildElementsFromClone(this.selecteds, this.afterCloneId,true);
};

CommandUpdateElement.prototype.getName = function(){
    return msg_updateelement_command;
};

CommandUpdateElement.prototype.clean = function(){
    removeCloneWorkflow(this.beforeCloneId);
    removeCloneWorkflow(this.afterCloneId);
};

function stackUpdateElement(groupId, beforeCloneId,afterCloneId){
    canvasArray[selectedCanvas].commandHistory.push_command(new CommandUpdateElement(groupId, beforeCloneId,afterCloneId));
}

/********************************************************************/
/********************************************************************/
/********************** CommandChangeCommentWf *****************************/
var currentChangeIdGroup = null;

function CommandChangeCommentWf(oldComment, newComment) {
    Command.call(this);
    this.oldComment = oldComment; 
    this.newComment = newComment;
};

CommandChangeCommentWf.prototype = Object.create(Command.prototype);
CommandChangeCommentWf.prototype.constructor = CommandChangeCommentWf;

CommandChangeCommentWf.prototype.undo = function(){
    //alert("Undo");
    updateWfComment(this.oldComment);
};

CommandChangeCommentWf.prototype.redo = function(){
    //alert("Redo");
    updateWfComment(this.newComment);
};

CommandChangeCommentWf.prototype.getName = function(){
    return msg_changeworkflowcomment_command;
};

function execChangeCommentWfCommand(oldComment, newComment){
    if(oldComment != newComment){
        canvasArray[selectedCanvas].commandHistory.execute(
        new CommandChangeCommentWf(oldComment, newComment));
    }
}

