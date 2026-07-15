# 배포 (CI/CD) 가이드

`main` 에 push 하면 GitHub Actions 가 자동으로 빌드 → EC2 전송 → `systemd` 재시작 → 헬스체크까지 수행한다.
실패하면 이전 jar 로 자동 롤백한다.

파이프라인 정의: [`.github/workflows/deploy.yml`](../.github/workflows/deploy.yml)

- 앱 포트: `8080` (앞단 nginx 가 80/443 리버스 프록시)
- API: `https://api.wonnature.co.kr` / 클라이언트: `https://www.wonnature.co.kr`

---

## 1. GitHub Secrets 등록 (최초 1회 · repo 관리자가 직접)

repo(`github.com/wonnature/back`) → **Settings → Secrets and variables → Actions → New repository secret** 에서 아래 3개를 등록한다.

| 이름 | 값 |
|---|---|
| `EC2_HOST` | `ec2-3-34-189-196.ap-northeast-2.compute.amazonaws.com` |
| `EC2_SSH_KEY` | `wonnature.pem` **파일 내용 전체** (`-----BEGIN ... KEY-----` 부터 끝까지) |
| `APPLICATION_YML` | `src/main/resources/application.yml` **파일 내용 전체** |

> ⚠️ `application.yml` 에는 RDS 비밀번호·AWS 키·Discord 웹훅이 들어있다. Secret 은 안전하지만,
> 이 값들이 외부로 노출된 적이 있다면 이 기회에 **AWS 키/RDS 비번 로테이션**을 권장한다.

---

## 2. EC2 서버 최초 설정 (systemd · 최초 1회)

`sudo nohup` 대신 `systemd` 로 앱을 관리한다 → 크래시/재부팅 시 자동 재시작, `systemctl restart` 로 배포.

```bash
# (로컬) 유닛 파일 업로드
scp -i wonnature.pem deploy/wonnature.service \
  ubuntu@ec2-3-34-189-196.ap-northeast-2.compute.amazonaws.com:/tmp/wonnature.service

# (서버) 아래를 순서대로 실행
sudo mv /tmp/wonnature.service /etc/systemd/system/wonnature.service
sudo systemctl daemon-reload
sudo systemctl enable wonnature

# 기존 nohup 프로세스 종료 후 systemd 로 기동
sudo pkill -f 'java -jar wonnature-' || true
cp wonnature-1.0.6-SNAPSHOT.jar wonnature.jar   # 현재 운영 중인 jar 를 안정 경로로
sudo systemctl start wonnature
sudo systemctl status wonnature --no-pager
```

> 서비스는 현재 운영 방식(root 구동)에 맞춰 `User=root` 로 설정돼 있다. 추후 `ubuntu` 로 낮추려면
> `deploy/wonnature.service` 의 `User=` 를 바꾸고 재적용하면 된다 (앱이 8080 비특권 포트라 가능).

---

## 3. 운영 명령어

```bash
sudo systemctl status wonnature          # 상태
sudo systemctl restart wonnature         # 수동 재시작
sudo journalctl -u wonnature -f          # 실시간 로그 (기존 nohup.out 대체)
sudo journalctl -u wonnature -n 200      # 최근 200줄
```

## 4. 롤백

- CI 는 배포 실패(앱이 8080 응답 없음) 시 `wonnature.jar.bak` 으로 **자동 롤백**한다.
- 수동 롤백: `cp wonnature.jar.bak wonnature.jar && sudo systemctl restart wonnature`

## 5. 동작 흐름

```
main push
  └─ GitHub Actions (ubuntu-latest)
       ├─ JDK 17 셋업
       ├─ Secret 에서 application.yml 복원
       ├─ ./gradlew clean bootJar -x test
       └─ EC2 전송 후 deploy/remote-deploy.sh 실행
            ├─ wonnature.jar 백업 → 새 jar 교체
            ├─ sudo systemctl restart wonnature
            ├─ :8080 응답 대기 (최대 60초)
            └─ 실패 시 이전 jar 로 롤백
```
