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

function sendNick(id, result)
	{
		console("inputSendNick");
		console.log(id);
		console.log(result);
		
		if(typeof str == "undefined" || str == null || str == "")
            console.log("data undefined");
        else
		{
			var NickJson = 
			{
				"settingKey": "sensorAlias",
				"settingValue" : {
				"sensorID" : id,
				"sensorAlias" : result
				}
			};
			console.log("sendNick" + NickJson);
			sendSensorID.send(NickJson);
		}
	}

window.onload = function()
{
	var sendSensorID = commModule.createChannel("SensorSetting");
	console.log("NickCh");
	
	
}

