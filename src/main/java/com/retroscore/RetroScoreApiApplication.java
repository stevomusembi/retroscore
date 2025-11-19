package com.retroscore;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RetroScoreApiApplication {


    @PostConstruct
    public void logDatabaseConfig() {
        System.out.println("Connecting to DB:");
        System.out.println("  URL: " + System.getenv("DATABASE_URL"));
        System.out.println("  Username: " + System.getenv("DATABASE_USERNAME"));
    }
	public static void main(String[] args) {

		SpringApplication.run(RetroScoreApiApplication.class, args);

		System.out.println();
		System.out.println("  ╔═══════════════════════╗");
		System.out.println("  ║ ⚽ RETROSCORE API ⚽  ║");
		System.out.println("  ╚═══════════════════════╝");

        System.out.println();
        System.out.println("     ██████████████");
        System.out.println("   ██▓▓▓▓▓▓▓▓▓▓▓▓▓▓██");
        System.out.println("  ██▓▓▓▓██▓▓▓▓██▓▓▓▓██");
        System.out.println(" ██▓▓▓▓▓▓██▓▓██▓▓▓▓▓▓██");
        System.out.println("██▓▓▓▓▓▓▓▓████▓▓▓▓▓▓▓▓██");
        System.out.println("██▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓██");
        System.out.println("██▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓██");
        System.out.println(" ██▓▓▓▓▓▓██▓▓██▓▓▓▓▓▓██");
        System.out.println("  ██▓▓▓▓██▓▓▓▓██▓▓▓▓██");
        System.out.println("   ██▓▓▓▓▓▓▓▓▓▓▓▓▓▓██");
        System.out.println("     ██████████████");
        System.out.println();

		System.out.println("  ╔══════════════════╗");
		System.out.println("  ║   RetroScore     ║");
		System.out.println("  ║   API Server     ║");
		System.out.println("  ║   is Running!    ║");
		System.out.println("  ╚══════════════════╝");
		System.out.println();
	}
	}
