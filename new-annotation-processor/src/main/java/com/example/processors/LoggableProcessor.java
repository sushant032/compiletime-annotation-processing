package com.example.processors;

import com.example.annotations.Loggable;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes({"com.example.annotations.Loggable"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class LoggableProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Loggable.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement classElement = (TypeElement) element;
                generateClassWithLogging(classElement);
            }
        }
        return true;
    }

    private void generateClassWithLogging(TypeElement classElement) {
        String className = classElement.getSimpleName() + "WithLogging";
        String packageName = processingEnv.getElementUtils().getPackageOf(classElement).toString();
        String originalClassName = classElement.getSimpleName().toString();

        try {
            // Create a new source file
            JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(packageName + "." + className);
            try (Writer writer = builderFile.openWriter()) {
                writer.write("package " + packageName + ";\n\n");
                writer.write("public class " + className + " extends " + originalClassName + " {\n");

                // Add logging to methods
                for (ExecutableElement method : ElementFilter.methodsIn(classElement.getEnclosedElements())) {
                    String methodName = method.getSimpleName().toString();
                    writer.write("  @Override\n");
                    writer.write("  public " + method.getReturnType() + " " + methodName + "() {\n");
                    writer.write("    System.out.println(\"Entering method: " + methodName + "\");\n");
                    writer.write("    super." + methodName + "();\n");
                    writer.write("    System.out.println(\"Exiting method: " + methodName + "\");\n");
                    writer.write("  }\n");
                }

                writer.write("}\n");
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }
}
