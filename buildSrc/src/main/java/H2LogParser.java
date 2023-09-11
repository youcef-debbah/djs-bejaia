//import org.gradle.api.Transformer;
//import org.gradle.api.tasks.Copy;
//import org.jetbrains.annotations.NotNull;
//
//import java.nio.charset.StandardCharsets;
//import java.util.regex.Pattern;
//
//public class H2LogParser extends Copy {
//
//    private static final boolean SKIP_LONG_COLUMNS_LIST = true;
//    public static final int MAX_ALLOWED_COLUMNS_LIST_LENGTH = 60;
//
//    public H2LogParser() {
//        include("*.trace.db");
//        rename("(.+?)\\.trace\\.db", "$1.sql");
//        filter(new H2LogTransformer());
//        setFilteringCharset(StandardCharsets.UTF_8.name());
//    }
//
//    private static final class H2LogTransformer implements Transformer<String, String> {
//
//        private static final Pattern PARAMETERS_LIST_START = Pattern.compile("\\{1:");
//        public static final String NOINSPECTION_SQL_UNUSED = "// noinspection SqlUnused\n";
//        public static final String DYNAMIC_COMMENT_MARKER = "#log-this-statement-as-the-comment:";
//        public static final Pattern LONG_COLUMN_LIST_REGEX = Pattern.compile("select\\s+\\S.{" + MAX_ALLOWED_COLUMNS_LIST_LENGTH + ",}?\\sfrom");
//
//        @Override
//        @SuppressWarnings("NullableProblems")
//        public String transform(String line) {
//            if (!line.startsWith("/*SQL") || line.endsWith("*/COMMIT;"))
//                return null;
//
//            int commentIndex = line.indexOf(DYNAMIC_COMMENT_MARKER);
//            if (commentIndex >= 0)
//                return "/* " + extractComment(line, commentIndex) + " */\n";
//
//            String parsedLine = line.substring(line.indexOf("*/") + 2)
//                    .replace("\\\"", "\"")
//                    .replace("*/ ", "*/\n");
//
//            if (parsedLine.endsWith("};")) {
//                parsedLine = PARAMETERS_LIST_START.matcher(parsedLine).replaceFirst("; /* 1:");
//                parsedLine = parsedLine.substring(0, parsedLine.length() - 2) + " */";
//            }
//
//            if (parsedLine.equals("SELECT VALUE FROM INFORMATION_SCHEMA.SETTINGS WHERE NAME=? ; /* 1: 'QUERY_TIMEOUT' */"))
//                return null;
//
//            if (parsedLine.equals("SET TRACE_LEVEL_FILE 2;"))
//                return null;
//
//            if (parsedLine.startsWith("/* select count(association.id) from BasicPrincipalEntity"))
//                parsedLine = NOINSPECTION_SQL_UNUSED + parsedLine;
//
//            if (SKIP_LONG_COLUMNS_LIST)
//                parsedLine = LONG_COLUMN_LIST_REGEX.matcher(parsedLine).replaceAll("select * /*...*/ from");
//
//            return parsedLine + "\n";
//        }
//
//        @NotNull
//        public String extractComment(String line, int commentIndex) {
//            int beginIndex = commentIndex + DYNAMIC_COMMENT_MARKER.length();
//            String substring = line.substring(beginIndex);
//
//            int endIndex = substring.lastIndexOf('\'');
//            if (endIndex > 0)
//                return substring.substring(0, endIndex);
//            else
//                return substring;
//        }
//    }
//}
