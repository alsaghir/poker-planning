This is a Kotlin Multiplatform project targeting Web, Desktop (JVM).

### Build and Run

 ```shell
 gradle :composeApp:hotDevJvm
 gradle :composeApp:jvmRun
 gradle :composeApp:wasmJsBrowserDevelopmentRun
 gradle :composeApp:jsBrowserDevelopmentRun
 ```

--- 

### Run Tests

```shell
gradle :composeApp:jvmTest --rerun-tasks
gradle :composeApp:jsTest --rerun-tasks --info --stacktrace --console=plain
gradle :composeApp:wasmJsTest --rerun-tasks --info --stacktrace --console=plain
```