# Step 1 실행 계획: engagement-svc 골격 생성

## 1. Step 1에서 최종적으로 만들 것

Step 1의 목표는 `engagement-svc`를 앞으로 기능 개발이 가능한 Spring Boot + Spring Modulith 서비스 골격으로 만드는 것이다.

최종 결과물은 다음과 같아야 한다.

- Spring Boot 4 애플리케이션이 실행된다.
- Java 21 + Gradle 빌드가 정상 동작한다.
- `community`, `gamification`, `shared` 모듈 패키지가 존재한다.
- `community`와 `gamification`은 서로 직접 의존하지 않는다.
- 공통으로 필요한 코드는 `shared`에 둔다.
- Modulith 구조 검증 테스트가 통과한다.
- Dockerfile로 애플리케이션 이미지를 빌드할 수 있다.

Step 1에서는 비즈니스 로직을 구현하지 않는다. 그룹 CRUD, 멤버 관리, XP, 배지, Kafka, DB 마이그레이션은 Step 2 이후 작업이다.

## 2. 전체 작업 순서

Step 1은 아래 순서로 진행한다.

1. Gradle과 Spring Boot 기본 설정 확인
2. Modulith 의존성 추가
3. 메인 애플리케이션에 `@Modulithic` 적용
4. 모듈 패키지 생성
5. 각 모듈의 `package-info.java` 작성
6. 각 모듈에 최소 Controller / Service 생성
7. Modulith 구조 검증 테스트 작성
8. 테스트와 빌드 실행
9. Dockerfile 완성
10. Docker 이미지 빌드 확인
11. docs의 Step 1 체크 상태 갱신

## 3. Gradle 설정

먼저 `build.gradle.kts`에서 Java 21과 Spring Boot 4가 설정되어 있어야 한다.

확인할 항목:

```kotlin
plugins {
    java
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
```

그다음 Modulith 의존성을 추가한다.

```kotlin
dependencies {
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.modulith:spring-modulith-bom:1.3.0")
    }
}
```

의미:

- `spring-modulith-starter-core`: 애플리케이션 모듈 구조를 인식하고 관리하기 위한 핵심 의존성
- `spring-modulith-starter-test`: `ApplicationModules.verify()` 같은 모듈 검증 테스트에 필요
- BOM: Modulith 관련 의존성 버전을 한 번에 맞추기 위한 설정

주의할 점:

- Spring Boot 4와 Modulith 버전 호환성을 확인해야 한다.
- 의존성 추가 후 반드시 테스트를 실행해 실제로 resolve되는지 확인한다.

## 4. 메인 애플리케이션 설정

`EngagementSvcApplication.java`에 `@Modulithic`을 붙인다.

위치:

```text
src/main/java/com/synapse/engagement/EngagementSvcApplication.java
```

예상 형태:

```java
package com.synapse.engagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

@Modulithic
@SpringBootApplication
public class EngagementSvcApplication {

    public static void main(String[] args) {
        SpringApplication.run(EngagementSvcApplication.class, args);
    }
}
```

`@Modulithic`은 이 애플리케이션을 Modulith 기반 애플리케이션으로 다룬다는 선언이다. 이후 `ApplicationModules.of(EngagementSvcApplication.class)`가 이 클래스를 기준으로 하위 패키지들을 분석한다.

## 5. 모듈 패키지 만들기

Step 1에서는 최소 3개 패키지를 만든다.

```text
src/main/java/com/synapse/engagement/community
src/main/java/com/synapse/engagement/gamification
src/main/java/com/synapse/engagement/shared
```

각 모듈의 역할은 다음처럼 나눈다.

| 패키지 | 역할 |
|------|------|
| `community` | 그룹, 멤버, 공유, 신고 등 커뮤니티 기능이 들어갈 영역 |
| `gamification` | XP, 레벨, 배지, 스트릭, 리더보드 기능이 들어갈 영역 |
| `shared` | 여러 모듈이 공통으로 써야 하는 타입, 유틸, 예외, 공통 응답 등이 들어갈 영역 |

중요한 원칙:

- `community`에서 `gamification`을 직접 import하지 않는다.
- `gamification`에서 `community`를 직접 import하지 않는다.
- 공통으로 필요한 것은 `shared`로 올린다.
- 기능 간 연동은 나중에 이벤트 또는 명확한 공개 API 방식으로 처리한다.

