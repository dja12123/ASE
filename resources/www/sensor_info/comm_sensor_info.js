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

    // 이전데이터 띄우기
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
                    var time = data.time.split("/");
				    setSensorData(new Date(time[0], time[1], time[2], time[3], time[4], time[5]), data.xg, data.yg, data.xa, data.ya, data.za, data.al);
                }
				
			}

        }
        else
        {
            beforeSensorData.close();
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
    var sensorLog = commModule.createChannel("RealtimeSensorLog",()=>
    {
        sensorLog.send(sensorID);
    },(e) =>
    {
        var data = JSON.parse(e.data);
        if(data.result == true)
        {
            console.log("logData:" + data);
            console.log(object.key(data).length);
            if(object.key(data).length>1)
            {
                addLog(data.level, data.time, data.message);
            }
        }
        else
        {
            sensorLog.close();
        }

    });

    window.scrollTo(0,document.body.scrollHeight);
}