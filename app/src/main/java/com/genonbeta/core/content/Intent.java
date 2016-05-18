package com.genonbeta.core.content;

public class Intent
{
    public static final String ACTION_NOTIFY_CHANGES = "com.gebonbeta.action.NOTIFY_CHANGES";

    public static android.content.Intent getNotifyIntent()
	{
        return new android.content.Intent(ACTION_NOTIFY_CHANGES);
	}
}
