import * as COMM from '/js/CommModule.js';

// 서버로부터 데이터를 받은 모듈
var commModule = new COMM.CommModule(function()
{//최초연결
    console.log("commModule load");
    console.log(commModule.sessionUUID);

	
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
    dataSetKey(sensorID);

    // 이전 센서 데이터 요청
    var beforeSensorData = commModule.createChannel("AllSensorDataRequest", ()=>
    {
        var numbers = String(1);
        beforeSensorData.send(sensorID + "/" + numbers);
    }, (e) =>
    {
        var data = JSON.parse(e.data);
        if(data.result = true)
        {
            console.log(Object.keys(data).length);
			if(Object.keys(data).length > 1)
			{
                for(var i in data.sensorData)   // 센서 수 만큼 반복문
                {
                    var sensorData = data.sensorData[i];
                    console.log(sensorData);
                    var time = sensorData.time.split("/");
                    console.log(data.xg);
				    setSensorData(new Date(time[0], time[1], time[2], time[3], time[4], time[5]), sensorData.xg, sensorData.yg, sensorData.xa, sensorData.ya, sensorData.za, sensorData.al);
                }
				
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
        var numbers = String(1);
        beforeSensorLog.send(sensorID + "/" + numbers);
    }, (e) =>
    {
        var data = JSON.parse(e.data);
        if(data.result = true)
        {
			console.log(data);
            console.log(Object.keys(data).length);
			if(Object.keys(data).length > 1)
			{
                for(var i in data.sensorData)
                {
                    var sensorData = data.sensorData[i];
                    console.log(sensorData);
                    addLog(sensorData.level, new Date(sensorData.time), sensorData.message)
                }

			}

        }
        else
        {
            beforeSensorLog.close();
        }
    }
    );
    
	
    
    // 실시간 센서 데이터 요청
    var sensorData = commModule.createChannel("RealtimeSensorDataRequest",()=>
	{
		sensorData.send(sensorID);
	}, (e) =>
	{
		var data = JSON.parse(e.data);
		if(data.result == true)
		{
			console.log(Object.keys(data).length);
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
		else if(data[0] == "state")
		{
			setState(data[1] == "true");
		}
    });

    // log Bottom
    // 실시간 센서 로그 요청
    var sensorLog = commModule.createChannel("RealtimeLogDataRequest",()=>
    {
        sensorLog.send(sensorID);
    },(e) =>
    {
        console.log(e.data);
        var data = JSON.parse(e.data);
        if(data.result == true)
        {
            console.log("logData:" + data);
            console.log(object.key(data).length);
            if(object.key(data).length>1) 
            {
                addLog(data.level, new Date(data.time), data.message);
            }
        }
        else
        {
            sensorLog.close();
        }

    });

    window.scrollTo(0,document.body.scrollHeight);
}