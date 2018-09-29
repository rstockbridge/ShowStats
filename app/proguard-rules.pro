# Gson uses generic type information stored in a class file when working with fields.
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature
# Gson specific classes
-keep class sun.misc.Unsafe { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.github.rstockbridge.showstats.api.models.** { *; }
-keep class com.github.rstockbridge.showstats.database.** { *; }