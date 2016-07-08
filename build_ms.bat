@echo OFF
set MSBT_ALLOW_HTTP_PROXY=1
set https_proxy=http://wwwproxy.ms.com:8080/
set http_proxy=http://wwwproxy.ms.com:8080/

call module load msjava/oraclejdk/1.8.0_74
call module load yam/sbt


call sbt