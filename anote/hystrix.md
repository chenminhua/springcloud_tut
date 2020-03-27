举个例子，在一个电商网站中，我们可能会将系统拆分成用户、订单、库存、积分、评论等一系列服务单元。
用户创建一个订单的时候，客户端将调用订单服务的创建订单接口，此时创建订单接口又会向库存服务来请求出货(判断是否有足够库存来出货)。 
此时若库存服务因自身处理逻辑等原因造成响应缓慢，会直接导致创建订单服务的线程被挂起，以等待库存申请服务的响应。
在漫长的等待之后用户会因为请求库存失败而得到创建订单失败的结果。
如果在高并发情况之下，因这些挂起的线程在等待库存服务的响应而未能释放，使得后续到来的创建订单请求被阻塞，最终导致订单服务也不可用。

## 使用方法
1. 在客户端启用  @EnableCircuitBreaker 注解开启断路器。

注意:这里还可以使用 Spring Cloud 应用中的 @SpringCloudApplication 注解来修饰应用主类，该注解的具体定义如下所示。 

```$xslt
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
public @interface SpringCloudApplication {
}

```

2. 在客户端调用远端服务的service的方法上增加 @HystrixCommand(fallbackMethod = "getReservationNamesFallback") 注解

## 原理分析

1. 创建HystrixCommand或HystrixObservableCommand对象，用来表示对依赖服务的操作请求，同时传递所有需要的参数。 
从其命名中我们就能知道它采用了 “命令模式” 来实现对服务调用操作的封装。 而这两个 Command 对象分别针对不同的应用场景。

• HystrixCommand: 用在依赖的服务返回单个操作结果的时候。
• HystrixObservableCommand: 用在依赖的服务返回多个操作结果的时候。

命令模式，将来自客户端的请求封装成一个对象，从而让你可以使用不同的请求对客户端进行参数化。 
它可以被用于实现 “行为请求者” 与 “行为实现者” 的解耦，以便使两者可以适应变化。

HystrixCommand实现了下面两个执行方式。
• execute(): 同步执行，从依赖的服务返回一个单一的结果对象，或是在发生错误的时候抛出异常。
• queue(): 异步执行，直接返回一个Future对象，其中包含了服务执行结束时要返回的单一结果。

而HystrixObservableCommand实现了另外两种执行方式。
• observe(): 返回Observable对象，它代表了操作的多个结果，它是一个Hot Observable。
• toObservable(): 同样会返回Observable对象，也代表了操作的多个结果，但它返回的是一个Cold Observable。


Observable向Subscriber发布事件，Subscriber在接收到事件后对其进行处理，而在这里所指的事件通常就是对依赖服务的调用。
一个Observable可以发出多个事件， 直到结束或是发生异常。
Observable对象每发出一个事件，就会调用对应观察者Subscriber对象的onNext()方法。
每一个Observable的执行，最后一定会通过调用 Subscriber.onCompleted() 或者Subscriber.onError()来结束该事件的操作流。

## hystrix 限流

1:Hystrix使用命令模式HystrixCommand(Command)包装依赖调用逻辑，每个命令在单独线程中/信号授权下执行。
2:可配置依赖调用超时时间,超时时间一般设为比99.5%平均时间略高即可.当调用超时时，直接返回或执行fallback逻辑。
3:为每个依赖提供一个小的线程池（或信号），如果线程池已满调用将被立即拒绝，默认不采用排队.加速失败判定时间。
4:依赖调用结果分:成功，失败（抛出异常），超时，线程拒绝，短路。 请求失败(异常，拒绝，超时，短路)时执行fallback(降级)逻辑。
5:提供熔断器组件,可以自动运行或手动调用,停止当前依赖一段时间(10秒)，熔断器默认错误率阈值为50%,超过将自动运行。
6:提供近实时依赖的统计和监控