package ase.web.httpServer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.content.Cookie;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;
import org.nanohttpd.util.ServerRunner;

import ase.console.LogWriter;
import ase.fileIO.FileHandler;
import ase.sensorReadServer.ServerCore;
import ase.web.webSocket.WebSession;
import ase.web.webSocket.WebSessionManager;

public class HTTPServer extends NanoHTTPD
{
	private static final Logger logger = LogWriter.createLogger(HTTPServer.class, "HTTPServer");
	
	public static final String rootDirectory = FileHandler.getExtResourceFile("www").toString();
	private static final String PROP_SESSION_COOKIE_TIMEOUT = "SessionCookieTimeoutSecond";
	public static final String WEB_RES_DIR = "/www";
	private static final String CONTROL_GET_UUID_REQUEST = "control_get_uuid";
	private static final String CONTROL_GET_UUID_REQUEST_date = "date";
	private static final DateFormat REQ_COOKIE_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd/hh/mm/ss");
	static
	{
		REQ_COOKIE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
	private final WebSessionManager webSessionManager;
	private int sessionCookieTimeout;
	private Thread serviceThread;
	
	public HTTPServer(int port, WebSessionManager webSessionManager)
	{
		super(port);
		this.webSessionManager = webSessionManager;
	}

	private static Response serveImage(MIME_TYPE imageType, String dir)
	{
		String imageTypeStr = imageType.toString();
		
		return Response.newFixedLengthResponse(Status.OK, imageTypeStr, FileHandler.getResInputStream(dir), -1);
	}
	
	private static Response serveStrFile(MIME_TYPE mimeType, String dir)
	{
		return Response.newFixedLengthResponse(Status.OK, mimeType.toString(), FileHandler.getResInputStream(dir), -1);
	}
	
	private static Response serveError(Status status, String errorStr)
	{
		return Response.newFixedLengthResponse(status, errorStr, "");
	}

	@Override
	public Response serve(IHTTPSession request)
	{
		Method method = request.getMethod();
		String uri = request.getUri();

		//responseSocketHandler.openWebSocket(session); //소켓 세션
		
		System.out.println(method + " '" + uri + "' ");

		// 웹서비스 할 때 필요한 파일 스트림 모듈로 만들기(fileIO 패키지)
		// StringBuffer 적극 사용
		// url이용해서 어떤 요청인지 구분 ->
		// refer::
		// https://github.com/Teaonly/android-eye/blob/master/src/teaonly/droideye/TeaServer.java
		
		//System.out.println("root >> " + rootDirectory);
		//
		String msg = "";

		if (uri.startsWith("/"))
		{ // Root Mapping
			if(method == Method.GET)
			{
				switch(uri)
				{
				case "/"+CONTROL_GET_UUID_REQUEST:
					return this.serviceUUID(request);
				}
			}
			
			String dir = WEB_RES_DIR+uri;
			if(!FileHandler.isExistResFile(dir))
			{
				return HTTPServer.serveError(Status.NOT_FOUND, "Error 404: File not found");
			}
			
			int pos = uri.lastIndexOf( "." );
			
			if(pos == -1)
			{
				return HTTPServer.serveError(Status.BAD_REQUEST, "Error 400: Bad Request");
			}
			String ext = uri.substring( pos + 1 );
			
			String sessionUIDStr = getCookie(request, WebSessionManager.COOKIE_KEY_SESSION);
			UUID sessionUID;
			if(sessionUIDStr != null)
			{
				sessionUID = UUID.fromString(sessionUIDStr);
				//setCookie(request, WebSessionManager.COOKIE_KEY_SESSION, sessionUIDStr, this.sessionCookieTimeout);
				WebSession s = this.webSessionManager.sessionMap.getOrDefault(sessionUID, null);
				if(s!= null)
				{
					System.out.println("존재하는 세션에 대한 요청"+s.toString());
				}
			}
			
			switch(ext)
			{
			case "html":
				return serveStrFile(MIME_TYPE.MIME_HTML, dir);
			case "jpg":
				return HTTPServer.serveImage(MIME_TYPE.MIME_JPEG, dir);
			case "png":
				return HTTPServer.serveImage(MIME_TYPE.MIME_PNG, dir);
			case "js":
				return serveStrFile(MIME_TYPE.MIME_JS,  dir);
			case "css":
				return serveStrFile(MIME_TYPE.MIME_CSS,  dir);
			default:
				return serveStrFile(MIME_TYPE.MIME_PLAINTEXT,  dir);
				
			}
		}
		
		return Response.newFixedLengthResponse(msg);
	}
	
	private Response serviceUUID(IHTTPSession request)
	{
		logger.log(Level.INFO, "service UUID");
		UUID sessionUID = UUID.randomUUID();
		List<String> params = request.getParameters().get(CONTROL_GET_UUID_REQUEST_date);
		if(params == null || params.size() != 1)
		{
			logger.log(Level.WARNING, "UUID 서비스중 오류1");
			return HTTPServer.serveError(Status.BAD_REQUEST, "get date error");
		}
		Date reqDate;
		logger.log(Level.INFO, params.get(0));
		try
		{
			reqDate = REQ_COOKIE_DATE_FORMAT.parse(params.get(0));
		}
		catch (ParseException e)
		{
			logger.log(Level.WARNING, "UUID 서비스중 오류2", e);
			return HTTPServer.serveError(Status.BAD_REQUEST, "parse date error");
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		System.out.println(dateFormat.format(reqDate));
		setCookie(request, WebSessionManager.COOKIE_KEY_SESSION, sessionUID.toString(), reqDate, this.sessionCookieTimeout);
		return Response.newFixedLengthResponse(sessionUID.toString());
	}
	
	public static String getCookie(IHTTPSession request, String key)
	{
		String value = request.getCookies().read(key);
		return value;
	}
	
	public static void setCookie(IHTTPSession request, String key, String value, Date clientDate, int second)
	{
		Cookie cookie = new Cookie(key, value, getCookieTime(clientDate, second));
		Cookie cookie1 = new Cookie(key, value);
		System.out.println("normal:"+cookie1.getHTTPTime(1));;
		
		request.getCookies().set(cookie);
	}
	
	private static String getCookieTime(Date date, int second)
	{
		Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        calendar.add(Calendar.SECOND, second);
        System.out.println("change:"+dateFormat.format(calendar.getTime()));
        return dateFormat.format(calendar.getTime());
    }

	@Override
	public void start()
	{
		this.sessionCookieTimeout = Integer.parseInt(ServerCore.getProp(PROP_SESSION_COOKIE_TIMEOUT));
		this.serviceThread = new Thread(()->
		{
			ServerRunner.executeInstance(this);
		});
		this.serviceThread.setDaemon(true);
		this.serviceThread.start();
		
	}
}
