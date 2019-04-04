//통신모듈
import * as COMM from '../CommModule.js';



var commModule = new COMM.CommModule(function()
{
    console.log("commModule load");
    console.log(commModule.sessionUUID);
    // 서버로 데이터를 보내는 모듈
    var sendID = commModule.createChannel("RealtimeSensorDataRequest",()=>
	{
		sendID.send("1001");
	}, (e)=>
	{
		console.log("data:"+e.data);
		
		var data = JSON.parse(e.data);
        console.log(data.result);
        console.log(data.time);
        console.log(data.xg);
        console.log(data.yg);
        console.log(data.xa);
        console.log(data.za);
        console.log(data.al);
    });

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
        console.log("count:" + data.count)
        console.log("id:" + data.data[0].id)
       for(var i in data.data)// 센서 수 만큼 반복문
        {
            addItem(data.data[i].id, data.data[i].on);
           
        }
        sensorListCh.close();

        
    });
     

    var sensorOnOffCh = commModule.createChannel("RealtimeAllSensorOnOffRequest",null, (e)=>
    {
        console.log(e.data);
    });

});
window.onload = function()
{
    
    
}