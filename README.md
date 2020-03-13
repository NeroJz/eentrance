# Handheld QR Code Scanner

An android application to scan QR code.

This project implements QR code scanning using camera. The scanned text is validated
by calling the RESTful API. Implementing these features allow to explore the use of
Retrofit2 and RxJava2 libraries in order to handle the asynchronous and event-based
response returned by the API.

## Gettting Started

### Prerequisites

This application requires minimum Java 8+ or Android API 21+.

## Libraries

**[GSON](https://github.com/google/gson)** is Java library that can be used to convert Java Object into JSON representation. <br/>
**[Retrofit2](https://github.com/square/retrofit)** is a type-safe HTTP client for Android and Java. It uses for RESTful API calling.<br/>
**[RxJava2](https://github.com/ReactiveX/RxJava)** is a library for composing asynchronous and event-based programs by using observable sequences.<br/>
**[Zing](https://github.com/pethoalpar/ZxingExample)** is a library for reading a barcode.<br/>
**[Scalars Converter](https://github.com/square/retrofit/tree/master/retrofit-converters/scalars)** supports converting strings and both primitives and their boxed types to text/plain bodies.

### Build with

- [Android Studio](https://developer.android.com/studio)
- [Gradle](https://spring.io/guides/gs/gradle/)
