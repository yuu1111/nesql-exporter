# nesql-exporter GTNH 2.8.3 互換性修正

## 概要

nesql-exporter 0.5.2 は GTNH 2.8.3 と互換性がなかった。
GregTech の API 変更により `GT_LanguageManager` クラスが存在しないためエラーが発生していた。

## 修正内容

### RecipeMap.java

**変更前**:
```java
import gregtech.api.util.GT_LanguageManager;
// ...
this.name = GT_LanguageManager.getTranslation(recipeMap.unlocalizedName);
```

**変更後**:
```java
import net.minecraft.util.StatCollector;
// ...
this.name = StatCollector.translateToLocal(recipeMap.unlocalizedName);
```

Minecraft 標準の `StatCollector.translateToLocal()` を使用することで、
GTNH 2.8.3 の GregTech API 変更に対応。

## 環境情報

- **GTNH Version**: 2.8.3
- **nesql-exporter Version**: 0.5.2+
- **Minecraft**: 1.7.10
- **Java**: 17

## ビルド手順

```bash
cd c:\Dev\Minecraft\nesql-exporter
./gradlew build
```

ビルド成果物:
- `build/libs/NESQL-Exporter-*.jar` - Mod 本体
- `build/libs/NESQL-Exporter-*-deps.jar` - 依存関係

## テスト手順

1. ビルドした JAR を GTNH の `mods/` にコピー
2. Minecraft 起動
3. クリエイティブワールドで `/nesql` 実行
4. エクスポート完了を確認
