# 릴리스/Kafka/Flyway 배포 선결조건 보강

## 작업일

- 2026-06-05

## 작업 내용

- GitHub issue #25 기준으로 `Deploy` workflow를 semver 이미지 릴리스 경로로 보정했다.
  - 기존 shared reusable workflow는 `github.sha` 태그로 ECR push 및 gitops 직접 bump를 수행했다.
  - gitops image-updater가 semver-only tag를 감지하는 구조와 맞추기 위해 `1.1.0` 같은 semver tag push 또는 workflow dispatch 입력값으로만 ECR `synapse/engagement-svc:<semver>` 이미지를 push하도록 변경했다.
- GitHub issue #26 기준으로 Kafka Producer/Consumer 커스텀 설정에 `spring.kafka.security.protocol`을 배선했다.
  - 기본값은 `PLAINTEXT`라 로컬 docker-compose 영향은 없다.
  - EKS/MSK TLS-only 환경에서는 `KAFKA_SECURITY_PROTOCOL=SSL`이 Kafka client props까지 전달된다.
  - Producer/Consumer 양쪽에 같은 설정이 들어가는지 `KafkaSecurityProtocolConfigTests`를 추가했다.
- GitHub issue #28 기준으로 Flyway 표준을 반영했다.
  - `.github/workflows/flyway-guard.yml` caller를 추가해 PR/push에서 shared Flyway guard를 실행한다.
  - `application.yml`의 `spring.flyway`에 `out-of-order: true`, `baseline-on-migrate: false`를 명시했다.
- W5 workflow 문서에 #27 라이브 E2E 항목과 #25/#26/#28 배포 선결조건을 연결했다.

## 검증

- `KafkaSecurityProtocolConfigTests`로 Kafka client config에 `security.protocol=SSL`이 들어가는지 검증한다.
- Flyway guard는 PR/push에서 shared reusable workflow로 검증된다.
- semver deploy workflow는 trigger filter를 넓게 받고 내부 shell step에서 `^[0-9]+\.[0-9]+\.[0-9]+$` 형식만 허용한다.

## 남은 이슈

- ECR `synapse/engagement-svc:<신규 semver>` 도착 확인은 실제 tag push 또는 workflow dispatch 실행 후 확인해야 한다.
- image-updater의 gitops dev `newTag` 자동 bump는 ECR 이미지 push 이후 gitops 쪽에서 확인해야 한다.
- #27 notification 연동 + 게이미피케이션 + 모더레이션 라이브 E2E는 W5 통합 환경에서 실행해야 한다.