## 6. package-info.java 작성

각 모듈 패키지에 `package-info.java`를 만든다.

`community/package-info.java`:

```java
@org.springframework.modulith.ApplicationModule(
        displayName = "Community",
        allowedDependencies = { "shared" })
package com.synapse.engagement.community;
```

`gamification/package-info.java`:

```java
@org.springframework.modulith.ApplicationModule(
        displayName = "Gamification",
        allowedDependencies = { "shared" })
package com.synapse.engagement.gamification;
```

`shared/package-info.java`:

```java
@org.springframework.modulith.ApplicationModule(displayName = "Shared")
package com.synapse.engagement.shared;
```

이렇게 작성하면 `community`와 `gamification`은 `shared`에만 의존할 수 있다.

예를 들어 아래 코드는 피해야 한다.

```java
// community 안에서 gamification 클래스를 직접 import
import com.synapse.engagement.gamification.GamificationService;
```

이런 직접 참조가 생기면 모듈 경계가 흐려지고, 나중에 기능이 커졌을 때 순환 의존이 생기기 쉽다.

## 7. 최소 Controller / Service 만들기

Step 1에서는 실제 기능 API를 만들지 않는다. 대신 모듈이 Spring Bean으로 정상 등록되는지 확인할 수 있는 최소 클래스만 만든다.

`CommunityService.java`:

```java
package com.synapse.engagement.community;

import org.springframework.stereotype.Service;

@Service
public class CommunityService {

    public String ping() {
        return "community";
    }
}
```

`CommunityController.java`:

```java
package com.synapse.engagement.community;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping("/api/v1/community/ping")
    public String ping() {
        return communityService.ping();
    }
}
```

`GamificationService.java`:

```java
package com.synapse.engagement.gamification;

import org.springframework.stereotype.Service;

@Service
public class GamificationService {

    public String ping() {
        return "gamification";
    }
}
```

`GamificationController.java`:

```java
package com.synapse.engagement.gamification;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GamificationController {

    private final GamificationService gamificationService;

    public GamificationController(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    @GetMapping("/api/v1/gamification/ping")
    public String ping() {
        return gamificationService.ping();
    }
}
```

이 ping API는 실제 기능이 아니라 모듈별 Bean 등록과 라우팅 확인용이다. Step 2 이후 실제 API가 생기면 필요에 따라 유지하거나 제거한다.

## 8. PlaceholderComponent를 둘지 결정

빈 패키지는 Git이나 빌드 과정에서 의미가 약해질 수 있으므로 `PlaceholderComponent`를 둘 수 있다.

예:

```java
package com.synapse.engagement.shared;

import org.springframework.stereotype.Component;

@Component
public class PlaceholderComponent {
}
```

단, Controller와 Service가 이미 존재하는 모듈에는 placeholder가 꼭 필요하지 않다. 남겨둘 경우 "이 모듈이 아직 골격 단계다"라는 표시 역할만 한다.

정리 기준:

- `shared`처럼 아직 실제 코드가 없는 패키지는 placeholder 유지 가능
- `community`, `gamification`처럼 Controller/Service가 있으면 placeholder 제거 가능
- 제거할 때는 Modulith 검증 테스트가 계속 통과하는지 확인한다.

## 9. Modulith 구조 검증 테스트 작성

테스트 파일 위치:

```text
src/test/java/com/synapse/engagement/ModuleStructureTest.java
```

작성 내용:

```java
package com.synapse.engagement;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModuleStructureTest {

    @Test
    void verifyModuleStructure() {
        ApplicationModules.of(EngagementSvcApplication.class).verify();
    }
}
```

이 테스트가 하는 일:

- 애플리케이션 하위 패키지를 Modulith 모듈로 분석한다.
- `package-info.java`의 `allowedDependencies` 규칙을 확인한다.
- 모듈 간 순환 의존이 있는지 확인한다.
- 잘못된 직접 참조가 있는지 확인한다.

Step 1에서 이 테스트가 중요한 이유:

