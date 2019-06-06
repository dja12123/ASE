//realtime graph communication module
import * as COMM from '/js/CommModule.js';

// 서버로부터 데이터를 받은 모듈
var commModule = new COMM.CommModule(function()
{
},
// 연결 끊김
function disconnect(){
    listDisconnect();
},
// 재접속
function reconnect(){
    listReconnect();
});

window.onload = function()
{
	// input realtime graph data channel
	
	var sensorData = commModule.createChannel("RealtimeSensorDataRequest",()=>
	{
		sensorData.send(sensorID);
	}, (e) =>
	{
		var data = JSON.parse(e.data);
		if(data.result == true)
		{
			if(Object.keys(data).length > 1)
			{
				var time = data.time.split("/");
				setSensorData(new Date(time[0], time[1], time[2], time[3], time[4], time[5]), data.xg, data.yg, data.xa, data.ya, data.za, data.al);
			}
		}
		else
		{
			sensorData.close();
		}
	});
}
