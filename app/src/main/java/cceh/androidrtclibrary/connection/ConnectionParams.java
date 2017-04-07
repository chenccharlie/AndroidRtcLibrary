package cceh.androidrtclibrary.connection;

import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;

import java.util.List;

/**
 * Params of a {@link Connection}, including {@link org.webrtc.PeerConnection.IceServer}s
 * and {@link org.webrtc.MediaConstraints}.
 *
 * Created by Charlie Chen (ccehshmily@gmail.com) on 4/6/17.
 */
public class ConnectionParams {
  private final List<PeerConnection.IceServer> iceServers;
  private final MediaConstraints connectionConstraints;

  public ConnectionParams(List<PeerConnection.IceServer> iceServers) {
    this(
        iceServers,
        defaultConnectionConstraints());
  }

  public ConnectionParams(
      List<PeerConnection.IceServer> iceServers,
      MediaConstraints connectionConstraints) {
    this.iceServers = iceServers;
    this.connectionConstraints = connectionConstraints;
  }

  public List<PeerConnection.IceServer> getIceServers() {
    return iceServers;
  }

  public MediaConstraints getConnectionConstraints() {
    return connectionConstraints;
  }

  private static MediaConstraints defaultConnectionConstraints() {
    MediaConstraints constraints = new MediaConstraints();
    constraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
    constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"));
    constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
    return constraints;
  }
}
