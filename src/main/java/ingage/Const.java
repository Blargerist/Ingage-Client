package ingage;

public class Const {

	public static final String WORKING_DIRECTORY = System.getenv("AppData")+"/Ingage Client/";
	public static final String LOG_DIRECTORY = WORKING_DIRECTORY+"logs/";
	public static final String DATA_DIRECTORY = WORKING_DIRECTORY+"data/";
	public static final String INTEGRATIONS_DIRECTORY = Const.DATA_DIRECTORY+"integrations/";
	public static final String ACCOUNTS_DIRECTORY = Const.DATA_DIRECTORY+"accounts/";
	public static final String INTEGRATION_SETTINGS_DIRECTORY = Const.INTEGRATIONS_DIRECTORY+"settings/";
	public static final String STATS_DIRECTORY = Const.DATA_DIRECTORY+"stats/";
	public static final String PROFILES_DIRECTORY = Const.DATA_DIRECTORY+"profiles/";
}
