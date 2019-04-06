var total = 0;

//add, del함수 실행시 total체크
function setTotal()
{
	var msg;
	if(total > 0) msg = total + "개의 센서 확인";
	else msg = "확인된 센서가 없습니다"
	document.getElementById("total")
		.innerHTML = msg;
}

// "센서" 추가
// * addItem(string, boolean) 센서 키, 작동상태(on/off)
function addItem(key, on) //table -> div 수정
{
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
		'<button class="item-btn" onclick="location.href=\'sensor_info.html?key=',
		key,
		'\'">VIEW</button>',
		'</td>',
		'</tr></tbody>',
	].join("");
	document.getElementById('items')
		.append(eItem);
}

// "센서" 지우기
// ＊delItem(string) 센서 키
function delItem(key)
{
	total--;
	setTotal();
	document.getElementById(key)
		.remove();
}

//  "작동상태" 변경(on/off)
// ＊state(string, boolean) 센서 키, 작동상태(on/off)
function state(key, on)
{
	var sensor = document.getElementById("stat"+key);
	if(sensor.checked != on) sensor.checked = on;
}