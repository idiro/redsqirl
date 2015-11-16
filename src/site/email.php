<?php

$valid = true;

if( isset( $_GET["p"])){
$sub = "Hadoop Version Support";
$msg="Hi,
I am using the Hadoop distribution ___ version ___ and I couldn't find it online.
Do you plan to support this version soon?

Regards,

";
}

if(isset($_POST['submit'])){
  
  //Email information
  $admin_email = "igfasouza@gmail.com";
  $email = $_REQUEST['email'];
  $subject = $_REQUEST['subject'];
  $comment = $_REQUEST['comment'];

  
  //send email
  mail($admin_email, "$subject", $comment, "From:" . $email);
  
  //Email response
  echo "Thank you for contacting us!";
  $valid = false;

  }
  
  //if "email" variable is not filled out, display the form
  if($valid){
?>

 <form name="f" method="post" onsubmit="return validateForm()" >
  Email: <input name="email" type="text"  style="margin-left: 13px;" /><br />
  Subject: <input name="subject" type="text" value="<?php echo $sub;?>" /><br /><br/>
  Message:<br />
  <textarea name="comment" rows="15" cols="40" ><?php echo $msg;?></textarea><br />
  <input type="submit" value="Send" name="submit" />
  </form>

<?php
}
?>


<script>

function validateForm() {
    var x = document.forms["f"]["email"].value;
    if (x == null || x == "") {
        alert("email must be filled out");
        return false;
    }
    if(!validateEmail(x)){
        alert("Please enter a valid email address");
        return false;
    }
    var y = document.forms["f"]["subject"].value;
    if (y == null || y == "") {
        alert("subject must be filled out");
        return false;
    }
    var z = document.forms["f"]["comment"].value;
    if (z == null || z == "") {
        alert("comment must be filled out");
        return false;
    }
}

function validateEmail(email){
    var re = /\S+@\S+\.\S+/;
    return re.test(email);
}

</script>
  
<?php
  
?>
