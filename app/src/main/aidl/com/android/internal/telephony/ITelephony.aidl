package com.android.internal.telephony;

interface ITelephony {
	void call(String number);

	boolean endCall();

	void answerRingingCall();
}