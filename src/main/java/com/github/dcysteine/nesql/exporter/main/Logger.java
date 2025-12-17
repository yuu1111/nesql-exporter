package com.github.dcysteine.nesql.exporter.main;

import com.github.dcysteine.nesql.exporter.main.config.ConfigOptions;
import com.github.dcysteine.nesql.sql.Plugin;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;

/**
 * ログ出力ユーティリティクラス
 *
 * 標準のlog4jロガーに加え、エクスポート中の警告・エラーを
 * 別ファイル(export-errors.log)に保存する機能を提供する
 */
public final class Logger {
    /** MOD全体で使用するメインロガー */
    public static final org.apache.logging.log4j.Logger MOD = LogManager.getLogger(Main.MOD_NAME);

    private static PrintWriter errorLogWriter = null;
    private static int warningCount = 0;
    private static int errorCount = 0;

    // 静的クラス
    private Logger() {}

    /**
     * プラグイン用のロガーを取得する
     *
     * @param plugin プラグイン
     * @return プラグイン名付きのロガー
     */
    public static org.apache.logging.log4j.Logger getLogger(Plugin plugin) {
        return LogManager.getLogger(String.format("%s/%s", Main.MOD_NAME, plugin.getName()));
    }

    /**
     * ゲーム内チャットにメッセージを表示する
     *
     * @param message 表示するメッセージ
     */
    public static void chatMessage(String message) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(message));
    }

    /**
     * エラーログファイルを初期化する
     * エクスポート開始時に呼び出す
     *
     * ログファイルは指定されたリポジトリディレクトリ内に
     * export-errors.log として作成される
     *
     * @param repositoryDirectory リポジトリディレクトリ
     */
    public static void initErrorLog(File repositoryDirectory) {
        closeErrorLog(); // 既存のログをクローズ
        warningCount = 0;
        errorCount = 0;

        try {
            File errorLogFile = new File(repositoryDirectory, "export-errors.log");
            errorLogWriter = new PrintWriter(new FileWriter(errorLogFile, false));

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            errorLogWriter.println("=".repeat(80));
            errorLogWriter.println("NESQL Exporter - Error Log");
            errorLogWriter.println("Started: " + timestamp);
            errorLogWriter.println("=".repeat(80));
            errorLogWriter.println();
            errorLogWriter.flush();

            MOD.info("Error log initialized: {}", errorLogFile.getAbsolutePath());
        } catch (IOException e) {
            MOD.error("Failed to create error log file", e);
        }
    }

    /**
     * エラーログファイルをクローズする
     * エクスポート終了時に呼び出す
     *
     * <p>クローズ時にサマリー(警告数、エラー数)が出力される
     */
    public static void closeErrorLog() {
        if (errorLogWriter != null) {
            errorLogWriter.println();
            errorLogWriter.println("=".repeat(80));
            errorLogWriter.println("Summary: " + warningCount + " warnings, " + errorCount + " errors");
            errorLogWriter.println("=".repeat(80));
            errorLogWriter.close();
            errorLogWriter = null;

            if (warningCount > 0 || errorCount > 0) {
                MOD.info("Export completed with {} warnings and {} errors. See export-errors.log for details.",
                        warningCount, errorCount);
            }
        }
    }

    /**
     * 警告メッセージをログ出力する
     * 標準ロガーとエラーログファイルの両方に出力される
     *
     * @param logger 使用するロガー
     * @param message メッセージ（{}でパラメータを指定）
     * @param args メッセージのパラメータ
     */
    public static void warn(org.apache.logging.log4j.Logger logger, String message, Object... args) {
        logger.warn(message, args);
        writeToErrorLog("WARN", logger.getName(), formatMessage(message, args));
        warningCount++;
    }

    /**
     * エラーメッセージをログ出力する
     * 標準ロガーとエラーログファイルの両方に出力される
     *
     * @param logger 使用するロガー
     * @param message メッセージ（{}でパラメータを指定）
     * @param args メッセージのパラメータ
     */
    public static void error(org.apache.logging.log4j.Logger logger, String message, Object... args) {
        logger.error(message, args);
        writeToErrorLog("ERROR", logger.getName(), formatMessage(message, args));
        errorCount++;
    }

    /**
     * エラーログファイルに1行書き込む
     */
    private static void writeToErrorLog(String level, String loggerName, String message) {
        if (errorLogWriter != null) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            errorLogWriter.printf("[%s] [%s] [%s] %s%n", timestamp, level, loggerName, message);
            errorLogWriter.flush();
        }
    }

    /**
     * log4j形式のメッセージをフォーマットする。
     * {}をパラメータで置換する。
     */
    private static String formatMessage(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }
        String result = message;
        for (Object arg : args) {
            String replacement = arg != null ? arg.toString() : "null";
            result = result.replaceFirst("\\{}", Matcher.quoteReplacement(replacement));
        }
        return result;
    }

    /**
     * 設定されたログ間隔に基づき、メッセージをログ出力すべきかを判定する
     *
     * @param count 現在のカウント
     * @return ログ出力すべき場合はtrue
     */
    public static boolean intermittentLog(int count) {
        int loggingFrequency = ConfigOptions.LOGGING_FREQUENCY.get();
        if (loggingFrequency <= 0) {
            return false;
        }

        return count % loggingFrequency == 0;
    }

    /**
     * 設定されたログ間隔でメッセージをログ出力する
     *
     * @param logger 使用するロガー
     * @param formatString フォーマット文字列（{}が1つ含まれ、countで置換される）
     * @param count 現在の進捗カウント
     * @return メッセージがログ出力された場合はtrue
     */
    public static boolean intermittentLog(
            org.apache.logging.log4j.Logger logger, String formatString, int count) {
        if (intermittentLog(count)) {
            logger.info(formatString, count);
            return true;
        }
        return false;
    }

    /**
     * 現在のセッションでの警告数を取得する
     *
     * @return 警告数
     */
    public static int getWarningCount() {
        return warningCount;
    }

    /**
     * 現在のセッションでのエラー数を取得する
     *
     * @return エラー数
     */
    public static int getErrorCount() {
        return errorCount;
    }
}
