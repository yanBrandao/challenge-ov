package com.ovinto.member_calculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MemberDistanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MemberDistanceApplication.class, args);
	}

}
