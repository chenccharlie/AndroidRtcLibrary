package cceh.androidrtclibrary.network;

/**
 * {@link Exception} which happens processing network functions.
 *
 * Created by Charlie Chen (ccehshmily@gmail.com) on 4/6/17.
 */
public class NetworkException extends Exception {
  public NetworkException(Throwable e) {
    this("", e);
  }

  public NetworkException(String message) {
    super(message);
  }

  public NetworkException(String message, Throwable e) {
    super(message, e);
  }
}
