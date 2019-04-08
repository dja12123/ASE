
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
// table -> div 수정
function addItem(key, on) {
	var state = on ? "checked" : "";
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
		'<input id="stat',
		key,
		'" type="checkbox"',
		state,
		'>',
		'<div class="slider round"></div>',
		'</label>',
		'</td>',
		'<td>',
		'<button class="item-btn" onclick="location.href=\'/sensor_info/sensor_info.html?key=',
		key,
		'\'">VIEW</button>',
		'</td>',
		'</tr></tbody>',
	].join("");
	document.getElementById('items').append(eItem);
	setTotal();
}

// "센서" 지우기
// ＊delItem(String) 센서 키
function delItem(key) {
	document.getElementById(key).remove();
	setTotal();
}

//  "작동상태" 변경(on/off)
// ＊state(String, Boolean) 센서 키, 작동상태(on/off)
function state(key, on) {
	var sensor = document.getElementById("stat"+key);
	if(sensor.checked != on) sensor.checked = on;
}

function listDisconnect() {
    document.getElementById("main").style.opacity = 0.4;
    document.body.innerHTML += 
    '<div id="disconnected" class="disconnected">' +
    '<span>연결 끊김<br/>재접속 시도중</span>' +
    '</div>';
}

// 재접속
function listReconnect() {
    document.getElementById("main").style.opacity = 1;
	document.body.removeChild(document.getElementById("disconnected"));
	removeAllItem();
}
