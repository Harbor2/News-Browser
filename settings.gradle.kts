pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Pangle
        maven { url = uri("https://artifact.bytedance.com/repository/pangle") }

        // mintergral
        maven { url = uri("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea") }

        // IronSource
        maven { url = uri("https://android-sdk.is.com") }

        // Vungle
        maven { url = uri("https://s01.oss.sonatype.org/content/groups/staging/") }

        // Chartboost
        maven { url = uri("https://cboost.jfrog.io/artifactory/chartboost-ads/") }

        // Appnext
        maven { url = uri("https://dl.appnext.com/") }

        // Anythink
        maven { url = uri("https://jfrog.anythinktech.com/artifactory/overseas_sdk") }

        // Huawei
        maven { url = uri("https://developer.huawei.com/repo/") }

        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
    }
}

rootProject.name = "News Browser"
include(":app")
 