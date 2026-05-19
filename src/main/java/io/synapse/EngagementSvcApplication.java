package io.synapse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

@Modulithic
@SpringBootApplication
// engagement-svc의 시작점입니다. community, gamification 모듈을 Spring Modulith로 묶어 실행합니다.
public class EngagementSvcApplication {

	public static void main(String[] args) {
		SpringApplication.run(EngagementSvcApplication.class, args);
	}

}
