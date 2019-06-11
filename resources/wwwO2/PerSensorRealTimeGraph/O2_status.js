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

// "센서" 추가
// * addGraphValue(String, Boolean) 센서 키, 작동상태(on/off)
function addGraphValue(key, on) {
	var state = on ? "checked" : "";
	var eItem = document.createElement('span');

	//eItem.id = key; //키값 중복
	eItem.className = 'item'; 
	eItem.innerHTML = [
		'<button type="button" class="btn btn-light" style="display: inline-block" id="b',key,'">',
				key, ': <span class="badge badge-light" id="',key,'"></span>',
				'<span id="ss', key,'"></span>',
		'</button>',
			//'<span id="SensorStat" />'
	].join("");
	document.getElementById('items').append(eItem);
	setTotal();
}

function initCanvas()	{
	var ctx = document.getElementById('canvas').getContext('2d');
	window.myLine = new Chart(ctx, config);
}

function updateValue(key, xTime, data)	{ // 센서 아이디에 따른 값 셋팅 함수
	var uniqueID=key;
	var getData=data*100;
	getData=Number.parseFloat(getData).toFixed(2);
	// Set millisecond 자릿수 to 2
	
	var StringDate= getData.toISOString();
	console.log(StringDate);
	
	//var ChemicalStatus=document.getElementById(ButtonValue);
	
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
	//ChemicalStatus.innerHTML='';
	//stats= getData + '%';
	//ChemicalStatus.insertAdjacentHTML('beforeend',stats);
	//changeButtonColor(key, getData);
	
	//id랑 비교하여 데이터 값 업데이트
	
}

// 버튼 색깔 변경
/*
function changeButtonColor(key, value)	{
	var ButtonID=document.getElementById("ButtonStat");
	var ButtonColorStatus=document.getElementById(ButtonID);
				
				if(value>=21)
					ButtonColorStatus.className="btn btn-success"
				else if(value>=18 && value<21)
					ButtonColorStatus.className="btn btn-warning"
				else if(value<18)
					ButtonColorStatus.className="btn btn-danger"
				
	
}	
*/
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
					text: '공기 농도 측정도'
				},
				scales: {
					xAxes: [{
						display: true,
						//type: 'time',
						scaleLabel: {
							display: true,
							labelString: 'time'
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
							min: 15,
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
