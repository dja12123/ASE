
// "로그" 삭제
// 로그가 있으면 삭제
function removeLog(num) {
    elem = document.getElementById("log" + num);
    if (elem != null) elem.remove();
    else return 1;
}

// 날짜 포멧: 두자리
function getFormatDate(date) {
    return (date < 10) ? '0' + date : date;
}

// 모든 로그 삭제
// 재접속 시도: infoReconnect()
function removeAllLogs() {
    for (let i = 0; i <= logMax; i++) {
        if(removeLog(i))
            break;
    }
    logNum = 0;
}


// 페이지 시작시 표시할 "센서 키"
// * dateSetKey(String) // 센서 키
function dataSetKey(key) {
    document.title = "SENSOR " + key;
    document.getElementById("state_name").innerHTML = key;
}

// 센서의 안전 or 경고 표시
function setState(key) {
    docu'<button type="button" class="btn btn-light" style="display: inline-block" id="b',key,'">',
				key, ': <span class="badge badge-light" id="',key,'"></span>',
				'<span id="ss', key,'"></span>',
		'</button>',
	].join("");
	
	);
}


function updateValue(key, state)	{
	var uniqueID=key;
	var ChemicalStatus=document.getElementById(uniqueID);
	
	ChemicalStatus.innerHTML='';
	ChemicalStatus.insertAdjacentHTML('beforeend',stats);
	changeButtonColor(key, state);
	checkSafety(key, state);
	//id랑 비교하여 데이터 값 업데이트
	
}


function checkSafety(key, state) {
				var SensorStatusID= 'ss' + key;
				var SensorStatus=document.getElementById(SensorStatusID);
				var content;
				SensorStatus.innerHTML='';
				
				if(state==0)
					content= '<span class="badge badge-success" style="display: inline-block">Safe/안전</span> </h5>';
				else if(state==1)
					content= '<span class="badge badge-danger" style="display: inline-block">Danger/경고</span> </h5>';
				
				SensorStatus.insertAdjacentHTML('beforeend',content);
			}

// 버튼 색깔 변경
function changeButtonColor(key, state)
{
	var ButtonID='b'+key;
	var ButtonColorStatus=document.getElementById(ButtonID);
				
				if(state==0)
					ButtonColorStatus.className="btn btn-success";
				else if(state==1)
					ButtonColorStatus.className="btn btn-danger"
				
	
}

// "센서 데이터 값" 설정
// ＊setElem(Date, Number...) // 날짜, 데이터 6개
function setSensorData(date, xa, ya, za) {
	setDate(date);
	document.getElementById("accX").innerHTML = xa.toFixed(4);
	document.getElementById("accY").innerHTML = ya.toFixed(4);
	document.getElementById("accZ").innerHTML = za.toFixed(4);
}

// "데이터 수집 시간" 설정
// * setDate(Date) // Date 객체
function setDate(date) {
    document.getElementById('uptime').innerHTML =
	(date.getFullYear()+"년 "+
	date.getMonth()+"월 "+
	date.getDate()+"일 "+
	date.getHours()+"시 "+
	date.getMinutes()+"분 "+
	date.getSeconds()+"초");
}

const logMax = 99;
var logNum = 0;
// "로그" 추가
// * addLog(string, Date, String) // NFO, Date 객체, 메세지
function addLog(level, date, msg) { //100개 제한, 원형큐
    removeLog(logNum);
    var eLog = document.createElement("div");
    eLog.id = "log" + logNum;
    eLog.className = "log";
    eLog.innerHTML = [
    '<span class="log_info">[' +
    level +
    "][" +
    date.getFullYear() +"/"+
	getFormatDate(date.getMonth()) +"/"+
	getFormatDate(date.getDate()) +" "+
	getFormatDate(date.getHours()) +":"+
	getFormatDate(date.getMinutes()) +":"+
	getFormatDate(date.getSeconds()) +
    '] </span> <span class="log_msg">' +
    msg +
    '</span>'
    ];
    // 자동 스크롤
    var log = document.getElementById('log');
    log.append(eLog);
    log.scrollTop = log.scrollHeight;
    logNum++;
    if (logNum > logMax) logNum = 0; //로그개수 제한
}

// 연결 끊김
function infoDisconnect() {
    document.getElementById("main").style.opacity = 0.4;
    document.body.innerHTML += 
    '<div id="disconnected" class="disconnected">' +
    '<span>연결 끊김<br/>재접속 시도중</span>' +
    '</div>';
    
}

// 재접속
function infoReconnect() {
    document.getElementById("main").style.opacity = 1;
    document.body.removeChild(document.getElementById("disconnected"));
    removeAllLogs();
}


function getParameter(name){
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
