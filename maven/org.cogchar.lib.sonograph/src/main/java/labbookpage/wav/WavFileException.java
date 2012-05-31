package labbookpage.wav;
// A.Greensted
// http://www.labbookpages.co.uk

public class WavFileException extends Exception
{
	public WavFileException()
	{
		super();
	}

	public WavFileException(String message)
	{
		super(message);
	}

	public WavFileException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public WavFileException(Throwable cause)
	{
		super(cause);
	}
}
