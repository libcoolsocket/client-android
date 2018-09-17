package com.genonbeta.CoolSocket.test.helper;

import android.support.v4.util.ArrayMap;

import com.genonbeta.CoolSocket.CoolSocket;
import com.genonbeta.core.util.NetworkDeviceScanner;
import com.genonbeta.core.util.NetworkDeviceScanner.ScannerHandler;
import com.genonbeta.core.util.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeoutException;

public class PeerListHelper
{
	private static final ArrayMap<String, DeviceInfo> mIndex = new ArrayMap<>();
	private static final NetworkDeviceScanner mScanner = new NetworkDeviceScanner();

	public static ArrayMap<String, DeviceInfo> getList()
	{
		synchronized (mIndex)
		{
			return mIndex;
		}
	}

	public static NetworkDeviceScanner getScanner()
	{
		return mScanner;
	}

	public static boolean update(ResultHandler resultHandler)
	{
		if (!mScanner.isScannerAvailable())
			return false;

		getList().clear();

		return mScanner.scan(NetworkUtils.getInterfacesWithOnlyIp(true, new String[]{"rmnet"}), resultHandler);
	}

	public static class ResultHandler implements ScannerHandler
	{
		@Override
		public void onRunning(InetAddress address)
		{

		}

		@Override
		public void onDeviceFound(InetAddress inetAddress)
		{
			final String hostAddress = inetAddress.getHostAddress();
			final DeviceInfo deviceInfo = new DeviceInfo();

			PeerListHelper.getList().put(hostAddress, deviceInfo);

			deviceInfo.isTested = true;

			deviceInfo.trebleShot = NetworkUtils.testSocket(hostAddress, 1128);
			deviceInfo.coolSocket = NetworkUtils.testSocket(hostAddress, 3000);
			deviceInfo.deviceController = NetworkUtils.testSocket(hostAddress, 4632);

			if (deviceInfo.deviceController)
				CoolSocket.connect(new CoolSocket.Client.ConnectionHandler()
				{
					@Override
					public void onConnect(CoolSocket.Client client)
					{
						try {
							CoolSocket.ActiveConnection activeConnection = client.connect(new InetSocketAddress(hostAddress, 4632), 10000);

							activeConnection.reply(new JSONObject()
									.put("printDeviceName", true)
									.toString());

							JSONObject response = new JSONObject(activeConnection.receive().response);

							if (response.has("deviceName"))
								deviceInfo.deviceName = response.getString("deviceName");

						} catch (IOException e) {
							e.printStackTrace();
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (TimeoutException e) {
							e.printStackTrace();
						}
					}
				}, null);
		}

		@Override
		public void onThreadsCompleted()
		{
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
}
