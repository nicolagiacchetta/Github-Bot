package it.nicolagiacchetta.github.utils;

import org.junit.Test;

import static it.nicolagiacchetta.github.utils.BotCommandsUtils.BOT_HOTWORD;
import static it.nicolagiacchetta.github.utils.BotCommandsUtils.extractCommand;
import static it.nicolagiacchetta.github.utils.BotCommandsUtils.isHotComment;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class BotCommandsUtilsTest {

    private static final String COMMAND = "command";

    private static final String HOT_COMMENT = BOT_HOTWORD + " " + COMMAND;
    private static final String HOT_COMMENT_2 = " " + BOT_HOTWORD + " " + COMMAND;

    private static final String NOT_HOT_COMMENT = "bot command";
    private static final String NOT_HOT_COMMENT_2 = "@" + BOT_HOTWORD + " command";


    @Test
    public void isHotCommentTest_caseTrue () {
        assertIsHotComment(HOT_COMMENT,
                           HOT_COMMENT_2);
    }

    private void assertIsHotComment (String... comments) {
        for (String comment : comments)
            assertTrue("Failed assertion on " + comment, isHotComment(comment));
    }

    @Test
    public void isHotCommentTest_caseFalse () {
        assertIsNotHotComment(NOT_HOT_COMMENT,
                              NOT_HOT_COMMENT_2);
    }

    private void assertIsNotHotComment (String... comments) {
        for (String comment : comments)
            assertFalse("Failed assertion on " + comment, isHotComment(comment));
    }

    @Test
    public void extractCommandTest () {
        assertExtractCommand(HOT_COMMENT,
                             HOT_COMMENT_2);
    }

    private void assertExtractCommand (String... comments) {
        for (String comment : comments)
            assertEquals("Failed assertion on " + comment, COMMAND, extractCommand(comment));
    }
}
