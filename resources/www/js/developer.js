/*
서버(데이터 전체) -> 클라이언트(분배) -> 페이지(하나씩)
데이터 리스트를 만듬 -> 데이터 리스트를 띄움
 */

var structItem = [];

window.onload = function () {
    var item = new Object();
    item.state = false;
    item.elem = [];
    for (let i = 0; i < 3; i++) {
        for (let j = 0; j < 6; j++) {
            item.elem[j] = Math.random() * 100;
        }
        var key = Math.floor(Math.random() * 10000);
        structItem[key] = item;

        var on = Math.floor(Math.random()*10)%2;
        addItem(key, on);
    }
}

function maxLengthCheck(object) {
    if (object.value.length > object.maxLength) {
        object.value = object.value.slice(0, object.maxLength);
    }
}

// 형식체크
function formCheck(key) {
    if (isNaN(key)) { console.log("입력된 데이터타입: \"" + typeof (key) + "\""); return 1 }
    else if (key < 0) { console.log("underflow: \"" + key + "\""); return 1 }
}
// 중복체크
function keyCheck(key) {
    if (typeof structItem[key] != "undefined" && structItem[key] != 0) { console.log("중복된 키: \"" + key + "\""); return 1 }
}

// 매개변수 추가
// 데이터(테스트) 검사: 데이터타입, 중복키, 오버플로우, 언더플로우
function clientLoad(input) {
    var key = parseInt(input.value);
    console.log(typeof structItem[key] + ", " + structItem[key]);
    if (formCheck(key)) return 0;
    else if(keyCheck(key)) return 0;

    var elem = [];
    for (let i = 0; i < 6; i++) {
        elem[i] = Math.random() * 100;
    }
    // var on = Math.floor(Math.random()*10)%2;
    var on = document.getElementById("state_check").checked;
    addItem(key, on);
    sensorData(key, false, elem);
}

//key배열 전송
//매개변수 -> sensor 배열로 수정
function sensorData(key, state, elem) {
    var item = new Object();
    item.state = state;
    item.elem = elem;
    structItem[key] = item;
    console.log("Load 완료");
}

function delData(input) {
    var key = parseInt(input.value);
    if (formCheck(key)) return 0;
    if (typeof structItem[key] == "undefined" || structItem[key] == 0) { console.log("비어있는 키: \"" + key + "\""); return 1 }
    delItem(key);
    structItem[key] = 0;
}

function onState(input) {
    var key = parseInt(input.value);
    if (formCheck(key)) return 0;
    state(key, true);
}

function offState(input) {
    var key = parseInt(input.value);
    if (formCheck(key)) return 0;
    state(key, false);
}
