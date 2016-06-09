package com.genonbeta.CoolSocket.test.helper;

import android.support.v4.util.ArrayMap;

import com.genonbeta.CoolSocket.CoolCommunication;
import com.genonbeta.CoolSocket.CoolJsonCommunication;
import com.genonbeta.core.util.NetworkDeviceScanner;
import com.genonbeta.core.util.NetworkDeviceScanner.ScannerHandler;
import com.genonbeta.core.util.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class PairListHelper
{
    private static ArrayMap<String, DeviceInfo> mIndex = new ArrayMap<String, DeviceInfo>();
    private static NetworkDeviceScanner mScanner = new NetworkDeviceScanner();

    public static class ResultHandler implements ScannerHandler
	{
        @Override
        public void onDeviceFound(InetAddress inetAddress)
		{
            if (!PairListHelper.mIndex.containsKey(inetAddress.getHostAddress()))
			{
                String hostAddress = inetAddress.getHostAddress();

                DeviceInfo deviceInfo = new DeviceInfo();
                mIndex.put(hostAddress, deviceInfo);
            }
        }

        @Override
        public void onThreadsCompleted()
		{
            for (String str : PairListHelper.getList().keySet())
			{
                final DeviceInfo deviceInfo = PairListHelper.getList().get(str);

                deviceInfo.isTested = true;
                deviceInfo.trebleShot = NetworkUtils.testSocket(str, 1128);
                deviceInfo.coolSocket = NetworkUtils.testSocket(str, 3000);
                deviceInfo.deviceController = NetworkUtils.testSocket(str, 4632);

				if (deviceInfo.deviceController)
					CoolJsonCommunication.Messenger.sendOnCurrentThread(str, 4632, null, new CoolJsonCommunication.JsonResponseHandler()
						{
							@Override
							public void onJsonMessage(Socket socket, CoolCommunication.Messenger.Process process, JSONObject json)
							{
								try
								{
									json.put("printDeviceName", true);
									
									JSONObject response = new JSONObject(process.waitForResponse());
									
									if (response.has("deviceName"))
										deviceInfo.deviceName = response.getString("deviceName");
								}
								catch (JSONException e)
								{}
							}
						}
					);

            }
        }
    }

    public static class DeviceInfo
	{
        public boolean coolSocket = false;
        public boolean deviceController = false;
        public boolean isTested = false;
        public boolean trebleShot = false;
		public String deviceName = null;
	}

    public static ArrayMap<String, DeviceInfo> getList()
	{
        return mIndex;
    }

    public static NetworkDeviceScanner getScanner()
	{
        return mScanner;
    }

    public static boolean update(ResultHandler resultHandler)
	{
        if (!mScanner.isScannerAvaiable())
            return false;

        mIndex.clear();

        ArrayList<String> interfacesWithOnlyIp = NetworkUtils.getInterfacesWithOnlyIp(true, new String[] {"rmnet"});

        for (String str : interfacesWithOnlyIp)
		{
            if (!mIndex.containsKey(str))
			{
                DeviceInfo deviceInfo = new DeviceInfo();
                mIndex.put(str, deviceInfo);
            }
        }

        return mScanner.scan(interfacesWithOnlyIp, resultHandler);
    }
}
