#!/bin/bash

build_image() {
  set -e

  rm -r build/libs || true

  ## ソースコードからAnnihilationをビルド
  gradle jar

  ## dockerイメージのビルド（初回は数十分かかります）
  docker-compose build -m 2g
}

stop_docker_service() {
  set -e
  docker-compose down
}

set -e
gradle :jar

# 子プロセス側で関数をコマンドとして参照したいためexportする
export -f build_image
export -f stop_docker_service

# 既存のサービスを落とし、ビルド完了を待つ処理を並列実行する
echo "stop_docker_service build_image" | xargs -P 0 -n 1 bash -c

## デバッグに必要なdockerコンテナを起動
## (起動後はCtrl+Cで停止できます)
docker-compose up --abort-on-container-exit