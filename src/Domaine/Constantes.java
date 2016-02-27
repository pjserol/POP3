package Domaine;

public final class Constantes {
	public static String ADDR_SERVER = "127.0.0.1";
	public static int DEFAULT_PORT_SERVER = 110;
	public static String WELCOME_MESSAGE_SERVER = "{0} {1} server ready.\r\n";
	public static String FOLDER_MAIL = "D:\\Mail\\{0}";
	public static String PASS_FILE = "D:\\Mail\\pass.txt";
	public static String MESSAGE_OK_USER = "{0} user {1}\r\n";
	public static String MESSAGE_RSET_OK = "{0} {1}''s maildrop has {2} messages ({3} octets)\r\n";
	public static String RESPONSE_STAT_OK = "{0} {1} {2}\r\n";
	public static String RESPONSE_LIST_OK = "{0} {1} messages ({2} octets)\r\n{3}.\r\n";
	public static String RESPONSE_LIST_OK_EMPTY = "{0} 0 messages (0 octets)\r\n.\r\n";
	public static String RESPONSE_RETR_OK = "{0} {1} octets\r\n{2}.\r\n";
	public static String RESPONSE_DELE_OK = "{0} message {1} deleted\r\n";
	public static String RESPONSE_NOOP_OK = "+OK";
	public static String RESPONSE_QUIT_OK = "{0} {1} server signing off ({2})\r\n";
	public static String MESSAGE_EMPTY_MAILDROP = "maildrop empty";
	public static String MESSAGE_ERREUR = "-ERR {0}\r\n";
	public static String RESPONSE_QUIT_NOK = "{0} {1} server signing off\r\n";
	public static String ERROR_COMMAND = " unknown command or missing parameter";
	public static String ERROR_PARAM_RETR = "no such message, only {0} messages in maildrop";
	public static String ERROR_WRONG_PASSWORD = "wrong password";
	public static String ERROR_UNKNOWN_USER = "this mailbox doesn't exist";
	public static String ERROR_UNKNOWN_HOST = "The Host is unreachable";
	public static String ERROR_NOT_CONNECTED = "Not connected to a host";
	public static String ERROR_MESSAGE_CLIENT = "Server has returned an error : {0}";
	
	public static String COMMAND_USER = "USER ";
	public static String COMMAND_PASS = "PASS ";
	public static String COMMAND_QUIT = "QUIT";
	public static String COMMAND_STAT = "STAT";
	public static String COMMAND_RETR = "RETR";
	public static String COMMAND_LIST = "LIST";
	
	public static String COMMAND_DELE = "DELE";
	public static String COMMAND_NOOP = "NOOP";
	public static String COMMAND_RSET = "RSET";
}
