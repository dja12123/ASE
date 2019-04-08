//통신모듈
import * as COMM from '/js/CommModule.js';

// 서버로부터 데이터를 받은 모듈
var commModule = new COMM.CommModule(function()
{
	console.log("commModule load");
	console.log(commModule.sessionUUID);

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
	var tatal=0;
	var sensorListCh = commModule.createChannel("SensorListRequest", null, (e) =>
	{
		console.log("data:" + e.data);
		var data = JSON.parse(e.data);
		console.log(data);
		console.log("count:" + data.count)
		total= total+data.count;
		for(var i in data.data) // 센서 수 만큼 반복문
		{
			addItem(data.data[i].id, data.data[i].on);
		}
		sensorListCh.close();
	});
	
	var allSensorOnOff = commModule.createChannel("RealtimeAllSensorOnOffRequest", null, (e) =>
	{
		console.log("data:" + e.data);
		var data = e.data.split("/");
		state(data[0], data[1] == "true");
	});

	var sensorAddRemove = commModule.createChannel("RealtimeSensorAddRemoveRequest", null, (e) =>
	{
		console.log(e.data);
		console.log("data:" + e.data);
		var data = e.data.split("/");
		//console.log("sensor status: "+data[1]);
		if(data[1] == "true")
		{
			total = total+1;
			addItem(data[0], false);
		}
		else
		{
			delItem(key[0]);
		}
	});

	setTotal(total);
}
