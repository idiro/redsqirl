
function sessionStorage() {

	if(typeof(Storage) !== "undefined") {
		// Code for localStorage/sessionStorage.
		console.log("yes");
		localStorage.setItem("email", "igor.souza@idiro.com");
	} else {
		// Sorry! No Web Storage support..
		console.log("no");
	}

}

function sessionStorageToken(token) {
	if(typeof(Storage) !== "undefined") {
		localStorage.setItem("token", token);
	} 
}

function sessionStorageEmail(email) {
	if(typeof(Storage) !== "undefined") {
		localStorage.setItem("email", email);
	} 
}

function getsessionToken() {
	if(typeof(Storage) !== "undefined") {
		return localStorage.getItem("token");
	}
}

function getsessionEmail() {
	if(typeof(Storage) !== "undefined") {
		return localStorage.getItem("email");
	}
}


function makeBaseAuth(user, password) {
    var tok = user + ':' + password;
    var hash = btoa(tok);
    return 'Basic' + hash;
}

function errorAjax() {

	$.ajaxSetup({
		error: function (x, status, error) {
		    if (x.status == 403) {
		        alert("Sorry, your session has expired. Please login again to continue");
		        window.location.href ="index.html";
		    } else if (x.status == 401) {
		        alert("Sorry, your session has expired. Please login again to continue");
		        window.location.href ="index.html";
		    } else if (x.status == 200) {

		    } else {
		        alert("An error occurred: " + status + "nError: " + error);
		    }
		}
	});


}

function getUrlParameter(sParam) {
    var sPageURL = decodeURIComponent(window.location.search.substring(1)),
        sURLVariables = sPageURL.split('&'),
        sParameterName,
        i;

    for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam) {
            return sParameterName[1] === undefined ? true : sParameterName[1];
        }
    }
}
