package in.soniccomputer.www.ammajuzz;

import com.google.android.vending.expansion.downloader.impl.DownloaderService;

/**
 * This class demonstrates the minimal client implementation of the
 * DownloaderService from the Downloader library.
 */
public class SampleDownloaderService extends DownloaderService {
    // stuff for LVL -- MODIFY FOR YOUR APPLICATION!
    private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtsG+tqRa0dJieO5TtNBQrQM7EhRljrLX8MqdnkoABQK7brc5RBWBylD1Mk8qrydBvoBEeW/DSRrS8MgCMKDp4CnPRMLuJBhx4eSkBgUL0INbbSfwxEbWO3ZK7EtYd/gqc8IfUCeuneb3TM5QuAdRbOwJkIGUU94bJNJ/Z7qxGeNw//JDjYvMN135ZTJdSWDdXn6qiZjRZMCkhTjP8It8cFSYykGwz5Q8fJkhMdCzj9GyqotuuFgasndnTfisyp24DRnqet2u92OrNO4OMuvqiR2ULTWKvDVI7PTjRTV6FakT/gWFCGUmyuTUxXfADIxb+Zrfji2eN2YD6ElkVycmkQIDAQAB";
    // used by the preference obfuscater
    private static final byte[] SALT = new byte[] {
            1, 43, -12, -1, 54, 96,
            -100, -12, 43, 2, -8, -4, 9, 5, -106, -108, -33, 45, -1, 84
    };

    /**
     * This public key comes from your Android Market publisher account, and it
     * used by the LVL to validate responses from Market on your behalf.
     */
    @Override
    public String getPublicKey() {
        return BASE64_PUBLIC_KEY;
    }

    /**
     * This is used by the preference obfuscater to make sure that your
     * obfuscated preferences are different than the ones used by other
     * applications.
     */
    @Override
    public byte[] getSALT() {
        return SALT;
    }

    /**
     * Fill this in with the class name for your alarm receiver. We do this
     * because receivers must be unique across all of Android (it's a good idea
     * to make sure that your receiver is in your unique package)
     */
    @Override
    public String getAlarmReceiverClassName() {
        return SampleAlarmReceiver.class.getName();
    }

}
