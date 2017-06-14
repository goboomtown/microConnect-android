package com.goboomtown.btconnecthelp.service;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.util.Log;

import com.goboomtown.btconnecthelp.R;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * DNS-SD aka NSD service for microConnect.
 */
public class BoomtownDNSSDService {

    public static final String TAG = BoomtownDNSSDService.class.getSimpleName();
    public static final String DNS_SD_KEY_TXT = "txt";
    public static final String NSD_SVC_NAME = "MicroConnect";
    public static final String NSD_SVC_NAME_PROTOCOL = "_microconnect._tcp.";
    public static final int NSD_SVC_PORT = 22666;
    public static final String DEVICE_OS_METADATA_KEY = "com.goboomtown.device_os";
    public static final String QUOTE = "\"";
    public static final String SPACE = " ";

    /**
     * singleton class instance
     */
    private static BoomtownDNSSDService instance;

    /**
     *
     */
    private static Context ctx;

    /**
     * DNS-SD (NSD) service broadcast name
     */
    protected String serviceName;

    /**
     * NSD svc manager
     */
    private NsdManager mNsdManager;

    /**
     * NSD svc registration listener
     */
    private NsdManager.RegistrationListener mRegistrationListener;

    /**
     * flag indicates if svc registration in progress
     */
    protected boolean svcRegistering;

    /**
     * flag indicates if svc registration finished+success
     */
    protected boolean svcRegistered;

    /**
     * default c'tor
     */
    private BoomtownDNSSDService() {
        serviceName = null;
    }

    public static BoomtownDNSSDService getInstance(Context context) throws IllegalStateException {
        if (context == null && ctx == null) {
            throw new IllegalStateException("getInstance called with null context");
        }
        if (instance == null) {
            instance = new BoomtownDNSSDService();
            ctx = context;
        }
        return instance;
    }

    /**
     * @return true if NSD registration underway, false otherwise
     */
    public boolean isSvcRegistering() {
        return svcRegistering;
    }

    /**
     * @return true if NSD svc registered, false otherwise
     */
    public boolean isSvcRegistered() {
        return svcRegistered;
    }

    public void start() {
        int port = ctx.getResources().getInteger(R.integer.dns_sd_port);
        if (mRegistrationListener == null) {
            initializeRegistrationListener();
        }
        registerDNSSDService(port);
    }

    /**
     * Unregister NSD svc.
     */
    public void tearDown() {
        if (!svcRegistered) {
            Log.v(TAG, "DNS-SD service not registered");
            return;
        }
        if (mRegistrationListener == null) {
            Log.i(TAG, "unable to unregister DNS-SD service, listener undefined");
            return;
        }
        mNsdManager.unregisterService(mRegistrationListener);
    }

    /**
     * Register NSD service.  Exits with no action if svc registration already underway.
     *
     * @param port
     */
    protected void registerDNSSDService(int port) {
        if (isSvcRegistering()) {
            Log.v(TAG, "registerDNSSDService called while service in process of registering");
            return;
        }
        if (isSvcRegistered()) {
            Log.v(TAG, "registerDNSSDService already registered");
            return;
        }
        svcRegistering = true;
        svcRegistered = false;
        NsdServiceInfo serviceInfo = buildDNSSDServiceInfo(port);
        mNsdManager = (NsdManager) ctx.getSystemService(Context.NSD_SERVICE);
        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }

