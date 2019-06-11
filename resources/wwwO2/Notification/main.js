window.onload = function()
{
    var hw = document.getElementById('hw');
    hw.addEventListener('click', function()
	{
        alert('Hello world');
    })

	console.log("service worker install 1");
	console.dir(navigator);
	// 서비스 워커 등록
	if ('serviceWorker' in navigator)
	{
		console.log("service worker install 2");
		window.addEventListener('load', function()
		{
			navigator.serviceWorker.register('/sw.js').then(function(registration)
			{
				// Registration was successful
				console.log('ServiceWorker registration successful with scope: ', registration.scope);
			}, function(err)
			{
				// registration failed :(
				console.log('ServiceWorker registration failed: ', err);
			});
		});
	}
}
