
//"로그" 삭제
//로그가 있으면 삭제
function delLog() {
    elem = document.getElementById("log" + logNum);
    if (elem != null) elem.remove();
}

// 자동 스크롤: 현재 스크롤 위치
function getCurrentScrollPercentage() {
    return (window.scrollY + window.innerHeight) / document.body.clientHeight * 100;
}

// 날짜 포멧: 두자리
function getFormatDate(date) {
    return (date < 10) ? '0' + date : date;
}

// 페이지 시작시 표시할 "센서 키"
// * dateSetKey(String) // 센서 키
function dataSetKey(key) {
    //입력된 키 
    document.title = "sensor " + key;
    document.getElementById("state_name").innerHTML = key;
}

// 센서의 상태 표시 및 변경 "작동상태"
// ＊setState(Boolean) // 작동상태(on/off)
function setState(on) {
    console.log(typeof on);
    document.getElementById("state").innerHTML = ((on) ? "작동중" : "중지");
    //asdf
}

// "센서 데이터 값" 설정
// ＊setElem(Date, Number...) // 날짜, 데이터 6개
function setSensorData(time, xg, yg, xa, ya, za, al) {
	setDate(time);
	document.getElementById("slopX").innerHTML = xg.toFixed(4);
	document.getElementById("slopY").innerHTML = yg.toFixed(4);
	document.getElementById("accX").innerHTML = xa.toFixed(4);
	document.getElementById("accY").innerHTML = ya.toFixed(4);
	document.getElementById("accZ").innerHTML = za.toFixed(4);
	document.getElementById("alti").innerHTML = al.toFixed(4);
}

// "데이터 수집 시간" 설정
// * setDate(Date) // 날짜
function setDate(date) {
    document.getElementById('uptime').innerHTML =
	(date.getFullYear()+"년 "+
	date.getMonth()+"월 "+
	date.getDate()+"일 "+
	date.getHours()+"시 "+
	date.getMinutes()+"분 "+
	date.getSeconds()+"초");
}

var logNum = 0;
// "로그" 추가
// * addLog(string, Date, String) // NFO, 날짜, 메세지
function addLog(level, date, msg) { //100개 제한, 원형큐
    delLog();
    var eLog = document.createElement("div");
    eLog.id = "log" + logNum;
    eLog.className = "log";
    eLog.innerHTML = [
    "[" +
    level +
    "][" +
    date.getFullYear() +"/"+
	getFormatDate((date.getMonth()+1)) +"/"+
	getFormatDate(date.getDate()) +" "+
	getFormatDate(date.getHours()) +":"+
	getFormatDate(date.getMinutes()) +":"+
	getFormatDate(date.getSeconds()) +
    "] " +
    msg
    ];
    // 자동스크롤
    if(getCurrentScrollPercentage() > 95){
        document.getElementById('log').append(eLog);
        window.scrollTo(0, document.body.scrollHeight);
    }
    else
        document.getElementById('log').append(eLog);
    logNum++;
    if (logNum > 99) logNum = 0; //로그갯수 제한
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