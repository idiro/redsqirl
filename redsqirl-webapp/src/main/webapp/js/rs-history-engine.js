/********************************************************************/
/********************************************************************/
/********************************************************************/
/*********************** Framework **********************************/

function CommandHistory() {
    this.size_max = 50;
	this.cur_index = -1;
	this.hist_stack = new Array();
	this.saveIndex = -1;
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
		
		if(this.saveIndex == this.cur_index){
			var canvasNameStar = jQuery('#canvasNameStar-'+getSelectedByName()).text();
			if(canvasNameStar[canvasNameStar.length-1] == "*" ){
				jQuery('#canvasNameStar-'+getSelectedByName()).text(getSelectedByName());
				jQuery('#updateCanvasNameStar').click();
			}
		}else if(this.saveIndex == this.cur_index+1){
			var canvasNameStar = jQuery('#canvasNameStar-'+getSelectedByName()).text();
			if(canvasNameStar[canvasNameStar.length-1] != "*" ){
				jQuery('#canvasNameStar-'+getSelectedByName()).text(getSelectedByName()+"*");
				jQuery('#updateCanvasNameStar').click();
			}
		}
		
	}
};

CommandHistory.prototype.redo = function() {
    jQuery(".tooltipCanvas").remove();
	if(this.cur_index < this.hist_stack.length -1 ){
		++this.cur_index;
		this.hist_stack[this.cur_index].redo();
		this.update_buttonname();
		
		if(this.saveIndex == this.cur_index){
			var canvasNameStar = jQuery('#canvasNameStar-'+getSelectedByName()).text();
			if(canvasNameStar[canvasNameStar.length-1] == "*" ){
				jQuery('#canvasNameStar-'+getSelectedByName()).text(getSelectedByName());
				jQuery('#updateCanvasNameStar').click();
			}
		}else if(this.saveIndex == this.cur_index-1){
			var canvasNameStar = jQuery('#canvasNameStar-'+getSelectedByName()).text();
			if(canvasNameStar[canvasNameStar.length-1] != "*" ){
				jQuery('#canvasNameStar-'+getSelectedByName()).text(getSelectedByName()+"*");
				jQuery('#updateCanvasNameStar').click();
			}
		}
		
	}
};

CommandHistory.prototype.addSaveHistoric = function() {
	this.saveIndex = this.cur_index;
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
        this.saveIndex -= i;
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
	
	if(this.saveIndex == this.cur_index){
		var canvasNameStar = jQuery('#canvasNameStar-'+getSelectedByName()).text();
		if(canvasNameStar[canvasNameStar.length-1] == "*" ){
			jQuery('#canvasNameStar-'+getSelectedByName()).text(getSelectedByName());
			jQuery('#updateCanvasNameStar').click();
		}
	}else if(this.saveIndex == this.cur_index-1){
		var canvasNameStar = jQuery('#canvasNameStar-'+getSelectedByName()).text();
		if(canvasNameStar[canvasNameStar.length-1] != "*" ){
			jQuery('#canvasNameStar-'+getSelectedByName()).text(getSelectedByName()+"*");
			jQuery('#updateCanvasNameStar').click();
		}
	}
	
};

CommandHistory.prototype.execute = function(command) {
	this.push_command(command);
	command.redo();
};

