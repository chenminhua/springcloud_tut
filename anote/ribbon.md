客户端负载均衡

## 使用方法

服务提供者只需要启动多个服务实例并注册到一个注册中心或是多个相关联的服务 注册中心。
服务消费者直接通过调用被@LoadBalanced 注解修饰过的 RestTemplate 来实现面 向服务的接口调用。

## 实现原理

探索一下Ribbon是如何通过 RestTemplate 实现客户端负载均衡的。

@LoadBalanced

```$xslt
public interface LoadBalancerClient extends ServiceInstanceChooser {
    // 使用从负载均衡器中挑选出的服务实例来执行请求内容 。
    <T> T execute(String serviceId, LoadBalancerRequest<T> request) throws IOException;
    <T> T execute(String serviceId, ServiceInstance serviceInstance, LoadBalancerRequest<T> request) throws IOException;
    为系统构建 一 个合适的host:po江形式的URI。 在分布式系统中， 我们使用逻辑 上的服务名称作为host来构建URIC替代服务实例的host:port形式)进行请求， 比如http://myservice/pa印/七o/service 。 在该操作 的 定 义中 ， 前者 Servic釭nstance对象是带有host和port的具体服务实例 ， 而后者URI对象则
    是使 用 逻 辑服 务名定 义为host的URI , 而返回的URI内 容则是通 过 Servic釭ns七ance的服务实例详情拼接出的具体host:pos七形式的请求地址。

    URI reconstructURI(ServiceInstance instance, URI original);
}

public interface ServiceInstanceChooser {
    //根据传入的服务名 serviceld,从负载均衡器中挑选一个对应服务的实例 。
    ServiceInstance choose(String serviceId);
}
```
 
从 LoadBalancerAutoConfiguration 类头上的注解可以知道，Ribbon实现的负载均衡自动化配置需要满足下面两个条件。

```$xslt
// 有RestTemplate类存在
@ConditionalOnClass({RestTemplate.class})
有LoadBalancerClient存在
@ConditionalOnBean({LoadBalancerClient.class})
```

在该自动化配置类中， 主要做了下面三件事:
 • 创建了一个 LoadBalancerInterceptor 的Bean, 实现对客户端发起请求时进行拦截，以实现客户端负载均衡。
 • 创建了一个 RestTemplateCustomizer 的 Bean, 用于给 RestTemplate 增加 LoadBalancerInterceptor 拦截器。
 • 维护了一个被@LoadBalanced 注解修饰的 RestTemplate 对象列表，并在这里进行初始化。
 
接下来， 我们看看 LoadBalancerInterceptor 拦截器是如何将一个普通的 RestTemplate 变成客户端负载均衡的:
 
```$xslt
public class LoadBalancerInterceptor implements ClientHttpRequestInterceptor {
    private LoadBalancerClient loadBalancer;
    private LoadBalancerRequestFactory requestFactory;

    public LoadBalancerInterceptor(LoadBalancerClient loadBalancer, LoadBalancerRequestFactory requestFactory) {
        this.loadBalancer = loadBalancer;
        this.requestFactory = requestFactory;
    }

    public LoadBalancerInterceptor(LoadBalancerClient loadBalancer) {
        this(loadBalancer, new LoadBalancerRequestFactory(loadBalancer));
    }

    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException {
        URI originalUri = request.getURI();
        String serviceName = originalUri.getHost();
        Assert.state(serviceName != null, "Request URI does not contain a valid hostname: " + originalUri);
        return (ClientHttpResponse)this.loadBalancer.execute(serviceName, this.requestFactory.createRequest(request, body, execution));
    }
}
```

在拦截器中注入了 LoadBalancerClient 的实现。 
当一个被 @LoadBalanced 注解修饰的 RestTemplate 对象向外发起 HTTP 请求时，会被 LoadBalancerInterceptor 类的 intercept 函数所拦截。
由于我们在使用 RestTemplate 时采用了服务名作为 host, 所以直接从 HttpRequest 的URI对象中 通过 getHost ()就可以拿到服务名，然后调用 execute 函数去根据服务名来选择实例并发起实际的请求。

下面我们来看下RibbonLoadBalancerClient的实现


## 负载均衡器

虽然 Spring Cloud中定义了LoadBalancerC巨e工作为负载均衡器的通用接口， 并且针对 R巾bon实现了贮bbonLoadBalancerClien七， 但是 它在具体实现客户端负载均衡时，
是通过伈bbon的ILoadBalancer接口实现的。 在上一节进行分析时候， 我们对该接口 的实现结构已经做了一 些简单的介绍， 下面我们根据ILoadBalancer接口的实现类逐个
看看它是如何实现客户端负载均衡的。