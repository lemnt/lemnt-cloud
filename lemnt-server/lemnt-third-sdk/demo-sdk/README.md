# demo-sdk

最小第三方 SDK 示例，使用 Java 17 自带的 HTTP Client，无额外依赖。

```java
DemoClient client = new DemoClient();
HttpResponse<String> response = client.get(URI.create("http://localhost:8080/demo"));
```

调用方按状态码处理响应，并负责处理 IOException 和 InterruptedException。
示例不会自动注册为 Spring Bean，也不会在启动时发起请求。
