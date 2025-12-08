pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {

        // 添加国内镜像源
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }

        // 华为镜像
        maven { url = uri("https://repo.huaweicloud.com/repository/maven/") }

        // 腾讯云镜像
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }

        // 可选：将官方源放在镜像后面作为备用
        maven { url = uri("https://dl.google.com/dl/android/maven2/") }
        maven { url = uri("https://repo.maven.apache.org/maven2") }

        google()
        mavenCentral()

    }
}

rootProject.name = "My Application"
include(":app")
