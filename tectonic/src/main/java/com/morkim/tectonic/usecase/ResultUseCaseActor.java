//package com.morkim.tectonic.usecase;
//
//import com.morkim.tectonic.flow.Step;
//
//import java.util.UUID;
//
//public class ResultUseCaseActor<E, R> implements ResultActor<E, R> {
//
//    private static final Step STEP = new Step() {
//        @Override
//        public void terminate() {
//
//        }
//    };
//
//    private Class<? extends UseCase<R>> cls;
//    private UUID key;
//
//    public ResultUseCaseActor(Class<? extends UseCase<R>> cls, UUID key) {
//        this.cls = cls;
//        this.key = key;
//    }
//
//    @Override
//    public void onComplete(E e, R result) {
//        UseCase.replyWith(STEP, key, result);
//    }
//
//    @Override
//    public void onAbort(E e) {
//        UseCase.replyWith(STEP, key, new UseCaseAborted(cls));
//    }
//}
