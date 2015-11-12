$( document ).ready(function() {
       
	var p = location.search.split('p=')[1]
	if(p != null){
		$("#emailDownload").show();
		$("#email").hide();
	}else{
		$("#emailDownload").hide();
		$("#email").show();
	}

});

