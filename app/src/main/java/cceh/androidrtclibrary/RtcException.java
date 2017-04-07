package cceh.androidrtclibrary;

/**
 * {@link Exception} in Rtc communication.
 *
 * Created by Charlie Chen (ccehshmily@gmail.com) on 4/6/17.
 */
public class RtcException extends Exception {
  public RtcException(Throwable e) {
    this("", e);
  }

  public RtcException(String message) {
    super(message);
  }

  public RtcException(String message, Throwable e) {
    super(message, e);
  }
}
