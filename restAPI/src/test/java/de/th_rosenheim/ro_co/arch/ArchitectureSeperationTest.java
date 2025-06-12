package de.th_rosenheim.ro_co.arch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@SuppressWarnings("unused")
@Tag("architecture")
@AnalyzeClasses(packages = "de.th_rosenheim.ro_co.restapi", importOptions = {ImportOption.DoNotIncludeTests.class, ImportOption.DoNotIncludeJars.class})
public class ArchitectureSeperationTest {

    public static final String[] ALWAYS_ALLOWED_PACKAGES = {
            "..springframework..",
            "..java..",
            "..exceptions..",
            "..slf4j..",
            "..lombok..",
            "..security..",
            "..aopalliance..",
            "..jakarta.."
    };
    //ther should not be a dependecy between any controller_

    @ArchTest
    public static final ArchRule AllowedControllerDependencies = classes()
            .that().resideInAPackage("..controller..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(ArrayUtils.addAll(new String[]{"..controller..", "..service..", "..dto..", "..swagger..", "..apache.."}, ALWAYS_ALLOWED_PACKAGES));


    @ArchTest
    public static final ArchRule AllowedDtoDependencies = classes()
            .that().resideInAPackage("..dto..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(ArrayUtils.addAll(new String[]{"..dto.."}, ALWAYS_ALLOWED_PACKAGES));

    @ArchTest
    public static final ArchRule AllowedMapperDependencies = classes()
            .that().resideInAPackage("..mapper..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(ArrayUtils.addAll(new String[]{"..mapper..", "..model..", "..dto..","..mapstruct.." }, ALWAYS_ALLOWED_PACKAGES));

    @ArchTest
    public static final ArchRule AllowedModelDependencies = classes()
            .that().resideInAPackage("..model..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(ArrayUtils.addAll(new String[]{"..model..", "..bson..", "..unirest.."}, ALWAYS_ALLOWED_PACKAGES));


    @ArchTest
    public static final ArchRule AllowedRepositoryDependencies = classes()
            .that().resideInAPackage("..repository..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(ArrayUtils.addAll(new String[]{"..repository..", "..model.." }, ALWAYS_ALLOWED_PACKAGES));


    @ArchTest
    public static final ArchRule AllowedServiceDependencies = classes()
            .that().resideInAPackage("..service..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(ArrayUtils.addAll(new String[]{"..service..", "..dto..", "..mapper..", "..model..", "..repository..", "..security.." , "..jsonwebtoken..", "..bson.."}, ALWAYS_ALLOWED_PACKAGES));


}