CommandHistory.prototype.removeLastAction = function() {
	this.hist_stack.pop();
	--this.cur_index;
	this.update_buttonname();
	if(this.saveIndex == this.cur_index){
		var canvasNameStar = jQuery('#canvasNameStar-'+getSelectedByName()).text();
		if(canvasNameStar[canvasNameStar.length-1] == "*" ){
			jQuery('#canvasNameStar-'+getSelectedByName()).text(getSelectedByName());
			jQuery('#updateCanvasNameStar').click();
		}
	}
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

function deleteArrow(canvasName,arrowName){
     canvasArray[canvasName].commandHistory.execute(new CommandDelete("",arrowName));
}

/********************************************************************/
/********************************************************************/
/********************* CommandAddObj ***************************/
function CommandAddObj(canvasName, elementType, elementImg, posx, posy, numSides, groupId, selecteds,privilege) {

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
	this.privilege = privilege;
};

CommandAddObj.prototype = Object.create(Command.prototype);
CommandAddObj.prototype.constructor = CommandAddObj;

CommandAddObj.prototype.undo = function(){
    console.timeStamp("CommandAddObj.undo begin");
	deleteElementsJS(this.groupId, "");
	console.timeStamp("CommandAddObj.undo end");
};

CommandAddObj.prototype.redo = function(){
	console.timeStamp("CommandAddObj.redo begin");
	addElement(this.canvasName,
			this.elementType,
			this.elementImg,
			this.posx,
			this.posy,
			this.numSides,
			this.groupId,
			this.selecteds,
			this.privilege
		);
    tmpCommandObj = this;
    
    var cn = this.canvasName;
    var gi = this.groupId;
    
    addElementBt(this.elementType,this.groupId,this.elementId);
    updateTypeObj(this.canvasName, this.groupId, this.groupId);
	canvasArray[this.canvasName].stage.draw();
	
	setTimeout(function(){ retrieveVoranoiPolygonTitleJS(cn, tmpCommandObj.elementId, gi); }, 1000);
	
	canvasArray[this.canvasName].polygonLayer.draw();
	console.timeStamp("CommandAddObj.redo end");
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
	this.cloneId = "";
	
	tmpCommandObj = this;
};

CommandAddArrow.prototype = Object.create(Command.prototype);
CommandAddArrow.prototype.constructor = CommandAddArrow;

CommandAddArrow.prototype.undo = function(){
    console.timeStamp("CommandAddArrow.undo begin");
	//deleteElementsJS("", this.name);
	
    
    console.log("undo a " + this.cloneId);
    
	deleteAllElements();
	replaceWFByCloneVoronoi("",this.cloneId, false);
	
	console.timeStamp("CommandAddArrow.undo end");
};

CommandAddArrow.prototype.redo = function(){
    //console.timeStamp("CommandAddArrow.redo begin");
    
    console.log("redo clone A ");
    
    tmpCommandObj = this;
    
    console.log("redo clone B ");
    
    //!ADD A LINK ON BACK-END
    addLinkBt();
	addLink(this.canvasName, this.outId, this.inId);
	updateArrowColor('#{canvasBean.paramOutId}','#{canvasBean.paramInId}', '#{canvasBean.nameOutput}');
	
	//console.timeStamp("CommandAddArrow.redo end");
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
    console.timeStamp("CommandPaste.prototype.undo begin");
	deleteElementsJS(this.idsToPaste, "");
	console.timeStamp("CommandPaste.prototype.undo end");
};

CommandPaste.prototype.redo = function(){
    console.timeStamp("CommandPaste.prototype.redo begin");
	tmpCommandObj = this;
	if(this.cloneId.empty()){
		//generate clone inside
		pasteJS(this.selecteds);
	}else{
		//use clone
		undoPasteCloneWorkflow(this.selecteds, this.cloneId, true);
	}
	console.timeStamp("CommandPaste.prototype.redo end");
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
function CommandReplaceAll(selecteds, oldStr, newStr, changeLabel, regex) {
	Command.call(this);
	this.selecteds = selecteds;
	this.oldStr = oldStr;
	this.newStr = newStr;
	this.changeLabel = changeLabel;
	this.regex = regex;
	this.cloneId = "";
};

CommandReplaceAll.prototype = Object.create(Command.prototype);
CommandReplaceAll.prototype.constructor = CommandReplaceAll;

CommandReplaceAll.prototype.undo = function(){
    console.timeStamp("CommandReplaceAll.prototype.undo begin");
	tmpCommandObj = this;
	rebuildElementsFromClone(this.selecteds, this.cloneId,false);
	console.timeStamp("CommandReplaceAll.prototype.undo end");
};

CommandReplaceAll.prototype.redo = function(){
    console.timeStamp("CommandReplaceAll.prototype.redo begin");
	tmpCommandObj = this;
	replaceJS(getAllIconPositions());
	console.timeStamp("CommandReplaceAll.prototype.redo end");
};

CommandReplaceAll.prototype.getName = function(){
	return msg_replaceAll_command;
};

CommandReplaceAll.prototype.clean = function(){
	removeCloneWorkflow(this.cloneId);
};

function replaceAll(canvasName,selecteds, oldStr, newStr, changeLabel, regex){
	canvasArray[canvasName].commandHistory.execute(new CommandReplaceAll(selecteds, oldStr, newStr, changeLabel, regex));
}

/********************************************************************/
/********************************************************************/
/************************ CommandMove *******************************/
function CommandMove(oldValues,newValues) {
    Command.call(this);
    this.oldValues = oldValues;
    this.newValues = newValues;
};

CommandMove.prototype = Object.create(Command.prototype);
CommandMove.prototype.constructor = CommandMove;

CommandMove.prototype.undo = function(){
    console.timeStamp("CommandMove.prototype.undo begin");
    jQuery.each(this.oldValues, function(index, value) {
        if(value.elementId !== undefined ){
            var group = canvasArray[selectedCanvas].polygonLayer.get('#' + value.elementId)[0];
            if(group !== undefined ){
            	group.setPosition(value.X,value.Y);
            	changePositionArrow(selectedCanvas, group);
            }else{
            	
            	var group;
            	jQuery.each(canvasArray[selectedCanvas].polygonLayer.get('.group1'), function() {
                    var g = this;
                    if(g.getId() == value.elementId){
                    	group = g;
                    }
            	});
                group.setPosition(value.X,value.Y);
            	changePositionArrow(selectedCanvas, group);
            	
            }
        }
    });
    canvasArray[selectedCanvas].polygonLayer.draw();
    canvasArray[selectedCanvas].layer.draw();
    console.timeStamp("CommandMove.prototype.undo end");
};

CommandMove.prototype.redo = function(){
    console.timeStamp("CommandMove.prototype.redo begin");
    jQuery.each(this.newValues, function(index, value) {
        if(value.elementId !== undefined ){
            var group = canvasArray[selectedCanvas].polygonLayer.get('#' + value.elementId)[0];
            if(group !== undefined ){
            	group.setPosition(value.X,value.Y);
            	changePositionArrow(selectedCanvas, group);
            }
        }
    });
    canvasArray[selectedCanvas].polygonLayer.draw();
    canvasArray[selectedCanvas].layer.draw();
    console.timeStamp("CommandMove.prototype.redo end");
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
    console.timeStamp("CommandChangeId.prototype.undo begin");
    jQuery('#canvas-tabs').block({ message: jQuery('#domMessageDivCanvas1') });
    currentChangeIdGroup = this.groupId;
    changeIdElement(this.groupId,this.oldId,this.oldComment);
    updateLabelObj(this.groupId,this.oldId);
    console.timeStamp("CommandChangeId.prototype.undo end");
};

CommandChangeId.prototype.redo = function(){
    console.timeStamp("CommandChangeId.prototype.redo begin");
    jQuery('#canvas-tabs').block({ message: jQuery('#domMessageDivCanvas1') });
    currentChangeIdGroup = this.groupId;
    changeIdElement(this.groupId,this.newId,this.newComment);
    updateLabelObj(this.groupId,this.newId);
    console.timeStamp("CommandChangeId.prototype.redo end");
};

CommandChangeId.prototype.getName = function(){
    return msg_changeelementid_command;
};

function execChangeIdElementCommand(loadMainWindow , groupId, oldId, newId, oldComment, newComment){
    console.log("execChangeIdElementCommand begin"+loadMainWindow+", "+groupId+", "+oldId+", "+newId+", "+oldComment+", "+newComment);
    
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
    console.timeStamp("execChangeIdElementCommand end");
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
    console.timeStamp("CommandUpdateElement.prototype.undo begin");
    tmpCommandObj = this;
    rebuildElementsFromClone(this.selecteds, this.beforeCloneId,true);
    console.timeStamp("CommandUpdateElement.prototype.undo end");
};

CommandUpdateElement.prototype.redo = function(){
    console.timeStamp("CommandUpdateElement.prototype.redo begin");
    tmpCommandObj = this;
    rebuildElementsFromClone(this.selecteds, this.afterCloneId,true);
    console.timeStamp("CommandUpdateElement.prototype.redo end");
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
/********************** CommandChangeCommentWf **********************/
var currentChangeIdGroup = null;

function CommandChangeCommentWf(oldComment, newComment) {
    Command.call(this);
    this.oldComment = oldComment; 
    this.newComment = newComment;
};

CommandChangeCommentWf.prototype = Object.create(Command.prototype);
CommandChangeCommentWf.prototype.constructor = CommandChangeCommentWf;

CommandChangeCommentWf.prototype.undo = function(){
    console.timeStamp("CommandChangeCommentWf.prototype.undo begin");
    updateWfComment(this.oldComment);
    console.timeStamp("CommandChangeCommentWf.prototype.undo end");
};

CommandChangeCommentWf.prototype.redo = function(){
    console.timeStamp("CommandChangeCommentWf.prototype.redo begin");
    updateWfComment(this.newComment);
    console.timeStamp("CommandChangeCommentWf.prototype.redo end");
};

CommandChangeCommentWf.prototype.getName = function(){
    return msg_changeworkflowcomment_command;
};

function execChangeCommentWfCommand(oldComment, newComment){
    console.timeStamp("execChangeCommentWfCommand begin");
    console.log('hahaho');
    if(oldComment != newComment){
        canvasArray[selectedCanvas].commandHistory.execute(
        new CommandChangeCommentWf(oldComment, newComment));
    }
    console.timeStamp("execChangeCommentWfCommand end");
}

/********************************************************************/
/********************************************************************/
/********************* CommandAggregate *****************************/
function CommandAggregate() {
    Command.call(this);
    this.cloneId = "";
    this.nameSA = "";
    this.nameModel = "";
};

CommandAggregate.prototype = Object.create(Command.prototype);
CommandAggregate.prototype.constructor = CommandAggregate;

CommandAggregate.prototype.undo = function(){
    console.timeStamp("CommandAggregate.prototype.undo begin");
	//alert("undo");
	deleteAllElements();
	replaceWFByClone("",this.cloneId, false);
	undoAggregate(this.nameModel,this.nameSA);
	console.timeStamp("CommandAggregate.prototype.undo end");
};

CommandAggregate.prototype.redo = function(){
    console.timeStamp("CommandAggregate.prototype.redo begin");
	//alert("redo");
	tmpCommandObj = this;
	cloneBeforeAggregate(getAllIconPositions());
	console.timeStamp("CommandAggregate.prototype.redo end");
};

CommandAggregate.prototype.getName = function(){
	return msg_aggregate_command;
};

CommandAggregate.prototype.clean = function(){
	removeCloneWorkflow(this.cloneId);
};

function undoRedoAggregate(){
	canvasArray[selectedCanvas].commandHistory.execute(new CommandAggregate());
}


/********************************************************************/
/********************************************************************/
/********************* CommandExpand *****************************/
function CommandExpand(selectedSAIcons) {
    Command.call(this);
    this.selectedSAIcons = selectedSAIcons;
    this.cloneId = "";
    this.nameSA = "";
};

CommandExpand.prototype = Object.create(Command.prototype);
CommandExpand.prototype.constructor = CommandExpand;

CommandExpand.prototype.undo = function(){
    console.timeStamp("CommandExpand.prototype.undo begin");
	//alert("undo");
	deleteAllElements();
	replaceWFByClone("",this.cloneId, false);
	console.timeStamp("CommandExpand.prototype.undo end");
};

CommandExpand.prototype.redo = function(){
    console.timeStamp("CommandExpand.prototype.redo begin");
	tmpCommandObj = this;
	cloneBeforeExpand(getAllIconPositions());
	console.timeStamp("CommandExpand.prototype.redo end");
};

CommandExpand.prototype.getName = function(){
	return msg_expand_command;
};

CommandExpand.prototype.clean = function(){
	removeCloneWorkflow(this.cloneId);
};

function undoRedoExpand(selectedSAIcons){
	canvasArray[selectedCanvas].commandHistory.execute(new CommandExpand(selectedSAIcons));
}
