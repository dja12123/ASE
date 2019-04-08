const COOKIE_KEY_SESSION = "sessionUID";
const CONTROL_CHANNEL_KEY = "control";
const WEB_SOCKET_PORT = "8080";

export class CommModule
{
	constructor(startCallback, disconnectCallback, reConnectCallback)
	{
		this.channelList = new Array();
		this.isConnect = false;
		this.ip = location.host;
		//this.ip = "172.16.1.4";
		this.sessionUUID = getCookie(COOKIE_KEY_SESSION);
		this.startCallback = startCallback;
		this.disconnectCallback = disconnectCallback;
		this.reConnectCallback = reConnectCallback;
		this.isReconnect = false;
		this.controlChannel = new Channel(this.ip, CONTROL_CHANNEL_KEY, ()=>{this.controlStart();}, (e)=>{this.controlReceive(e);}, ()=>{this.controlDisconnect();});
		this.controlChannel.connect();
	}
	
	controlStart()
	{
		console.log("control channel open");
	}
	
	controlDisconnect()
	{
		if(this.isConnect && this.disconnectCallback != null)
		{
			console.log("disconnect, try to reconnect");
			this.disconnectCallback();
		}
		else
		{
			console.log("try to reconnect...");
		}
		this.isConnect = false;
		setTimeout(()=>
		{
			this.controlChannel.connect();
		}, 3000);
	}
	
	controlReceive(e)
	{
		var data = JSON.parse(e.data);
		if(data.cmdType == "setUUID")
		{
			this.sessionUUID = data.sessionUUID;
			setCookie("sessionUUID", this.sessionUUID);
			this.isConnect = true;
			if(this.isReconnect)
			{
				if(this.reConnectCallback != null) this.reConnectCallback();
				this.channelList.forEach((e)=>
				{
					e.connect();
				});
				console.log("reconnection successful, sessionID:"+this.sessionUUID);
				
			}
			else
			{
				this.isReconnect = true;
				if(this.startCallback != null) this.startCallback();
				this.channelList.forEach((e)=>
				{
					e.connect();
				});
				console.log("connection successful, sessionID:"+this.sessionUUID);
			}
		}
	}
	
	createChannel(key, wsOpen, onMessage, wsClose)
	{
		var channel = new Channel(this.ip, key, wsOpen, onMessage, wsClose, (ch)=>
		{
			var idx = this.channelList.indexOf(ch);
			this.channelList.splice(idx);
		});
		this.channelList.push(channel);
		if(this.isConnect) channel.connect();
		return channel;
	}	
}

export class Channel
{
	constructor(ip, key, wsOpen, onMessage, wsClose, onClose)
	{
		this.ip = ip;
		this.key = key;
		this.wsOpen = wsOpen;
		this.onMessage = onMessage;
		this.wsClose = wsClose;
		this.onClose = onClose;
		this.isConnect = false;
		this.connecting = false;
	}
	
	connect()
	{
		if(this.connecting || this.isConnect) return;
		this.connecting = true;
		this.ws = new WebSocket("ws://"+this.ip+":"+WEB_SOCKET_PORT);
		this.ws.onopen = () =>
		{
			this.isConnect = true;
			this.connecting = false;
			this.ws.send(this.key);
			if(this.wsOpen != null) this.wsOpen(this);
		};
		this.ws.onmessage = (e) =>
		{
			if(this.onMessage != null) this.onMessage(e, this);
		};
		this.ws.onclose = () =>
		{
			this.isConnect = false;
			this.connecting = false;
			if(this.wsClose != null) this.wsClose(this);
		};
	}
	
	send(msg)
	{
		if(this.isConnect)
		{
			this.ws.send(msg);
		}
	}
	
	close()
	{// 웹소켓 닫기
		if(this.connecting) this.ws.close();
		if(this.wsClose) this.wsClose(this);
	}
	
	closeCh()
	{
		if(this.connecting) this.ws.close();
		if(this.onClose) this.onClose(this);
	}
}

function getCookie(cookieName)
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

function setCookie(cookieName, cookieValue)
{
	document.cookie = cookieName+"="+cookieValue+"; Path=/";
}

function httpGet(theUrl, callback, params)
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
