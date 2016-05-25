$(document).ready(function() {
       
	var p = location.search.split('p=')[1]
	if(p != null){
		$("#emailDownload").show();
		$("#email").hide();
	}else{
		$("#emailDownload").hide();
		$("#email").show();
	}

	$(".dropdown-toggle").each(function(index) {
	  if($(this).text().indexOf("space") >=0 ){
	     $(this).text('');
	     $(this).css({'cursor' :"default"});
	     $(this).children().remove();
	     $(this).next().remove();
	     $(this).removeAttr('class');
	  }
	  if($(this).text().indexOf("Sign") >=0 ){
	     $(this).children().remove();
	     $(this).next().remove();
	  }
	  if($(this).text().indexOf("Login") >=0 ){
	     $(this).children().remove();
	     $(this).next().remove();
	  }
	});
	
});
