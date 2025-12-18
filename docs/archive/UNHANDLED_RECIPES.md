# クラフトレシピ対応状況

GTNH 2.8.3 でのエクスポート時に検出された特殊レシピタイプの対応状況です。

## 概要

| ステータス | レシピ数 | 説明 |
|-----------|---------|------|
| ✅ 実装済み | 2,600 | ハンドラ実装完了 |
| ⏭️ スキップ | 2,304 | 意図的にスキップ (ShulkerNBTRecipe) |
| ❌ 未対応 | 28 | 低優先度、対応予定なし |

## 実装済みレシピ

### 高優先度 (100件以上) - ✅ 全て対応済み

| レシピタイプ | 件数 | MOD | ステータス |
|-------------|------|-----|-----------|
| `ShulkerNBTRecipe` | 2,304 | GTNH (Et Futurum Requiem) | ⏭️ スキップ |
| `ShapedRecipe` | 142 | Applied Energistics 2 | ✅ 実装済み |
| `ShapelessRecipe` | 18 | Applied Energistics 2 | ✅ 実装済み |

### 中優先度 (30件以上) - ✅ 全て対応済み

| レシピタイプ | 件数 | MOD | ステータス |
|-------------|------|-----|-----------|
| `ShapedBloodOrbRecipe` | 61 | Blood Magic | ✅ 実装済み |
| `BWNBTDependantCraftingRecipe` | 37 | BartWorks | ✅ 実装済み |
| `ShapelessResetRecipe` | 33 | Logistics Pipes | ✅ 実装済み |
| `ShapelessBloodOrbRecipe` | 5 | Blood Magic | ✅ 実装済み |

### 低優先度 - ❌ 未対応 (28件)

合計28件の特殊レシピは対応しない。詳細は [付録A](#付録a-未対応レシピ詳細) を参照

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

### ✅ 実装済み (v0.5.3)

| レシピタイプ | MOD | 実装方法 |
|-------------|-----|---------|
| `ShapedRecipe` | Applied Energistics 2 | `IIngredient.getItemStackSet()` |
| `ShapelessRecipe` | Applied Energistics 2 | `IIngredient.getItemStackSet()` |
| `BWNBTDependantCraftingRecipe` | BartWorks | リフレクション (`shape`, `charToStackMap`) |
| `ShapedBloodOrbRecipe` | Blood Magic | `getInput()` |
| `ShapelessBloodOrbRecipe` | Blood Magic | `getInput()` |
| `ShapelessResetRecipe` | Logistics Pipes | リフレクション (`item`, `meta`) |

### ⏭️ スキップ対応 (v0.5.3)

| レシピタイプ | MOD | 理由 |
|-------------|-----|------|
| `ShulkerNBTRecipe` | GTNH Et Futurum | 2,304件全て色違いバリエーション、保存価値低 |

### ❌ 未対応 (対応予定なし)

低優先度レシピ (合計28件) は以下の理由で対応しない:
- 動的生成レシピ (マイクロブロック等) は全パターン記録が非現実的
- 特殊操作レシピ (`MatingRecipe`等) はクラフティングテーブル外の操作
- 件数が少なく実装コストに見合わない

## 備考

- 多くの特殊レシピはNBTデータに依存するため、単純な入出力の記録だけでは不十分な場合がある
- 一部のレシピ (`MatingRecipe`等) はゲーム内での特殊な操作を表すもので、クラフティングテーブルでは作成できない
- マイクロブロック系レシピは動的に生成されるため、全パターンの記録は現実的ではない

---

## 付録A: 未対応レシピ詳細

以下のレシピタイプは対応しない (合計28件)。

### 動的生成レシピ

| レシピタイプ | 件数 | MOD | 理由 |
|-------------|------|-----|------|
| `RecipeMicroBlocks` | 4 | Extra Utilities | マイクロブロック、パターン無限 |
| `MicroRecipe$` | 1 | Forge Microblocks | マイクロブロック、パターン無限 |

### 装飾・カラーリングレシピ

| レシピタイプ | 件数 | MOD | 理由 |
|-------------|------|-----|------|
| `KnightArmourRecipie` | 4 | Battlegear 2 | 紋章カスタマイズ |
| `LocomotivePaintingRecipe` | 3 | Railcraft | 機関車塗装、色パターン多数 |
| `EnderStorageRecipe` | 1 | Ender Storage | 色変更レシピ |

### 特殊クラフトレシピ

| レシピタイプ | 件数 | MOD | 理由 |
|-------------|------|-----|------|
| `BigDoorRecipe` | 2 | Malisis Doors | サイズ可変ドア |
| `ShapedBuilderRecipe` | 2 | ProjectRed | 回路基板、動的出力 |
| `ShapelessBuilderRecipe` | 1 | ProjectRed | 回路基板、動的出力 |
| `FabricationRecipes$$anon$1` | 1 | ProjectRed | ICチップ製造 |
| `HelmRevealingRecipe` | 1 | Botania | Thaumcraft連携ヘルメット |
| `RecipeRemoteInventory` | 1 | RemoteIO | インベントリリンク |
| `PrinterPaperRollRecipe` | 1 | OpenPrinter | 印刷用紙 |
| `AdvRecipe` | 1 | IC2 | IC2高度レシピ |

### コピー・転送レシピ

| レシピタイプ | 件数 | MOD | 理由 |
|-------------|------|-----|------|
| `RoutingTicketCopyRecipe` | 1 | Railcraft | チケットコピー、NBT依存 |
| `GeneRecipe` | 1 | Gendustry | 遺伝子コピー、NBT依存 |

### Battlegear 2 固有

| レシピタイプ | 件数 | MOD | 理由 |
|-------------|------|-----|------|
| `ShieldRemoveArrowRecipie` | 1 | Battlegear 2 | 盾から矢を外す |
| `QuiverRecipie2` | 1 | Battlegear 2 | 矢筒レシピ |

### Extra Utilities 固有

| レシピタイプ | 件数 | MOD | 理由 |
|-------------|------|-----|------|
| `RecipeUnsigil` | 1 | Extra Utilities | シジル解除 |
| `RecipeSoul` | 1 | Extra Utilities | ソウル関連 |

### ゲーム外操作

| レシピタイプ | 件数 | MOD | 理由 |
|-------------|------|-----|------|
| `MatingRecipe` | 1 | Forestry | 蝶の交配、クラフト不可 |
