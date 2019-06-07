//통신모듈
import * as COMM from '../js/CommModule.js';

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
	// O2SensorListRequestChannel
	var O2sensorListCh = commModule.createChannel("O2SensorListRequest", ()=>
	{
		O2sensorListCh.send("getdata");
	}, (e) =>
	{
		var data = JSON.parse(e.data);
		
		for(var i in data.data) // 센서 수 만큼 반복문
		{
			addItem(data.data[i].id, true);	// 임시로 sensor On 상태
		}
		
		sensorListCh.close();
	});
	
	// Realtime Value of each O2sensor Request
	var O2SensorRealTimeValue = commModule.createChannel("RealtimeAllO2ValueRequest", null, (e) =>
	{
		var data = JSON.parse(e.data);
		updateValue(data.id, data.value);	// 값 업데이트 함수에다 ID와 value를 넘겨줌
		
		
	});
}