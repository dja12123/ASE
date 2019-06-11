window.onload = function()
{
    var hw = document.getElementById('hw');
    hw.addEventListener('click', function()
	{
        alert('Hello world');
    })


	// 서비스 워커 등록
	if ('serviceWorker' in navigator)
	{
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
