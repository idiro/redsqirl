/*function getIntroPage(restURL){
	var xmlhttp;
	if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
		xmlhttp = new XMLHttpRequest();
	}
	xmlhttp.onreadystatechange = function() {
		//alert(1);
		if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
			//alert(2);
			document.getElementById("package_list").innerHTML 
				= create_table_all_packages(restURL, JSON.parse(xmlhttp.responseText));
		    $(document).ready(function() {
                $('#repo').load('./repo.html');
            });
			displaySwap("intro","package");
		}
	};
	xmlhttp.open("GET", restURL, true);
	xmlhttp.send();
}

function getPackagePage(restURL, package, version){
	//alert("get package "+package);
	var xmlhttp;
	if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
		xmlhttp = new XMLHttpRequest();
	}
	xmlhttp.onreadystatechange = function() {
		if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
			//alert("create package "+package);
			var packages = JSON.parse(xmlhttp.responseText);
			var curpack= packages[0];
			for(var i = 1; i < packages.length; i++) {
				if(packages[i].version == version){
					curpack = packages[i];
				};
			}
			document.getElementById("package").innerHTML
			= create_package_page(restURL, curpack, packages);
			//alert(curpack.description);
            $(document).ready(function() {
                $('#help_package').load(curpack.description);
            });
			displaySwap("package","intro");
		};
	};
	xmlhttp.open("GET", restURL+'?id='+package+'&version='+version, true);
	xmlhttp.send();
}

function displaySwap(toDisplay,toHide){
    document.getElementById(toDisplay).style.display = 'block';
    document.getElementById(toHide).style.display = 'none';
}


function create_table_all_packages(restURL, jsonTable){
	
	//alert(intro);
	var table = '<table class="order-table"><thead class="order-table-header"><tr><th>Package Name</th><th>License</th><th>Description</th></tr></thead><tbody>';
	for ( var i = 0; i < jsonTable.length; i++) {
		if (i % 2 == 0) {
			table = table + '<tr class="order-table-even-row"><td>'
					+ '<a href="#" onclick=\'getPackagePage("'+restURL+'","'+ jsonTable[i].id + '","")\'>'
					+ jsonTable[i].name + "</a>" + "</td><td>"
					+ jsonTable[i].license + "</td><td>"
					+ jsonTable[i].short_description + "</td></tr>";
		} else {
			table = table + '<tr class="order-table-odd-row"><td>'
					+ '<a href="#" onclick=\'getPackagePage("'+restURL+'","'+ jsonTable[i].id + '","")\'>'
					+ jsonTable[i].name + "</td><td>" + jsonTable[i].license
					+ "</td><td>" + jsonTable[i].short_description
					+ "</td></tr>";
		}
	}
	table = table + "</tbody></table>";
	//alert(table);
	return table;
}

function downloadPage(url){
    location.href = url;
}

function  create_package_page(restURL, jsonPackage, jsonVersions){

	var page = '<div id="header">'+'<a href="#" onclick=\'getIntroPage("'+restURL+'")\'>back</a></div>';
	//		   +'<h1>Package '+jsonPackage.name+'</h1></div>';
	page += '<div id="left"><div id="help_package"></div>';
	//page += '<h2>Description</h2>'+jsonPackage.description;

    
	page += '<table class="order-table"><thead class="order-table-header"><tr><th>Version</th></tr></thead><tbody>';
	for ( var i = 0; i < jsonVersions.length; i++) {
		if (i % 2 == 0) {
			page += '<tr class="order-table-even-row"><td>'
					+ '<a href="#" onclick=\'getPackagePage("'+restURL+'","'+ jsonPackage.id + '","'+jsonVersions[i].version +'")\'>'
					+ jsonVersions[i].version + "</a>" + "</td></tr>";
		} else {
			page += '<tr class="order-table-odd-row><td>'
					+ '<a href="#" onclick=\'getPackagePage("'+restURL+'","'+ jsonPackage.id + '","'+jsonVersions[i].version +'")\'>'
					+ jsonVersions[i].version + "</a>" + "</td></tr>";
		};
	}
	page +="</tbody></table>";
	page +="</div>";
	page +='<div id="right">';
	
	var d = new Date(jsonPackage.package_date);
	var curr_date = d.getDate();
	var curr_month = d.getMonth() + 1; //Months are zero based
	var curr_year = d.getFullYear();
	
	page +='Date: '+curr_year+ "-"+curr_month+ "-" +curr_date+'<br />';
	page +='License: '+jsonPackage.license+'<br />';
	page +='Version: '+jsonPackage.version+'<br />';
	page +='Price: '+jsonPackage.price+' <br />';
	page +='<button id="button-download" type="button" onclick=\'downloadPage("'+jsonPackage.url+'");\'>Download</button>';
	page +="</div>";
	return page;
}
*/

jQuery(function() {
	
    jQuery('#nav ul li a').each(function(){
        var path = window.location.href;
        var current = path.substring(path.lastIndexOf('/')+1);
        var url = jQuery(this).attr('href');

        if(url == current){
        	jQuery(this).addClass('active');
        };
    });
});

function goToUrl(event, component){
	event.preventDefault();
	var target = jQuery(event.target);
	if(!target.is("input")){
		window.location = jQuery(component).find("a.hiddenLink").attr("href");
	}
	return false;
}

/*function to select all checkbox in a table*/
function selectAllCheckbox(checkbox, checkboxId) {
    var elements = checkbox.form.elements;
    for (var i = 0; i < elements.length; i++) {
        var element = elements[i];
        if (checkboxId.test(element.id)) {
            element.checked = checkbox.checked;
        }
    }
}

function showHideComponent(checkbox, component){
	var c;
	if (checkbox.checked){
		for (c in component){
			$(component[c]).fadeOut("fast");
		}
	}
	else{
		for (c in component){
			$(component[c]).fadeIn("fast");
		}
	}
}

function scrollTo(element){
    jQuery('html, body').animate({
        scrollTop: jQuery(element).offset().top
    }, 2000);
}