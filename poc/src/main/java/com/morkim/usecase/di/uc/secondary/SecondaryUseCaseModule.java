//package com.morkim.usecase.di.uc.secondary;
//
//import com.morkim.usecase.auth.AuthenticationFlow;
//import com.morkim.usecase.backend.BackendImpl;
//import com.morkim.usecase.di.PerUseCase;
//import com.morkim.usecase.uc.main.MainUseCase;
//import com.morkim.usecase.uc.secondary.SecondaryUseCase;
//
//import dagger.Module;
//import dagger.Provides;
//
//@Module
//public class SecondaryUseCaseModule {
//
////    private MainUseCase.UI UI;
////
////    public SecondaryUseCaseModule(MainUseCase.UI UI) {
////        this.UI = UI;
////    }
////
////    @Provides
////    @PerUseCase
////    SecondaryUseCase.Backend provideBackend() {
////        return new BackendImpl();
////    }
////
////    @Provides
////    @PerUseCase
////    SecondaryUseCase.Authenticator provideAuthenticator(AuthenticationFlow authenticationFlow) {
////        return authenticationFlow;
////    }
////
////    @Provides
////    @PerUseCase
////    SecondaryUseCase.UI provideUser() {
////        return UI;
////    }
//
//}
