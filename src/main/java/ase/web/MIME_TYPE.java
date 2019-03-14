package ase.web;

public enum MIME_TYPE
{
	MIME_PLAINTEXT("text/plain"),
    MIME_HTML("text/html"),
    MIME_JS("application/javascript"),
    MIME_CSS("text/css"),
    MIME_PNG("image/png"),
    MIME_JPEG("image/jpeg"),
    MIME_DEFAULT_BINARY("application/octet-stream"),
    MIME_XML("text/xml");

	String typeString;

	MIME_TYPE(String typeString)
	{
		this.typeString = typeString;
	}

	@Override
	public String toString()
	{
		return typeString;
	}
}