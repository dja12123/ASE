            //통신모듈
import * as COMM from 'CommModule.js';





// 서버로부터 데이터를 받은 모듈
var commModule = new COMM.CommModule(function()
{
    console.log("commModule load");
    console.log(commModule.sessionUUID);

    var sensorListCh = commModule.createChannel("SensorListRequest",null, (e)=>
    {
        console.log("data:"+e.data);
        var data = JSON.parse(e.data);
        console.log(data);
        console.log(data.result);
        console.log(data.time);
        console.log(data.xg);
        console.log(data.yg);
        console.log(data.xa);
        console.log(data.za);
        console.log(data.al);
        
    });

    var sendSensorID = commModule.createChannel("RealtimeSensorDataRequest",()=>
	{
		sendSensorID.send("1001");
	}, (e)=>
	{
		console.log("data:"+e.data);
		
		var data = JSON.parse(e.data);
		console.log(data);
	});

    

    var sensorOnOffCh = commModule.createChannel("RealtimeAllSensorOnOffRequest",null, (e)=>
    {
        console.log(e.data);
    });
});
            window.onload = function()
            {
                
                
            }
        