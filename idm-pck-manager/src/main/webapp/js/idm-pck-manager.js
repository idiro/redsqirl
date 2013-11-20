function getIntroPage(restURL){
	var xmlhttp;
	if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
		xmlhttp = new XMLHttpRequest();
	}
	xmlhttp.onreadystatechange = function() {
		//alert(1);
		if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
			//alert(2);
			document.getElementById("all").innerHTML 
				= create_table_all_packages(restURL, JSON.parse(xmlhttp.responseText));
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
			document.getElementById("all").innerHTML
			= create_package_page(restURL, curpack, packages);
		};
	};
	xmlhttp.open("GET", restURL+'?name='+package+'&version='+version, true);
	xmlhttp.send();
}

function create_table_all_packages(restURL, jsonTable){
	var intro = '<h1>IDM Package Manager</h1>';
	intro += '<p>Welcome to IDM Package manager. The website aims to '+
        	'gather all the official package available on the IDM '+
        	'platform. </p>';
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
			table = table + '<tr class="order-table-odd-row><td>'
					+ '<a href="#" onclick=\'getPackagePage("'+restURL+'","'+ jsonTable[i].name + '","")\'>'
					+ jsonTable[i].name + "</td><td>" + jsonTable[i].license
					+ "</td><td>" + jsonTable[i].short_description
					+ "</td></tr>";
		}
	}
	table = table + "</tbody></table>";
	//alert(table);
	return intro + table;
}

function  create_package_page(restURL, jsonPackage, jsonVersions){

	var page = '<div id="header">'+'<a href="#" onclick=\'getIntroPage("'+restURL+'")\'>back</a>'+
			   '<h1>Package '+jsonPackage.name+'</h1></div>';
	page += '<div id="left"><p>'+jsonPackage.short_description+'</p>';
	page += '<h2>Description</h2>'+jsonPackage.description;
	
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
