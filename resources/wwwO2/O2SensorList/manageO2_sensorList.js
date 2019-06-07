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
	else msg = "확인된 센서가 없습니다"
	document.getElementById("total").innerHTML = msg;
}

// "센서" 추가
// * addItem(String, Boolean) 센서 키, 작동상태(on/off)
function addItem(key, on) {
	var state = on ? "checked" : "";
	var eItem = document.createElement('span');
	//eItem.id = key; //키값 중복
	eItem.className = 'item'; 
	eItem.innerHTML = [
		'<button type="button" class="btn btn-light" style="display: inline-block" id="b', key,'">',
				key, ': <span class="badge badge-light" id="',key,'"></span>',
				'<span id="ss', key,'"></span>',
		'</button>',
			//'<span id="SensorStat" />'
	].join("");
	document.getElementById('items').append(eItem);
	setTotal();
}


function updateValue(key, data)	{ // 센서 아이디에 따른 값 셋팅 함수
	var uniqueID=key;
	var getData=data*100;
	
	getData=Number.parseFloat(getData).toFixed(2);
	
	var ChemicalStatus=document.getElementById(uniqueID);
	
	ChemicalStatus.innerHTML='';
	stats= getData + '%';
	ChemicalStatus.insertAdjacentHTML('beforeend',stats);
	checkSafety(key, getData);
	
	//id랑 비교하여 데이터 값 업데이트
	
}

// 버튼 색깔 변경
function changeButtonColor(key, value)
{
	var ButtonID='b'+key;
	var ButtonColorStatus=document.getElementById(ButtonID);
	var content;
	ButtonColorStatus.innerHTML='';
				
				if(value>=21)
					content= 	'<button type="button" class="btn btn-success" style="display: inline-block" id="',ButtonID,'">',
									key, ': <span class="badge badge-light" id="',key,'"></span>',
									'<span id="ss', key,'"></span>',
								'</button>';
				else if(value>=18 && value<21)
					content= '<button type="button" class="btn btn-warning" style="display: inline-block" id="',ButtonID,'">',
									key, ': <span class="badge badge-light" id="',key,'"></span>',
									'<span id="ss', key,'"></span>',
								'</button>';
				else if(value<18)
					content= '<button type="button" class="btn btn-danger" style="display: inline-block" id="',ButtonID,'">',
									key, ': <span class="badge badge-light" id="',key,'"></span>',
									'<span id="ss', key,'"></span>',
								'</button>';
				
				ButtonColorStatus.insertAdjacentHTML('beforeend',content);
	
}

function checkSafety(key, value) {
				var SensorStatusID= 'ss' + key;
				var SensorStatus=document.getElementById(SensorStatusID);
				var content;
				SensorStatus.innerHTML='';
				
				if(value>=21)
					content= '<span class="badge badge-success" style="display: inline-block">Safe/안전</span> </h5>';
				else if(value>=18 && value<21)
					content= '<span class="badge badge-warning" style="display: inline-block">Warning/주의</span> </h5>';
				else if(value<18)
					content= '<span class="badge badge-danger" style="display: inline-block">Danger/경보</span> </h5>';
				
				SensorStatus.insertAdjacentHTML('beforeend',content);
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
