const CONTROL_GET_UUID_REQUEST = "control_get_uuid";
const COOKIE_KEY_SESSION = "sessionUID";
const INNO_STORAGE_SESSION = "commSession";

export class CommModule
{
	constructor(readyCallback)
	{
		console.log("loaded4");
		this.ip = location.host;
		var storage = window.sessionStorage;
		var storageSession = storage.getItem(INNO_STORAGE_SESSION);
		console.log(storageSession);
		if(storageSession === null)
		{
			console.log("loaded6");
			this.httpGet(CONTROL_GET_UUID_REQUEST, function(uid)
			{
				console.log("loaded7");
				storageSession = new Session(uid);
				storage.setItem(INNO_STORAGE_SESSION, storageSession);
				CommModule.this.session = storageSession;
				readyCallback();
			});
			return;
		}
		this.session = storageSession;
		console.log("loaded5");
	}
	
	getSession()
	{
		return this.session;
	}

	httpGet(theUrl, callback, params)
	{
		var xmlHttp = new XMLHttpRequest();
		var paramStr="";
		for(var key in params)
		{
			paramStr+=key+"="+params[key]+"&";
		}
		if(params)
		{
			paramStr = "?"+paramStr.slice(0, -1);
		}
		xmlHttp.onreadystatechange = function()
		{
			if(xmlHttp.readyState == 4 && xmlHttp.status == 200)
				callback(xmlHttp.responseText);
		}
		xmlHttp.open("GET", theUrl+paramStr);
		xmlHttp.send(null);
		return xmlHttp.responseText;
	}

	getCookie(cookieName)
	{
		var search = cookieName + "=";
		var cookie = document.cookie;

		if(cookie.length > 0)
		{
			startIndex = cookie.indexOf(cookieName);

			if(startIndex != -1)
			{
				startIndex += cookieName.length;
				endIndex = cookie.indexOf(";", startIndex);
				if(endIndex == -1) endIndex = cookie.length;
				return unescape(cookie.substring(startIndex + 1, endIndex));
			}
		}
		return false;
	}
}

export class Session
{
	constructor(sessionUID)
	{
		this.uuid = sessionUID;
	}
}