package cceh.androidrtclibrary;

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
  private final MediaConstraints videoConstraints;
  private final MediaConstraints audioConstraints;

  public ConnectionParams(List<PeerConnection.IceServer> iceServers) {
    this(
        iceServers,
        defaultConnectionConstraints(),
        defaultVideoConstraints(),
        defaultAudioConstraints());
  }

  public ConnectionParams(
      List<PeerConnection.IceServer> iceServers,
      MediaConstraints connectionConstraints,
      MediaConstraints videoConstraints,
      MediaConstraints audioConstraints) {
    this.iceServers = iceServers;
    this.connectionConstraints = connectionConstraints;
    this.videoConstraints = videoConstraints;
    this.audioConstraints = audioConstraints;
  }

  public List<PeerConnection.IceServer> getIceServers() {
    return iceServers;
  }

  public MediaConstraints getConnectionConstraints() {
    return connectionConstraints;
  }

  public MediaConstraints getVideoConstraints() {
    return videoConstraints;
  }

  public MediaConstraints getAudioConstraints() {
    return audioConstraints;
  }

  private static MediaConstraints defaultConnectionConstraints() {
    MediaConstraints constraints = new MediaConstraints();
    constraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
    constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"));
    constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
    return constraints;
  }

  private static MediaConstraints defaultVideoConstraints() {
    MediaConstraints videoConstraints = new MediaConstraints();
    videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth","1280"));
    videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight","720"));
    videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minWidth", "640"));
    videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minHeight","480"));
    return videoConstraints;
  }

  private static MediaConstraints defaultAudioConstraints() {
    MediaConstraints audioConstraints = new MediaConstraints();
    return audioConstraints;
  }
}
