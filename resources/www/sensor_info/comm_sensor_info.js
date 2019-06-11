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
    var sensorID = getParameter("key");
    dataSetKey(sensorID); // manage에서 구현한 함수

    // 이전 센서 데이터 요청
    var beforeSensorData = commModule.createChannel("AllSensorDataRequest", ()=>
    {
        beforeSensorData.send(sensorID + "/" + String(1));
    }, (e) =>
    {
        var data = JSON.parse(e.data);
        if(data.result = true)
        {		
			for(var i in data.sensorData)
			{
				var sensorData = data.sensorData[i];
				var time = sensorData.time.split("/");
				setSensorData(new Date(time[0], time[1], time[2], time[3], time[4], time[5]), sensorData.xa, sensorData.ya, sensorData.za);
			}
        }
        else
        {
            beforeSensorData.close();
        }
    }
    );

    // 이전 데이터 로그 요청e
    var beforeSensorLog = commModule.createChannel("AllSensorLogRequest", ()=>
    {
        beforeSensorLog.send(sensorID + "/" + String(100));
    }, (e) =>
    {
        var data = JSON.parse(e.data);
        if(data.result == true)
        {
			for(var i in data.sensorLog)
			{
				var sensorLog = data.sensorLog[i];
				var time = sensorLog.time.split("/");
				addLog(sensorLog.level, new Date(time[0], time[1], time[2], time[3], time[4], time[5]), sensorLog.message)
			}
        }
        else
        {
            beforeSensorLog.close();
        }
    });
    
	
    
    // 실시간 센서 데이터 요청
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

    // 센서 전원 상태
    var sensorOnOff = commModule.createChannel("RealtimeSensorOnOffRequest",()=>
    {
		sensorOnOff.send(sensorID);
    }, (e)=>
    {
		var data = e.data.split("/");
		if(data[0] == "result" && data[1] != "true")
		{
			sensorOnOff.close();
		}
		/*else if(data[0] == "state")
		{
			setState(data[1] == "true");
		}*/
    });

    // log Bottom
    // 실시간 센서 로그 요청
    var sensorLog = commModule.createChannel("RealtimeLogDataRequest",()=>
    {
        sensorLog.send(sensorID);
    },(e) =>
    {
        var data = JSON.parse(e.data);
		if(data.result != true)
		{
			sensorLog.close();
		}
        if(data.result == true && data.message)
        {
			var time = data.time.split("/");
            addLog(data.level, new Date(time[0], time[1], time[2], time[3], time[4], time[5]), data.message);
        }
    });

    window.scrollTo(0,document.body.scrollHeight);
}