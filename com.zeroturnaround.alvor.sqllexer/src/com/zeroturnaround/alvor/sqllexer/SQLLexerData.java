package ee.stacc.productivity.edsl.sqllexer;

public class SQLLexerData{
/** Char classes (char - class (char)) */
private static final String CHAR_CLASSES_PACKED = 
"\10\0\2\3\1\5\2\0\1\4\22\0\1\3\1\31\1\0\2\0\1\30\1\0\1\6\1\13\1\14" + 
"\1\10\1\20\1\12\1\21\1\17\1\7\12\2\1\27\1\26\1\23\1\22\1\24\1\25\1\0\15\1" + 
"\1\33\14\1\4\0\1\11\1\0\15\1\1\33\14\1\1\15\1\32\1\16\uff82\0" + 
"";

public static final char[] CHAR_CLASSES = unpackCharClasses(CHAR_CLASSES_PACKED);

  /** 
   * Copied from JFlex code
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] unpackCharClasses(String packed) {
    char [] map = new char[0x10000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    int length = packed.length();
    while (i < length) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }

// Alphabet:
// 8 9 10 13 32 !%'()*+,-./0123456789:;<=>?ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz{|}

/** Number of states (table height) */
public static final int STATE_COUNT = 47;

/** Number of character classes (table width) */
public static final int CHAR_CLASS_COUNT = 28;

/** Transitions (state : state(char class)) */
public static final int[][] TRANSITIONS = {
/*   0 : */{    1,    2,    3,    4,    1,    4,    5,    6,    7,    1,    8,    9,   10,   11,   12,   13,   14,   15,   16,   17,   18,   19,   20,   21,   22,   23,   24,   25, },
/*   1 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*   2 : */{   -1,    2,    2,   -1,   -1,   -1,   -1,   -1,   -1,    2,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,    2, },
/*   3 : */{   -1,   26,    3,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   27,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   26, },
/*   4 : */{   -1,   -1,   -1,    4,   -1,    4,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*   5 : */{    5,    5,    5,    5,   -1,   -1,   28,    5,    5,    5,    5,    5,    5,    5,    5,    5,    5,    5,    5,    5,    5,    5,    5,    5,    5,    5,    5,    5, },
/*   6 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   29,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*   7 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*   8 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*   9 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   30,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  10 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  11 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  12 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  13 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  14 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  15 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   31,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  16 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   32,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  17 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   33,   -1,   34,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  18 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   35,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  19 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  20 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  21 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   36,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  22 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  23 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   34,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  24 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   37,   -1, },
/*  25 : */{   -1,    2,    2,   -1,   -1,   -1,    5,   -1,   -1,    2,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,    2, },
/*  26 : */{   -1,   26,   26,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   26, },
/*  27 : */{   -1,   -1,   38,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  28 : */{   -1,   -1,   -1,   -1,   -1,   -1,    5,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  29 : */{   29,   29,   29,   29,   29,   -1,   29,   39,   40,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29, },
/*  30 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   41,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  31 : */{   31,   31,   31,   31,   31,   -1,   31,   31,   31,   31,   31,   31,   31,   31,   31,   31,   31,   31,   31,   31,   31,   31,   31,   31,   31,   31,   31,   31, },
/*  32 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  33 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  34 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  35 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  36 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  37 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  38 : */{   -1,   -1,   38,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  39 : */{   29,   29,   29,   29,   29,   -1,   29,   42,   -1,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29, },
/*  40 : */{   29,   29,   29,   29,   29,   -1,   29,   43,   44,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29, },
/*  41 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  42 : */{   29,   29,   29,   29,   29,   -1,   29,   42,   40,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29, },
/*  43 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  44 : */{   29,   29,   29,   29,   29,   -1,   29,   39,   45,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29, },
/*  45 : */{   29,   29,   29,   29,   29,   -1,   29,   46,   45,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29, },
/*  46 : */{   29,   29,   29,   29,   29,   -1,   29,   42,   -1,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29, },
};

/** Attributes (state - attrs (oct)) */
public static final int[] ATTRIBUTES = {
/*   0 */ 0000,
/*   1 */ 0011,
/*   2 */ 0001,
/*   3 */ 0001,
/*   4 */ 0001,
/*   5 */ 0001,
/*   6 */ 0001,
/*   7 */ 0011,
/*   8 */ 0011,
/*   9 */ 0001,
/*  10 */ 0011,
/*  11 */ 0011,
/*  12 */ 0011,
/*  13 */ 0011,
/*  14 */ 0011,
/*  15 */ 0001,
/*  16 */ 0001,
/*  17 */ 0001,
/*  18 */ 0001,
/*  19 */ 0011,
/*  20 */ 0011,
/*  21 */ 0001,
/*  22 */ 0011,
/*  23 */ 0001,
/*  24 */ 0001,
/*  25 */ 0001,
/*  26 */ 0001,
/*  27 */ 0000,
/*  28 */ 0001,
/*  29 */ 0001,
/*  30 */ 0000,
/*  31 */ 0001,
/*  32 */ 0011,
/*  33 */ 0011,
/*  34 */ 0011,
/*  35 */ 0011,
/*  36 */ 0011,
/*  37 */ 0011,
/*  38 */ 0001,
/*  39 */ 0000,
/*  40 */ 0000,
/*  41 */ 0011,
/*  42 */ 0001,
/*  43 */ 0011,
/*  44 */ 0001,
/*  45 */ 0001,
/*  46 */ 0001,
};

/** Actions (state - action) */
public static final int[] ACTIONS = {
/*   0*/    0,
/*   1*/    1,
/*   2*/    2,
/*   3*/    3,
/*   4*/    4,
/*   5*/    5,
/*   6*/    6,
/*   7*/    7,
/*   8*/    8,
/*   9*/    9,
/*  10*/   10,
/*  11*/   11,
/*  12*/   12,
/*  13*/   13,
/*  14*/   14,
/*  15*/   15,
/*  16*/   16,
/*  17*/   17,
/*  18*/   18,
/*  19*/   19,
/*  20*/   20,
/*  21*/   21,
/*  22*/   22,
/*  23*/    1,
/*  24*/    1,
/*  25*/    2,
/*  26*/   23,
/*  27*/    0,
/*  28*/   24,
/*  29*/   25,
/*  30*/    0,
/*  31*/    4,
/*  32*/   26,
/*  33*/   27,
/*  34*/   28,
/*  35*/   29,
/*  36*/   30,
/*  37*/   31,
/*  38*/    3,
/*  39*/    0,
/*  40*/    0,
/*  41*/   32,
/*  42*/   25,
/*  43*/    4,
/*  44*/   25,
/*  45*/   25,
/*  46*/    4,
};

    public static final String[] KEYWORDS = {
        "PARTITION",
        "IMMEDIATE",
        "DISTINCT",
        "EXECUTE",
        "BETWEEN",
        "DECLARE",
        "VALUES",
        "SELECT",
        "DELETE",
        "INSERT",
        "ESCAPE",
        "OVER",
        "UPDATE",
        "EXISTS",
        "HAVING",
        "COMMIT",
        "WHERE",
        "BEGIN",
        "TABLE",
        "ORDER",
        "OR",
        "GROUP",
        "RIGHT",
        "INNER",
        "OUTER",
        "UNION",
        "FROM",
        "WHEN",
        "THEN",
        "CASE",
        "CAST",
        "CALL",
        "ELSE",
        "DESC",
        "LIKE",
        "JOIN",
        "LEFT",
        "NULL",
        "FULL",
        "INTO",
        "AND",
        "SET",
        "END",
        "ASC",
        "XOR",
        "FOR",
        "NOT",
        "ON",
        "BY",
        "AS",
        "IN",
        "IS",
        "UNKNOWN",
        "UNKNOWN_BODY",
        "UNKNOWN_LEX",
        "REAL",
        "ACTION",
        "MIN",
        "LOCAL",
        "SECOND",
        "BIT",
        "OCTET_LENGTH",
        "PRECISION",
        "BOTH",
        "SOME",
        "MINUTE",
        "CROSS",
        "DEFERRED",
        "DEFERRABLE",
        "MONTH",
        "SMALLINT",
        "WITH",
        "NCHAR",
        "ZONE",
        "NO",
        "INTERSECT",
        "COUNT",
        "SESSION_USER",
        "NATURAL",
        "DATE",
        "ALL",
        "DOUBLE",
        "NULLIF",
        "CURRENT_DATE",
        "SUM",
        "CORRESPONDING",
        "UNIQUE",
        "ANY",
        "COLLATE",
        "KEY",
        "AVG",
        "INITIALLY",
        "UPPER",
        "TIMESTAMP",
        "CONSTRAINT",
        "LEADING",
        "NUMERIC",
        "DAY",
        "DECIMAL",
        "DEC",
        "EXCEPT",
        "TRUE",
        "MODULE",
        "EXTRACT",
        "CHAR_LENGTH",
        "TIME",
        "SYSTEM_USER",
        "SUBSTRING",
        "INTEGER",
        "CURRENT_TIME",
        "CREATE",
        "PARTIAL",
        "PRIMARY",
        "CHECK",
        "CHARACTER",
        "USER",
        "CHAR",
        "TIMEZONE_HOUR",
        "REFERENCES",
        "MAX",
        "CURRENT_TIMESTAMP",
        "GLOBAL",
        "LOWER",
        "USING",
        "ROWS",
        "TO",
        "CASCADE",
        "TRAILING",
        "TEMPORARY",
        "HOUR",
        "BIT_LENGTH",
        "INDICATOR",
        "FALSE",
        "VALUE",
        "FOREIGN",
        "YEAR",
        "OVERLAPS",
        "CHARACTER_LENGTH",
        "MATCH",
        "INT",
        "CONVERT",
        "NATIONAL",
        "FLOAT",
        "CURRENT_USER",
        "TRANSLATE",
        "INTERVAL",
        "VARCHAR",
        "DEFAULT",
        "VARYING",
        "PRESERVE",
        "POSITION",
        "COALESCE",
        "TRIM",
        "TIMEZONE_MINUTE",
        "AT",
    };
/** Tokens (action - name)*/
public static final String[] TOKENS = new String[ACTIONS.length];
static {
    TOKENS[  22] = "%";
    TOKENS[  26] = "EQUALSGT";
    TOKENS[  20] = ";";
    TOKENS[  10] = ")";
    TOKENS[  16] = "=";
    TOKENS[  30] = "COLONEQUALS";
    TOKENS[  14] = "+";
    TOKENS[  19] = "?";
    TOKENS[  15] = "-";
    TOKENS[  25] = "MULTILINE_COMMENT_ERR";
    TOKENS[   4] = "";
    TOKENS[   6] = "/";
    TOKENS[  11] = "{";
    TOKENS[  32] = "OUTERJ";
    TOKENS[  21] = ":";
    TOKENS[   9] = "(";
    TOKENS[  27] = "LE";
    TOKENS[   2] = "ID";
    TOKENS[  12] = "}";
    TOKENS[  17] = "<";
    TOKENS[   1] = "UNKNOWN_CHARACTER_ERR";
    TOKENS[   7] = "*";
    TOKENS[  18] = ">";
    TOKENS[  24] = "STRING_SQ";
    TOKENS[  23] = "DIGAL_ERR";
    TOKENS[   3] = "NUMBER";
    TOKENS[   8] = ",";
    TOKENS[  29] = "GE";
    TOKENS[   5] = "STRING_SQ_ERR";
    TOKENS[  31] = "CONCAT";
    TOKENS[  28] = "NE";
    TOKENS[  13] = ".";
}
}