- 지금 모듈 경계를 잡아두면 Step 2, Step 3에서 기능이 늘어나도 구조가 망가지는 것을 바로 잡을 수 있다.
- 사람이 코드 리뷰로 놓칠 수 있는 의존성 문제를 테스트가 자동으로 잡는다.

## 10. 기본 Spring context 테스트

기본 애플리케이션 로딩 테스트도 유지한다.

위치:

```text
src/test/java/com/synapse/engagement/EngagementSvcApplicationTests.java
```

예상 형태:

```java
package com.synapse.engagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EngagementSvcApplicationTests {

    @Test
    void contextLoads() {
    }
}
```

이 테스트는 Spring ApplicationContext가 정상적으로 뜨는지 확인한다.

역할 구분:

- `EngagementSvcApplicationTests`: Spring Boot 애플리케이션 로딩 확인
- `ModuleStructureTest`: Modulith 모듈 구조 확인

둘 다 통과해야 Step 1의 테스트 기준을 만족한다.

## 11. 테스트 실행

Bash 기준:

```bash
./gradlew test
```

성공 기준:

```text
BUILD SUCCESSFUL
```

테스트 실패 시 확인할 것:

- Modulith 의존성이 제대로 resolve되었는지
- `@Modulithic` import가 맞는지
- `package-info.java` 패키지명이 실제 경로와 일치하는지
- `allowedDependencies`에 없는 모듈을 import하지 않았는지
- Controller나 Service 생성자 주입에서 누락된 Bean이 없는지

Gradle wrapper 캐시 권한 문제로 실패할 수 있다.

예:

```text
gradle-9.4.1-bin.zip.lck
액세스가 거부되었습니다
```

이 경우 코드 문제가 아니라 로컬 Gradle 캐시 접근 권한 문제다. 권한 문제 해결 후 다시 실행한다.

## 12. 전체 빌드 실행

테스트만 통과한 뒤에는 전체 빌드도 확인한다.

```bash
./gradlew build
```

성공 기준:

```text
BUILD SUCCESSFUL
```

`build`는 컴파일, 테스트, jar 생성까지 포함하므로 Step 1 완료 판단에 더 적합하다.

## 13. Dockerfile 작성

현재 Step 1 완료 기준에는 Docker 이미지 빌드 성공이 포함되어 있다. 따라서 Dockerfile은 한 줄짜리 builder 선언으로 끝나면 안 되고, 실제 실행 가능한 이미지까지 만들어야 한다.

권장 형태:

