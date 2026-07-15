#!/usr/bin/env bash
# EC2 서버에서 실행되는 배포 스크립트. GitHub Actions 가 jar 를 올린 뒤 호출한다.
set -e
cd /home/ubuntu

# 현재 jar 백업 후, 새로 올라온 jar 를 교체
[ -f wonnature.jar ] && cp wonnature.jar wonnature.jar.bak
mv deploy/wonnature.jar.new wonnature.jar

sudo systemctl restart wonnature

# 앱이 8080 에서 응답할 때까지 대기 (최대 60초). 인증 endpoint 라도 HTTP 응답이 오면 기동 성공.
OK=0
for i in $(seq 1 30); do
  code=$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/ || true)
  if [ -n "$code" ] && [ "$code" != "000" ]; then OK=1; break; fi
  sleep 2
done

if [ "$OK" = "1" ]; then
  echo "✅ DEPLOY OK (app responding on :8080, http=$code)"
  exit 0
fi

# 기동 실패 시 이전 jar 로 자동 롤백
echo "❌ HEALTHCHECK FAILED - rolling back to previous jar"
if [ -f wonnature.jar.bak ]; then
  cp wonnature.jar.bak wonnature.jar
  sudo systemctl restart wonnature
fi
exit 1
