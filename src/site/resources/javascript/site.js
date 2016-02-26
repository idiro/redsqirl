$(document).ready(function() {
       
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

	//$("a[title='About']").addClass("scroll");
	//$("a[title='Take a Look']").addClass("scroll");

	//$(".scroll").click(function(event){
		//event.preventDefault();
		//$('html,body').animate({scrollTop:5000},1000);
	//});

	$(".container").prev().append("<img alt='Red Sqirl' src='images/logo_redsqirl.png' id='icons' width='180'/>");

});
