# Add project specific ProGuard rules here.

# OkHttp (via ktor-client-okhttp) optionally logs through SLF4J if present on
# the classpath; it isn't a real dependency here, so R8 can't resolve it.
# Safe to ignore rather than keep — the app never provides an SLF4J binding.
# (Same fix MEND needed after its first real release build — see
# github.com/WhymzikalZyxxyZ/mend, proguard-rules.pro.)
-dontwarn org.slf4j.**
