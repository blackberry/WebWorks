var deployMachine = 'http://it0000000049025/'; //include the http protocol and trailing slash
var deployFolder = 'jam_extensions_jars/'; //do not include a leading slash but keep the trailing slash

var deployUrl = deployMachine + deployFolder;

var linkId = 'deploy-link';

function setDownloadLink() {
	var jarAnchor = document.getElementById(linkId);
	
	if(jarAnchor != undefined && jarAnchor != null) {
		var pageName = extractPageName(jarAnchor.href);
		jarAnchor.href = deployUrl + pageName;
	}
}

function extractPageName(fromUrl) {
	return fromUrl.substring(fromUrl.lastIndexOf('/') + 1, fromUrl.length);
}