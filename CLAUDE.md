# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## プロジェクト概要

NESQL Exporter は Minecraft 1.7.10 (GTNH) 向けの Forge Mod で、ゲーム内データ（アイテム、レシピ、クエストなど）を SQL データベース（HSQLDB または PostgreSQL）にエクスポートする。サーバーモジュール [nesql-server](https://github.com/D-Cysteine/nesql-server) と連携して動作する。

## ビルドコマンド

```bash
# ビルド（mod JAR + 依存関係 JAR を生成）
./gradlew build

# Minecraft 開発環境を起動（テスト用）
./gradlew runClient

# 依存関係 Shadow JAR のみビルド
./gradlew depsJar

# SQL スキーマ JAR のみビルド（nesql-server 用）
./gradlew sqlJar

# Protobuf コード生成
./gradlew generateProto
```

## アーキテクチャ

### プラグインシステム
エクスポート処理はプラグインベースで拡張可能：

1. **ライフサイクル**: `initialize()` → `process()` → `postProcess()`
2. **登録**: [PluginRegistry.java](src/main/java/com/github/dcysteine/nesql/exporter/registry/PluginRegistry.java) で新規プラグインを追加
3. **基底クラス**: [PluginExporter.java](src/main/java/com/github/dcysteine/nesql/exporter/plugin/PluginExporter.java) を継承

### プラグイン一覧
| プラグイン | 依存Mod | 説明 |
|-----------|---------|------|
| BASE | - | 基本テーブル定義、後処理 |
| MINECRAFT | - | バニラクラフト/かまどレシピ |
| NEI | NotEnoughItems | NEI アイテムリスト |
| FORGE | - | 鉱石辞書、液体コンテナ |
| GREGTECH | GT5-Unofficial | GT レシピマップ |
| THAUMCRAFT | Thaumcraft + NEI Plugin | アスペクトデータ |
| QUEST | BetterQuesting | クエストデータ |
| AVARITIA | Avaritia | エクストリームクラフトレシピ |
| MOBS_INFO | Mobs-Info | Mobドロップ情報 |

### パッケージ構成
```
src/main/java/com/github/dcysteine/nesql/
├── exporter/           # Mod 本体
│   ├── main/          # エントリーポイント、コマンド、設定
│   ├── plugin/        # プラグイン実装
│   │   ├── base/      # 基本ファクトリ（Item, Fluid, Recipe）
│   │   └── [plugin]/  # 各プラグイン
│   ├── registry/      # プラグイン登録
│   ├── render/        # アイコン/Mob 画像レンダリング
│   └── util/          # ユーティリティ
└── sql/               # JPA エンティティ（データベーススキーマ）
    ├── base/          # Item, Fluid, Recipe, Mob
    ├── forge/         # OreDictionary, FluidContainer
    ├── gregtech/      # GregTechRecipe
    ├── quest/         # Quest, QuestLine, Task, Reward
    └── thaumcraft/    # Aspect, AspectEntry
```

### データフロー
1. **Minecraft データ** → **Processor** が処理
2. **Processor** → **Factory** で SQL エンティティ生成
3. **Factory** → **EntityManager** で Hibernate 経由 DB 永続化
4. **Renderer** → アイテム/液体アイコンを ZIP に書き出し

### 技術スタック
- **ORM**: Hibernate + Jakarta Persistence (JPA)
- **DB**: HSQLDB（デフォルト）/ PostgreSQL
- **シリアライゼーション**: Protocol Buffers（NBT、レシピ構造）
- **アノテーション**: Lombok, AutoValue
- **Mod フレームワーク**: Forge 1.7.10 (RetroFuturaGradle)

## 新規プラグイン追加手順

1. `sql/[plugin]/` に JPA エンティティと Repository を作成
2. `Plugin` enum に新規プラグイン追加
3. `exporter/plugin/[plugin]/` に `*PluginExporter`, Factory, Processor を実装
4. `PluginRegistry` に `RegistryEntry` を追加（依存 Mod があれば `ModDependency` も追加）

## 設定オプション

主な設定は [ConfigOptions.java](src/main/java/com/github/dcysteine/nesql/exporter/main/config/ConfigOptions.java) で定義：
- `use_postgresql`: PostgreSQL 使用（デフォルト: false → HSQLDB）
- `render_icons` / `render_mobs`: 画像レンダリング有効化
- `enabled_plugins`: 有効プラグインリスト
