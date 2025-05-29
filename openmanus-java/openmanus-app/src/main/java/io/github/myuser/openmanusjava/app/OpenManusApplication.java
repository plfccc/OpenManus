package io.github.myuser.openmanusjava.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan; // Import
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories; // Import
import org.springframework.scheduling.annotation.EnableAsync; // Good to add if using @Async

@SpringBootApplication(scanBasePackages = "io.github.myuser.openmanusjava")
@EnableJpaRepositories(basePackages = "io.github.myuser.openmanusjava.core.repository") // Specify repo package
@EntityScan(basePackages = "io.github.myuser.openmanusjava.core.model")           // Specify entity package
@ConfigurationPropertiesScan(basePackages = "io.github.myuser.openmanusjava")
@EnableAsync // If @Async methods are planned in services (like TaskOrchestrationService)
public class OpenManusApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpenManusApplication.class, args);
    }
}
