client-side service discovery
only "fixed point": 服务注册
drawback: all clients must implement a certain logic to interact with the fixed point

注意spring和spring-cloud的版本兼容问题


components:
    a service registry  (Eureka server)
    a rest service which registers itself at the registry
    a web-application consuming the rest service
    
    
课程:
    josh long 《building microservice with spring cloud》  "youtube"
    
## 
[入门视频](https://www.youtube.com/watch?v=aO3W-lYnw-o)

http://cloudnativejava.io/
    
## why spring cloud


## component
#### configuration
用来将config从application中移出来，放到中心化的存储中
config server可以使用git, svn, filesystem, vault来存储config
config client可以在启动的时候取到配置，并在配置发生变化的时候被通知到

#### service discovery

netfilx eureka
zookeeper
consul

#### circuit breakers
netflix hystrix

#### routing and messaging
routing and load balancing
    netflix ribbon and open feign
messaging:
    rabbitmq or kafka

#### api gateway
netflix zuul
    利用服务发现和负载均衡

spring cloud gateway

#### tracing
spring cloud sleuth and zipkin

#### CI pipelines and testing
Spring cloud contact

#### other
bus, stream, data and task, aws...


## spring cloud
spring cloud eureka, configserver, zipkin



    