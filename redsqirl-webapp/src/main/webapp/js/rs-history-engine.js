function CommandHistory() {
  this.cur_index = -1;
  this.hist_stack= new Array();
  
}

CommandHistory.prototype.undo = function() {
    if(this.cur_index > 0){
        this.hist_stack[this.cur_index].undo();
        --this.cur_index;
    }
};

CommandHistory.prototype.redo = function() {
    if(this.cur_index < this.hist_stack.length -2 ){
        ++this.cur_index;
        this.hist_stack[this.cur_index].redo();
    }
};

CommandHistory.prototype.execute = function(command) {
     while(this.cur_index + 1 < this.hist_stack.length){
        this.hist_stack.pop();
     }
     ++this.cur_index;
     this.hist_stack[this.cur_index] = command;
     command.redo();
};


function Command(){
}

Command.prototype.undo = function(){
};

Command.prototype.redo = function(){
};


function CommandExample(myExStr) {
  Command.call(this);
  this.myExStr = myExStr;
};

CommandExample.prototype = Object.create(Command.prototype); // See note below
CommandExample.prototype.constructor = CommandExample;

