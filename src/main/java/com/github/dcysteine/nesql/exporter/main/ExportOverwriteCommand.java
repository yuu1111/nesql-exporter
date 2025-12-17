package com.github.dcysteine.nesql.exporter.main;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;

import java.util.List;

/**
 * NESQLエクスポートを実行するコマンド
 * 既存リポジトリがある場合は上書きする
 */
final class ExportOverwriteCommand implements ICommand {
    /**
     * コマンド名を取得する
     *
     * @return コマンド名
     */
    @Override
    public String getCommandName() {
        return "nesqlf";
    }

    /**
     * コマンドの使用方法を取得する
     *
     * @param unused 未使用
     * @return 使用方法の文字列
     */
    @Override
    public String getCommandUsage(ICommandSender unused) {
        return "/nesqlf [filename suffix]";
    }

    /**
     * コマンドのエイリアスを取得する
     *
     * @return エイリアスなし
     */
    @Override
    @SuppressWarnings("rawtypes")
    public List getCommandAliases() {
        return null;
    }

    /**
     * コマンドを実行する
     *
     * @param sender コマンド送信者
     * @param args コマンド引数
     */
    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length > 1) {
            Log.chatMessage("Too many parameters! Usage: " + getCommandUsage(sender));
            return;
        }

        Exporter exporter;
        if (args.length == 1) {
            exporter = new Exporter(true, args[0]);
        } else {
            exporter = new Exporter(true);
        }
        new Thread(exporter::exportReportException).start();
    }

    /**
     * コマンド送信者がこのコマンドを使用できるか判定する
     *
     * @param unused 未使用
     * @return 常にtrue
     */
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender unused) {
        return true;
    }

    /**
     * タブ補完オプションを取得する
     *
     * @param unused 未使用
     * @param args コマンド引数
     * @return 補完オプションなし
     */
    @Override
    @SuppressWarnings("rawtypes")
    public List addTabCompletionOptions(ICommandSender unused, String[] args) {
        return null;
    }

    /**
     * 指定インデックスがユーザー名かどうか判定する
     *
     * @param strings 引数配列
     * @param i インデックス
     * @return 常にfalse
     */
    @Override
    public boolean isUsernameIndex(String[] strings, int i) {
        return false;
    }

    /**
     * コマンドを比較する
     *
     * @param other 比較対象
     * @return 比較結果
     */
    @Override
    public int compareTo(Object other) {
        if (other instanceof ICommand) {
            return this.getCommandName().compareTo(((ICommand) other).getCommandName());
        }

        return 0;
    }
}
