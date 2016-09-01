package the.autarch.annotatedparseobject.compiler;

import com.google.auto.service.AutoService;
import com.google.common.collect.Sets;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import the.autarch.annotatedparseobject.api.AnnotatedParseObject;
import the.autarch.annotatedparseobject.api.ParseKey;

@AutoService(Processor.class)
public class AnnotatedParseObjectAnnotationProcessor extends AbstractProcessor {

    private Messager messager;
    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Map<String, AnnotatedParseObjectAnnotatedClass> annotatedClasses = new LinkedHashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Sets.newHashSet(
                AnnotatedParseObject.class.getCanonicalName(),
                ParseKey.class.getCanonicalName()
        );
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        try {

            for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(AnnotatedParseObject.class)) {

                log("good to here");

                // Check if a class has been annotated with @AnnotatedParseClass
                if (annotatedElement.getKind() != ElementKind.CLASS) {
                    error(annotatedElement, "Only classes can be annotated with @%s", AnnotatedParseObject.class.getSimpleName());
                    return true;
                }

                // We can cast it, because we know that it is of ElementKind.CLASS
                TypeElement typeElement = (TypeElement) annotatedElement;
                AnnotatedParseObjectAnnotatedClass annotatedClass = new AnnotatedParseObjectAnnotatedClass(typeElement);
                if(!isValid(annotatedClass)) {
                    return true;
                }

                // Everything is fine, so try to add
                if(!annotatedClasses.containsKey(annotatedClass.getParseClassName())) {
                    annotatedClasses.put(annotatedClass.getParseClassName(), annotatedClass);
                }
            }

            // Generate code
            for (AnnotatedParseObjectAnnotatedClass annotatedClass : annotatedClasses.values()) {
                try {
                    annotatedClass.generateCode(elementUtils, filer);
                } catch(Exception e) {
                    error(annotatedClass.getTypeElement(), "got error: %s", e);
                    return true;
                }
            }

            annotatedClasses.clear();

        } catch (Exception e) {
            error(null, e.getMessage());
        }

        return true;
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e
        );
    }

    private void log(String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.NOTE,
                String.format(msg, args)
        );
    }

    private boolean isValid(AnnotatedParseObjectAnnotatedClass item) {

        // Cast to TypeElement, has more type specific methods
        TypeElement classElement = item.getTypeElement();

        if (!classElement.getModifiers().contains(Modifier.PUBLIC)) {
            error(classElement, "The class %s is not public.", classElement.getQualifiedName().toString());
            return false;
        }

        // Check if it's an abstract class
        if (classElement.getModifiers().contains(Modifier.ABSTRACT)) {
            error(classElement, "The class %s is abstract. You can't annotate abstract classes with @%", classElement.getQualifiedName().toString(), AnnotatedParseObject.class.getSimpleName());
            return false;
        }

        // Check inheritance: Class must be childclass as specified in @Factory.type();
//        TypeElement superClassElement = elementUtils.getTypeElement(item.getQualifiedFactoryGroupName());
//        if (superClassElement.getKind() == ElementKind.INTERFACE) {
//
//            // NOTE: we have to do manual iteration here because Kotlin classes cause TypeMirror#equals()
//            // to fail, but Types#isSameType() still works (more info: http://michaelevans.org/blog/2016/02/17/using-dagger-1-and-kotlin/)
//            boolean foundSameType = false;
//            for(TypeMirror iface : classElement.getInterfaces()) {
//                if(typeUtils.isSameType(superClassElement.asType(), iface)) {
//                    foundSameType = true;
//                    break;
//                }
//            }
//
////            if (!classElement.getInterfaces().contains(superClassElement.asType())) {
//            if(!foundSameType) {
//                error(classElement, "The class %s annotated with @%s must implement the interface %s", classElement.getQualifiedName().toString(), AnnotatedParseObject.class.getSimpleName(), item.getQualifiedFactoryGroupName());
//                return false;
//            }
//        } else {
//            // Check subclassing
//            TypeElement currentClass = classElement;
//            while (true) {
//                TypeMirror superClassType = currentClass.getSuperclass();
//
//                if (superClassType.getKind() == TypeKind.NONE) {
//                    // Basis class (java.lang.Object) reached, so exit
//                    error(classElement, "The class %s annotated with @%s must inherit from %s", classElement.getQualifiedName().toString(), AnnotatedParseObject.class.getSimpleName(), item.getQualifiedFactoryGroupName());
//                    return false;
//                }
//
//                if (superClassType.toString().equals(item.getQualifiedFactoryGroupName())) {
//                    // Required super class found
//                    break;
//                }
//
//                // Moving up in inheritance tree
//                currentClass = (TypeElement) typeUtils.asElement(superClassType);
//            }
//        }

        // Check if an empty public constructor is given
        for (Element enclosed : classElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructorElement = (ExecutableElement) enclosed;
                if (constructorElement.getParameters().size() == 0 && constructorElement.getModifiers().contains(Modifier.PUBLIC)) {
                    // Found an empty constructor
                    return true;
                }
            }
        }

        // No empty constructor found
        error(classElement, "The class %s must provide an public empty default constructor", classElement.getQualifiedName().toString());
        return false;
    }
}
