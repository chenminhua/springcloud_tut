## service discovery

netfilx eureka
zookeeper
consul

### 使用方式
1. 首先需要运行服务注册中心，可参考eureka-service服务。在主类上添加@EnableEurekaServer，并添加相应application.properties属性

2. 业务服务需要配置客户端，引入spring-cloud-starter-netflix-eureka-client，并在主类上启用@EnableDiscoveryClient

3. 在客户端还需要配置 eureka.client.serviceUrl.defaultZone

4. 去到服务注册中心的web页面检查是否服务已经注册成功。

5. 服务消费者也需要引入引入spring-cloud-starter-netflix-eureka-client，并在主类上启用@EnableDiscoveryClient

6. 消费者在消费时不再需要知道服务的ip和端口，只需要知道服务的名称即可

### 高可用注册中心
一组相互注册的双节点服务注册中心

### 服务提供者
“服务提供者” 在启动的时候会通过发送REST请求的方式将自己注册到EurekaServer上，同时带上了自身服务的一些元数据信息。 Eureka Server接收到这个REST请求之后，
将元数据信息存储在一个双层结构Map中， 其中第一层的key是服务名， 第二层的key是 具体服务的实例名。

eureka.client.register-with-eureka=true 该值默认为true。 若设置为false将不会启动注册操作。

在注册完服务之后，服务提供者会维护一个心跳到EurekaServer，以防止被从服务列表中排除出去。
eureka.instance.lease-renewal-interval-in-seconds=30    服务续约任务的间隔时间
eureka.ins七ance.lease-expiration-duration-in-seconds=90   定义服务失效的时间

### 服务消费者
获取服务是服务消费者的基础，所以必须确保eureka.client.fetch-registry=true
eureka.client.registry-fetch-interval-seconds=30 表示缓存的更新时间

服务消费者在获取服务清单后，通过服务名可以获得具体提供服务的实例名和该实例的元数据信息。 
因为有这些服务实例的详细信息，所以客户端可以根据自己的需要决定具体调用哪个实例，在ribbon中会默认采用轮询的方式进行调用，从而实现客户端的负载均衡。
对于访问实例的选择，Eureka中有Region和Zone的概念，一个Region中可以包含多个Zone, 每个服务客户端需要被注册到一个Zone中，所以每个客户端对应一个Region和一个Zone。 
在进行服务调用的时候，优先访问同处一个Zone中的服务提供方，若访问不到，就访问其他的Zone。

这里的两个服务提供者分别注册到了两个不同的服务注册中心上，也就是说，它们的信息分别被两个服务注册中心所维护。
此时，当服务提供者发送注册请求到一个服务注册中心时，它会将该请求转发给集群中相连的其他注册中心，从而实现注册中心之间的服务同步。
通过服务同步，两个服务提供者的服务信息就可以通过这两台服务注册中心中的任意一台获取到。

### 服务下线
当服务正常关闭时，会触发一个下线操作，告诉eureka server自己将下线。
eureka server本身也存在一个定时任务，定时清理超期没有发送心跳包的服务。

## 源码分析

```$xslt
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({EnableDiscoveryClientImportSelector.class})
public @interface EnableDiscoveryClient {
    boolean autoRegister() default true;
}
```
这个注解是用来开启DiscoveryClient的，DiscoveryClient是一个接口，而EurekaDiscoveryClient则是其实现类。


### eureka的基础架构

### eureka的服务治理机制

### eureka的配置