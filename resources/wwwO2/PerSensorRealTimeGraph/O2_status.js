var targetKey;
//var result;

function dataSetKey(key) {
    targetKey=key;
}


// 모든 센서 삭제
function removeAllItem() {
	var items = document.getElementById("items");
	while (items.hasChildNodes())
		items.removeChild(items.firstChild);
	setTotal();
}

// 센서 개수 체크
function setTotal() {
	var total = document.getElementById("items").childElementCount;
	var msg;
	if(total > 0) msg = total + "개의 센서 확인";
	else msg = "확인된 센서가 없습니다";
	document.getElementById("total").innerHTML = msg;
}

function initCanvas()	{
	var ctx = document.getElementById('canvas').getContext('2d');
	window.myLine = new Chart(ctx, config);
}

/*
function giveNick()	{
	var id=targetKey;
	
	var btn = document.getElementById("updateNick");
	
	btn.addEventListener("click",function()	{
		var result;
		var firLet, rest="";
		var input = document.getElementById("input_nick").value;
		var nickname=input.split("");
		var english=/^[A-Za-z]*$/;

		
		for (var i=0 ; i < input.length ; i++)	{ 
			var testwd = nickname[i]; 

			//firLet = testwd.substr(0,1);
			//rest   = testwd.substr(1, testwd.length -1);

			if(i==0)
				firLet=nickname[i];
			else if(i<input.length && i>0)
				rest+=nickname[i];
		}
		
		rest=parseInt(rest);
		console.log(rest);
		if(english.test(firLet)==true && isNaN(rest)==false)
		{
			//rest=rest.toString();
			result	= firLet + rest ;
			console.log(result);
			sendNick(id, result);
		}
		else
		{
			nickname=null;
			input=null;
		}
	});
}
*/
var sendNick;

function callFunc(callback)
{
	 sendNick = callback;
	
}


function giveNick()	{
	var id=targetKey;
	var result;
	var firLet, rest="";
	var input = document.getElementById("input_nick").value;
	var nickname=input.split("");
	var english=/^[A-Za-z]*$/;

		
		for (var i=0 ; i < input.length ; i++)	{ 
			var testwd = nickname[i]; 

			//firLet = testwd.substr(0,1);
			//rest   = testwd.substr(1, testwd.length -1);

			if(i==0)
				firLet=nickname[i];
			else if(i<input.length && i>0)
				rest+=nickname[i];
		}
		
		rest=parseInt(rest);
		
		if(english.test(firLet)==true && isNaN(rest)==false)
		{
			//rest=rest.toString();
			result	= firLet + rest ;
			sendNick(id, result);
		}
		else
		{
			nickname=null;
			input=null;
		}
}

function updateButtonState(state)	{
	changeButtonColor(state);
	checkSafety(state);
}

function updateValue(key, xYear,xMonth,xDay,xHour,xMin,xSec,xMSec, data)	{ // 센서 아이디에 따른 값 셋팅 함수
	var uniqueID=key;
	var getData=data*100;
	var xTime= xYear+"-"+xMonth+"-"+xDay+ " " + xHour +":"+ xMin +":"+ xSec +"."+ xMSec;
	var SIDText='<h4> Sensor ID : '+uniqueID+'</h4>';
	getData=Number.parseFloat(getData).toFixed(2);
	// Decimal set to 2nd place
	
	
	var SID=document.getElementById("SID");
	SID.innerHTML='';
	SID.insertAdjacentHTML('beforeend',SIDText);
	
	var ChemicalStatus=document.getElementById("ButtonValue");
	
	if (config.data.datasets.length > 0) {
			config.data.labels.push(xTime);
			config.data.datasets[0].data.push(getData);
		}
		
	 if (config.data.datasets[0].data.length > 20) {
		config.data.labels.shift();
		config.data.datasets.forEach(dataset => {
			dataset.data[0] = dataset.data[1];
			dataset.data.splice(1, 1);
		})
    }
		window.myLine.update();
	ChemicalStatus.innerHTML='';
	stats= getData + '%';
	ChemicalStatus.insertAdjacentHTML('beforeend',stats);
	
	//id랑 비교하여 데이터 값 업데이트
	
}

// 버튼 색깔 변경
function changeButtonColor(state)	{
	var ButtonID=document.getElementById("ButtonStat");
				
				if(state==1)
					ButtonID.className="btn btn-success"
				else if(state==2)
					ButtonID.className="btn btn-warning"
				else if(state==3)
					ButtonID.className="btn btn-danger"
				
	
}	

