---
name: deploy
description: ModをビルドしてMinecraftのmodsフォルダにデプロイする。「デプロイして」「deploy」「ビルドしてデプロイ」などの場面で使用
---

# Mod Deploy

## 実行方法

`.claude/skills/deploy/scripts/deploy.bat` を実行する

```bash
cmd //c ".claude/skills/deploy/scripts/deploy.bat"
```

## スクリプトの動作

1. `gradlew.bat build --no-daemon` でビルド
2. `build/libs/` から JAR ファイルを mods フォルダにコピー

## エラー処理

- ビルドエラー: エラー内容を報告し、修正を提案
- コピー失敗: パスを確認して再試行

## バージョン更新時

`deploy.bat` 内の `JAR_NAME` と `DEPS_JAR_NAME` を更新
