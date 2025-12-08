// 应用插件部分 - 声明项目使用的插件
plugins {
    // 应用 Android 应用插件，使用版本目录中定义的版本
    alias(libs.plugins.android.application)
}

// Android 配置块 - 定义应用的所有 Android 特定设置
android {
    // 应用的命名空间，通常与包名相同
    namespace = "com.example.myapplication"

    // 编译 SDK 配置
    compileSdk {
        // 使用 Android 36 版本进行编译
        version = release(36)
    }

    // 默认配置块 - 定义应用的基本属性
    defaultConfig {
        // 应用的唯一标识符，用于在 Google Play 等应用商店中标识应用
        applicationId = "com.example.myapplication"

        // 应用支持的最低 Android SDK 版本（Android 7.0 Nougat）
        minSdk = 24

        // 应用针对的目标 Android SDK 版本
        targetSdk = 36

        // 应用的版本代码，用于内部版本控制和升级检查
        versionCode = 1

        // 应用的版本名称，显示给用户
        versionName = "1.0"

        // 用于集成测试的测试运行器
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // 构建类型配置 - 定义不同的构建变体
    buildTypes {
        // Release 构建类型配置
        release {
            // 是否启用代码混淆（当前为关闭状态）
            isMinifyEnabled = false

            // 配置 ProGuard 文件，用于代码混淆和优化
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // 编译选项配置
    compileOptions {
        // 指定 Java 源代码兼容性版本
        sourceCompatibility = JavaVersion.VERSION_11

        // 指定生成的类文件的目标 Java 版本
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

// 依赖项配置块 - 声明项目所需的外部库和模块
dependencies {
    // 实现 androidx.appcompat 库，提供向后兼容的 UI 组件
    implementation(libs.appcompat)

    // 实现 Material Design 组件库
    implementation(libs.material)

    // 实现 Activity 库，提供 Activity 相关功能
    implementation(libs.activity)

    // 实现 ConstraintLayout 布局库
    implementation(libs.constraintlayout)
    implementation(libs.swiperefreshlayout)
    implementation(libs.vectordrawable)
    // 测试依赖 - JUnit 测试框架
    testImplementation(libs.junit)

    // Android 测试依赖 - AndroidX Test JUnit 扩展
    androidTestImplementation(libs.ext.junit)

    // Android 测试依赖 - Espresso UI 测试框架核心库
    androidTestImplementation(libs.espresso.core)

    // 实现ViewPager2页面切换
    implementation("androidx.viewpager2:viewpager2:1.1.0")

    // 实现RecyclerView 用于瀑布流列表
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // 实现CardView 用于卡片布局
    implementation("androidx.cardview:cardview:1.0.0")

    // 实现Glide 图片加载库
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    // 引入Gson库，用于JSON解析
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.github.bumptech.glide:glide:4.13.2")
    annotationProcessor("com.github.bumptech.glide:compiler:4.13.2")

    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.5.1")
    implementation("androidx.lifecycle:lifecycle-livedata:2.5.1")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.5.1")
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // 添加ExoPlayer音乐播放器依赖
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.media3:media3-session:1.3.1")

}