function checkSafety(state) {
				var SensorStatus=document.getElementById("info_data");
				var content;
				SensorStatus.innerHTML='';
				
				if(state==1)
					content= '<span class="badge badge-success" style="display: inline-block">Safe/안전</span> </h5>';
				else if(state==2)
					content= '<span class="badge badge-warning" style="display: inline-block">Warning/주의</span> </h5>';
				else if(state==3)
					content= '<span class="badge badge-danger" style="display: inline-block">Danger/경보</span> </h5>';
				
				SensorStatus.insertAdjacentHTML('beforeend',content);
			}

// Graph		
		var color = Chart.helpers.color;
		var config = {
			type: 'line',
			data: {
				datasets: [{
					label: 'O2 Level',
					backgroundColor: color(window.chartColors.red).alpha(0.5).rgbString(),
					borderColor: window.chartColors.red,
					fill: false,
					lineTension: 0
				}]
			},
			options: {
                responsive: true,
				title: {
					display: true,
					text: '산소 농도 측정도'
				},
				scales: {
					xAxes: [{
						display: true,
						//type: 'time',
						scaleLabel: {
							display: true,
							labelString: 'time'
						},
						gridLines: {
							display:false
						},
						ticks:{
							autoskip: true
						},
						time:	{
							displayFormats:	{
								quarter: 'h:mm:ss.SSS'
							}
						}
					}],
					yAxes: [{
						display: true,
						scaleLabel: {
							display: true,
							labelString: 'value'
						},
						ticks:	{
							min: 5,
							max: 25,
							stepSize: 0.5,
							fixedStepSize: 0.5
						}
					}]
				},
				
				tooltips: {
					mode: 'nearest',
					intersect: false
				},
				hover: {
					mode: 'nearest',
					intersect: false
				}
			}
		};

		// Automatically add points in the graph every second.
		//window.setInterval(updateValue,1000);
		

			
function getParameter(name){ // 키값 가지고 오는 함수
    search=location.search;
    if(!search){
        //파라미터가 하나도 없을때
        document.write("에러 출력 텍스트");
        return false;
    }
 
    search=search.split("?");
    data=search[1].split("=");
    if(search[1].indexOf(name)==(-1) || data[0]!=name){
        //해당하는 파라미터가 없을때.
        return "";
        return;
    }
    if(search[1].indexOf("&")==(-1)){
        //한개의 파라미터일때.
        data=search[1].split("=");
        return data[1];
    }else{
    //여러개의 파라미터 일때.
    data=search[1].split("&"); //엠퍼센트로 자름.
    for(i=0;i<=data.length-1;i++){
        l_data=data[i].split("=");
        if(l_data[0]==name){
            return l_data[1];
            break;
        }else continue;
        }
    }
}
// *연결 끊김
function listDisconnect() {
    document.getElementById("main").style.opacity = 0.4;
    document.body.innerHTML += 
    '<div id="disconnected" class="disconnected">' +
    '<span>연결 끊김<br/>재접속 시도중</span>' +
    '</div>';
}

// *재접속
function listReconnect() {
    document.getElementById("main").style.opacity = 1;
    document.body.removeChild(document.getElementById("disconnected"));
    removeAllItem();
}

// *로그인 세션
// id: 36글자, string
// pw: 16글자, string 
function loginRequest() {
    document.getElementById("main").style.opacity = 0.4;
    document.body.innerHTML += 
    '<div id="loginSess" class="loginSess"> <div class="login">' +
    // *form action 서버 작성
    '<form action="" id="loginForm">' +
    '<div class="input_form"> <span id="error" class="error"></span> </div>' +
    '<div class="input_form"> <input id="id" class="id" type="text" autocomplete="off" onkeydown="if(event.keyCode==13) {login()}" maxlength="36" placeholder="아이디"> </div>' +
    '<div class="input_form"> <input id="pw" class="pw" type="password" onkeydown="if(event.keyCode==13) {login()}" maxlength="16" placeholder="비밀번호"> </div>' +
    '<div class="input_form"> <input class="login_btn" type="button" onclick="login()" value="로그인"> </div>' +
    '</form> </div> </div>';
}

// *로그인 id, pw 전송
function login() {
    if(inputId == '') {
        document.getElementById('id').focus();
		document.getElementById("error").innerHTML = "아이디를 입력해주세요.";
    } else if(inputPw == '') {
        document.getElementById('pw').focus();
		document.getElementById("error").innerHTML = "비밀번호를 입력해주세요.";
    } else
		document.getElementById("loginForm").submit();
}

// *로그인 성공
function loginSucceed() {
    document.getElementById("main").style.opacity = 1;
    document.body.removeChild(document.getElementById("loginSess"));
}

// *로그인 실패
function loginFailed() {
	document.getElementById('pw').value = "";
    document.getElementById("error").innerHTML = "아이디, 비밀번호를 다시 확인해주세요.";
}
