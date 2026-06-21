# Add project specific ProGuard rules here.
# By default, the active-sitting/ergonomic tracking models and entities are preserved.
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}
