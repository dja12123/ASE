
// 알림을 보내는 함수
function mailMe()
{
  var parameter_noti = {
	title:"[공유] Web Notification 공유",
	icon:"http://img.naver.net/static/www/up/2014/0123/mat_19165764t.jpg",
	body:"안녕하세요 김대현입니다. 오늘은 재밌는 Web Notification을 공유하려고 합니다~~~~"
  };
  
  if (!"Notification" in window) {
    alert("This browser does not support desktop notification");
  }
  else if (Notification.permission === "granted") {
    var notification = new Notification(parameter_noti.title,{
    	icon:parameter_noti.icon, 
    	body:parameter_noti.body
    });
  }
  else if (Notification.permission !== 'denied') {
    Notification.requestPermission(function (permission) {
      if(!('permission' in Notification)) {
        Notification.permission = permission;
      }
      if (permission === "granted") {
        var notification = new Notification(parameter_noti.title,{
        	icon:parameter_noti.icon,
        	body:parameter_noti.body
        });
      }
    });
  }
}