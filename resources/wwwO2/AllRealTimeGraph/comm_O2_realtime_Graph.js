// 통신모듈
import * as COMM from '/js/CommModule.js';

// 서버로부터 데이터를 받은 모듈
var commModule = new COMM.CommModule(function()
{//최초연결
},
// 연결 끊김
function disconnect(){
    infoDisconnect();
},
// 재접속
function reconnect(){
    infoReconnect();
});

window.onload = function()
{
	// O2SensorListRequestChannel
	var O2sensorListCh = commModule.createChannel("RealtimeAllO2ValueSender", 
}