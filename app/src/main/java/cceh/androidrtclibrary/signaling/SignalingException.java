package cceh.androidrtclibrary.signaling;

/**
 * {@link Exception} which happens during signaling.
 *
 * Created by Charlie Chen (ccehshmily@gmail.com) on 4/5/17.
 */
public class SignalingException extends Exception {
  public SignalingException(Throwable e) {
    this("", e);
  }

  public SignalingException(String message) {
    super(message);
  }

  public SignalingException(String message, Throwable e) {
    super(message, e);
  }
}
