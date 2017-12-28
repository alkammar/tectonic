package com.morkim.tectonic.entities;


import com.morkim.tectonic.Result;
import com.morkim.tectonic.UseCase;

public class LongExecutionUseCase extends UseCase<PendingActionRequest, Result> {

	@Override
	protected void onExecute(PendingActionRequest request) {

		if (request == null) {
			new Thread(new Runnable() {
				@Override
				public void run() {

					int i = 0;
					while (i < 10000) {
						i++;
					}

					finish();
				}
			}).start();
		} else {
//			int i = 0;
//			while (i < 100000) {
//				i++;
//			}

			finish();
		}
	}
}
