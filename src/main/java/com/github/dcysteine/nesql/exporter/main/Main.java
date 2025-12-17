package com.github.dcysteine.nesql.exporter.main;

import com.github.dcysteine.nesql.Tags;
import com.github.dcysteine.nesql.exporter.main.config.Config;
import com.github.dcysteine.nesql.exporter.main.config.ConfigGuiFactory;
import com.github.dcysteine.nesql.exporter.main.config.ConfigOptions;
import com.github.dcysteine.nesql.exporter.render.Renderer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;

/**
 * NESQL Exporterのメインエントリーポイント
 */
@Mod(
        modid = Main.MOD_ID,
        name = Main.MOD_NAME,
        version = Main.MOD_VERSION,
        acceptableRemoteVersions = "*",  // Client-side-only mod.
        dependencies = Main.MOD_DEPENDENCIES,
        guiFactory = ConfigGuiFactory.CLASS_NAME)
public final class Main {
    /**
     * Mod ID
     */
    public static final String MOD_ID = "nesql-exporter";

    /**
     * Mod名
     */
    public static final String MOD_NAME = "Not Enough SQL Exporter";

    /**
     * Modバージョン
     */
    public static final String MOD_VERSION = Tags.EXPORTER_VERSION;

    /**
     * Mod依存関係
     */
    public static final String MOD_DEPENDENCIES = "required-after:NotEnoughItems;";

    /**
     * Modインスタンス
     */
    @Instance(MOD_ID)
    @SuppressWarnings("unused")
    public static Main instance;

    /**
     * Mod初期化時に呼ばれる
     *
     * @param event 初期化イベント
     */
    @EventHandler
    @SuppressWarnings("unused")
    public void onInitialization(FMLInitializationEvent event) {
        if (event.getSide() != Side.CLIENT) {
            return;
        }
        Log.MOD.info("Mod initialization starting...");

        ConfigGuiFactory.checkClassName();
        Config.initialize();
        Config.updateConfig();
        FMLCommonHandler.instance().bus().register(Renderer.INSTANCE);
        FMLCommonHandler.instance().bus().register(this);

        Log.MOD.info("Mod initialization complete!");
    }

    /**
     * サーバー起動時に呼ばれる
     * コマンドを登録する
     *
     * @param event サーバー起動イベント
     */
    @EventHandler
    @SuppressWarnings("unused")
    public void onServerStart(FMLServerStartingEvent event) {
        if (event.getSide() != Side.CLIENT) {
            return;
        }

        event.registerServerCommand(new ExportCommand());
        event.registerServerCommand(new ExportOverwriteCommand());
        Log.MOD.info("Commands registered!");
    }

    /**
     * クライアントがサーバーに接続した時に呼ばれる
     * 設定が有効な場合は自動エクスポートを実行する
     *
     * @param event 接続イベント
     */
    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onClientConnected(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        if (ConfigOptions.AUTO_EXPORT_ON_CONNECT.get()) {
            Log.MOD.info("Automatically exporting...");
            new Thread(new Exporter(false)::exportReportException).start();
        }
    }
}
