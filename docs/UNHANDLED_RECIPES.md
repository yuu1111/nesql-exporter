# 未対応クラフトレシピ一覧

GTNH 2.8.3 でのエクスポート時に検出された未対応レシピタイプの一覧です。

## 概要

| 優先度 | レシピ数 | 説明 |
|--------|---------|------|
| 高 | 2,464 | 100件以上のレシピタイプ |
| 中 | 136 | 30件以上のレシピタイプ |
| 低 | 28 | その他 |
| **合計** | **2,628** | |

## 詳細一覧

### 高優先度（100件以上）

| レシピタイプ | 件数 | MOD | パッケージ |
|-------------|------|-----|-----------|
| `ShulkerNBTRecipe` | 2,304 | GTNH (Et Futurum Requiem) | `com.dreammaster.scripts.ScriptEFR$ShulkerNBTRecipe` |
| `ShapedRecipe` | 142 | Applied Energistics 2 | `appeng.recipes.game.ShapedRecipe` |
| `ShapelessRecipe` | 18 | Applied Energistics 2 | `appeng.recipes.game.ShapelessRecipe` |

### 中優先度（30件以上）

| レシピタイプ | 件数 | MOD | パッケージ |
|-------------|------|-----|-----------|
| `ShapedBloodOrbRecipe` | 61 | Blood Magic | `WayofTime.alchemicalWizardry.api.items.ShapedBloodOrbRecipe` |
| `BWNBTDependantCraftingRecipe` | 37 | BartWorks | `bartworks.API.recipe.BWNBTDependantCraftingRecipe` |
| `ShapelessResetRecipe` | 33 | Logistics Pipes | `logisticspipes.recipes.ShapelessResetRecipe` |
| `ShapelessBloodOrbRecipe` | 5 | Blood Magic | `WayofTime.alchemicalWizardry.api.items.ShapelessBloodOrbRecipe` |

### 低優先度（その他）

| レシピタイプ | 件数 | MOD | パッケージ |
|-------------|------|-----|-----------|
| `KnightArmourRecipie` | 4 | Battlegear 2 | `mods.battlegear2.heraldry.KnightArmourRecipie` |
| `RecipeMicroBlocks` | 4 | Extra Utilities | `com.rwtema.extrautils.multipart.microblock.RecipeMicroBlocks` |
| `LocomotivePaintingRecipe` | 3 | Railcraft | `mods.railcraft.common.carts.LocomotivePaintingRecipe` |
| `BigDoorRecipe` | 2 | Malisis Doors | `net.malisis.doors.recipe.BigDoorRecipe` |
| `ShapedBuilderRecipe` | 2 | ProjectRed | `mrtjp.projectred.core.libmc.recipe.ShapedBuilderRecipe` |
| `HelmRevealingRecipe` | 1 | Botania | `vazkii.botania.common.crafting.recipe.HelmRevealingRecipe` |
| `RecipeRemoteInventory` | 1 | RemoteIO | `remoteio.common.recipe.RecipeRemoteInventory` |
| `PrinterPaperRollRecipe` | 1 | OpenPrinter | `pcl.openprinter.items.PrinterPaperRollRecipe` |
| `GeneRecipe` | 1 | Gendustry | `net.bdew.gendustry.forestry.GeneRecipe` |
| `FabricationRecipes$$anon$1` | 1 | ProjectRed | `mrtjp.projectred.fabrication.FabricationRecipes$$anon$1` |
| `ShapelessBuilderRecipe` | 1 | ProjectRed | `mrtjp.projectred.core.libmc.recipe.ShapelessBuilderRecipe` |
| `RoutingTicketCopyRecipe` | 1 | Railcraft | `mods.railcraft.common.util.crafting.RoutingTicketCopyRecipe` |
| `ShieldRemoveArrowRecipie` | 1 | Battlegear 2 | `mods.battlegear2.recipies.ShieldRemoveArrowRecipie` |
| `QuiverRecipie2` | 1 | Battlegear 2 | `mods.battlegear2.recipies.QuiverRecipie2` |
| `AdvRecipe` | 1 | IC2 | `ic2.core.AdvRecipe` |
| `MatingRecipe` | 1 | Forestry | `forestry.lepidopterology.recipes.MatingRecipe` |
| `RecipeUnsigil` | 1 | Extra Utilities | `com.rwtema.extrautils.crafting.RecipeUnsigil` |
| `RecipeSoul` | 1 | Extra Utilities | `com.rwtema.extrautils.crafting.RecipeSoul` |
| `MicroRecipe$` | 1 | Forge Microblocks | `codechicken.microblock.MicroRecipe$` |
| `EnderStorageRecipe` | 1 | Ender Storage | `codechicken.enderstorage.common.EnderStorageRecipe` |

