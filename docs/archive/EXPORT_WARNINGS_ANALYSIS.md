# Export Warnings Analysis

エクスポート時に発生する警告の分析結果 (2025-12-17)

## サマリー

- 警告総数: 354件
- エラー: 0件

## カテゴリ別分析

### 1. Null Output レシピ (約180件)

動的レシピで `getRecipeOutput()` が null を返すケース。入力が確定するまで出力が決まらないレシピ。

| タイプ | Mod | レシピクラス |
|--------|-----|-------------|
| 染色 | Minecraft | RecipesArmorDyes |
| 染色 | Botania | LensDyeingRecipe, PhantomInkRecipe |
| 染色 | Thaumcraft | RecipesRobeArmorDyes, RecipesVoidRobeArmorDyes |
| 染色 | OpenComputers | ColorizeRecipe, DecolorizeRecipe |
| 染色 | Witchery | RobeColourizationRecipe, CloakColourizationRecipe |
| 染色 | Battlegear2 | DyeRecipie |
| コピー | Minecraft | RecipesMapCloning, RecipeBookCloning |
| コピー | TwilightForest | TFMapCloningRecipe |
| コピー | Railcraft | RoutingTableCopyRecipe |
| コピー | Et Futurum | RecipeDuplicatePattern |
| 合成 | Botania | CompositeLensRecipe, ManaGunLensRecipe |
| 合成 | AE2 | FacadeRecipe |
| 修復 | Railcraft | RotorRepairRecipe |
| 修復 | Thaumic Horizons | RecipeVoidPuttyRepair |
| 修復 | Witchery | RecipeShapelessRepair (多数) |
| 特殊 | Botania | AesirRingRecipe, KeepIvyRecipe, CosmeticAttachRecipe |
| 特殊 | Witchery | RecipeShapelessAddColor, RecipeShapelessAddPotion, RecipeShapelessPoppet |
| 特殊 | Et Futurum | RecipeAddPattern, RecipeFixedFireworks |
| 特殊 | Steve's Factory | ClusterRecipe |
| 特殊 | ProjectBlue | CraftControlPanel, CraftMiniatureItem, PaintControl |
| 特殊 | Binnie | CeramicTileRecipe, PigmentRecipe |
| 特殊 | ProjectRed | ChipResetRecipe |
| 特殊 | Backpack | RecipeRecolorBackpack, RecipeIntelligentWorkbenchBackpack |
| 特殊 | Nuclear Control | StorageArrayRecipe |
| 特殊 | Extra Utilities | RecipeHorseTransmutation, RecipeUnEnchanting |
| 特殊 | OpenBlocks | CrayonMixingRecipe, MapCloneRecipe |
| 特殊 | Akashic Tome | AttachementRecipe |
| 特殊 | Tainted Magic | RecipeVoidsentBlood |
| 特殊 | Thaumic Tinkerer | SpellClothRecipe |

**影響度**: 低
**対応**: 仕様上避けられない。ログレベルをINFOに下げることを検討

### 2. Bad Stack Size (約70件)

#### Size 0 のレシピ (問題あり)
```
[0xitem.map@32767]
[0xitem.brewingStand@0] (5件)
```

#### Size 2-4 のレシピ (false positive)
```
[2xitem.itemGearTumbaga@0] (2件)
[2xitem.itemGearEglinSteel@0] (2件)
[2xtile.etfuturum.cherry_log@2] (4件)
[4xgt.blockgranites@0,7,8,15] (16件)
[4xgt.blockconcretes@0,7,8,15] (16件)
[4xgt.blockstones@0,7,8,15] (16件)
```

**影響度**: 中 (false positive が多い)
**対応**: stack size 0 のみを警告するよう修正が必要

### 3. 未対応レシピタイプ (約25件)

カスタムレシピクラスで現在未対応のもの:

| Mod | レシピクラス | 用途 |
|-----|-------------|------|
| Railcraft | LocomotivePaintingRecipe | 機関車の塗装 |
| Railcraft | RoutingTicketCopyRecipe | ルーティングチケットのコピー |
| CodeChicken | MicroRecipe | マイクロブロック |
| CodeChicken | EnderStorageRecipe | エンダーストレージ |
| ProjectRed | ShapedBuilderRecipe | 回路基板 |
| ProjectRed | FabricationRecipes | ICチップ製造 |
| IC2 | AdvRecipe | 高度なレシピ |
| Extra Utilities | RecipeMicroBlocks | マイクロブロック |
| Battlegear2 | QuiverRecipie2 | 矢筒 |
| Forestry | MatingRecipe | 蝶の交配 |
| Gendustry | GeneRecipe | 遺伝子操作 |
| Botania | HelmRevealingRecipe | ヘルメット (Revealing) |
| OpenPrinter | PrinterPaperRollRecipe | プリンター用紙 |

**影響度**: 中
**対応**: 必要に応じて個別ハンドラを追加。[UNHANDLED_RECIPES.md](UNHANDLED_RECIPES.md) 参照

### 4. AE2 IIngredient 失敗 (9件)

```
Failed to get ItemStack from AE2 IIngredient: appliedenergistics2:ItemMaterial.IronNugget:0
```

**原因**: GTNHでは鉄ナゲットが削除/GT版に置換されている
**影響度**: 低
**対応**: 削除アイテムとして無視してOK

## 推奨対応

### 優先度: 高
1. **Bad stack size チェックの修正**: size 0 のみを警告、1以上は許容

### 優先度: 中
2. **警告レベルの調整**: null output を INFO レベルに下げる (大量すぎるため)
3. **未対応レシピハンドラの追加**: 必要なものから順次対応

### 優先度: 低
4. **AE2 IIngredient**: 現状のまま (削除アイテムのため)

## 関連ファイル

- [UNHANDLED_RECIPES.md](UNHANDLED_RECIPES.md) - 未対応レシピタイプの詳細
- [GTNH_283_COMPATIBILITY.md](GTNH_283_COMPATIBILITY.md) - GTNH 2.8.3 互換性情報
