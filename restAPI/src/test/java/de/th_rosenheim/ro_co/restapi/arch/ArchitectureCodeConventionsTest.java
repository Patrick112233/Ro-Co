package de.th_rosenheim.ro_co.restapi.arch;


import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Tag;
import org.mapstruct.Mapper;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@SuppressWarnings("unused")
@Tag("architecture")
@AnalyzeClasses(packages = "de.th_rosenheim.ro_co.restapi", importOptions = {ImportOption.DoNotIncludeTests.class, ImportOption.DoNotIncludeJars.class})
public class ArchitectureCodeConventionsTest {

    /*Class Naming*/
    @ArchTest
    public static final ArchRule ControllerNaming = classes().that().areAnnotatedWith(RestController.class).should().haveSimpleNameEndingWith("Controller").because("Controller classes should end with 'Controller' to indicate their purpose.");

    @ArchTest
    public static final ArchRule DtoNaming = classes().that().resideInAPackage("..dto..").should().haveSimpleNameEndingWith("Dto").because("DTO classes should end with 'Dto' to indicate their purpose.");

    @ArchTest
    public static final ArchRule ExceptionNaming = classes()
        .that().areAssignableTo(Exception.class).or().areAssignableTo(RuntimeException.class)
        .should().haveSimpleNameEndingWith("Exception")
        .because("Exception classes should end with 'Exception' to indicate their purpose.");

    @ArchTest
    public static final ArchRule MapperNaming = classes().that().areAnnotatedWith(Mapper.class).should().haveSimpleNameEndingWith("Mapper").because("Mapper classes should end with 'Mapper' to indicate their purpose.");

    @ArchTest
    public static final ArchRule ServiceNaming = classes().that().areAnnotatedWith(Service.class).should().haveSimpleNameEndingWith("Service").because("Service classes should end with 'Service' to indicate their purpose.");

    @ArchTest
    public static final ArchRule RepositoryNaming = classes().that().areAssignableTo(MongoRepository.class).should().haveSimpleNameEndingWith("Repository").because("Repository classes should end with 'Repository' to indicate their purpose.");




    /*Check Class location*/

    @ArchTest
    public static final ArchRule ControllerPackage = classes()
            .that().areAnnotatedWith(RestController.class)
            .should().resideInAPackage("..controller..")
            .because("Controller classes should reside in the 'controller' package.");

    @ArchTest
    public static final ArchRule DtoPackage = classes()
            .that().haveSimpleNameEndingWith("Dto")
            .should().resideInAPackage("..dto..")
            .because("DTO classes should reside in the 'dto' package.");

    @ArchTest
    public static final ArchRule ExcaptionPackage = classes()
            .that().areAssignableTo(Exception.class).or().areAssignableTo(RuntimeException.class)
            .should().resideInAPackage("..exceptions..")
            .because("Exception classes should reside in the 'exceptions' package.");

    @ArchTest
    public static final ArchRule MapperPackage = classes()
            .that().areAnnotatedWith(Mapper.class)
            .should().resideInAPackage("..mapper..")
            .because("Mapper classes should reside in the 'mapper' package.");


    @ArchTest
    public static final ArchRule DocumentPackage = classes()
            .that().areAnnotatedWith(org.springframework.data.mongodb.core.mapping.Document.class)
            .should().resideInAPackage("..model..").because("Data classes should reside in the 'model' package.");

    @ArchTest
    public static final ArchRule RepositoryPackage = classes()
            .that().areAssignableTo(MongoRepository.class)
            .should().resideInAPackage("..repository..")
            .because("Repository classes should reside in the 'repository' package.");

    @ArchTest
    public static final ArchRule ServicePackage = classes()
            .that().areAnnotatedWith(Service.class)
            .should().resideInAPackage("..service..")
            .because("Service classes should reside in the 'service' package.");



}