## 対応方針

### Applied Energistics 2 レシピ

AE2の`ShapedRecipe`と`ShapelessRecipe`は標準の`IRecipe`を継承しているため、
`ShapedOreRecipe`/`ShapelessOreRecipe`と同様の方法で処理可能。

```java
// appeng.recipes.game.ShapedRecipe extends IRecipe
// appeng.recipes.game.ShapelessRecipe extends IRecipe
```

### BartWorks NBT依存レシピ

Circuit Imprint（回路インプリント）関連のレシピ。NBTデータによって出力が変わる特殊なレシピ。
`BWNBTDependantCraftingRecipe`は`IRecipe`を直接実装しており、`getInput()`メソッドがないため
リフレクションで内部フィールド（`shape`と`charToStackMap`）にアクセスして処理する。

```java
// リフレクションで取得するフィールド
// String[] shape - 3x3クラフトグリッドのパターン
// Map<Character, ItemStack> charToStackMap - パターン文字とアイテムのマッピング
```

### Blood Magic レシピ

Blood Orb（血のオーブ）を材料として使用するレシピ。
`ShapedBloodOrbRecipe`と`ShapelessBloodOrbRecipe`は両方とも`getInput()`メソッドを持つため、
標準的な方法で処理可能。

```java
// ShapedBloodOrbRecipe.getInput() -> Object[]
// ShapelessBloodOrbRecipe.getInput() -> ArrayList<Object>
```

### GTNH ShulkerNBTRecipe

Et Futurum Requiem で追加されるシュルカーボックスの染色レシピ。
2,304件と大量だが、全て色違いのバリエーションであり、データベースに保存する価値が低いためスキップ。
クラス名による判定（`ShulkerNBTRecipe`を含むかどうか）で識別。

### Logistics Pipes レシピ

`ShapelessResetRecipe`はアイテムのNBTデータをリセットするレシピ。
`item`と`meta`フィールドにリフレクションでアクセスして入力アイテムを取得。

```java
// リフレクションで取得するフィールド
// Item item - 対象アイテム
// int meta - メタデータ
```

## 実装ステータス

- [x] Applied Energistics 2 (`ShapedRecipe`, `ShapelessRecipe`) - v0.5.3で実装
- [x] BartWorks (`BWNBTDependantCraftingRecipe`) - v0.5.3で実装（リフレクション使用）
- [x] Blood Magic (`ShapedBloodOrbRecipe`, `ShapelessBloodOrbRecipe`) - v0.5.3で実装
- [x] GTNH Et Futurum (`ShulkerNBTRecipe`) - v0.5.3でスキップ対応（2,304件の染色レシピ）
- [x] Logistics Pipes (`ShapelessResetRecipe`) - v0.5.3で実装（リフレクション使用）
- [ ] その他（低優先度、合計28件）

## 備考

- 多くの特殊レシピはNBTデータに依存するため、単純な入出力の記録だけでは不十分な場合がある
- 一部のレシピ（`MatingRecipe`等）はゲーム内での特殊な操作を表すもので、クラフティングテーブルでは作成できない
- マイクロブロック系レシピは動的に生成されるため、全パターンの記録は現実的ではない
