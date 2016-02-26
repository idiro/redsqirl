<?php
if (isset($_POST['send'])) {

     $to = 'beta@redsqirl.com'; // Use your own email address
     $subject = 'Red Sqirl Beta Testing';
     $message = 'Name: ' . $_POST['name'] . "\r\n\r\n";
     $message .= 'Email: ' . $_POST['email'];
     $success = mail($to, $subject, $message);

}
    

?>

<!DOCTYPE html>
<html>
<head>
<title>Red Sqirl</title>
<link href="bootstrap.css" rel="stylesheet" type="text/css" media="all" />
<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
<script src="js/jquery.min.js"></script>
<!-- Custom Theme files -->
<!--theme-style-->
<link href="style.css" rel="stylesheet" type="text/css" media="all" />	
<!--//theme-style-->
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
<meta name="keywords" content="Red, Sqirl, Hadoop, Data, Big, Mining, Drag, Drop">
<meta name="description" content="Red Sqirl is a drag & drop mining tool for Hadoop">
<script type="application/x-javascript"> addEventListener("load", function() { setTimeout(hideURLbar, 0); }, false); function hideURLbar(){ window.scrollTo(0,1); } </script>
<!--fonts-->
<link href='http://fonts.googleapis.com/css?family=Open+Sans:400,300,600,700,800' rel='stylesheet' type='text/css'>
<!--//fonts-->
<script type="text/javascript" src="js/move-top.js"></script>
<script type="text/javascript" src="js/easing.js"></script>
<script src="SpryAssets/SpryValidationTextField.js" type="text/javascript"></script>
<script src="SpryAssets/SpryValidationTextarea.js" type="text/javascript"></script>
<link rel="shortcut icon" href="images/favicon.ico" type="image/x-icon">
<link rel="icon" href="images/favicon.ico" type="image/x-icon">
<!--//google-analytics-->
<script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-63375912-1', 'auto');
  ga('send', 'pageview');
</script>
</head>

<body> 
<!--header-->
	<div class="header">
		<div class="container">
			<div class="header-top">
				<div class="logo">
					
				</div>
				<div class="top-nav">
					<span class="menu"> </span>
					
					<!-- script-nav -->
			<script>
			$("span.menu").click(function(){
				$(".top-nav ul").slideToggle(500, function(){
				});
			});
			</script>
				<script type="text/javascript">
					jQuery(document).ready(function($) {
						$(".scroll").click(function(event){		
							event.preventDefault();
							$('html,body').animate({scrollTop:$(this.hash).offset().top},1000);
						});
					});
					</script>
				</div>
				<div class="clearfix"> </div>
			</div>
			<div class="header-matter">
			<a href="/index.html"><img src="red_logo.png" id="logored" class="img-responsive" alt="Red Sqirl" /></a>
				<h2>THANK <span>YOU</span></h2>
				<p>WE WILL BE IN TOUCH SHORTLY</p>
				
			
			</div>
		</div>
	</div>
	 </div>

</body>
</html>