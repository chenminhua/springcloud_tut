package com.chenminhua.reservationclient;


import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

//@EnableCircuitBreaker
@EnableZuulProxy
@EnableDiscoveryClient
@SpringBootApplication
public class ReservationClientApp {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(ReservationClientApp.class, args);
    }

}

@RestController
@RequestMapping("/reservations")
class ReservationApiGatewayRestController {

    @Autowired
    private RestTemplate restTemplate;

    public Collection<String> getReservationNamesFallback() {
        return new ArrayList<>();
    }

    // spring cloud stream
//    @Autowired private Source source;
//    @PostMapping()
//    public void writeReservation(@RequestBody Reservation r) {
//        Message<String> msg = MessageBuilder.withPayload(r.getReservationName()).build();
//        this.source.output().send(msg);
//    }


    // fallback
    @HystrixCommand(fallbackMethod = "getReservationNamesFallback")
    @GetMapping("/names")
    public Collection<String> getReservationNames() {
        ParameterizedTypeReference<Resources<Reservation>> ptr =
                new ParameterizedTypeReference<Resources<Reservation>>() {};
        ResponseEntity<Resources<Reservation>> entity = this.restTemplate
                .exchange("http://reservation/reservations", HttpMethod.GET, null, ptr);

        return entity.getBody()
                .getContent()
                .stream()
                .map(Reservation::getReservationName)
                .collect(Collectors.toList());
    }
}

class Reservation {

    private String reservationName;

    public String getReservationName() {
        return reservationName;
    }
}