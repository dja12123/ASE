//통신모듈
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
	var O2sensorListCh = commModule.createChannel("O2SensorListRequest", null, (e) =>
	{
		var data = JSON.parse(e.data);

		for(var i in data.data) // 센서 수 만큼 반복문
		{
			addItem(data.data[i].id, true);
		}
		sensorListCh.close();
	});
}