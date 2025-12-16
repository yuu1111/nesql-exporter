# nesql-exporter GTNH 2.8.3 互換性修正

## 概要

nesql-exporter 0.5.2 は GTNH 2.8.3 と互換性がない。
GregTechのAPI変更により `GT_LanguageManager` クラスが存在しないためエラーが発生する。

## エラー内容

```
java.lang.NoClassDefFoundError: gregtech/api/util/GT_LanguageManager
    at com.github.dcysteine.nesql.exporter.plugin.gregtech.util.RecipeMap.<init>(RecipeMap.java:646)
    at com.github.dcysteine.nesql.exporter.plugin.gregtech.util.RecipeMap.<clinit>(RecipeMap.java:10)
```

## 修正が必要なファイル

### 1. RecipeMap.java (Line 646)

**場所**: `src/main/java/com/github/dcysteine/nesql/exporter/plugin/gregtech/util/RecipeMap.java`

**問題のコード**:
```java
import gregtech.api.util.GT_LanguageManager;

// Line 646
this.name = GT_LanguageManager.getTranslation(recipeMap.unlocalizedName);
```

**修正方針**:
GTNH 2.8.3 で `GT_LanguageManager` の代替となるAPIを探す必要がある。
考えられる選択肢:
1. `recipeMap.getLocalizedName()` メソッドがあれば使用
2. `StatCollector.translateToLocal()` を使用
3. `unlocalizedName` をそのまま使用（翻訳なし）

## 環境情報

- **GTNH Version**: 2.8.3
- **nesql-exporter Version**: 0.5.2
- **Minecraft**: 1.7.10
- **Java**: 17

## セットアップ状況

### 完了済み
- [x] nesql-exporter をclone (`c:\Dev\Minecraft\nesql-exporter`)
- [x] Docker PostgreSQL コンテナ起動 (`webnei-postgres` on port 5432)
- [x] データベース `nesql-repository` 作成済み
- [x] nesql-exporter config 設定済み（PostgreSQL出力モード）
- [x] webnei-backend の .env 設定済み

### 未完了
- [ ] RecipeMap.java の GT_LanguageManager 修正
- [ ] nesql-exporter ビルド＆テスト
- [ ] エクスポート実行
- [ ] webnei-backend 動作確認

## ビルド手順

```bash
cd c:\Dev\Minecraft\nesql-exporter
./gradlew build
```

ビルド成果物: `build/libs/NESQL-Exporter-*.jar`

## テスト手順

1. ビルドしたJARを GTNH の mods/ にコピー
2. Minecraft起動
3. クリエイティブワールドで `/nesql` 実行
4. PostgreSQL にデータが入るか確認:
   ```bash
   docker exec webnei-postgres psql -U postgres -d "nesql-repository" -c "SELECT COUNT(*) FROM item;"
   ```

## 関連ファイルパス

| ファイル | パス |
|---------|------|
| nesql-exporter | `c:\Dev\Minecraft\nesql-exporter` |
| webnei-backend | `c:\Dev\Minecraft\webnei-backend` |
| GTNH mods | `C:\Users\yuu21\AppData\Roaming\PrismLauncher\instances\GT_New_Horizons_2.8.3_Java_17-25\.minecraft\mods` |
| nesql config | `...\.minecraft\config\nesql-exporter.cfg` |

## 次のアクション

1. GTNH 2.8.3 の GregTech ソースコードを確認し、`GT_LanguageManager` の代替を特定
2. `RecipeMap.java` を修正
3. ビルド＆テスト
