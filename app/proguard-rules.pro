# Gson DTOs are serialized by their explicitly named fields.
-keep class com.brewkery.app.data.remote.*Dto { *; }
-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault
