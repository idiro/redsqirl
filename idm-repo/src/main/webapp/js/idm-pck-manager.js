function getIntroPage(restURL){
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
	xmlhttp.open("GET", restURL+'?name='+package+'&version='+version, true);
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
					+ '<a href="#" onclick=\'getPackagePage("'+restURL+'","'+ jsonTable[i].name + '","")\'>'
					+ jsonTable[i].name + "</a>" + "</td><td>"
					+ jsonTable[i].license + "</td><td>"
					+ jsonTable[i].short_description + "</td></tr>";
		} else {
			table = table + '<tr class="order-table-odd-row"><td>'
					+ '<a href="#" onclick=\'getPackagePage("'+restURL+'","'+ jsonTable[i].name + '","")\'>'
					+ jsonTable[i].name + "</td><td>" + jsonTable[i].license
					+ "</td><td>" + jsonTable[i].short_description
					+ "</td></tr>";
		}
	}
	table = table + "</tbody></table>";
	//alert(table);
	return table;
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
					+ '<a href="#" onclick=\'getPackagePage("'+restURL+'","'+ jsonPackage.name + '","'+jsonVersions[i].version +'")\'>'
					+ jsonVersions[i].version + "</a>" + "</td></tr>";
		} else {
			page += '<tr class="order-table-odd-row><td>'
					+ '<a href="#" onclick=\'getPackagePage("'+restURL+'","'+ jsonPackage.name + '","'+jsonVersions[i].version +'")\'>'
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
	page +='<button type="button" onclick="downloadPage("'+jsonPackage.url+'");">Download</button>';
	page +="</div>";
	return page;
}
