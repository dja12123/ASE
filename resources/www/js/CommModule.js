const CONTROL_GET_UUID_REQUEST = "control_get_uuid";
const COOKIE_KEY_SESSION = "sessionUID";
const CONTROL_CHANNEL_KEY = "control";

export class CommModule
{
	constructor(startCallback, disconnectCallback, reConnectCallback)
	{
		this.isConnect = false;
		//this.ip = location.host;
		this.ip = "172.16.1.4";
		this.sessionUUID = this.getCookie(COOKIE_KEY_SESSION);
		this.startCallback = startCallback;
		this.disconnectCallback = disconnectCallback;
		this.reConnectCallback = reConnectCallback;
		this.controlChannel = this.createChannel(CONTROL_CHANNEL_KEY, ()=>{this.controlStart();}, null, ()=>{this.controlDisconnect();});
	}
	
	controlStart()
	{
		console.log("통신 연결 성공");
		this.isConnect = true;
		if(this.startCallback != null) this.startCallback();
	}
	
	controlDisconnect()
	{
		console.log("연결 끊김 재접속 시도..");
		if(this.isConnect && this.disconnectCallback != null) this.disconnectCallback();
		this.isConnect = false;
		setTimeout(()=>
		{
			this.controlChannel = this.createChannel(CONTROL_CHANNEL_KEY, ()=>{this.controlReconnect();}, null, ()=>{this.controlDisconnect();});
		}, 3000);
	}
	
	controlReconnect()
	{
		console.log("재접속 완료");
		this.isConnect = true;
		if(this.reConnectCallback != null)this.reConnectCallback();
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
			var startIndex = cookie.indexOf(cookieName);

			if(startIndex != -1)
			{
				startIndex += cookieName.length;
				var endIndex = cookie.indexOf(";", startIndex);
				if(endIndex == -1) endIndex = cookie.length;
				return unescape(cookie.substring(startIndex + 1, endIndex));
			}
		}
		return false;
	}
	
	createChannel(key, onOpen, onMessage, onClose)
	{
		var ws = new WebSocket("ws://" + this.ip + ":8080");
		ws.onopen = () =>
		{
			ws.send(key);
			if(onOpen != null) onOpen();
		};
		ws.onmessage = onMessage;
		ws.onclose = onClose;
		return ws;
	}
	
}