```dockerfile
FROM gradle:8.11.1-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

설명:

- 첫 번째 stage는 Gradle로 jar를 빌드한다.
- 두 번째 stage는 JRE만 포함한 가벼운 런타임 이미지다.
- 최종 컨테이너는 `java -jar app.jar`로 실행된다.

주의할 점:

- `COPY . .`는 간단하지만 빌드 캐시 효율은 낮다.
- 나중에 최적화할 때는 `build.gradle.kts`, `settings.gradle.kts`, `gradle/`, `gradlew` 등을 먼저 복사하고 의존성 캐시를 분리할 수 있다.
- Step 1에서는 우선 이미지 빌드와 실행 가능성이 더 중요하다.

## 14. Docker 이미지 빌드

Dockerfile 작성 후 이미지 빌드를 실행한다.

```bash
docker build -t synapse-engagement-svc .
```

성공 기준:

```text
Successfully built ...
Successfully tagged synapse-engagement-svc:latest
```

실패 시 확인할 것:

- Dockerfile의 Gradle 버전과 프로젝트 wrapper 버전이 충돌하지 않는지
- jar 파일이 `build/libs`에 생성되는지
- 테스트 실패 때문에 `bootJar` 이전 단계에서 멈추는지
- Docker daemon이 실행 중인지

## 15. 컨테이너 실행 확인

이미지 빌드 후 단독 실행을 확인한다.

```bash
docker run --rm -p 8080:8080 synapse-engagement-svc
```

다른 터미널에서 ping API를 확인한다.

```bash
curl http://localhost:8080/api/v1/community/ping
curl http://localhost:8080/api/v1/gamification/ping
```

예상 응답:

```text
community
gamification
```

Actuator health를 열어두었다면 이것도 확인한다.

```bash
curl http://localhost:8080/actuator/health
```

예상 응답:

```json
{"status":"UP"}
```

## 16. 완료 전 점검 체크리스트

Step 1을 Done으로 바꾸기 전에 아래를 확인한다.

- [ ] `build.gradle.kts`에 Spring Boot 4 설정이 있다.
- [ ] Java 21 toolchain이 설정되어 있다.
- [ ] Modulith core/test 의존성이 있다.
- [ ] `EngagementSvcApplication`에 `@Modulithic`이 있다.
- [ ] `community` 패키지가 있다.
- [ ] `gamification` 패키지가 있다.
- [ ] `shared` 패키지가 있다.
- [ ] 각 모듈의 `package-info.java`가 있다.
- [ ] `community`와 `gamification`은 `shared`에만 의존한다.
- [ ] 최소 Controller/Service가 Bean으로 등록된다.
- [ ] `ModuleStructureTest`가 있다.
- [ ] `./gradlew test`가 성공한다.
- [ ] `./gradlew build`가 성공한다.
- [ ] Dockerfile이 multi-stage build 형태로 완성되어 있다.
- [ ] `docker build -t synapse-engagement-svc .`가 성공한다.
- [ ] 컨테이너 실행 후 ping API 또는 health endpoint가 응답한다.

## 17. docs 업데이트 기준

위 체크리스트를 모두 만족하면 docs를 업데이트한다.

수정할 문서:

```text
docs/project-management/task/TASK_engagement.md
docs/project-management/workflow/WORKFLOW_engagement_W1.md
docs/project-management/history/HISTORY_engagement.md
```

업데이트 방식:

- `TASK_engagement.md`의 Step 1 `Done When` 체크박스를 완료 상태로 바꾼다.
- `TASK_engagement.md`의 Step 1 Status를 `Done`으로 바꾼다.
- `WORKFLOW_engagement_W1.md`의 Step 1 하위 체크박스를 완료 상태로 바꾼다.
- `HISTORY_engagement.md`의 Step 1 상태, 시작일, 완료일을 기록한다.
- `HISTORY_engagement.md`의 해당 날짜 로그에 테스트/빌드/Docker 확인 결과를 적는다.

이번 문서는 실행 계획을 정리하는 파일이므로, 실제 docs 수정은 Step 1 작업이 끝난 뒤에 한다.

## 18. Step 1에서 하지 말아야 할 것

아래 작업은 Step 1 범위를 넘는다.

- Group Entity 생성
- groups 테이블 Flyway 마이그레이션 작성
- GroupRepository 작성
- GroupService CRUD 구현
- JWT 인증/인가 구현
- Kafka Consumer/Producer 작성
- XP 적립 로직 구현
- 배지/레벨/리더보드 구현
- 실제 공유/신고 API 구현

이것들을 Step 1에서 같이 넣으면 골격 작업과 기능 작업이 섞여서 테스트 실패 원인을 추적하기 어려워진다.

## 19. 추천 커밋 단위

Step 1은 다음 정도로 나눠 커밋하면 좋다.

1. `chore: configure spring boot modulith skeleton`
   - Gradle 설정
   - `@Modulithic`
   - 모듈 패키지 생성

2. `test: add modulith module structure verification`
   - `ModuleStructureTest`
   - context load 테스트 확인

3. `chore: add docker build for engagement service`
   - Dockerfile 완성
   - Docker 이미지 빌드 확인

하나의 커밋으로 묶어도 되지만, 테스트와 Docker는 실패 원인이 다르기 때문에 분리하는 편이 리뷰하기 쉽다.

## 20. Step 1 완료 정의

Step 1은 아래 명령이 모두 성공하면 완료로 본다.

```bash
./gradlew test
./gradlew build
docker build -t synapse-engagement-svc .
docker run --rm -p 8080:8080 synapse-engagement-svc
```

그리고 실행 중인 컨테이너에서 아래 요청이 응답해야 한다.

```bash
curl http://localhost:8080/api/v1/community/ping
curl http://localhost:8080/api/v1/gamification/ping
curl http://localhost:8080/actuator/health
```

이 상태가 되면 `engagement-svc`는 Step 2의 community 그룹 CRUD 구현을 시작할 수 있는 골격 상태가 된다.
