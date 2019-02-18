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

### 服务续约
在注册完服务之后，服务提供者会维护一个心跳到EurekaServer，以防止被从服务列表中排除出去。
eureka.instance.lease-renewal-interval-in-seconds=30    服务续约任务的间隔时间
eureka.ins七ance.lease-expiration-duration-in-seconds=90   定义服务失效的时间


### eureka的基础架构

### eureka的服务治理机制

### eureka的配置