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
	
    var sensorID = getParameter("key"); // 문제가 되는 구간
	
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

    // 이전 데이터 로그 요청
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
				setSensorData(new Date(time[0], time[1], time[2], time[3], time[4], time[5]), data.xa, data.ya, data.za);
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
	
	
	// 센서 상태 정보 업데이트
	var statInfo = commModule.createChannel("RealtimeAllSensorSafetyRequest", null, (e) =>	// 선용이한테 키값 받기
    {
        var data = JSON.parse(e.data);
		var dataSplit = data.split('/'); 
		updateValue(dataSplit[0], dataSplit[1]);
    });
	
	// 센서 아이디 서버에 전송
	var sendSensorID = commModule.createChannel("SensorSetting", () =>
	{
		var rawData = giveNick().split('/'); 
		var byteArray = new ArrayBuffer(11);
		// 2바이트(0x0010), 4바이트(센서ID), 값(5바이트)
		byteArray[0] = 0x0010;
		byteArray[2] = rawData[0];
		byteArray[6] = rawData[1];
		
		console.log(byteArray);
		sendSensorID.send(byteArray);
		
	}
	);

    window.scrollTo(0,document.body.scrollHeight);
}