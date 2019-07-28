package com.morkim.tectonic.compiler;

import com.morkim.tectonic.annotation.EventTrigger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

public final class EventTriggerProcessor extends AbstractProcessor {

    private Messager messager;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(EventTrigger.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        messager = processingEnvironment.getMessager();
    }

    private class EventParams {

        Object value;
        String fileName;
        Name name;
    }

    private Map<Object, EventParams> events = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {

        // Only one annotation, so just use annotations.iterator().next();
        try {
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(annotations.iterator().next());

            Set<VariableElement> fields = ElementFilter.fieldsIn(elements);
            for (VariableElement field : fields) {

                EventParams params = new EventParams();
                params.value = field.getConstantValue();
                params.name = field.getSimpleName();
                params.fileName = field.getEnclosingElement().asType().toString();

                EventParams existingEvent = events.get(params.value);
                if (existingEvent != null) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "event " + params.name + " with value " + params.value + " in file " + params.fileName + " already exists with name " + existingEvent.name + " in " + existingEvent.fileName);
                } else {
                    events.put(field.getConstantValue(), params);
                }
            }

            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}
