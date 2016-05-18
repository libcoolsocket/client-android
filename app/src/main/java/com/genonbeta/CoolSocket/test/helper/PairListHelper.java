package com.genonbeta.CoolSocket.test.helper;

import android.support.v4.util.ArrayMap;
import com.genonbeta.core.util.NetworkDeviceScanner;
import com.genonbeta.core.util.NetworkDeviceScanner.ScannerHandler;
import com.genonbeta.core.util.NetworkUtils;
import java.net.InetAddress;
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
                DeviceInfo deviceInfo = PairListHelper.getList().get(str);
				
                deviceInfo.isTested = true;
                deviceInfo.trebleShot = NetworkUtils.testSocket(str, 1128);
                deviceInfo.coolSocket = NetworkUtils.testSocket(str, 3000);
                deviceInfo.deviceController = NetworkUtils.testSocket(str, 4632);
            }
        }
    }

    public static class DeviceInfo
	{
        public boolean coolSocket = false;
        public boolean deviceController = false;
        public boolean isTested = false;
        public boolean trebleShot = false;
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
