package lib.morkim.di;

public class UseCaseInjector {

    private static SecondaryUseCaseComponent secondaryUseCaseComponent;

    public static SecondaryUseCaseComponent getSecondaryUseCaseComponent() {
        return secondaryUseCaseComponent;
    }

    public static void setSecondaryUseCaseComponent(SecondaryUseCaseComponent secondaryUseCaseComponent) {
        UseCaseInjector.secondaryUseCaseComponent = secondaryUseCaseComponent;
    }
}
