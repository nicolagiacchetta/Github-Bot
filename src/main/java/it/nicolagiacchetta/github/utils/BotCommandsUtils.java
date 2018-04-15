package it.nicolagiacchetta.github.utils;

import org.eclipse.egit.github.core.Comment;

public class BotCommandsUtils {

    public final static String BOT_HOTWORD = "@bot";

    public static boolean isHotComment (Comment comment) {
        return isHotComment(comment.getBody());
    }

    public static boolean isHotComment (String comment) {
        return comment.trim().startsWith(BOT_HOTWORD);
    }

    public static String extractCommand (Comment comment) {
        return extractCommand(comment.getBody());
    }

    public static String extractCommand (String comment) {
        return comment.replace(BOT_HOTWORD, "").trim();
    }
}
