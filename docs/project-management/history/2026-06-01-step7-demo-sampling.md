# Step 7 Demo 샘플링

## 작업일

- 2026-06-01

## 작업 내용

- `C:\workspace\team2_\demo\step7`에 Step 7 Avro Kafka 리팩토링 내용을 설명용 샘플 프로젝트로 분리했다.
- Avro 스키마(`LevelUp.avsc`, `BadgeEarned.avsc`), Kafka Producer, Kafka 설정, Noop Publisher, mock 기반 테스트를 포함했다.
- Step 7 샘플 코드에 Kafka key, Avro generated class, Schema Registry, Noop Publisher 역할을 설명하는 주석을 추가했다.
- `C:\workspace\team2_\demo\step3`에서 Flyway SQL 문법 오류와 Entity 컬럼명 오타를 수정했다.

## 검증

- `C:\workspace\team2_\demo\step7`에서 `.\gradlew.bat test` 통과.
- `C:\workspace\team2_\demo\step3`에서 `.\gradlew.bat test` 통과.

## 남은 이슈

- Step 7 demo는 실제 Kafka broker 없이 mock 기반으로 동작하는 샘플이다.
