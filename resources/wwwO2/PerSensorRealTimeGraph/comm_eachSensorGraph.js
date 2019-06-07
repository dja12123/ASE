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
	var sensorID = getParameter("key"); //GET 방식으로 붙인 key 값을 가져옴

	var PreviouseDataRequestCh = commModule.createChannel("PreviouseO2DataRequest", ()=>	// 센서의 이전 데이터 요청
	{
		PreviouseDataRequestCh.send(sensorID + "/" + String(50)); 
	}, (e) =>
	{
		var data = JSON.parse(e.data);
		
		
		console.log(data);
        /*if(data.result = true)
		{
			
			// 진우오빠가 만든 홈페이지 셋팅 함수의 인자에 맞게 넣어줌 initgraph()
		}*/
	});
	
	
	//var perSensorRealTimeCh = 	// 센서 실시간 데이터 요청 채널
	
}