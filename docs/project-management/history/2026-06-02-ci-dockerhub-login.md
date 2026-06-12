# CI Docker Hub 로그인 보완

## 작업일

- 2026-06-02

## 작업 내용

- GitHub issue #18에서 보고된 `dev-smoke` 잡의 Docker Hub pull rate-limit/타임아웃 문제를 확인했다.
- `.github/workflows/ci-java.yml`의 `dev-smoke` 잡에서 `docker compose ... up -d --wait` 실행 직전에 Docker Hub 로그인 스텝을 추가했다.
- 로그인에는 `docker/login-action@v3`를 사용하고, GitHub Secrets의 `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`을 참조하도록 구성했다.

## 검증

- workflow 파일에서 `Log in to Docker Hub` 스텝이 `Start dev services` 바로 앞에 위치하는지 확인했다.
- 변경 범위는 CI workflow에 한정된다.

## 남은 이슈

- GitHub org 또는 repo secrets에 `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`이 등록되어 있어야 실제 CI에서 로그인된다.
- 로컬에서는 GitHub Actions runner secret을 검증할 수 없으므로, 최종 확인은 PR/CI 재실행으로 해야 한다.
