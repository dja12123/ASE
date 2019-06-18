window.onload = function()
{
	Notification.requestPermission();
	
	var permission = Notification.permission;
	if(permission == "granted")
		console.log("알림이 허용되었습니다.");
	else if(permission == "denied")
		console.log("알림이 거부되었습니다.");
	
	
    
    // 데스크탑 알림 요청
    var notification = new Notification("DororongJu", options);
    
	function notify(msg) {
    var options = {
        body: msg
    }
	
    // 3초뒤 알람 닫기
    setTimeout(function(){
        notification.close();
    }, 3000);
}

}