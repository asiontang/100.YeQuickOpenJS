# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Program Files\Android\android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
# 将.class信息中的类名重新定义为"Proguard"字符串
#-renamesourcefileattribute _
# 并保留源文件名为"Proguard"字符串，而非原始的类名 并保留行号
-keepattributes SourceFile,LineNumberTable

#指定代码的压缩级别
-optimizationpasses 10

#包明不混合大小写
#-dontusemixedcaseclassnames
#不去忽略非公共的库类
#-dontskipnonpubliclibraryclasses
#优化  不优化输入的类文件
#-dontoptimize
#预校验
#-dontpreverify
#混淆时是否记录日志
#-verbose
# 混淆时所采用的算法
# -optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

#忽略警告
#-ignorewarning

#===================================================================================================
# #apk 包内所有 class 的内部结构
# -dump class_files.txt
# #未混淆的类和成员
# -printseeds seeds.txt
# #列出从 apk 中删除的代码
# -printusage unused.txt
# #混淆前后的映射
# -printmapping mapping.txt

#===================================================================================================
#友盟
#-libraryjars libs/umeng-analytics-v5.2.4.jar
#-keep class com.umeng.**{*;}

#忽略警告
#-dontwarn com.veidy.mobile.common.**

#保留一个完整的包
#-keep class com.veidy.mobile.common.** {*;}

#===================================================================================================
# 保持自定义控件类不被混淆
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
