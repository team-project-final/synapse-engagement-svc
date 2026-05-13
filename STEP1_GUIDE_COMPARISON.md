# Step 1 Guide Comparison

> 비교 기준: `C:\Users\G\Downloads\engagement-owner__engagement-svc-spring-modulith-scaffold-workflow-guide (한승완).html`  
> 비교 대상: `C:\workspace\team2_lab\synapse-engagement-svc`  
> 작성일: 2026-05-13

## 결론

현재 프로젝트는 Step 1의 핵심 골격인 Spring Boot 4, Java 21, Spring Modulith 의존성, community/gamification 모듈, Controller/Service, Modulith 구조 테스트, Dockerfile까지는 들어와 있다.

다만 가이드 기준으로 Step 1 Done 처리하려면 아래 항목은 아직 정리해야 한다.

- 애플리케이션 포트가 가이드의 `8082`로 설정되어 있지 않다.
- actuator health/info/probes 설정이 빠져 있다.
- Dockerfile은 multi-stage이지만 `EXPOSE 8080`으로 되어 있어 가이드의 `8082`와 다르다.
- Modulith 테스트 파일명이 가이드의 `ApplicationModulesTest`가 아니라 `ModuleStructureTest`다.
- `docker build`와 docker compose 실행 확인은 아직 증거가 없다.
- 가이드의 목표 구조에는 `community/internal`, `gamification/internal` 패키지가 있으나 현재는 없다.
- 가이드의 "빈 Controller/Service" 기준과 달리 현재는 `/api/v1/*/ping` 임시 API가 있다.
- 가이드 예시는 `application.yml`을 쓰지만 현재 프로젝트는 `application.properties`만 있고 값도 `spring.application.name` 하나뿐이다.

## Step 1 Done When 대조

| 기준 | 가이드 요구사항 | 현재 프로젝트 | 판정 |
| --- | --- | --- | --- |
| Spring Boot 4 프로젝트 | Spring Boot 4 + Java 21 + Gradle | `build.gradle.kts`에 Spring Boot `4.0.0`, Java toolchain `21` | 충족 |
| Modulith 의존성 | `spring-modulith-starter-core`, `spring-modulith-starter-test` | `build.gradle.kts`에 둘 다 있음. BOM은 `org.springframework.modulith:spring-modulith-bom:1.3.0` | 충족 |
| 빌드 성공 | `./gradlew build` 성공 | 2026-05-13 실행 결과 `BUILD SUCCESSFUL` | 충족 |
| community 모듈 | package + `package-info.java` + Controller/Service | `community` 패키지, `package-info.java`, `CommunityController`, `CommunityService` 있음 | 대부분 충족 |
| gamification 모듈 | package + `package-info.java` + Controller/Service | `gamification` 패키지, `package-info.java`, `GamificationController`, `GamificationService` 있음 | 대부분 충족 |
| internal 패키지 | `community/internal`, `gamification/internal` | 현재 없음 | 미충족 또는 보류 |
| Modulith 구조 테스트 | `ApplicationModulesTest` 작성 및 통과 | 동일 기능의 `ModuleStructureTest` 있음. `ApplicationModules.of(...).verify()` 사용 | 기능은 충족, 이름 불일치 |
| Dockerfile | multi-stage build, Java 21, 8082 expose | multi-stage Dockerfile 있음. 단 `EXPOSE 8080` | 부분 충족 |
| Docker image build | `docker build -t synapse-engagement-svc:local .` 성공 | 이번 비교에서 실행하지 않음 | 미확인 |
| docker compose 실행 | engagement-svc compose 실행 확인 | repo 안에서 compose 파일 검색 결과 없음 | 미충족 또는 외부 infra 의존 |

## 가이드와 현재 코드 차이

### 1. 애플리케이션 설정

가이드 기준:

```yaml
server:
  port: 8082

spring:
  application:
    name: synapse-engagement-svc

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      probes:
        enabled: true
```

현재:

```properties
spring.application.name=engagement-svc
```

해야 할 일:

