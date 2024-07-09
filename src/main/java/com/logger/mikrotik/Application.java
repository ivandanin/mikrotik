package com.logger.mikrotik;

import io.prometheus.client.exporter.HTTPServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Scanner;

@RestController
@SpringBootApplication
public class Application extends Connector {

    static Application app = new Application();

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);

        // starting the Prometheus server on port 9091
        HTTPServer httpServer = new HTTPServer(9091);

        Scanner scanner = new Scanner(System.in);
        app.connect();
        app.read(scanner);
        // make a call to print all the logs and push it into afrr file, that will be used as a machine learning output
        app.disconnect();
    }

    @GetMapping("/something")
    public ResponseEntity<String> createLogs() {
        return ResponseEntity.ok().body("All ok");
    }

}