    /**
     * Build NSD service parameters.
     *
     * @param port port to use for registering NSD svc
     * @return hydrated NsdServiceInfo instance
     */
    protected NsdServiceInfo buildDNSSDServiceInfo(int port) {
        InetAddress hostAddy = retrieveNonLoopbackIP();
        NsdServiceInfo serviceInfo = null;
        if (hostAddy != null) {
            serviceInfo = new NsdServiceInfo();
            // svc name subject to change based on conflicts
            // with other services advertised on the same network.
            serviceInfo.setServiceName(NSD_SVC_NAME);
            serviceInfo.setServiceType(NSD_SVC_NAME_PROTOCOL);
            serviceInfo.setHost(hostAddy);
            serviceInfo.setPort(NSD_SVC_PORT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appendDNSSDServiceLollipop(serviceInfo);
        }
        return serviceInfo;
    }

    /**
     * Append TXT attribute (name/val pair) to serviceInfo instance.
     * The value used for TXT is provide by {@link this#buildBoomtownUserAgentAsNameValPairs(Context)}.
     *
     * @param serviceInfo
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void appendDNSSDServiceLollipop(NsdServiceInfo serviceInfo) {
        if (serviceInfo == null) {
            return;
        }
        serviceInfo.setAttribute(DNS_SD_KEY_TXT, BoomtownDNSSDService.buildBoomtownUserAgentAsNameValPairs(ctx));
    }

    /**
     * Initialize NSD svc listener.
     */
    protected void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                // Save the service name.  Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                serviceName = serviceInfo.getServiceName();
                svcRegistering = false;
                svcRegistered = true;
                // log success
                StringBuilder logMsg = new StringBuilder();
                logMsg.append("DSNSD service registration success!  serviceInfo{} ");
                if (null != logMsg) {
                    logMsg.append(" name=").append(serviceInfo.getServiceName())
                            .append(", port=")
                            .append(serviceInfo.getPort())
                            .append(", type=")
                            .append(serviceInfo.getServiceType())
                            .append(", host=" + serviceInfo.getHost());
                } else {
                    logMsg.append(" = null");
                }
                Log.i(TAG, logMsg.toString());
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // failed registration
                svcRegistering = false;
                svcRegistered = false;
                StringBuilder logMsg = new StringBuilder();
                logMsg.append("DSNSD service registration failed, serviceInfo{} ");
                if (null != logMsg) {
                    logMsg.append(" name=").append(serviceInfo.getServiceName())
                            .append(", port=")
                            .append(serviceInfo.getPort())
                            .append(", type=")
                            .append(serviceInfo.getServiceType())
                            .append(", host=" + serviceInfo.getHost());
                } else {
                    logMsg.append(" = null");
                }
                logMsg.append(", errorCode=" + errorCode);
                Log.e(TAG, logMsg.toString());
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
                // service unregistered.  Only happens when NsdManager.unregisterService() invoked with this listener.
                svcRegistering = false;
                svcRegistered = false;
                Log.i(TAG, "DSNSD service unregistered, serviceInfo.name=" + (null != serviceInfo ? serviceInfo.getServiceName() : "null"));
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // failed unregistration
                svcRegistering = false;
                svcRegistered = false;
                StringBuilder errMsg = new StringBuilder();
                errMsg.append("DSNSD service un-registration failed, serviceInfo{} ");
                if (null != errMsg) {
                    errMsg.append(" name=").append(serviceInfo.getServiceName())
                            .append(", port=")
                            .append(serviceInfo.getPort())
                            .append(", type=")
                            .append(serviceInfo.getServiceType())
                            .append(", host=" + serviceInfo.getHost());
                } else {
                    errMsg.append(" = null");
                }
                errMsg.append(", errorCode=" + errorCode);
                Log.e(TAG, errMsg.toString());
            }
        };
    }

    /**
     * Retrieve a non-loopback IP address assigned to this device.  Goes through all network
     * interfaces, and all IP addresses assigned to each interface.
     *
     * @return the first non-loopback, IPv4 address assigned to a network interface on the device
     */
    public static InetAddress retrieveNonLoopbackIP() {
        InetAddress hostAddy = null;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements() && hostAddy == null; ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && !inetAddress.isLinkLocalAddress()
                            && inetAddress.isSiteLocalAddress()) {
                        Log.v(TAG, "network scan got host address=" + inetAddress.getHostAddress().toString() + "\n");
                        hostAddy = inetAddress;
                        break;
                    }

                }
            }
        } catch (SocketException ex) {
            Log.e("LOG_TAG", ex.toString());
        }
        return hostAddy;
    }

    /**
     * assemble custom user-agent string as name/value pairs
     * "build=1" "device=android" "bundle_id=com.goboomtown.BoomtownConnectPaySTAGE" "manufacturer=Apple" "os=iOS 10.3.2" "version=3.2.2" "product_name=ConnectSTAGE"
     *
     * @param context
     * @return custom user-agent string
     */
    public static String buildBoomtownUserAgentAsNameValPairs(Context context) {
        StringBuilder sb = new StringBuilder();
        String packageName = context.getPackageName();
        sb.append(QUOTE);
        sb.append("product_name=");
        sb.append(context.getString(R.string.app_name));
        sb.append(QUOTE);
        sb.append(SPACE);
        sb.append(QUOTE);
        sb.append("bundle_id=");
        sb.append(packageName);
        sb.append(QUOTE);
        sb.append(SPACE);
        PackageInfo info = null;
        try {
            info = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        sb.append(QUOTE);
        sb.append("version=");
        if (info != null) {
            sb.append(info.versionName);
        } else {
            sb.append("??");
        }
        sb.append(QUOTE);
        sb.append(SPACE);
        sb.append(QUOTE);
        sb.append("device=");
        sb.append(android.os.Build.MODEL);
        sb.append(QUOTE);
        sb.append(SPACE);
        sb.append(QUOTE);
        sb.append("manufacturer=");
        sb.append(android.os.Build.BRAND);
        sb.append(QUOTE);
        sb.append(SPACE);
        sb.append(QUOTE);
        sb.append("os=");
        sb.append(deviceOS(context));
        sb.append(SPACE);
        sb.append(android.os.Build.VERSION.RELEASE);
        sb.append(QUOTE);
        return sb.toString();
    }

    /**
     * Get name of OS running on device.
     *
     * @param context
     * @return device OS name
     */
    public static String deviceOS(Context context) {
        String device_os = null;
        try {
            String package_name = context.getPackageName();
            if (package_name != null) {
                ApplicationInfo info = context.getPackageManager().getApplicationInfo(package_name, PackageManager.GET_META_DATA);
                if (info instanceof ApplicationInfo && info.metaData instanceof Bundle) {
                    device_os = info.metaData.getString(DEVICE_OS_METADATA_KEY);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "cannot get metadata from AndroidManifest\n" + Log.getStackTraceString(e));
        }
        if (device_os == null) {
            device_os = "android";
        }
        return device_os;
    }
}
