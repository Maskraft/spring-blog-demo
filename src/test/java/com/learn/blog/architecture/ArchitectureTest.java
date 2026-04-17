package com.learn.blog.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;

// プロジェクトのアーキテクチャ制約を ArchUnit で検証する。
// 規約で定めたレイヤー構造・パッケージ配置・アノテーション配置からの逸脱を、ビルド時に検知する。
@AnalyzeClasses(
        packages = "com.learn.blog",
        importOptions = {ImportOption.DoNotIncludeTests.class})
class ArchitectureTest {

    // レイヤー依存方向：controller → service → repository のみを許可する。
    // entity / dto / exception は横断的に使用されるため依存元を限定しない。
    @ArchTest
    static final ArchRule layeredArchitecture =
            Architectures.layeredArchitecture()
                    .consideringAllDependencies()
                    .layer("Controller")
                    .definedBy("com.learn.blog.controller..")
                    .layer("Service")
                    .definedBy("com.learn.blog.service..")
                    .layer("Repository")
                    .definedBy("com.learn.blog.repository..")
                    .whereLayer("Controller")
                    .mayNotBeAccessedByAnyLayer()
                    .whereLayer("Service")
                    .mayOnlyBeAccessedByLayers("Controller")
                    .whereLayer("Repository")
                    .mayOnlyBeAccessedByLayers("Service");

    // @RestController / @Controller 付きクラスは controller パッケージに配置する
    @ArchTest
    static final ArchRule controllersShouldResideInControllerPackage =
            classes()
                    .that()
                    .areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                    .or()
                    .areAnnotatedWith(org.springframework.stereotype.Controller.class)
                    .should()
                    .resideInAPackage("..controller..");

    // @Service 付きクラスは service パッケージに配置する
    @ArchTest
    static final ArchRule servicesShouldResideInServicePackage =
            classes()
                    .that()
                    .areAnnotatedWith(org.springframework.stereotype.Service.class)
                    .should()
                    .resideInAPackage("..service..");

    // @Repository 付き or JpaRepository 継承クラスは repository パッケージに配置する
    @ArchTest
    static final ArchRule repositoriesShouldResideInRepositoryPackage =
            classes()
                    .that()
                    .areAnnotatedWith(org.springframework.stereotype.Repository.class)
                    .or()
                    .areAssignableTo(org.springframework.data.jpa.repository.JpaRepository.class)
                    .should()
                    .resideInAPackage("..repository..");

    // @Entity 付きクラスは entity パッケージに配置する
    @ArchTest
    static final ArchRule entitiesShouldResideInEntityPackage =
            classes()
                    .that()
                    .areAnnotatedWith(jakarta.persistence.Entity.class)
                    .should()
                    .resideInAPackage("..entity..");

    // @Transactional はサービス層にのみ付与する（規約: @Transactional はサービス層に記述）
    @ArchTest
    static final ArchRule transactionalShouldOnlyBeUsedInServiceLayer =
            noClasses()
                    .that()
                    .resideOutsideOfPackage("..service..")
                    .should()
                    .beAnnotatedWith(org.springframework.transaction.annotation.Transactional.class)
                    .orShould()
                    .beAnnotatedWith("org.springframework.transaction.annotation.Transactional");
}
