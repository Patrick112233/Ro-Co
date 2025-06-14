package de.th_rosenheim.ro_co.arch;

import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.validation.Valid;
import org.junit.jupiter.api.Tag;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.annotation.Annotation;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

@SuppressWarnings("unused")
@Tag("architecture")
@AnalyzeClasses(packages = "de.th_rosenheim.ro_co.restapi", importOptions = {ImportOption.DoNotIncludeTests.class, ImportOption.DoNotIncludeJars.class})
public class ArchitectureAnnotationTest {

    @ArchTest
    public static final ArchRule swaggerAnnotation = methods()
            .that().areAnnotatedWith("org.springframework.web.bind.annotation.RequestMapping")
            .or().areAnnotatedWith("org.springframework.web.bind.annotation.GetMapping")
            .or().areAnnotatedWith("org.springframework.web.bind.annotation.PostMapping")
            .or().areAnnotatedWith("org.springframework.web.bind.annotation.PutMapping")
            .or().areAnnotatedWith("org.springframework.web.bind.annotation.DeleteMapping")
            .should().beAnnotatedWith("io.swagger.v3.oas.annotations.Operation");


    @ArchTest
    public static final ArchRule validAnnotation = methods()
            .that().areAnnotatedWith("org.springframework.web.bind.annotation.RequestMapping")
            .or().areAnnotatedWith("org.springframework.web.bind.annotation.GetMapping")
            .or().areAnnotatedWith("org.springframework.web.bind.annotation.PostMapping")
            .or().areAnnotatedWith("org.springframework.web.bind.annotation.PutMapping")
            .or().areAnnotatedWith("org.springframework.web.bind.annotation.DeleteMapping")
            .should(parameterHasAnnotationIfAnnotatedWith(RequestBody.class, Valid.class));


    private static ArchCondition<JavaMethod> parameterHasAnnotationIfAnnotatedWith( Class<? extends Annotation> ifAnnotation, Class<? extends Annotation> annotatedWith) {
        return new ArchCondition<>("a parameter annotated with " +ifAnnotation.getCanonicalName()+ " also be annotated with " + annotatedWith.getCanonicalName()) {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                boolean satisfiesAnnotation = true;
                for (JavaParameter p : method.getParameters()){
                    if (p.isAnnotatedWith(ifAnnotation)) {
                        satisfiesAnnotation = p.isAnnotatedWith(annotatedWith);
                        break;
                    }
                }
                String message = "Method " + method.getName() + " in " + method.getOwner().getFullName()  + " has" + (satisfiesAnnotation? " ": " missing")+ annotatedWith.getCanonicalName() + "  annotation for " + ifAnnotation.getCanonicalName() + " annotated parameter";
                events.add(new SimpleConditionEvent(method, satisfiesAnnotation, message));
            }
        };
    }


}
