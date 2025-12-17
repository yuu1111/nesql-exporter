package com.github.dcysteine.nesql.exporter.main;

import com.github.dcysteine.nesql.exporter.main.config.ConfigOptions;
import com.github.dcysteine.nesql.sql.Plugin;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
 * - プラグイン用ロガーの取得
 * - ゲーム内チャットへのメッセージ送信
 * - エラーログファイルへの警告・エラー出力
 * - 進捗表示用の間欠ログ
 */
public final class Log {

    private static final String ERROR_LOG_FILENAME = "export-errors.log";
    private static final String SEPARATOR_LINE = "=".repeat(80);
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * MOD全体で使用するメインロガー
     */
    public static final Logger MOD = LogManager.getLogger(Main.MOD_NAME);

    private static PrintWriter errorLogWriter = null;
    private static int warningCount = 0;
    private static int errorCount = 0;

    private Log() {}

    /**
     * プラグイン用のロガーを取得する
     *
     * @param plugin プラグイン
     * @return プラグイン名付きのロガー
     */
    public static Logger getLogger(Plugin plugin) {
        return LogManager.getLogger(Main.MOD_NAME + "/" + plugin.getName());
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
     * @param repositoryDirectory リポジトリディレクトリ
     */
    public static void initErrorLog(File repositoryDirectory) {
        closeErrorLog();
        warningCount = 0;
        errorCount = 0;

        try {
            File errorLogFile = new File(repositoryDirectory, ERROR_LOG_FILENAME);
            errorLogWriter = new PrintWriter(new FileWriter(errorLogFile, false));
            writeHeader();
            MOD.info("Error log initialized: {}", errorLogFile.getAbsolutePath());
        } catch (IOException e) {
            MOD.error("Failed to create error log file", e);
        }
    }

    /**
     * エラーログファイルをクローズする
     * エクスポート終了時に呼び出す
     */
    public static void closeErrorLog() {
        if (errorLogWriter != null) {
            writeFooter();
            errorLogWriter.close();
            errorLogWriter = null;

            if (warningCount > 0 || errorCount > 0) {
                MOD.info("Export completed with {} warnings and {} errors. See {} for details.",
                        warningCount, errorCount, ERROR_LOG_FILENAME);
            }
        }
    }

    /**
     * 警告メッセージをログ出力する
     * 標準ロガーとエラーログファイルの両方に出力する
     *
     * @param logger 使用するロガー
     * @param message フォーマット用メッセージ
     * @param args メッセージのパラメータ
     */
    public static void warn(Logger logger, String message, Object... args) {
        logger.warn(message, args);
        writeToErrorLog("WARN", logger.getName(), formatMessage(message, args));
        warningCount++;
    }

    /**
     * エラーメッセージをログ出力する
     * 標準ロガーとエラーログファイルの両方に出力する
     *
     * @param logger 使用するロガー
     * @param message フォーマット用メッセージ
     * @param args メッセージのパラメータ
     */
    public static void error(Logger logger, String message, Object... args) {
        logger.error(message, args);
        writeToErrorLog("ERROR", logger.getName(), formatMessage(message, args));
        errorCount++;
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

    /**
     * 設定されたログ間隔に基づき、メッセージをログ出力すべきかを判定する
     *
     * @param count 現在のカウント
     * @return ログ出力すべき場合はtrue
     */
    public static boolean intermittentLog(int count) {
        int loggingFrequency = ConfigOptions.LOGGING_FREQUENCY.get();
        return loggingFrequency > 0 && count % loggingFrequency == 0;
    }

    /**
     * 設定されたログ間隔でメッセージをログ出力する
     *
     * @param logger 使用するロガー
     * @param formatString countで置換されるプレースホルダを含むフォーマット文字列
     * @param count 現在の進捗カウント
     * @return メッセージがログ出力された場合はtrue
     */
    public static boolean intermittentLog(Logger logger, String formatString, int count) {
        if (intermittentLog(count)) {
            logger.info(formatString, count);
            return true;
        }
        return false;
    }

    private static void writeToErrorLog(String level, String loggerName, String message) {
        if (errorLogWriter != null) {
            String timestamp = LocalDateTime.now().format(TIME_FORMAT);
            errorLogWriter.printf("[%s] [%s] [%s] %s%n", timestamp, level, loggerName, message);
            errorLogWriter.flush();
        }
    }

    private static void writeHeader() {
        String timestamp = LocalDateTime.now().format(DATE_TIME_FORMAT);
        errorLogWriter.println(SEPARATOR_LINE);
        errorLogWriter.println("NESQL Exporter - Error Log");
        errorLogWriter.println("Started: " + timestamp);
        errorLogWriter.println(SEPARATOR_LINE);
        errorLogWriter.println();
        errorLogWriter.flush();
    }

    private static void writeFooter() {
        errorLogWriter.println();
        errorLogWriter.println(SEPARATOR_LINE);
        errorLogWriter.printf("Summary: %d warnings, %d errors%n", warningCount, errorCount);
        errorLogWriter.println(SEPARATOR_LINE);
    }

    /**
     * log4j形式のメッセージをフォーマットする
     * プレースホルダ {@code {}} をパラメータで置換する
     *
     * @param message フォーマット用メッセージ
     * @param args 置換パラメータ
     * @return フォーマット済みメッセージ
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
}
