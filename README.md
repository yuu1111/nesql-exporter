## Not Enough SQL Exporter

NESQL のエクスポーターモジュールです。サーバーモジュールは[こちら](https://github.com/D-Cysteine/nesql-server)にあります。現在も開発中です。

エクスポートされるデータベースは [HSQLDB](http://hsqldb.org/) 形式で、HSQLDB クライアントで直接クエリを実行することも可能です。

現在サポートされているエクスポートデータ:

* NEI アイテムリスト
* Forge 鉱石辞書
* Forge 液体および液体コンテナデータ
* Minecraft 作業台・かまどレシピ
* GTNH GT5 レシピマップ
* BetterQuesting クエスト
* Thaumcraft アイテムアスペクト

GTNH の場合、エクスポートされるデータベースファイルは約 600 MB で、エクスポート処理には約 60 分かかります。エクスポートされる画像は約 15 万枚で、合計約 90 MB（ファイルオーバーヘッドによりディスク上ではそれ以上）になります。

### 使用方法

1. `NESQL-Exporter-*.jar` と `NESQL-Exporter-*-deps.jar` を `mods/` フォルダに配置します。必須の依存 Mod は `NotEnoughItems` のみです。
2. `mods/` フォルダに `bugtorch-1.7.10-*.jar` がある場合は、エンチャントアイテムのレンダリングと競合するため、一時的に別の場所に移動してください。詳細は下記を参照。
3. Minecraft を起動してワールドに参加します。新規のクリエイティブ・シングルプレイヤーワールドの使用を推奨します。エクスポーターは現在のプレイヤー状態を使用するため、例えば `Spice of Life` がインストールされている場合、ツールチップには食べた食べ物が反映されます。
4. GTNH 版の `NotEnoughItems` を使用している場合は、インベントリを開いて NEI アイテムリストを表示し、読み込ませてください。これを忘れると、一部のアイテムがエクスポートされない場合があります。
5. `Thaumcraft` データをエクスポートする場合は、すべての `Thaumcraft` 知識を取得することを推奨します。知識が不足していると、一部のデータが正しくエクスポートされない場合があります。詳細な手順は下記を参照。
6. `/nesql` コマンドを実行します。オプションで `/nesql your_repository_name` のようにリポジトリ名を指定することもできます。
7. ゲームを一時停止しても、エクスポートはバックグラウンドで継続されます。これにより処理が少し速くなる場合があります。
8. エクスポート処理が完了するまで待ちます。インストールされている Mod の数によっては非常に長い時間がかかる場合があります。エクスポートされたデータベースは `.minecraft/nesql` にあります。
9. エクスポートが完了したら、2つの mod jar を削除できます。先ほど `bugtorch-1.7.10-*.jar` を移動した場合は、元に戻すことを忘れないでください。

ログに `System.exit()` の呼び出しに関する Forge の警告が表示される場合がありますが、これは Hibernate のライブラリの1つに `System.exit()` の呼び出しが含まれているためです。無視して問題ありません。

### エンチャントアイテムのレンダリング問題

エンチャントアイテム（紫色の光沢オーバーレイがあるもの）が空白の画像として表示される場合、BugTorch Mod との競合が原因と考えられます。

[この行](https://github.com/GTNewHorizons/BugTorch/blob/adec7fb0d48f499344cb9f4cf9c2f597b6ddb687/src/main/java/jss/bugtorch/mixins/minecraft/client/renderer/entity/MixinItemRenderer.java)が問題の原因と思われますが、Mixin であるため NESQL Exporter 側での修正は困難です。エクスポート完了まで `mods/` フォルダから BugTorch を一時的に削除することを推奨します。

### Thaumcraft 知識

`Thaumcraft` データをエクスポートする場合、キャラクターに `Thaumcraft` 知識が不足していると一部のデータが正しくエクスポートされない場合があります。エクスポート前に以下の手順ですべての知識を取得することを推奨します：

1. クリエイティブ版ソーモノミコンを読む
2. 以下のコマンドを実行してすべての歪みを削除：
   * `/tc warp @p set 0`
   * `/tc warp @p set 0 PERM`
   * `/tc warp @p set 0 TEMP`
