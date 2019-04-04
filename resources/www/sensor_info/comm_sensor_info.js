//통신모듈
import * as COMM from './CommModule.js';

function addItem(key, on)   //table -> div 수정
{ 
    
    var state = on? "checked": "";
    var eItem = document.createElement("table");
    eItem.id = key;
    eItem.className = 'item';
    eItem.innerHTML = [
        '<tbody><tr><td class="title">',
        key,
        '</td>',
        '<td></td>',
        '<td>',
        '<label class="switch">',
        '<input id="',
        key+"stat",
        '" type="checkbox"',
        state,
        '>',
        '<div class="slider round"></div>',
        '</label>',
        '</td>',
        '<td>',
        '<button class="item-btn" onclick="location.href=`manage_sensor_info.html`">VIEW</button>',
        '</td>',
        '</tr></tbody>',
    ].join("");
    document.getElementById('items').append(eItem);
}


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
     

    var sensorOnOffCh = commModule.createChannel("RealtimeAllSensorOnOffRequest",null, (e)=>
    {
        console.log(e.data);
    });

});
window.onload = function()
{
    
    
}