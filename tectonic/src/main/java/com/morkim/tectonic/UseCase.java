package com.morkim.tectonic;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class UseCase<Rq extends Request, Rs extends Result> {

	private static final SimpleUseCaseListener EMPTY_USE_CASE_LISTENER = new SimpleUseCaseListener<>();
	private Rq request;

	private class Prerequisite {

		Class<? extends UseCase> useCase;
		UseCaseListener listener;
		boolean condition;

		Prerequisite(Class<? extends UseCase> useCase, boolean condition, UseCaseListener listener) {

			this.useCase = useCase;
			this.condition = condition;
			this.listener = listener;
		}
	}

	private static Map<Class<? extends UseCase>, UseCase> inProgress = new HashMap<>();

	private final List<Prerequisite> prerequisites;
	private int prerequisiteIndex;

	private UseCaseListener<Rs> useCaseListener;
	private static Map<Class<? extends UseCase>, List<UseCaseListener<? extends Result>>> subscriptions = new HashMap<>();

	private static Map<Class<? extends UseCase>, Map<Integer, Result>> cachedResults = new HashMap<>();


	public UseCase() {

		useCaseListener = EMPTY_USE_CASE_LISTENER;

		if (subscriptions.get(this.getClass()) == null)
			subscriptions.put(this.getClass(), new ArrayList<UseCaseListener<? extends Result>>());

		prerequisites = new ArrayList<>();
		onAddPrerequisites();
	}

	public void execute() {
		execute(null);
	}

	public void execute(Rq request) {

		this.request = request;

//		for (UseCaseListener listener : useCaseListener)
			useCaseListener.onStart();

		if (inProgress.get(this.getClass()) == null) {
			for (UseCaseListener listener : subscriptions.get(this.getClass()))
				listener.onStart();

			inProgress.put(this.getClass(), this);

			if (prerequisites.isEmpty()) {
				onExecute(this.request);
			} else {
				prerequisiteIndex = 0;
				executePrerequisite();
			}
		}
	}

	protected void onAddPrerequisites() {

	}

	protected abstract void onExecute(Rq request);

	protected void addPrerequisite(Class<? extends UseCase> useCase, UseCaseListener listener) {
		addPrerequisite(useCase, true, listener);
	}

	protected void addPrerequisite(Class<? extends UseCase> useCase, boolean condition, UseCaseListener listener) {
		prerequisites.add(new Prerequisite(useCase, condition, listener));
	}

	private void executePrerequisite() {

		Prerequisite prerequisite = prerequisites.get(prerequisiteIndex);

		if (prerequisite.condition) {
			try {
				UseCase prerequisiteUseCase = prerequisite.useCase.newInstance();
				//noinspection unchecked
				prerequisiteUseCase.subscribe(prerequisite.listener);
				//noinspection unchecked
				subscribe(prerequisite.useCase, new SimpleUseCaseListener() {
					@Override
					public void onComplete() {
						executeNextPrerequisite();
					}
				});
				prerequisiteUseCase.execute();

			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			executeNextPrerequisite();
		}
	}

	private void executeNextPrerequisite() {

		prerequisiteIndex++;
		if (prerequisiteIndex < prerequisites.size()) {
			executePrerequisite();
		} else {
			onExecute(request);
		}
	}

	public void executeCached() {
		executeCached(null);
	}

	public void executeCached(Rq request) {

		if (isCachable()) {

			if (cachedResults.get(this.getClass()) == null) cachedResults.put(this.getClass(), new HashMap<Integer, Result>());
			//noinspection unchecked
			Rs result = (Rs) cachedResults.get(this.getClass())
					.get(request == null ? Request.NO_ID : request.id());
			if (result != null)
				updateSubscribers(result);
			else
				execute(request);
		} else
			execute(request);
	}

	protected boolean isCachable() {
		return false;
	}

	public void subscribe(UseCaseListener<Rs> useCaseListener) {
//		this.useCaseListener.add(useCaseListener);
		this.useCaseListener = useCaseListener;
	}

	protected void updateSubscribers(Rs result) {

//		for (UseCaseListener<Rs> listener : useCaseListener)
//			listener.onUpdate(result);
		useCaseListener.onUpdate(result);

		for (UseCaseListener listener : subscriptions.get(this.getClass()))
			//noinspection unchecked
			listener.onUpdate(result);

		Map<Integer, Result> resultsMap;
		if (cachedResults.get(this.getClass()) == null) {
			resultsMap = new HashMap<>();
			cachedResults.put(this.getClass(), resultsMap);
		} else
			resultsMap = cachedResults.get(this.getClass());

		resultsMap.put(request == null ? Request.NO_ID : request.id(), result);
	}

	public void unsubscribe(UseCaseListener<Rs> useCaseListener) {
//		this.useCaseListener.remove(useCaseListener);
		this.useCaseListener = EMPTY_USE_CASE_LISTENER;
	}

	public static void clearCache(Class<? extends UseCase> useCaseClass) {
		cachedResults.remove(useCaseClass);
	}

	public static void subscribe(Class<? extends UseCase> useCaseClass, UseCaseListener<? extends Result> listener) {

		if (subscriptions == null) subscriptions = new HashMap<>();

		if (subscriptions.get(useCaseClass) == null) subscriptions.put(useCaseClass, new ArrayList<UseCaseListener<? extends Result>>());
		subscriptions.get(useCaseClass).add(listener);
	}

	public static void unsubscribe(Class<? extends UseCase> useCaseClass, UseCaseListener<? extends Result> listener) {

		List<UseCaseListener<? extends Result>> useCaseListeners = subscriptions.get(useCaseClass);
		if (useCaseListeners != null) {
			if (listener != null)
				useCaseListeners.remove(listener);
			else
				useCaseListeners.clear();
		}
	}

	public static void unsubscribe(Class<? extends UseCase> useCaseClass) {
		unsubscribe(useCaseClass, null);
	}

	public static void unsubscribeAll() {
		subscriptions.clear();
	}

	public static void clearAllInProgress() {
		inProgress.clear();
	}

	protected void finish() {

		inProgress.remove(this.getClass());

//		for (UseCaseListener listener : useCaseListener)
//			listener.onComplete();
		useCaseListener.onComplete();
		for (UseCaseListener listener : subscriptions.get(this.getClass()))
			listener.onComplete();
	}

	public static void cancel(Class<? extends UseCase> useCaseClass) {

		UseCase useCase = inProgress.get(useCaseClass);
		if (useCase != null)
			useCase.cancel();
	}

	protected void cancel() {

//		for (UseCaseListener listener : useCaseListener)
//			listener.onCancel();
		useCaseListener.onCancel();
		for (UseCaseListener listener : subscriptions.get(this.getClass()))
			listener.onCancel();

		inProgress.remove(this.getClass());
	}

	protected void requestInput(int code) {
		inProgress.remove(this.getClass());
		useCaseListener.onInputRequired(code);
	}
}
