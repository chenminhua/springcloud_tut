package com.chenminhua.reservation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Collection;
import java.util.stream.Stream;

@SpringBootApplication
@EnableDiscoveryClient
public class ReservationApp {

    @Bean
    CommandLineRunner commandLineRunner(ReservationRepository reservationRepository) {
        return strings -> {
            Stream.of("Josh", "peter", "tasha", "eric", "susia", "max").forEach(
                    n -> reservationRepository.save(new Reservation(n)));
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(ReservationApp.class, args);
    }

}


/**
 *  curl -d{} http://localhost:8000/actuator/refresh
 */

@RefreshScope
@RestController
class MessageRestController {
    @Value("${messages}")
    private String msg;

    @RequestMapping("/message")
    String message() {
        return this.msg;
    }
}

@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @RestResource(path="by-name")
    Collection<Reservation> findByReservationName(String rn);
}

@Entity
class Reservation {

    @Id
    @GeneratedValue
    private Long id;
    private String reservationName;

    public Reservation(String n) {
        this.reservationName = n;
    }

    public Reservation() { }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", reservationName=" + reservationName + "}";
    }

    public Long getId() {
        return id;
    }

    public String getReservationName() {
        return reservationName;
    }

}

