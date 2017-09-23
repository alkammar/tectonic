package com.morkim.tectonic;

import com.morkim.tectonic.entities.PrerequisiteTestUseCase;
import com.morkim.tectonic.entities.TestUseCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class PrerequisiteTest {

    private long prerequisite1TimeStamp;
    private long prerequisite2TimeStamp;
    private long prerequisite3TimeStamp;
    private long mainTimeStamp;

    @Before
    public void setup() {

        prerequisite1TimeStamp = 0;
        prerequisite2TimeStamp = 0;
        prerequisite3TimeStamp = 0;
        mainTimeStamp = 0;
    }

    @Test
    public void execute_prerequisitesExecuted() throws Exception {

        TestUseCase useCase;

        useCase = new MainTestUseCase();
        useCase.subscribe(new UseCase.OnCompleteListener() {
            @Override
            public void onComplete() {
                mainTimeStamp = System.nanoTime();
            }
        })
                .execute();

        assertNotEquals(0, prerequisite1TimeStamp);
        assertEquals(0, prerequisite2TimeStamp);
        assertNotEquals(0, prerequisite3TimeStamp);
        assertNotEquals(0, mainTimeStamp);
        assertTrue(prerequisite1TimeStamp < mainTimeStamp);
        assertTrue(prerequisite1TimeStamp < prerequisite3TimeStamp);
        assertTrue(prerequisite3TimeStamp < mainTimeStamp);
    }

    @Test
    public void reexecute_prerequisitesExecuted() throws Exception {

        TestUseCase useCase;

        useCase = new MainTestUseCase();
        useCase.subscribe(new UseCase.OnCompleteListener() {
            @Override
            public void onComplete() {
                mainTimeStamp = System.nanoTime();
            }
        }).execute();

        prerequisite1TimeStamp = 0;
        prerequisite2TimeStamp = 0;
        prerequisite3TimeStamp = 0;
        mainTimeStamp = 0;

        useCase.execute();

        assertNotEquals(0, prerequisite1TimeStamp);
        assertEquals(0, prerequisite2TimeStamp);
        assertNotEquals(0, prerequisite3TimeStamp);
        assertNotEquals(0, mainTimeStamp);
        assertTrue(prerequisite1TimeStamp < mainTimeStamp);
        assertTrue(prerequisite1TimeStamp < prerequisite3TimeStamp);
        assertTrue(prerequisite3TimeStamp < mainTimeStamp);
    }

    private class MainTestUseCase extends TestUseCase {

        @Override
        protected void onAddPrerequisites() {

            addPrerequisite(
                    PrerequisiteTestUseCase.class,
                    new UseCase.OnCompleteListener() {
                        @Override
                        public void onComplete() {
                            prerequisite1TimeStamp = System.nanoTime();
                        }
                    });

            addPrerequisite(
                    TestUseCase.class,
                    false,
                    new OnCompleteListener() {
                        @Override
                        public void onComplete() {
                            prerequisite2TimeStamp = System.nanoTime();
                        }
                    });

            addPrerequisite(
                    TestUseCase.class,
                    new OnCompleteListener() {
                        @Override
                        public void onComplete() {
                            prerequisite3TimeStamp = System.nanoTime();
                        }
                    });
        }
    }

}