package fr.maxlego08.essentials.module.modules.chat;

import java.util.List;
import java.util.Set;

/**
 * Handles forbidden Unicode character filtering in chat messages.
 * <p>
 * When {@code cancelOnFail} is {@code false} (default): removes forbidden characters and lets the message through.
 * When {@code cancelOnFail} is {@code true}: cancels the entire message if any forbidden character is found.
 */
public class ForbiddenUnicode {

    private final Set<Integer> forbiddenCodePoints;
    private final boolean cancelOnFail;

    public ForbiddenUnicode(List<String> forbiddenChars, boolean cancelOnFail) {
        this.cancelOnFail = cancelOnFail;
        // Pre-build a set of code points for O(1) lookup – avoids string scanning per-char
        this.forbiddenCodePoints = new java.util.HashSet<>();
        for (String s : forbiddenChars) {
            if (s == null || s.isEmpty()) continue;
            s.codePoints().forEach(forbiddenCodePoints::add);
        }
    }

    /**
     * @return {@code true} if the forbidden list is empty and filtering can be skipped entirely.
     */
    public boolean isEmpty() {
        return forbiddenCodePoints.isEmpty();
    }

    /**
     * Checks whether the message contains any forbidden code point.
     */
    public boolean containsForbidden(String message) {
        return message.codePoints().anyMatch(forbiddenCodePoints::contains);
    }

    /**
     * Returns a cleaned copy of {@code message} with all forbidden code points removed.
     * This is only called when {@code cancelOnFail == false}.
     */
    public String removeForbidden(String message) {
        StringBuilder sb = new StringBuilder(message.length());
        message.codePoints()
                .filter(cp -> !forbiddenCodePoints.contains(cp))
                .forEach(cp -> sb.appendCodePoint(cp));
        return sb.toString();
    }

    public boolean isCancelOnFail() {
        return cancelOnFail;
    }
}
