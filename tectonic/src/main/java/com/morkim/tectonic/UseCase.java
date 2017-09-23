package com.morkim.tectonic;


import android.annotation.SuppressLint;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class UseCase<Rq extends Request, Rs extends Result> {

    private static final OnStartListener EMPTY_ON_START_LISTENER = new OnStartListener() {
        @Override
        public void onStart() {

        }
    };
    private static final OnUpdateListener EMPTY_ON_UPDATE_LISTENER = new OnUpdateListener() {
        @Override
        public void onUpdate(Result result) {

        }
    };
    private static final OnCompleteListener EMPTY_ON_COMPLETE_LISTENER = new OnCompleteListener() {
        @Override
        public void onComplete() {

        }
    };
    private static final OnAbortListener EMPTY_ON_ABORT_LISTENER = new OnAbortListener() {
        @Override
        public void onCancel() {

        }
    };
    private static final OnInputRequiredListener EMPTY_ON_INPUT_REQUIRED_LISTENER = new OnInputRequiredListener() {
        @Override
        public void onInputRequired(int code) {

        }
    };

    private Rq request;

    private class Prerequisite {

        Class<? extends UseCase> useCase;
        OnCompleteListener onCompleteListener;
        OnAbortListener onAbortListener;
        boolean condition;

        Prerequisite(Class<? extends UseCase> useCase,
                     boolean condition,
                     OnCompleteListener onCompleteListener,
                     OnAbortListener onAbortListener) {

            this.useCase = useCase;
            this.condition = condition;
            this.onCompleteListener = onCompleteListener;
            this.onAbortListener = onAbortListener;
        }
    }

    private static Map<Class<? extends UseCase>, UseCase> inProgress = new HashMap<>();

    private final List<Prerequisite> prerequisites;
    private int prerequisiteIndex;

    private OnStartListener onStartListener;
    private OnUpdateListener<Rs> onUpdateListener;
    private OnCompleteListener onCompleteListener;
    private OnAbortListener onAbortListener;
    private OnInputRequiredListener onInputRequiredListener;

    private static Map<Class<? extends UseCase>, List<OnStartListener>> onStartSubscriptions = new HashMap<>();
    private static Map<Class<? extends UseCase>, List<OnUpdateListener>> onUpdateSubscriptions = new HashMap<>();
    private static Map<Class<? extends UseCase>, List<OnCompleteListener>> onCompleteSubscriptions = new HashMap<>();
    private static Map<Class<? extends UseCase>, List<OnAbortListener>> onAbortSubscriptions = new HashMap<>();
    private static Map<Class<? extends UseCase>, List<OnInputRequiredListener>> onInputRequiredSubscriptions = new HashMap<>();

    private static Map<Class<? extends UseCase>, Map<Integer, Result>> cachedResults = new HashMap<>();


    public UseCase() {

        onStartListener = EMPTY_ON_START_LISTENER;
        //noinspection unchecked
        onUpdateListener = EMPTY_ON_UPDATE_LISTENER;
        onCompleteListener = EMPTY_ON_COMPLETE_LISTENER;
        onAbortListener = EMPTY_ON_ABORT_LISTENER;
        onInputRequiredListener = EMPTY_ON_INPUT_REQUIRED_LISTENER;

        if (onStartSubscriptions.get(this.getClass()) == null)
            onStartSubscriptions.put(this.getClass(), new ArrayList<OnStartListener>());
        if (onUpdateSubscriptions.get(this.getClass()) == null)
            onUpdateSubscriptions.put(this.getClass(), new ArrayList<OnUpdateListener>());
        if (onCompleteSubscriptions.get(this.getClass()) == null)
            onCompleteSubscriptions.put(this.getClass(), new ArrayList<OnCompleteListener>());
        if (onAbortSubscriptions.get(this.getClass()) == null)
            onAbortSubscriptions.put(this.getClass(), new ArrayList<OnAbortListener>());
        if (onInputRequiredSubscriptions.get(this.getClass()) == null)
            onInputRequiredSubscriptions.put(this.getClass(), new ArrayList<OnInputRequiredListener>());

        prerequisites = new ArrayList<>();
        onAddPrerequisites();
    }

    public void execute() {
        execute(null);
    }

    public void execute(Rq request) {

        this.request = request;

        if (onStartListener != null) onStartListener.onStart();

        if (inProgress.get(this.getClass()) == null) {
            for (OnStartListener listener : onStartSubscriptions.get(this.getClass()))
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

    protected UseCase<Rq, Rs> addPrerequisite(Class<? extends UseCase> useCase, OnCompleteListener listener) {
        addPrerequisite(useCase, true, listener);

        return this;
    }

    protected UseCase<Rq, Rs> addPrerequisite(Class<? extends UseCase> useCase, OnAbortListener listener) {
        addPrerequisite(useCase, true, EMPTY_ON_COMPLETE_LISTENER, listener);

        return this;
    }

    protected UseCase<Rq, Rs> addPrerequisite(Class<? extends UseCase> useCase, boolean condition, OnCompleteListener listener) {
        prerequisites.add(new Prerequisite(useCase, condition, listener, EMPTY_ON_ABORT_LISTENER));

        return this;
    }

    protected UseCase<Rq, Rs> addPrerequisite(Class<? extends UseCase> useCase, boolean condition, OnCompleteListener onCompleteListener, OnAbortListener onAbortListener) {
        prerequisites.add(new Prerequisite(useCase, condition, onCompleteListener, onAbortListener));

        return this;
    }

    private void executePrerequisite() {

        Prerequisite prerequisite = prerequisites.get(prerequisiteIndex);

        if (prerequisite.condition) {
            try {
                subscribe(prerequisite.useCase, new OnCompleteListener() {
                    @Override
                    public void onComplete() {
                        executeNextPrerequisite();
                    }
                });

                UseCase prerequisiteUseCase = prerequisite.useCase.newInstance();
                prerequisiteUseCase
                        .subscribe(prerequisite.onCompleteListener)
                        .subscribe(prerequisite.onAbortListener)
                        .execute();

            } catch (InstantiationException e) {
                Log.e("Use Case Prerequisite", "Unable to Instantiate class " + prerequisite.useCase);
            } catch (IllegalAccessException e) {
                Log.e("Use Case Prerequisite", "Unable to Access class " + prerequisite.useCase);
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

    @SuppressLint("UseSparseArrays")
    public void executeCached(Rq request) {

        if (willCache()) {

            if (cachedResults.get(this.getClass()) == null)
                cachedResults.put(this.getClass(), new HashMap<Integer, Result>());
            //noinspection unchecked
            Rs result = (Rs) cachedResults.get(this.getClass()).get(request == null ? Request.NO_ID : request.id());
            if (result != null)
                updateSubscribers(result);
            else
                execute(request);
        } else
            execute(request);
    }

    protected boolean willCache() {
        return false;
    }

    public UseCase<Rq, Rs> subscribe(OnStartListener useCaseListener) {
        this.onStartListener = useCaseListener;

        return this;
    }

    public UseCase<Rq, Rs> subscribe(OnUpdateListener<Rs> onUpdateListener) {
        this.onUpdateListener = onUpdateListener;

        return this;
    }

    public UseCase<Rq, Rs> subscribe(OnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;

        return this;
    }

    public UseCase<Rq, Rs> subscribe(OnAbortListener onAbortListener) {
        this.onAbortListener = onAbortListener;

        return this;
    }

    public UseCase<Rq, Rs> subscribe(OnInputRequiredListener onInputRequiredListener) {
        this.onInputRequiredListener = onInputRequiredListener;

        return this;
    }

    @SuppressLint("UseSparseArrays")
    protected void updateSubscribers(Rs result) {

        onUpdateListener.onUpdate(result);
        for (OnUpdateListener listener : onUpdateSubscriptions.get(this.getClass()))
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

    public void unsubscribe() {
        this.onStartListener = EMPTY_ON_START_LISTENER;
        //noinspection unchecked
        this.onUpdateListener = EMPTY_ON_UPDATE_LISTENER;
        this.onCompleteListener = EMPTY_ON_COMPLETE_LISTENER;
        this.onAbortListener = EMPTY_ON_ABORT_LISTENER;
        this.onInputRequiredListener = EMPTY_ON_INPUT_REQUIRED_LISTENER;
    }

    public static void clearCache(Class<? extends UseCase> useCaseClass) {
        cachedResults.remove(useCaseClass);
    }

    public static void subscribe(Class<? extends UseCase> useCaseClass, OnStartListener listener) {

        if (onStartSubscriptions == null) onStartSubscriptions = new HashMap<>();

        // TODO synchronize with remove
        if (onStartSubscriptions.get(useCaseClass) == null)
            onStartSubscriptions.put(useCaseClass, new ArrayList<OnStartListener>());
        onStartSubscriptions.get(useCaseClass).add(listener);
    }

    public static void subscribe(Class<? extends UseCase> useCaseClass, OnUpdateListener listener) {

        if (onUpdateSubscriptions == null) onUpdateSubscriptions = new HashMap<>();

        // TODO synchronize with remove
        if (onUpdateSubscriptions.get(useCaseClass) == null)
            onUpdateSubscriptions.put(useCaseClass, new ArrayList<OnUpdateListener>());
        onUpdateSubscriptions.get(useCaseClass).add(listener);
    }

    public static void subscribe(Class<? extends UseCase> useCaseClass, OnCompleteListener listener) {

        if (onCompleteSubscriptions == null) onCompleteSubscriptions = new HashMap<>();

        // TODO synchronize with remove
        if (onCompleteSubscriptions.get(useCaseClass) == null)
            onCompleteSubscriptions.put(useCaseClass, new ArrayList<OnCompleteListener>());
        onCompleteSubscriptions.get(useCaseClass).add(listener);
    }

    public static void unsubscribe(Class<? extends UseCase> useCaseClass, OnStartListener listener) {

        List<OnStartListener> useCaseListeners = onStartSubscriptions.get(useCaseClass);
        if (useCaseListeners != null) {
            if (listener != null)
                useCaseListeners.remove(listener);
            else
                useCaseListeners.clear();
        }
    }

    public static void unsubscribe(Class<? extends UseCase> useCaseClass, OnUpdateListener listener) {

        List<OnUpdateListener> useCaseListeners = onUpdateSubscriptions.get(useCaseClass);
        if (useCaseListeners != null) {
            if (listener != null)
                useCaseListeners.remove(listener);
            else
                useCaseListeners.clear();
        }
    }

    public static void unsubscribe(Class<? extends UseCase> useCaseClass, OnCompleteListener listener) {

        List<OnCompleteListener> useCaseListeners = onCompleteSubscriptions.get(useCaseClass);
        if (useCaseListeners != null) {
            if (listener != null)
                useCaseListeners.remove(listener);
            else
                useCaseListeners.clear();
        }
    }

    public static void unsubscribe(Class<? extends UseCase> useCaseClass) {
        unsubscribe(useCaseClass, (OnStartListener) null);
        unsubscribe(useCaseClass, (OnUpdateListener) null);
    }

    public static void unsubscribeAll() {
        onStartSubscriptions.clear();
    }

    public static void clearAllInProgress() {
        inProgress.clear();
    }

    protected void finish() {

        // TODO synchronize
        inProgress.remove(this.getClass());

        onCompleteListener.onComplete();
        for (OnCompleteListener listener : onCompleteSubscriptions.get(this.getClass()))
            listener.onComplete();

        onPostExecute();
    }

    public static void abort(Class<? extends UseCase> useCaseClass) {

        UseCase useCase = inProgress.get(useCaseClass);
        if (useCase != null)
            useCase.abort();
    }

    protected void abort() {

        onAbortListener.onCancel();
        for (OnAbortListener listener : onAbortSubscriptions.get(this.getClass()))
            listener.onCancel();

        inProgress.remove(this.getClass());
    }

    protected void requestInput(int code) {
        inProgress.remove(this.getClass());
        onInputRequiredListener.onInputRequired(code);
    }

    protected void onPostExecute() {

    }

    interface OnStartListener {

        void onStart();
    }

    interface OnUpdateListener<Rs extends Result> {

        void onUpdate(Rs result);
    }

    public interface OnCompleteListener {

        void onComplete();
    }

    public interface OnAbortListener {

        void onCancel();
    }

    interface OnInputRequiredListener {

        void onInputRequired(int code);
    }
}
