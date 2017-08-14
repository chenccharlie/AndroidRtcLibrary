package cceh.androidrtclibrary.testapp;

import org.webrtc.MediaConstraints;

/**
 * Factory for {@link MediaConstraints}.
 *
 * Created by Charlie Chen (ccehshmily@gmail.com) on 4/7/17.
 */
public class MediaParams {
  public static MediaConstraints defaultVideoConstraints() {
    MediaConstraints videoConstraints = new MediaConstraints();
    videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth","1920"));
    videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight","1080"));
    videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minWidth", "640"));
    videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minHeight","480"));
    return videoConstraints;
  }

  public static MediaConstraints defaultAudioConstraints() {
    MediaConstraints audioConstraints = new MediaConstraints();
    audioConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
    audioConstraints.optional.add(new MediaConstraints.KeyValuePair("RtpDataChannels", "true"));
    audioConstraints.optional.add(new MediaConstraints.KeyValuePair("internalSctpDataChannels", "true"));
    return audioConstraints;
  }
}