- `server.port=8082` 추가
- actuator health/info 노출 설정 추가
- health probe 설정 추가
- 서비스명을 문서 기준 `synapse-engagement-svc`로 맞출지, 현재 `engagement-svc`를 유지할지 결정

권장:

- 다른 서비스와 compose/service name이 `engagement-svc`를 기대한다면 `spring.application.name=engagement-svc`는 유지해도 된다.
- 단 Step 1 가이드와 정확히 맞추려면 `synapse-engagement-svc`로 변경한다.
- 파일 형식은 `application.properties`를 유지해도 Spring Boot 동작에는 문제 없다. 가이드 스크린샷/산출물 기준까지 맞추려면 `application.yml`로 바꾼다.

### 2. Dockerfile

가이드 기준:

- multi-stage build
- Java 21
- `./gradlew clean bootJar --no-daemon`
- runtime image에서 jar 실행
- `EXPOSE 8082`

현재:

```dockerfile
FROM gradle:8.11.1-jdk21 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

해야 할 일:

- `EXPOSE 8080`을 `EXPOSE 8082`로 변경
- 가이드 기준을 엄격히 따르면 `RUN ./gradlew clean bootJar --no-daemon`로 변경
- Docker 이미지 빌드 검증 필요

### 3. Modulith 테스트

가이드 기준:

- 파일명: `ApplicationModulesTest.java`
- 테스트명 예시: `verifiesModularStructure`
- 내용: `ApplicationModules.of(EngagementSvcApplication.class).verify()`

현재:

```java
class ModuleStructureTest {

    @Test
    void verifyModuleStructure() {
        ApplicationModules.of(EngagementSvcApplication.class).verify();
    }
}
```

해야 할 일:

- 기능은 이미 맞다.
- 문서/가이드 산출물 기준까지 맞추려면 파일명과 클래스명을 `ApplicationModulesTest`로 변경한다.
- Cursor/IDE 테스트 Run/Debug CodeLens 문제는 이 파일명 문제와 별개로 보인다. Gradle test/build는 정상이다.

### 4. Modulith 모듈 의존성 선언

가이드 예시:

```java
@ApplicationModule(displayName = "Community", allowedDependencies = {})
package com.synapse.engagement.community;
```

```java
@ApplicationModule(displayName = "Gamification", allowedDependencies = {"community"})
package com.synapse.engagement.gamification;
```

가이드 주의사항:

- 실제 직접 호출이 없으면 `allowedDependencies = {}`가 더 엄격하다.
- 모듈 간 직접 의존은 나중에 필요할 때만 열어야 한다.

현재:

```java
@ApplicationModule(displayName = "Community", allowedDependencies = { "shared" })
package com.synapse.engagement.community;
```

```java
@ApplicationModule(displayName = "Gamification", allowedDependencies = { "shared" })
package com.synapse.engagement.gamification;
```

현재 프로젝트에는 가이드에 없는 `shared` 모듈이 추가되어 있다.

해야 할 일:

- `shared`를 공통 타입/이벤트/응답 규격용 모듈로 계속 둘지 결정
- `shared`를 유지한다면 현재 선언은 합리적이다.
- Step 1 가이드와 최대한 동일하게 맞출 목적이면 `shared` 모듈을 제거하거나, community/gamification의 `allowedDependencies`를 비워야 한다.

권장:

- 지금 단계에서는 `shared`를 꼭 써야 하는 코드가 없다면 제거 또는 보류가 더 단순하다.
- 다만 앞으로 공통 이벤트 타입을 둘 계획이면 `shared` 유지도 가능하다. 이 경우 문서 기준과 다르다는 점만 PR에 명시한다.

### 5. Controller/Service 골격

가이드 기준:

- 빈 Controller/Service 클래스
- 비즈니스 API 구현 없음
- 공개 API는 health endpoint만

현재:

- `GET /api/v1/community/ping`
- `GET /api/v1/gamification/ping`
- 각 Service에 `ping()` 메서드 있음

해야 할 일:

- 가이드 기준을 엄격히 따르면 ping API를 제거하고 빈 Controller/Service로 둔다.
- smoke 확인용 임시 API로 유지하려면, Step 1 범위에서 추가된 임시 endpoint라고 PR 설명에 남긴다.

권장:

- Step 1 산출물 검수만 통과시키는 목적이면 제거하는 쪽이 문서와 더 잘 맞는다.
- Docker/bootRun 확인을 쉽게 하려면 actuator health만으로도 충분하다.

### 6. 패키지 구조

가이드 목표:

```text
src/main/java/com/synapse/engagement
  EngagementSvcApplication.java
  community/
    package-info.java
    CommunityController.java
    CommunityService.java
    internal/
  gamification/
    package-info.java
    GamificationController.java
    GamificationService.java
    internal/
