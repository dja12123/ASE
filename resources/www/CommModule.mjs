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
		
		if(storageSession === null || typeof(storageSession) != Session)
		{
			console.log("loaded6");
			this.httpGet(CONTROL_GET_UUID_REQUEST, (uid) =>
			{
				console.log("loaded7");
				console.log(this);
				this.session = new Session(uid);
				storage.setItem(INNO_STORAGE_SESSION, this.session);
				readyCallback();
			});
			
			return;
		}
		this.session = storageSession;
		console.log(storageSession.uuid);
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
