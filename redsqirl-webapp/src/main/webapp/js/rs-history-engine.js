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