```

현재:

```text
src/main/java/com/synapse/engagement
  EngagementSvcApplication.java
  community/
  gamification/
  shared/
```

해야 할 일:

- `internal` 패키지는 빈 디렉터리만 만들면 Git에 보통 잡히지 않는다.
- 실제 내부 구현 클래스가 생길 때 만들거나, Step 1 산출물로 꼭 필요하면 `.gitkeep` 같은 placeholder가 필요하다.
- 현재 있는 `shared`는 가이드 기준에는 없으므로 유지 사유를 정해야 한다.

## Step 1에서 지금 해야 할 작업 목록

### 필수

- `application.properties` 또는 `application.yml`에 `server.port=8082` 추가
- actuator health/info/probes 설정 추가
- Dockerfile의 `EXPOSE 8080`을 `EXPOSE 8082`로 변경
- `ModuleStructureTest`를 `ApplicationModulesTest`로 이름 정리
- `./gradlew clean build` 재확인
- `docker build -t synapse-engagement-svc:local .` 실행 확인

### 결정 필요

- `spring.application.name`을 `engagement-svc`로 유지할지, 가이드의 `synapse-engagement-svc`로 맞출지 결정
- `shared` 모듈을 유지할지 제거할지 결정
- ping API를 임시 smoke endpoint로 유지할지 제거할지 결정
- docker compose 파일을 이 repo에 둘지, 상위 infra/docker repo에서 관리할지 결정

### 선택

- `application.properties`를 `application.yml`로 바꿔 가이드와 형식을 맞추기
- `community/internal`, `gamification/internal` 패키지 placeholder 추가
- Dockerfile build stage에 `clean` 추가

## 권장 작업 순서

1. 설정 정리
   - `server.port=8082`
   - actuator health/info/probes
   - application name 결정

2. 테스트 이름 정리
   - `ModuleStructureTest` -> `ApplicationModulesTest`
   - Gradle 테스트 기준으로 확인

3. Dockerfile 정리
   - `EXPOSE 8082`
   - 필요하면 `clean bootJar`

4. Step 1 검증
   - `./gradlew clean build`
   - `./gradlew test --tests "*ApplicationModulesTest"`
   - `./gradlew bootRun`
   - `curl http://localhost:8082/actuator/health`
   - `docker build -t synapse-engagement-svc:local .`

5. compose 확인
   - 이 repo 안에 compose 파일이 없으므로, 팀 compose/infra 위치를 확인해야 한다.
   - compose가 외부 repo에 있다면 Step 1 PR에는 "external compose에서 engagement-svc 연결 확인" 증거를 남긴다.

## 현재 Step 1 판정

현재 상태는 "골격 구현은 대부분 완료, Step 1 Done은 아직 보류"가 맞다.

보류 사유:

- build는 성공했지만 Docker image build 검증이 없다.
- runtime 포트가 8082로 정리되지 않았다.
- actuator 설정이 부족하다.
- Dockerfile expose 포트가 8080이다.
- 가이드 산출물명인 `ApplicationModulesTest`와 현재 테스트명이 다르다.
- docker compose 실행 확인 자료가 없다.

위 필수 항목만 정리하면 Step 1은 Done으로 올릴 수 있다.
